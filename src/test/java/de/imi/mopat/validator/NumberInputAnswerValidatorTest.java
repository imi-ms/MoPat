package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.NumberInputAnswerTest;
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
public class NumberInputAnswerValidatorTest {

    public static Random random = new Random();
    @Autowired
    NumberInputAnswerValidator numberInputAnswerValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link NumberInputAnswer#supports(java.lang.Class)}<br> Valid input:
     * {@link NumberInputAnswer#class}<br> Invalid input: Other classes than
     * {@link NumberInputAnswer}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for NumberInputAnswer.class failed. NumberInputAnswerValidator didn't support NumberInputAnswer.class except it was expected to do.",
            numberInputAnswerValidator.supports(NumberInputAnswer.class));
        assertFalse(
            "Supports method for random class failed. NumberInputAnswerValidator supported that class except it wasn't expected to do.",
            numberInputAnswerValidator.supports(Random.class));
    }


    /**
     * Test of
     * {@link NumberInputAnswerValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}. <br> Valid input: Instance of {@link AnswerDTO}
     * containing values for {@link NumberInputAnswer} object with <br> (i)  MaxValue>MinValue,
     * <br>(ii) stepSize > 0, <br>(iii)MaxValue - MinValue > stepSize, <br>(iv) (MaxValue -
     * MinValue)%stepSize = 0.<br> and instance of instantiable subclass of
     * {@link BindingResult}.<br> Invalid input: All instances of {@link AnswerDTO} which conditions
     * for MinValue, MaxValue, stepSize that are named above doesn't fit.
     * <br><br>
     * Notice: minValue, maxValue and stepSize can't be set to invalid values or <code>null</code>
     * due to assert statements.
     */
    @Test
    public void testValidate() {
        String message, testErrorMessage;
        NumberInputAnswer numberInputAnswer = NumberInputAnswerTest.getNewValidNumberInputAnswer();
        Double minValue, maxValue, stepSize, invalidStepSize, distanceMinMax;
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));

        //Validate all necessary test cases. Don't include the cases where min and max don't respect the conditions of the appropriate setter methods.
        //case 1: test valid instance
        numberInputAnswer.setMaxValue(null);
        numberInputAnswer.setMinValue(null);
        minValue = random.nextDouble();
        maxValue = minValue + random.nextDouble();
        numberInputAnswer.setMaxValue(maxValue);
        numberInputAnswer.setMinValue(minValue);
        distanceMinMax = numberInputAnswer.getMaxValue() - numberInputAnswer.getMinValue();
        do {
            stepSize = distanceMinMax / ((Integer) (random.nextInt(11) + 1)).doubleValue();
        } while (BigDecimal.valueOf(distanceMinMax).remainder(BigDecimal.valueOf(stepSize))
            .compareTo(new BigDecimal(0)) != 0);
        numberInputAnswer.setStepsize(stepSize);

        numberInputAnswerValidator.validate(numberInputAnswer, result);
        assertFalse(
            "Validation of numberInputAnswer failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        //case 2: test invalid instance with > distanceMinMax
        invalidStepSize = stepSize + distanceMinMax;
        numberInputAnswer.setStepsize(invalidStepSize);
        numberInputAnswerValidator.validate(numberInputAnswer, result);
        assertTrue(
            "Validation of numberInputAnswer failed for invalid instance with stepsize greater than distance between min and max. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage(
            "numberInputAnswer.validator.stepsizeBiggerThanDifferenceMaxMin", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of numberInputAnswer failed for invalid instance with stepsize greater than distance between min and max. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        numberInputAnswer.setStepsize(stepSize);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //case 3: test invalid instance with stepsize doesn't divide distanceMinMax
        numberInputAnswer.setMinValue(minValue);
        numberInputAnswer.setMaxValue(maxValue + random.nextDouble());
        numberInputAnswerValidator.validate(numberInputAnswer, result);
        assertTrue(
            "Validation of numberInputAnswer failed for invalid instance with stepsize not dividing distance between min and max. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage(
            "numberInputAnswer.validator.differenceMaxMinNotDivisibleByStepsize", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of numberInputAnswer failed for invalid instance with stepsize not dividing distance between min and max. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        numberInputAnswer.setStepsize(stepSize);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        numberInputAnswer.setMinMax(null, maxValue);
        numberInputAnswerValidator.validate(numberInputAnswer, result);
        assertFalse(
            "Validation of numberInputAnswer failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        numberInputAnswer.setMinMax(minValue, null);
        numberInputAnswerValidator.validate(numberInputAnswer, result);
        assertFalse(
            "Validation of numberInputAnswer failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        numberInputAnswer.setMinMax(minValue, maxValue);
        numberInputAnswer.setStepsize(null);
        numberInputAnswerValidator.validate(numberInputAnswer, result);
        assertFalse(
            "Validation of numberInputAnswer failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());
    }
}
