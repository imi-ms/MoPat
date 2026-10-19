package de.imi.mopat.io;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.io.impl.EncounterExporterTemplateREDCap;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Questionnaire;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.test.util.ReflectionTestUtils;

public class EncounterExporterTemplateREDCapTest {

    /**
     * Provides a temporary directory for the exported test file.
     * The directory is automatically removed after the test.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Verifies that the content written by the regular file export
     * is identical to the content returned for manual direct download.
     *
     * @throws Exception if preparing or writing the export fails
     */
    @Test
    public void exportedFileAndDownloadContentShouldBeEqual() throws Exception {

        // Arrange: create dependencies required by the REDCap exporter
        ConfigurationDao configurationDao = mock(ConfigurationDao.class);
        Encounter encounter = mock(Encounter.class);
        ExportTemplate exportTemplate = mock(ExportTemplate.class);
        Questionnaire questionnaire = mock(Questionnaire.class);

        // Define values required to construct export path and filename
        when(encounter.getCaseNumber()).thenReturn("test");
        when(questionnaire.getName()).thenReturn("TestQuestionnaire");
        when(exportTemplate.getQuestionnaire()).thenReturn(questionnaire);
        when(exportTemplate.getName()).thenReturn("TestTemplate");
        when(exportTemplate.getOriginalFilename()).thenReturn("test-export");

        // Create actual REDCap exporter
        EncounterExporterTemplateREDCap exporter =
                new EncounterExporterTemplateREDCap(configurationDao);

        //Set internal exporter state directly
        ReflectionTestUtils.setField(exporter, "encounter", encounter);
        ReflectionTestUtils.setField(exporter, "exportTemplate", exportTemplate);
        ReflectionTestUtils.setField(
                exporter,
                "exportJSON",
                Map.of("record_id", "test")
        );

        // Use temporary directory as the destination for the regular file export
        String exportPath = temporaryFolder.getRoot().getAbsolutePath();

        // Act: create regular export file
        exporter.exportToDirectory(exportPath);

        Path exportDirectory = Path.of(
                exportPath,
                "TestQuestionnaire",
                "TestTemplate"
        );

        // Locate file
        Path exportedFile;

        try (var files = Files.list(exportDirectory)) {
            exportedFile = files.findFirst().orElseThrow();
        }

        // Read content
        String exportedContent = Files.readString(
                exportedFile,
                StandardCharsets.UTF_8
        );

        // Act: generate the download-content
        String downloadContent = exporter.getExportContent();

        // Assert that both output paths produce exactly the same content
        assertEquals(downloadContent, exportedContent);
    }
}