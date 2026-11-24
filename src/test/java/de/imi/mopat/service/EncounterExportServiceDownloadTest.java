package de.imi.mopat.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.io.EncounterExporter;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;


/**
 * Tests the download-content generation for encounter export templates.
 * <p>
 * These tests verify that export content can be generated on demand for frontend downloads without
 * executing the regular export flow. In particular, they ensure that no export file is written, no
 * external transmission is triggered and no export history entry is created.
 */
public class EncounterExportServiceDownloadTest {

    private static final String CASE_NUMBER = "CASE-RED-001";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ConfigurationDao configurationDao;
    private EncounterDao encounterDao;
    private MessageSource messageSource;

    private EncounterExportService encounterExportService;

    @Before
    public void setUp() {
        configurationDao = mock(ConfigurationDao.class);
        encounterDao = mock(EncounterDao.class);
        messageSource = mock(MessageSource.class);

        EncounterExporter encounterExporter = new EncounterExporter();
        ReflectionTestUtils.setField(encounterExporter, "configurationDao", configurationDao);
        ReflectionTestUtils.setField(encounterExporter, "encounterDao", encounterDao);
        ReflectionTestUtils.setField(encounterExporter, "messageSource", messageSource);

        encounterExportService = new EncounterExportService();
        ReflectionTestUtils.setField(encounterExportService, "encounterExporter", encounterExporter);
    }

    @Test
    public void getExportContent_redcap_returnsValidJsonWithoutSideEffects() throws Exception {
        File objectStorageRoot = temporaryFolder.newFolder("object-storage");

        when(configurationDao.getObjectStoragePath())
            .thenReturn(objectStorageRoot.getAbsolutePath());

        File templateDirectory = new File(
            objectStorageRoot.getAbsolutePath() + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY);
        assertTrue(templateDirectory.mkdirs());

        String templateFilename = "redcap-test-template.json";

        String redcapTemplateJson = """
            [
              {
                "record_id": "",
                "redcap_repeat_instrument": "",
                "redcap_repeat_instance": "",
                "redcap_event_name": "",
                "redcap_data_access_group": "",
                "test_field": "",
                "test_form_complete": ""
              }
            ]
            """;

        Files.writeString(
            new File(templateDirectory, templateFilename).toPath(),
            redcapTemplateJson,
            StandardCharsets.UTF_8
        );

        ConfigurationGroup configurationGroup = configurationGroup(
            config("exportInDirectory", "true", 1),
            config("exportPath", new File(objectStorageRoot, "exports").getAbsolutePath(), 2),
            config("exportViaRest", "false", 3)
        );

        TestFixture fixture = createFixture(
            ExportTemplateType.REDCap,
            templateFilename,
            "redcap-test-template",
            configurationGroup
        );

        Encounter encounter = fixture.encounter();
        ExportTemplate exportTemplate = fixture.exportTemplate();

        int historyEntriesBefore = encounter.getEncounterExportTemplates().size();
        long fileCountBefore = countFiles(objectStorageRoot.toPath());

        String exportContent = encounterExportService.getExportContent(encounter, exportTemplate);

        assertNotNull(exportContent);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(exportContent);

        assertTrue("REDCap export content should be a JSON array", root.isArray());
        assertEquals("REDCap JSON array should contain exactly one record", 1, root.size());

        JsonNode firstRecord = root.get(0);

        assertEquals(CASE_NUMBER, firstRecord.get("record_id").asText());
        assertEquals("1", firstRecord.get("test_form_complete").asText());

        assertEquals(
            "Download content generation must not create export history entries",
            historyEntriesBefore,
            encounter.getEncounterExportTemplates().size()
        );

        assertEquals(
            "Download content generation must not create additional files",
            fileCountBefore,
            countFiles(objectStorageRoot.toPath())
        );

        verify(encounterDao, never()).merge(any(Encounter.class));
    }

    private TestFixture createFixture(
        final ExportTemplateType exportTemplateType,
        final String filename,
        final String originalFilename,
        final ConfigurationGroup configurationGroup
    ) {
        Questionnaire questionnaire = new Questionnaire(
            "Test Questionnaire",
            "Test Questionnaire Description",
            1L,
            true
        );

        Bundle bundle = new Bundle(
            "Test Bundle",
            "Test Bundle Description",
            1L,
            true,
            true,
            false
        );

        BundleQuestionnaire bundleQuestionnaire = new BundleQuestionnaire(
            bundle,
            questionnaire,
            1,
            true,
            false
        );

        Encounter encounter = new Encounter(bundle, CASE_NUMBER);
        encounter.setBundleLanguage("de_DE");

        ExportTemplate exportTemplate = new ExportTemplate();
        exportTemplate.setName("Test Export Template");
        exportTemplate.setExportTemplateType(exportTemplateType);
        exportTemplate.setFilename(filename);
        exportTemplate.setOriginalFilename(originalFilename);
        exportTemplate.setConfigurationGroup(configurationGroup);
        exportTemplate.setQuestionnaire(questionnaire);

        bundleQuestionnaire.addExportTemplate(exportTemplate);

        return new TestFixture(encounter, exportTemplate);
    }

    private ConfigurationGroup configurationGroup(final Configuration... configurations) {
        ConfigurationGroup configurationGroup = new ConfigurationGroup();
        configurationGroup.setName("Test Configuration Group");
        configurationGroup.setLabelMessageCode("test.configuration.group");
        configurationGroup.setConfigurations(new ArrayList<>());

        for (Configuration configuration : configurations) {
            configurationGroup.getConfigurations().add(configuration);
        }

        return configurationGroup;
    }

    private Configuration config(
        final String attribute,
        final String value,
        final Integer position
    ) {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getAttribute()).thenReturn(attribute);
        when(configuration.getValue()).thenReturn(value);
        when(configuration.getPosition()).thenReturn(position);
        return configuration;
    }

    private long countFiles(final Path root) throws Exception {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                .filter(Files::isRegularFile)
                .count();
        }
    }

    private record TestFixture(
        Encounter encounter,
        ExportTemplate exportTemplate
    ) {
    }
}