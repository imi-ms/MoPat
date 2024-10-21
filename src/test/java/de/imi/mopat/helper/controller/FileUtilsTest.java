package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConfigurationDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

public class FileUtilsTest {

    @Mock
    private ConfigurationDao configurationDao;

    @InjectMocks
    private FileUtils fileUtils;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCopyTemplateFile_success() throws Exception {
        // Arrange
        when(configurationDao.getObjectStoragePath()).thenReturn("/tmp/storage");
        String sourceFileName = "template1.json";
        String destinationFileName = "template1_copy.json";

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            // Act
            fileUtils.copyTemplateFile(sourceFileName, destinationFileName);

            // Verify that the Files.copy method was called with correct arguments
            mockedFiles.verify(() -> Files.copy(any(Path.class), any(Path.class), any(StandardCopyOption.class)));
        }
    }

    @Test(expected = IOException.class)
    public void testCopyTemplateFile_failure() throws Exception {
        // Arrange
        when(configurationDao.getObjectStoragePath()).thenReturn("/tmp/storage");
        String sourceFileName = "template_ERROR.json";
        String destinationFileName = "template_copy.json";
        Path sourcePath = Paths.get("/tmp/storage/templates", sourceFileName);
        Path destinationPath = Paths.get("/tmp/storage/templates", destinationFileName);

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING))
                    .thenThrow(new IOException("Test"));

            // Act
            fileUtils.copyTemplateFile(sourceFileName, destinationFileName);
        }
    }

    @Test
    public void testDeleteExportTemplateFrom_success() {
        // Arrange
        when(configurationDao.getObjectStoragePath()).thenReturn("/tmp/storage");
        String fileName = "template1.json";
        Path filePath = Paths.get("/tmp/storage/templates", fileName);

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(filePath)).thenReturn(true);

            // Act
            fileUtils.deleteExportTemplateFrom(fileName);

            // Verify that the file was deleted
            mockedFiles.verify(() -> Files.deleteIfExists(any(Path.class)));
        }
    }

    @Test
    public void testDeleteExportTemplateFrom_nullFileName() {
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            // Act
            fileUtils.deleteExportTemplateFrom(null);

            // Verify that nothing was attempted since the filename was null
            mockedFiles.verify(() -> Files.deleteIfExists(any(Path.class)), never());
        }
    }
}