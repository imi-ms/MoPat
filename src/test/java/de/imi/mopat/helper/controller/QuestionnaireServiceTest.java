package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireVersionDao;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.helper.model.QuestionnaireFactory;
import de.imi.mopat.model.*;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireDTOTest;
import de.imi.mopat.model.score.*;
import de.imi.mopat.validator.LogoValidator;
import de.imi.mopat.validator.QuestionnaireDTOValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class QuestionnaireServiceTest {

    public static final String VALID_LOGO_FILENAME = "test.png";
    public static final String VALID_LOGO_CONTENT_TYPE = "image/png";
    public static final String INVALID_LOGO_FILENAME = "test.fail";
    public static final String INVALID_LOGO_CONTENT_TYPE = "image/fail";
    public static final String LOGO_FIELD_NAME = "logo";

    public static final byte[] VALID_LOGO_CONTENT = new byte[] {(byte)137, (byte)80, (byte)78, (byte)71, (byte)13, (byte)10, (byte)26, (byte)10};
    public static final byte[] INVALID_LOGO_CONTENT = "Invalid content".getBytes();
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static final MultipartFile EMPTY_LOGO = new MockMultipartFile(LOGO_FIELD_NAME, EMPTY_BYTE_ARRAY);

    private Random random;

    @Mock
    private ConfigurationDao configurationDao;

    @Mock
    private QuestionnaireDao questionnaireDao;

    @Mock
    private QuestionService questionService;

    @Mock
    private QuestionnaireVersionDao questionnaireVersionDao;

    @Mock
    private QuestionnaireDTOMapper questionnaireDTOMapper;

    @Mock
    private QuestionnaireDTOValidator questionnaireDTOValidator;

    @Mock
    private AuthService authService;

    @InjectMocks
    private QuestionnaireService questionnaireService;

    @Mock
    private BundleService bundleService;

    @Mock
    private QuestionnaireFactory questionnaireFactory;

    @Mock
    private LogoValidator logoValidator;

    // Helper method to generate a positive random long ID
    private Long getPositiveId(){
        return random.nextLong() & Long.MAX_VALUE;
    }

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
    }

    /**
     * Test of {@link QuestionnaireService#validateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} and valid logo file
     */
    @Test
    public void testValidateQuestionnaireWithValidLogo() {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        MultipartFile validLogoFile = new MockMultipartFile(
                LOGO_FIELD_NAME,
                VALID_LOGO_FILENAME,
                VALID_LOGO_CONTENT_TYPE,
                VALID_LOGO_CONTENT);
        BindingResult bindingResultMock = mock(BindingResult.class);

        // Act
        questionnaireService.validateQuestionnaire(validQuestionnaireDTO, validLogoFile, bindingResultMock);

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
        MultipartFile invalidLogoFile = new MockMultipartFile(
                LOGO_FIELD_NAME,
                INVALID_LOGO_FILENAME,
                INVALID_LOGO_CONTENT_TYPE,
                INVALID_LOGO_CONTENT);
        BindingResult bindingResultMock = mock(BindingResult.class);

        // Simulate the behavior of the LogoValidator to trigger rejectValue
        doAnswer(invocation -> {
            BindingResult bindingResult = invocation.getArgument(1);
            bindingResult.rejectValue(LOGO_FIELD_NAME, "error.wrongImageType");
            return null;
        }).when(logoValidator).validateLogo(any(MultipartFile.class), any(BindingResult.class));

        // Act
        questionnaireService.validateQuestionnaire(validQuestionnaireDTO, invalidLogoFile, bindingResultMock);

        // Assert
        verify(questionnaireDTOValidator).validate(validQuestionnaireDTO, bindingResultMock);
        // Since the logo is invalid, the rejectValue method should be called
        verify(bindingResultMock, atLeastOnce()).rejectValue(eq(LOGO_FIELD_NAME), eq("error.wrongImageType"));
    }

    /**
     * Test of {@link QuestionnaireService#validateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} and no logo file
     */
    @Test
    public void testValidateQuestionnaireWithNoLogo() {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        MultipartFile noLogoFile = new MockMultipartFile(LOGO_FIELD_NAME, EMPTY_BYTE_ARRAY);
        BindingResult bindingResultMock = mock(BindingResult.class);

        // Act
        questionnaireService.validateQuestionnaire(validQuestionnaireDTO, noLogoFile, bindingResultMock);

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
        Long validUserId = getPositiveId();

        when(questionnaireDao.getElementById(anyLong())).thenReturn(null);
        when(questionnaireFactory.createQuestionnaire(
                anyString(),
                anyString(),
                anyLong(),
                anyLong(),
                anyBoolean())
        ).thenReturn(newQuestionnaire);

        // Act
        Questionnaire createdQuestionnaire = questionnaireService.saveOrUpdateQuestionnaire(newQuestionnaireDTO, EMPTY_LOGO, validUserId);

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
    public void testSaveOrUpdateQuestionnaire_AdminModeratorCanEditQuestionnaireWithoutExecutedSurveys() {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long validUserId = getPositiveId();

        Questionnaire modifiableQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());

        when(authService.hasRoleOrAbove("ROLE_MODERATOR")).thenReturn(true);
        when(questionnaireDao.getElementById(any())).thenReturn(modifiableQuestionnaire);
        doReturn(true).when(modifiableQuestionnaire).isModifiable();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(validQuestionnaireDTO, EMPTY_LOGO, validUserId);

        // Assert
        Assert.assertNotNull("The updated questionnaire should not be null", result);
        verify(questionnaireDao, times(2)).merge(modifiableQuestionnaire); // Ensure the existing questionnaire is merged
        verify(questionService, never()).duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class)); // Ensure no questions are copied, it's an update
        verify(modifiableQuestionnaire, never()).setCreatedBy(anyLong()); // Ensure setCreatedBy is not called, which is specific to new questionnaires
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} with ID, but admin can't edit the questionnaire with executed surveys
     */
    @Test
    public void testSaveOrUpdateQuestionnaire_AdminModeratorCantEditQuestionnaireWithExecutedSurvey() {
        // Arrange
        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long userId = getPositiveId();

        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        when(authService.hasRoleOrAbove("ROLE_MODERATOR")).thenReturn(true);
        when(questionnaireDao.getElementById(any())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(any(), any(), any(), any(), any())).thenReturn(newQuestionnaire);
        doReturn(false).when(existingQuestionnaire).isModifiable();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(questionnaireDTO, EMPTY_LOGO, userId);

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
    public void testSaveOrUpdateQuestionnaire_EditorCannotEditWithExecutedSurveys() {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long validUserId = getPositiveId();

        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire copiedQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        when(authService.hasExactRole("ROLE_EDITOR")).thenReturn(true);
        when(questionnaireDao.getElementById(any())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(any(), any(), any(), any(), any())).thenReturn(copiedQuestionnaire);
        doReturn(false).when(existingQuestionnaire).isModifiable();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(validQuestionnaireDTO, EMPTY_LOGO, validUserId);

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
    public void testSaveOrUpdateQuestionnaire_EditorCannotEditIfPartOfEnabledBundle() {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long validUserId = getPositiveId();

        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire copiedQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        BundleQuestionnaire enabledBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        enabledBundleQuestionnaire.setIsEnabled(true);
        List<BundleQuestionnaire> bundleQuestionnaireList = List.of(enabledBundleQuestionnaire);

        when(bundleService.findByQuestionnaire(any())).thenReturn(bundleQuestionnaireList);

        when(authService.hasExactRole("ROLE_EDITOR")).thenReturn(true);
        when(questionnaireDao.getElementById(any())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(any(), any(), any(), any(), any())).thenReturn(copiedQuestionnaire);
        doReturn(true).when(existingQuestionnaire).isModifiable();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(validQuestionnaireDTO, EMPTY_LOGO, validUserId);

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
    public void testSaveOrUpdateQuestionnaire_EditorCanEditIfNotPartOfEnabledBundle() {
        // Arrange
        QuestionnaireDTO validQuestionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long validUserId = getPositiveId();

        Questionnaire existingQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire copiedQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        // Create a BundleQuestionnaire with isEnabled set to false
        BundleQuestionnaire newValidBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        newValidBundleQuestionnaire.setIsEnabled(false);
        List<BundleQuestionnaire> bundleQuestionnaireList = List.of(newValidBundleQuestionnaire);

        // Simulating all bundles are not enabled
        when(bundleService.findByQuestionnaire(any())).thenReturn(bundleQuestionnaireList);

        when(authService.hasExactRole("ROLE_EDITOR")).thenReturn(true);
        when(questionnaireDao.getElementById(any())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(any(), any(), any(), any(), any())).thenReturn(copiedQuestionnaire);
        doReturn(true).when(existingQuestionnaire).isModifiable();

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(validQuestionnaireDTO, EMPTY_LOGO, validUserId);

        // Assert
        Assert.assertNotNull("The updated questionnaire should not be null", result);
        verify(questionnaireDao, times(2)).merge(any(Questionnaire.class)); // Ensure the existing questionnaire is merged
        verify(questionnaireDao, never()).merge(copiedQuestionnaire); // Ensure no new questionnaire is merged
        verify(questionService, never()).duplicateQuestionsToNewQuestionnaire(anySet(), any(Questionnaire.class)); // Ensure no questions are copied, implying it's an update
        verify(existingQuestionnaire, never()).setCreatedBy(anyLong()); // Ensure setCreatedBy is not called, which is specific to new questionnaires
    }

    /**
     * Test of {@link QuestionnaireService#saveOrUpdateQuestionnaire}<br>
     * Valid input: valid {@link QuestionnaireDTO} that triggers versioning information to be saved
     */
    @Test
    public void testSaveVersioningInformation() {
        // Arrange
        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Questionnaire existingQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        existingQuestionnaire.setVersion(1);

        Questionnaire copiedQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        copiedQuestionnaire.setVersion(2);

        Long validUserId = getPositiveId();

        when(questionnaireDao.getElementById(anyLong())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(anyString(), anyString(), anyLong(), anyLong(), anyBoolean())).thenReturn(copiedQuestionnaire);

        // Act
        questionnaireService.saveOrUpdateQuestionnaire(questionnaireDTO, EMPTY_LOGO, validUserId);

        // Capture the QuestionnaireVersion object passed to the merge method
        ArgumentCaptor<QuestionnaireVersion> captor = ArgumentCaptor.forClass(QuestionnaireVersion.class);
        verify(questionnaireVersionDao, times(1)).merge(captor.capture());

        // Assert
        QuestionnaireVersion savedVersion = captor.getValue();
        Assert.assertEquals("Previous questionnaire should match the existing questionnaire",
                existingQuestionnaire, savedVersion.getPreviousQuestionnaire());
        Assert.assertEquals("Current questionnaire should match the copied questionnaire",
                copiedQuestionnaire, savedVersion.getCurrentQuestionnaire());
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
        long validQuestionnaireId = getPositiveId();
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
        Questionnaire existingQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        Long userId = getPositiveId();
        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        MultipartFile logoFile = new MockMultipartFile(
                LOGO_FIELD_NAME,
                VALID_LOGO_FILENAME,
                VALID_LOGO_CONTENT_TYPE,
                VALID_LOGO_CONTENT
        );

        when(questionnaireDao.getElementById(any())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(anyString(), anyString(), anyLong(), anyLong(), anyBoolean()))
                .thenReturn(newQuestionnaire);

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(questionnaireDTO, logoFile, userId);

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
        Questionnaire existingQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        QuestionnaireDTO questionnaireDTO = QuestionnaireDTOTest.getNewValidQuestionnaireDTO();
        questionnaireDTO.setDeleteLogo(true);
        Long userId = getPositiveId();
        existingQuestionnaire.setLogo(VALID_LOGO_FILENAME);
        MultipartFile logoFile = new MockMultipartFile(LOGO_FIELD_NAME, EMPTY_BYTE_ARRAY);

        when(questionnaireDao.getElementById(anyLong())).thenReturn(existingQuestionnaire);
        when(questionnaireFactory.createQuestionnaire(anyString(), anyString(), anyLong(), anyLong(), anyBoolean()))
                .thenReturn(existingQuestionnaire);

        // Act
        Questionnaire result = questionnaireService.saveOrUpdateQuestionnaire(questionnaireDTO, logoFile, userId);

        // Assert
        Assert.assertNotNull("The updated questionnaire should not be null", result);
        Assert.assertNull("The logo should be null after deletion", result.getLogo());

        File deletedFile = new File(configurationDao.getImageUploadPath() + "/" + Constants.IMAGE_QUESTIONNAIRE + "/" + existingQuestionnaire.getId() + VALID_LOGO_FILENAME);
        Assert.assertFalse("The old logo file should be deleted", deletedFile.exists());
    }
}