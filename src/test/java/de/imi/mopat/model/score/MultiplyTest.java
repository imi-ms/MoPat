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
public class MultiplyTest {

    private static final Random random = new Random();
    private Multiply multiplyOperator;
    private BinaryExpression binaryExpression;
    private Encounter encounter;

    /**
     * Returns a new valid instance of {@link Divide}.
     *
     * @return New valid divide instance
     */
    public static Multiply getNewValidMultiply() {
        return new Multiply();
    }

    @Before
    public void setUp() {
        multiplyOperator = MultiplyTest.getNewValidMultiply();
        binaryExpression = BinaryExpressionTest.getNewValidBinaryExpressionWithUnaries(
            multiplyOperator);
        encounter = EncounterTest.getNewValidEncounter();
    }

    /**
     * Test of {@link Multiply#evaluate}<br> Invalid input: value of first or second
     * {@link UnaryExpression}
     * <code>null</code><br>
     * Valid input: random {@link BinaryExpression} with two
     * {@link UnaryExpression UnaryExpressions} with random values
     */
    @Test
    public void testEvaluate() {
        Iterator<Expression> iterator = binaryExpression.getExpressions().iterator();
        UnaryExpression expression1 = (UnaryExpression) iterator.next();
        UnaryExpression expression2 = (UnaryExpression) iterator.next();

        Double value =
            (Double) expression1.evaluate(encounter) * (Double) expression2.evaluate(encounter);
        Double testValue = multiplyOperator.evaluate(binaryExpression, encounter);

        assertNotNull(
            "Evaluating divide failed. The returned value was null although not null value was expected.",
            testValue);
        assertEquals("Evaluating divide failed. The returned value didn't match the expected value",
            value, testValue);

        // BinaryOperator with first expression value null
        BinaryExpression testBinaryExpression = new BinaryExpression();
        assertNull(
            "Evaluating a BinaryExpression with no Expressions and the less operator was not null",
            multiplyOperator.evaluate(testBinaryExpression, encounter));
        testBinaryExpression.setOperator(multiplyOperator);
        UnaryExpression unaryExpression1 = UnaryExpressionTest.getNewValidUnaryExpression();
        UnaryExpression unaryExpression2 = UnaryExpressionTest.getNewValidUnaryExpression();
        unaryExpression1.setOperator(ValueOperatorTest.getNewValidValueOperator());
        unaryExpression2.setOperator(ValueOperatorTest.getNewValidValueOperator());
        unaryExpression1.setValue(null);
        unaryExpression2.setValue(Math.abs(random.nextDouble()));
        List<Expression> expressions = new ArrayList<>();
        expressions.add(unaryExpression1);
        expressions.add(unaryExpression2);
        testBinaryExpression.setExpressions(expressions);
        assertNull("Evaluating a BinaryExpression with first expression value null was not null",
            multiplyOperator.evaluate(testBinaryExpression, encounter));

        // BinaryOperator with second expression value null
        unaryExpression1.setValue(Math.abs(random.nextDouble()));
        unaryExpression2.setValue(null);
        expressions = new ArrayList<>();
        expressions.add(unaryExpression1);
        expressions.add(unaryExpression2);
        testBinaryExpression.setExpressions(expressions);
        assertNull("Evaluating a BinaryExpression with second expression value null was not null",
            multiplyOperator.evaluate(testBinaryExpression, encounter));
    }

    /**
     * Test of {@link Multiply#evaluate}<br> Invalid input: <code>null</code>, random
     * {@link UnaryExpression} and random {@link MultiExpression}
     */
    @Test
    public void testEvaluateWithWrongExpressions() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        Double testValue = multiplyOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating divide failed. The returned value didn't match null although unary expression was used as param",
            testValue);

        expression = MultiExpressionTest.getNewValidMultiExpression();
        testValue = multiplyOperator.evaluate(expression, encounter);
        assertNull(
            "Evaluating divide failed. The returned value didn't match null although multi expression was used as param",
            testValue);

        testValue = multiplyOperator.evaluate(null, encounter);
        assertNull(
            "Evaluating divide failed. The returned value didn't match null although null was used as param",
            testValue);
    }

    /**
     * Test of {@link Multiply#evaluate}<br> Invalid input: random {@link BinaryExpression} with
     * more or less than two {@link Expression Expressions}
     */
    @Test
    public void testEvaluateExpressionsOutOfBounds() {
        List<Expression> expressions = new ArrayList<>();

        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        binaryExpression.setExpressions(expressions);

        for (int i = 0; i < random.nextInt(11) + 1; i++) {
            binaryExpression.getExpressions().add(UnaryExpressionTest.getNewValidUnaryExpression());
        }

        Double testValue = multiplyOperator.evaluate(binaryExpression, encounter);
        assertNull("Evaluating divide failed. The returned value didn't match null", testValue);

        expressions.clear();
        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        binaryExpression.setExpressions(expressions);

        if (random.nextBoolean()) {
            binaryExpression.getExpressions().remove(random.nextInt(2));
        } else {
            binaryExpression.getExpressions().clear();
        }

        testValue = multiplyOperator.evaluate(binaryExpression, encounter);
        assertNull(
            "Evaluating divide failed. The returned value didn't match null although adhering expressions were less than 2",
            testValue);
    }

    /**
     * Test of {@link Multiply#getFormula}<br> Valid input: random {@link BinaryExpression} with two
     * {@link UnaryExpression UnaryExpressions} with random values
     */
    @Test
    public void testGetFormula() {
        Iterator<Expression> iterator = binaryExpression.getExpressions().iterator();
        UnaryExpression expression1 = (UnaryExpression) iterator.next();
        UnaryExpression expression2 = (UnaryExpression) iterator.next();

        Double value1 = (Double) expression1.evaluate(encounter);
        Double value2 = (Double) expression2.evaluate(encounter);
        String formula = "(" + value1.toString() + " * " + value2.toString() + ")";
        String testFormula = binaryExpression.getFormula(encounter, Helper.getRandomLocale());

        assertNotNull(
            "Getting formula failed. The returned formula was null although not null was expected.",
            testFormula);
        assertEquals(
            "Getting formula failed. The returned formula didn't match the expected value.",
            formula, testFormula);
    }

    /**
     * Test of {@link Multiply#getFormula}<br> Invalid input: <code>null</code>, random
     * {@link UnaryExpression} and random {@link MultiExpression}
     */
    @Test
    public void testGetFormulaWithWrongExpressions() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        String testFormula = multiplyOperator.getFormula(expression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Evaluating divide failed. The returned value didn't match null although unary expression was used as param",
            testFormula);

        expression = MultiExpressionTest.getNewValidMultiExpression();
        testFormula = multiplyOperator.getFormula(expression, encounter, Helper.getRandomLocale());
        assertNull(
            "Evaluating divide failed. The returned value didn't match null although multi expression was used as param",
            testFormula);

        testFormula = multiplyOperator.getFormula(null, encounter, Helper.getRandomLocale());
        assertNull(
            "Evaluating divide failed. The returned value didn't match null although null was used as param",
            testFormula);
    }

    /**
     * Test of {@link Multiply#getFormula}<br> Invalid input: random {@link BinaryExpression} with
     * more or less than 2 {@link Expression Expressions}
     */
    @Test
    public void testGetFormulaExpressionsOutOfBounds() {
        List<Expression> expressions = new ArrayList<>();

        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        binaryExpression.setExpressions(expressions);

        for (int i = 0; i < random.nextInt(11) + 1; i++) {
            binaryExpression.getExpressions().add(UnaryExpressionTest.getNewValidUnaryExpression());
        }

        String testValue = multiplyOperator.getFormula(binaryExpression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Evaluating divide failed. The returned value didn't match null although adhering expressions were more than 2",
            testValue);

        expressions.clear();
        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        binaryExpression.setExpressions(expressions);

        if (random.nextBoolean()) {
            binaryExpression.getExpressions().remove(random.nextInt(2));
        } else {
            binaryExpression.getExpressions().clear();
        }

        testValue = multiplyOperator.getFormula(binaryExpression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Evaluating divide failed. The returned value didn't match null  although adhering expressions were less than 2",
            testValue);
    }
}
