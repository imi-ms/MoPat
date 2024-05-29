package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class DateAnswerTest {

    private static final Random random = new Random();
    private DateAnswer testDateAnswer;

    public DateAnswerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new DateAnswer
     *
     * @return Returns a valid new DateAnswer
     */
    public static DateAnswer getNewValidDateAnswer() {
        Question question = QuestionTest.getNewValidQuestion();
        Boolean isEnabled = random.nextBoolean();
        Date startDate = new Date(Math.abs(System.currentTimeMillis() - random.nextLong()));
        Date endDate = new Date(Math.abs(System.currentTimeMillis() - random.nextLong()));

        DateAnswer dateAnswer = new DateAnswer(question, isEnabled, startDate, endDate);

        return dateAnswer;
    }

    public static DateAnswer getNewValidDateAnswer(Date startDate, Date endDate) {
        Question question = QuestionTest.getNewValidQuestion();
        Boolean isEnabled = random.nextBoolean();
        DateAnswer dateAnswer = new DateAnswer(question, isEnabled, startDate, endDate);

        return dateAnswer;
    }

    @Before
    public void setUp() {
        testDateAnswer = getNewValidDateAnswer();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link DateAnswer#cloneWithoutReferences}.<br> Valid input: valid {@link DateAnswer}
     */
    @Test
    public void testCloneWithoutReferences() {
        DateAnswer testClone = testDateAnswer.cloneWithoutReferences();
        assertEquals("The getting isEnabled was not the expected one",
            testDateAnswer.getIsEnabled(), testClone.getIsEnabled());
        assertEquals("The getting StartDate was not the expected one",
            testDateAnswer.getStartDate(), testClone.getStartDate());
        assertEquals("The getting EndDate was not the expected one", testDateAnswer.getEndDate(),
            testClone.getEndDate());
    }

    /**
     * Test of {@link DateAnswer#getStartDate} and {@link DateAnswer#setStartDate}.<br> Valid input:
     * valid {@link Date}
     */
    @Test
    public void testGetAndSetStartDate() {
        Date testDate = new Date(Math.abs(System.currentTimeMillis() - random.nextLong()));
        testDateAnswer.setStartDate(testDate);
        assertEquals("The getting StartDate was not the expected one", testDate,
            testDateAnswer.getStartDate());
    }

    /**
     * Test of {@link DateAnswer#getEndDate} and {@link DateAnswer#setEndDate}.<br> Valid input:
     * valid {@link Date}
     */
    @Test
    public void testGetAndSetEndDate() {
        Date testDate = new Date(Math.abs(System.currentTimeMillis() - random.nextLong()));
        testDateAnswer.setEndDate(testDate);
        assertEquals("The getting EndDate was not the expected one", testDate,
            testDateAnswer.getEndDate());
    }
}
