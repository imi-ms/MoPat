package de.imi.mopat.model.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterTest;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ValueOfScoreOperatorTest {

    private static final Random random = new Random();
    private ValueOfScoreOperator valueOfScoreOperator;
    private Encounter encounter;
    private Score score;
    private UnaryExpression unaryExpression;

    /**
     * Returns a new valid instance of {@link ValueOfScoreOperator}
     *
     * @return New valid valueOfScoreOperator instance
     */
    public static ValueOfScoreOperator getNewValidValueOfScoreOperator() {
        return new ValueOfScoreOperator();
    }

    @Before
    public void setUp() {
        valueOfScoreOperator = ValueOfScoreOperatorTest.getNewValidValueOfScoreOperator();
        encounter = EncounterTest.getNewValidEncounter();
        score = ScoreTest.getNewValidScore();
    }

    /**
     * Test of {@link ValueOfScoreOperator#evaluate}<br> Valid input: random
     * {@link BinaryExpression} with two {@link UnaryExpression UnaryExpressions} with random
     * values
     */
    @Test
    public void testEvaluate() {
        BinaryOperatorNumeric binaryOperator = BinaryOperatorNumericTest.getNewValidBinaryOperatorNumeric();
        BinaryExpression binaryExpression = BinaryExpressionTest.getNewValidBinaryExpressionWithUnaries(
            binaryOperator);
        UnaryExpression unary1 = UnaryExpressionTest.getNewValidUnaryExpression(), unary2 = UnaryExpressionTest.getNewValidUnaryExpression();
        Double value1 = random.nextDouble(), value2 = random.nextDouble();
        unary1.setValue(value1);
        unary2.setValue(value2);
        List<Expression> expressions = new ArrayList<>();
        expressions.add(unary1);
        expressions.add(unary2);
        binaryExpression.setExpressions(expressions);
        score.setExpression(binaryExpression);
        unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression(score,
            valueOfScoreOperator);

        Double value = 0.0;
        if (binaryOperator instanceof Divide) {
            value = value1 / value2;
        }
        if (binaryOperator instanceof Minus) {
            value = value1 - value2;
        }
        if (binaryOperator instanceof Plus) {
            value = value1 + value2;
        }

        Double testValue = (Double) valueOfScoreOperator.evaluate(unaryExpression, encounter);
        assertNotNull(
            "Evaluating valueOfScoreOperator failed. The expected value was null although not null was expected.",
            testValue);
        assertEquals(
            "Evaluating valueOfScoreOperator failed. The expected value was null although not null was expected.",
            value, testValue);
    }

    /**
     * Test of {@link ValueOfScoreOperator#evaluate}<br> Invalid input: <code>null</code>, random
     * {@link BinaryExpression} and random {@link MultiExpression}
     */
    @Test
    public void testEvaluateWithWrongExpressions() {
        Expression expression = BinaryExpressionTest.getNewValidBinaryExpression();
        Object testValue = valueOfScoreOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating valueOfScoreOperator failed. The returned value didn't match null although binary expression was used as param.",
            testValue);

        expression = MultiExpressionTest.getNewValidMultiExpression();
        testValue = valueOfScoreOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating valueOfScoreOperator failed. The returned value didn't match null although multi expression was used as param.",
            testValue);

        testValue = valueOfScoreOperator.evaluate(null, encounter);
        assertNull(
            "Evaluating valueOfScoreOperator failed. The returned value didn't match null although null was used as param.",
            testValue);
    }

    /**
     * Test of {@link ValueOfScoreOperator#evaluate}<br> Invalid input: {@link UnaryExpression} with
     * {@link Score}
     * <code>null</code>
     */
    @Test
    public void testEvaluateNullScore() {
        unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression(null,
            valueOfScoreOperator);
        Object testValue = valueOfScoreOperator.evaluate(unaryExpression, encounter);
        assertNull("Evaluating valueOfScoreOperator failed. The returned value didn't match null",
            testValue);
    }

    /**
     * Test of {@link ValueOfScoreOperator#getFormula}<br> Valid input: random
     * {@link BinaryExpression} with two {@link UnaryExpression UnaryExpressions} with random
     * values
     */
    @Test
    public void testGetFormula() {
        BinaryOperatorNumeric binaryOperator = BinaryOperatorNumericTest.getNewValidBinaryOperatorNumeric();
        BinaryExpression binaryExpression = BinaryExpressionTest.getNewValidBinaryExpressionWithUnaries(
            binaryOperator);
        UnaryExpression unary1 = UnaryExpressionTest.getNewValidUnaryExpression(), unary2 = UnaryExpressionTest.getNewValidUnaryExpression();
        Double value1 = random.nextDouble(), value2 = random.nextDouble();
        unary1.setValue(value1);
        unary2.setValue(value2);
        List<Expression> expressions = new ArrayList<>();
        expressions.add(unary1);
        expressions.add(unary2);
        binaryExpression.setExpressions(expressions);
        score.setExpression(binaryExpression);
        unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression(score,
            valueOfScoreOperator);

        StringBuffer formula = new StringBuffer();
        if (binaryOperator instanceof Divide) {
            formula.append("(").append(value1).append(" / ").append(value2).append(")");
        }
        if (binaryOperator instanceof Minus) {
            formula.append("(").append(value1).append(" - ").append(value2).append(")");
        }
        if (binaryOperator instanceof Plus) {
            formula.append("(").append(value1).append(" + ").append(value2).append(")");
        }

        String testValue = valueOfScoreOperator.getFormula(unaryExpression, encounter,
            Helper.getRandomLocale());
        assertNotNull(
            "Evaluating valueOfScoreOperator failed. The expected value was null although not null was expected.",
            testValue);
        assertEquals(
            "Evaluating valueOfScoreOperator failed. The expected value was null although not null was expected.",
            formula.toString(), testValue);
    }

    /**
     * Test of {@link ValueOfScoreOperator#getFormula}<br> Invalid input: <code>null</code>, random
     * {@link BinaryExpression} and random {@link MultiExpression}
     */
    @Test
    public void testGetFormulaWithWrongExpressions() {
        Expression expression = BinaryExpressionTest.getNewValidBinaryExpression();
        String testFormula = valueOfScoreOperator.getFormula(expression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Getting formula failed. The returned value didn't match null although a binary expression was used as param",
            testFormula);

        expression = MultiExpressionTest.getNewValidMultiExpression();
        testFormula = valueOfScoreOperator.getFormula(expression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Getting formula failed. The returned value didn't match null although a multi expression was used as param",
            testFormula);

        testFormula = valueOfScoreOperator.getFormula(null, encounter, Helper.getRandomLocale());
        assertNull(
            "Getting formula failed. The returned value didn't match null although null was used as param",
            testFormula);
    }

    /**
     * Test of {@link ValueOfScoreOperator#getFormula}<br> Invalid input: {@link UnaryExpression}
     * with {@link Score}
     * <code>null</code>
     */
    @Test
    public void testGetFormulaNullScore() {
        unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression(null,
            valueOfScoreOperator);
        String testFormula = valueOfScoreOperator.getFormula(unaryExpression, encounter,
            Helper.getRandomLocale());
        assertNull("Getting formula failed. The returned value didn't match null", testFormula);
    }
}
