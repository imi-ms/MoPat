package de.imi.mopat.model.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterTest;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.dto.ExpressionDTO;
import de.imi.mopat.utils.Helper;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class UnaryExpressionTest {

    private static final Random random = new Random();
    private UnaryExpression unaryExpression;
    private Encounter encounter;

    /**
     * Return a new valid instance of {@link UnaryExpression}
     *
     * @return New valid UnaryExpression instance.
     */
    public static UnaryExpression getNewValidUnaryExpression() {
        UnaryExpression instance = new UnaryExpression();
        instance.setOperator(ValueOperatorTest.getNewValidValueOperator());
        instance.setValue(random.nextDouble());
        return instance;
    }

    /**
     * Return a new valid instance of {@link UnaryExpression} with given {@link Score} and
     * {@link ValueOfScoreOperator}
     *
     * @param score    {@link Score} of this {@link UnaryExpression}
     * @param operator {@link ValueOfScoreOperator} of this {@link UnaryExpression}
     * @return New valid UnaryExpression instance.
     */
    public static UnaryExpression getNewValidUnaryExpression(Score score,
        ValueOfScoreOperator operator) {
        UnaryExpression instance = new UnaryExpression();
        instance.setOperator(operator);
        instance.setScore(score);
        return instance;
    }

    /**
     * Return a new valid instance of {@link UnaryExpression} with the given {@link Question}
     *
     * @param question {@link Question} of this {@link UnaryExpression}
     * @return New valid UnaryExpression instance.
     */
    public static UnaryExpression getNewValidUnaryExpression(Question question) {
        UnaryExpression instance = new UnaryExpression();
        instance.setQuestion(question);
        return instance;
    }

    @Before
    public void setUp() {
        unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression();
        encounter = EncounterTest.getNewValidEncounter();
    }

    /**
     * Test of {@link UnaryExpression#evaluate}<br> Valid input: random {@link UnaryExpression}
     * witzh random value and {@link ValueOperator}
     */
    @Test
    public void testEvaluate() {
        ValueOperator valueOperator = ValueOperatorTest.getNewValidValueOperator();
        Double value = random.nextDouble();
        unaryExpression.setValue(value);
        unaryExpression.setOperator(valueOperator);
        Double testValue = (Double) unaryExpression.evaluate(encounter);
        assertNotNull(
            "Evaluating UnaryExpression failed. The returned value was null although not null was expected.",
            testValue);
        assertEquals(
            "Evaluating UnaryExpression failed. The retunred value didn't match the expected value.",
            value, testValue);
    }

    /**
     * Test of {@link UnaryExpression#getFormula}<br> Valid input: random {@link UnaryExpression}
     * witzh random value and {@link ValueOperator}
     */
    @Test
    public void testGetFormula() {
        ValueOperator valueOperator = ValueOperatorTest.getNewValidValueOperator();
        Double value = random.nextDouble();
        unaryExpression.setValue(value);
        unaryExpression.setOperator(valueOperator);
        String formula = value.toString();
        String testFormula = unaryExpression.getFormula(encounter, Helper.getRandomLocale());
        assertNotNull(
            "Evaluating UnaryExpression failed. The returned value was null although not null was expected.",
            testFormula);
        assertEquals(
            "Evaluating UnaryExpression failed. The retunred value didn't match the expected value.",
            formula, testFormula);
    }

    /**
     * Test of {@link UnaryExpression#setOperator} and {@link UnaryExpression#getOperator}
     * methods.<br> Valid input: random {@link UnaryOperator}
     */
    @Test
    public void testSetGetOperator() {
        UnaryOperator unaryOperator = UnaryOperatorTest.getNewValidUnaryOperator();
        unaryExpression.setOperator(unaryOperator);
        Operator testOperator = unaryExpression.getOperator();
        assertNotNull(
            "Setting the operator failed. The returned value was null although not-null was expected",
            testOperator);
        assertEquals(
            "Setting the operator failed. The returned value didn't match the expected value.",
            unaryOperator, testOperator);
    }

    /**
     * Test of {@link UnaryExpression#setValue} and {@link UnaryExpression#getValue} methods.<br>
     * Valid input: random {@link UnaryExpression} with random value
     */
    @Test
    public void testSetGetValue() {
        Double value = random.nextDouble();
        unaryExpression.setValue(value);
        Double testValue = unaryExpression.getValue();
        assertNotNull(
            "Setting the value failed. The returned value was null although not-null was expected",
            testValue);
        assertEquals(
            "Setting the value failed. The returned value didn't match the expected value.", value,
            testValue);
    }

    /**
     * Test of {@link UnaryExpression#setQuestion} and {@link UnaryExpression#getQuestion}
     * methods.<br> Valid input: random {@link Question}
     */
    @Test
    public void testSetGetQuestion() {
        Question question = QuestionTest.getNewValidQuestion();
        unaryExpression.setQuestion(question);
        Question testQuestion = unaryExpression.getQuestion();
        assertNotNull(
            "Setting the question failed. The returned value was null although not-null was expected.",
            testQuestion);
        assertEquals(
            "Setting the question failed. The returned value didn't match the expected value.",
            question, testQuestion);
    }

    /**
     * Test of {@link UnaryExpression#toExpressionDTO()} method.<br> Valid input: random
     * {@link ValueOfQuestionOperator}, random {@link ValueOfScoreOperator} and random
     * {@link ValueOperator}
     */
    @Test
    public void testToExpressionDTO() {
        // Value of Question Operator
        UnaryOperator unaryOperator = Mockito.spy(
            ValueOfQuestionOperatorTest.getNewValidValueOfQuestionOperator());
        Mockito.when(unaryOperator.getId()).thenReturn(Math.abs(random.nextLong()));
        unaryExpression.setOperator(unaryOperator);
        ExpressionDTO expressionDTO = unaryExpression.toExpressionDTO();

        assertEquals("The getting OperatorId was not the expected one", unaryOperator.getId(),
            expressionDTO.getOperatorId());
        assertNull("The QuestionId was not null although there was no associated Question",
            expressionDTO.getQuestionId());

        Question question = Mockito.spy(QuestionTest.getNewValidQuestion());
        Mockito.when(question.getId()).thenReturn(Math.abs(random.nextLong()));
        unaryExpression.setQuestion(question);
        expressionDTO = unaryExpression.toExpressionDTO();

        assertEquals("The getting OperatorId was not the expected one", unaryOperator.getId(),
            expressionDTO.getOperatorId());
        assertEquals("The getting QuestionId was not the expected one", question.getId(),
            expressionDTO.getQuestionId());

        // Value of Score Operator
        unaryOperator = Mockito.spy(ValueOfScoreOperatorTest.getNewValidValueOfScoreOperator());
        Mockito.when(unaryOperator.getId()).thenReturn(Math.abs(random.nextLong()));
        unaryExpression.setOperator(unaryOperator);
        expressionDTO = unaryExpression.toExpressionDTO();

        assertEquals("The getting OperatorId was not the expected one", unaryOperator.getId(),
            expressionDTO.getOperatorId());
        assertNull("The ScoreId was not null although there was no associated Score",
            expressionDTO.getScoreId());

        Score score = Mockito.spy(ScoreTest.getNewValidScore());
        Mockito.when(score.getId()).thenReturn(Math.abs(random.nextLong()));
        unaryExpression.setScore(score);
        expressionDTO = unaryExpression.toExpressionDTO();

        assertEquals("The getting OperatorId was not the expected one", unaryOperator.getId(),
            expressionDTO.getOperatorId());
        assertEquals("The getting ScoreId was not the expected one", score.getId(),
            expressionDTO.getScoreId());

        // Value Operator
        unaryOperator = Mockito.spy(ValueOperatorTest.getNewValidValueOperator());
        Mockito.when(unaryOperator.getId()).thenReturn(Math.abs(random.nextLong()));
        unaryExpression.setOperator(unaryOperator);
        Double testDouble = random.nextDouble();
        unaryExpression.setValue(testDouble);
        expressionDTO = unaryExpression.toExpressionDTO();

        assertEquals("The getting OperatorId was not the expected one", unaryOperator.getId(),
            expressionDTO.getOperatorId());
        assertEquals("The getting Value was not the expected one", testDouble.toString(),
            expressionDTO.getValue());
    }
}
