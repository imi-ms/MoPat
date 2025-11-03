package de.imi.mopat.io;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.imi.mopat.controller.QuestionnaireController;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.OperatorDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.controller.QuestionnaireVersionGroupService;
import de.imi.mopat.io.impl.MetadataExporterFhirDstu3;
import de.imi.mopat.io.impl.MetadataExporterFhirR4b;
import de.imi.mopat.io.impl.MetadataExporterFhirR5;
import de.imi.mopat.io.importer.ImportFailedException;
import de.imi.mopat.io.importer.ImportQuestionnaireError;
import de.imi.mopat.io.importer.ImportQuestionnaireValidation;
import de.imi.mopat.io.importer.MoPatQuestionnaireImporter;
import de.imi.mopat.io.importer.fhir.FhirDstu3Helper;
import de.imi.mopat.io.importer.fhir.FhirImporter;
import de.imi.mopat.io.importer.fhir.FhirR4bHelper;
import de.imi.mopat.io.importer.fhir.FhirR5Helper;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireVersionGroup;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.model.enumeration.FhirVersion;
import de.imi.mopat.model.user.User;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RunWith(MockitoJUnitRunner.class)
public class FhirImporterTest {

    @Mock
    private static HttpServletRequest request;

    @Mock
    private static HttpSession session;

    @Mock
    private static ServletContext context;

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
    private static ConfigurationGroupDao configurationGroupDao;

    @Mock
    private ExportTemplateDao exportTemplateDao;

    @Mock
    private static QuestionnaireDao questionnaireDao;

    @Mock
    private QuestionDao questionDao;

    @Mock
    private ScoreDao scoreDao;

    private static MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @Mock
    private static SecurityContext mockContext;

    @Mock
    private static Authentication mockAuth;

    @Mock
    private static User mockUser;

    @Mock
    private static OperatorDao operatorDao;

    @Mock
    private static QuestionnaireVersionGroup questionnaireVersionGroup;

    @Mock
    private static QuestionnaireVersionGroupService questionnaireVersionGroupService;

    @Mock
    private ExportTemplate exportTemplate;

    @InjectMocks
    private MoPatQuestionnaireImporter moPatQuestionnaireImporter;

    @InjectMocks
    private QuestionnaireController questionnaireController;

    @InjectMocks
    private FhirImporter fhirImporter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockContext.getAuthentication()).thenReturn(mockAuth);
        when(SecurityContextHolder.getContext()).thenReturn(mockContext);

        when(mockUser.getId()).thenReturn(12345L);

        when(mockAuth.getPrincipal()).thenReturn(mockUser);

        when(operatorDao.getAllElements()).thenReturn(new ArrayList<>());

        when(questionnaireDao.isQuestionnaireNameUnique(any(String.class),
            any(Long.class))).thenReturn(true);

        when(questionnaireVersionGroupService.createQuestionnaireGroup(
            any(String.class))).thenReturn(new QuestionnaireVersionGroup());

        when(configurationGroupDao.getConfigurationGroups(any(String.class))).thenReturn(
            new ArrayList<>());

        try (MockedStatic<ExportTemplate> mocked = mockStatic(ExportTemplate.class)) {

            mocked.when(() -> ExportTemplate.createExportTemplates(any(String.class),
                    any(ExportTemplateType.class), any(MultipartFile.class),
                    any(ConfigurationGroupDao.class), any(ExportTemplateDao.class)))
                .thenAnswer(invocation -> null);
        }
    }

    @BeforeClass
    public static void init() throws IOException {
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
    }

    @Test
    public void testImportFhirQuestionnaireDstu3_MetadataExportValidation() throws IOException {
        File file = new File("src/test/resources/Demo_Questionnaire_MoPat.json");
        FileInputStream input = new FileInputStream(file);

        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
            IOUtils.toByteArray(input));

        Questionnaire questionnaire = moPatQuestionnaireImporter.importQuestionnaire(multipartFile);

        LocalDateTime dateTime = LocalDateTime.of(2000, 1, 1, 20, 0, 0, 0);
        Timestamp timestamp = Timestamp.valueOf(dateTime);

        questionnaire.setUpdatedAt(timestamp);

        MetadataExporterFhirDstu3 exporter = new MetadataExporterFhirDstu3();

        byte[] fhirExport = exporter.export(questionnaire, messageSource, configurationDao,
            configurationGroupDao, exportTemplateDao, questionnaireDao, questionDao, scoreDao);

        File controlFile = new File(
            "src/test/resources/fhir-control-files/fhir-dstu3-metadata-export-test.xml");
        byte[] control = new FileInputStream(controlFile).readAllBytes();

        assertEquals(new String(fhirExport).replaceAll("\\s+", " ").trim(),
            new String(control).replaceAll("\\s+", " ").trim());
    }

    @Test
    public void testImportFhirQuestionnaireR4b_MetadataExportValidation() throws IOException {
        File file = new File("src/test/resources/Demo_Questionnaire_MoPat.json");
        FileInputStream input = new FileInputStream(file);

        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
            IOUtils.toByteArray(input));

        Questionnaire questionnaire = moPatQuestionnaireImporter.importQuestionnaire(multipartFile);

        LocalDateTime dateTime = LocalDateTime.of(2000, 1, 1, 20, 0, 0, 0);
        Timestamp timestamp = Timestamp.valueOf(dateTime);

        questionnaire.setUpdatedAt(timestamp);

        MetadataExporterFhirR4b exporter = new MetadataExporterFhirR4b();

        byte[] fhirExport = exporter.export(questionnaire, messageSource, configurationDao,
            configurationGroupDao, exportTemplateDao, questionnaireDao, questionDao, scoreDao);

        File controlFile = new File(
            "src/test/resources/fhir-control-files/fhir-r4b-metadata-export-test.xml");
        byte[] control = new FileInputStream(controlFile).readAllBytes();

        assertEquals(new String(fhirExport).replaceAll("\\s+", " ").trim(),
            new String(control).replaceAll("\\s+", " ").trim());
    }

    @Test
    public void testImportFhirQuestionnaireR5_MetadataExportValidation() throws IOException {
        File file = new File("src/test/resources/Demo_Questionnaire_MoPat.json");
        FileInputStream input = new FileInputStream(file);

        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
            IOUtils.toByteArray(input));

        Questionnaire questionnaire = moPatQuestionnaireImporter.importQuestionnaire(multipartFile);

        LocalDateTime dateTime = LocalDateTime.of(2000, 1, 1, 20, 0, 0, 0);
        Timestamp timestamp = Timestamp.valueOf(dateTime);

        questionnaire.setUpdatedAt(timestamp);

        MetadataExporterFhirR5 exporter = new MetadataExporterFhirR5();

        byte[] fhirExport = exporter.export(questionnaire, messageSource, configurationDao,
            configurationGroupDao, exportTemplateDao, questionnaireDao, questionDao, scoreDao);

        File controlFile = new File(
            "src/test/resources/fhir-control-files/fhir-r5-metadata-export-test.xml");
        byte[] control = new FileInputStream(controlFile).readAllBytes();

        assertEquals(new String(fhirExport).replaceAll("\\s+", " ").trim(),
            new String(control).replaceAll("\\s+", " ").trim());
    }

    @Test
    public void testImportFhirQuestionnaireDstu3_SuccessfulValidation() throws IOException {
        File file = new File("src/test/resources/fhir-control-files/fhir-dstu3-import-test.xml");
        FileInputStream input = new FileInputStream(file);

        ImportQuestionnaireValidation result = new ImportQuestionnaireValidation();

        String fhirString = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        boolean validationResult = FhirDstu3Helper.validateFileWithFhirInstanceValidator(fhirString,
            result, "de_DE");

        assertTrue("The validation of the resource was not successful. ", validationResult);
    }

    @Test
    public void testImportFhirQuestionnaireR4b_SuccessfulValidation() throws IOException {
        File file = new File("src/test/resources/fhir-control-files/fhir-r4b-import-test.xml");
        FileInputStream input = new FileInputStream(file);

        ImportQuestionnaireValidation result = new ImportQuestionnaireValidation();

        String fhirString = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        boolean validationResult = FhirR4bHelper.validateFileWithFhirInstanceValidator(fhirString,
            result, "de_DE");

        assertTrue("The validation of the resource was not successful. ", validationResult);
    }

    @Test
    public void testImportFhirQuestionnaireR5_SuccessfulValidation() throws IOException {
        File file = new File("src/test/resources/fhir-control-files/fhir-r5-import-test.xml");
        FileInputStream input = new FileInputStream(file);

        ImportQuestionnaireValidation result = new ImportQuestionnaireValidation();

        String fhirString = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        boolean validationResult = FhirR5Helper.validateFileWithFhirInstanceValidator(fhirString,
            result, "de_DE");

        assertTrue("The validation of the resource was not successful. ", validationResult);
    }


    @Test
    public void testImportFhirQuestionnaireDstu3_SuccessfulImport() throws IOException {
        File file = new File("src/test/resources/fhir-control-files/fhir-dstu3-import-test.xml");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
            IOUtils.toByteArray(input));

        String url = "";

        ImportQuestionnaireValidation result = fhirImporter.importFhirQuestionnaire(multipartFile,
            url, FhirVersion.DSTU3, "de_DE");

        assertNotNull("The result was null, although it should be created", result);
        assertFalse(
            "The import had errors, although it should not: " + (result.getValidationErrors()
                .stream().map(ImportQuestionnaireError::getErrorCode)
                .collect(Collectors.joining(", "))), result.hasErrors());
        assertNotNull("There was no import result present", result.getImportResult());
    }

    @Test
    public void testImportFhirQuestionnaireR4b_SuccessfulImport() throws IOException {
        File file = new File("src/test/resources/fhir-control-files/fhir-r4b-import-test.xml");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
            IOUtils.toByteArray(input));

        String url = "";

        ImportQuestionnaireValidation result = fhirImporter.importFhirQuestionnaire(multipartFile,
            url, FhirVersion.R4B, "de_DE");

        assertNotNull("The result was null, although it should be created", result);
        assertFalse(
            "The import had errors, although it should not: " + (result.getValidationErrors()
                .stream().map(ImportQuestionnaireError::getErrorCode)
                .collect(Collectors.joining(", "))), result.hasErrors());
        assertNotNull("There was no import result present", result.getImportResult());
    }

    @Test
    public void testImportFhirQuestionnaireR5_SuccessfulImport() throws IOException {
        File file = new File("src/test/resources/fhir-control-files/fhir-r5-import-test.xml");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
            IOUtils.toByteArray(input));

        String url = "";

        ImportQuestionnaireValidation result = fhirImporter.importFhirQuestionnaire(multipartFile,
            url, FhirVersion.R5, "de_DE");

        assertNotNull("The result was null, although it should be created", result);
        assertFalse(
            "The import had errors, although it should not: " + (result.getValidationErrors()
                .stream().map(ImportQuestionnaireError::getErrorCode)
                .collect(Collectors.joining(", "))), result.hasErrors());
        assertNotNull("There was no import result present", result.getImportResult());
    }

    @Test
    public void testImportFhirQuestionnaireDstu3_WithErrors() throws IOException {
        File file = new File("src/test/resources/fhir-control-files/fhir-dstu3-with-errors.xml");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
            IOUtils.toByteArray(input));

        String url = "";

        try {
            ImportQuestionnaireValidation result = fhirImporter.importFhirQuestionnaire(
                multipartFile, url, FhirVersion.DSTU3, "de_DE");

            assertNull("The result was present, although it should be faulty and therefore null",
                result.getImportResult());

            //<!-- Repeated linkId (should be unique per Questionnaire) -->
            assertTrue(checkIfErrorIsPresent(result, "error", 2, "Questionnaire",
                "!!Regel que-2: 'The link ids for groups and questions must be unique within the questionnaire' fehlgeschlagen"));

            //<!-- Invalid element: 'titel' instead of 'title' -->
            assertTrue(checkIfErrorIsPresent(result, "error", 9, "/f:Questionnaire",
                "!!Undefiniertes Element 'titel'"));

            //<!-- Missing description value -->
            assertTrue(checkIfErrorIsPresent(result, "error", 27, "/f:Questionnaire/f:description",
                "Element muss einen Inhalt haben"));
            assertTrue(checkIfErrorIsPresent(result, "error", 27, "Questionnaire.description",
                "Primitive Typen müssen einen Wert, oder child Extensions haben"));
            assertTrue(checkIfErrorIsPresent(result, "error", 27, "Questionnaire.description",
                "Constraint failed: ele-1: 'All FHIR elements must have a @value or children' (defined in Element)"));

            //<!-- Invalid date format -->
            assertTrue(checkIfErrorIsPresent(result, "error", 24, "Questionnaire.date",
                "Kein gültiges Datum/Uhrzeit ('30-06-2025' doesn't meet format requirements for dateTime)"));

            //<!-- Missing 'type' -->
            assertTrue(checkIfErrorIsPresent(result, "error", 43, "Questionnaire.item[1]",
                "!!Regel que-1: 'Group items must have nested items, display items cannot have nested items' fehlgeschlagen (type: ; item: ) (log:  (type: ; item: ))"));
            assertTrue(checkIfErrorIsPresent(result, "error", 43, "Questionnaire.item[1]",
                "Questionnaire.item.type: mindestens erforderlich = 1, aber nur gefunden 0"));

            //<!-- repeats as string instead of boolean -->
            assertTrue(checkIfErrorIsPresent(result, "error", 49, "Questionnaire.item[1].repeats",
                "Boolesche Werte müssen 'wahr' oder 'falsch' sein."));

            //<!-- Group without items (constraint violation) -->
            assertTrue(checkIfErrorIsPresent(result, "error", 70, "Questionnaire.item[3]",
                "!!Regel que-1: 'Group items must have nested items, display items cannot have nested items' fehlgeschlagen"));

            //<!-- Invalid code, should be 'en' per BCP-47 -->
            assertTrue(checkIfErrorIsPresent(result, "error", 4, "Questionnaire.language",
                "Der bereitgestellte Code ist nicht im maximum value set 'All Languages' (http://hl7.org/fhir/ValueSet/all-languages|3.0.2), und ein Code aus diesem ValueSet ist erforderlich) (Code = Unknown code 'urn:ietf:bcp:47#english' for in-memory expansion of ValueSet 'http://hl7.org/fhir/ValueSet/all-languages'#{2}, Fehler = {3}))"));

            //<!-- Wrong datatype: status should be a code from allowed values -->
            assertTrue(checkIfErrorIsPresent(result, "error", 21, "Questionnaire.status",
                "Unknown code 'http://hl7.org/fhir/publication-status#drafted'"));
            assertTrue(checkIfErrorIsPresent(result, "error", 21, "Questionnaire.status",
                "!!Der angegebene Wert ('drafted') ist nicht im ValueSet 'PublicationStatus' (http://hl7.org/fhir/ValueSet/publication-status|3.0.2), und ein Code aus diesem Valueset ist erforderlich) (error message = Unknown code 'http://hl7.org/fhir/publication-status#drafted' for in-memory expansion of ValueSet 'http://hl7.org/fhir/ValueSet/publication-status')"));

            //<!-- Invalid system URL -->
            assertTrue(checkIfErrorIsPresent(result, "error", 35, "Questionnaire.item[0].option[0].value.ofType(Coding)",
                "Coding.system muss eine absolute Referenz sein, nicht eine lokale Referenz"));


        } catch (ImportFailedException ex) {
            assertEquals("File validation failed", ex.getMessage());
        }
    }

    @Test
    public void testImportFhirQuestionnaireR4b_WithErrors() throws IOException {
        File file = new File("src/test/resources/fhir-control-files/fhir-r4b-with-errors.xml");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
            IOUtils.toByteArray(input));

        String url = "";

        try {
            ImportQuestionnaireValidation result = fhirImporter.importFhirQuestionnaire(
                multipartFile, url, FhirVersion.R4B, "de_DE");

            assertNull("The result was present, although it should be faulty and therefore null",
                result.getImportResult());

            //<!-- Repeated linkId (should be unique per Questionnaire) -->
            assertTrue(checkIfErrorIsPresent(result, "error", 2, "Questionnaire",
                "Constraint failed: que-2: 'The link ids for groups and questions must be unique within the questionnaire' (defined in http://hl7.org/fhir/StructureDefinition/Questionnaire)"));

            //<!-- Invalid element: 'titel' instead of 'title' -->
            assertTrue(checkIfErrorIsPresent(result, "error", 9, "/f:Questionnaire",
                "Undefined element 'titel' at /f:Questionnaire"));

            //<!-- Missing description value -->
            assertTrue(checkIfErrorIsPresent(result, "error", 27, "/f:Questionnaire/f:description",
                "Element must have some content"));
            assertTrue(checkIfErrorIsPresent(result, "error", 27, "Questionnaire.description",
                "Primitive types must have a value or must have child extensions"));
            assertTrue(checkIfErrorIsPresent(result, "error", 27, "Questionnaire.description",
                "Constraint failed: ele-1: 'All FHIR elements must have a @value or children' (defined in http://hl7.org/fhir/StructureDefinition/Element)"));

            //<!-- Invalid date format -->
            assertTrue(checkIfErrorIsPresent(result, "error", 24, "Questionnaire.date",
                "Not a valid date/time ('30-06-2025' doesn't meet format requirements for dateTime)"));

            //<!-- Missing 'type' -->
            assertTrue(checkIfErrorIsPresent(result, "error", 43, "Questionnaire.item[1]",
                "Constraint failed: que-6: 'Required and repeat aren't permitted for display items' (defined in http://hl7.org/fhir/StructureDefinition/Questionnaire)"));
            assertTrue(checkIfErrorIsPresent(result, "error", 43, "Questionnaire.item[1]",
                "Questionnaire.item.type: minimum required = 1, but only found 0 (from http://hl7.org/fhir/StructureDefinition/Questionnaire|4.3.0)"));

            //<!-- repeats as string instead of boolean -->
            assertTrue(checkIfErrorIsPresent(result, "error", 49, "Questionnaire.item[1].repeats",
                "Boolean values must be 'true' or 'false'"));

            //<!-- Wrong datatype: status should be a code from allowed values -->
            assertTrue(checkIfErrorIsPresent(result, "error", 21, "Questionnaire.status",
                "Unknown code 'http://hl7.org/fhir/publication-status#drafted'"));
            assertTrue(checkIfErrorIsPresent(result, "error", 21, "Questionnaire.status",
                "The value provided ('drafted') was not found in the value set 'PublicationStatus' (http://hl7.org/fhir/ValueSet/publication-status|4.3.0), and a code is required from this value set  (error message = Unknown code 'http://hl7.org/fhir/publication-status#drafted' for in-memory expansion of ValueSet 'http://hl7.org/fhir/ValueSet/publication-status')"));

            //<!-- Invalid system URL -->
            assertTrue(checkIfErrorIsPresent(result, "error", 35, "Questionnaire.item[0].answerOption[0].value.ofType(Coding)",
                "Coding.system must be an absolute reference, not a local reference"));


        } catch (ImportFailedException ex) {
            assertEquals("File validation failed", ex.getMessage());
        }
    }

    @Test
    public void testImportFhirQuestionnaireR5_WithErrors() throws IOException {
        File file = new File("src/test/resources/fhir-control-files/fhir-r5-with-errors.xml");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain",
            IOUtils.toByteArray(input));

        String url = "";

        try {
            ImportQuestionnaireValidation result = fhirImporter.importFhirQuestionnaire(
                multipartFile, url, FhirVersion.R5, "de_DE");

            assertNull("The result was present, although it should be faulty and therefore null",
                result.getImportResult());

            //<!-- Repeated linkId (should be unique per Questionnaire) -->
            assertTrue(checkIfErrorIsPresent(result, "error", 2, "Questionnaire",
                "Constraint failed: que-2: 'The link ids for groups and questions must be unique within the questionnaire' (defined in http://hl7.org/fhir/StructureDefinition/Questionnaire)"));

            //<!-- Invalid element: 'titel' instead of 'title' -->
            assertTrue(checkIfErrorIsPresent(result, "error", 9, "/f:Questionnaire",
                "Undefined element 'titel' at /f:Questionnaire"));

            //<!-- Missing description value -->
            assertTrue(checkIfErrorIsPresent(result, "error", 27, "/f:Questionnaire/f:description",
                "Element must have some content"));
            assertTrue(checkIfErrorIsPresent(result, "error", 27, "Questionnaire.description",
                "Primitive types must have a value or must have child extensions"));
            assertTrue(checkIfErrorIsPresent(result, "error", 27, "Questionnaire.description",
                "Constraint failed: ele-1: 'All FHIR elements must have a @value or children' (defined in http://hl7.org/fhir/StructureDefinition/Element)"));

            //<!-- Invalid date format -->
            assertTrue(checkIfErrorIsPresent(result, "error", 24, "Questionnaire.date",
                "Not a valid date/time ('30-06-2025' doesn't meet format requirements for dateTime)"));

            //<!-- Missing 'type' -->
            assertTrue(checkIfErrorIsPresent(result, "error", 44, "Questionnaire.item[1]",
                "Constraint failed: que-6: 'Required and repeat aren't permitted for display items' (defined in http://hl7.org/fhir/StructureDefinition/Questionnaire)"));
            assertTrue(checkIfErrorIsPresent(result, "error", 44, "Questionnaire.item[1]",
                "Questionnaire.item.type: minimum required = 1, but only found 0 (from http://hl7.org/fhir/StructureDefinition/Questionnaire|5.0.0)"));

            //<!-- repeats as string instead of boolean -->
            assertTrue(checkIfErrorIsPresent(result, "error", 50, "Questionnaire.item[1].repeats",
                "Boolean values must be 'true' or 'false'"));

            //<!-- Wrong datatype: status should be a code from allowed values -->
            assertTrue(checkIfErrorIsPresent(result, "error", 21, "Questionnaire.status",
                "Unknown code 'http://hl7.org/fhir/publication-status#drafted'"));
            assertTrue(checkIfErrorIsPresent(result, "error", 21, "Questionnaire.status",
                "The value provided ('drafted') was not found in the value set 'PublicationStatus' (http://hl7.org/fhir/ValueSet/publication-status|5.0.0), and a code is required from this value set  (error message = Unknown code 'http://hl7.org/fhir/publication-status#drafted' for in-memory expansion of ValueSet 'http://hl7.org/fhir/ValueSet/publication-status')"));

            //<!-- Invalid type for R5: valid values include choice, string, date, etc. -->
            assertTrue(checkIfErrorIsPresent(result, "error", 34, "Questionnaire.item[0].type",
                "The value provided ('choice') was not found in the value set 'Questionnaire Item Type' (http://hl7.org/fhir/ValueSet/item-type|5.0.0), and a code is required from this value set  (error message = Unknown code 'http://hl7.org/fhir/item-type#choice' for in-memory expansion of ValueSet 'http://hl7.org/fhir/ValueSet/item-type')"));

            //<!-- Invalid system URL -->
            assertTrue(checkIfErrorIsPresent(result, "error", 36, "Questionnaire.item[0].answerOption[0].value.ofType(Coding)",
                "Coding.system must be an absolute reference, not a local reference"));


        } catch (ImportFailedException ex) {
            assertEquals("File validation failed", ex.getMessage());
        }
    }


    private boolean checkIfErrorIsPresent(ImportQuestionnaireValidation result, String type,
        int lineNumber, String fhirPath, String message) {
        boolean match = false;

        for (ImportQuestionnaireError error : filterValidationByIndex(result, lineNumber)) {
            Object[] arguments = error.getErrorArguments();
            try {
                if (arguments[0].equals(type) && lineNumber == ((int) arguments[1])
                    && arguments[2].equals(fhirPath) && arguments[3].equals(message)) {
                    match = true;
                }
            } catch (Exception ignored) {
            }
        }
        return match;
    }

    private List<ImportQuestionnaireError> filterValidationByIndex(
        ImportQuestionnaireValidation result, int index) {
        return result.getValidationErrors().stream().filter(
            err -> err.getErrorArguments() != null && err.getErrorArguments().length > 1
                && err.getErrorArguments()[1] instanceof Integer
                && ((Integer) err.getErrorArguments()[1]) == index).toList();
    }
}
