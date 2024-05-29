package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.utils.Helper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
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
public class SliderAnswerDTOValidatorTest {

    public static Random random = new Random();
    @Autowired
    SliderAnswerDTOValidator sliderAnswerDTOValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link SliderAnswerDTOValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link AnswerDTO#class} <br> Invalid input: Other classes than {@link AnswerDTO#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for AnswerDTO.class failed. SliderAnswerDTOValidator didn't support AnswerDTO.class except it was expected to do.",
            sliderAnswerDTOValidator.supports(AnswerDTO.class));
        assertFalse(
            "Supports method for random class failed. SliderAnswerDTOValidator supported that class except it wasn't expected to do.",
            sliderAnswerDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link SliderAnswerDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}<br> Valid input: {@link AnswerDTO} that contains
     * {@link AnswerDTO#maxValue} greater than {@link AnswerDTO#minValue} and both values greater or
     * equal to zero. Further it's valid if {@link AnswerDTO#stepsize} is positive and lower than
     * the difference between maxValue and minValue and if this difference is divisible by the
     * stepsize.<br> Invalid input: {@link AnswerDTO} where {@link AnswerDTO#maxValue} is lower than
     * {@link AnswerDTO#minValue} or both values are lower than zero or they are
     * <code>null</code>.<br> Further it's invalid if {@link AnswerDTO#stepsize} isn't positive or
     * greater than the difference between maxValue and minValue or this difference isn't divisble
     * by the stepsize.
     */
    @Test
    public void testValidate() {
        String message, testErrorMessage;
        Double minValue, maxValue, stepSize, invalidStepSize;
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        AnswerDTO answerDTO = new AnswerDTO();

        //case 1: valid instance
        minValue = random.nextDouble();
        maxValue = minValue + random.nextDouble();
        answerDTO.setMinValue(minValue);
        answerDTO.setMaxValue(maxValue);
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
                answerDTO.setMaxValue(maxValue);
            }
        } while (differenceMaxMin.remainder(BigDecimal.valueOf(stepSize)).compareTo(zero) != 0);
        answerDTO.setStepsize(stepSize.toString());
        sliderAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of sliderAnswerDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //case 2: invalid stepsize < 0
        invalidStepSize = -1d * stepSize;
        answerDTO.setStepsize(invalidStepSize.toString());
        sliderAnswerDTOValidator.validate(answerDTO, result);
        assertTrue(
            "Validation of sliderAnswerDTO failed for invalid instance with stepsize less than zero. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("sliderAnswer.validator.stepsizeWrongPattern",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of sliderAnswerDTO failed for invalid instance with stepsize not matching pattern. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        message = messageSource.getMessage("sliderAnswer.validator.stepsizeLowerEqualZero",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(1).getDefaultMessage();
        assertEquals(
            "Validation of sliderAnswerDTO failed for invalid instance with stepsize less than zero. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //case 3: invalid stepsize > distanceMinMax
        invalidStepSize = stepSize + differenceMaxMin.doubleValue();
        answerDTO.setStepsize(invalidStepSize.toString());
        sliderAnswerDTOValidator.validate(answerDTO, result);
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
        answerDTO.setStepsize(stepSize.toString());

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //case 4: invalid MinMax min > max
        answerDTO.setMinValue(maxValue);
        answerDTO.setMaxValue(minValue);
        sliderAnswerDTOValidator.validate(answerDTO, result);
        assertTrue(
            "Validation of sliderAnswerDTO failed for invalid instance with min greater than max. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("sliderAnswer.validator.minBiggerThanMax",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of sliderAnswerDTO failed for invalid instance with min greater than max. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //case 5: invalid stepsize does not divide distanceMinMax
        //build invalid min and max values
        answerDTO.setMinValue(minValue);
        answerDTO.setMaxValue(maxValue + random.nextDouble() + 0.02);
        invalidStepSize = answerDTO.getMaxValue() - answerDTO.getMinValue() - 0.015;
        answerDTO.setStepsize(invalidStepSize.toString());
        sliderAnswerDTOValidator.validate(answerDTO, result);
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

        answerDTO.setStepsize(stepSize.toString());
        //case 6: minvalue and maxValue null
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        answerDTO.setMinValue(null);
        answerDTO.setMaxValue(maxValue);
        sliderAnswerDTOValidator.validate(answerDTO, result);
        assertTrue(
            "Validation of sliderAnswerDTO failed for invalid instance with minValue null. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("sliderAnswer.validator.minValueNotNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of sliderAnswerDTO failed for invalid instance with minValue null. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        answerDTO.setMinValue(minValue);
        answerDTO.setMaxValue(null);
        sliderAnswerDTOValidator.validate(answerDTO, result);
        assertTrue(
            "Validation of sliderAnswerDTO failed for invalid instance with minValue null. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("sliderAnswer.validator.maxValueNotNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of sliderAnswerDTO failed for invalid instance with minValue null. The returned error message didn't match the expected one.",
            message, testErrorMessage);
    }
}
