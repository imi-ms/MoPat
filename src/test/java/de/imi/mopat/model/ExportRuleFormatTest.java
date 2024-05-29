package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.model.enumeration.ExportDecimalDelimiterType;
import de.imi.mopat.model.enumeration.ExportNumberType;
import de.imi.mopat.model.enumeration.ExportRoundingStrategyType;
import de.imi.mopat.utils.Helper;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class ExportRuleFormatTest {

    private final Random random = new Random();
    private ExportRuleFormat testExportRuleFormat;

    public ExportRuleFormatTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new ExportRuleFormat
     *
     * @return Returns a valid new ExportRuleFormat
     */
    public static ExportRuleFormat getNewValidExportRuleFormat() {
        ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
        return exportRuleFormat;
    }

    @Before
    public void setUp() {
        testExportRuleFormat = getNewValidExportRuleFormat();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ExportRuleFormat#getNumberType} and
     * {@link ExportRuleFormat#setNumberType}.<br> Valid input: random {@link ExportNumberType}
     */
    @Test
    public void testGetAndSetNumberType() {
        ExportNumberType testExportNumberType = Helper.getRandomEnum(ExportNumberType.class);
        testExportRuleFormat.setNumberType(testExportNumberType);
        assertEquals("The getting ExportNumberType was not the expected one", testExportNumberType,
            testExportRuleFormat.getNumberType());
    }

    /**
     * Test of {@link ExportRuleFormat#getRoundingStrategy} and
     * {@link ExportRuleFormat#setRoundingStrategy}.<br> Valid input: random
     * {@link ExportRoundingStrategyType}
     */
    @Test
    public void testGetAndSetRoundingStrategy() {
        ExportRoundingStrategyType testRoundingStrategy = Helper.getRandomEnum(
            ExportRoundingStrategyType.class);
        testExportRuleFormat.setRoundingStrategy(testRoundingStrategy);
        assertEquals("The getting ExportRoundingStrategyType was not the expected one",
            testRoundingStrategy, testExportRuleFormat.getRoundingStrategy());
    }

    /**
     * Test of {@link ExportRuleFormat#getDecimalPlaces} and
     * {@link ExportRuleFormat#setDecimalPlaces}.<br> Invalid input: <code>null</code>, negative
     * value<br> Valid input: positive value
     */
    @Test
    public void testGetAndSetDecimalPlaces() {
        Integer testDecimalPlaces = null;
        Throwable e = null;
        try {
            testExportRuleFormat.setDecimalPlaces(testDecimalPlaces);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the decimalPlaces as null", e instanceof AssertionError);

        testDecimalPlaces = Math.abs(random.nextInt() + 1) * -1;
        e = null;
        try {
            testExportRuleFormat.setDecimalPlaces(testDecimalPlaces);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set negative decimalPlaces", e instanceof AssertionError);

        testDecimalPlaces *= -1;
        testExportRuleFormat.setDecimalPlaces(testDecimalPlaces);
        assertEquals("The getting decimalPlaces was not the expected one", testDecimalPlaces,
            testExportRuleFormat.getDecimalPlaces());
    }

    /**
     * Test of {@link ExportRuleFormat#getDecimalDelimiter} and
     * {@link ExportRuleFormat#setDecimalDelimiter}.<br> Valid input: random
     * {@link ExportDecimalDelimiterType}
     */
    @Test
    public void testGetAndSetDecimalDelimiter() {
        ExportDecimalDelimiterType testDecimalDelimiter = Helper.getRandomEnum(
            ExportDecimalDelimiterType.class);
        testExportRuleFormat.setDecimalDelimiter(testDecimalDelimiter);
        assertEquals("The getting ExportDecimalDelimiterType was not the expected one",
            testDecimalDelimiter, testExportRuleFormat.getDecimalDelimiter());
    }

    /**
     * Test of {@link ExportRuleFormat#getDateFormat} and
     * {@link ExportRuleFormat#setDateFormat}.<br> Valid input: random {@link ExportDateFormatType}
     */
    @Test
    public void testGetAndSetDateFormat() {
        ExportDateFormatType testDateFormat = Helper.getRandomEnum(ExportDateFormatType.class);
        testExportRuleFormat.setDateFormat(testDateFormat);
        assertEquals("The getting ExportDateFormatType was not the expected one", testDateFormat,
            testExportRuleFormat.getDateFormat());
    }

    /**
     * Test of {@link ExportRuleFormat#getExportRules} and
     * {@link ExportRuleFormat#addExportRules}.<br> Invalid input: <code>null</code><br> Valid
     * input: random number of {@link ExportRule ExportRules}
     */
    @Test
    public void testGetAndAddExportRules() {
        Set<ExportRule> testSet = null;
        Throwable e = null;
        try {
            testExportRuleFormat.addExportRules(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the ExportRules", e instanceof AssertionError);

        testSet = new HashSet<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testSet.add(ExportRuleTest.getNewValidRandomExportRule());
        }
        testExportRuleFormat.addExportRules(testSet);
        assertEquals("The getting ExportRules was not the expected one", testSet,
            testExportRuleFormat.getExportRules());
    }

    /**
     * Test of {@link ExportRuleFormat#addExportRule}.<br> Invalid input: <code>null</code><br>
     * Valid input: random number of {@link ExportRule ExportRules}
     */
    @Test
    public void testAddExportRule() {
        ExportRule testExportRule = null;
        Throwable e = null;
        try {
            testExportRuleFormat.addExportRule(testExportRule);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the ExportRules", e instanceof AssertionError);

        Set<ExportRule> testSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testExportRule = ExportRuleTest.getNewValidRandomExportRule();
            testExportRuleFormat.addExportRule(testExportRule);
            testSet.add(testExportRule);
        }
        // Add the last ExportRule again
        testExportRuleFormat.addExportRule(testExportRule);
        // Add an ExportRule with another ExportRuleFormat
        testExportRule = ExportRuleTest.getNewValidRandomExportRule();
        getNewValidExportRuleFormat().addExportRule(testExportRule);
        testExportRuleFormat.addExportRule(testExportRule);
        testSet.add(testExportRule);

        assertEquals("The getting ExportRules was not the expected one", testSet,
            testExportRuleFormat.getExportRules());
    }

    /**
     * Test of {@link ExportRuleFormat#removeExportRule}.<br> Invalid input: <code>null</code><br>
     * Valid input: random number of {@link ExportRule ExportRules}
     */
    @Test
    public void testRemoveExportRule() {
        ExportRule testExportRule = null;
        Throwable e = null;
        try {
            testExportRuleFormat.removeExportRule(testExportRule);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the ExportRules",
            e instanceof AssertionError);

        Set<ExportRule> testSet = new HashSet<>();
        Set<ExportRule> removeSet = new HashSet<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testExportRule = ExportRuleTest.getNewValidRandomExportRule();
            testExportRuleFormat.addExportRule(testExportRule);
            testSet.add(testExportRule);
            if (random.nextBoolean()) {
                removeSet.add(testExportRule);
            }
        }
        assertEquals("The getting ExportRules was not the expected one", testSet,
            testExportRuleFormat.getExportRules());

        for (ExportRule exportRule : removeSet) {
            testExportRuleFormat.removeExportRule(exportRule);
        }
        testSet.removeAll(removeSet);
        assertEquals("The getting set of ExportRules after removing was not the expected one",
            testSet, testExportRuleFormat.getExportRules());

        // Try to remove an ExportRule which is not associated
        testExportRule = ExportRuleTest.getNewValidRandomExportRule();
        getNewValidExportRuleFormat().addExportRule(testExportRule);
        testExportRuleFormat.removeExportRule(testExportRule);
        assertEquals(
            "The getting set of ExportRules after removing a not associated ExportRule was not the expected one",
            testSet, testExportRuleFormat.getExportRules());
    }

    /**
     * Test of {@link ExportRuleFormat#equals}.<br> Invalid input: one {@link ExportRuleFormat}
     * twice in a HashSet
     */
    @Test
    public void testEquals() {
        Set<ExportRuleFormat> testSet = new HashSet<>();
        testSet.add(testExportRuleFormat);
        testSet.add(testExportRuleFormat);
        assertEquals("It was possible to set the same ExportRuleFormat twice in one set", 1,
            testSet.size());

        assertEquals("The ExportRuleFormat was not equal to itself", testExportRuleFormat,
            testExportRuleFormat);
        assertNotEquals("The ExportRuleFormat was equal to null", null, testExportRuleFormat);
        ExportRuleFormat otherExportRuleFormat = getNewValidExportRuleFormat();
        assertNotEquals("The ExportRuleFormat was equal to a different ExportRuleFormat",
            testExportRuleFormat, otherExportRuleFormat);
        Object otherObject = new Object();
        assertNotEquals("The ExportRuleFormat was equal to a different Object",
            testExportRuleFormat, otherObject);
    }
}
