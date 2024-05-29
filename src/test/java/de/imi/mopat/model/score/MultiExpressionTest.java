package de.imi.mopat.model.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterTest;
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
public class MultiExpressionTest {

    private static final Random random = new Random();
    private MultiExpression multiExpression;
    private Encounter encounter;
    private Integer size;

    /**
     * Returns a valid new {@link MultiExpression}
     *
     * @return Returns a valid new {@link MultiExpression}
     */
    public static MultiExpression getNewValidMultiExpression() {
        return new MultiExpression();
    }

    /**
     * Returns a valid new {@link MultiExpression} with given {@link MultiOperator}
     *
     * @param operator {@link MultiOperator} of this {@link MultiExpression}
     * @return Returns a valid new {@link MultiExpression} with given {@link MultiOperator}
     */
    public static MultiExpression getNewValidMultiExpressionWithUnaries(
        MultiOperator multiOperator) {
        MultiExpression instance = new MultiExpression();
        instance.setOperator(multiOperator);

        Integer size = random.nextInt(19) + 2;
        List<Expression> expressions = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        }
        instance.setExpressions(expressions);
        return instance;
    }

    @Before
    public void setUp() {
        multiExpression = MultiExpressionTest.getNewValidMultiExpression();
        encounter = EncounterTest.getNewValidEncounter();
        size = random.nextInt(11) + 1;
    }

    /**
     * Test of {@link MultiExpression#evaluate} method.<br> Valid input: random
     * {@link MultiExpression} with {@link Sum} as {@link MultiOperator} and random number of
     * {@link UnaryExpression UnaryExpressions}
     */
    @Test
    public void testEvaluate() {
        Sum sum = SumTest.getNewValidSum();
        multiExpression.setOperator(sum);
        Double value = 0.0;

        List<Expression> expressions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UnaryExpression unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression();
            Double currentValue = random.nextDouble();
            value += currentValue;
            unaryExpression.setValue(currentValue);
            expressions.add(unaryExpression);
        }
        multiExpression.setExpressions(expressions);

        Double testValue = (Double) multiExpression.evaluate(encounter);
        assertNotNull(
            "Evaluating MultiExpression failed. The returned value was null although not null was expected.",
            testValue);
        assertEquals(
            "Evaluating MultiExpression failed. The returned value didn't match the expected value.",
            value, testValue);
    }

    /**
     * Test of {@link MultiExpression#getFormula} method.<br> Valid input: random
     * {@link MultiExpression} with {@link Sum} as {@link MultiOperator} and random number of
     * {@link UnaryExpression UnaryExpressions}
     */
    @Test
    public void testGetFormula() {
        Sum sum = SumTest.getNewValidSum();
        multiExpression.setOperator(sum);
        StringBuffer formula = new StringBuffer();
        formula.append("Sum(");

        List<Expression> expressions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UnaryExpression unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression();
            Double currentValue = random.nextDouble();
            unaryExpression.setValue(currentValue);
            expressions.add(unaryExpression);

            formula.append(currentValue);
            if (i < size - 1) {
                formula.append(", ");
            }
        }
        formula.append(")");
        multiExpression.setExpressions(expressions);

        String testFormula = multiExpression.getFormula(encounter, Helper.getRandomLocale());
        assertNotNull(
            "Evaluating MultiExpression failed. The returned value was null although not null was expected.",
            testFormula);
        assertEquals(
            "Evaluating MultiExpression failed. The returned value didn't match the expected value.",
            formula.toString(), testFormula);
    }

    /**
     * Test of {@link MultiExpression#setOperator} and {@link MultiExpression#getOperator}
     * method.<br> Valid input: random {@link MultiOperator}
     */
    @Test
    public void testSetAndGetOperator() {
        MultiOperator operator = MultiOperatorTest.getNewValidMultiOperator();
        multiExpression.setOperator(operator);
        Operator testOperator = multiExpression.getOperator();
        assertNotNull(
            "Setting operator failed. The given operator was null although not null value was expect.",
            testOperator);
        assertEquals("Setting operator failed. The given operator didn't match the expected value.",
            operator, testOperator);
    }

    /**
     * Test of {@link MultiExpression#setExpressions} and {@link MultiExpression#getExpressions}
     * method.<br> Valid input: list of random {@link Expression Expressions}
     */
    @Test
    public void testSetGetExpressions() {
        Integer size = random.nextInt(19) + 1;
        List<Expression> expressions = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            expressions.add(ExpressionTest.getNewValidExpression());
        }

        multiExpression.setExpressions(expressions);
        List<Expression> testExpressions = multiExpression.getExpressions();
        assertNotNull(
            "Setting expressions failed. The returned value was null although not null value was expected.",
            testExpressions);
        assertEquals(
            "Setting expressions failed. The returned value didn't match the expected value.",
            expressions, testExpressions);
    }

    /**
     * Test of {@link MultiExpression#toExpressionDTO()} method.<br> Valid input: random
     * {@link MultiExpression} with random number of random
     * {@link UnaryExpression UnaryExpressions}
     */
    @Test
    public void testToExpressionDTO() {
        Integer size = random.nextInt(19) + 1;
        MultiOperator operator = Mockito.spy(MultiOperatorTest.getNewValidMultiOperator());
        Mockito.when(operator.getId()).thenReturn(Math.abs(random.nextLong()));
        multiExpression.setOperator(operator);

        List<Expression> expressions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UnaryExpression unaryExpression = Mockito.spy(
                UnaryExpressionTest.getNewValidUnaryExpression());
            UnaryOperator unaryOperator = Mockito.spy(ValueOperatorTest.getNewValidValueOperator());
            unaryExpression.setOperator(unaryOperator);
            Mockito.when(unaryExpression.getId()).thenReturn(Math.abs(random.nextLong()));
            Mockito.when(unaryOperator.getId()).thenReturn(Math.abs(random.nextLong()));
            expressions.add(unaryExpression);
        }
        multiExpression.setExpressions(expressions);

        ExpressionDTO expressionDTO = multiExpression.toExpressionDTO();
        assertNotNull(
            "ToExpressionDTO failed. The returned value was null although not null value was expected.",
            expressionDTO);
        assertEquals("ToExpressionDTO failed. The returned id didn't match the expected value.",
            operator.getId(), expressionDTO.getOperatorId());
        assertEquals("ToExpressionDTO failed. The returned list didn't match the expected value.",
            expressions.size(), expressionDTO.getExpressions().size());
    }
}
