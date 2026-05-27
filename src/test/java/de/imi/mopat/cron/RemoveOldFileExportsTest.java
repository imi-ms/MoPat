package de.imi.mopat.cron;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoveOldFileExportsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private ConfigurationDao configurationDao;

    @Mock
    private ConfigurationGroupDao configurationGroupDao;

    @Mock
    private AuditEntryDao auditEntryDao;

    @InjectMocks
    private RemoveOldFileExports removeOldFileExports;

    @Test
    public void deleteFiles_shouldDeleteOnlyOldMatchingFiles() throws Exception {
        File exportDir = temporaryFolder.newFolder("exports");
        prepareFiles(exportDir);

        when(configurationDao.getFileDeletionTimeWindow()).thenReturn(30L * 24 * 60 * 60 * 1000);

        Configuration configuration = mock(Configuration.class);
        when(configuration.getAttribute()).thenReturn("exportPath");
        when(configuration.getValue()).thenReturn(exportDir.getAbsolutePath());

        ConfigurationGroup group = mock(ConfigurationGroup.class);
        when(group.getConfigurations()).thenReturn(List.of(configuration));

        when(configurationGroupDao.getAllConfigurationGroups()).thenReturn(List.of(group));

        removeOldFileExports.deleteFiles();

        File[] remainingFiles = exportDir.listFiles();
        assertNotNull(remainingFiles);

        long oldFilesRemaining = Arrays.stream(remainingFiles)
            .filter(File::isFile)
            .map(File::getName)
            .map(this::extractCaseNumberForTest)
            .filter(caseNumber -> caseNumber != null && caseNumber.startsWith("1000"))
            .count();

        long recentFilesRemaining = Arrays.stream(remainingFiles)
            .filter(File::isFile)
            .map(File::getName)
            .map(this::extractCaseNumberForTest)
            .filter(caseNumber -> caseNumber != null && caseNumber.startsWith("2000"))
            .count();


        assertEquals(0, oldFilesRemaining);
        assertEquals(30, recentFilesRemaining);

        ArgumentCaptor<List> caseNumbersCaptor = ArgumentCaptor.forClass(List.class);

        verify(auditEntryDao).writeAuditEntries(
            eq("RemoveOldFileExports"),
            eq("deleteFiles()"),
            caseNumbersCaptor.capture(),
            anySet(),
            eq(AuditEntryActionType.DELETE)
        );

        assertEquals(30, caseNumbersCaptor.getValue().size());


    }

    private void prepareFiles(File exportDir) throws Exception {
        Random random = new Random(42);

        for (int i = 0; i < 30; i++) {
            LocalDateTime oldTimestamp = randomDateTimeBetweenDaysAgo(random, 31, 90);
            File oldFile = new File(exportDir, buildFilename("1000" + i, oldTimestamp));
            Files.write(oldFile.toPath(), Collections.singletonList("old"));

            LocalDateTime recentTimestamp = randomDateTimeBetweenDaysAgo(random, 0, 29);
            File recentFile = new File(exportDir, buildFilename("2000" + i, recentTimestamp));
            Files.write(recentFile.toPath(), Collections.singletonList("recent"));
        }
    }

    private LocalDateTime randomDateTimeBetweenDaysAgo(Random random, int minDaysAgo, int maxDaysAgo) {
        int daysAgo = minDaysAgo + random.nextInt(maxDaysAgo - minDaysAgo + 1);
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);

        return LocalDate.now()
            .minusDays(daysAgo)
            .atTime(hour, minute, second);
    }

    private String buildFilename(String caseNumber, LocalDateTime timestamp) {
        String formattedTimestamp = Constants.EXPORT_DATE_FORMAT.format(
            java.sql.Timestamp.valueOf(timestamp)
        );
        return caseNumber + "_someExport_" + formattedTimestamp + ".xml";
    }

    private String extractCaseNumberForTest(String filename) {
        int underscore = filename.indexOf('_');
        if (underscore <= 0) {
            return null;
        }
        return filename.substring(0, underscore);
    }
}