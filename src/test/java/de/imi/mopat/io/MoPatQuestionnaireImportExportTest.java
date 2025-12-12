package de.imi.mopat.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportRuleDao;
import de.imi.mopat.dao.ExportRuleFormatDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.io.impl.MetadataExporterMoPatComplete;
import de.imi.mopat.io.importer.ImportQuestionnaireValidation;
import de.imi.mopat.io.importer.MopatCompleteQuestionnaireImporter;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.AnswerTest;
import de.imi.mopat.model.ExportRuleAnswer;
import de.imi.mopat.model.ExportRuleAnswerTest;
import de.imi.mopat.model.ExportRuleFormat;
import de.imi.mopat.model.ExportRuleFormatTest;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.ExportTemplateTest;
import de.imi.mopat.model.FreetextAnswerTest;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.dto.export.JsonCompleteQuestionnaireDTO;
import de.imi.mopat.model.dto.export.JsonExportTemplateDTO;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.model.user.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class MoPatQuestionnaireImportExportTest {

    private static final Random random = new Random();
    private Questionnaire testQuestionnaire;
    private Questionnaire testQuestionnaireWithoutExportTemplates;

    private MetadataExporterMoPatComplete exporter;


    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private ConfigurationGroupDao configurationGroupDao;

    @Autowired
    private ExportTemplateDao exportTemplateDao;

    @Autowired
    private ExportRuleDao exportRuleDao;

    @Autowired
    private ExportRuleFormatDao exportRuleFormatDao;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private ScoreDao scoreDao;

    @Mock
    private MessageSource messageSource;

    @Autowired
    private MopatCompleteQuestionnaireImporter importer;


    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        exporter = new MetadataExporterMoPatComplete();
        setupSecurityContext();
        setupTestData();
    }

    @After
    public void tearDown() {
        questionnaireDao.remove(testQuestionnaire);
        questionnaireDao.remove(testQuestionnaireWithoutExportTemplates);
        SecurityContextHolder.clearContext();
    }

    private void setupSecurityContext() {

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setFirstname("Test");
        mockUser.setLastname("User");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("testPassword");

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            mockUser,
            "testPassword",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof de.imi.mopat.model.user.User)) {
            throw new IllegalStateException(
                "Security context principal is not de.imi.mopat.model.user.User. " +
                    "Actual type: " + principal.getClass().getName()
            );
        }
    }

    private void setupTestData() throws IOException {

        testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        testQuestionnaireWithoutExportTemplates = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);
        questionnaireDao.merge(testQuestionnaireWithoutExportTemplates);

        // Add 3 questions using existing helpers
        for (int i = 0; i < 3; i++) {
            Question question = QuestionTest.getNewValidQuestion();
            question.setQuestionnaire(testQuestionnaire);
            question.setQuestionType(QuestionType.FREE_TEXT);
            questionDao.merge(question);
            Answer a = FreetextAnswerTest.getNewValidFreetextAnswer(question);
            a.setQuestion(question);
            answerDao.merge(a);

            questionDao.merge(question);
            testQuestionnaire.addQuestion(question);
            questionnaireDao.merge(testQuestionnaire);

        }

        Question q = QuestionTest.getNewValidQuestion();
        q.setQuestionnaire(testQuestionnaireWithoutExportTemplates);
        questionDao.merge(q);
        testQuestionnaireWithoutExportTemplates.addQuestion(q);

        questionnaireDao.merge(testQuestionnaire);
        questionnaireDao.merge(testQuestionnaireWithoutExportTemplates);

        // Create and add export templates
        createAndAddExportTemplates();

        questionnaireDao.merge(testQuestionnaire);
    }

    private void createAndAddExportTemplates() throws IOException {
        createExportTemplateWithRules(ExportTemplateType.ODM, "odm_template.xml");

        createExportTemplateWithRules(ExportTemplateType.ODM, "odm_template_2.xml");
    }

    private void createExportTemplateWithRules(ExportTemplateType type, String filename)
        throws IOException {

        ExportTemplate template = ExportTemplateTest.getNewValidExportTemplate();
        template.setExportTemplateType(type);
        template.setOriginalFilename(filename);
        template.setQuestionnaire(testQuestionnaire);
        template.setConfigurationGroup(
            configurationGroupDao.getConfigurationGroups(type.getConfigurationMessageCode())
                .stream().findFirst().get());
        exportTemplateDao.merge(template);

        template.setFilename(template.getId() + "_" + filename);
        exportTemplateDao.merge(template);

        // Create template file
        createTemplateFile(template);

        // Add export rules for all answers in questionnaire
        addExportRulesToTemplate(template);

        testQuestionnaire.addExportTemplate(template);
        questionnaireDao.merge(testQuestionnaire);
    }

    private void createTemplateFile(ExportTemplate exportTemplate) throws IOException {

        String objectStoragePath = configurationDao.getObjectStoragePath();
        String contextPath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;

        File templateFile = new File(contextPath, exportTemplate.getFilename());
        templateFile.createNewFile();

        String content = String.format(
            "<?xml version=\"1.0\"?>\n<template name=\"%s\">Test Content</template>",
            exportTemplate.getName()
        );
        Files.writeString(templateFile.toPath(), content);
    }

    private void addExportRulesToTemplate(ExportTemplate template) {

        // Iterate through all questions and their answers
        for (Question question : testQuestionnaire.getQuestions()) {
            for (Answer answer : question.getAnswers()) {
                createExportRuleAnswer(template, answer);
            }
            questionDao.merge(question);
        }

    }

    private void createExportRuleAnswer(ExportTemplate template, Answer answer) {
        ExportRuleAnswer rule = ExportRuleAnswerTest.getNewValidExportRuleAnswer(answer, template);
        rule.setExportField("field_" + answer.getId());

        // Add format settings
        ExportRuleFormat format = ExportRuleFormatTest.getNewValidExportRuleFormat();
        format.addExportRule(rule);
        format.setDecimalPlaces(0);
        rule.setExportRuleFormat(format);
        exportRuleFormatDao.merge(format);

        exportRuleDao.merge(rule);
        template.addExportRule(rule);
        exportTemplateDao.merge(template);
        answer.addExportRule(rule);
        answerDao.merge(answer);

    }


    private MultipartFile createMultipartFile(byte[] exportedData) {
        return new MockMultipartFile(
            "file", "test.json", "application/json", exportedData
        );
    }

    @Test
    public void testExportQuestionnaireWithExportTemplates() throws IOException {
        byte[] exportedData = exporter.export(
            testQuestionnaire, messageSource, configurationDao,
            configurationGroupDao, exportTemplateDao, questionnaireDao,
            questionDao, scoreDao
        );

        assertNotNull("Exporting questionnaire failed. The returned data was null.", exportedData);
        assertTrue("Exporting questionnaire failed. The returned data was empty.",
            exportedData.length > 0);

        String jsonString = new String(exportedData);

        assertTrue(
            "Exporting questionnaire failed. The JSON doesn't contain the expected type.",
            jsonString.contains("\"type\" : \"questionnaireComplete\""));

        ObjectMapper mapper = new ObjectMapper();
        JsonCompleteQuestionnaireDTO dto = mapper.readValue(exportedData,
            JsonCompleteQuestionnaireDTO.class);

        assertNotNull("Exported DTO was null.", dto);

        assertEquals("Exported questionnaire name didn't match the expected value.",
            testQuestionnaire.getName(), dto.getName());

        assertEquals("Exported questionnaire description didn't match the expected value.",
            testQuestionnaire.getDescription(), dto.getDescription());

        assertNotNull("Exported questions were null.", dto.getQuestionDTOs());
        assertFalse("Exported questions set was empty.", dto.getQuestionDTOs().isEmpty());
        assertNotNull("Exported export templates were null.", dto.getExportDTOs());
        assertEquals("Export didn't contain the expected number of templates.",
            2, dto.getExportDTOs().size());

        for (JsonExportTemplateDTO templateDTO : dto.getExportDTOs().values()) {
            assertNotNull("Export template file encoding failed. The file byte array was null.",
                templateDTO.getFileByteArrayEncoded());
            assertNotNull("Original filename was null although a not-null value was expected.",
                templateDTO.getOriginalFilename());
            assertTrue("Original filename doesn't end with .xml although it was expected.",
                templateDTO.getOriginalFilename().endsWith(".xml"));

            byte[] decodedFile = Base64.decodeBase64(templateDTO.getFileByteArrayEncoded());
            String fileContent = new String(decodedFile);
            assertTrue(
                "Export template file decoding failed. The content doesn't match expected value.",
                fileContent.contains("Test Content"));
            assertNotNull("Export rules were null.", templateDTO.getExportRuleDTOs());
            assertFalse("Export rules set was empty.", templateDTO.getExportRuleDTOs().isEmpty());

            boolean hasAnswerRule = templateDTO.getExportRuleDTOs().values().stream()
                .anyMatch(rule -> rule.getAnswerId() != null);
            assertTrue("Export rules don't contain answer mappings.", hasAnswerRule);
        }

        MultipartFile file = createMultipartFile(exportedData);

        Questionnaire imported = importer.importQuestionnaire(file);

        assertNotNull("Import failed. Questionnaire was null.", imported);

        assertEquals(
            "Import failed. Questionnaire description didn't match the expected value.",
            testQuestionnaire.getDescription(), imported.getDescription());

        assertNotNull("Import failed. Export templates were null.", imported.getExportTemplates());
        assertFalse("Import failed. Export templates were empty.",
            imported.getExportTemplates().isEmpty());


        Collection<ExportTemplate> exportTemplates = exportTemplateDao.getElementsById(imported.getExportTemplates().stream()
            .map(ExportTemplate::getId)
            .toList());

        ExportTemplate updatedExportTemplate = exportTemplates.stream().filter(x->!x.getExportRules().isEmpty()).findFirst().get();

        mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);

        assertNotNull("Import failed. Export rules were null.",
            updatedExportTemplate.getExportRules());

        assertFalse("Import failed. Export rules were empty.",
            updatedExportTemplate.getExportRules().isEmpty());

        try(InputStream input = getClass().getClassLoader().getResourceAsStream("Sample_exported.json")){

            String sampleExportFile = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            JsonCompleteQuestionnaireDTO expectedDTO = mapper.readValue(sampleExportFile,
                JsonCompleteQuestionnaireDTO.class);

            assertEquals("Number of questions doesn't match expected sample.",
                expectedDTO.getQuestionDTOs().size(), dto.getQuestionDTOs().size());
            assertEquals("Number of export templates doesn't match expected sample.",
                expectedDTO.getExportDTOs().size(), dto.getExportDTOs().size());
        }

    }

    @Test
    public void testQuestionnaireWithoutExportTemplates() throws Exception {

        byte[] exportedData = exporter.export(
            testQuestionnaireWithoutExportTemplates, messageSource, configurationDao,
            configurationGroupDao, exportTemplateDao, questionnaireDao,
            questionDao, scoreDao
        );

        ObjectMapper mapper = new ObjectMapper();
        JsonCompleteQuestionnaireDTO dto = mapper.readValue(exportedData,
            JsonCompleteQuestionnaireDTO.class);

        assertNotNull("Exporting questionnaire failed. The returned data was null.", exportedData);
        assertTrue("Exporting questionnaire failed. The returned data was empty.",
            exportedData.length > 0);
        assertNotNull("Export DTOs map was null.", dto.getExportDTOs());
        assertTrue("Export DTOs map wasn't empty.", dto.getExportDTOs().isEmpty());
        assertNotNull("Exported questions were null.", dto.getQuestionDTOs());
        assertFalse("Exported questions set was empty.", dto.getQuestionDTOs().isEmpty());
    }

    @Test
    public void testEmptyFileImport() {
        MultipartFile file = new MockMultipartFile(
            "file", "test.json", "application/json", new byte[0]
        );

        Throwable e = null;
        try {
            importer.importQuestionnaire(file);
        } catch (Throwable ex) {
            e = ex;
        }

        assertNotNull("Import of empty file should throw an exception.", e);
    }

    @Test
    public void testInvalidJSONImport() {
        MultipartFile file = new MockMultipartFile(
            "file", "test.json", "application/json",
            "{ invalid  }".getBytes()
        );

        Throwable e = null;
        try {
            importer.importQuestionnaire(file);
        } catch (Throwable ex) {
            e = ex;
        }

        assertTrue("It was possible to import invalid JSON although an exception was expected.",
            e instanceof IOException);
    }


}