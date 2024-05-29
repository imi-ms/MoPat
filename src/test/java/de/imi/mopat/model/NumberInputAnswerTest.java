package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class NumberInputAnswerTest {

    private static final Random random = new Random();
    private NumberInputAnswer testNumberInputAnswer;

    public NumberInputAnswerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new NumberInputAnswer
     *
     * @return Returns a valid new NumberInputAnswer
     */
    public static NumberInputAnswer getNewValidNumberInputAnswer() {
        Question question = QuestionTest.getNewValidQuestion();
        Boolean isEnabled = random.nextBoolean();
        Double min = Math.abs(random.nextDouble());
        Double max;
        do {
            max = Math.abs(random.nextDouble());
        } while (max < min);
        Double stepsize = Math.abs(random.nextDouble());

        NumberInputAnswer numberInputAnswer = new NumberInputAnswer(question, isEnabled, min, max,
            stepsize);

        return numberInputAnswer;
    }

    @Before
    public void setUp() {
        testNumberInputAnswer = getNewValidNumberInputAnswer();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link NumberInputAnswer#cloneWithoutReferences}.<br> Valid input: valid
     * {@link NumberInputAnswer}
     */
    @Test
    public void testCloneWithoutReferences() {
        NumberInputAnswer testClone = testNumberInputAnswer.cloneWithoutReferences();
        assertEquals("The getting isEnabled was not the expected one",
            testNumberInputAnswer.getIsEnabled(), testClone.getIsEnabled());
        assertEquals("The getting minValue was not the expected one",
            testNumberInputAnswer.getMinValue(), testClone.getMinValue());
        assertEquals("The getting maxValue was not the expected one",
            testNumberInputAnswer.getMaxValue(), testClone.getMaxValue());
        assertEquals("The getting stepsize was not the expected one",
            testNumberInputAnswer.getStepsize(), testClone.getStepsize());
    }

    /**
     * Test of {@link NumberInputAnswer#getMinValue} and {@link NumberInputAnswer#setMinValue}.<br>
     * Invalid input: min greater than max<br> Valid input: min lower than max
     */
    @Test
    public void testGetAndSetMinValue() {
        Double min = random.nextDouble();
        testNumberInputAnswer.setMinMax(null, null);
        Throwable e = null;
        try {
            testNumberInputAnswer.setMaxValue(min / 2);
            testNumberInputAnswer.setMinValue(min);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a greater minValue than maxValue",
            e instanceof AssertionError);

        testNumberInputAnswer.setMaxValue(min * 2);
        testNumberInputAnswer.setMinValue(min);
        assertEquals("The getting minValue was not the expected one", min,
            testNumberInputAnswer.getMinValue());

        min = random.nextDouble();
        testNumberInputAnswer.setMaxValue(null);
        testNumberInputAnswer.setMinValue(min);
        assertEquals("The getting minValue was not the expected one", min,
            testNumberInputAnswer.getMinValue());
    }

    /**
     * Test of {@link NumberInputAnswer#getMaxValue} and {@link NumberInputAnswer#setMaxValue}.<br>
     * Invalid input: max lower than min<br> Valid input: max greater than min
     */
    @Test
    public void testGetAndSetMaxValue() {
        Double max = random.nextDouble();
        testNumberInputAnswer.setMinMax(null, null);
        Throwable e = null;
        try {
            testNumberInputAnswer.setMinValue(max * 2);
            testNumberInputAnswer.setMaxValue(max);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a lower maxValue than minValue",
            e instanceof AssertionError);

        testNumberInputAnswer.setMinValue(max / 2);
        testNumberInputAnswer.setMaxValue(max);
        assertEquals("The getting maxValue was not the expected one", max,
            testNumberInputAnswer.getMaxValue());

        max = random.nextDouble();
        testNumberInputAnswer.setMinValue(null);
        testNumberInputAnswer.setMaxValue(max);
        assertEquals("The getting maxValue was not the expected one", max,
            testNumberInputAnswer.getMaxValue());
    }

    /**
     * Test of {@link NumberInputAnswer#setMinMax}.<br> Invalid input: max lower than min<br> Valid
     * input: max greater than min
     */
    @Test
    public void testSetMinMax() {
        Double number = random.nextDouble();
        testNumberInputAnswer.setMinMax(null, null);
        Throwable e = null;
        try {
            testNumberInputAnswer.setMinMax(number * 2, number / 2);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a lower maxValue than minValue",
            e instanceof AssertionError);

        number = random.nextDouble();
        testNumberInputAnswer.setMinMax(number / 2, null);
        assertEquals("The getting minValue was not the expected one", (Double) (number / 2),
            testNumberInputAnswer.getMinValue());
        assertNull("The getting maxValue was not null after setting it so",
            testNumberInputAnswer.getMaxValue());

        number = random.nextDouble();
        testNumberInputAnswer.setMinMax(null, number * 2);
        assertEquals("The getting minValue was not the expected one", (Double) (number * 2),
            testNumberInputAnswer.getMaxValue());
        assertNull("The getting maxValue was not null after setting it so",
            testNumberInputAnswer.getMinValue());

        number = random.nextDouble();
        testNumberInputAnswer.setMinMax(number / 2, number * 2);
        assertEquals("The getting minValue was not the expected one", (Double) (number / 2),
            testNumberInputAnswer.getMinValue());
        assertEquals("The getting maxValue was not the expected one", (Double) (number * 2),
            testNumberInputAnswer.getMaxValue());
    }

    /**
     * Test of {@link NumberInputAnswer#getStepsize} and {@link NumberInputAnswer#setStepsize}.<br>
     * Invalid input: negative stepsize<br> Valid input: positive stepsize, <code>null</code>
     */
    @Test
    public void testGetAndSetStepsize() {
        testNumberInputAnswer.setStepsize(null);
        assertNull("The getting stepsize was not null after setting it so",
            testNumberInputAnswer.getStepsize());

        Double testStepsize = Math.abs(random.nextDouble() + 1) * -1;
        Throwable e = null;
        try {
            testNumberInputAnswer.setStepsize(testStepsize);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative stepsize", e instanceof AssertionError);

        testStepsize *= -1;
        testNumberInputAnswer.setStepsize(testStepsize);
        assertEquals("The getting stepsize was not the expected one", testStepsize,
            testNumberInputAnswer.getStepsize());
    }
}
