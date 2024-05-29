package de.imi.mopat.model.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterTest;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.dto.ExpressionDTO;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class BinaryExpressionTest {

    private static final Random random = new Random();
    private BinaryExpression binaryExpression;
    private Encounter encounter;

    /**
     * Returns a valid new {@link BinaryExpression}
     *
     * @return Returns a valid new {@link BinaryExpression}
     */
    public static BinaryExpression getNewValidBinaryExpression() {
        BinaryExpression instance = new BinaryExpression();
        return instance;
    }

    /**
     * Returns a valid new {@link BinaryExpression} with given {@link BinaryOperator}
     *
     * @param operator {@link BinaryOperator} of this {@link BinaryExpression}
     * @return Returns a valid new {@link BinaryExpression} with given {@link BinaryOperator}
     */
    public static BinaryExpression getNewValidBinaryExpressionWithUnaries(BinaryOperator operator) {
        BinaryExpression instance = new BinaryExpression();
        instance.setOperator(operator);
        UnaryExpression unaryExpression1 = UnaryExpressionTest.getNewValidUnaryExpression();
        UnaryExpression unaryExpression2 = UnaryExpressionTest.getNewValidUnaryExpression();
        unaryExpression1.setOperator(ValueOperatorTest.getNewValidValueOperator());
        unaryExpression2.setOperator(ValueOperatorTest.getNewValidValueOperator());
        unaryExpression1.setValue(Math.abs(random.nextDouble()));
        unaryExpression2.setValue(Math.abs(random.nextDouble()));
        List<Expression> expressions = new ArrayList<>();
        expressions.add(unaryExpression1);
        expressions.add(unaryExpression2);
        instance.setExpressions(expressions);
        return instance;
    }

    @Before
    public void setUp() {
        binaryExpression = BinaryExpressionTest.getNewValidBinaryExpression();
        encounter = EncounterTest.getNewValidEncounter();
    }

    /**
     * Test of {@link BinaryExpression#evaluate}<br> Valid input: {@link Plus} as
     * {@link BinaryOperator} and two {@link UnaryExpression UnaryExpressions} with random double
     * values
     */
    @Test
    public void testEvaluate() {
        Plus plusOperator = PlusTest.getNewValidPlus();
        binaryExpression.setOperator(plusOperator);
        List<Expression> expressions = new ArrayList<>();
        UnaryExpression unaryExpression1 = UnaryExpressionTest.getNewValidUnaryExpression();
        UnaryExpression unaryExpression2 = UnaryExpressionTest.getNewValidUnaryExpression();
        Double value1 = random.nextDouble();
        Double value2 = random.nextDouble();
        unaryExpression1.setValue(value1);
        unaryExpression2.setValue(value2);
        expressions.add(unaryExpression1);
        expressions.add(unaryExpression2);
        binaryExpression.setExpressions(expressions);
        Double value = value1 + value2;
        Double testValue = (Double) binaryExpression.evaluate(encounter);
        assertNotNull(
            "Evaluating BinaryExpression failed. The returned value was null although not null was expected.",
            testValue);
        assertEquals(
            "Evaluating BinaryExpression failed. The returned value didn't match the expected value.",
            value, testValue);
    }

    /**
     * Test of {@link BinaryExpression#getFormula}<br> Valid input: {@link Plus} as
     * {@link BinaryOperator} and two {@link UnaryExpression UnaryExpressions} with random double
     * values
     */
    @Test
    public void testGetFormula() {
        Minus minusOperator = MinusTest.getNewValidMinus();
        binaryExpression.setOperator(minusOperator);
        List<Expression> expressions = new ArrayList<>();
        UnaryExpression unaryExpression1 = UnaryExpressionTest.getNewValidUnaryExpression();
        UnaryExpression unaryExpression2 = UnaryExpressionTest.getNewValidUnaryExpression();
        Double value1 = random.nextDouble();
        Double value2 = random.nextDouble();
        unaryExpression1.setValue(value1);
        unaryExpression2.setValue(value2);
        expressions.add(unaryExpression1);
        expressions.add(unaryExpression2);
        binaryExpression.setExpressions(expressions);
        String formula = "(" + value1 + " - " + value2 + ")";
        String testFormula = binaryExpression.getFormula(encounter, Helper.getRandomLocale());
        assertNotNull(
            "Getting formula of BinaryExpression failed. The returned value was null although not null was expected.",
            testFormula);
        assertEquals(
            "Getting formula of BinaryExpression failed. The returned value didn't match the expected value.",
            formula, testFormula);
    }

    /**
     * Test of {@link BinaryExpression#setOperator} and {@link BinaryExpression#getOperator}
     * method.<br> Valid input: random {@link BinaryOperator}
     */
    @Test
    public void testSetAndGetOperator() {
        BinaryOperator operator = BinaryOperatorTest.getNewValidBinaryOperator();
        binaryExpression.setOperator(operator);
        Operator testOperator = binaryExpression.getOperator();

        assertNotNull(
            "Setting operator failed. The returned value was null although not-null was expected.",
            testOperator);
        assertEquals("Setting operator failed. The returned value didn't match the expected value.",
            operator, testOperator);
    }

    /**
     * Test of {@link BinaryExpression#setExpressions} and {@link BinaryExpression#getExpressions}
     * methods.<br> Invalid input: <code>null</code>, less than two {@link Expression Expressions},
     * more than two {@link Expression Expressions}<br> Valid input: list with two random
     * {@link Expression Expressions}
     */
    @Test
    public void testSetAndGetExpressions() {
        List<Expression> expressions = null;
        Throwable e = null;
        try {
            binaryExpression.setExpressions(expressions);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Expressions", e instanceof AssertionError);

        int greaterSize = random.nextInt(10) + 3;
        expressions = new ArrayList<>();
        for (int i = 0; i < greaterSize; i++) {
            expressions.add(ExpressionTest.getNewValidExpression());
        }
        e = null;
        try {
            binaryExpression.setExpressions(expressions);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set more than two Expressions", e instanceof AssertionError);

        int smallerSize = random.nextInt(2);
        expressions = new ArrayList<>();
        for (int i = 0; i < smallerSize; i++) {
            expressions.add(ExpressionTest.getNewValidExpression());
        }
        e = null;
        try {
            binaryExpression.setExpressions(expressions);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set less than two Expressions", e instanceof AssertionError);

        expressions = new ArrayList<>();
        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        binaryExpression.setExpressions(expressions);
        List<Expression> testExpressions = binaryExpression.getExpressions();

        assertNotNull(
            "Setting expressions failed. The returned value was null although not-null was expected.",
            testExpressions);
        assertEquals(
            "Setting expressions failed. The returned value didn't match the expected value.",
            expressions, testExpressions);
    }

    /**
     * Test of {@link BinaryExpression#toExpressionDTO()} method.<br> Valid input: random
     * {@link BinaryExpression} with random {@link BinaryOperator} and two
     * {@link UnaryExpression UnaryExpressions}
     */
    @Test
    public void testToExpressionDTO() {
        BinaryOperator operator = Mockito.spy(BinaryOperatorTest.getNewValidBinaryOperator());
        Mockito.when(operator.getId()).thenReturn(Math.abs(random.nextLong()));
        binaryExpression.setOperator(operator);
        List<Expression> expressions = new ArrayList<>();

        UnaryExpression unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression();
        UnaryOperator unaryOperator = Mockito.spy(UnaryOperatorTest.getNewValidUnaryOperator());
        Mockito.when(unaryOperator.getId()).thenReturn(Math.abs(random.nextLong()));
        unaryExpression.setOperator(unaryOperator);

        if (unaryOperator instanceof ValueOperator) {
            unaryExpression.setValue(random.nextDouble());
        }
        if (unaryOperator instanceof ValueOfQuestionOperator) {
            unaryExpression.setQuestion(QuestionTest.getNewValidQuestion());
        }

        expressions.add(unaryExpression);

        unaryExpression = Mockito.spy(UnaryExpressionTest.getNewValidUnaryExpression());
        unaryOperator = Mockito.spy(UnaryOperatorTest.getNewValidUnaryOperator());
        Mockito.when(unaryOperator.getId()).thenReturn(Math.abs(random.nextLong()));
        unaryExpression.setOperator(unaryOperator);

        if (unaryOperator instanceof ValueOperator) {
            unaryExpression.setValue(random.nextDouble());
        }
        if (unaryOperator instanceof ValueOfQuestionOperator) {
            unaryExpression.setQuestion(QuestionTest.getNewValidQuestion());
        }

        expressions.add(unaryExpression);

        binaryExpression.setExpressions(expressions);
        ExpressionDTO expressionDTO = binaryExpression.toExpressionDTO();
        assertEquals(
            "ToExpressionDTO failed. The returned operator didn't match the expected value.",
            operator.getId(), expressionDTO.getOperatorId());
        assertEquals(
            "ToExpressionDTO failed. The returned expressions didn't match the expected value.",
            expressions.size(), expressionDTO.getExpressions().size());
    }
}
