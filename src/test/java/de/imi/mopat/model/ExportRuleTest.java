package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.utils.Helper;
import java.util.HashSet;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class ExportRuleTest {

    private static final Random random = new Random();
    private ExportRule testExportRule;

    public ExportRuleTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new ExportRule
     *
     * @return Returns a valid new ExportRule
     */
    public static ExportRule getNewValidRandomExportRule() {
        switch (random.nextInt(3)) {
            case 0:
                return ExportRuleAnswerTest.getNewValidExportRuleAnswer();
            case 1:
                return ExportRuleEncounterTest.getNewValidExportRuleEncounter();
            default:
                return ExportRuleScoreTest.getNewValidExportRuleScore();
        }
    }

    @Before
    public void setUp() {
        testExportRule = getNewValidRandomExportRule();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ExportRule#getExportField} and {@link ExportRule#setExportField}.<br> Invalid
     * input: <code>null</code><br> Valid input: random String
     */
    @Test
    public void testGetAndSetExportField() {
        String testExportField = null;
        Throwable e = null;
        try {
            testExportRule.setExportField(testExportField);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the ExportField", e instanceof AssertionError);

        testExportField = Helper.getRandomString(random.nextInt(50) + 1);
        testExportRule.setExportField(testExportField);
        assertEquals("The getting ExportField was not the expected one", testExportField,
            testExportRule.getExportField());
    }

    /**
     * Test of {@link ExportRule#getExportTemplate} and {@link ExportRule#setExportTemplate}.<br>
     * Invalid input: <code>null</code><br> Valid input: random {@link ExportTemplate}
     */
    @Test
    public void testGetAndSetExportTemplate() {
        ExportTemplate testExportTemplate = null;
        Throwable e = null;
        try {
            testExportRule.setExportTemplate(testExportTemplate);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the ExportTemplate",
            e instanceof AssertionError);

        testExportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        testExportRule.setExportTemplate(testExportTemplate);
        assertEquals("The getting ExportTemplate was not the expected one", testExportTemplate,
            testExportRule.getExportTemplate());
    }

    /**
     * Test of {@link ExportRule#getExportRuleFormat} and
     * {@link ExportRule#setExportRuleFormat}.<br> Valid input: random {@link ExportRuleFormat}
     */
    @Test
    public void testGetAndSetExportRuleFormat() {
        ExportRuleFormat testExportRuleFormat = ExportRuleFormatTest.getNewValidExportRuleFormat();
        testExportRule.setExportRuleFormat(testExportRuleFormat);
        assertEquals("The getting ExportRuleFormat was not the expected one", testExportRuleFormat,
            testExportRule.getExportRuleFormat());
    }

    /**
     * Test of {@link ExportRule#removeExportRuleFormat}.<br> Valid input: random
     * {@link ExportRuleFormat}
     */
    @Test
    public void testRemoveExportRuleFormat() {
        testExportRule.setExportRuleFormat(ExportRuleFormatTest.getNewValidExportRuleFormat());
        assertNotNull("The ExportRuleFormat was null after setting it",
            testExportRule.getExportRuleFormat());
        testExportRule.removeExportRuleFormat();
        assertNull("The ExportRuleFormat was not null after removing it",
            testExportRule.getExportRuleFormat());
        testExportRule.removeExportRuleFormat();
        assertNull("The ExportRuleFormat was not null after removing it again",
            testExportRule.getExportRuleFormat());
    }

    /**
     * Test of {@link ExportRule#removeExportTemplate}.<br> Valid input: random
     * {@link ExportTemplate}
     */
    @Test
    public void testRemoveExportTemplate() {
        testExportRule.setExportTemplate(ExportTemplateTest.getNewValidExportTemplate());
        assertNotNull("The ExportTemplate was null after setting it",
            testExportRule.getExportTemplate());
        testExportRule.removeExportTemplate();
        assertNull("The ExportTemplate was not null after removing it",
            testExportRule.getExportTemplate());
        testExportRule.removeExportTemplate();
        assertNull("The ExportTemplate was not null after removing it again",
            testExportRule.getExportTemplate());
    }

    /**
     * Test of {@link ExportRule#equals}.<br> Invalid input: one {@link ExportRule} twice in a
     * HashSet
     */
    @Test
    public void testEquals() {
        HashSet<ExportRule> testSet = new HashSet<>();
        testSet.add(testExportRule);
        testSet.add(testExportRule);
        assertEquals("It was possible to set the same ExportRule twice in one set", 1,
            testSet.size());

        assertEquals("The ExportRule was not equal to itself", testExportRule, testExportRule);
        assertNotEquals("The ExportRule was equal to null", null, testExportRule);
        ExportRule otherExportRule = getNewValidRandomExportRule();
        assertNotEquals("The ExportRule was equal to a different ExportRule", testExportRule,
            otherExportRule);
        Object otherObject = new Object();
        assertNotEquals("The ExportRule was equal to a different Object", testExportRule,
            otherObject);
    }
}
