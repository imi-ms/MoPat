package de.imi.mopat.model.conditions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.model.Answer;
import de.imi.mopat.model.AnswerTest;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleTest;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.SelectAnswerTest;
import de.imi.mopat.model.SliderAnswerTest;
import de.imi.mopat.model.dto.ConditionDTO;
import de.imi.mopat.utils.Helper;
import java.util.HashSet;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @since v1.2
 */
public class ConditionTest {

    private static final Random random = new Random();
    private Condition testCondition;

    public ConditionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new Condition
     *
     * @return Returns a valid new Condition
     */
    public static Condition getNewValidCondition() {
        if (random.nextBoolean()) {
            return SelectAnswerConditionTest.getNewValidSelectAnswerCondition();
        }
        return SliderAnswerThresholdConditionTest.getNewValidSliderAnswerThresholdCondition();
    }

    @Before
    public void setUp() {
        testCondition = getNewValidCondition();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of
     * {@link Condition#Condition(ConditionTrigger, ConditionTarget, ConditionActionType,
     * Bundle)}.<br> Valid input: {@link Question} as {@link ConditionTarget} and {@link Answer} as
     * {@link ConditionTarget}
     */
    @Test
    public void testConstructor() {
        ConditionActionType testAction = Helper.getRandomEnum(ConditionActionType.class);
        testCondition = new SelectAnswerCondition(SelectAnswerTest.getNewValidSelectAnswer(),
            QuestionTest.getNewValidQuestion(), testAction, null);
        assertNull("The TargetAnswerQuestion was not null although the target was not an Answer",
            testCondition.getTargetAnswerQuestion());
        testCondition = new SelectAnswerCondition(SelectAnswerTest.getNewValidSelectAnswer(),
            SelectAnswerTest.getNewValidSelectAnswer(), testAction, null);
        assertNotNull("The TargetAnswerQuestion was null although the target was an Answer",
            testCondition.getTargetAnswerQuestion());
    }

    /**
     * Test of {@link Condition#getTrigger} and {@link Condition#setTrigger}.<br> Invalid input:
     * <code>null</code><br> Valid input: random {@link ConditionTrigger}
     */
    @Test
    public void testGetAndSetTrigger() {
        Answer testTrigger = null;
        Throwable e = null;
        try {
            testCondition.setTrigger(testTrigger);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the trigger as null", e instanceof AssertionError);

        if (random.nextBoolean()) {
            testTrigger = SelectAnswerTest.getNewValidSelectAnswer();
        } else {
            testTrigger = SliderAnswerTest.getNewValidSliderAnswer();
        }
        testCondition.setTrigger(testTrigger);
        assertEquals("The getting Trigger was not the expected one", testTrigger,
            testCondition.getTrigger());
        // Set same Trigger again
        testCondition.setTrigger(testTrigger);
        assertEquals(
            "The getting Trigger was not the expected one after setting the same Trigger again",
            testTrigger, testCondition.getTrigger());
    }

    /**
     * Test of {@link Condition#removeTrigger}.<br> Valid input: random {@link Condition} with
     * random {@link ConditionTrigger}
     */
    @Test
    public void testRemoveTrigger() {
        assertNotNull("The Trigger was null before removing", testCondition.getTrigger());
        testCondition.removeTrigger();
        assertNull("The Trigger was not null after removing", testCondition.getTrigger());
    }

    /**
     * Test of {@link Condition#getTarget} and {@link Condition#setTarget}.<br> Invalid input:
     * <code>null</code><br> Valid input: random {@link ConditionTarget}
     */
    @Test
    public void testGetAndSetTarget() {
        ConditionTarget testTarget = null;
        Throwable e = null;
        try {
            testCondition.setTarget(testTarget);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the target as null", e instanceof AssertionError);

        int targetType = random.nextInt(3);
        switch (targetType) {
            case 0:
                testTarget = AnswerTest.getNewValidRandomAnswer();
                break;
            case 1:
                testTarget = QuestionTest.getNewValidQuestion();
                break;
            default:
                testTarget = QuestionnaireTest.getNewValidQuestionnaire();
                break;
        }
        testCondition.setTarget(testTarget);
        assertEquals("The getting Target was not the expected one", testTarget,
            testCondition.getTarget());
    }

    /**
     * Test of {@link Condition#getAction} and {@link Condition#setAction}.<br> Invalid input:
     * <code>null</code><br> Valid input: random {@link ConditionActionType}
     */
    @Test
    public void testGetAndSetAction() {
        ConditionActionType testAction = null;
        Throwable e = null;
        try {
            testCondition.setAction(testAction);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the action as null", e instanceof AssertionError);

        testAction = Helper.getRandomEnum(ConditionActionType.class);
        testCondition.setAction(testAction);
        assertEquals("The getting ActionType was not the expected one", testAction,
            testCondition.getAction());
    }

    /**
     * Test of {@link Condition#getBundle} and {@link Condition#setBundle}.<br> Valid input: random
     * {@link Bundle}, <code>null</code>
     */
    @Test
    public void testGetAndSetBundle() {
        Bundle testBundle = BundleTest.getNewValidBundle();
        testCondition.setBundle(testBundle);
        assertEquals("The getting Bundle was not the expected one", testBundle,
            testCondition.getBundle());
        testCondition.setBundle(null);
        assertNull("The getting Bundle was not null", testCondition.getBundle());
    }

    /**
     * Test of {@link Condition#getTargetAnswerQuestion} and
     * {@link Condition#setTargetAnswerQuestion}.<br> Valid input: random {@link Question},
     * <code>null</code>
     */
    @Test
    public void testGetAndSetTargetAnswerQuestion() {
        Question testQuestion = QuestionTest.getNewValidQuestion();
        testCondition.setTargetAnswerQuestion(testQuestion);
        assertEquals("The getting TargetAnswerQuestion was not the expected one", testQuestion,
            testCondition.getTargetAnswerQuestion());
        testCondition.setTargetAnswerQuestion(null);
        assertNull("The getting TargetAnswerQuestion was not null",
            testCondition.getTargetAnswerQuestion());
    }

    /**
     * Test of {@link Condition#toConditionDTO}.<br> Valid input: Either a
     * {@link SelectAnswerCondition} or a {@link SliderAnswerThresholdCondition}
     */
    @Test
    public void testToConditionDTO() {
        Condition spyCondition = spy(testCondition);
        Mockito.when(spyCondition.getId()).thenReturn(Math.abs(random.nextLong()));
        ConditionTrigger spyTrigger = spy(SelectAnswerTest.getNewValidSelectAnswer());
        Mockito.when(spyTrigger.getId()).thenReturn(Math.abs(random.nextLong()));
        ConditionTarget spyTarget = spy(QuestionTest.getNewValidQuestion());
        Mockito.when(spyTarget.getId()).thenReturn(Math.abs(random.nextLong()));
        Question spyQuestion = spy(QuestionTest.getNewValidQuestion());
        Mockito.when(spyQuestion.getId()).thenReturn(Math.abs(random.nextLong()));

        spyCondition.setTrigger(spyTrigger);
        spyCondition.setTarget(spyTarget);
        spyCondition.setTargetAnswerQuestion(spyQuestion);
        spyCondition.setBundle(null);

        ConditionDTO testConditionDTO = spyCondition.toConditionDTO();
        assertEquals("The getting Id was not the expected one", spyCondition.getId(),
            testConditionDTO.getId());
        assertEquals("The getting Action was not the expected one", spyCondition.getAction().name(),
            testConditionDTO.getAction());
        assertEquals("The getting TargetClass was not the expected one",
            spyCondition.getTargetClass(), testConditionDTO.getTargetClass());
        assertEquals("The getting TargetId was not the expected one", spyTarget.getId(),
            testConditionDTO.getTargetId());
        assertEquals("The getting TriggerId was not the expected one", spyTrigger.getId(),
            testConditionDTO.getTriggerId());
        assertEquals("The getting TargetAnswerQuestionId was not the expected one",
            spyQuestion.getId(), testConditionDTO.getTargetAnswerQuestionId());
        assertNull("The getting BundleId was not null although there was no bundle",
            testConditionDTO.getBundleId());
        if (spyCondition instanceof SliderAnswerThresholdCondition) {
            assertEquals("The getting ThresholdType was not the expected one",
                ((SliderAnswerThresholdCondition) spyCondition).getThresholdComparisonType(),
                testConditionDTO.getThresholdType());
            assertEquals("The getting ThresholdValue was not the expected one",
                ((SliderAnswerThresholdCondition) spyCondition).getThreshold(),
                testConditionDTO.getThresholdValue());
        }

        Bundle spyBundle = spy(BundleTest.getNewValidBundle());
        Mockito.when(spyBundle.getId()).thenReturn(Math.abs(random.nextLong()));
        spyCondition.setBundle(spyBundle);

        testConditionDTO = spyCondition.toConditionDTO();
        assertEquals("The getting BundleId was not the expected one", spyBundle.getId(),
            testConditionDTO.getBundleId());
    }

    /**
     * Test of {@link Condition#equals}.<br> Valid input: the same
     * {@link Condition, a different {@link Condition}, a different Object and
     * <code>null</code>
     */
    @Test
    public void testEquals() {
        HashSet<Condition> testSet = new HashSet<>();
        testSet.add(testCondition);
        testSet.add(testCondition);
        assertEquals("It was possible to add the same Condtion twice to one set", 1,
            testSet.size());

        assertEquals("The Condition was not equal to itself", testCondition, testCondition);
        assertNotEquals("The Condition was equal to null", null, testCondition);

        Condition otherCondtion = getNewValidCondition();
        assertNotEquals("Two different Conditions were equal", testCondition, otherCondtion);

        Object otherObject = new Object();
        assertNotEquals("The Condition was equal to a different Object", testCondition,
            otherObject);
    }
}
