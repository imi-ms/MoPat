package de.imi.mopat.model.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterTest;
import de.imi.mopat.utils.Helper;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ValueOperatorTest {

    private static final Random random = new Random();
    private ValueOperator valueOperator;
    private UnaryExpression unaryExpression;
    private Encounter encounter;
    private Double value;

    /**
     * Returns a new valid instance of {@link ValueOperator}
     *
     * @return New valid valueOperator instance
     */
    public static ValueOperator getNewValidValueOperator() {
        return new ValueOperator();
    }

    @Before
    public void setUp() {
        valueOperator = ValueOperatorTest.getNewValidValueOperator();
        unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression();
        encounter = EncounterTest.getNewValidEncounter();
        value = Math.abs(random.nextDouble());
    }

    /**
     * Test of {@link ValueOperator#evaluate}<br> Valid input: random {@link UnaryExpression} with
     * random value
     */
    @Test
    public void testEvaluate() {
        unaryExpression.setValue(value);
        Double testValue = valueOperator.evaluate(unaryExpression, encounter);
        assertNotNull(
            "Evaluate fails. The returned value was null although a not null value was expected.",
            testValue);
        assertEquals("Evaluate fails. The returned value didn't match the expected value.", value,
            testValue);
    }

    /**
     * Test of {@link ValueOperator#evaluate}<br> Invalid input: <code>null</code>, random
     * {@link BinaryExpression} and random {@link MultiExpression}
     */
    @Test
    public void testEvaluateWithWrongExpressions() {
        Expression expression = BinaryExpressionTest.getNewValidBinaryExpression();
        Double testValue = valueOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluate fails. The returned value didn't match null although a binary expression was used as param.",
            testValue);

        expression = MultiExpressionTest.getNewValidMultiExpression();
        testValue = valueOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluate fails. The returned value didn't match null although a multi expression was used as param.",
            testValue);

        testValue = valueOperator.evaluate(null, encounter);
        assertNull(
            "Evaluate fails. The returned value didn't match null although null was used as param.",
            testValue);
    }

    /**
     * Test of {@link ValueOperator#evaluate}<br> Invalid input: {@link UnaryExpression} with value
     * <code>null</code>
     */
    @Test
    public void testEvaluateNullExpression() {
        unaryExpression.setValue(null);
        Double testValue = valueOperator.evaluate(unaryExpression, encounter);
        assertNull("Evaluate fails. The returned value didn't match null.", testValue);
    }

    /**
     * Test of {@link ValueOperator#getFormula}<br> Valid input: random {@link UnaryExpression} with
     * random value
     */
    @Test
    public void testGetFormula() {
        unaryExpression.setValue(value);
        String formula = value.toString();
        String testFormula = valueOperator.getFormula(unaryExpression, encounter,
            Helper.getRandomLocale());
        assertNotNull(
            "Getting formula fails. The returned value was null although a not null value was expected.",
            testFormula);
        assertEquals("Getting formula fails. The returned value didn't match the expected value.",
            formula, testFormula);
    }

    /**
     * Test of {@link ValueOperator#getFormula}<br> Invalid input: <code>null</code>, random
     * {@link BinaryExpression} and random {@link MultiExpression}
     */
    @Test
    public void testGetFormulaWithWrongExpressions() {
        Expression expression = BinaryExpressionTest.getNewValidBinaryExpression();
        String testFormula = valueOperator.getFormula(expression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Getting formula failed. The returned value didn't match null although a binary expression was used as param.",
            testFormula);

        expression = MultiExpressionTest.getNewValidMultiExpression();
        testFormula = valueOperator.getFormula(expression, encounter, Helper.getRandomLocale());
        assertNull(
            "Getting formula failed. The returned value didn't match null although a multi expression was used as param.",
            testFormula);

        testFormula = valueOperator.getFormula(null, encounter, Helper.getRandomLocale());
        assertNull(
            "Getting formula failed. The returned value didn't match null although a multi expression was used as param.",
            testFormula);
    }
}
