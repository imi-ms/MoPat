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
public class SumTest {

    private static final Random random = new Random();
    private Sum sumOperator;
    private MultiExpression multiExpression;
    private Encounter encounter;

    /**
     * Return a new valid instance of {@link Sum}
     *
     * @return New valid sum instance.
     */
    public static Sum getNewValidSum() {
        return new Sum();
    }

    @Before
    public void setUp() {
        sumOperator = SumTest.getNewValidSum();
        multiExpression = MultiExpressionTest.getNewValidMultiExpressionWithUnaries(sumOperator);
        encounter = EncounterTest.getNewValidEncounter();
    }

    /**
     * Test of {@link Sum#evaluate}<br> Valid input: random {@link MultiExpression} with random
     * number of {@link UnaryExpression UnaryExpressions}
     */
    @Test
    public void testEvaluate() {
        List<Expression> expressions = new ArrayList<>();
        Integer size = random.nextInt(19) + 1;
        Double sum = 0.0, value;
        UnaryExpression expression;

        for (int i = 0; i < size; i++) {
            expression = UnaryExpressionTest.getNewValidUnaryExpression();
            value = random.nextDouble();
            expression.setValue(value);
            expressions.add(expression);
            sum += value;
        }

        multiExpression.setExpressions(expressions);

        Double testSum = sumOperator.evaluate(multiExpression, encounter);
        assertNotNull(
            "Evaluating sum failed. The returned value was null although not-null was expected.",
            testSum);
        assertEquals("Evaluating sum failed. The returned value didn't match the expected value.",
            sum, testSum);
    }

    /**
     * Test of {@link Sum#evaluate}<br> Invalid input: random {@link MultiExpression} with random
     * number of {@link UnaryExpression UnaryExpressions} with at least one instance of
     * {@link Expression} containing <code>null</code>
     */
    @Test
    public void testEvaluateNull() {
        List<Expression> expressions = new ArrayList<>();
        Integer size = random.nextInt(19) + 1, nullValue = random.nextInt(size);
        UnaryExpression expression;

        for (int i = 0; i < size; i++) {
            expression = UnaryExpressionTest.getNewValidUnaryExpression();
            if (i != nullValue) {
                expression.setValue(random.nextDouble());
            } else {
                expression.setValue(null);
            }
            expressions.add(expression);
        }

        multiExpression.setExpressions(expressions);

        Double testSum = sumOperator.evaluate(multiExpression, encounter);
        assertNull(
            "Evaluating sum failed. The returned value was not null although null was expected.",
            testSum);
    }

    /**
     * Test of {@link Sum#evaluate}<br> Invalid input: random {@link UnaryExpression} and random
     * {@link BinaryExpression}
     */
    @Test
    public void testEvaluateWrongExpression() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        Double testValue = sumOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating average failed. The returned value didn't match null although a unary expression was used as param.",
            testValue);

        expression = BinaryExpressionTest.getNewValidBinaryExpression();
        testValue = sumOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating average failed. The returned value didn't match null although a binary expression was used as param.",
            testValue);
    }

    /**
     * Test of {@link Sum#evaluate}<br> Invalid input: random {@link MultiExpression} with an empty
     * list of {@link Expression Expressions}.
     */
    @Test
    public void testEvaluateEmptyExpressions() {
        multiExpression.setExpressions(new ArrayList<>());
        Double testValue = sumOperator.evaluate(multiExpression, encounter);
        assertNull("Evaluating average failed. The returned value didn't match null.", testValue);
    }

    /**
     * Test of {@link Sum#evaluate}<br> Invalid input: random {@link MultiExpression} with
     * <code>null</code> as {@link Expression Expressions}.
     */
    @Test
    public void testEvaluateNullExpressions() {
        multiExpression.setExpressions(null);
        Double testValue = sumOperator.evaluate(multiExpression, encounter);
        assertNull("Evaluating average failed. The returned value didn't match null.", testValue);
    }

    /**
     * Test of {@link Sum#getFormula}<br> Valid input: random {@link MultiExpression} with random
     * number of {@link UnaryExpression UnaryExpressions}
     */
    @Test
    public void testGetFormula() {
        List<Expression> expressions = new ArrayList<>();
        Integer size = random.nextInt(19) + 1;
        Double value;
        UnaryExpression expression;
        StringBuilder sumBuffer = new StringBuilder();
        sumBuffer.append("Sum(");

        for (int i = 0; i < size; i++) {
            expression = UnaryExpressionTest.getNewValidUnaryExpression();
            value = random.nextDouble();
            expression.setValue(value);
            expressions.add(expression);
            sumBuffer.append(value);
            if (i < size - 1) {
                sumBuffer.append(", ");
            }
        }
        sumBuffer.append(")");
        String sum = sumBuffer.toString();
        multiExpression.setExpressions(expressions);

        String testSum = sumOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNotNull(
            "Get formula failed. The returned value was null although not null value was expected.",
            testSum);
        assertEquals("Get formula failed. The returned value didn't match the expected value.", sum,
            testSum);

        assertNull(
            "Evaluating average failed. The returned value didn't match null although a unary expression was used as param.",
            sumOperator.getFormula(UnaryExpressionTest.getNewValidUnaryExpression(), encounter,
                Helper.getRandomLocale()));
        assertNull(
            "Evaluating average failed. The returned value didn't match null although a binary expression was used as param.",
            sumOperator.getFormula(BinaryExpressionTest.getNewValidBinaryExpression(), encounter,
                Helper.getRandomLocale()));
    }

    /**
     * Test of {@link Sum#getFormula}<br> Invalid input: random {@link UnaryExpression} and random
     * {@link BinaryExpression}
     */
    @Test
    public void testGetFormulaWithWrongExpressions() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        String testFormula = sumOperator.getFormula(expression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Evaluating average failed. The returned value didn't match null although a unary expression was used as param.",
            testFormula);

        expression = BinaryExpressionTest.getNewValidBinaryExpression();
        testFormula = sumOperator.getFormula(expression, encounter, Helper.getRandomLocale());
        assertNull(
            "Evaluating average failed. The returned value didn't match null although a binary expression was used as param.",
            testFormula);
    }

    /**
     * Test of {@link Sum#getFormula}<br> Invalid input: random {@link MultiExpression} with an
     * empty list of {@link Expression Expressions}.
     */
    @Test
    public void testGetFormulaEmptyExpressions() {
        multiExpression.setExpressions(new ArrayList<>());
        String testValue = sumOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNull("Evaluating average failed. The returned value didn't match null.", testValue);
    }

    /**
     * Test of {@link Sum#getFormula}<br> Invalid input: random {@link MultiExpression} with
     * <code>null</code> as {@link Expression Expressions}.
     */
    @Test
    public void testGetFormulaNullExpressions() {
        multiExpression.setExpressions(null);
        String testValue = sumOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNull("Evaluating average failed. The returned value didn't match null.", testValue);
    }
}
