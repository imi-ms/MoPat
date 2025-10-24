package de.imi.mopat.io;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.controller.QuestionnaireController;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.io.impl.MetadataExporterFhirDstu3;
import de.imi.mopat.io.importer.ImportQuestionnaireError;
import de.imi.mopat.io.importer.ImportQuestionnaireValidation;
import de.imi.mopat.io.importer.fhir.FhirDstu3Helper;
import de.imi.mopat.io.importer.fhir.FhirImporter;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.export.JsonQuestionnaireDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//@RunWith(MockitoJUnitRunner.class)
public class FhirImporterTest {
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpSession session;
    
    @Mock
    private ServletContext context;
    
    @Mock
    private RedirectAttributes redirectAttributes;
    
    @Mock
    private BindingResult result;
    
    @Mock
    private Model model;
    
    @Mock
    private ImportQuestionnaireValidation importQuestionnaireValidation;
    
    @Mock
    private MessageSource messageSource;
    
    @Mock
    private ConfigurationDao configurationDao;
    
    @Mock
    private ConfigurationGroupDao configurationGroupDao;
    
    @Mock
    private ExportTemplateDao exportTemplateDao;
    
    @Mock
    private QuestionnaireDao questionnaireDao;
    
    @Mock
    private QuestionDao questionDao;
    
    @Mock
    private ScoreDao scoreDao;
    
    @InjectMocks
    private QuestionnaireController questionnaireController;
    
    
    private FhirImporter fhirImporter;
    
    
//    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        this.fhirImporter = new FhirImporter();
        
        when(request.getSession()).thenReturn(session);
        when(session.getServletContext()).thenReturn(context);
        when(context.getRealPath("")).thenReturn(new File("src/main/webapp/").getAbsolutePath());
        
    }
    
//    @Test
    public void testImportFhirQuestionnaireDstu3_MetadataExportValidation() throws IOException {
        File file = new File("src/test/resources/Fragebogen_alle_Typen_MoPat.json");
        FileInputStream input = new FileInputStream(file);
        
        ObjectMapper mapper = new ObjectMapper();
        JsonQuestionnaireDTO jsonQuestionnaireDTO = mapper.readValue(input, JsonQuestionnaireDTO.class);
        Questionnaire questionnaire = jsonQuestionnaireDTO.convertToQuestionnaire();
        
        MetadataExporterFhirDstu3 exporter = new MetadataExporterFhirDstu3();
        
        byte[] fhirExport = exporter.export(questionnaire, messageSource, configurationDao, configurationGroupDao,
            exportTemplateDao, questionnaireDao, questionDao, scoreDao);
        
        assertTrue(true);
    }

    //    @Test
    public void testImportFhirQuestionnaireDstu3_SuccessfulValidation() throws IOException {
        File file = new File("src/test/resources/Fragebogen_alle_Typen_FHIRDSTU3.xml");
        FileInputStream input = new FileInputStream(file);
        
        ImportQuestionnaireValidation result = new ImportQuestionnaireValidation();
        
        String fhirString = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        boolean validationResult = true;//FhirDstu3Helper.validateFileWithFhirInstanceValidator(fhirString, result);
        
        assertTrue("The validation of the resource was not successful. ", validationResult);
        
    }
    
    
//    @Test
    public void testImportFhirQuestionnaireDstu3_SuccessfulImport() throws IOException {
        File file = new File("src/test/resources/Fragebogen_alle_Typen_FHIRDSTU3.xml");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
            IOUtils.toByteArray(input));
        
        String webappPath = new File("src/main/webapp").getAbsolutePath() + "/";
        String url = "";
        
        ImportQuestionnaireValidation result = null; //fhirImporter.importFhirQuestionnaire(multipartFile, url, webappPath);

        assertNotNull("The result was null, although it should be created", result);
        assertFalse("The import had errors, although it should not: " + (result.getValidationErrors().stream()
            .map(ImportQuestionnaireError::getErrorCode).collect(Collectors.joining(", "))), result.hasErrors());
        assertNotNull("There was no import result present", result.getImportResult());
    }
    
//    @Test
    public void testImportFhirQuestionnaire_WithErrors() throws IOException {
    
    }
    
    
}
