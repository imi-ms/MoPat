package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.DateAnswerTest;
import de.imi.mopat.model.FreetextAnswer;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SelectAnswerTest;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.utils.Helper;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
public class QuestionValidatorTest {

    private static final Random random = new Random();
    @Autowired
    MessageSource messageSource;
    @Autowired
    private QuestionValidator questionValidator;

    /**
     * Test of {@link QuestionValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link Question#class}<br> Invalid input: Other class than {@link Question#class}<br>
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for Question.class failed. QuestionValidator didn't support Question.class except it was expected to do.",
            questionValidator.supports(Question.class));
        assertFalse(
            "Supports method for random class failed. QuestionValidator supported that class except it wasn't expected to do.",
            questionValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link QuestionValidator#validate(java.lang.Object, org.springframework.validation.Errors)}.
     * <br> Valid input: Instance of {@link Question} and {@link Errors} <br> Validation of
     * instance
     * of {@link Question} depends to its {@link QuestionType}. If no conditions for invalid
     * instances (watch below) is fitting for the object, it's valid.<br>
     * <p>
     * Invalid input: Instance of {@link Question} with {@link QuestionType}
     * <code>MULTIPLE_CHOICE</code> or <code>DROP_DOWN</code> is invalid if one of the following
     * conditions fit for minNumberAnswers and maxNumberAnswers.<br> minNumberAnswers and/or
     * maxNumberAnswers is <code>null</code>.<br> minNumberAnswers greater than
     * maxNumberAnswers.<br> minNumberAnswers and/or maxNumberAnswers is greater than the count of
     * answers.<br> Instance of {@link QuestionDTO} of any {@link QuestionType} is invalid if one of
     * the {@link QuestionDTO#getLocalizedQuestionText()} is <code>null</code>.
     * <p>
     * Notice: There's no possible way to set the answer's (SliderAnswer, SliderAnswerFreetext and
     * NumberInput) Minimum and Maximum to invalid values, so it's impossible to test the validation
     * for these cases.
     */
    @Test
    public void testValidate() {
        Question question = QuestionTest.getNewValidQuestion();
        Answer answer;
        Double min, max, stepsize, distanceMinMax;
        Integer size = random.nextInt(13) + 1;

        min = ((Integer) random.nextInt(13)).doubleValue();
        max = min + random.nextInt(12) + 1;
        distanceMinMax = Math.abs(max - min);

        do {
            stepsize = Math.floor(distanceMinMax / (random.nextInt(13) + 1) + 1);
        } while (BigDecimal.valueOf(distanceMinMax).remainder(BigDecimal.valueOf(stepsize))
            .compareTo(new BigDecimal(0)) != 0);

        Map<String, String> localizedLabel = new HashMap<>();
        for (int i = 0; i < random.nextInt(37) + 1; i++) {
            localizedLabel.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(random.nextInt(23) + 1));
        }

        //case 1: valid instance for all types of question
        //1 MultipleChoice
        question.setQuestionType(QuestionType.MULTIPLE_CHOICE);

        for (int i = 0; i < size + 1; i++) {
            answer = new SelectAnswer(question, random.nextBoolean(), localizedLabel,
                random.nextBoolean());
            question.addAnswer(answer);
        }
        question.setMinNumberAnswers(random.nextInt(size));
        question.setMaxNumberAnswers(
            question.getMinNumberAnswers() + random.nextInt(size - question.getMinNumberAnswers()));

        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13) + 1));
        questionValidator.validate(question, result);
        assertFalse(
            "Validation of question failed for valid instance of type multiple choice. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //2 DropDown
        question.setQuestionType(QuestionType.DROP_DOWN);
        questionValidator.validate(question, result);
        assertFalse(
            "Validation of question failed for valid instance of type drop down. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //3 Slider
        question.removeAllAnswers();
        question.setQuestionType(QuestionType.SLIDER);
        answer = new SliderAnswer(question, random.nextBoolean(), min, max, stepsize,
            random.nextBoolean());
        question.addAnswer(answer);
        questionValidator.validate(question, result);
        assertFalse(
            "Validation of question failed for valid instance of type slider. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //4 NumberCheckbox
        question.setQuestionType(QuestionType.NUMBER_CHECKBOX);
        questionValidator.validate(question, result);
        assertFalse(
            "Validation of question failed for valid instance of type number checkbox. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //5 NumberCheckboxText
        question.removeAllAnswers();
        question.setQuestionType(QuestionType.NUMBER_CHECKBOX_TEXT);
        answer = new SliderFreetextAnswer(question, random.nextBoolean(), min, max, stepsize,
            localizedLabel, random.nextBoolean());
        question.addAnswer(answer);
        questionValidator.validate(question, result);
        assertFalse(
            "Validation of question failed for valid instance of type number checkbox text. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //6 NumberInput
        question.removeAllAnswers();
        question.setQuestionType(QuestionType.NUMBER_INPUT);
        answer = new NumberInputAnswer(question, random.nextBoolean(), min, max, stepsize);
        question.addAnswer(answer);
        questionValidator.validate(question, result);
        assertFalse(
            "Validation of question failed for valid instance of type number input. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //7 Freetext
        question.removeAllAnswers();
        question.setQuestionType(QuestionType.FREE_TEXT);
        answer = new FreetextAnswer(question, random.nextBoolean());
        question.addAnswer(answer);
        questionValidator.validate(question, result);
        assertFalse(
            "Validation of question failed for valid instance of type freetext. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //8 Date
        question.removeAllAnswers();
        question.setQuestionType(QuestionType.DATE);
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + Math.abs(random.nextLong()));
        answer = new DateAnswer(question, random.nextBoolean(), startDate, endDate);
        question.addAnswer(answer);
        questionValidator.validate(question, result);
        assertFalse(
            "Validation of question failed for valid instance of type date. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //9 Infotext
        question.removeAllAnswers();
        question.setQuestionType(QuestionType.INFO_TEXT);
        questionValidator.validate(question, result);
        assertFalse(
            "Validation of question failed for valid instance of type info text. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //case 2: invalid question text
        question.removeAllAnswers();
        question.setQuestionType(QuestionType.INFO_TEXT);
        question.getLocalizedQuestionText()
            .put(Helper.getRandomAlphabeticString(random.nextInt(13) + 1), null);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13) + 1));
        questionValidator.validate(question, result);
        assertTrue(
            "Validation of question failed for invalid instance with info text is null. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        String message, testErrorMessage;
        message = messageSource.getMessage("question.error.infoTextIsNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionDTO failed for invalid instance with info text is null. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        do {
            question.setQuestionType(Helper.getRandomEnum(QuestionType.class));
        } while (question.getQuestionType() != QuestionType.DROP_DOWN
            && question.getQuestionType() != QuestionType.MULTIPLE_CHOICE);

        for (int i = 0; i < size; i++) {
            question.addAnswer(SelectAnswerTest.getNewValidSelectAnswer());
        }

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13) + 1));
        questionValidator.validate(question, result);
        assertTrue(
            "Validation of question failed for invalid instance with question text is null. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.questionTextIsNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of question failed for invalid instance with question text is null. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        question.setLocalizedQuestionText(localizedLabel);

        //case 3: minNumberAnswers and maxNumberAnswers are set to null
        //first case minNumberAnswers null and maxNumberAnswers has got a value
        question.setMinNumberAnswers(null);
        question.setMaxNumberAnswers(random.nextInt(size) + 1);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13) + 1));
        questionValidator.validate(question, result);
        assertTrue(
            "Validation of question failed for invalid instance with minNumberAnswers null. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.minNumberAnswersMissing", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of question failed for invalid instance with minNumberAnswers null. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //second case maxNumberAnswers null and minNumberAnswers has got a value
        question.setMinNumberAnswers(random.nextInt(question.getMaxNumberAnswers()));
        question.setMaxNumberAnswers(null);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13) + 1));
        questionValidator.validate(question, result);
        assertTrue(
            "Validation of question failed for invalid instance with maxNumberAnswers missing. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.maxNumberAnswersMissing", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of question failed for invalid instance with maxNumberAnswers missing. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //case 4: minNumberAnswers or maxNumberAnswers greater than the amount of answers, need to set minNumberAnswers or maxNumberAnwers to null (it's already set to null above)
        //first case minNumberAnswers > answers.size
        question.setMinNumberAnswers(question.getAnswers().size() + 1);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13) + 1));
        questionValidator.validate(question, result);
        assertTrue(
            "Validation of question failed for invalid instance with minNumberAnswers greater than the amount of answers. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.minNumberBiggerThanAmountOfAnswers",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(1)
            .getDefaultMessage();    //MaxNumberAnswers are missing so there's another error message before the tested one
        assertEquals(
            "Validation of question failed for invalid instance with minNumberAnswers greater than the amount of answers. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //second case maxNumberAnswers > answer.size
        question.setMinNumberAnswers(null);
        question.setMaxNumberAnswers(question.getAnswers().size() + 1);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13) + 1));
        questionValidator.validate(question, result);
        assertTrue(
            "Validation of question failed for invalid instance with maxNumberAnswers greater than the amount of answers. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.maxNumberBiggerThanAmountOfAnswers",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(1)
            .getDefaultMessage();    //MinNumberAnswers are missing so there's another error message before the tested one
        assertEquals(
            "Validation of question failed for invalid instance with maxNumberAnswers greater than the amount of answers. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        question.removeAllAnswers();
        //case 5: dateAnswer's endDate is before startDate
        question.setQuestionType(QuestionType.DATE);
        DateAnswer dateAnswer = DateAnswerTest.getNewValidDateAnswer();

        do {
            dateAnswer.setEndDate(new Date(Math.abs(random.nextLong())));
        } while (dateAnswer.getEndDate().getTime() > dateAnswer.getStartDate().getTime());

        question.addAnswer(dateAnswer);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13) + 1));
        questionValidator.validate(question, result);
        assertTrue(
            "Validation of question failed for invalid instance with endDate before startDate. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("dateAnswer.validator.endEarlierThanStart",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0)
            .getDefaultMessage();    //MinNumberAnswers are missing so there's another error message before the tested one
        assertEquals(
            "Validation of question failed for invalid instance with endDate before startDate. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13) + 1));
        dateAnswer = new DateAnswer(question, true, new Date(), null);
        question.removeAllAnswers();
        question.addAnswer(dateAnswer);
        questionValidator.validate(question, result);
        assertFalse(
            "Validation of question failed for invalid instance with endDate before startDate. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        dateAnswer.setStartDate(null);
        dateAnswer.setEndDate(new Date());
        question.removeAllAnswers();
        question.addAnswer(dateAnswer);
        questionValidator.validate(question, result);
        assertFalse(
            "Validation of question failed for invalid instance with endDate before startDate. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        localizedLabel.clear();
        localizedLabel.put(Helper.getRandomLocale(), "");
        question.setLocalizedQuestionText(localizedLabel);
        questionValidator.validate(question, result);
        assertTrue(
            "Validation of question failed for invalid instance with question text empty. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("question.error.questionTextIsNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0)
            .getDefaultMessage();    //MinNumberAnswers are missing so there's another error message before the tested one
        assertEquals(
            "Validation of question failed for invalid instance with question text empty. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13) + 1));
        localizedLabel.clear();
        localizedLabel.put(Helper.getRandomLocale(), null);
        question.setLocalizedQuestionText(localizedLabel);
        questionValidator.validate(question, result);
        assertTrue(
            "Validation of question failed for invalid instance with question text empty. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        testErrorMessage = result.getAllErrors().get(0)
            .getDefaultMessage();    //MinNumberAnswers are missing so there's another error message before the tested one
        assertEquals(
            "Validation of question failed for invalid instance with question text empty. The returned error message didn't match the expected one.",
            message, testErrorMessage);
    }
}
