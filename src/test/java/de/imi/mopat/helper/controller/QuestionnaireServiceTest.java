package de.imi.mopat.helper.controller;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.*;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.helper.model.QuestionnaireFactory;
import de.imi.mopat.model.*;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireDTOTest;
import de.imi.mopat.model.enumeration.ApprovalStatus;
import de.imi.mopat.model.user.UserRole;
import de.imi.mopat.utils.Helper;
import de.imi.mopat.utils.MultipartFileUtils;
import de.imi.mopat.validator.LogoValidator;
import de.imi.mopat.validator.QuestionnaireDTOValidator;
import jakarta.persistence.EntityNotFoundException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
        MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class QuestionnaireServiceTest {

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Autowired
    private ExportTemplateDao exportTemplateDao;

    @Autowired
    private ResponseDao responseDao;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private BundleDao bundleDao;

    @Autowired
    private EncounterDao encounterDao;

    @Autowired
    private ConfigurationGroupDao configurationGroupDao;

    @Autowired
    private QuestionnaireVersionGroupDao questionnaireVersionGroupDao;

    @Autowired
    private ScoreDao scoreDao;

    @Autowired
    private ExpressionDao expressionDao;

    @Mock
    private MessageSource messageSource;

    @Mock
    private QuestionService questionService;

    @Mock
    private QuestionnaireDTOMapper questionnaireDTOMapper;

    @Mock
    private QuestionnaireDTOValidator questionnaireDTOValidator;

    @Mock
    private AuthService authService;

    @Mock
    private BundleService bundleService;

    @Mock
    private QuestionnaireFactory questionnaireFactory;

    @Mock
    private LogoValidator logoValidator;

    @Mock
    private QuestionnaireVersionGroupService questionnaireVersionGroupService;

    @Mock
    private FileUtils fileUtils;

    @Autowired
    @InjectMocks
    private QuestionnaireService questionnaireService;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(new SecurityContextImpl(new UsernamePasswordAuthenticationToken("testUser", "password", List.of(new SimpleGrantedAuthority("ROLE_MODERATOR")))));
    }

    // Helper method to create and persist Questionnaire
    private Questionnaire createAndPersistQuestionnaire() {
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(questionnaire);
        return questionnaire;
    }

    // Helper method to create and persist ExportTemplate
    private ExportTemplate createAndPersistExportTemplate(String name, String filename, ConfigurationGroup configGroup, Questionnaire questionnaire) {
        ExportTemplate exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        exportTemplate.setName(name);
        exportTemplate.setFilename(filename);
        exportTemplate.setConfigurationGroup(configGroup);
        exportTemplate.setQuestionnaire(questionnaire);
        exportTemplateDao.merge(exportTemplate);
        return exportTemplate;
    }

    /**
     * Test of {@link QuestionnaireService#validateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} and valid logo file
     */
    @Test
    public void testValidateQuestionnaireWithValidLogo() {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        BindingResult bindingResultMock = mock(BindingResult.class);

        // Act
        questionnaireService.validateQuestionnaire(
                validQuestionnaireDTO,
                MultipartFileUtils.getValidLogoFile(),
                bindingResultMock
        );

        // Assert
        verify(questionnaireDTOValidator).validate(validQuestionnaireDTO, bindingResultMock);
        // Since the logo is valid, the rejectValue method should not be called
        verify(bindingResultMock, never()).rejectValue(any(), any(), any());
    }

    /**
     * Test of {@link QuestionnaireService#validateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} and invalid logo file
     */
    @Test
    public void testValidateQuestionnaireWithInvalidLogo() {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        BindingResult bindingResultMock = mock(BindingResult.class);

        // Simulate the behavior of the LogoValidator to trigger rejectValue
        doAnswer(invocation -> {
            BindingResult bindingResult = invocation.getArgument(1);
            bindingResult.rejectValue(MultipartFileUtils.LOGO_FIELD_NAME, "error.wrongImageType");
            return null;
        }).when(logoValidator).validateLogo(any(MultipartFile.class), any(BindingResult.class));

        // Act
        questionnaireService.validateQuestionnaire(
                validQuestionnaireDTO,
                MultipartFileUtils.getInvalidLogoFile(),
                bindingResultMock
        );

        // Assert
        verify(questionnaireDTOValidator).validate(validQuestionnaireDTO, bindingResultMock);
        // Since the logo is invalid, the rejectValue method should be called
        verify(bindingResultMock, atLeastOnce()).rejectValue(eq(MultipartFileUtils.LOGO_FIELD_NAME), eq("error.wrongImageType"));
    }

    /**
     * Test of {@link QuestionnaireService#validateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} and no logo file
     */
    @Test
    public void testValidateQuestionnaireWithNoLogo() {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        BindingResult bindingResultMock = mock(BindingResult.class);

        // Act
        questionnaireService.validateQuestionnaire(
                validQuestionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                bindingResultMock
        );

        // Assert
        verify(questionnaireDTOValidator).validate(validQuestionnaireDTO, bindingResultMock);
        // Since no logo is provided, the rejectValue method should not be called
        verify(bindingResultMock, never()).rejectValue(anyString(), anyString(), anyString());
    }

    /**
     * Test of {@link QuestionnaireService#validateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} and null logo file
     */
    @Test
    public void testValidateQuestionnaireWithNullLogo() {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        MultipartFile nullLogoFile = null;
        BindingResult bindingResultMock = mock(BindingResult.class);

        // Act
        questionnaireService.validateQuestionnaire(validQuestionnaireDTO, nullLogoFile, bindingResultMock);

        // Assert
        verify(questionnaireDTOValidator).validate(validQuestionnaireDTO, bindingResultMock);
        // Since the logo is null, the rejectValue method should not be called
        verify(bindingResultMock, never()).rejectValue(anyString(), anyString(), anyString());
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} without ID, implying a new questionnaire creation
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_CreateNew() {
        // Arrange
        QuestionnaireDTO newQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        newQuestionnaireDTO.setId(null);
        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Long validUserId = Helper.generatePositiveNonZeroLong();

        when(questionnaireFactory.createQuestionnaire(
                anyString(),
                anyString(),
                anyLong(),
                anyBoolean())
        ).thenReturn(newQuestionnaire);

        // Act
        Questionnaire createdQuestionnaire = questionnaireService.saveOrUpdateQuestionnaire(
                newQuestionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                validUserId
        );

        // Assert
        verify(questionService, times(0)).duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class));
        Assert.assertNotNull("The created questionnaire should not be null", createdQuestionnaire);

        questionnaireDao.remove(createdQuestionnaire);
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with ID, and admin can edit the questionnaire without executed surveys
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_AdminModeratorCanEditQuestionnaireWithoutExecutedSurveys() {
        // Arrange
        Questionnaire newValidQuestionnaire = createAndPersistQuestionnaire();
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        validQuestionnaireDTO.setId(newValidQuestionnaire.getId());
        Long validUserId = Helper.generatePositiveNonZeroLong();
        Questionnaire modifiableQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());

        when(authService.hasRoleOrAbove(UserRole.ROLE_MODERATOR)).thenReturn(true);
        doReturn(true).when(modifiableQuestionnaire).isModifiable();
        doReturn(Helper.generatePositiveNonZeroLong()).when(modifiableQuestionnaire).getId();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                validQuestionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                validUserId
        );

        // Assert
        Assert.assertNotNull("The updated questionnaire should not be null", result);
        verify(questionService, never()).duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class)); // Ensure no questions are copied, it's an update

        questionnaireDao.remove(newValidQuestionnaire);
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with ID, but admin can't edit the questionnaire with executed surveys
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_AdminModeratorCantEditQuestionnaireWithExecutedSurvey() {
        // Arrange
        Questionnaire existingQUestionnaire = createAndPersistQuestionnaire();
        Question testQuestion = QuestionTest.getNewValidQuestion(existingQUestionnaire);
        questionDao.merge(testQuestion);
        SliderFreetextAnswer testAnswer = SliderFreetextAnswerTest.getNewValidSliderFreetextAnswer();
        testAnswer.setQuestion(testQuestion);
        answerDao.merge(testAnswer);
        Bundle testBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundle);
        Encounter testEncounter = EncounterTest.getNewValidEncounter(testBundle);
        encounterDao.merge(testEncounter);
        Response testResponse = new Response(testAnswer, testEncounter);
        responseDao.merge(testResponse);
        questionnaireDao.merge(existingQUestionnaire);

        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        questionnaireDTO.setId(existingQUestionnaire.getId());
        Long userId = Helper.generatePositiveNonZeroLong();
        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        when(authService.hasRoleOrAbove(UserRole.ROLE_MODERATOR)).thenReturn(true);
        when(questionnaireFactory.createQuestionnaire(any(), any(), any(), any())).thenReturn(newQuestionnaire);

        // Mock question duplication with empty maps
        when(questionService.duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class))).thenReturn(newQuestionnaire);
        when(questionService.getMappingForDuplicatedQuestions(any(Questionnaire.class), any(Questionnaire.class))).thenReturn(new MapHolder(Collections.emptyMap(), Collections.emptyMap()));

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                questionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                userId
        );

        // Assert
        Assert.assertNotNull("The created questionnaire should not be null", result);
        verify(questionService, atLeastOnce()).duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class)); // Ensure questions are copied, implying it's an update

        responseDao.remove(testResponse);
        encounterDao.remove(testEncounter);
        bundleDao.remove(testBundle);
        questionnaireDao.remove(existingQUestionnaire);
        questionnaireDao.remove(result);
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with ID, and editor cannot edit the questionnaire if it has executed surveys
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_EditorCannotEditWithExecutedSurveys() {
        // Arrange
        Questionnaire existingQUestionnaire = createAndPersistQuestionnaire();
        Question existingQuestion = QuestionTest.getNewValidQuestion(existingQUestionnaire);
        questionDao.merge(existingQuestion);
        SliderFreetextAnswer existingAnswer = SliderFreetextAnswerTest.getNewValidSliderFreetextAnswer();
        existingAnswer.setQuestion(existingQuestion);
        answerDao.merge(existingAnswer);
        Bundle existingBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(existingBundle);
        Encounter testEncounter = EncounterTest.getNewValidEncounter(existingBundle);
        encounterDao.merge(testEncounter);
        Response testResponse = new Response(existingAnswer, testEncounter);
        responseDao.merge(testResponse);
        questionnaireDao.merge(existingQUestionnaire);

        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        questionnaireDTO.setId(existingQUestionnaire.getId());
        Long userId = Helper.generatePositiveNonZeroLong();
        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        when(authService.hasExactRole(UserRole.ROLE_EDITOR)).thenReturn(true);
        when(questionnaireFactory.createQuestionnaire(any(), any(), any(), any())).thenReturn(newQuestionnaire);

        // Mock question duplication with empty maps
        when(questionService.duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class))).thenReturn(newQuestionnaire);
        when(questionService.getMappingForDuplicatedQuestions(any(Questionnaire.class), any(Questionnaire.class))).thenReturn(new MapHolder(Collections.emptyMap(), Collections.emptyMap()));

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                questionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                userId
        );

        // Assert
        Assert.assertNotNull("The created questionnaire should not be null", result);
        verify(questionService, atLeastOnce()).duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class)); // Ensure questions are copied, implying it's an update

        responseDao.remove(testResponse);
        encounterDao.remove(testEncounter);
        bundleDao.remove(existingBundle);
        questionnaireDao.remove(existingQUestionnaire);
        questionnaireDao.remove(result);
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with ID, and editor cannot edit the questionnaire if it belongs to a bundle that is enabled
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_EditorCannotEditIfPartOfEnabledBundle() throws Exception {
        // Arrange
        Questionnaire testQuestionnaire = createAndPersistQuestionnaire();
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        validQuestionnaireDTO.setId(testQuestionnaire.getId());
        Long validUserId = Helper.generatePositiveNonZeroLong();

        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire copiedQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        BundleQuestionnaire enabledBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        enabledBundleQuestionnaire.setIsEnabled(true);
        List<BundleQuestionnaire> bundleQuestionnaireList = List.of(enabledBundleQuestionnaire);

        when(bundleService.findByQuestionnaireId(any())).thenReturn(bundleQuestionnaireList);
        when(authService.hasExactRole(UserRole.ROLE_EDITOR)).thenReturn(true);
        when(questionnaireFactory.createQuestionnaire(any(), any(), any(), any())).thenReturn(copiedQuestionnaire);
        doReturn(true).when(existingQuestionnaire).isModifiable();
        doReturn(Helper.generatePositiveNonZeroLong()).when(existingQuestionnaire).getId();

        // Mock question duplication with empty maps
        when(questionService.duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class))).thenReturn(copiedQuestionnaire);
        when(questionService.getMappingForDuplicatedQuestions(any(Questionnaire.class), any(Questionnaire.class))).thenReturn(new MapHolder(Collections.emptyMap(), Collections.emptyMap()));

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                validQuestionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                validUserId
        );

        // Assert
        Assert.assertNotNull("The created questionnaire should not be null", result);
        verify(questionService, times(1)).duplicateQuestionsToNewQuestionnaire(anySet(), eq(copiedQuestionnaire)); // Ensure questions are copied to the new questionnaire

        questionnaireDao.remove(testQuestionnaire);
        questionnaireDao.remove(copiedQuestionnaire);

    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with ID, and editor can edit the questionnaire if it does not belong to a bundle that is enabled
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_EditorCanEditIfNotPartOfEnabledBundle() {
        // Arrange
        Questionnaire testQuestionnaire = createAndPersistQuestionnaire();
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        validQuestionnaireDTO.setId(testQuestionnaire.getId());
        Long validUserId = Helper.generatePositiveNonZeroLong();

        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire copiedQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        // Create a BundleQuestionnaire with isEnabled set to false
        BundleQuestionnaire newValidBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        newValidBundleQuestionnaire.setIsEnabled(false);
        List<BundleQuestionnaire> bundleQuestionnaireList = List.of(newValidBundleQuestionnaire);

        when(bundleService.findByQuestionnaireId(any())).thenReturn(bundleQuestionnaireList);
        when(authService.hasExactRole(UserRole.ROLE_EDITOR)).thenReturn(true);
        when(questionnaireFactory.createQuestionnaire(any(), any(), any(), any())).thenReturn(copiedQuestionnaire);
        doReturn(true).when(existingQuestionnaire).isModifiable();
        doReturn(1L).when(existingQuestionnaire).getId();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                validQuestionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                validUserId
        );

        // Assert
        Assert.assertNotNull("The updated questionnaire should not be null", result);
        verify(questionService, never()).duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class)); // Ensure no questions are copied, implying it's an update

        questionnaireDao.remove(testQuestionnaire);
        questionnaireDao.remove(copiedQuestionnaire);
    }

    /**
     * Test of {@link QuestionnaireService#getQuestionnaireDTOById}<br>
     * Valid input: null ID
     */
    @Test
    public void testGetQuestionnaireDTOById_NullId() {
        Optional<QuestionnaireDTO> result = questionnaireService.getQuestionnaireDTOById(null);
        assertTrue("The result should be empty for a null ID", result.isEmpty());
    }

    /**
     * Test of {@link QuestionnaireService#getQuestionnaireDTOById}<br>
     * Valid input: invalid ID (0 or negative)
     */
    @Test
    public void testGetQuestionnaireDTOById_InvalidId() {
        Optional<QuestionnaireDTO> result = questionnaireService.getQuestionnaireDTOById(0L);
        assertTrue("The result should be empty for an Id of 0", result.isEmpty());

        result = questionnaireService.getQuestionnaireDTOById(-1L);
        assertTrue("The result should be empty for a negative Id", result.isEmpty());
    }

    /**
     * Test of {@link QuestionnaireService#getQuestionnaireDTOById}<br>
     * Valid input: ID that does not exist in the database
     */
    @Test
    public void testGetQuestionnaireDTOById_NotFound() {

        Optional<QuestionnaireDTO> result = questionnaireService.getQuestionnaireDTOById(1L);
        Assert.assertFalse("The result should not be present for a non-existent ID", result.isPresent());
    }

    /**
     * Test of {@link QuestionnaireService#getQuestionnaireDTOById}<br>
     * Valid input: valid ID that exists in the database
     */
    @Test
    public void testGetQuestionnaireDTOById_Found() {
        Questionnaire existingQuestionnaire = createAndPersistQuestionnaire();

        QuestionnaireDTO expectedQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();

        when(questionnaireDTOMapper.apply(existingQuestionnaire))
                .thenReturn(expectedQuestionnaireDTO);

        Optional<QuestionnaireDTO> result = questionnaireService.getQuestionnaireDTOById(existingQuestionnaire.getId());
        assertTrue("The result should be present for a valid ID", result.isPresent());
        assertEquals("The result should match the expected QuestionnaireDTO", expectedQuestionnaireDTO, result.get());

        questionnaireDao.remove(existingQuestionnaire);
    }

    /**
     * Test of {@link QuestionnaireService#processLocalizedText}<br>
     * Valid input: valid {@link QuestionnaireDTO} with localized welcome and final texts containing unnecessary HTML tags
     */
    @Test
    public void testProcessLocalizedText() {
        // Arrange
        QuestionnaireDTO testQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();

        // Creating welcome text map with unnecessary HTML tags
        SortedMap<String, String> welcomeTextMap = new TreeMap<>();
        welcomeTextMap.put("en", "<p><br></p>");  // This should be cleared
        welcomeTextMap.put("de", "<br>");         // This should be cleared
        welcomeTextMap.put("nl", "Hoi");    // This should remain unchanged

        // Creating final text map with unnecessary HTML tags
        SortedMap<String, String> finalTextMap = new TreeMap<>();
        finalTextMap.put("en", "Thanks!");      // This should remain unchanged
        finalTextMap.put("de", "<br>");           // This should be cleared
        finalTextMap.put("nl", "<p><br></p>");    // This should be cleared

        testQuestionnaireDTO.setLocalizedWelcomeText(welcomeTextMap);
        testQuestionnaireDTO.setLocalizedFinalText(finalTextMap);

        // Act
        questionnaireService.processLocalizedText(testQuestionnaireDTO);

        // Assert
        // Verify that unnecessary HTML tags are removed from the welcome text map
        assertEquals("The English welcome text should be empty", "", testQuestionnaireDTO.getLocalizedWelcomeText().get("en"));
        assertEquals("The German welcome text should be empty", "", testQuestionnaireDTO.getLocalizedWelcomeText().get("de"));
        assertEquals("The Dutch welcome text should be 'Hoi'", "Hoi", testQuestionnaireDTO.getLocalizedWelcomeText().get("nl"));

        // Verify that unnecessary HTML tags are removed from the final text map
        assertEquals("The English final text should be 'Thanks!'", "Thanks!", testQuestionnaireDTO.getLocalizedFinalText().get("en"));
        assertEquals("The German final text should be empty", "", testQuestionnaireDTO.getLocalizedFinalText().get("de"));
        assertEquals("The Dutch final text should be empty", "", testQuestionnaireDTO.getLocalizedFinalText().get("nl"));
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with a new logo to upload
     */
    @Test
    public void testUploadNewLogo() {
        // Arrange
        Questionnaire existingQuestionnaire = createAndPersistQuestionnaire();
        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        questionnaireDTO.setId(existingQuestionnaire.getId());
        Long userId = Helper.generatePositiveNonZeroLong();
        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        when(questionnaireFactory.createQuestionnaire(anyString(), anyString(), anyLong(), anyBoolean()))
                .thenReturn(newQuestionnaire);
        // Mock question duplication with empty maps
        when(questionService.duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class)))
                .thenReturn(newQuestionnaire);
        when(questionService.getMappingForDuplicatedQuestions(any(Questionnaire.class), any(Questionnaire.class)))
                .thenReturn(new MapHolder(Collections.emptyMap(), Collections.emptyMap()));

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                questionnaireDTO,
                MultipartFileUtils.getValidLogoFile(),
                userId
        );

        // Assert
        Assert.assertNotNull("The created questionnaire should not be null", result);
        assertEquals("The logo should be set correctly", "logo.png", result.getLogo());

        questionnaireDao.remove(existingQuestionnaire);
        questionnaireDao.remove(result);
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} requesting deletion of the existing logo
     */
    @Test
    public void testDeleteExistingLogo(){
        // Arrange
        Questionnaire existingQuestionnaire = createAndPersistQuestionnaire();
        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        questionnaireDTO.setId(existingQuestionnaire.getId());
        questionnaireDTO.setDeleteLogo(true);
        Long userId = Helper.generatePositiveNonZeroLong();
        existingQuestionnaire.setLogo(MultipartFileUtils.VALID_LOGO_FILENAME);

        when(questionnaireFactory.createQuestionnaire(anyString(), anyString(), anyLong(), anyBoolean()))
                .thenReturn(existingQuestionnaire);

        // Mock question duplication with empty maps
        when(questionService.duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class)))
                .thenReturn(existingQuestionnaire);
        when(questionService.getMappingForDuplicatedQuestions(any(Questionnaire.class), any(Questionnaire.class)))
            .thenReturn(new MapHolder(Collections.emptyMap(), Collections.emptyMap()));

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                questionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                userId
        );

        // Assert
        Assert.assertNotNull("The updated questionnaire should not be null", result);
        Assert.assertNull("The logo should be null after deletion", result.getLogo());

        questionnaireDao.remove(existingQuestionnaire);
    }

    @Test
    public void testCopyExportTemplates(){
        // Arrange
        Questionnaire existingQuestionnaire = createAndPersistQuestionnaire();
        Questionnaire newQuestionnaire = createAndPersistQuestionnaire();

        ConfigurationGroup configGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();
        configGroup.setName("TestConfigurationGroup");
        configGroup.setPosition(1);
        configGroup.setRepeating(false);
        configGroup.setLabelMessageCode("TestMessageCode");
        configurationGroupDao.merge(configGroup);

        ExportTemplate newExportTemplate1 = createAndPersistExportTemplate("test_test_test1", "test1.json", configGroup, existingQuestionnaire);
        ExportTemplate newExportTemplate2 = createAndPersistExportTemplate("test_test_test2", "test2.json", configGroup, existingQuestionnaire);
        ExportTemplate newExportTemplate3 = createAndPersistExportTemplate("test_test_test3", "test3.json", configGroup, existingQuestionnaire);
        Set<ExportTemplate> exportTemplates = new HashSet<>(List.of(newExportTemplate1, newExportTemplate2, newExportTemplate3));

        when(fileUtils.generateFileNameForExportTemplate(anyString(), anyLong())).thenReturn("TestGeneratedFileName");

        // Act
        Set<ExportTemplate> copiedExportTemplates = questionnaireService.copyExportTemplates(exportTemplates, newQuestionnaire);

        // Assert
        assertEquals("The size for the original and the copied export templates fail to match", copiedExportTemplates.size(), exportTemplates.size());
        for(ExportTemplate exportTemplate : copiedExportTemplates){
            assertEquals("Export templates are not equal",exportTemplate.getQuestionnaire(), newQuestionnaire);
        }
        questionnaireDao.remove(existingQuestionnaire);
        questionnaireDao.remove(newQuestionnaire);
    }

    @Test
    public void testCopyExportTemplates_success() throws Exception {
        // Arrange
        Questionnaire existingQuestionnaire = createAndPersistQuestionnaire();
        Questionnaire newQuestionnaire = createAndPersistQuestionnaire();

        ConfigurationGroup configGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();
        configGroup.setName("TestConfigurationGroup");
        configGroup.setPosition(1);
        configGroup.setRepeating(false);
        configGroup.setLabelMessageCode("TestMessageCode");
        configurationGroupDao.merge(configGroup);

        ExportTemplate exportTemplateToCopy = createAndPersistExportTemplate("Template 1", "template1.json", configGroup, existingQuestionnaire);
        exportTemplateToCopy.setName("Template 1");
        exportTemplateToCopy.setFilename("template1.json");
        exportTemplateToCopy.setConfigurationGroup(configGroup);
        exportTemplateToCopy.setQuestionnaire(existingQuestionnaire);

        Set<ExportTemplate> templates = new HashSet<>();
        templates.add(exportTemplateToCopy);
        when(fileUtils.generateFileNameForExportTemplate(anyString(), anyLong())).thenReturn("template1_copy.json");
        doNothing().when(fileUtils).copyTemplateFile(anyString(), anyString());

        // Act
        Set<ExportTemplate> copiedTemplates = questionnaireService.copyExportTemplates(templates, newQuestionnaire);

        // Assert
        assertEquals(1, copiedTemplates.size());
        ExportTemplate copiedTemplate = copiedTemplates.iterator().next();
        assertEquals("Template 1", copiedTemplate.getName());
        assertEquals("template1_copy.json", copiedTemplate.getFilename());

        // Verify
        verify(fileUtils).generateFileNameForExportTemplate(eq("template1.json"), anyLong());
        verify(fileUtils).copyTemplateFile("template1.json", "template1_copy.json");


        questionnaireDao.remove(existingQuestionnaire);
        questionnaireDao.remove(newQuestionnaire);
        configurationGroupDao.remove(configGroup);
    }

    @Test
    public void testCopyExportTemplates_failureOnFileCopy() throws Exception {
        // Arrange
        Questionnaire existingQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(existingQuestionnaire);
        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(newQuestionnaire);

        ConfigurationGroup newValidConfigurationGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();
        newValidConfigurationGroup.setName("TestConfigurationGroup");
        newValidConfigurationGroup.setPosition(1);
        newValidConfigurationGroup.setRepeating(false);
        newValidConfigurationGroup.setLabelMessageCode("TestMessageCode");
        configurationGroupDao.merge(newValidConfigurationGroup);

        ExportTemplate templateToCopy = createAndPersistExportTemplate("Template 1", "template1.json", newValidConfigurationGroup, existingQuestionnaire);
        Set<ExportTemplate> templatesToCopy = new HashSet<>(List.of(templateToCopy));

        when(fileUtils.generateFileNameForExportTemplate(anyString(), anyLong())).thenReturn("template1_copy.json");
        doThrow(new IOException("File copy failed")).when(fileUtils).copyTemplateFile(anyString(), anyString());
        doNothing().when(fileUtils).deleteExportTemplateFrom(anyString());

        // Act
        Set<ExportTemplate> copiedTemplates = questionnaireService.copyExportTemplates(templatesToCopy, newQuestionnaire);

        // Assert
        assertTrue(copiedTemplates.isEmpty());  // Ensure no templates were copied due to failure

        // Verify
        verify(fileUtils).generateFileNameForExportTemplate(eq("template1.json"), anyLong());
        verify(fileUtils).copyTemplateFile("template1.json", "template1_copy.json");
        verify(fileUtils).deleteExportTemplateFrom("template1_copy.json");

        questionnaireDao.remove(existingQuestionnaire);
        questionnaireDao.remove(newQuestionnaire);
    }

    @Test
    public void testCanEditQuestionnaireWithReason_ReturnsCorrectMessages() {
        // Arrange
        Questionnaire existingQUestionnaire = createAndPersistQuestionnaire();
        Question testQuestion = QuestionTest.getNewValidQuestion(existingQUestionnaire);
        questionDao.merge(testQuestion);
        SliderFreetextAnswer testAnswer = SliderFreetextAnswerTest.getNewValidSliderFreetextAnswer();
        testAnswer.setQuestion(testQuestion);
        answerDao.merge(testAnswer);
        Bundle testBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundle);
        Encounter testEncounter = EncounterTest.getNewValidEncounter(testBundle);
        encounterDao.merge(testEncounter);
        Response testResponse = new Response(testAnswer, testEncounter);
        responseDao.merge(testResponse);
        questionnaireDao.merge(existingQUestionnaire);

        when(authService.hasExactRole(UserRole.ROLE_EDITOR)).thenReturn(true);
        String errorMessage = "Questionnaire has executed Encounters";
        when(messageSource.getMessage(eq("questionnaire.message.executedEncounters"), any(), any(), any()))
                .thenReturn(errorMessage);

        QuestionnaireDTO dto = new QuestionnaireDTO();
        dto.setId(existingQUestionnaire.getId());

        // Act
        Pair<Boolean, String> result = questionnaireService.canEditQuestionnaireWithReason(dto);

        // Assert
        assertFalse("Editor should not be allowed to edit questionnaires with executed encounters", result.getLeft());
        assertEquals("The error message should be set", errorMessage, result.getRight());
    }

    @Test
    public void testDisapproveQuestionnaire_Success() {
        // Arrange
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaire.setStatusApprove();
        questionnaireDao.merge(questionnaire);

        when(authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)).thenReturn(true);

        // Act
        questionnaireService.disapproveQuestionnaire(questionnaire.getId(), Locale.getDefault());

        // Assert
        Questionnaire updated = questionnaireDao.getElementById(questionnaire.getId());
        assertEquals(ApprovalStatus.DRAFT, updated.getApprovalStatus());
    }

    @Test(expected = AccessDeniedException.class)
    public void testDisapproveQuestionnaire_AccessDenied() {
        // Arrange
        when(authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)).thenReturn(false);
        when(messageSource.getMessage(eq("questionnaire.error.unauthorized"), any(), any()))
                .thenReturn("Unauthorized");

        // Act
        questionnaireService.disapproveQuestionnaire(1L, Locale.getDefault());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDisapproveQuestionnaire_QuestionnaireNotFound() {
        // Arrange
        when(authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)).thenReturn(true);
        when(messageSource.getMessage(eq("questionnaire.error.notFound"), any(), any()))
                .thenReturn("Questionnaire not found");

        // Act
        questionnaireService.disapproveQuestionnaire(null, Locale.ENGLISH);
    }

    @Test(expected = IllegalStateException.class)
    public void testDisapproveQuestionnaire_AlreadyDraft() {
        // Arrange
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaire.setStatusDraft();
        questionnaireDao.merge(questionnaire);
        when(authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)).thenReturn(true);
        when(messageSource.getMessage(eq("questionnaire.error.notApproved"), any(), any()))
                .thenReturn("Questionnaire is not approved");

        // Act
        questionnaireService.disapproveQuestionnaire(questionnaire.getId(), Locale.getDefault());
    }

    @Test(expected = IllegalStateException.class)
    public void testDisapproveQuestionnaire_PartOfEnabledBundle() {
        // Arrange
        Questionnaire exsitingQuestionnaire = createAndPersistQuestionnaire();
        exsitingQuestionnaire.setStatusApprove();
        questionnaireDao.merge(exsitingQuestionnaire);
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        validQuestionnaireDTO.setId(exsitingQuestionnaire.getId());

        BundleQuestionnaire enabledBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        enabledBundleQuestionnaire.setIsEnabled(true);
        List<BundleQuestionnaire> bundleQuestionnaireList = List.of(enabledBundleQuestionnaire);

        when(bundleService.findByQuestionnaireId(any())).thenReturn(bundleQuestionnaireList);
        when(authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)).thenReturn(true);
        when(messageSource.getMessage(eq("questionnaire.error.enabledBundle"), any(), any()))
                .thenReturn("Questionnaire is part of an enabled bundle");

        // Act
        questionnaireService.disapproveQuestionnaire(exsitingQuestionnaire.getId(), Locale.getDefault());
    }

    @Test
    public void testApproveQuestionnaire_Success() {
        // Arrange
        Questionnaire exsitingQuestionnaire = createAndPersistQuestionnaire();

        when(authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)).thenReturn(true);

        // Act
        questionnaireService.approveQuestionnaire(exsitingQuestionnaire.getId(), Locale.getDefault());

        // Assert
        Questionnaire updated = questionnaireDao.getElementById(exsitingQuestionnaire.getId());
        assertEquals(ApprovalStatus.APPROVED, updated.getApprovalStatus());
    }

    @Test(expected = AccessDeniedException.class)
    public void testApproveQuestionnaire_AccessDenied() {
        // Arrange
        when(authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)).thenReturn(false);
        when(messageSource.getMessage(eq("questionnaire.error.unauthorized"), any(), any()))
                .thenReturn("Unauthorized");

        // Act
        questionnaireService.approveQuestionnaire(1L, Locale.getDefault());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testApproveQuestionnaire_QuestionnaireNotFound() {
        // Arrange
        when(authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)).thenReturn(true);
        when(messageSource.getMessage(eq("questionnaire.error.notFound"), any(), any()))
                .thenReturn("Questionnaire not found");

        // Act
        questionnaireService.approveQuestionnaire(null, Locale.getDefault());
    }

    @After
    public void cleanUp() {
        SecurityContextHolder.clearContext();
    }
}