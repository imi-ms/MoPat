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
public class NumberInputAnswerDTOValidatorTest {

    private static final Random random = new Random();
    @Autowired
    NumberInputAnswerDTOValidator numberInputAnswerDTOValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link NumberInputAnswerDTOValidator#supports(java.lang.Class)} Valid input:
     * {@link AnswerDTO#class} Invalid input: Other class than {@link NumberInputAnswer}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for AnswerDTO.class failed. NumberInputAnswerDTOValidator didn't support AnswerDTO.class except it was expected to do.",
            numberInputAnswerDTOValidator.supports(AnswerDTO.class));
        assertFalse(
            "Supports method for random class failed. NumberInputAnswerDTOValidator supported that class except it wasn't expected to do.",
            numberInputAnswerDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link NumberInputAnswerDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}<br> Valid input: Instance of {@link AnswerDTO}
     * containing values for {@link NumberInputAnswer} object with <br> (i) MinValue < MaxValue,
     * <br>(ii) stepSize > 0, <br>(iii) stepSize < MaxValue - MinValue, <br>(iv) (MaxValue -
     * MinValue)%stepSize = 0.<br> and instance of instantiable subclass of
     * {@link BindingResult}.<br> Invalid input: All instances of {@link AnswerDTO} which conditions
     * for MinValue, MaxValue, stepSize that are named above doesn't fit.
     */
    @Test
    public void testValidate() {
        String message, testErrorMessage;
        Double minValue, maxValue, stepSize, invalidStepSize, distanceMinMax;
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        AnswerDTO answerDTO = new AnswerDTO();

        //case 1: valid instance
        minValue = random.nextDouble();
        maxValue = minValue + random.nextDouble();
        answerDTO.setMinValue(minValue);
        answerDTO.setMaxValue(maxValue);
        distanceMinMax = answerDTO.getMaxValue() - answerDTO.getMinValue();
        do {
            stepSize = distanceMinMax / ((Integer) (random.nextInt(11) + 1)).doubleValue();
        } while (BigDecimal.valueOf(distanceMinMax).remainder(BigDecimal.valueOf(stepSize))
            .compareTo(new BigDecimal(0)) != 0);
        answerDTO.setStepsize(stepSize.toString());
        numberInputAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of numberInputAnswerDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //case minValue null and maxValue null -> valid expected
        answerDTO.setMinValue(null);
        numberInputAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of numberInputAnswerDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        answerDTO.setMinValue(minValue);
        answerDTO.setMaxValue(null);
        numberInputAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of numberInputAnswerDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());
        answerDTO.setMaxValue(maxValue);
/*
        answerDTO.setStepsize(null);
        numberInputAnswerDTOValidator.validate(answerDTO, result);
        assertFalse("Validation of numberInputAnswerDTO failed for valid instance. The result has caught errors except it wasn't expected to do.", result.hasErrors());
        answerDTO.setStepsize(stepSize.toString());*/

        //case 2: invalid stepsize < 0
        invalidStepSize = -1d * stepSize;
        answerDTO.setStepsize(invalidStepSize.toString());
        numberInputAnswerDTOValidator.validate(answerDTO, result);
        assertTrue(
            "Validation of numberInputAnswerDTO failed for invalid instance with stepsize less than zero. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("numberInputAnswer.validator.stepsizeLowerEqualZero",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of numberInputAnswerDTO failed for invalid instance with stepsize less than zero. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //case 3: invalid stepsize > distanceMinMax
        invalidStepSize = stepSize + distanceMinMax;
        answerDTO.setStepsize(invalidStepSize.toString());
        numberInputAnswerDTOValidator.validate(answerDTO, result);
        assertTrue(
            "Validation of numberInputAnswerDTO failed for invalid instance with stepsize greater than distance between min and max. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage(
            "numberInputAnswer.validator.stepsizeBiggerThanDifferenceMaxMin", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of numberInputAnswerDTO failed for invalid instance with stepsize greater than distance between min and max. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        answerDTO.setStepsize(stepSize.toString());

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //case 4: invalid MinMax min > max
        answerDTO.setMinValue(maxValue);
        answerDTO.setMaxValue(minValue);
        numberInputAnswerDTOValidator.validate(answerDTO, result);
        assertTrue(
            "Validation of numberInputAnswerDTO failed for invalid instance with min greater than max. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("numberInputAnswer.validator.minBiggerThanMax",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of numberInputAnswerDTO failed for invalid instance with min greater than max. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        answerDTO.setStepsize(stepSize.toString());

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //case 5: invalid stepsize does not divide distanceMinMax
        answerDTO.setMinValue(minValue);
        answerDTO.setMaxValue(maxValue + random.nextDouble());
        numberInputAnswerDTOValidator.validate(answerDTO, result);
        assertTrue(
            "Validation of numberInputAnswerDTO failed for invalid instance with stepsize not dividing distance between min and max. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage(
            "numberInputAnswer.validator.differenceMaxMinNotDivisibleByStepsize", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of numberInputAnswerDTO failed for invalid instance with stepsize not dividing distance between min and max. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        answerDTO.setStepsize(stepSize.toString());
    }
}
