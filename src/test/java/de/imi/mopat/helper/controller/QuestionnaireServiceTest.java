package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.helper.model.QuestionnaireFactory;
import de.imi.mopat.model.*;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireDTOTest;
import de.imi.mopat.model.score.*;
import de.imi.mopat.model.user.UserRole;
import de.imi.mopat.utils.Helper;
import de.imi.mopat.utils.MultipartFileUtils;
import de.imi.mopat.validator.LogoValidator;
import de.imi.mopat.validator.QuestionnaireDTOValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class QuestionnaireServiceTest {


    private Random random;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ConfigurationDao configurationDao;

    @Mock
    private QuestionnaireDao questionnaireDao;

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

    @InjectMocks
    private QuestionnaireService questionnaireService;

    @Mock
    private ExportTemplateDao exportTemplateDao;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // Helper method to create a Score object with a BinaryExpression
    private Score createValidScoreWithBinaryExpression(Questionnaire questionnaire) {
        Score score = new Score();
        score.setName("Test Binary Score");
        score.setQuestionnaire(questionnaire);

        // Create and set a BinaryExpression
        BinaryExpression binaryExpression = createValidBinaryExpression();
        score.setExpression(binaryExpression);

        return score;
    }

    // Helper method to create a Score object with a MultiExpression
    private Score createValidScoreWithMultiExpression(Questionnaire questionnaire) {
        Score score = new Score();
        score.setName("Test Multi Score");
        score.setQuestionnaire(questionnaire);

        MultiExpression multiExpression = createValidMultiExpression();
        score.setExpression(multiExpression);

        return score;
    }

    // Helper method to create a BinaryExpression
    private BinaryExpression createValidBinaryExpression() {
        BinaryExpression expression = new BinaryExpression();
        BinaryOperator binaryOperator = mock(BinaryOperator.class);
        expression.setOperator(binaryOperator);

        UnaryExpression childExpression1 = createValidUnaryExpression();
        UnaryExpression childExpression2 = createValidUnaryExpression();

        List<Expression> expressions = new ArrayList<>();
        expressions.add(childExpression1);
        expressions.add(childExpression2);

        expression.setExpressions(expressions);

        return expression;
    }

    // Helper method to create a MultiExpression
    private MultiExpression createValidMultiExpression() {
        MultiExpression expression = new MultiExpression();
        MultiOperator multiOperator = mock(MultiOperator.class);
        expression.setOperator(multiOperator);

        UnaryExpression childExpression1 = createValidUnaryExpression();
        UnaryExpression childExpression2 = createValidUnaryExpression();

        List<Expression> expressions = new ArrayList<>();
        expressions.add(childExpression1);
        expressions.add(childExpression2);

        expression.setExpressions(expressions);

        return expression;
    }

    // Helper method to create a UnaryExpression
    private UnaryExpression createValidUnaryExpression() {
        UnaryExpression expression = new UnaryExpression();
        UnaryOperator unaryOperator = mock(UnaryOperator.class);
        expression.setOperator(unaryOperator);
        expression.setQuestion(new Question());
        return expression;
    }

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        random = new Random();

        // Mocking the behavior of the merge method to set an ID using a custom Answer
        doAnswer(invocation -> {
            Questionnaire questionnaire = invocation.getArgument(0);
            if (questionnaire.getId() == null) {
                Field idField = Questionnaire.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(questionnaire, random.nextLong());
            }
            return null;
        }).when(questionnaireDao).merge(any(Questionnaire.class));

        doAnswer(invocation -> {
            ExportTemplate exportTemplate = invocation.getArgument(0);
            if (exportTemplate.getId() == null) {
                Field idField = ExportTemplate.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(exportTemplate, random.nextLong());
            }
            return null;
        }).when(exportTemplateDao).merge(any(ExportTemplate.class));
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

        when(questionnaireDao.getElementById(anyLong())).thenReturn(null);
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
        verify(questionnaireDao, times(2)).merge(newQuestionnaire);
        verify(questionService, times(0)).duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class));
        Assert.assertNotNull("The created questionnaire should not be null", createdQuestionnaire);
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with ID, and admin can edit the questionnaire without executed surveys
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_AdminModeratorCanEditQuestionnaireWithoutExecutedSurveys() throws Exception {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long validUserId = Helper.generatePositiveNonZeroLong();
        Questionnaire modifiableQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());


        when(authService.hasRoleOrAbove(UserRole.ROLE_MODERATOR)).thenReturn(true);
        when(questionnaireDao.getElementById(any())).thenReturn(modifiableQuestionnaire);
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
        verify(questionnaireDao, times(1)).merge(modifiableQuestionnaire); // Ensure the existing questionnaire is merged
        verify(questionService, never()).duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class)); // Ensure no questions are copied, it's an update
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with ID, but admin can't edit the questionnaire with executed surveys
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_AdminModeratorCantEditQuestionnaireWithExecutedSurvey() {
        // Arrange
        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long userId = Helper.generatePositiveNonZeroLong();

        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        when(authService.hasRoleOrAbove(UserRole.ROLE_MODERATOR)).thenReturn(true);
        when(questionnaireDao.getElementById(any())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(any(), any(), any(), any())).thenReturn(newQuestionnaire);
        doReturn(false).when(existingQuestionnaire).isModifiable();
        doReturn(Helper.generatePositiveNonZeroLong()).when(existingQuestionnaire).getId();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                questionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                userId
        );

        // Assert
        Assert.assertNotNull("The created questionnaire should not be null", result);
        verify(questionnaireDao, never()).merge(existingQuestionnaire); // Ensure merge is not called on the specific questionnaire object (no update)
        verify(questionnaireDao, times(2)).merge(any()); // Ensure a questionnaire is merged
        verify(questionService, atLeastOnce()).duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class)); // Ensure questions are copied, implying it's an update
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with ID, and editor cannot edit the questionnaire if it has executed surveys
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_EditorCannotEditWithExecutedSurveys() throws Exception {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long validUserId = Helper.generatePositiveNonZeroLong();

        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire copiedQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        when(authService.hasExactRole(UserRole.ROLE_EDITOR)).thenReturn(true);
        when(questionnaireDao.getElementById(any())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(any(), any(), any(), any())).thenReturn(copiedQuestionnaire);
        doReturn(false).when(existingQuestionnaire).isModifiable();
        doReturn(Helper.generatePositiveNonZeroLong()).when(existingQuestionnaire).getId();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                validQuestionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                validUserId
        );

        // Assert
        Assert.assertNotNull("The created questionnaire should not be null", result);
        verify(questionnaireDao, never()).merge(existingQuestionnaire); // Ensure merge is not called on the specific questionnaire object (no update)
        verify(questionnaireDao, times(2)).merge(copiedQuestionnaire); // Ensure a new questionnaire is merged
        verify(questionService, times(1)).duplicateQuestionsToNewQuestionnaire(anySet(), eq(copiedQuestionnaire)); // Ensure questions are copied to the new questionnaire
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with ID, and editor cannot edit the questionnaire if it belongs to a bundle that is enabled
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_EditorCannotEditIfPartOfEnabledBundle() throws Exception {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long validUserId = Helper.generatePositiveNonZeroLong();

        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire copiedQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        BundleQuestionnaire enabledBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        enabledBundleQuestionnaire.setIsEnabled(true);
        List<BundleQuestionnaire> bundleQuestionnaireList = List.of(enabledBundleQuestionnaire);

        when(bundleService.findByQuestionnaireId(any())).thenReturn(bundleQuestionnaireList);
        when(authService.hasExactRole(UserRole.ROLE_EDITOR)).thenReturn(true);
        when(questionnaireDao.getElementById(any())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(any(), any(), any(), any())).thenReturn(copiedQuestionnaire);
        doReturn(true).when(existingQuestionnaire).isModifiable();
        doReturn(Helper.generatePositiveNonZeroLong()).when(existingQuestionnaire).getId();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                validQuestionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                validUserId
        );

        // Assert
        Assert.assertNotNull("The created questionnaire should not be null", result);
        verify(questionnaireDao, never()).merge(existingQuestionnaire); // Ensure merge is not called on the specific questionnaire object (no update)
        verify(questionnaireDao, times(2)).merge(copiedQuestionnaire); // Ensure a new questionnaire is merged
        verify(questionService, times(1)).duplicateQuestionsToNewQuestionnaire(anySet(), eq(copiedQuestionnaire)); // Ensure questions are copied to the new questionnaire
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with ID, and editor can edit the questionnaire if it does not belong to a bundle that is enabled
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_EditorCanEditIfNotPartOfEnabledBundle() throws Exception {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long validUserId = Helper.generatePositiveNonZeroLong();

        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire copiedQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        // Create a BundleQuestionnaire with isEnabled set to false
        BundleQuestionnaire newValidBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        newValidBundleQuestionnaire.setIsEnabled(false);
        List<BundleQuestionnaire> bundleQuestionnaireList = List.of(newValidBundleQuestionnaire);

        // Simulating all bundles are not enabled
        when(bundleService.findByQuestionnaireId(any())).thenReturn(bundleQuestionnaireList);

        when(authService.hasExactRole(UserRole.ROLE_EDITOR)).thenReturn(true);
        when(questionnaireDao.getElementById(any())).thenReturn(existingQuestionnaire);
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
        verify(questionnaireDao, times(1)).merge(any(Questionnaire.class)); // Ensure the existing questionnaire is merged
        verify(questionnaireDao, never()).merge(copiedQuestionnaire); // Ensure no new questionnaire is merged
        verify(questionService, never()).duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class)); // Ensure no questions are copied, implying it's an update
    }

    @Test
    public void testSaveOrUpdateQuestionnaire_NewQuestionnaireIdNull() {
        // Arrange
        Long userId = Helper.generatePositiveNonZeroLong();
        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire newQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());

        doReturn(existingQuestionnaire).when(questionnaireDao).getElementById(anyLong());
        doReturn(null).when(newQuestionnaire).getId();
        doReturn(newQuestionnaire).when(questionnaireFactory).createQuestionnaire(anyString(), anyString(), anyLong(), anyBoolean());

        // Assert
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("The new questionnaire must be persisted before saving versioning information.");

        // Act
        questionnaireService.saveOrUpdateQuestionnaire(
                questionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                userId
        );
    }

    /**
     * Test of {@link QuestionnaireService#getQuestionnaireDTOById}<br>
     * Valid input: null ID
     */
    @Test
    public void testGetQuestionnaireDTOById_NullId() {
        Optional<QuestionnaireDTO> result = questionnaireService.getQuestionnaireDTOById(null);
        Assert.assertTrue("The result should be empty for a null ID", result.isEmpty());
    }

    /**
     * Test of {@link QuestionnaireService#getQuestionnaireDTOById}<br>
     * Valid input: invalid ID (0 or negative)
     */
    @Test
    public void testGetQuestionnaireDTOById_InvalidId() {
        Optional<QuestionnaireDTO> result = questionnaireService.getQuestionnaireDTOById(0L);
        Assert.assertTrue("The result should be empty for an Id of 0", result.isEmpty());

        result = questionnaireService.getQuestionnaireDTOById(-1L);
        Assert.assertTrue("The result should be empty for a negative Id", result.isEmpty());
    }

    /**
     * Test of {@link QuestionnaireService#getQuestionnaireDTOById}<br>
     * Valid input: ID that does not exist in the database
     */
    @Test
    public void testGetQuestionnaireDTOById_NotFound() {
        when(questionnaireDao.getElementById(anyLong())).thenReturn(null);

        Optional<QuestionnaireDTO> result = questionnaireService.getQuestionnaireDTOById(1L);
        Assert.assertFalse("The result should not be present for a non-existent ID", result.isPresent());
    }

    /**
     * Test of {@link QuestionnaireService#getQuestionnaireDTOById}<br>
     * Valid input: valid ID that exists in the database
     */
    @Test
    public void testGetQuestionnaireDTOById_Found() {
        long validQuestionnaireId = Helper.generatePositiveNonZeroLong();
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        QuestionnaireDTO expectedQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();

        when(questionnaireDao.getElementById(validQuestionnaireId)).thenReturn(questionnaire);
        when(questionnaireDTOMapper.apply(questionnaire)).thenReturn(expectedQuestionnaireDTO);

        Optional<QuestionnaireDTO> result = questionnaireService.getQuestionnaireDTOById(validQuestionnaireId);
        Assert.assertTrue("The result should be present for a valid ID", result.isPresent());
        Assert.assertEquals("The result should match the expected QuestionnaireDTO", expectedQuestionnaireDTO, result.get());
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
        Assert.assertEquals("The English welcome text should be empty", "", testQuestionnaireDTO.getLocalizedWelcomeText().get("en"));
        Assert.assertEquals("The German welcome text should be empty", "", testQuestionnaireDTO.getLocalizedWelcomeText().get("de"));
        Assert.assertEquals("The Dutch welcome text should be 'Hoi'", "Hoi", testQuestionnaireDTO.getLocalizedWelcomeText().get("nl"));

        // Verify that unnecessary HTML tags are removed from the final text map
        Assert.assertEquals("The English final text should be 'Thanks!'", "Thanks!", testQuestionnaireDTO.getLocalizedFinalText().get("en"));
        Assert.assertEquals("The German final text should be empty", "", testQuestionnaireDTO.getLocalizedFinalText().get("de"));
        Assert.assertEquals("The Dutch final text should be empty", "", testQuestionnaireDTO.getLocalizedFinalText().get("nl"));
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with a new logo to upload
     */
    @Test
    public void testUploadNewLogo() {
        // Arrange
        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long userId = Helper.generatePositiveNonZeroLong();
        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        when(questionnaireDao.getElementById(any())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(anyString(), anyString(), anyLong(), anyBoolean()))
                .thenReturn(newQuestionnaire);
        doReturn(Helper.generatePositiveNonZeroLong()).when(existingQuestionnaire).getId();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                questionnaireDTO,
                MultipartFileUtils.getValidLogoFile(),
                userId
        );

        // Assert
        Assert.assertNotNull("The created questionnaire should not be null", result);
        Assert.assertEquals("The logo should be set correctly", "logo.png", result.getLogo());
        File uploadDir = new File(configurationDao.getImageUploadPath() + "/" + Constants.IMAGE_QUESTIONNAIRE + "/" + newQuestionnaire.getId());
        Assert.assertTrue("The upload directory should exist", uploadDir.exists());
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} requesting deletion of the existing logo
     */
    @Test
    public void testDeleteExistingLogo(){
        // Arrange
        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        questionnaireDTO.setDeleteLogo(true);
        Long userId = Helper.generatePositiveNonZeroLong();
        existingQuestionnaire.setLogo(MultipartFileUtils.VALID_LOGO_FILENAME);

        when(questionnaireDao.getElementById(anyLong())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(anyString(), anyString(), anyLong(), anyBoolean()))
                .thenReturn(existingQuestionnaire);
        doReturn(Helper.generatePositiveNonZeroLong()).when(existingQuestionnaire).getId();
        doReturn(true).when(existingQuestionnaire).isOriginal();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(
                questionnaireDTO,
                MultipartFileUtils.getEmptyLogo(),
                userId
        );

        // Assert
        Assert.assertNotNull("The updated questionnaire should not be null", result);
        Assert.assertNull("The logo should be null after deletion", result.getLogo());

        File deletedFile = new File(configurationDao.getImageUploadPath() +
                "/" +
                Constants.IMAGE_QUESTIONNAIRE + "/" +
                existingQuestionnaire.getId() +
                MultipartFileUtils.VALID_LOGO_FILENAME
        );
        Assert.assertFalse("The old logo file should be deleted", deletedFile.exists());
    }

    @Test
    public void testCopyExportTemplates(){
        // Arrange
        Set<ExportTemplate> exportTemplates = new HashSet<>();
        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        ExportTemplate newExportTemplate1 = ExportTemplateTest.getNewValidExportTemplate();
        ExportTemplate newExportTemplate2 = ExportTemplateTest.getNewValidExportTemplate();
        ExportTemplate newExportTemplate3 = ExportTemplateTest.getNewValidExportTemplate();
        newExportTemplate1.setFilename("test_test_test1");
        newExportTemplate2.setFilename("test_test_test2");
        newExportTemplate3.setFilename("test_test_test3");
        exportTemplates.add(newExportTemplate1);
        exportTemplates.add(newExportTemplate2);
        exportTemplates.add(newExportTemplate3);

        // Act

        Set<ExportTemplate> copiedExportTemplates = questionnaireService.copyExportTemplates(exportTemplates, newQuestionnaire);

        // Assert
        Assert.assertEquals("The size for the original and the copied export templates fail to match", copiedExportTemplates.size(), exportTemplates.size());

        for(ExportTemplate exportTemplate : copiedExportTemplates){
            Assert.assertEquals("Export templates are not equal",exportTemplate.getQuestionnaire(), newQuestionnaire);
        }

    }
}