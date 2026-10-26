package de.imi.mopat.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.enumeration.ExportStatus;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import de.imi.mopat.model.enumeration.ExportTemplateType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EncounterExporterTemplateHL7v2Test {

    private static final String HL7_TEMPLATE_RESOURCE = "HL7_export-test.xml";

    private static final String TEMPLATE_FILENAME = "HL7_export-test.xml";

    private static final String CASE_NUMBER = "CASE-001";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Verifies that the regular HL7v2 file export and the download
     * contain exactly the same content.
     */
    @Test
    public void exportedFileAndDownloadShouldContainSameData() throws Exception {

        // Arrange: create temporary object storage and export directories.
        Path objectStoragePath = temporaryFolder.newFolder("object-storage").toPath();

        Path exportPath = temporaryFolder.newFolder("exports").toPath();

        Path templateDirectory = Path.of(objectStoragePath.toString() + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY);

        Files.createDirectories(templateDirectory);

        // Copy the existing test template into the expected object storage location.
        URL templateResource = getClass().getClassLoader().getResource(HL7_TEMPLATE_RESOURCE);

        assertNotNull("HL7 test template could not be found: " + HL7_TEMPLATE_RESOURCE, templateResource);

        Files.copy(Path.of(templateResource.toURI()), templateDirectory.resolve(TEMPLATE_FILENAME), StandardCopyOption.REPLACE_EXISTING);

        ConfigurationDao configurationDao = mock(ConfigurationDao.class);

        when(configurationDao.getObjectStoragePath()).thenReturn(objectStoragePath.toString());

        Encounter encounter = createEncounter();

        ExportTemplate exportTemplate = createExportTemplate(exportPath);

        EncounterExporterTemplateHL7v2 exporter = new EncounterExporterTemplateHL7v2(configurationDao);

        exporter.load(encounter, exportTemplate);

        // Act: create the regular file export.
        ExportStatus exportStatus = exporter.flush();

        assertEquals(ExportStatus.SUCCESS, exportStatus);

        Path exportedFile = findExportedFile(exportPath, exportTemplate.getQuestionnaire().getName(), exportTemplate.getName());

        String exportedContent = Files.readString(exportedFile, StandardCharsets.UTF_8);

        // Act: create the content used for the download.
        String downloadContent = exporter.getExportContent();

        // Assert: both output paths must produce exactly the same content.
        assertEquals(normalizeHl7Timestamp(downloadContent), normalizeHl7Timestamp(exportedContent));
    }

    private Encounter createEncounter() {

        Bundle bundle = new Bundle("Test Bundle", "Test Bundle Description", 1L, true, true, false);

        Encounter encounter = new Encounter(bundle, CASE_NUMBER);

        encounter.setBundleLanguage("de_DE");

        return encounter;
    }

    private ExportTemplate createExportTemplate(final Path exportPath) {

        Questionnaire questionnaire = new Questionnaire("Test Questionnaire", "Test Questionnaire Description", 1L, true);

        ExportTemplate exportTemplate = new ExportTemplate();

        exportTemplate.setName("HL7 Test Template");
        exportTemplate.setFilename(TEMPLATE_FILENAME);
        exportTemplate.setOriginalFilename("hl7-test-template");
        exportTemplate.setQuestionnaire(questionnaire);
        exportTemplate.setExportTemplateType(ExportTemplateType.HL7v2);
        exportTemplate.setConfigurationGroup(createConfigurationGroup(exportPath));

        return exportTemplate;
    }

    private ConfigurationGroup createConfigurationGroup(final Path exportPath) {

        ConfigurationGroup configurationGroup = new ConfigurationGroup();

        configurationGroup.setName("HL7 Test Configuration");
        configurationGroup.setLabelMessageCode("test.hl7.configuration");

        List<Configuration> configurations = new ArrayList<>();

        configurations.add(config("exportInDirectory", "true"));

        configurations.add(config("exportPath", exportPath.toString()));

        configurations.add(config("exportViaCommunicationServer", "false"));

        configurations.add(config("sendingFacility", "TestSendingFacility"));

        configurations.add(config("receivingApplication", "TestReceivingApplication"));

        configurations.add(config("receivingFacility", "TestReceivingFacility"));

        configurations.add(config("OBRFillerOrderNumber", "TestOrderNumber"));

        configurationGroup.setConfigurations(configurations);

        return configurationGroup;
    }

    private Configuration config(final String attribute, final String value) {

        Configuration configuration = mock(Configuration.class);

        when(configuration.getAttribute()).thenReturn(attribute);

        when(configuration.getValue()).thenReturn(value);

        return configuration;
    }

    private Path findExportedFile(final Path exportPath, final String questionnaireName, final String templateName) throws Exception {

        Path exportDirectory = exportPath.resolve(questionnaireName.replaceAll(":", "_")).resolve(templateName.replaceAll(":", "_"));

        try (var files = Files.list(exportDirectory)) {
            return files.filter(Files::isRegularFile).findFirst().orElseThrow(() -> new AssertionError("No HL7 export file found in " + exportDirectory));
        }
    }

    private String normalizeHl7Timestamp(final String content) {
        return content.replaceFirst("\\|\\d{14}\\.\\d{3}[+-]\\d{4}\\|", "|<TIMESTAMP>|");
    }
}