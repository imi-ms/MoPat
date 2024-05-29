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
public class CounterTest {

    private static final Random random = new Random();
    private Counter counterOperator;
    private MultiExpression multiExpression;
    private Encounter encounter;

    /**
     * Returns a new valid instance of {@link Counter}.
     *
     * @return New valid Counter instance
     */
    public static Counter getNewValidCounter() {
        Counter instance = new Counter();

        return instance;
    }

    @Before
    public void setUp() {
        counterOperator = CounterTest.getNewValidCounter();
        multiExpression = MultiExpressionTest.getNewValidMultiExpressionWithUnaries(
            counterOperator);
        encounter = EncounterTest.getNewValidEncounter();
    }

    /**
     * Test of {@link Counter#evaluate}<br> Valid input: random {@link MultiExpression} with random
     * number of {@link BinaryExpression BinaryExpressions}
     */
    @Test
    public void testEvaluate() {
        Integer size = random.nextInt(23) + 1;
        Double counter = 0.0;
        List<Expression> expressions = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            List<Expression> unaryExpressions = new ArrayList<>();
            BinaryExpression binaryExpression = BinaryExpressionTest.getNewValidBinaryExpression();
            BinaryOperatorBoolean binaryOperatorBoolean = BinaryOperatorBooleanTest.getNewValidBinaryOperatorBoolean();
            binaryExpression.setOperator(binaryOperatorBoolean);
            UnaryExpression unaryExpression1 = UnaryExpressionTest.getNewValidUnaryExpression();
            UnaryExpression unaryExpression2 = UnaryExpressionTest.getNewValidUnaryExpression();
            unaryExpression1.setValue(random.nextDouble());
            unaryExpression2.setValue(random.nextDouble());

            if (binaryOperatorBoolean instanceof Different && !unaryExpression1.getValue()
                .equals(unaryExpression2.getValue())
                || binaryOperatorBoolean instanceof Equals && unaryExpression1.getValue()
                .equals(unaryExpression2.getValue()) || binaryOperatorBoolean instanceof Greater
                && unaryExpression1.getValue() > unaryExpression2.getValue()
                || binaryOperatorBoolean instanceof GreaterEquals
                && unaryExpression1.getValue() >= unaryExpression2.getValue()
                || binaryOperatorBoolean instanceof Less
                && unaryExpression1.getValue() < unaryExpression2.getValue()
                || binaryOperatorBoolean instanceof LessEquals
                && unaryExpression1.getValue() <= unaryExpression2.getValue()) {
                counter++;
            }

            unaryExpressions.add(unaryExpression1);
            unaryExpressions.add(unaryExpression2);
            binaryExpression.setExpressions(unaryExpressions);
            expressions.add(binaryExpression);
        }

        multiExpression.setExpressions(expressions);

        Double testCounter = counterOperator.evaluate(multiExpression, encounter);
        assertNotNull(
            "Evaluating counter failed. The returned value was null although not null was expected.",
            testCounter);
        assertEquals(
            "Evaluating counter failed. The returned value didn't match the expected value.",
            counter, testCounter);
    }

    /**
     * Test of {@link Counter#evaluate}<br> Invalid input: random {@link MultiExpression} with
     * random number of {@link BinaryExpression BinaryExpressions} with at least one instance of
     * {@link Expression} containing <code>null</code>
     */
    @Test
    public void testEvaluateNull() {
        Integer size = random.nextInt(23) + 1;
        Integer nullPosition = random.nextInt(size);
        List<Expression> expressions = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            List<Expression> unaryExpressions = new ArrayList<>();
            BinaryExpression binaryExpression = BinaryExpressionTest.getNewValidBinaryExpression();
            BinaryOperatorBoolean binaryOperatorBoolean = BinaryOperatorBooleanTest.getNewValidBinaryOperatorBoolean();
            binaryExpression.setOperator(binaryOperatorBoolean);
            UnaryExpression unaryExpression1 = UnaryExpressionTest.getNewValidUnaryExpression();
            UnaryExpression unaryExpression2 = UnaryExpressionTest.getNewValidUnaryExpression();

            if (i == nullPosition) {
                unaryExpression1.setValue(null);
                unaryExpression2.setValue(null);
            } else {
                unaryExpression1.setValue(random.nextDouble());
                unaryExpression2.setValue(random.nextDouble());
            }

            unaryExpressions.add(unaryExpression1);
            unaryExpressions.add(unaryExpression2);
            binaryExpression.setExpressions(unaryExpressions);
            expressions.add(binaryExpression);
        }

        multiExpression.setExpressions(expressions);

        Double testValue = counterOperator.evaluate(multiExpression, encounter);
        assertNull(
            "Evaluating counter failed. The returned value was not null although null was expected.",
            testValue);
    }

    /**
     * Test of {@link Counter#evaluate}<br> Invalid input: random {@link UnaryExpression} and random
     * {@link BinaryExpression}
     */
    @Test
    public void testEvaluateWrongExpressions() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        Double testValue = counterOperator.evaluate(expression, encounter);
        assertNull("Evaluating average failed. The returned value didn't match null", testValue);

        expression = BinaryExpressionTest.getNewValidBinaryExpression();
        testValue = counterOperator.evaluate(expression, encounter);
        assertNull("Evaluating average failed. The returned value didn't match null", testValue);
    }

    /**
     * Test of {@link Counter#evaluate}<br> Invalid input: random {@link MultiExpression} with an
     * empty list of {@link Expression Expressions}.
     */
    @Test
    public void testEvaluateEmptyExpressions() {
        multiExpression.setExpressions(new ArrayList<>());
        Double testValue = counterOperator.evaluate(multiExpression, encounter);
        assertNull("Evaluating average failed. The returned value didn't match null", testValue);
    }

    /**
     * Test of {@link Counter#evaluate}<br> Invalid input: random {@link MultiExpression} with
     * <code>null</code> as {@link Expression Expressions}.
     */
    @Test
    public void testEvaluateNullExpressions() {
        multiExpression.setExpressions(null);
        Double testValue = counterOperator.evaluate(multiExpression, encounter);
        assertNull("Evaluating average failed. The returned value didn't match null", testValue);
    }

    /**
     * Test of {@link Counter#getFormula}<br> Valid input: random {@link MultiExpression} with
     * random number of {@link BinaryExpression BinaryExpressions}
     */
    @Test
    public void testGetFormula() {
        Integer size = random.nextInt(23) + 1;
        String formula = "Count of(";
        List<Expression> expressions = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            List<Expression> unaryExpressions = new ArrayList<>();
            BinaryExpression binaryExpression = BinaryExpressionTest.getNewValidBinaryExpression();
            BinaryOperatorBoolean binaryOperatorBoolean = BinaryOperatorBooleanTest.getNewValidBinaryOperatorBoolean();
            binaryExpression.setOperator(binaryOperatorBoolean);
            UnaryExpression unaryExpression1 = UnaryExpressionTest.getNewValidUnaryExpression();
            UnaryExpression unaryExpression2 = UnaryExpressionTest.getNewValidUnaryExpression();
            unaryExpression1.setValue(random.nextDouble());
            unaryExpression2.setValue(random.nextDouble());

            if (binaryOperatorBoolean instanceof Different) {
                formula = formula + "(" + unaryExpression1.getValue() + " != "
                    + unaryExpression2.getValue() + ")";
            }
            if (binaryOperatorBoolean instanceof Equals) {
                formula = formula + "(" + unaryExpression1.getValue() + " == "
                    + unaryExpression2.getValue() + ")";
            }
            if (binaryOperatorBoolean instanceof Greater) {
                formula = formula + "(" + unaryExpression1.getValue() + " > "
                    + unaryExpression2.getValue() + ")";
            }
            if (binaryOperatorBoolean instanceof GreaterEquals) {
                formula = formula + "(" + unaryExpression1.getValue() + " >= "
                    + unaryExpression2.getValue() + ")";
            }
            if (binaryOperatorBoolean instanceof Less) {
                formula = formula + "(" + unaryExpression1.getValue() + " < "
                    + unaryExpression2.getValue() + ")";
            }
            if (binaryOperatorBoolean instanceof LessEquals) {
                formula = formula + "(" + unaryExpression1.getValue() + " <= "
                    + unaryExpression2.getValue() + ")";
            }
            if (i < size - 1) {
                formula = formula + ", ";
            }
            unaryExpressions.add(unaryExpression1);
            unaryExpressions.add(unaryExpression2);
            binaryExpression.setExpressions(unaryExpressions);
            expressions.add(binaryExpression);
        }

        formula = formula + ")";

        multiExpression.setExpressions(expressions);

        String testFormula = counterOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNotNull(
            "Get formula failed. The returned value was null although not null value was expected.",
            testFormula);
        assertEquals("Get formula failed. The returned value didn't match the expected value.",
            formula, testFormula);
    }

    /**
     * Test of {@link Counter#getFormula}<br> Invalid input: random {@link UnaryExpression} and
     * random {@link BinaryExpression}
     */
    @Test
    public void testGetFormulaWithWrongExpressions() {
        Expression expression = UnaryExpressionTest.getNewValidUnaryExpression();
        String testFormula = counterOperator.getFormula(expression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Evaluating average failed. The returned value didn't match null although a unary expression was used as param.",
            testFormula);

        expression = BinaryExpressionTest.getNewValidBinaryExpression();
        testFormula = counterOperator.getFormula(expression, encounter, Helper.getRandomLocale());
        assertNull(
            "Evaluating average failed. The returned value didn't match null although a binary expression was used as param.",
            testFormula);
    }

    /**
     * Test of {@link Counter#getFormula}<br> Invalid input: random {@link MultiExpression} with an
     * empty list of {@link Expression Expressions}.
     */
    @Test
    public void testGetFormulaEmptyExpressions() {
        multiExpression.setExpressions(new ArrayList<>());
        String testFormula = counterOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Evaluating average failed. The returned value didn't match null although it was expected",
            testFormula);
    }

    /**
     * Test of {@link Counter#getFormula}<br> Invalid input: random {@link MultiExpression} with
     * <code>null</code> as {@link Expression Expressions}.
     */
    @Test
    public void testGetFormulaNullExpressions() {
        multiExpression.setExpressions(null);
        String testFormula = counterOperator.getFormula(multiExpression, encounter,
            Helper.getRandomLocale());
        assertNull(
            "Evaluating average failed. The returned value didn't match null although it was expected",
            testFormula);
    }
}
