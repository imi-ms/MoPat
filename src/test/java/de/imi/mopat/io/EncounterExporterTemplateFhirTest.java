package de.imi.mopat.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.io.EncounterExporterTemplate;
import de.imi.mopat.io.impl.EncounterExporterTemplateFhirR4b;
import de.imi.mopat.io.impl.EncounterExporterTemplateFhirR5;
import de.imi.mopat.io.impl.EncounterExporterTemplateFhirDstu3;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.enumeration.ExportStatus;
import de.imi.mopat.model.enumeration.ExportTemplateType;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EncounterExporterTemplateFhirTest {

    private static final String CASE_NUMBER = "CASE-001";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void exportedDstu3FileAndDownloadShouldContainSameData()
            throws Exception {

        assertFileExportEqualsDownload(
                "FHIRDSTU3_exportTemplate_test.xml",
                ExportTemplateType.FHIR_DSTU3,
                configurationDao ->
                        new EncounterExporterTemplateFhirDstu3(configurationDao)
        );
    }

    @Test
    public void exportedR4bFileAndDownloadShouldContainSameData()
            throws Exception {

        assertFileExportEqualsDownload(
                "FHIRR4B_exportTemplate_test.xml",
                ExportTemplateType.FHIR_R4B,
                configurationDao ->
                        new EncounterExporterTemplateFhirR4b(configurationDao)
        );
    }

    @Test
    public void exportedR5FileAndDownloadShouldContainSameData()
            throws Exception {

        assertFileExportEqualsDownload(
                "FHIRR5_exportTemplate_test.xml",
                ExportTemplateType.FHIR_R5,
                configurationDao ->
                        new EncounterExporterTemplateFhirR5(configurationDao)
        );
    }

    private void assertFileExportEqualsDownload(
            final String templateFilename,
            final ExportTemplateType exportTemplateType,
            final ExporterFactory exporterFactory)
            throws Exception {

        Path objectStoragePath =
                temporaryFolder.newFolder("object-storage").toPath();

        Path exportPath =
                temporaryFolder.newFolder("exports").toPath();

        Path templateDirectory = Path.of(
                objectStoragePath.toString()
                        + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY
        );

        Files.createDirectories(templateDirectory);

        URL templateResource = getClass()
                .getClassLoader()
                .getResource("export-control-files/" + templateFilename);

        assertNotNull(
                "FHIR test template could not be found: "
                        + templateFilename,
                templateResource
        );

        Files.copy(
                Path.of(templateResource.toURI()),
                templateDirectory.resolve(templateFilename),
                StandardCopyOption.REPLACE_EXISTING
        );

        ConfigurationDao configurationDao =
                mock(ConfigurationDao.class);

        when(configurationDao.getObjectStoragePath())
                .thenReturn(objectStoragePath.toString());

        Encounter encounter =
                createEncounter();

        ExportTemplate exportTemplate =
                createExportTemplate(
                        exportPath,
                        templateFilename,
                        exportTemplateType
                );

        EncounterExporterTemplate exporter =
                exporterFactory.create(configurationDao);

        exporter.load(
                encounter,
                exportTemplate
        );

        ExportStatus exportStatus =
                exporter.flush();

        assertEquals(
                ExportStatus.SUCCESS,
                exportStatus
        );

        Path exportedFile =
                findExportedFile(
                        exportPath,
                        exportTemplate.getQuestionnaire().getName(),
                        exportTemplate.getName()
                );

        String exportedContent =
                Files.readString(
                        exportedFile,
                        StandardCharsets.UTF_8
                );

        String downloadContent =
                exporter.getExportContent();

        assertEquals(
                downloadContent,
                exportedContent
        );
    }

    private Encounter createEncounter() {

        Bundle bundle = new Bundle(
                "Test Bundle",
                "Test Bundle Description",
                1L,
                true,
                true,
                false
        );

        Encounter encounter =
                new Encounter(
                        bundle,
                        CASE_NUMBER
                );

        encounter.setBundleLanguage("de_DE");

        return encounter;
    }

    private ExportTemplate createExportTemplate(
            final Path exportPath,
            final String templateFilename,
            final ExportTemplateType exportTemplateType) {

        Questionnaire questionnaire =
                new Questionnaire(
                        "Test Questionnaire",
                        "Test Questionnaire Description",
                        1L,
                        true
                );

        ExportTemplate exportTemplate =
                new ExportTemplate();

        exportTemplate.setName(
                "FHIR Test Template"
        );

        exportTemplate.setFilename(
                templateFilename
        );

        exportTemplate.setOriginalFilename(
                "fhir-test-template"
        );

        exportTemplate.setQuestionnaire(
                questionnaire
        );

        exportTemplate.setExportTemplateType(
                exportTemplateType
        );

        exportTemplate.setConfigurationGroup(
                createConfigurationGroup(
                        exportPath
                )
        );

        return exportTemplate;
    }

    private ConfigurationGroup createConfigurationGroup(
            final Path exportPath) {

        ConfigurationGroup configurationGroup =
                new ConfigurationGroup();

        configurationGroup.setName(
                "FHIR Test Configuration"
        );

        configurationGroup.setLabelMessageCode(
                "test.fhir.configuration"
        );

        List<Configuration> configurations =
                new ArrayList<>();

        configurations.add(
                config(
                        "exportInDirectory",
                        "true"
                )
        );

        configurations.add(
                config(
                        "exportPath",
                        exportPath.toString()
                )
        );

        configurations.add(
                config(
                        "exportViaRest",
                        "false"
                )
        );

        configurationGroup.setConfigurations(
                configurations
        );

        return configurationGroup;
    }

    private Configuration config(
            final String attribute,
            final String value) {

        Configuration configuration =
                mock(Configuration.class);

        when(configuration.getAttribute())
                .thenReturn(attribute);

        when(configuration.getValue())
                .thenReturn(value);

        return configuration;
    }

    private Path findExportedFile(
            final Path exportPath,
            final String questionnaireName,
            final String templateName)
            throws Exception {

        Path exportDirectory =
                exportPath
                        .resolve(
                                questionnaireName.replaceAll(
                                        ":",
                                        "_"
                                )
                        )
                        .resolve(
                                templateName.replaceAll(
                                        ":",
                                        "_"
                                )
                        );

        try (var files =
                     Files.list(exportDirectory)) {

            return files
                    .filter(
                            Files::isRegularFile
                    )
                    .findFirst()
                    .orElseThrow(
                            () ->
                                    new AssertionError(
                                            "No FHIR export file found in "
                                                    + exportDirectory
                                    )
                    );
        }
    }

    @FunctionalInterface
    private interface ExporterFactory {

        EncounterExporterTemplate create(
                ConfigurationDao configurationDao
        );
    }
}