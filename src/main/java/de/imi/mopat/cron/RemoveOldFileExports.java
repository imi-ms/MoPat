package de.imi.mopat.cron;

import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RemoveOldFileExports {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        RemoveOldFileExports.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(
        Constants.EXPORT_DATE_FORMAT.toPattern()
    );
    private Long fileDeletionThresholdInMillis;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private ConfigurationGroupDao configurationGroupDao;

    @Autowired
    private AuditEntryDao auditEntryDao;


    /**
     * Scheduled job that deletes exported files whose embedded filename timestamp is older than the
     * configured deletion threshold.
     */
    @Scheduled(cron = "${de.imi.mopat.cron.RemoveOldFileExports.checkTime}")
    public void deleteFiles() {
        initDeletionWindow();

        Set<String> deletedFiles = new HashSet<>();

        for (String filepath : getFilepathsForFilesToDelete()) {
            File file = new File(filepath);
            if (file.isFile()) {

                LOGGER.info("Delete old file export: {}", file.getAbsolutePath());
                deletedFiles.add(file.getName());
                file.delete();
            }
        }

        if (!deletedFiles.isEmpty()) {
            writeAuditLog(getCaseNumbersForDeletedFiles(deletedFiles));
        }
    }

    /**
     * Initializes the file deletion threshold from configuration.
     */
    private void initDeletionWindow() {
        this.fileDeletionThresholdInMillis = configurationDao.getFileDeletionTimeWindow();
    }

    /**
     * Returns all configured export directories from configuration groups using the attribute
     * {@code exportPath}.
     *
     * @return a list of configured export directory paths
     */
    private List<String> getExportDirectories() {
        List<String> exportDirectories = new ArrayList<>();

        for (ConfigurationGroup group : configurationGroupDao.getAllConfigurationGroups()) {
            if (group == null || group.getConfigurations() == null) {
                continue;
            }

            for (Configuration configuration : group.getConfigurations()) {
                if (configuration != null && "exportPath".equals(configuration.getAttribute())) {
                    exportDirectories.add(configuration.getValue());
                }
            }
        }

        return exportDirectories;
    }


    /**
     * Traverses the given directory recursively and collects absolute file paths for files whose
     * filename timestamp is older than the configured threshold.
     *
     * @param directory     the directory to traverse
     * @param filesToDelete the list collecting file paths marked for deletion
     */
    private void processDirectoryRecursively(File directory, List<String> filesToDelete) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectoryRecursively(file, filesToDelete);
            } else if (isFilenameOlderThanThreshold(file.getName())) {
                filesToDelete.add(file.getAbsolutePath());
            }
        }
    }

    /**
     * Collects absolute file paths of all files eligible for deletion from all configured export
     * directories.
     *
     * @return a list of absolute file paths for files to delete
     */
    private List<String> getFilepathsForFilesToDelete() {
        List<String> filepathsToDelete = new ArrayList<>();

        for (String exportDirectory : getExportDirectories()) {
            processDirectoryRecursively(new File(exportDirectory), filepathsToDelete);
        }

        return filepathsToDelete;
    }

    /**
     * Extracts the timestamp portion from the filename based on the configured export date format.
     * Assumes the timestamp is located directly before the file extension.
     *
     * @param filename The filename to extract the timestampt from.
     *
     * @return The timestamp from the filename as String.
     */
    private String extractTimestampFromFilename(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }

        String pattern = Constants.EXPORT_DATE_FORMAT.toPattern();
        int timestampLength = pattern.length();

        if (dot < timestampLength) {
            return null;
        }

        int start = dot - timestampLength;
        if (start < 0 || start >= dot) {
            return null;
        }

        String candidate = filename.substring(start, dot);

        try {
            Constants.EXPORT_DATE_FORMAT.parse(candidate);
            return candidate;
        } catch (java.text.ParseException e) {
            return null;
        }
    }


    /**
     * Checks whether the timestamp embedded in the given filename is older than the configured
     * deletion threshold.
     *
     * @param filename the filename to inspect
     * @return {@code true} if the filename timestamp is older than the threshold, otherwise
     * {@code false}
     */
    private Boolean isFilenameOlderThanThreshold(String filename) {
        String timestamp = extractTimestampFromFilename(filename);
        if (timestamp == null) {
            return false;
        }

        try {
            LocalDate fileDate = LocalDate.parse(timestamp, FORMATTER);
            long fileMillis = fileDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                .toEpochMilli();
            long nowMillis = System.currentTimeMillis();
            return nowMillis - fileMillis > fileDeletionThresholdInMillis;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void writeAuditLog(List<String> deletedCaseNumbers) {
        Set<AuditPatientAttribute> patientAttributes = new HashSet<>(Arrays.asList(
            AuditPatientAttribute.PATIENT_ID,
            AuditPatientAttribute.TREATMENT_DATA,
            AuditPatientAttribute.CASE_NUMBER
        ));

        try {
            auditEntryDao.writeAuditEntries(this.getClass().getSimpleName(),
                "deleteFiles()", deletedCaseNumbers, patientAttributes,
                AuditEntryActionType.DELETE);
        } catch (Exception e) {
            LOGGER.error("Something went wrong while writing audit logs of deleted old Encounters "
                + "and old EncounterScheduleds. Since this is important for having a complete "
                + "audit log, investigate this error ASAP", e);
        }
    }

    /**
     * Processes the list of deleted filenames and returns a set of
     * case numbers extracted from the filenames.
     * Normally, case numbers are always in the beginning of
     * exported files.
     *
     * @param deletedFilenames to extract case numbers from
     * @return List with case numbers.
     */
    private List<String> getCaseNumbersForDeletedFiles(Set<String> deletedFilenames) {
        List<String> caseNumbers = new ArrayList<>();
        for (String deletedFilename : deletedFilenames) {
            caseNumbers.add(getCaseNumberFromFilename(deletedFilename));
        }
        return caseNumbers;
    }

    /**
     * Extracts the case number from a filename.
     * MoPat always exports files with the leading case number until the first "_"
     * @param filename to extract the case number from
     * @return case number from the exported file
     */
    private String getCaseNumberFromFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        int underscore = filename.indexOf('_');
        if (underscore <= 0) {
            return null;
        }

        return filename.substring(0, underscore);
    }
}
