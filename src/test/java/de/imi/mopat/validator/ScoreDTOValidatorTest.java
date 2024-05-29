package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.dto.ExpressionDTO;
import de.imi.mopat.model.dto.ScoreDTO;
import de.imi.mopat.utils.Helper;
import java.util.HashMap;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.annotation.Repeat;
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
public class ScoreDTOValidatorTest {

    private static final Random random = new Random();
    @Autowired
    ScoreDTOValidator scoreDTOValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link ScoreDTOValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link ScoreDTO#class}<br> Invalid input: Other class than {@link ScoreDTO#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for ScoreDTO.class failed. ScoreDTOValidator didn't support ScoreDTO.class except it was expected to do.",
            scoreDTOValidator.supports(ScoreDTO.class));
        assertFalse(
            "Supports method for random class failed. ScoreDTOValidator supported that class except it wasn't expected to do.",
            scoreDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link ScoreDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}<br> Valid input: Instance of {@link ScoreDTO} which
     * contains a name without comma and a valid {@link ExpressionDTO} object and instance of
     * {@link Errors}.<br> Invalid input: Instance of {@link ScoreDTO} which contains an empty
     * String or <code>null</code> as name or a name with any number of commata. Further instance is
     * invalid if the adherent {@link ExpressionDTO} object is invalid.<br>
     */
    @Test
    @Repeat(1000)
    public void testValidate() {
        String message, testErrorMessage;
        ExpressionDTO firstExpression, secondExpression;
        ScoreDTO scoreDTO = new ScoreDTO();
        ExpressionDTO expressionDTO = new ExpressionDTO();
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));

        //Get the id of a random operator which is unequal to == or != for later use
        Long randomNumericOperatorId = 0L;
        switch (random.nextInt(4)) {
            case 0:
                randomNumericOperatorId = 1L;
                break;
            case 1:
                randomNumericOperatorId = 2L;
                break;
            case 2:
                randomNumericOperatorId = 3L;
                break;
            case 3:
                randomNumericOperatorId = 4L;
                break;
        }

        Long randomBooleanOperatorId = 0L;
        switch (random.nextInt(4)) {
            case 0:
                randomBooleanOperatorId = 8L;
                break;
            case 1:
                randomBooleanOperatorId = 9L;
                break;
            case 2:
                randomBooleanOperatorId = 10L;
                break;
            case 3:
                randomBooleanOperatorId = 11L;
                break;
            case 4:
                randomBooleanOperatorId = 12L;
                break;
            case 5:
                randomBooleanOperatorId = 13L;
                break;
        }

        //validate valid instance
        scoreDTO.setName(Helper.getRandomAlphabeticString(random.nextInt(23) + 1));

        //Valid instance with value operator
        expressionDTO.setOperatorId(6L);
        expressionDTO.setValue(Helper.getRandomAlphabeticString(random.nextInt(23) + 1));
        scoreDTO.setExpression(expressionDTO);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        //validate invalid isntance with name null
        scoreDTO.setName(null);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with name set to null. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.name.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with name set to null. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //validate invalid isntance with empty name
        scoreDTO.setName("");
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with name set to null. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.name.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with name set to null. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //validate invalid isntance with name containing comma
        scoreDTO.setName(Helper.getRandomString(random.nextInt(23) + 1).concat(","));
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with name set to null. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.name.noComma", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with name set to null. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //validate invalid instance with null as expression
        scoreDTO.setName(Helper.getRandomAlphabeticString(random.nextInt(23) + 1));
        scoreDTO.setExpression(null);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with name set to null. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expression.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with name set to null. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //Invalid expression with value operator (no value)
        expressionDTO.setValue(null);
        expressionDTO.setOperatorId(6L);
        scoreDTO.setExpression(expressionDTO);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with invalid value operator. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.value.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with invalid value operator. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //Valid expression with valueOf operator
        expressionDTO.setOperatorId(5L);
        expressionDTO.setQuestionId(Math.abs(random.nextLong()));
        scoreDTO.setExpression(expressionDTO);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with valueOf operator. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        //Invalid expression with valueOf operator (no questionId)
        expressionDTO.setQuestionId(null);
        scoreDTO.setExpression(expressionDTO);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with invalid valueOf operator. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.questionId.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with invalid valueOf operator. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //Invalid expression with operatorId null
        expressionDTO.setOperatorId(null);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with invalid operator. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.operator.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with invalid operator. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //Valid expression with random numericBinaryOperator
        expressionDTO.setOperatorId(randomNumericOperatorId);

        firstExpression = new ExpressionDTO();
        firstExpression.setOperatorId(6L);
        firstExpression.setValue(Helper.getRandomAlphabeticString(random.nextInt(11) + 1));
        secondExpression = new ExpressionDTO();
        secondExpression.setOperatorId(5L);
        secondExpression.setQuestionId(Math.abs(random.nextLong()));
        expressionDTO.addExpressions(firstExpression);
        expressionDTO.addExpressions(secondExpression);
        scoreDTO.setExpression(expressionDTO);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with valid binaryOperator. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        //Invalid expression with random binaryOperator with at least one of the two child operators is no numeric operator
        firstExpression.setOperatorId(12L);
        expressionDTO.getExpressions().clear();
        expressionDTO.addExpressions(firstExpression);
        expressionDTO.addExpressions(secondExpression);
        scoreDTO.setExpression(expressionDTO);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with invalid operator type. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.numeric", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with invalid operator type. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        firstExpression.setOperatorId(6L);
        secondExpression.setOperatorId(13L);
        expressionDTO.getExpressions().clear();
        expressionDTO.addExpressions(firstExpression);
        expressionDTO.addExpressions(secondExpression);
        scoreDTO.setExpression(expressionDTO);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with invalid operator type. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.numeric", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with invalid operator type. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //invalid size
        for (int i = 0; i < random.nextInt(11) + 1; i++) {
            expressionDTO.addExpressions(new ExpressionDTO());
        }
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with more than two expressions. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.notTwoExpressions",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with more than two expressions. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        expressionDTO.getExpressions().clear();
        if (random.nextBoolean()) {
            expressionDTO.addExpressions(new ExpressionDTO());
        }
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with less than two expressions. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.notTwoExpressions",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with less than two expressions. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //!= or == operator where both operators are equal and therefore valid
        if (random.nextBoolean()) {
            expressionDTO.setOperatorId(12L);
        } else {
            expressionDTO.setOperatorId(13L);
        }
        //both numeric
        expressionDTO.getExpressions().clear();
        firstExpression.setOperatorId(randomNumericOperatorId);
        secondExpression.setOperatorId(randomNumericOperatorId);
        ExpressionDTO firstChild = new ExpressionDTO();
        firstChild.setOperatorId(6L);
        firstChild.setValue(Helper.getRandomAlphabeticString(random.nextInt(11) + 1));
        ExpressionDTO secondChild = new ExpressionDTO();
        secondChild.setOperatorId(5L);
        secondChild.setQuestionId(Math.abs(random.nextLong()));
        firstExpression.addExpressions(firstChild);
        firstExpression.addExpressions(secondChild);
        secondExpression.addExpressions(firstChild);
        secondExpression.addExpressions(secondChild);
        expressionDTO.addExpressions(firstExpression);
        expressionDTO.addExpressions(secondExpression);
        scoreDTO.setExpression(expressionDTO);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with equal or unequal operator. The result has caught error although it wasn't expected to do.",
            result.hasErrors());
        //both boolean
        firstExpression.setOperatorId(randomBooleanOperatorId);
        secondExpression.setOperatorId(randomBooleanOperatorId);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with equal or unequal operator. The result has caught error although it wasn't expected to do.",
            result.hasErrors());

        //boolean-numeric
        secondExpression.setOperatorId(randomNumericOperatorId);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with equal/unequal operator and different child operators. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.twoExpressionsEqual",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with equal/unequal operator and different child operators. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //numeric-boolean
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        firstExpression.setOperatorId(randomNumericOperatorId);
        secondExpression.setOperatorId(randomBooleanOperatorId);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with equal/unequal operator and different child operators. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with equal/unequal operator and different child operators. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //wrong count
        firstExpression.setOperatorId(randomBooleanOperatorId);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //invalid size
        for (int i = 0; i < random.nextInt(11) + 1; i++) {
            expressionDTO.addExpressions(new ExpressionDTO());
        }
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with more than two expressions. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.notTwoExpressions",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with more than two expressions. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        expressionDTO.getExpressions().clear();
        if (random.nextBoolean()) {
            expressionDTO.addExpressions(new ExpressionDTO());
        }
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with less than two expressions. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.notTwoExpressions",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with less than two expressions. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //Valid expression with sum operator
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        expressionDTO.getExpressions().clear();
        expressionDTO.setOperatorId(7L);
        for (int i = 0; i < random.nextInt(11) + 1; i++) {
            ExpressionDTO child = new ExpressionDTO();
            if (random.nextBoolean()) {
                child.setOperatorId(5L);
                child.setQuestionId(Math.abs(random.nextLong()));
            } else {
                child.setOperatorId(6L);
                child.setValue(Helper.getRandomAlphabeticString(random.nextInt(11) + 1));
            }
            expressionDTO.addExpressions(child);
        }
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with sum operator. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        ExpressionDTO invalidExpression = new ExpressionDTO();
        invalidExpression.setOperatorId(null);
        expressionDTO.addExpressions(invalidExpression);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with a expression containing null as operator. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.operator.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with a expression containing null as operator. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        invalidExpression.setOperatorId(randomBooleanOperatorId);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid sum expression with a expression containing invalid operator type. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.numeric", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid sum expression with a expression containing invalid operator type. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //Minimum operator
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        expressionDTO.getExpressions().clear();
        expressionDTO.setOperatorId(18L);
        for (int i = 0; i < random.nextInt(11) + 1; i++) {
            ExpressionDTO child = new ExpressionDTO();
            if (random.nextBoolean()) {
                child.setOperatorId(5L);
                child.setQuestionId(Math.abs(random.nextLong()));
            } else {
                child.setOperatorId(6L);
                child.setValue(Helper.getRandomAlphabeticString(random.nextInt(11) + 1));
            }
            expressionDTO.addExpressions(child);
        }
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with minimum operator. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        invalidExpression.setOperatorId(null);
        expressionDTO.addExpressions(invalidExpression);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with minimum expression containing null as operator. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.operator.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with minimum expression containing null as operator. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        invalidExpression.setOperatorId(randomBooleanOperatorId);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid minimum expression with a expression containing invalid operator type. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.numeric", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid minimum expression with a expression containing invalid operator type. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //Maximum operator
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        expressionDTO.getExpressions().clear();
        expressionDTO.setOperatorId(17L);
        for (int i = 0; i < random.nextInt(11) + 1; i++) {
            ExpressionDTO child = new ExpressionDTO();
            if (random.nextBoolean()) {
                child.setOperatorId(5L);
                child.setQuestionId(Math.abs(random.nextLong()));
            } else {
                child.setOperatorId(6L);
                child.setValue(Helper.getRandomAlphabeticString(random.nextInt(11) + 1));
            }
            expressionDTO.addExpressions(child);
        }
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with maximum operator. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        invalidExpression.setOperatorId(null);
        expressionDTO.addExpressions(invalidExpression);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with maximum a expression containing null as operator. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.operator.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with maximum a expression containing null as operator. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        invalidExpression.setOperatorId(randomBooleanOperatorId);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid maximum expression with a expression containing invalid operator type. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.numeric", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid maximum expression with a expression containing invalid operator type. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //Average operator
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        expressionDTO.getExpressions().clear();
        expressionDTO.setOperatorId(15L);
        for (int i = 0; i < random.nextInt(11) + 1; i++) {
            ExpressionDTO child = new ExpressionDTO();
            if (random.nextBoolean()) {
                child.setOperatorId(5L);
                child.setQuestionId(Math.abs(random.nextLong()));
            } else {
                child.setOperatorId(6L);
                child.setValue(Helper.getRandomAlphabeticString(random.nextInt(11) + 1));
            }
            expressionDTO.addExpressions(child);
        }
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with average operator. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        invalidExpression.setOperatorId(null);
        expressionDTO.addExpressions(invalidExpression);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with a average expression containing null as operator. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.operator.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with a average expression containing null as operator. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        invalidExpression.setOperatorId(randomBooleanOperatorId);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid average expression with a expression containing invalid operator type. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.numeric", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid average expression with a expression containing invalid operator type. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        expressionDTO.getExpressions().clear();
        //Valid expression with counter operator
        expressionDTO.setOperatorId(14L);
        for (int i = 0; i < random.nextInt(11) + 1; i++) {
            ExpressionDTO child = new ExpressionDTO();
            ExpressionDTO first = new ExpressionDTO();
            ExpressionDTO second = new ExpressionDTO();

            child.setOperatorId(randomBooleanOperatorId);

            if (random.nextBoolean()) {
                first.setOperatorId(5L);
                first.setQuestionId(Math.abs(random.nextLong()));
                second.setOperatorId(6L);
                second.setValue(Helper.getRandomAlphabeticString(i + 1));
            } else {
                second.setOperatorId(5L);
                second.setQuestionId(Math.abs(random.nextLong()));
                first.setOperatorId(6L);
                first.setValue(Helper.getRandomAlphabeticString(i + 1));
            }

            child.addExpressions(first);
            child.addExpressions(second);
            expressionDTO.addExpressions(child);
        }
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with counter operator. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        invalidExpression.setOperatorId(null);
        expressionDTO.addExpressions(invalidExpression);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with a expression containing null as operator. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.operator.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with a expression containing null as operator. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        invalidExpression.setOperatorId(randomNumericOperatorId);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with a expression containing invalid operator type. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.expressions.boolean", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with a expression containing invalid operator type. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        expressionDTO.getExpressions().clear();
        expressionDTO.setOperatorId(16L);
        expressionDTO.setScoreId(Math.abs(random.nextLong()));
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with valueOfScore as operator type. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        expressionDTO.setScoreId(null);
        scoreDTOValidator.validate(scoreDTO, result);
        assertTrue(
            "Validation of scoreDTO failed for invalid instance with valueOfScore as operator type. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());
        //check error message
        message = messageSource.getMessage("score.error.scoreId.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of scoreDTO failed for invalid instance with valueOfScore as operator type. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        firstExpression.getExpressions().clear();
        secondExpression.getExpressions().clear();
        firstExpression.setOperatorId(5L);
        firstExpression.setQuestionId(Math.abs(random.nextLong()));
        secondExpression.setOperatorId(6L);
        secondExpression.setValue(Helper.getRandomAlphabeticString(random.nextInt(11) + 1));
        expressionDTO.addExpressions(firstExpression);
        expressionDTO.addExpressions(secondExpression);
        expressionDTO.setOperatorId(1L);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with plus as operator type. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        expressionDTO.setOperatorId(2L);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with minus as operator type. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        expressionDTO.setOperatorId(3L);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with multiply as operator type. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        expressionDTO.setOperatorId(4L);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with divide as operator type. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        expressionDTO.setOperatorId(8L);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with greater as operator type. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        expressionDTO.setOperatorId(9L);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with greater equals as operator type. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        expressionDTO.setOperatorId(10L);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with less as operator type. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        expressionDTO.setOperatorId(11L);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with less equals as operator type. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        expressionDTO.setOperatorId(12L);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with equals as operator type. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        expressionDTO.setOperatorId(13L);
        scoreDTOValidator.validate(scoreDTO, result);
        assertFalse(
            "Validation of scoreDTO failed for valid instance with different as operator type. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());
    }
}
