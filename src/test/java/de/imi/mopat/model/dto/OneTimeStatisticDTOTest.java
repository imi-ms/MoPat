package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.utils.Helper;
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
public class OneTimeStatisticDTOTest {

    private static final Random random = new Random();
    private OneTimeStatisticDTO testOneTimeStatisticDTO;

    public OneTimeStatisticDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testOneTimeStatisticDTO = new OneTimeStatisticDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link OneTimeStatisticDTO#OneTimeStatisticDTO(java.util.Date, java.util.Date)}.<br>
     * Valid input: random start- and enddate
     */
    @Test
    public void testConstructor() {
        Date testStartDate = new Date(random.nextLong());
        Date testEndDate = new Date(random.nextLong());
        testOneTimeStatisticDTO = new OneTimeStatisticDTO(testStartDate, testEndDate);
        assertEquals("The getting BundleStartDate was not the expected one", testStartDate,
            testOneTimeStatisticDTO.getBundleStartDate());
        assertEquals("The getting BundleEndDate was not the expected one", testEndDate,
            testOneTimeStatisticDTO.getBundleEndDate());
        assertEquals("The getting PatientStartDate was not the expected one", testStartDate,
            testOneTimeStatisticDTO.getPatientStartDate());
        assertEquals("The getting PatientEndDate was not the expected one", testEndDate,
            testOneTimeStatisticDTO.getPatientEndDate());
        assertEquals("The getting BundlePatientStartDate was not the expected one", testStartDate,
            testOneTimeStatisticDTO.getBundlePatientStartDate());
        assertEquals("The getting BundlePatientEndDate was not the expected one", testEndDate,
            testOneTimeStatisticDTO.getBundlePatientEndDate());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getBundleId} and
     * {@link OneTimeStatisticDTO#setBundleId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetBundleId() {
        Long testBundleID = Math.abs(random.nextLong());
        testOneTimeStatisticDTO.setBundleId(testBundleID);
        assertEquals("The getting BundleID was not the expected one", testBundleID,
            testOneTimeStatisticDTO.getBundleId());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getBundleStartDate} and
     * {@link OneTimeStatisticDTO#setBundleStartDate}.<br> Valid input: random Date
     */
    @Test
    public void testGetAndSetBundleStartDate() {
        Date testBundleStartDate = new Date(random.nextLong());
        testOneTimeStatisticDTO.setBundleStartDate(testBundleStartDate);
        assertEquals("The getting BundleStartDate was not the expected one", testBundleStartDate,
            testOneTimeStatisticDTO.getBundleStartDate());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getBundleEndDate} and
     * {@link OneTimeStatisticDTO#setBundleEndDate}.<br> Valid input: random Date
     */
    @Test
    public void testGetAndSetBundleEndDate() {
        Date testBundleEndDate = new Date(random.nextLong());
        testOneTimeStatisticDTO.setBundleEndDate(testBundleEndDate);
        assertEquals("The getting BundleEndDate was not the expected one", testBundleEndDate,
            testOneTimeStatisticDTO.getBundleEndDate());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getPatientId} and
     * {@link OneTimeStatisticDTO#setPatientId}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetPatientId() {
        String testPatientID = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testOneTimeStatisticDTO.setPatientId(testPatientID);
        assertEquals("The getting PatientID was not the expected one", testPatientID,
            testOneTimeStatisticDTO.getPatientId());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getPatientStartDate} and
     * {@link OneTimeStatisticDTO#setPatientStartDate}.<br> Valid input: random Date
     */
    @Test
    public void testGetAndSetPatientStartDate() {
        Date testPatientStartDate = new Date(random.nextLong());
        testOneTimeStatisticDTO.setPatientStartDate(testPatientStartDate);
        assertEquals("The getting PatientStartDate was not the expected one", testPatientStartDate,
            testOneTimeStatisticDTO.getPatientStartDate());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getPatientEndDate} and
     * {@link OneTimeStatisticDTO#setPatientEndDate}.<br> Valid input: random Date
     */
    @Test
    public void testGetAndSetPatientEndDate() {
        Date testPatientEndDate = new Date(random.nextLong());
        testOneTimeStatisticDTO.setPatientEndDate(testPatientEndDate);
        assertEquals("The getting PatientEndDate was not the expected one", testPatientEndDate,
            testOneTimeStatisticDTO.getPatientEndDate());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getBundlePatientPatientId} and
     * {@link OneTimeStatisticDTO#setBundlePatientPatientId}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetBundlePatientPatientId() {
        String testBundlePatientPatientID = Helper.getRandomAlphanumericString(
            random.nextInt(50) + 1);
        testOneTimeStatisticDTO.setBundlePatientPatientId(testBundlePatientPatientID);
        assertEquals("The getting BundlePatientPatientID was not the expected one",
            testBundlePatientPatientID, testOneTimeStatisticDTO.getBundlePatientPatientId());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getBundlePatientBundleId} and
     * {@link OneTimeStatisticDTO#setBundlePatientBundleId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetBundlePatientBundleId() {
        Long testBundlePatientBundleID = Math.abs(random.nextLong());
        testOneTimeStatisticDTO.setBundlePatientBundleId(testBundlePatientBundleID);
        assertEquals("The getting BundlePatientBundleID was not the expected one",
            testBundlePatientBundleID, testOneTimeStatisticDTO.getBundlePatientBundleId());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getBundlePatientStartDate} and
     * {@link OneTimeStatisticDTO#setBundlePatientStartDate}.<br> Valid input: random Date
     */
    @Test
    public void testGetAndSetBundlePatientStartDate() {
        Date testBundlePatientStartDate = new Date(random.nextLong());
        testOneTimeStatisticDTO.setBundlePatientStartDate(testBundlePatientStartDate);
        assertEquals("The getting BundlePatientStartDate was not the expected one",
            testBundlePatientStartDate, testOneTimeStatisticDTO.getBundlePatientStartDate());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getBundlePatientEndDate} and
     * {@link OneTimeStatisticDTO#setBundlePatientEndDate}.<br> Valid input: random Date
     */
    @Test
    public void testGetAndSetBundlePatientEndDate() {
        Date testBundlePatientEndDate = new Date(random.nextLong());
        testOneTimeStatisticDTO.setBundlePatientEndDate(testBundlePatientEndDate);
        assertEquals("The getting BundlePatientEndDate was not the expected one",
            testBundlePatientEndDate, testOneTimeStatisticDTO.getBundlePatientEndDate());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getEncounterCountByBundleInInterval} and
     * {@link OneTimeStatisticDTO#setEncounterCountByBundleInInterval}.<br> Valid input: random
     * Long
     */
    @Test
    public void testGetAndSetEncounterCountByBundleInInterval() {
        Long testEncounterCountByBundleInInterval = Math.abs(random.nextLong());
        testOneTimeStatisticDTO.setEncounterCountByBundleInInterval(
            testEncounterCountByBundleInInterval);
        assertEquals("The getting EncounterCountByBundleInInterval was not the expected one",
            testEncounterCountByBundleInInterval,
            testOneTimeStatisticDTO.getEncounterCountByBundleInInterval());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getEncounterCountByCaseNumberInInterval} and
     * {@link OneTimeStatisticDTO#setEncounterCountByCaseNumberInInterval}.<br> Valid input: random
     * Long
     */
    @Test
    public void testGetAndSetEncounterCountByCaseNumberInInterval() {
        Long testEncounterCountByCaseNumberInInterval = Math.abs(random.nextLong());
        testOneTimeStatisticDTO.setEncounterCountByCaseNumberInInterval(
            testEncounterCountByCaseNumberInInterval);
        assertEquals("The getting EncounterCountByCaseNumberInInterval was not the expected one",
            testEncounterCountByCaseNumberInInterval,
            testOneTimeStatisticDTO.getEncounterCountByCaseNumberInInterval());
    }

    /**
     * Test of {@link OneTimeStatisticDTO#getEncounterCountByCaseNumberByBundleInInterval} and
     * {@link OneTimeStatisticDTO#setEncounterCountByCaseNumberByBundleInInterval}.<br> Valid input:
     * random Long
     */
    @Test
    public void testGetAndSetEncounterCountByCaseNumberByBundleInInterval() {
        Long testEncounterCountByCaseNumberByBundleInInterval = Math.abs(random.nextLong());
        testOneTimeStatisticDTO.setEncounterCountByCaseNumberByBundleInInterval(
            testEncounterCountByCaseNumberByBundleInInterval);
        assertEquals(
            "The getting EncounterCountByCaseNumberByBundleInInterval was not the expected one",
            testEncounterCountByCaseNumberByBundleInInterval,
            testOneTimeStatisticDTO.getEncounterCountByCaseNumberByBundleInInterval());
    }
}
