package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.utils.Helper;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

public class SliderFreetextAnswerTest {

    private static final Random random = new Random();
    private SliderFreetextAnswer testSliderFreetextAnswer;

    /**
     * Returns a valid new SliderFreetextAnswer
     *
     * @return Returns a valid new SliderFreetextAnswer
     */
    public static SliderFreetextAnswer getNewValidSliderFreetextAnswer() {
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
        Map<String, String> localizedFreetextLabel = new HashMap<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            localizedFreetextLabel.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(random.nextInt(255)));
        }

        return new SliderFreetextAnswer(question, isEnabled, min, max, stepsize,
            localizedFreetextLabel, vertical);
    }

    @Before
    public void setUp() {
        testSliderFreetextAnswer = getNewValidSliderFreetextAnswer();
    }

    /**
     * Test of {@link SliderFreetextAnswer#cloneWithoutReferences}.<br> Valid input: valid
     * {@link SliderFreetextAnswer}
     */
    @Test
    public void testCloneWithoutReferences() {
        testSliderFreetextAnswer.setShowValueOnButton(random.nextBoolean());
        SliderFreetextAnswer testClone = testSliderFreetextAnswer.cloneWithoutReferences();
        assertEquals("The getting isEnabled was not the expected one",
            testSliderFreetextAnswer.getIsEnabled(), testClone.getIsEnabled());
        assertEquals("The getting minValue was not the expected one",
            testSliderFreetextAnswer.getMinValue(), testClone.getMinValue());
        assertEquals("The getting maxValue was not the expected one",
            testSliderFreetextAnswer.getMaxValue(), testClone.getMaxValue());
        assertEquals("The getting stepsize was not the expected one",
            testSliderFreetextAnswer.getStepsize(), testClone.getStepsize());
        assertEquals("The getting vertical was not the expected one",
            testSliderFreetextAnswer.getVertical(), testClone.getVertical());
        assertEquals("The getting showValueOnButton was not the expected one",
            testSliderFreetextAnswer.getShowValueOnButton(), testClone.getShowValueOnButton());
        assertEquals("The getting map of localizedFreetextLabel was not the expected one",
            testSliderFreetextAnswer.getLocalizedFreetextLabel(),
            testClone.getLocalizedFreetextLabel());
        assertNull("The getting map of localizedMinimumText was not the expected one",
            testSliderFreetextAnswer.getLocalizedMinimumText());
        assertNull("The getting map of localizedMaximumText was not the expected one",
            testSliderFreetextAnswer.getLocalizedMaximumText());

        Map<String, String> testLocalizedMinimumText = new HashMap<>();
        Map<String, String> testLocalizedMaximumText = new HashMap<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testLocalizedMinimumText.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(255));
            testLocalizedMaximumText.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(255));
        }
        testSliderFreetextAnswer.setLocalizedMinimumText(testLocalizedMinimumText);
        testSliderFreetextAnswer.setLocalizedMaximumText(testLocalizedMaximumText);
        testClone = testSliderFreetextAnswer.cloneWithoutReferences();
        assertEquals("The getting map of localizedMinimumText was not the expected one",
            testSliderFreetextAnswer.getLocalizedMinimumText(),
            testClone.getLocalizedMinimumText());
        assertEquals("The getting map of localizedMaximumText was not the expected one",
            testSliderFreetextAnswer.getLocalizedMaximumText(),
            testClone.getLocalizedMaximumText());
    }

    /**
     * Test of {@link SliderFreetextAnswer#getLocalizedFreetextLabel} and
     * {@link SliderFreetextAnswer#setLocalizedFreetextLabel}.<br> Invalid input: <code>null</code>,
     * empty map<br> Valid input: not empty map
     */
    @Test
    public void testGetAndSetLocalizedFreetextLabel() {
        Map<String, String> testLocalizedFreetextLabel = null;
        Throwable e = null;
        try {
            testSliderFreetextAnswer.setLocalizedFreetextLabel(testLocalizedFreetextLabel);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a null as the localizedFreetextLabel",
            e instanceof AssertionError);

        testLocalizedFreetextLabel = new HashMap<>();
        e = null;
        try {
            testSliderFreetextAnswer.setLocalizedFreetextLabel(testLocalizedFreetextLabel);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set an empty map as the localizedFreetextLabel",
            e instanceof AssertionError);

        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testLocalizedFreetextLabel.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(255));
        }
        testSliderFreetextAnswer.setLocalizedFreetextLabel(testLocalizedFreetextLabel);
        assertEquals("The getting localizedFreetextlabel was not the expected one",
            testLocalizedFreetextLabel, testSliderFreetextAnswer.getLocalizedFreetextLabel());
    }

    /**
     * Test of {@link SliderFreetextAnswer#toString}.<br> Valid input: valid
     * {@link SliderFreetextAnswer}
     */
    @Test
    public void testToString() {
        Double min = random.nextDouble() + 1;
        Double max = min * 2;
        Double stepsize = random.nextDouble() + 1;
        String testString = "{min: " + min + ", max: " + max + ", stepsize: " + stepsize + "}";
        testSliderFreetextAnswer.setMinMax(min, max);
        testSliderFreetextAnswer.setStepsize(stepsize);
        assertEquals("The getting String was not the expected one", testString,
            testSliderFreetextAnswer.toString());
    }
}
