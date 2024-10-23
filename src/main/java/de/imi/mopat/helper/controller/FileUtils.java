package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.model.ExportTemplate;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class FileUtils {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(FileUtils.class);

    private final ConfigurationDao configurationDao;

    @Autowired
    public FileUtils(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    /**
     * Copies the file associated with the export template from the source location to a new destination.
     * This is typically used when copying templates between questionnaires to maintain a backup of the original file.
     *
     * @param sourceFileName the name of the file to be copied.
     * @param destinationFileName the name of the destination file where the source file will be copied to.
     * @throws IOException if an error occurs during the file copying process.
     */
    public void copyTemplateFile(String sourceFileName, String destinationFileName) throws IOException {
        String objectStoragePath = configurationDao.getObjectStoragePath();
        String contextPath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;

        Path sourcePath = Paths.get(contextPath, sourceFileName);
        Path targetPath = Paths.get(contextPath, destinationFileName);

        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Generates a new filename for an {@link ExportTemplate} by appending the template's ID to the existing file name.
     * This ensures that the new filename is unique and can be used for storing the copied template.
     *
     * @param originalTemplateFilename the original file name of the export template.
     * @param newExportTemplateId the unique ID of the newly created export template.
     * @return a string representing the new file name for the export template, including the ID.
     */
    public String generateFileNameForExportTemplate(String originalTemplateFilename, Long newExportTemplateId){

        // Get base name and extension
        String baseName = FilenameUtils.getBaseName(originalTemplateFilename);
        String extension = FilenameUtils.getExtension(originalTemplateFilename);

        // Replace number before underscore or prepend new ID if none exists
        int firstUnderscoreIndex = baseName.indexOf('_');
        if (firstUnderscoreIndex != -1) {
            baseName = newExportTemplateId + baseName.substring(firstUnderscoreIndex);
        } else {
            baseName = newExportTemplateId + "_" + baseName;
        }

        // Return the new file name with the extension
        return extension.isEmpty() ? baseName : baseName + "." + extension;
    }

    /**
     * Deletes the file associated with the provided export template filename from the filesystem if it exists.
     * This method checks if the provided filename is null and skips deletion if so. It constructs the full path
     * using the object storage path and export template subdirectory, then attempts to delete the file if it exists.
     * If the file cannot be deleted or an I/O error occurs during deletion, the error is logged.
     *
     * @param fileName the name of the file to be deleted from the filesystem. If null, no action is taken.
     */
    public void deleteExportTemplateFrom(String fileName) {
        if (fileName == null) {
            return;
        }

        String objectStoragePath = configurationDao.getObjectStoragePath();
        String contextPath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
        Path filePath = Paths.get(contextPath, fileName);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            LOGGER.error("Failed to delete file for export template: {}", e.getMessage());
        }
    }
}