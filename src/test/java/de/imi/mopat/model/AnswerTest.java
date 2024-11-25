package de.imi.mopat.model;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTest;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class AnswerTest {

    private static final Random random = new Random();
    private Answer testAnswer;

    public AnswerTest() {
    }

    /**
     * Returns a valid new Answer
     *
     * @return Returns a valid new Answer
     */
    public static Answer getNewValidRandomAnswer() {
        switch (random.nextInt(7)) {
            case 0:
                return DateAnswerTest.getNewValidDateAnswer();
            case 1:
                return FreetextAnswerTest.getNewValidFreetextAnswer();
            case 2:
                return NumberInputAnswerTest.getNewValidNumberInputAnswer();
            case 3:
                return SelectAnswerTest.getNewValidSelectAnswer();
            case 4:
                return SliderAnswerTest.getNewValidSliderAnswer();
            case 5:
                return ImageAnswerTest.getNewValidImageAnswer();
            default:
                return SliderFreetextAnswerTest.getNewValidSliderFreetextAnswer();
        }
    }

    @Before
    public void beforeTest() {
        testAnswer = getNewValidRandomAnswer();
    }

    /**
     * Test of {@link Answer#getIsEnabled} and {@link Answer#setIsEnabled}.<br> Valid input: random
     * Boolean
     */
    @Test
    public void testGetAnsSetIsEnabled() {
        fail();
        /*
        Boolean testIsEnabled = random.nextBoolean();
        testAnswer.setIsEnabled(testIsEnabled);
        assertEquals("The getting isEnabled was not the expected one", testIsEnabled,
            testAnswer.getIsEnabled());
         */
    }

    /**
     * Test of {@link Answer#getQuestion} and {@link Answer#setQuestion}.<br> Invalid input:
     * <code>null</code><br> Valid input: random {@link Question}
     */
    @Test
    public void testGetAndSetQuestion() {
        Question testQuestion = null;
        Throwable e = null;
        try {
            testAnswer.setQuestion(testQuestion);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the question", e instanceof AssertionError);

        testQuestion = QuestionTest.getNewValidQuestion();
        testAnswer.setQuestion(testQuestion);
        assertEquals("The getting question was not the expected one", testQuestion,
            testAnswer.getQuestion());
    }

    /**
     * Test of {@link Answer#removeQuestion}.<br>
     */
    @Test
    public void testRemoveQuestion() {
        assertNotNull("The question was null before removing", testAnswer.getQuestion());
        testAnswer.removeQuestion();
        assertNull("The question was not null after removing", testAnswer.getQuestion());
        testAnswer.removeQuestion();
        assertNull("The question was not null after removing it twice", testAnswer.getQuestion());
    }

    /**
     * Test of {@link Answer#addResponses} and {@link Answer#getResponses}.<br> Invalid input:
     * <code>null</code><br> Valid input: random Set of {@link Response Responses}
     */
    @Test
    public void testGetAndAddResponses() {
        Set<Response> testSet = null;
        Throwable e = null;
        try {
            testAnswer.addResponses(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the reponses", e instanceof AssertionError);

        testSet = new HashSet<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testSet.add(ResponseTest.getNewValidResponse());
        }
        testAnswer.addResponses(testSet);
        assertEquals("The getting set of responses was not the expected one", testSet,
            testAnswer.getResponses());
    }

    /**
     * Test of {@link Answer#addResponse}.<br> Invalid input: <code>null</code><br> Valid input:
     * random number of {@link Response Responses}
     */
    @Test
    public void testAddResponse() {
        Response testResponse = null;
        Throwable e = null;
        try {
            testAnswer.addResponse(testResponse);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as a response", e instanceof AssertionError);

        Set<Response> testSet = new HashSet<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testResponse = ResponseTest.getNewValidResponse();
            testAnswer.addResponse(testResponse);
            testSet.add(testResponse);
        }
        // Add Response without Answer and Encounter
        Response testResponseWithAnswerNull = new Response();
        testAnswer.addResponse(testResponseWithAnswerNull);
        testSet.add(testResponseWithAnswerNull);
        assertEquals("The getting set of responses was not the expected one", testSet,
            testAnswer.getResponses());
    }

    /**
     * Test of {@link Answer#removeResponse}.<br> Valid input: random number of
     * {@link Response Responses}
     */
    @Test
    public void testRemoveResponse() {
        Response testResponse = null;
        Throwable e = null;
        try {
            testAnswer.removeResponse(testResponse);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove the response null", e instanceof AssertionError);

        Set<Response> testSet = new HashSet<>();
        Set<Response> removeSet = new HashSet<>();
        int count = random.nextInt(50) + 5;
        for (int i = 0; i < count; i++) {
            testResponse = ResponseTest.getNewValidResponse();
            testSet.add(testResponse);
            if (random.nextBoolean()) {
                removeSet.add(testResponse);
            }
        }
        testAnswer.addResponses(testSet);
        assertEquals("The getting set of responses was not the expected one", testSet,
            testAnswer.getResponses());
        for (Response response : removeSet) {
            testAnswer.removeResponse(response);
        }
        testSet.removeAll(removeSet);
        assertEquals("The getting set of responses after removing was not the expected one",
            testSet, testAnswer.getResponses());
    }

    /**
     * Test of {@link Answer#addExportRules} and {@link Answer#getExportRules}.<br> Invalid input:
     * <code>null</code><br> Valid input: random number of {@link ExportRule ExportRules}
     */
    @Test
    public void testGetAndAddExportRules() {
        Set<ExportRuleAnswer> testSet = null;
        Throwable e = null;
        try {
            testAnswer.addExportRules(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the ExportRules", e instanceof AssertionError);

        testSet = new HashSet<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testSet.add(ExportRuleAnswerTest.getNewValidExportRuleAnswer());
        }
        testAnswer.addExportRules(testSet);
        assertEquals("The getting set of ExportRules was not the expected one", testSet,
            testAnswer.getExportRules());
    }

    /**
     * Test of {@link Answer#addExportRule}.<br> Invalid input: <code>null</code><br> Valid input:
     * random number of {@link ExportRule ExportRules}
     */
    @Test
    public void testAddExportRule() {
        ExportRuleAnswer testExportRule = null;
        Throwable e = null;
        try {
            testAnswer.addExportRule(testExportRule);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the ExportRules", e instanceof AssertionError);

        Set<ExportRuleAnswer> testSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testExportRule = ExportRuleAnswerTest.getNewValidExportRuleAnswer();
            testAnswer.addExportRule(testExportRule);
            testSet.add(testExportRule);
        }
        // Add ExportRule without Answer
        ExportRuleAnswer testExportRuleWithAnswerNull = new ExportRuleAnswer();
        testAnswer.addExportRule(testExportRuleWithAnswerNull);
        testSet.add(testExportRuleWithAnswerNull);
        assertEquals("The getting set of ExportRules was not the expected one", testSet,
            testAnswer.getExportRules());
    }

    /**
     * Test of {@link Answer#removeExportRule}.<br> Valid input: random number of
     * {@link ExportRuleAnswer ExportRuleAnswers}
     */
    @Test
    public void testRemoveExportRule() {
        ExportRuleAnswer testExportRule = null;
        Throwable e = null;
        try {
            testAnswer.removeExportRule(testExportRule);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as a ExportRule", e instanceof AssertionError);

        Set<ExportRuleAnswer> testSet = new HashSet<>();
        Set<ExportRuleAnswer> removeSet = new HashSet<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testExportRule = ExportRuleAnswerTest.getNewValidExportRuleAnswer();
            testSet.add(testExportRule);
            if (random.nextBoolean()) {
                removeSet.add(testExportRule);
            }
        }

        // Add an ExportRule that is not connected to this Answer
        removeSet.add(ExportRuleAnswerTest.getNewValidExportRuleAnswer());
        testAnswer.addExportRules(testSet);
        assertEquals("The getting set of ExportRules was not the expected one", testSet,
            testAnswer.getExportRules());
        for (ExportRuleAnswer exportRule : removeSet) {
            testAnswer.removeExportRule(exportRule);
        }
        testSet.removeAll(removeSet);
        assertEquals("The getting set of ExportRules after removing was not the expected one",
            testSet, testAnswer.getExportRules());
    }

    /**
     * Test of {@link Answer#getExportRulesByExportTemplate}.<br> Valid input: random number of
     * {@link ExportRule ExportRules}
     */
    @Test
    public void testGetExportRulesByExportTemplate() {
        Set<ExportRuleAnswer> testSet = new HashSet<>();
        ExportTemplate exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            if (random.nextBoolean()) {
                ExportRuleAnswer exportRule = ExportRuleAnswerTest.getNewValidExportRuleAnswer(
                    exportTemplate);
                testAnswer.addExportRule(exportRule);
                testSet.add(exportRule);
            } else {
                testAnswer.addExportRule(ExportRuleAnswerTest.getNewValidExportRuleAnswer());
            }
        }
        assertEquals("The getting set of ExportRulesByExportTemplate was not the expected one",
            testSet, testAnswer.getExportRulesByExportTemplate(exportTemplate));
    }

    /**
     * Test of {@link Answer#equals}.<br> Invalid input: the same {@link Answer} twice in a HashSet
     */
    @Test
    public void testEquals() {
        Set<Answer> testSet = new HashSet<>();
        testSet.add(testAnswer);
        testSet.add(testAnswer);
        assertEquals("It was possible to set the same Answer twice in one set", 1, testSet.size());

        assertEquals("The Answer was not equal to itself", testAnswer, testAnswer);
        assertNotEquals("The Answer was equal to null", null, testAnswer);
        Answer otherAnswer = getNewValidRandomAnswer();
        assertNotEquals("The Answer was equal to a different Answer", testAnswer, otherAnswer);
        Object otherObject = new Object();
        assertNotEquals("The Answer was equal to a different Object", testAnswer, otherObject);
    }

    /**
     * Test of {@link Answer#addConditions}.<br> Invalid input: <code>null</code><br> Valid input:
     * random number of {@link Condition Conditions}
     */
    @Test
    public void testGetAndAddConditions() {
        Set<Condition> testSet = null;
        Throwable e = null;
        try {
            testAnswer.addConditions(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Conditions", e instanceof AssertionError);

        testSet = new HashSet<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testSet.add(ConditionTest.getNewValidCondition());
        }
        testAnswer.addConditions(testSet);
        assertEquals("The getting set of Conditions was not the expected one", testSet,
            testAnswer.getConditions());
    }

    /**
     * Test of {@link Answer#addCondition}.<br> Invalid input: <code>null</code><br> Valid input:
     * random number of {@link Condition Conditions}
     */
    @Test
    public void testAddCondition() {
        Condition testCondition = null;
        Throwable e = null;
        try {
            testAnswer.addCondition(testCondition);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the Conditions", e instanceof AssertionError);

        Set<Condition> testSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testCondition = ConditionTest.getNewValidCondition();
            testAnswer.addCondition(testCondition);
            testSet.add(testCondition);
        }
        // Add Condition without Trigger
        Condition testConditionWithTriggerNull = new SelectAnswerCondition();
        testAnswer.addCondition(testConditionWithTriggerNull);
        testSet.add(testConditionWithTriggerNull);
        assertEquals("The getting set of Conditions was not the expected one", testSet,
            testAnswer.getConditions());
    }

    /**
     * Test of {@link Answer#contains}.<br> Invalid input: <code>null</code><br> Valid input:
     * containing and new {@link Condition}
     */
    @Test
    public void testContains() {
        Condition testCondition = null;
        Throwable e = null;
        try {
            testAnswer.contains(testCondition);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to check if null is contained", e instanceof AssertionError);

        testCondition = ConditionTest.getNewValidCondition();
        testAnswer.addCondition(testCondition);
        assertTrue("An added Condition was not contained", testAnswer.contains(testCondition));
        assertFalse("A new Condition was already in the set",
            testAnswer.contains(ConditionTest.getNewValidCondition()));
    }

    /**
     * Test of {@link Answer#removeCondition}.<br> Invalid input: <code>null</code><br> Valid input:
     * random number of {@link Condition Conditions}
     */
    @Test
    public void testRemoveCondition() {
        Condition testCondition = null;
        Throwable e = null;
        try {
            testAnswer.removeCondition(testCondition);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the Conditions",
            e instanceof AssertionError);

        Set<Condition> testSet = new HashSet<>();
        Set<Condition> removeSet = new HashSet<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testCondition = ConditionTest.getNewValidCondition();
            testSet.add(testCondition);
            if (random.nextBoolean()) {
                removeSet.add(testCondition);
            }
        }
        // Add a Condition that is not connected to this Answer
        removeSet.add(ConditionTest.getNewValidCondition());
        testAnswer.addConditions(testSet);
        assertEquals("The getting set of Conditions was not the expected one", testSet,
            testAnswer.getConditions());
        for (Condition condition : removeSet) {
            testAnswer.removeCondition(condition);
        }
        testSet.removeAll(removeSet);
        assertEquals("The getting set of Conditions after removing was not the expected one",
            testSet, testAnswer.getConditions());
    }

    /**
     * Test of {@link Answer#removeConditions}.<br> Invalid input: <code>null</code><br> Valid
     * input: random number of {@link Condition Conditions}
     */
    @Test
    public void testRemoveConditions() {
        Condition testCondition;
        Set<Condition> removeSet = null;
        Throwable e = null;
        try {
            testAnswer.removeConditions(removeSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the Conditions",
            e instanceof AssertionError);
        Set<Condition> testSet = new HashSet<>();
        removeSet = new HashSet<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testCondition = ConditionTest.getNewValidCondition();
            testSet.add(testCondition);
            if (random.nextBoolean()) {
                removeSet.add(testCondition);
            }
        }
        testAnswer.addConditions(testSet);
        assertEquals("The getting set of Conditions was not the expected one", testSet,
            testAnswer.getConditions());

        testAnswer.removeConditions(removeSet);
        testSet.removeAll(removeSet);
        assertEquals("The getting set of Conditions after removing was not the expected one",
            testSet, testAnswer.getConditions());
    }
}
