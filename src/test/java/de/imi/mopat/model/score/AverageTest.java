package de.imi.mopat.model.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterTest;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class AverageTest {

    private static final Random random = new Random();
    private Average averageOperator;
    private MultiExpression multiExpression;
    private Encounter encounter;

    /**
     * Returns a new valid instance of {@link Average}
     *
     * @return New valid Average instance
     */
    public static Average getNewValidAverage() {
        Average instance = new Average();

        return instance;
    }

    @Before
    public void setUp() {
        averageOperator = AverageTest.getNewValidAverage();
        multiExpression = MultiExpressionTest.getNewValidMultiExpressionWithUnaries(
            averageOperator);
        encounter = EncounterTest.getNewValidEncounter();
    }

    /**
     * Test of {@link Average#evaluate} method.<br> Valid input: random {@link MultiExpression} with
     * random existing {@link Expression Expression}
     */
    @Test
    public void testEvaluate() {
        List<Expression> expressions = multiExpression.getExpressions();

        Double sum = 0.0;
        Double counter = 0.0;
        Iterator<Expression> iterator = expressions.iterator();
        Double sumExpressionResult = (Double) iterator.next().evaluate(encounter);
        while (iterator.hasNext()) {
            if (sumExpressionResult != null) {
                counter++;
                sum += sumExpressionResult;
            }
            sumExpressionResult = (Double) iterator.next().evaluate(encounter);
        }

        Double value = sum / counter;
        Double testValue = (Double) averageOperator.evaluate(multiExpression, encounter);

        assertNotNull(
            "Evaluating average failed. The returned value was null although not null value was expected.",
            testValue);
        assertEquals(
            "Evaluating average failed. The returned value didn't match the expected value.", value,
            testValue);
    }

    /**
     * Test of {@link Average#evaluate} method.<br> Valid input: {@link MultiExpression} with only
     * one {@link UnaryExpression} without a value.
     */
    @Test
    public void testEvaluateWithCounterZero() {
        List<Expression> testExpressions = new ArrayList<>();

        UnaryExpression testExpression = UnaryExpressionTest.getNewValidUnaryExpression();
        testExpression.setValue(null);
        testExpressions.add(testExpression);

        UnaryExpression testMissingExpression = UnaryExpressionTest.getNewValidUnaryExpression();
        testMissingExpression.setValue(1.0);
        testExpressions.add(testMissingExpression);

        multiExpression.setExpressions(testExpressions);

        assertEquals("The value was not 0.0 although there was ony one expression without a value.",
            0.0, averageOperator.evaluate(multiExpression, encounter));
    }

    /**
     * Test of {@link Average#evaluate} method.<br> Valid input: random {@link MultiExpression} with
     * random missing and existing {@link Expression Expression}
     */
    @Test
    public void testEvaluateWithMissingExpressions() {
        List<Expression> expressions = new ArrayList<>();
        Double sum = 0.0, counter = 0.0, value, missingExpressions = 0.0, allowedMissingExpressions =
            ((Integer) random.nextInt(5)).doubleValue() + 1.0;
        Integer countExpressions = random.nextInt(17) + allowedMissingExpressions.intValue();
        UnaryExpression expression;

        for (int i = 0; i < countExpressions; i++) {
            expression = UnaryExpressionTest.getNewValidUnaryExpression();
            // Until the last expression ist not reached yet, set the expressions (with or without value) from which the average ist to be calculated
            if (i < countExpressions - 1) {
                // Set randomly missing expressions
                if (random.nextBoolean()) {
                    expression.setValue(null);
                    missingExpressions++;
                    // Or set the value and increase the counter and sum
                } else {
                    Double expressionValue = random.nextDouble();
                    expression.setValue(expressionValue);
                    sum += expressionValue;
                    counter++;
                }
                // The last expressions shows how many missing expressions are allowed
            } else {
                expression.setValue(allowedMissingExpressions);
            }
            // Add this expression to the list of all expressions
            expressions.add(expression);
        }
        // And set this list as the expressions
        multiExpression.setExpressions(expressions);

        // If there are more missing expressions than allowed or only one expression (the counter) than set the value to null
        if (missingExpressions > allowedMissingExpressions || countExpressions < 2) {
            value = null;
            // If the counter is 0 set the value to 0.0
        } else if (counter == 0) {
            value = 0.0;
            // Otherwise calculate the average
        } else {
            value = sum / counter;
        }

        Double testValue = (Double) averageOperator.evaluate(multiExpression, encounter);

        if (value == null) {
            assertNull(
                "Evaluating average failed. The returned value was not-null although null was expected.",
                testValue);
        } else {
            if (counter > 0) {
                value = sum / counter;
            } else {
                value = 0.0;
            }

            assertNotNull(
                "Evaluating average failed. The returned value was null although not-null was expected",
                testValue);
            assertEquals(
                "Evaluating average failed. The returned value didn't match the expected value",
                value, testValue);
        }
    }

    /**
     * Test of {@link Average#evaluate} method.<br> Invalid input: random {@link UnaryExpression}
     * and random {@link BinaryExpression}
     */
    @Test
    public void testEvaluateWrongExpression() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        Double testValue = (Double) averageOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating average failed. The returned value didn't match null although a unary expression was used as param",
            testValue);

        expression = BinaryExpressionTest.getNewValidBinaryExpression();
        testValue = (Double) averageOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating average failed. The returned value didn't match null although a binary expression was used as param",
            testValue);
    }

    /**
     * Test of {@link Average#evaluate} method.<br> Invalid input: random {@link MultiExpression}
     * with no {@link Expression Expressions}
     */
    @Test
    public void testEvaluateEmptyExpressions() {
        multiExpression.setExpressions(new ArrayList<>());
        Double testValue = (Double) averageOperator.evaluate(multiExpression, encounter);
        assertNull(
            "Evaluating average failed. The returned value didn't match null although an empty expression was used as param",
            testValue);
    }

    /**
     * Test of {@link Average#evaluate} method.<br> Invalid input: random {@link MultiExpression}
     * with <code>null</code> as {@link Expression Expressions}
     */
    @Test
    public void testEvaluateNullExpressions() {
        multiExpression.setExpressions(null);
        Double testValue = (Double) averageOperator.evaluate(multiExpression, encounter);
        assertNull(
            "Evaluating average failed. The returned value didn't match null although a null expression was used as param",
            testValue);
    }

    /**
     * Test of {@link Average#getFormula} method.<br> Valid input: random {@link MultiExpression}
     * with random missing and existing {@link Expression Expressions} and random number of missing
     * Expressions
     */
    @Test
    public void testGetFormula() {
        StringBuffer existingExpressions = new StringBuffer();
        existingExpressions.append("Average(");
        StringBuffer missingValues = new StringBuffer();
        missingValues.append("Missing Values(");
        List<Expression> expressions = new ArrayList<>();
        Double allowedMissingExpressions = random.nextInt(5) + 1.0;
        int counter = 0, missingExpressions = 0, size =
            random.nextInt(17) + allowedMissingExpressions.intValue();
        UnaryExpression expression;

        for (int i = 0; i < size; i++) {
            expression = UnaryExpressionTest.getNewValidUnaryExpression();
            if (i > 0 && random.nextBoolean()) {
                expression.setValue(null);
                missingValues.append("null, ");
                missingExpressions++;
            } else {
                Double expressionValue = random.nextDouble();
                expression.setValue(expressionValue);
                existingExpressions.append(expressionValue).append(", ");
                counter++;
            }
            expressions.add(expression);
        }
        // Add allowed missing expressions
        expression = UnaryExpressionTest.getNewValidUnaryExpression();
        expression.setValue(allowedMissingExpressions);
        expressions.add(expression);

        existingExpressions.replace(existingExpressions.length() - 2,
            existingExpressions.length() - 1, ")");
        missingValues.replace(missingValues.length() - 2, missingValues.length() - 1, ")");

        StringBuilder formula = new StringBuilder();
        if (counter > 0 && missingExpressions > 0) {
            formula.append(existingExpressions).append(", ").append(missingValues);
        } else if (counter > 0) {
            formula.append(existingExpressions);
        } else if (missingExpressions > 0) {
            formula.append(missingValues);
        }

        multiExpression.setExpressions(expressions);

        String testFormula = averageOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNotNull(
            "Get formula failed. The returned value was null although not null was expected.",
            testFormula);
        assertEquals("Get formula failed. The returned value didn't match the expected value.",
            formula.toString(), testFormula);
    }

    /**
     * Test of {@link Average#getFormula} method.<br> Valid input: random {@link MultiExpression}
     * with only random existing {@link Expression Expressions} and random number of missing
     * Expressions
     */
    @Test
    public void testGetFormulaOnlyUsedExpressions() {
        StringBuilder formula = new StringBuilder();
        formula.append("Average(");
        List<Expression> expressions = new ArrayList<>();
        Double allowedMissingExpressions = random.nextInt(5) + 1.0;
        int size = random.nextInt(17) + allowedMissingExpressions.intValue();
        UnaryExpression expression;

        for (int i = 0; i < size; i++) {
            expression = UnaryExpressionTest.getNewValidUnaryExpression();
            Double expressionValue = random.nextDouble();
            expression.setValue(expressionValue);
            formula.append(expressionValue).append(", ");
            expressions.add(expression);
        }
        // Add allowed missing expressions
        expression = UnaryExpressionTest.getNewValidUnaryExpression();
        expression.setValue(allowedMissingExpressions);
        expressions.add(expression);

        formula.replace(formula.length() - 2, formula.length() - 1, ")");
        multiExpression.setExpressions(expressions);

        String testFormula = averageOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNotNull(
            "Get formula failed. The returned value was null although not null was expected.",
            testFormula);
        assertEquals("Get formula failed. The returned value didn't match the expected value.",
            formula.toString(), testFormula);
    }

    /**
     * Test of {@link Average#getFormula} method.<br> Valid input: random {@link MultiExpression}
     * with only random missing {@link Expression Expressions} and random number of missing
     * Expressions
     */
    @Test
    public void testGetFormulaOnlyMissingExpressions() {
        StringBuilder formula = new StringBuilder();
        formula.append("Missing Values(");
        List<Expression> expressions = new ArrayList<>();
        Double allowedMissingExpressions = random.nextInt(5) + 1.0;
        UnaryExpression expression;

        for (int i = 0; i < allowedMissingExpressions.intValue(); i++) {
            expression = UnaryExpressionTest.getNewValidUnaryExpression();
            expression.setValue(null);
            formula.append("null, ");
            expressions.add(expression);
        }
        // Add allowed missing expressions
        expression = UnaryExpressionTest.getNewValidUnaryExpression();
        expression.setValue(allowedMissingExpressions);
        expressions.add(expression);

        formula.replace(formula.length() - 2, formula.length() - 1, ")");
        multiExpression.setExpressions(expressions);

        String testFormula = averageOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNotNull(
            "Get formula failed. The returned value was null although not null was expected.",
            testFormula);
        assertEquals("Get formula failed. The returned value didn't match the expected value.",
            formula.toString(), testFormula);
    }

    /**
     * Test of {@link Average#evaluate} method.<br> Invalid input: random {@link UnaryExpression}
     * and random {@link BinaryExpression}
     */
    @Test
    public void testGetFormulaWrongExpression() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        String testFormula = averageOperator.getFormula(expression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Getting formula with unary expression failed. The returned value wasn't null although it was expected to be null.",
            testFormula);

        expression = BinaryExpressionTest.getNewValidBinaryExpression();
        testFormula = averageOperator.getFormula(expression, encounter, Helper.getRandomLocale());
        assertNull(
            "Getting formula with binary expression failed. The returned value wasn't null although it was expected to be null.",
            testFormula);
    }

    /**
     * Test of {@link Average#evaluate} method.<br> Invalid input: random {@link MultiExpression}
     * with empty list of {@link Expression Expressions}
     */
    @Test
    public void testGetFormulaEmptyExpressions() {
        multiExpression.setExpressions(new ArrayList<>());
        String testFormula = averageOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Getting formula with empty expressions failed. The returned value wasn't null although it was expected to be null.",
            testFormula);
    }

    /**
     * Test of {@link Average#evaluate} method.<br> Invalid input: random {@link MultiExpression}
     * with only one {@link Expression Expressions}
     */
    @Test
    public void testGetFormulaSingleExpression() {
        List<Expression> expressions = new ArrayList<>();
        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        multiExpression.setExpressions(expressions);
        String testFormula = averageOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Getting formula with null expressions failed. The returned value wasn't null although it was expected to be null.",
            testFormula);
    }

    /**
     * Test of {@link Average#evaluate} method.<br> Invalid input: random {@link MultiExpression}
     * with null as {@link Expression Expressions}
     */
    @Test
    public void testGetFormulaNull() {
        multiExpression.setExpressions(null);
        String testFormula = averageOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Getting formula with null expressions failed. The returned value wasn't null although it was expected to be null.",
            testFormula);
    }
}
