package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.utils.Helper;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class SliderAnswerValidatorTest {

    private static final Random random = new Random();
    @Autowired
    SliderAnswerValidator sliderAnswerValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link SliderAnswerValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link SliderAnswer#class}<br> Invalid input: Other classes than {@link SliderAnswer#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for SliderAnswer.class failed. SliderAnswerValidator didn't support SliderAnswer.class except it was expected to do.",
            sliderAnswerValidator.supports(SliderAnswer.class));
        assertFalse(
            "Supports method for random class failed. SliderAnswerValidator supported that class except it wasn't expected to do.",
            sliderAnswerValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link SliderAnswerValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)} Valid input: Instance of {@link SliderAnswer} with
     * valid {@link Question} and {@link Questionnaire} instances and whose
     * {@link SliderAnswer#stepsize} is positiv and lower than the difference between
     * {@link SliderAnswer#maxValue} and {@link SliderAnswer#minValue} and whose differences is
     * divisble by stepsize.<br> Invalid input: Instance of {@link SliderAnswer} where
     * {@link SliderAnswer#stepsize} is not positive and the difference between the maxValue and
     * minValue is not divisible by stepsize.
     * <br>
     * <br>
     * Notice: Stepsize can't be tested with a value lower than zero and minValue can't be set to a
     * value that is greater than maxValue due to assert-statements at the setter methods. minValue
     * and maxValue can't set to null as well. This will cause a low line or branch coverage.
     */
    @Test
    public void testValidate() {
        String message, testErrorMessage;
        Double minValue, maxValue, stepSize, invalidStepSize;
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));

        //case 1: valid instance
        minValue = random.nextDouble();
        maxValue = minValue + random.nextDouble();
        BigDecimal unroundedDifferenceMaxMin = BigDecimal.valueOf(Math.abs(maxValue - minValue));
        // Round the difference to two decimal places
        BigDecimal differenceMaxMin = unroundedDifferenceMaxMin.setScale(2, RoundingMode.HALF_UP);
        BigDecimal zero = new BigDecimal(0);

        do {
            stepSize =
                differenceMaxMin.doubleValue() / ((Integer) (random.nextInt(11) + 1)).doubleValue();

            if (BigDecimal.valueOf(stepSize).compareTo(zero) == 0
                && differenceMaxMin.compareTo(zero) == 0) {
                stepSize = stepSize + 0.01;
                maxValue = maxValue + 0.01;
                unroundedDifferenceMaxMin = BigDecimal.valueOf(Math.abs(maxValue - minValue));
                differenceMaxMin = unroundedDifferenceMaxMin.setScale(2, RoundingMode.HALF_UP);
            }
        } while (differenceMaxMin.remainder(BigDecimal.valueOf(stepSize)).compareTo(zero) != 0);

        Map<String, String> questionText = new HashMap<>();
        questionText.put(Helper.getRandomLocale(), Helper.getRandomAlphabeticString(50) + 1);
        SliderAnswer sliderAnswer = new SliderAnswer(
            new Question(questionText, random.nextBoolean(), random.nextBoolean(),
                QuestionType.SLIDER, random.nextInt(50) + 1, new Questionnaire(), false),
            random.nextBoolean(), minValue, maxValue, stepSize, random.nextBoolean());
        sliderAnswerValidator.validate(sliderAnswer, result);
        assertFalse(
            "Validation of sliderAnswerDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //case 2: invalid stepsize > distanceMinMax
        invalidStepSize = stepSize + differenceMaxMin.doubleValue();
        sliderAnswer.setStepsize(invalidStepSize);
        sliderAnswerValidator.validate(sliderAnswer, result);
        assertTrue(
            "Validation of sliderAnswerDTO failed for invalid instance with stepsize greater than distance between min and max. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage(
            "sliderAnswer.validator.stepsizeBiggerThanDifferenceMaxMin", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of sliderAnswerDTO failed for invalid instance with stepsize greater than distance between min and max. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        sliderAnswer.setStepsize(stepSize);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //case 3: invalid stepsize does not divide distanceMinMax
        //build invalid min and max values
        sliderAnswer.setMinValue(minValue);
        sliderAnswer.setMaxValue(maxValue + random.nextDouble() + 0.02);
        invalidStepSize = sliderAnswer.getMaxValue() - sliderAnswer.getMinValue() - 0.015;
        sliderAnswer.setStepsize(invalidStepSize);
        sliderAnswerValidator.validate(sliderAnswer, result);
        assertTrue(
            "Validation of sliderAnswerDTO failed for invalid instance with stepsize not dividing distance between min and max. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage(
            "sliderAnswer.validator.differenceMaxMinNotDivisibleByStepsize", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of sliderAnswerDTO failed for invalid instance with stepsize not dividing distance between min and max. The returned error message didn't match the expected one.",
            message, testErrorMessage);

    }
}
