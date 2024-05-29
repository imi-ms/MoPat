package de.imi.mopat.model.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterTest;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Response;
import de.imi.mopat.model.ResponseTest;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderAnswerTest;
import de.imi.mopat.utils.Helper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class ValueOfQuestionOperatorTest {

    private static final Random random = new Random();
    private ValueOfQuestionOperator valueOfQuestionOperator;
    private Encounter encounter;
    private UnaryExpression unaryExpression;
    private Question question;

    /**
     * Returns a new valid instance of {@link ValueOfQuestionOperator}
     *
     * @return New valid valueOfQuestionOperator instance
     */
    public static ValueOfQuestionOperator getNewValidValueOfQuestionOperator() {
        return new ValueOfQuestionOperator();
    }

    @Before
    public void setUp() {
        valueOfQuestionOperator = ValueOfQuestionOperatorTest.getNewValidValueOfQuestionOperator();
        encounter = EncounterTest.getNewValidEncounter();
        question = QuestionTest.getNewValidQuestion();
        unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression(question);
    }

    /**
     * Test of {@link ValueOfQuestionOperator#evaluate}<br> Invalid input: {@link BinaryExpression},
     * {@link MultiExpression}, {@link Question}
     * <code>null</code>, {@link Question} with no
     * {@link Answer Answers}, {@link Response} value <code>null</code>, no {@link Response} for the
     * {@link Question}<br> Valid input: random valid {@link UnaryExpression} with {@link Question}
     * with {@link Answer Answers}with {@link Response Responses}
     */
    @Test
    public void testEvaluate() {
        // Other expression types
        assertNull(
            "The evaluate method returned not null although the given parameter was a binary Expression",
            valueOfQuestionOperator.evaluate(BinaryExpressionTest.getNewValidBinaryExpression(),
                encounter));
        assertNull(
            "The evaluate method returned not null although the given parameter was a multi Expression",
            valueOfQuestionOperator.evaluate(MultiExpressionTest.getNewValidMultiExpression(),
                encounter));

        // Question null
        unaryExpression.setQuestion(null);
        assertNull(
            "The evaluate method returned not null although the given Expression has no Question",
            valueOfQuestionOperator.evaluate(unaryExpression, encounter));

        // No Answers
        unaryExpression.setQuestion(question);
        assertNull(
            "The evaluate method returned not null although the given Expression has a Question with no Answers",
            valueOfQuestionOperator.evaluate(unaryExpression, encounter));

        int countAnswers = random.nextInt(10) + 2;
        SliderAnswer answer = Mockito.spy(SliderAnswerTest.getNewValidSliderAnswer());
        for (int i = 0; i < countAnswers; i++) {
            answer = Mockito.spy(SliderAnswerTest.getNewValidSliderAnswer());
            Mockito.when(answer.getId()).thenReturn(Math.abs(random.nextLong()));
            answer.setQuestion(question);
        }

        encounter.addResponse(ResponseTest.getNewValidResponse(answer));
        assertNull("The evaluate method returned not null although the Responses value was null",
            valueOfQuestionOperator.evaluate(unaryExpression, encounter));

        Set<Response> responses = new HashSet<>();
        SliderAnswer otherAnswer = Mockito.spy(SliderAnswerTest.getNewValidSliderAnswer());
        Mockito.when(otherAnswer.getId()).thenReturn(Math.abs(random.nextLong()));
        answer.setQuestion(question);
        responses.add(ResponseTest.getNewValidResponse(otherAnswer));
        encounter.setResponses(responses);
        assertNull(
            "The evaluate method returned not null although there was no response for the question",
            valueOfQuestionOperator.evaluate(unaryExpression, encounter));

        Response response = ResponseTest.getNewValidResponse(answer);
        Double value = 0.0;
        while (value < answer.getMinValue() || value > answer.getMaxValue()) {
            if (value < answer.getMinValue()) {
                value = value + random.nextDouble();
            } else if (value > answer.getMaxValue()) {
                value = value - random.nextDouble();
            }
        }

        response.setValue(value);
        responses.add(response);
        encounter.setResponses(responses);

        Double testValue = valueOfQuestionOperator.evaluate(unaryExpression, encounter);
        assertNotNull(
            "Evaluating operator failed. The returned value was null although not null value was expected.",
            testValue);
        assertEquals(
            "Evaluating operator failed. The returned value didn't match the expected value.",
            value, testValue);
    }

    /**
     * Test of {@link ValueOfQuestionOperator#getFormula}<br> Invalid input:
     * {@link BinaryExpression}, {@link MultiExpression},<code>null</code>, {@link Question}
     * <code>null</code><br> Valid input: random valid {@link UnaryExpression} with
     * {@link Question}
     * and valid locale as String
     */
    @Test
    public void testGetFormula() {
        // Other expression types
        assertNull(
            "Getting formula failed. The returned value didn't match null although a binary expression was used as param",
            valueOfQuestionOperator.getFormula(BinaryExpressionTest.getNewValidBinaryExpression(),
                encounter, Helper.getRandomLocale()));
        assertNull(
            "Getting formula failed. The returned value didn't match null although a multi expression was used as param",
            valueOfQuestionOperator.getFormula(MultiExpressionTest.getNewValidMultiExpression(),
                encounter, Helper.getRandomLocale()));
        assertNull(
            "Getting formula failed. The returned value didn't match null although null was used as param",
            valueOfQuestionOperator.getFormula(null, encounter, Helper.getRandomLocale()));

        // Question null
        unaryExpression.setQuestion(null);
        assertNull("Getting formula failed. The returned value didn't match null",
            valueOfQuestionOperator.getFormula(unaryExpression, encounter,
                Helper.getRandomLocale()));

        unaryExpression.setQuestion(question);
        SliderAnswer answer = Mockito.spy(SliderAnswerTest.getNewValidSliderAnswer());
        Mockito.when(answer.getId()).thenReturn(Math.abs(random.nextLong()));
        answer.setQuestion(question);

        Integer size = random.nextInt(13) + 1;
        Integer position = random.nextInt(size);
        String locale = "";
        Map<String, String> stringMap = new HashMap<>();

        for (int i = 0; i < size; i++) {
            if (i == position) {
                locale = Helper.getRandomLocale();
                stringMap.put(locale, Helper.getRandomAlphabeticString(random.nextInt(255) + 1));
            } else {
                stringMap.put(Helper.getRandomLocale(),
                    Helper.getRandomAlphabeticString(random.nextInt(255) + 1));
            }
        }
        stringMap.put("de", Helper.getRandomAlphabeticString(random.nextInt(255) + 1));
        question.setLocalizedQuestionText(stringMap);

        String formula = question.getLocalizedQuestionText().get(locale);
        assertNotNull(
            "Getting formula failed. The returned formula was null although a not null value was expected.",
            valueOfQuestionOperator.getFormula(unaryExpression, encounter, locale));
        assertEquals(
            "Getting formula failed. The returned formula didn't match the expected value.",
            formula, valueOfQuestionOperator.getFormula(unaryExpression, encounter, locale));

        formula = question.getLocalizedQuestionText().get(
            unaryExpression.getQuestion().getLocalizedQuestionText().keySet().iterator().next());
        assertEquals(
            "Getting formula failed. The returned formula didn't match the expected value.",
            formula, valueOfQuestionOperator.getFormula(unaryExpression, encounter,
                Helper.getRandomLocale()));

        assertEquals(
            "Getting formula failed. The returned formula didn't match the expected value.",
            formula, valueOfQuestionOperator.getFormula(unaryExpression, encounter,
                Helper.getRandomAlphabeticString(2) + "-" + Helper.getRandomAlphabeticString(2)
                    .toUpperCase()));

        formula = question.getLocalizedQuestionText().get("de");
        assertEquals(
            "Getting formula failed. The returned formula didn't match the expected value.",
            formula, valueOfQuestionOperator.getFormula(unaryExpression, encounter,
                "de-" + Helper.getRandomAlphabeticString(2).toUpperCase()));
    }
}
