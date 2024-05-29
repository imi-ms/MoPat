package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
public class ExportRuleAnswerTest {

    private static final Random random = new Random();
    private ExportRuleAnswer testExportRuleAnswer;

    public ExportRuleAnswerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new ExportRuleAnswer
     *
     * @return Returns a valid new ExportRuleAnswer
     */
    public static ExportRuleAnswer getNewValidExportRuleAnswer() {
        ExportTemplate exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        String exportField = Helper.getRandomString(random.nextInt(50) + 1);
        Answer answer = AnswerTest.getNewValidRandomAnswer();

        ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate, exportField,
            answer);

        return exportRuleAnswer;
    }

    /**
     * Returns a valid new ExportRuleAnswer
     *
     * @param answer {@link Answer} of this ExportRule
     * @return Returns a valid new ExportRuleAnswer
     */
    public static ExportRuleAnswer getNewValidExportRuleAnswer(Answer answer) {
        ExportTemplate exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        String exportField = Helper.getRandomString(random.nextInt(50) + 1);

        ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate, exportField,
            answer);

        return exportRuleAnswer;
    }

    /**
     * Returns a valid new ExportRuleAnswer
     *
     * @param exportTemplate {@link ExportTemplate} of this ExportRule
     * @return Returns a valid new ExportRuleAnswer
     */
    public static ExportRuleAnswer getNewValidExportRuleAnswer(ExportTemplate exportTemplate) {
        String exportField = Helper.getRandomString(random.nextInt(50) + 1);
        Answer answer = AnswerTest.getNewValidRandomAnswer();

        ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate, exportField,
            answer);

        return exportRuleAnswer;
    }

    /**
     * Returns a valid new ExportRuleAnswer
     *
     * @param answer           {@link Answer} of this ExportRule
     * @param useFreetextValue {@link Boolean}if the Freetext should be used or not
     * @return Returns a valid new ExportRuleAnswer
     */
    public static ExportRuleAnswer getNewValidExportRuleAnswer(Answer answer,
        boolean useFreetextValue) {
        ExportTemplate exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        String exportField = Helper.getRandomString(random.nextInt(50) + 1);

        ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate, exportField,
            answer, useFreetextValue);

        return exportRuleAnswer;
    }

    @Before
    public void setUp() {
        testExportRuleAnswer = getNewValidExportRuleAnswer();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of
     * {@link ExportRuleAnswer#ExportRuleAnswer(de.imi.mopat.model.ExportTemplate, java.lang.String,
     * de.imi.mopat.model.Answer)} and
     * {@link ExportRuleAnswer#ExportRuleAnswer(de.imi.mopat.model.ExportTemplate, java.lang.String,
     * de.imi.mopat.model.Answer, java.lang.Boolean)}.<br> Valid input: random
     * {@link ExportTemplate}, ExportField as {@link String}, {@link Answer}, use freetext value as
     * {@link Boolean}
     */
    @Test
    public void testConstructor() {
        ExportTemplate testExportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        String testExportField = Helper.getRandomString(random.nextInt(50) + 1);
        Answer testAnswer = AnswerTest.getNewValidRandomAnswer();
        boolean testUseFreetextValue = random.nextBoolean();

        testExportRuleAnswer = new ExportRuleAnswer(testExportTemplate, testExportField,
            testAnswer);
        assertEquals("The getting ExportTemplate was not the expected one", testExportTemplate,
            testExportRuleAnswer.getExportTemplate());
        assertEquals("The getting ExportField was not the expected one", testExportField,
            testExportRuleAnswer.getExportField());
        assertEquals("The getting Answer was not the expected one", testAnswer,
            testExportRuleAnswer.getAnswer());

        testExportRuleAnswer = new ExportRuleAnswer(testExportTemplate, testExportField, testAnswer,
            testUseFreetextValue);
        assertEquals("The getting ExportTemplate was not the expected one", testExportTemplate,
            testExportRuleAnswer.getExportTemplate());
        assertEquals("The getting ExportField was not the expected one", testExportField,
            testExportRuleAnswer.getExportField());
        assertEquals("The getting Answer was not the expected one", testAnswer,
            testExportRuleAnswer.getAnswer());
        assertEquals("The getting useFreetextValue was not the expected one", testUseFreetextValue,
            testExportRuleAnswer.getUseFreetextValue());
    }

    /**
     * Test of {@link ExportRuleAnswer#getAnswer} and {@link ExportRuleAnswer#setAnswer}.<br> Valid
     * input: random {@link Answer}
     */
    @Test
    public void testGetAndSetAnswer() {
        Answer testAnswer = AnswerTest.getNewValidRandomAnswer();
        testExportRuleAnswer.setAnswer(testAnswer);
        assertEquals("The getting answer was not the expected one", testAnswer,
            testExportRuleAnswer.getAnswer());
    }

    /**
     * Test of {@link ExportRuleAnswer#removeAnswer}.<br> Valid input: random {@link Answer}
     */
    @Test
    public void testRemoveAnswer() {
        testExportRuleAnswer.setAnswer(AnswerTest.getNewValidRandomAnswer());
        assertNotNull("The answer was null after setting it", testExportRuleAnswer.getAnswer());
        testExportRuleAnswer.removeAnswer();
        assertNull("The answer was not null after removing", testExportRuleAnswer.getAnswer());
    }

    /**
     * Test of {@link ExportRuleAnswer#getUseFreetextValue} and
     * {@link ExportRuleAnswer#setUseFreetextValue}.<br> Invalid input: <code>null</code><br> Valid
     * input: random Boolean
     */
    @Test
    public void testGetAndSetUseFreetextValue() {
        Boolean testUseFreetextValue = null;
        Throwable e = null;
        try {
            testExportRuleAnswer.setUseFreetextValue(testUseFreetextValue);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set useFreetextValue as null", e instanceof AssertionError);

        testUseFreetextValue = random.nextBoolean();
        testExportRuleAnswer.setUseFreetextValue(testUseFreetextValue);
        assertEquals("The getting useFreetextValue was not the expected one", testUseFreetextValue,
            testExportRuleAnswer.getUseFreetextValue());
    }
}
