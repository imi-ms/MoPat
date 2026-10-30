package de.imi.mopat.helper.controller;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.imi.mopat.controller.EncounterController;
import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.service.EncounterExportService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.support.SessionFlashMapManager;


public class EncounterControllerDownloadExportTest {

    private static final Long ENCOUNTER_ID = 11L;
    private static final Long TEMPLATE_ID = 22L;
    private static final String CASE_NUMBER = "CASE-001";
    private static final String EXPORT_CONTENT = "[{\"record_id\":\"CASE-001\"}]";

    private EncounterController encounterController;

    private ConfigurationDao configurationDao;
    private EncounterDao encounterDao;
    private ExportTemplateDao exportTemplateDao;
    private EncounterExportService encounterExportService;
    private AuditEntryDao auditEntryDao;
    private MockHttpServletRequest httpRequest;
    private MockHttpServletResponse httpResponse;

    @Before
    public void setUp() {
        configurationDao = mock(ConfigurationDao.class);
        encounterDao = mock(EncounterDao.class);
        exportTemplateDao = mock(ExportTemplateDao.class);
        encounterExportService = mock(EncounterExportService.class);
        auditEntryDao = mock(AuditEntryDao.class);

        encounterController = new EncounterController();

        ReflectionTestUtils.setField(encounterController, "configurationDao", configurationDao);
        ReflectionTestUtils.setField(encounterController, "encounterDao", encounterDao);
        ReflectionTestUtils.setField(encounterController, "exportTemplateDao", exportTemplateDao);
        ReflectionTestUtils.setField(encounterController, "encounterExportService", encounterExportService);
        ReflectionTestUtils.setField(encounterController, "auditEntryDao", auditEntryDao);
        httpRequest = new MockHttpServletRequest();
        httpRequest.setAttribute(DispatcherServlet.OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
        httpRequest.setAttribute(DispatcherServlet.FLASH_MAP_MANAGER_ATTRIBUTE, new SessionFlashMapManager());
        httpResponse = new MockHttpServletResponse();
    }

    /**
     * Verifies that a download request returns the generated export content as a file response when
     * frontend downloads are enabled and file export is enabled for the export template.
     * <p>
     * The test checks the HTTP status, response body, content type, content-disposition header and that
     * an audit entry is written.
     *
     * @throws Exception if generating or returning the export content fails
     */
    @Test
    public void downloadEncounterTemplate_whenDownloadEnabledAndFileExportEnabled_returnsFile() throws Exception {
        Encounter encounter = createEncounter();
        ExportTemplate exportTemplate = createExportTemplate(true);

        when(configurationDao.isEncounterTemplateDownloadEnabled()).thenReturn(true);
        when(encounterDao.getElementById(ENCOUNTER_ID)).thenReturn(encounter);
        when(exportTemplateDao.getElementById(TEMPLATE_ID)).thenReturn(exportTemplate);
        when(encounterExportService.getExportContent(encounter, exportTemplate)).thenReturn(EXPORT_CONTENT);

        ResponseEntity<byte[]> response = encounterController.downloadEncounterTemplate(ENCOUNTER_ID, TEMPLATE_ID, httpRequest, httpResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNotNull(response.getBody());
        assertArrayEquals(EXPORT_CONTENT.getBytes(StandardCharsets.UTF_8), response.getBody());

        MediaType expectedContentType = new MediaType("application", "json", StandardCharsets.UTF_8);

        assertEquals(expectedContentType, response.getHeaders().getContentType());

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);

        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("attachment"));
        assertTrue(contentDisposition.contains("filename=\"CASE-001_redcap-template.json\""));

        verify(encounterExportService).getExportContent(encounter, exportTemplate);

        verify(auditEntryDao).writeAuditEntry(eq(EncounterController.class.getSimpleName()), eq("downloadEncounterTemplate(" + ENCOUNTER_ID + ", " + TEMPLATE_ID + ")"), eq(CASE_NUMBER), anySet(), eq(AuditEntryActionType.READ));
    }

    /**
     * Verifies that the download endpoint rejects requests when frontend export downloads are globally
     * disabled.
     * <p>
     * The test ensures that no encounter or export template is loaded, no export content is generated
     * and no audit entry is written.
     *
     * @throws Exception if the controller invocation fails
     */
    @Test
    public void downloadEncounterTemplate_whenDownloadConfigDisabled_returnsForbidden() throws Exception {
        when(configurationDao.isEncounterTemplateDownloadEnabled()).thenReturn(false);

        ResponseEntity<byte[]> response = encounterController.downloadEncounterTemplate(ENCOUNTER_ID, TEMPLATE_ID, httpRequest, httpResponse);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(encounterDao, never()).getElementById(any());
        verify(exportTemplateDao, never()).getElementById(any());
        verify(encounterExportService, never()).getExportContent(any(), any());
        verify(auditEntryDao, never()).writeAuditEntry(any(), any(), any(), any(), any());
    }

    /**
     * Verifies that the download endpoint rejects requests when the selected export template does not
     * have file export enabled.
     * <p>
     * The test ensures that no export content is generated and no audit entry is written.
     *
     * @throws Exception if the controller invocation fails
     */
    @Test
    public void downloadEncounterTemplate_whenFileExportDisabled_returnsForbidden() throws Exception {
        Encounter encounter = createEncounter();
        ExportTemplate exportTemplate = createExportTemplate(false);

        when(configurationDao.isEncounterTemplateDownloadEnabled()).thenReturn(true);
        when(encounterDao.getElementById(ENCOUNTER_ID)).thenReturn(encounter);
        when(exportTemplateDao.getElementById(TEMPLATE_ID)).thenReturn(exportTemplate);

        ResponseEntity<byte[]> response = encounterController.downloadEncounterTemplate(ENCOUNTER_ID, TEMPLATE_ID, httpRequest, httpResponse);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(encounterExportService, never()).getExportContent(any(), any());
        verify(auditEntryDao, never()).writeAuditEntry(any(), any(), any(), any(), any());
    }

    /**
     * Verifies that a failed export generation does not propagate the exception to the user.
     * <p>
     * The test ensures that the request is redirected back to the encounter page, that the id of the
     * failed export template is passed as flash attribute and that no audit entry is written.
     *
     * @throws Exception if the controller invocation fails
     */
    @Test
    public void downloadEncounterTemplate_whenExportFails_redirectsWithFailedTemplateId() throws Exception {
        Encounter encounter = createEncounter();
        ExportTemplate exportTemplate = createExportTemplate(true);

        when(configurationDao.isEncounterTemplateDownloadEnabled()).thenReturn(true);
        when(encounterDao.getElementById(ENCOUNTER_ID)).thenReturn(encounter);
        when(exportTemplateDao.getElementById(TEMPLATE_ID)).thenReturn(exportTemplate);
        when(encounterExportService.getExportContent(encounter, exportTemplate)).thenThrow(new Exception("Content ist nicht zulässig in Prolog"));

        ResponseEntity<byte[]> response = encounterController.downloadEncounterTemplate(ENCOUNTER_ID, TEMPLATE_ID, httpRequest, httpResponse);

        assertEquals(HttpStatus.SEE_OTHER, response.getStatusCode());
        assertEquals("/encounter/show?id=" + ENCOUNTER_ID, response.getHeaders().getLocation().toString());
        assertEquals(TEMPLATE_ID, RequestContextUtils.getOutputFlashMap(httpRequest).get("downloadFailedTemplateId"));

        verify(auditEntryDao, never()).writeAuditEntry(any(), any(), any(), any(), any());
    }

    private Encounter createEncounter() {
        Bundle bundle = new Bundle("Test Bundle", "Test Bundle Description", 1L, true, true, false);

        Encounter encounter = new Encounter(bundle, CASE_NUMBER);
        encounter.setBundleLanguage("de_DE");

        return encounter;
    }

    private ExportTemplate createExportTemplate(final boolean fileExportEnabled) {
        Questionnaire questionnaire = new Questionnaire("Test Questionnaire", "Test Questionnaire Description", 1L, true);

        ExportTemplate exportTemplate = new ExportTemplate();
        exportTemplate.setName("REDCap Test Template");
        exportTemplate.setExportTemplateType(ExportTemplateType.REDCap);
        exportTemplate.setFilename("redcap-template.json");
        exportTemplate.setOriginalFilename("redcap-template");
        exportTemplate.setQuestionnaire(questionnaire);
        exportTemplate.setConfigurationGroup(configurationGroup(config("exportInDirectory", Boolean.toString(fileExportEnabled), 1), config("exportPath", "/tmp/mopat-test-export", 2), config("exportViaRest", "false", 3)));

        return exportTemplate;
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

    private Configuration config(final String attribute, final String value, final Integer position) {
        Configuration configuration = mock(Configuration.class);
        when(configuration.getAttribute()).thenReturn(attribute);
        when(configuration.getValue()).thenReturn(value);
        when(configuration.getPosition()).thenReturn(position);
        return configuration;
    }

}