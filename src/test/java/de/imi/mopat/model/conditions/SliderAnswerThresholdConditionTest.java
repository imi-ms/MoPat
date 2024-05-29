package de.imi.mopat.model.conditions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleTest;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderAnswerTest;
import de.imi.mopat.utils.Helper;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @since v1.2
 */
public class SliderAnswerThresholdConditionTest {

    private static final Random random = new Random();
    private SliderAnswerThresholdCondition testSliderAnswerThresholdCondition;

    public SliderAnswerThresholdConditionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new SliderAnswerThresholdCondition
     *
     * @return Returns a valid new SliderAnswerThresholdCondition
     */
    public static SliderAnswerThresholdCondition getNewValidSliderAnswerThresholdCondition() {
        ConditionActionType action = Helper.getRandomEnum(ConditionActionType.class);
        ConditionTrigger testTrigger = SliderAnswerTest.getNewValidSliderAnswer();
        ConditionTarget testTarget = QuestionTest.getNewValidQuestion();
        Bundle testBundle = BundleTest.getNewValidBundle();
        ThresholdComparisonType thresholdComparisonType = Helper.getRandomEnum(
            ThresholdComparisonType.class);
        double threshold = ((SliderAnswer) testTrigger).getMinValue() + (Math.random() * (
            (((SliderAnswer) testTrigger).getMaxValue()
                - ((SliderAnswer) testTrigger).getMinValue()) + 1));

        return new SliderAnswerThresholdCondition(testTrigger, testTarget, action, testBundle,
            thresholdComparisonType, threshold);
    }

    @Before
    public void setUp() {
        testSliderAnswerThresholdCondition = getNewValidSliderAnswerThresholdCondition();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link SliderAnswerThresholdCondition#getThreshold} and
     * {@link SliderAnswerThresholdCondition#setThreshold}.<br> Invalid input: <code>null</code>
     * Valid input: random Double
     */
    @Test
    public void testGetAndSetThreshold() {
        Double testThreshold = null;
        Throwable e = null;
        try {
            testSliderAnswerThresholdCondition.setThreshold(testThreshold);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the Threshold as null", e instanceof AssertionError);

        testThreshold = random.nextDouble();
        testSliderAnswerThresholdCondition.setThreshold(testThreshold);
        assertEquals("The getting Threshold was not the expected one", testThreshold,
            testSliderAnswerThresholdCondition.getThreshold());
    }

    /**
     * Test of {@link SliderAnswerThresholdCondition#getThresholdComparisonType} and
     * {@link SliderAnswerThresholdCondition#setThresholdComparisonType}.<br> Invalid input:
     * <code>null</code> Valid input: random {@link ThresholdComparisonType}
     */
    @Test
    public void testGetAndSetThresholdComparisonType() {
        ThresholdComparisonType testThresholdComparisonType = null;
        Throwable e = null;
        try {
            testSliderAnswerThresholdCondition.setThresholdComparisonType(
                testThresholdComparisonType);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the ThresholdComparisonType as null",
            e instanceof AssertionError);

        testThresholdComparisonType = Helper.getRandomEnum(ThresholdComparisonType.class);
        testSliderAnswerThresholdCondition.setThresholdComparisonType(testThresholdComparisonType);
        assertEquals("The getting Threshold was not the expected one", testThresholdComparisonType,
            testSliderAnswerThresholdCondition.getThresholdComparisonType());
    }
}
