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
public class MinimumTest {

    private static final Random random = new Random();
    private Minimum minimumOperator;
    private MultiExpression multiExpression;
    private Encounter encounter;

    /**
     * Returns a valid new instance of {@link Minimum}
     *
     * @return New instance of {@link Minimum}
     */
    public static Minimum getNewValidMinimum() {
        return new Minimum();
    }

    @Before
    public void setUp() {
        minimumOperator = MinimumTest.getNewValidMinimum();
        multiExpression = MultiExpressionTest.getNewValidMultiExpressionWithUnaries(
            minimumOperator);
        encounter = EncounterTest.getNewValidEncounter();
    }

    /**
     * Test of {@link Minimum#evaluate}<br> Valid input: random {@link MultiExpression} random
     * number of random {@link UnaryExpression UnaryExpressions} with random values.
     */
    @Test
    public void testEvaluate() {
        List<Expression> expressions = multiExpression.getExpressions();

        // Add a expression with a less value than his predecessor
        int lessPosition = random.nextInt(expressions.size()) + 1;
        UnaryExpression lessExpression = UnaryExpressionTest.getNewValidUnaryExpression();
        lessExpression.setValue(
            ((UnaryExpression) expressions.get(lessPosition - 1)).getValue() - random.nextDouble());
        expressions.add(lessPosition, lessExpression);

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
        Expression currentExpression, minimumExpression = iterator.next();
        Double value = (Double) minimumExpression.evaluate(encounter);

        while (iterator.hasNext()) {
            currentExpression = iterator.next();
            Double currentValue = (Double) currentExpression.evaluate(encounter);

            if (value != null) {
                if (currentValue != null && currentValue < value) {
                    value = currentValue;
                }
            } else {
                value = currentValue;
            }
        }

        Double testValue = (Double) minimumOperator.evaluate(multiExpression, encounter);
        assertNotNull(
            "Evaluating minimum failed. The returned value was null although not null was expected.",
            testValue);
        assertEquals(
            "Evaluating minimum failed. The returned value didn't match the expected value.", value,
            testValue);
    }

    /**
     * Test of {@link Minimum#evaluate}<br> Invalid input: random {@link UnaryExpression} and random
     * {@link BinaryExpression}
     */
    @Test
    public void testEvaluateWithWrongExpressions() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        Double testValue = (Double) minimumOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating minimum failed. The returned value didn't match null although a unary expression was used as param",
            testValue);

        expression = BinaryExpressionTest.getNewValidBinaryExpression();
        testValue = (Double) minimumOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating minimum failed. The returned value didn't match null although a binary expression was used as param",
            testValue);
    }

    /**
     * Test of {@link Minimum#evaluate}<br> Invalid input: random {@link MultiExpression} with
     * <code>null</code> as {@link Expression Expressions}
     */
    @Test
    public void testEvaluateNull() {
        multiExpression.setExpressions(null);
        Double testValue = (Double) minimumOperator.evaluate(multiExpression, encounter);
        assertNull(
            "Evaluating minimum failed. The returned value didn't match null although it was expected to be.",
            testValue);
    }

    /**
     * Test of {@link Minimum#evaluate}<br> Invalid input: random {@link MultiExpression} with an
     * empty list of {@link Expression Expressions}.
     */
    @Test
    public void testEvaluateEmpty() {
        List<Expression> expressions = new ArrayList<>();
        multiExpression.setExpressions(expressions);
        Double testValue = (Double) minimumOperator.evaluate(multiExpression, encounter);
        assertNull(
            "Evaluating minimum failed. The returned value didn't match null although it was expected to be.",
            testValue);
    }

    /**
     * Test of {@link Minimum#getFormula}<br> Valid input: random {@link MultiExpression} with
     * random number of {@link UnaryExpression UnaryExpressions}
     */
    @Test
    public void testGetFormula() {
        StringBuilder formula = new StringBuilder();
        formula.append("Minimum of(");
        Integer size = random.nextInt(13) + 1;
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

        String testFormula = minimumOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNotNull(
            "Getting formula maximum failed. The returned value was null although not null was expected");
        assertEquals(
            "Getting formula maximum failed. The returned value didn't match the expected value",
            formula.toString(), testFormula);
    }

    /**
     * Test of {@link Minimum#getFormula}<br> Invalid input: random {@link MultiExpression} with
     * <code>null</code> as {@link Expression Expressions}.
     */
    @Test
    public void testGetFormulaNull() {
        multiExpression.setExpressions(null);
        String testFormula = minimumOperator.getFormula(multiExpression, encounter,
            Helper.getRandomAlphabeticString(5));
        assertNull("Getting formula minimum failed. The returned value didn't match null",
            testFormula);
    }

    /**
     * Test of {@link Minimum#getFormula}<br> Invalid input: random {@link MultiExpression} with an
     * empty list of {@link Expression Expressions}.
     */
    @Test
    public void testGetFormulaEmpty() {
        multiExpression.setExpressions(new ArrayList<>());
        String testFormula = minimumOperator.getFormula(multiExpression, encounter,
            Helper.getRandomAlphabeticString(5));
        assertNull("Getting formula minimum failed. The returned value didn't match null",
            testFormula);
    }

    /**
     * Test of {@link Minimum#getFormula}<br> Invalid input: random {@link UnaryExpression} and
     * random {@link BinaryExpression}
     */
    @Test
    public void testGetFormulaWrongExpression() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        String formula = minimumOperator.getFormula(expression, encounter,
            Helper.getRandomAlphabeticString(5));
        assertNull(
            "Getting formula minimum failed. The returned value didn't match null although a unary expression was used as param",
            formula);

        expression = BinaryExpressionTest.getNewValidBinaryExpression();
        formula = minimumOperator.getFormula(expression, encounter,
            Helper.getRandomAlphabeticString(5));
        assertNull(
            "Getting formula minimum failed. The returned value didn't match null although a binary expression was used as param",
            formula);
    }

}
