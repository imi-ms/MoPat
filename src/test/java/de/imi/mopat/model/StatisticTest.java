package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
public class StatisticTest {

    private static final Random random = new Random();
    private Statistic testStatistic;

    public StatisticTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new Statistic
     *
     * @return Returns a valid new Statistic
     */
    public static Statistic getNewValidStatistic() {
        Statistic statistic = new Statistic();

        return statistic;
    }

    @Before
    public void setUp() {
        testStatistic = getNewValidStatistic();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link Statistic#getDate} and {@link Statistic#setDate}.<br> Invalid input:
     * <code>null</code><br> Valid input: random Date
     */
    @Test
    public void testGetAndSetDate() {
        Date testDate = null;
        Throwable e = null;
        try {
            testStatistic.setDate(testDate);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the date", e instanceof AssertionError);

        testDate = new Date(random.nextLong());
        testStatistic.setDate(testDate);
        assertEquals("The getting date was not the expected one", testDate,
            testStatistic.getDate());
    }

    /**
     * Test of {@link Statistic#getQuestionnaireCount} and
     * {@link Statistic#setQuestionnaireCount}.<br> Invalid input: <code>null</code><br> Valid
     * input: random Long
     */
    @Test
    public void testGetAndSetQuestionnaireCount() {
        Long testQuestionnaireCount = null;
        Throwable e = null;
        try {
            testStatistic.setQuestionnaireCount(testQuestionnaireCount);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the questionnaireCount",
            e instanceof AssertionError);

        testQuestionnaireCount = random.nextLong();
        testStatistic.setQuestionnaireCount(testQuestionnaireCount);
        assertEquals("The getting questionnaireCount was not the expected one",
            testQuestionnaireCount, testStatistic.getQuestionnaireCount());
    }

    /**
     * Test of {@link Statistic#getBundleCount} and {@link Statistic#setBundleCount}.<br> Invalid
     * input: <code>null</code><br> Valid input: random Long
     */
    @Test
    public void testGetAndSetBundleCount() {
        Long testBundleCount = null;
        Throwable e = null;
        try {
            testStatistic.setBundleCount(testBundleCount);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the bundleCount", e instanceof AssertionError);

        testBundleCount = random.nextLong();
        testStatistic.setBundleCount(testBundleCount);
        assertEquals("The getting bundleCount was not the expected one", testBundleCount,
            testStatistic.getBundleCount());
    }

    /**
     * Test of {@link Statistic#getClinicCount} and {@link Statistic#setClinicCount}.<br> Invalid
     * input: <code>null</code><br> Valid input: random Long
     */
    @Test
    public void testGetAndSetClinicCount() {
        Long testClinicCount = null;
        Throwable e = null;
        try {
            testStatistic.setClinicCount(testClinicCount);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the clinicCount", e instanceof AssertionError);

        testClinicCount = random.nextLong();
        testStatistic.setClinicCount(testClinicCount);
        assertEquals("The getting clinicCount was not the expected one", testClinicCount,
            testStatistic.getClinicCount());
    }

    /**
     * Test of {@link Statistic#getUserCount} and {@link Statistic#setUserCount}.<br> Invalid
     * input:
     * <code>null</code><br> Valid input: random Long
     */
    @Test
    public void testGetAndSetUserCount() {
        Long testUserCount = null;
        Throwable e = null;
        try {
            testStatistic.setUserCount(testUserCount);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the userCount", e instanceof AssertionError);

        testUserCount = random.nextLong();
        testStatistic.setUserCount(testUserCount);
        assertEquals("The getting userCount was not the expected one", testUserCount,
            testStatistic.getUserCount());
    }

    /**
     * Test of {@link Statistic#} and {@link Statistic#}.<br> Invalid input: <code>null</code><br>
     * Valid input: random Long
     */
    @Test
    public void testGetAndSetEncounterCount() {
        Long testEncounterCount = null;
        Throwable e = null;
        try {
            testStatistic.setEncounterCount(testEncounterCount);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the encounterCount",
            e instanceof AssertionError);

        testEncounterCount = random.nextLong();
        testStatistic.setEncounterCount(testEncounterCount);
        assertEquals("The getting encounterCount was not the expected one", testEncounterCount,
            testStatistic.getEncounterCount());
    }

    /**
     * Test of {@link Statistic#getIncompleteEncounterCount} and
     * {@link Statistic#setIncompleteEncounterCount}.<br> Invalid input: <code>null</code><br> Valid
     * input: random Long
     */
    @Test
    public void testGetAndSetIncompleteEncounterCount() {
        Long testIncompleteEncounterCount = null;
        Throwable e = null;
        try {
            testStatistic.setIncompleteEncounterCount(testIncompleteEncounterCount);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the incompleteEncounterCount",
            e instanceof AssertionError);

        testIncompleteEncounterCount = random.nextLong();
        testStatistic.setIncompleteEncounterCount(testIncompleteEncounterCount);
        assertEquals("The getting incompleteEncounterCount was not the expected one",
            testIncompleteEncounterCount, testStatistic.getIncompleteEncounterCount());
    }

    /**
     * Test of {@link Statistic#getCompleteEncounterDeletedCount} and
     * {@link Statistic#setCompleteEncounterDeletedCount}.<br> Invalid input: <code>null</code><br>
     * Valid input: random Long
     */
    @Test
    public void testGetAndSetCompleteEncounterDeletedCount() {
        Long testCompleteEncounterDeletedCount = null;
        Throwable e = null;
        try {
            testStatistic.setCompleteEncounterDeletedCount(testCompleteEncounterDeletedCount);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the completeEncounterDeletedCount",
            e instanceof AssertionError);

        testCompleteEncounterDeletedCount = random.nextLong();
        testStatistic.setCompleteEncounterDeletedCount(testCompleteEncounterDeletedCount);
        assertEquals("The getting completeEncounterDeletedCount was not the expected one",
            testCompleteEncounterDeletedCount, testStatistic.getCompleteEncounterDeletedCount());
    }

    /**
     * Test of {@link Statistic#getIncompleteEncounterDeletedCount} and
     * {@link Statistic#setIncompleteEncounterDeletedCount}.<br> Invalid input:
     * <code>null</code><br> Valid input: random Long
     */
    @Test
    public void testGetAndSetIncompleteEncounterDeletedCount() {
        Long testIncompleteEncounterDeletedCount = null;
        Throwable e = null;
        try {
            testStatistic.setIncompleteEncounterDeletedCount(testIncompleteEncounterDeletedCount);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the incompleteEncounterDeletedCount",
            e instanceof AssertionError);

        testIncompleteEncounterDeletedCount = random.nextLong();
        testStatistic.setIncompleteEncounterDeletedCount(testIncompleteEncounterDeletedCount);
        assertEquals("The getting incompleteEncounterDeletedCount was not the expected one",
            testIncompleteEncounterDeletedCount,
            testStatistic.getIncompleteEncounterDeletedCount());
    }

    /**
     * Test of {@link Statistic#getODMExportCount} and {@link Statistic#setODMExportCount}.<br>
     * Invalid input: <code>null</code><br> Valid input: random Long
     */
    @Test
    public void testGetAndSetODMExportCount() {
        Long testODMExportCount = null;
        Throwable e = null;
        try {
            testStatistic.setODMExportCount(testODMExportCount);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the ODMExportCount",
            e instanceof AssertionError);

        testODMExportCount = random.nextLong();
        testStatistic.setODMExportCount(testODMExportCount);
        assertEquals("The getting ODMExportCount was not the expected one", testODMExportCount,
            testStatistic.getODMExportCount());
    }

    /**
     * Test of {@link Statistic#getHL7ExportCount} and {@link Statistic#setHL7ExportCount}.<br>
     * Invalid input: <code>null</code><br> Valid input: random Long
     */
    @Test
    public void testGetAndSetHL7ExportCount() {
        Long testHL7ExportCount = null;
        Throwable e = null;
        try {
            testStatistic.setHL7ExportCount(testHL7ExportCount);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the HL7ExportCount",
            e instanceof AssertionError);

        testHL7ExportCount = random.nextLong();
        testStatistic.setHL7ExportCount(testHL7ExportCount);
        assertEquals("The getting HL7ExportCount was not the expected one", testHL7ExportCount,
            testStatistic.getHL7ExportCount());
    }

    /**
     * Test of {@link Statistic#compareTo}.<br>
     */
    @Test
    public void testCompareTo() {
        Statistic compareStatistic = getNewValidStatistic();
        Long testTimeInMillis = random.nextLong();
        testStatistic.setDate(new Date(testTimeInMillis));
        compareStatistic.setDate(new Date(testTimeInMillis + random.nextInt(1000000) + 1));
        assertEquals("Failure in compareTo() method", -1,
            testStatistic.compareTo(compareStatistic));
        assertEquals("Failure in compareTo() method", 0, testStatistic.compareTo(testStatistic));
        assertEquals("Failure in compareTo() method", 1, compareStatistic.compareTo(testStatistic));
    }
}
