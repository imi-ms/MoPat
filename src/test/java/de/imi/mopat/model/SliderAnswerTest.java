package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.utils.Helper;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class SliderAnswerTest {

    private static final Random random = new Random();
    private SliderAnswer testSliderAnswer;

    public SliderAnswerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    public static SliderAnswer getNewValidSliderAnswer() {
        Question question = QuestionTest.getNewValidQuestion();
        question.setQuestionType(QuestionType.SLIDER);
        Double min = random.nextDouble();
        Double max;
        do {
            max = random.nextDouble();
        } while (max < min);

        Double stepsize;
        do {
            stepsize = random.nextDouble();
        } while (stepsize >= Math.abs(max - min));
        Boolean isEnabled = random.nextBoolean();
        Boolean vertical = random.nextBoolean();

        return new SliderAnswer(question, isEnabled, min, max, stepsize, vertical);
    }

    public static SliderAnswer getNewValidSliderAnswer(Double min, Double max, Double stepsize) {
        Question question = QuestionTest.getNewValidQuestion();
        question.setQuestionType(QuestionType.SLIDER);
        Boolean isEnabled = random.nextBoolean();
        Boolean vertical = random.nextBoolean();

        return new SliderAnswer(question, isEnabled, min, max, stepsize, vertical);
    }

    @Before
    public void setUp() {
        testSliderAnswer = getNewValidSliderAnswer();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link SliderAnswer#cloneWithoutReferences}.<br> Valid input: valid
     * {@link SliderAnswer}
     */
    @Test
    public void testCloneWithoutReferences() {
        testSliderAnswer.setShowValueOnButton(random.nextBoolean());
        SliderAnswer testClone = testSliderAnswer.cloneWithoutReferences();
        assertEquals("The getting isEnabled was not the expected one",
            testSliderAnswer.getIsEnabled(), testClone.getIsEnabled());
        assertEquals("The getting minValue was not the expected one",
            testSliderAnswer.getMinValue(), testClone.getMinValue());
        assertEquals("The getting maxValue was not the expected one",
            testSliderAnswer.getMaxValue(), testClone.getMaxValue());
        assertEquals("The getting stepsize was not the expected one",
            testSliderAnswer.getStepsize(), testClone.getStepsize());
        assertEquals("The getting vertical was not the expected one",
            testSliderAnswer.getVertical(), testClone.getVertical());
        assertEquals("The getting showValueOnButton was not the expected one",
            testSliderAnswer.getShowValueOnButton(), testClone.getShowValueOnButton());
        assertEquals("The getting map of localizedMinimumText was not the expected one",
            testSliderAnswer.getLocalizedMinimumText(), testClone.getLocalizedMinimumText());
        assertEquals("The getting map of localizedMaximumText was not the expected one",
            testSliderAnswer.getLocalizedMaximumText(), testClone.getLocalizedMaximumText());

        Map<String, String> testLocalizedMinimumText = new HashMap<>();
        Map<String, String> testLocalizedMaximumText = new HashMap<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testLocalizedMinimumText.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(255));
            testLocalizedMaximumText.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(255));
        }
        testSliderAnswer.setLocalizedMaximumText(testLocalizedMaximumText);
        testSliderAnswer.setLocalizedMinimumText(testLocalizedMinimumText);
        testClone = testSliderAnswer.cloneWithoutReferences();
        assertEquals("The getting map of localizedMinimumText was not the expected one",
            testSliderAnswer.getLocalizedMinimumText(), testClone.getLocalizedMinimumText());
        assertEquals("The getting map of localizedMaximumText was not the expected one",
            testSliderAnswer.getLocalizedMaximumText(), testClone.getLocalizedMaximumText());
    }

    /**
     * Test of {@link SliderAnswer#getMinValue} and {@link SliderAnswer#setMinValue}.<br> Invalid
     * input: <code>null</code>, min greater than max<br> Valid input: min lower than max
     */
    @Test
    public void testGetAndSetMinValue() {
        Double min = null;
        Throwable e = null;
        try {
            testSliderAnswer.setMinValue(min);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a null as minValue", e instanceof AssertionError);

        min = random.nextDouble();
        testSliderAnswer.setMinMax(min / 2, min);
        e = null;
        try {
            testSliderAnswer.setMinValue(min * 2);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a greater minValue than maxValue",
            e instanceof AssertionError);
        testSliderAnswer.setMaxValue(min * 2);
        testSliderAnswer.setMinValue(min);
        assertEquals("The getting minValue was not the expected one", min,
            testSliderAnswer.getMinValue());
    }

    /**
     * Test of {@link SliderAnswer#getMaxValue} and {@link Slideranswer#setMaxValue}.<br> Invalid
     * input: <code>null</code>, max lower than min<br> Valid input: max greater than min
     */
    @Test
    public void testGetAndSetMaxValue() {
        Double max = null;
        Throwable e = null;
        try {
            testSliderAnswer.setMaxValue(max);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as maxValue", e instanceof AssertionError);

        max = random.nextDouble();
        testSliderAnswer.setMinMax(max, max * 2);
        e = null;
        try {
            testSliderAnswer.setMaxValue(max / 2);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a lower maxValue than minValue",
            e instanceof AssertionError);
        testSliderAnswer.setMinValue(max / 2);
        testSliderAnswer.setMaxValue(max);
        assertEquals("The getting maxValue was not the expected one", max,
            testSliderAnswer.getMaxValue());
    }

    /**
     * Test of {@link SliderAnswer#setMinMax}.<br> Invalid input: <code>null</code>, min greater
     * than max<br> Valid input: max greater than min
     */
    @Test
    public void testSetMinMax() {
        Throwable e = null;
        try {
            testSliderAnswer.setMinMax(null, random.nextDouble());
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as minValue", e instanceof AssertionError);

        e = null;
        try {
            testSliderAnswer.setMinMax(random.nextDouble(), null);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as maxValue", e instanceof AssertionError);

        Double number = random.nextDouble();
        e = null;
        try {
            testSliderAnswer.setMinMax(number * 2, number / 2);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a lower maxValue than minValue",
            e instanceof AssertionError);
        testSliderAnswer.setMinMax(number / 2, number * 2);
        assertEquals("The getting minValue was not the expected one", (Double) (number / 2),
            testSliderAnswer.getMinValue());
        assertEquals("The getting maxValue was not the expected one", (Double) (number * 2),
            testSliderAnswer.getMaxValue());
    }

    /**
     * Test of {@link SliderAnswer#getStepsize} and {@link SliderAnswer#setStepsize}.<br> Invalid
     * input: <code>null</code>, stepsize less or equal 0<br> Valid input: stepsize greater 0
     */
    @Test
    public void testGetAndSetStepsize() {
        Double testStepsize = null;
        Throwable e = null;
        try {
            testSliderAnswer.setStepsize(testStepsize);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as stepsize", e instanceof AssertionError);

        testStepsize = Math.abs(random.nextDouble() + 1) * -1;
        e = null;
        try {
            testSliderAnswer.setStepsize(testStepsize);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative stepsize", e instanceof AssertionError);
        testStepsize *= -1;
        testSliderAnswer.setStepsize(testStepsize);
        assertEquals("The getting stepsize was not the expected one", testStepsize,
            testSliderAnswer.getStepsize());
    }

    /**
     * Test of {@link SliderAnswer#getLocalizedMinimumText} and
     * {@link SliderAnswer#setLocalizedMinimumText}.<br> Valid input: Map of random Strings
     */
    @Test
    public void testGetAndSetLocalizedMinimumText() {
        Map<String, String> testLocalizedMinimumText = new HashMap<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testLocalizedMinimumText.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(random.nextInt(255)));
        }
        testSliderAnswer.setLocalizedMinimumText(testLocalizedMinimumText);
        assertEquals("The getting localizedMinimumText was not the expected one",
            testLocalizedMinimumText, testSliderAnswer.getLocalizedMinimumText());
    }

    /**
     * Test of {@link SliderAnswer#getLocalizedMaximumText} and
     * {@link SliderAnswer#setLocalizedMaximumText}.<br> Valid input: Map of random Strings
     */
    @Test
    public void testGetAndSetLocalizedMaximumText() {
        Map<String, String> testLocalizedMaximumText = new HashMap<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testLocalizedMaximumText.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(random.nextInt(255)));
        }
        testSliderAnswer.setLocalizedMaximumText(testLocalizedMaximumText);
        assertEquals("The getting localizedMaximumText was not the expected one",
            testLocalizedMaximumText, testSliderAnswer.getLocalizedMaximumText());
    }

    /**
     * Test of {@link SliderAnswer#getShowValueOnButton} and
     * {@link SliderAnswer#setShowValueOnButton}.<br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetShowValueOnButton() {
        Boolean testShowValueOnButton = random.nextBoolean();
        testSliderAnswer.setShowValueOnButton(testShowValueOnButton);
        assertEquals("The getting showValueOnButton was not the expected one",
            testShowValueOnButton, testSliderAnswer.getShowValueOnButton());
    }

    /**
     * Test of {@link SliderAnswer#toString}.<br> Valid input: valid {@link SliderAnswer}
     */
    @Test
    public void testToString() {
        Double min = random.nextDouble() + 1;
        Double max = min * 2;
        Double stepsize = random.nextDouble() + 1;
        String testString = "{min: " + min + ", max: " + max + ", stepsize: " + stepsize + "}";
        testSliderAnswer.setMinMax(min, max);
        testSliderAnswer.setStepsize(stepsize);
        assertEquals("The getting String was not the expected one", testString,
            testSliderAnswer.toString());
    }

    /**
     * Test of {@link SliderAnswer#getVertical} and {@link SliderAnswer#setVertical}.<br> Valid
     * input: random Boolean
     */
    @Test
    public void testGetAndSetVertical() {
        Boolean testVertical = random.nextBoolean();
        testSliderAnswer.setVertical(testVertical);
        assertEquals("The getting showValueOnButton was not the expected one", testVertical,
            testSliderAnswer.getVertical());
    }
}