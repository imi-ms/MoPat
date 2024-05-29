package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FreetextAnswerTest {

    private static final Random random = new Random();
    private FreetextAnswer testFreetextAnswer;

    public FreetextAnswerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new FreetextAnswer
     *
     * @return Returns a valid new FreetextAnswer
     */
    public static FreetextAnswer getNewValidFreetextAnswer() {
        Question question = QuestionTest.getNewValidQuestion();
        Boolean isEnabled = random.nextBoolean();

        FreetextAnswer freetextAnswer = new FreetextAnswer(question, isEnabled);

        return freetextAnswer;
    }

    @Before
    public void setUp() {
        testFreetextAnswer = getNewValidFreetextAnswer();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link FreetextAnswer#cloneWithoutReferences}.<br> Valid input: valid
     * {@link FreetextAnswer}
     */
    @Test
    public void testCloneWithoutReferences() {
        FreetextAnswer testClone = testFreetextAnswer.cloneWithoutReferences();
        assertEquals("The getting isEnabled was not the expected one",
            testFreetextAnswer.getIsEnabled(), testClone.getIsEnabled());
    }
}
