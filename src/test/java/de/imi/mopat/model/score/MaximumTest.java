package de.imi.mopat.model.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterTest;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class MaximumTest {

    private static final Random random = new Random();
    private Maximum maximumOperator;
    private MultiExpression multiExpression;
    private Encounter encounter;

    /**
     * Returns a new instance of {@link Maximum}
     *
     * @return New instance of {@link Maximum}
     */
    public static Maximum getNewValidMaximum() {
        return new Maximum();
    }

    @Before
    public void setUp() {
        maximumOperator = MaximumTest.getNewValidMaximum();
        multiExpression = MultiExpressionTest.getNewValidMultiExpressionWithUnaries(
            maximumOperator);
        encounter = EncounterTest.getNewValidEncounter();
    }

    /**
     * Test of {@link Maximum#evaluate}<br> Valid input: random {@link MultiExpression} random
     * number of random {@link UnaryExpression UnaryExpressions} with random values.
     */
    @Test
    public void testEvaluate() {
        List<Expression> expressions = multiExpression.getExpressions();

        // Add a expression with a greater value than his predecessor
        int greaterPosition = random.nextInt(expressions.size()) + 1;
        UnaryExpression greaterExpression = UnaryExpressionTest.getNewValidUnaryExpression();
        greaterExpression.setValue(
            ((UnaryExpression) expressions.get(greaterPosition - 1)).getValue()
                + random.nextDouble());
        expressions.add(greaterPosition, greaterExpression);

        // Add random expressions with null as value
        UnaryExpression testNullValueExpression = UnaryExpressionTest.getNewValidUnaryExpression();
        testNullValueExpression.setValue(null);
        expressions.add(0, testNullValueExpression);
        int countNullValues = random.nextInt(5) + 1;
        for (int i = 0; i < countNullValues; i++) {
            expressions.add(random.nextInt(expressions.size()), testNullValueExpression);
        }
        multiExpression.setExpressions(expressions);

        Iterator<Expression> iterator = expressions.iterator();
        Expression currentExpression, maximumExpression = iterator.next();
        Double value = (Double) maximumExpression.evaluate(encounter);

        while (iterator.hasNext()) {
            currentExpression = iterator.next();
            Double currentValue = (Double) currentExpression.evaluate(encounter);

            if (value != null) {
                if (currentValue != null && currentValue > value) {
                    value = currentValue;
                }
            } else {
                value = currentValue;
            }
        }

        Double testValue = (Double) maximumOperator.evaluate(multiExpression, encounter);
        assertNotNull(
            "Evaluating maximum failed. The returned value was null although not null was expected.",
            testValue);
        assertEquals(
            "Evaluating maximum failed. The returned value didn't match the expected value.", value,
            testValue);
    }

    /**
     * Test of {@link Maximum#evaluate}<br> Invalid input: random {@link UnaryExpression} and random
     * {@link BinaryExpression}
     */
    @Test
    public void testEvaluateWithWrongExpressions() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        Double testValue = (Double) maximumOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating maximum failed. The returned value didn't match null although a unary expression was used as param",
            testValue);

        expression = BinaryExpressionTest.getNewValidBinaryExpression();
        testValue = (Double) maximumOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating maximum failed. The returned value didn't match null although a binary expression was used as param",
            testValue);
    }

    /**
     * Test of {@link Maximum#evaluate}<br> Invalid input: random {@link MultiExpression} with
     * <code>null</code> as {@link Expression Expressions}
     */
    @Test
    public void testEvaluateNull() {
        multiExpression.setExpressions(null);
        Double testValue = (Double) maximumOperator.evaluate(multiExpression, encounter);
        assertNull(
            "Evaluating maximum failed. The returned value didn't match null although it was expected to be.",
            testValue);
    }

    /**
     * Test of {@link Maximum#evaluate}<br> Invalid input: random {@link MultiExpression} with an
     * empty list of {@link Expression Expressions}.
     */
    @Test
    public void testEvaluateEmpty() {
        List<Expression> expressions = new ArrayList<>();
        multiExpression.setExpressions(expressions);
        Double testValue = (Double) maximumOperator.evaluate(multiExpression, encounter);
        assertNull(
            "Evaluating maximum failed. The returned value didn't match null although it was expected to be.",
            testValue);
    }

    /**
     * Test of {@link Maximum#getFormula}<br> Valid input: random {@link MultiExpression} with
     * random number of {@link UnaryExpression UnaryExpressions}
     */
    @Test
    public void testGetFormula() {
        StringBuilder formula = new StringBuilder();
        formula.append("Maximum of(");
        Integer size = random.nextInt(13) + 2;
        List<Expression> expressions = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            UnaryExpression unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression();

            Double value = random.nextDouble();
            unaryExpression.setValue(value);
            formula.append(value);
            if (i < size - 1) {
                formula.append(", ");
            }
            expressions.add(unaryExpression);
        }

        formula.append(")");

        multiExpression.setExpressions(expressions);

        String testFormula = maximumOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNotNull(
            "Getting formula maximum failed. The returned value was null although not null was expected");
        assertEquals(
            "Getting formula maximum failed. The returned value didn't match the expected value",
            formula.toString(), testFormula);
    }

    /**
     * Test of {@link Maximum#getFormula}<br> Invalid input: random {@link MultiExpression} with
     * <code>null</code> as {@link Expression Expressions}.
     */
    @Test
    public void testGetFormulaNull() {
        multiExpression.setExpressions(null);
        String testFormula = maximumOperator.getFormula(multiExpression, encounter,
            Helper.getRandomAlphabeticString(5));
        assertNull("Getting formula maximum failed. The returned value didn't match null",
            testFormula);
    }

    /**
     * Test of {@link Maximum#getFormula}<br> Invalid input: random {@link MultiExpression} with an
     * empty list of {@link Expression Expressions}.
     */
    @Test
    public void testGetFormulaEmpty() {
        multiExpression.setExpressions(new ArrayList<>());
        String testFormula = maximumOperator.getFormula(multiExpression, encounter,
            Helper.getRandomAlphabeticString(5));
        assertNull("Getting formula maximum failed. The returned value didn't match null",
            testFormula);
    }

    /**
     * Test of {@link Maximum#getFormula}<br> Invalid input: random {@link UnaryExpression} and
     * random {@link BinaryExpression}
     */
    @Test
    public void testGetFormulaWithWrongExpression() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        String formula = maximumOperator.getFormula(expression, encounter,
            Helper.getRandomAlphabeticString(5));
        assertNull(
            "Getting formula maximum failed. The returned value didn't match null although a unary expression was used as param",
            formula);

        expression = BinaryExpressionTest.getNewValidBinaryExpression();
        formula = maximumOperator.getFormula(expression, encounter,
            Helper.getRandomAlphabeticString(5));
        assertNull(
            "Getting formula maximum failed. The returned value didn't match null although a binary expression was used as param",
            formula);
    }

}
