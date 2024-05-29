package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.model.enumeration.CodedValueType;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.utils.Helper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class QuestionDTOValidatorTest {

    public static Random random = new Random();
    @Autowired
    QuestionDTOValidator questionDTOValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link QuestionDTOValidator#supports(java.lang.Class)}.<br> Valid input:
     * {@link Question#class}<br> Invalid input: Other class than {@link Question#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for QuestionDTO.class failed. QuestionDTOValidator didn't support Question.class except it was expected to do.",
            questionDTOValidator.supports(QuestionDTO.class));
        assertFalse(
            "Supports method for random class failed. QuestionDTOValidator supported that class except it wasn't expected to do.",
            questionDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link QuestionDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}. <br> Valid input: Instance of {@link QuestionDTO}
     * and instance of {@link Errors}.<br> Invalid input: Instance of {@link QuestionDTO} with
     * {@link QuestionType} <code>MULTIPLE_CHOICE</code> is invalid if one of the following
     * conditions fit for minNumberAnswers and maxNumberAnswers.<br> minNumberAnswers and/or
     * maxNumberAnswers is <code>null</code>.<br> minNumberAnswers greater than
     * maxNumberAnswers.<br> minNumberAnswers and/or maxNumberAnswers is greater than the count of
     * answers.<br> Instance of {@link QuestionDTO} of any {@link QuestionType} is invalid if one of
     * the {@link QuestionDTO#getLocalizedQuestionText()} is <code>null</code>.
     * <br>
     * Notice: It's not necessary to test the different validate methods for different answer types
     * because they're tested in their own testclasses.
     */
    @Test
    public void testValidate() {
        QuestionDTO questionDTO = new QuestionDTO();
        Integer size = random.nextInt(7) + 1;

        SortedMap<Long, AnswerDTO> answers = new TreeMap<>();
        SortedMap<String, String> localizedLabel = new TreeMap<>();
        localizedLabel.put(Helper.getRandomLocale(),
            Helper.getRandomAlphabeticString(random.nextInt(13) + 1));
        for (int i = 0; i < size; i++) {
            AnswerDTO answerDTO = new AnswerDTO();
            answerDTO.setId(Math.abs(random.nextLong()));
            answerDTO.setLocalizedLabel(localizedLabel);
            answerDTO.setLocalizedFreetextLabel(localizedLabel);
            answerDTO.setLocalizedMaximumText(localizedLabel);
            answerDTO.setLocalizedMinimumText(localizedLabel);
            answerDTO.setCodedValue(String.valueOf(random.nextInt()));
            answers.put(answerDTO.getId(), answerDTO);
        }
        questionDTO.setAnswers(answers);

        //case 1: valid instance of question type multiple choice and drop down (those two types are validated the same way)
        questionDTO.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        questionDTO.setMaxNumberAnswers(size);
        questionDTO.setMinNumberAnswers(random.nextInt(size));
        questionDTO.setCodedValueType(CodedValueType.STRING);
        questionDTO.setIsEnabled(random.nextBoolean());
        questionDTO.setIsRequired(random.nextBoolean());
        SortedMap<String, String> localizedQuestionText = new TreeMap<>();
        localizedQuestionText.put(Helper.getRandomLocale(),
            Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        questionDTO.setLocalizedQuestionText(localizedQuestionText);

        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        questionDTOValidator.validate(questionDTO, result);
        assertFalse(
            "Validation of questionDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());
        questionDTO.setQuestionType(QuestionType.DROP_DOWN);
        assertFalse(
            "Validation of questionDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //case 2: invalid instance with invalid minNumberAnswers and maxNumberAnswers
        questionDTO.setMinNumberAnswers(null);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        questionDTOValidator.validate(questionDTO, result);
        assertTrue(
            "Validation of questionDTO failed for invalid instance with minNumberAnswer null. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        String message, testErrorMessage;
        message = messageSource.getMessage("question.error.minNumberAnswersMissing", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionDTO failed for invalid instance with minNumberAnswer null. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //case 3: invalid instance with empty question text differentiate beetwen info text and other question types
        questionDTO.setMinNumberAnswers(random.nextInt(size));
        questionDTO.setMaxNumberAnswers(null);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        questionDTOValidator.validate(questionDTO, result);
        assertTrue(
            "Validation of questionDTO failed for invalid instance with maxNumberAnswer null. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.maxNumberAnswersMissing", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionDTO failed for invalid instance with maxNumberAnswer null. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        questionDTO.setMinNumberAnswers(size);
        questionDTO.setMaxNumberAnswers(random.nextInt(size));
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        questionDTOValidator.validate(questionDTO, result);
        assertTrue(
            "Validation of questionDTO failed for invalid instance with minNumberAnswer greater than maxNumberAnswer. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.minNumberBiggerThanMaxNumber",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionDTO failed for invalid instance with minNumberAnswer greater than maxNumberAnswer. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        questionDTO.setMinNumberAnswers(size + 1);
        questionDTO.setMaxNumberAnswers(size + 2);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        questionDTOValidator.validate(questionDTO, result);
        assertTrue(
            "Validation of questionDTO failed for invalid instance with minNumberAnswer greater than answer count. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.minNumberBiggerThanAmountOfAnswers",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionDTO failed for invalid instance with minNumberAnswer greater than answer count. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        questionDTO.setMinNumberAnswers(random.nextInt(size));
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        questionDTOValidator.validate(questionDTO, result);
        assertTrue(
            "Validation of questionDTO failed for invalid instance with maxNumberAnswer greater than answer count. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.maxNumberBiggerThanAmountOfAnswers",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionDTO failed for invalid instance with maxNumberAnswer greater than answer count. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        localizedQuestionText = new TreeMap<>();
        localizedQuestionText.put(Helper.getRandomLocale(), "");
        questionDTO.setLocalizedQuestionText(localizedQuestionText);
        questionDTO.setMinNumberAnswers(random.nextInt(size));
        questionDTO.setMaxNumberAnswers(size);
        questionDTO.setQuestionType(QuestionType.INFO_TEXT);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        questionDTOValidator.validate(questionDTO, result);
        assertTrue(
            "Validation of questionDTO failed for invalid instance with invalid info text. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.infoTextIsNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionDTO failed for invalid instance with invalid info text. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        questionDTO.setQuestionType(QuestionType.DROP_DOWN);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        questionDTOValidator.validate(questionDTO, result);
        assertTrue(
            "Validation of questionDTO failed for invalid instance with invalid question text. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.questionTextIsNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionDTO failed for invalid instance with invalid question text. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        //Other question types haven't to be tested because they got their own validators that are tested.

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        questionDTO.setQuestionType(QuestionType.DROP_DOWN);
        localizedQuestionText = new TreeMap<>();
        localizedQuestionText.put(Helper.getRandomLocale(), null);
        questionDTO.setLocalizedQuestionText(localizedQuestionText);
        questionDTOValidator.validate(questionDTO, result);
        assertTrue(
            "Validation of questionDTO failed for invalid instance with invalid question text. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.questionTextIsNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionDTO failed for invalid instance with invalid question text. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        SortedMap<String, String> labels = new TreeMap<>();
        labels.put(Helper.getRandomLocale(),
            Helper.getRandomAlphabeticString(random.nextInt(55) + 3));
        questionDTO = new QuestionDTO();
        questionDTO.setIsEnabled(true);
        questionDTO.setIsRequired(true);
        questionDTO.setMaxNumberAnswers(1);
        questionDTO.setMinNumberAnswers(0);
        questionDTO.setPosition(1);
        questionDTO.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        questionDTO.setCodedValueType(CodedValueType.STRING);
        questionDTO.setLocalizedQuestionText(labels);
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setIsEnabled(true);
        answerDTO.setMinValue(0.0D);
        answerDTO.setMaxValue(5.0D);
        answerDTO.setStepsize("1,0");
        answerDTO.setCodedValue("abc");
        answerDTO.setLocalizedFreetextLabel(localizedLabel);
        answerDTO.setLocalizedLabel(localizedLabel);
        answerDTO.setStartDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        answerDTO.setEndDate(
            new SimpleDateFormat("yyyy-MM-dd").format(new Date(2 * System.currentTimeMillis())));
        SortedMap<Long, AnswerDTO> answerDTOs = new TreeMap<>();
        answerDTOs.put(0L, answerDTO);
        answerDTO = new AnswerDTO();
        answerDTO.setId(-1 * Math.abs(random.nextLong()));
        answerDTO.setIsEnabled(true);
        answerDTO.setMinValue(0.0D);
        answerDTO.setMaxValue(5.0D);
        answerDTO.setStepsize("1,0");
        answerDTO.setCodedValue("def");
        answerDTO.setLocalizedFreetextLabel(localizedLabel);
        answerDTO.setLocalizedLabel(localizedLabel);
        answerDTO.setStartDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        answerDTO.setEndDate(
            new SimpleDateFormat("yyyy-MM-dd").format(new Date(2 * System.currentTimeMillis())));
        answerDTOs.put(answerDTO.getId(), answerDTO);
        questionDTO.setAnswers(answerDTOs);
        questionDTOValidator.validate(questionDTO, result);
        assertFalse(
            "Validation of questionDTO failed for invalid instance with invalid question text. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        questionDTO.setQuestionType(QuestionType.SLIDER);
        questionDTOValidator.validate(questionDTO, result);
        assertFalse(
            "Validation of questionDTO failed for invalid instance with invalid question text. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        questionDTO.setQuestionType(QuestionType.DATE);
        questionDTOValidator.validate(questionDTO, result);
        assertFalse(
            "Validation of questionDTO failed for invalid instance with invalid question text. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        questionDTO.setQuestionType(QuestionType.NUMBER_INPUT);
        questionDTOValidator.validate(questionDTO, result);
        assertFalse(
            "Validation of questionDTO failed for invalid instance with invalid question text. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());
    }
}
