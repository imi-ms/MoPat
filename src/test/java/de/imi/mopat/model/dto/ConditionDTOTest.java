package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.model.conditions.ThresholdComparisonType;
import de.imi.mopat.utils.Helper;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class ConditionDTOTest {

    private static final Random random = new Random();
    private ConditionDTO testConditionDTO;

    public ConditionDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testConditionDTO = new ConditionDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ConditionDTO#getId} and {@link ConditionDTO#setId}.<br> Valid input: random
     * Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testConditionDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID, testConditionDTO.getId());
    }

    /**
     * Test of {@link ConditionDTO#getAction} and {@link ConditionDTO#setAction}.<br> Valid input:
     * random String
     */
    @Test
    public void testGetAndSetAction() {
        String testAction = Helper.getRandomAlphanumericString(random.nextInt(50));
        testConditionDTO.setAction(testAction);
        assertEquals("The getting Action was not the expected one", testAction,
            testConditionDTO.getAction());
    }

    /**
     * Test of {@link ConditionDTO#getTargetClass} and {@link ConditionDTO#setTargetClass}.<br>
     * Valid input: random String
     */
    @Test
    public void testGetAndSetTargetClass() {
        String testTargetClass = Helper.getRandomAlphanumericString(random.nextInt(50));
        testConditionDTO.setTargetClass(testTargetClass);
        assertEquals("The getting TargetClass was not the expected one", testTargetClass,
            testConditionDTO.getTargetClass());
    }

    /**
     * Test of {@link ConditionDTO#getTargetId} and {@link ConditionDTO#setTargetId}.<br> Valid
     * input: random Long
     */
    @Test
    public void testGetAndSetTargetId() {
        Long testTargetID = Math.abs(random.nextLong());
        testConditionDTO.setTargetId(testTargetID);
        assertEquals("The getting TargetID was not the expected one", testTargetID,
            testConditionDTO.getTargetId());
    }

    /**
     * Test of {@link ConditionDTO#getTriggerId} and {@link ConditionDTO#setTriggerId}.<br> Valid
     * input: random Long
     */
    @Test
    public void testGetAndSetTriggerId() {
        Long testTriggerID = Math.abs(random.nextLong());
        testConditionDTO.setTriggerId(testTriggerID);
        assertEquals("The getting TriggerID was not the expected one", testTriggerID,
            testConditionDTO.getTriggerId());
    }

    /**
     * Test of {@link ConditionDTO#getBundleId} and {@link ConditionDTO#setBundleId}.<br> Valid
     * input: random Long
     */
    @Test
    public void testGetAndSetBundleId() {
        Long testBundleID = Math.abs(random.nextLong());
        testConditionDTO.setBundleId(testBundleID);
        assertEquals("The getting BundleID was not the expected one", testBundleID,
            testConditionDTO.getBundleId());
    }

    /**
     * Test of {@link ConditionDTO#getTargetAnswerQuestionId} and
     * {@link ConditionDTO#setTargetAnswerQuestionId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetTargetAnswerQuestionId() {
        Long testTargetAnswerQuestionID = Math.abs(random.nextLong());
        testConditionDTO.setTargetAnswerQuestionId(testTargetAnswerQuestionID);
        assertEquals("The getting TargetAnswerQuestionID was not the expected one",
            testTargetAnswerQuestionID, testConditionDTO.getTargetAnswerQuestionId());
    }

    /**
     * Test of {@link ConditionDTO#getThresholdType} and {@link ConditionDTO#setThresholdType}.<br>
     * Valid input: random {@link ThresholdComparisonType}
     */
    @Test
    public void testGetAndSetThresholdType() {
        ThresholdComparisonType testThresholdType = Helper.getRandomEnum(
            ThresholdComparisonType.class);
        testConditionDTO.setThresholdType(testThresholdType);
        assertEquals("The getting ThresholdType was not the expected one", testThresholdType,
            testConditionDTO.getThresholdType());
    }

    /**
     * Test of {@link ConditionDTO#getThresholdValue} and
     * {@link ConditionDTO#setThresholdValue}.<br> Valid input: random Double
     */
    @Test
    public void testGetAndSetThresholdValue() {
        Double testThresholdValue = random.nextDouble();
        testConditionDTO.setThresholdValue(testThresholdValue);
        assertEquals("The getting ThresholdValue was not the expected one", testThresholdValue,
            testConditionDTO.getThresholdValue());
    }
}