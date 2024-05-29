package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.helper.controller.BundleService;
import de.imi.mopat.helper.controller.EncounterScheduledService;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.model.enumeration.EncounterScheduledMailStatus;
import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;
import de.imi.mopat.utils.Helper;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class EncounterScheduledTest {

    private static final Random random = new Random();
    @Autowired
    ApplicationMailer applicationMailer;
    @Autowired
    @Qualifier("messageSource")
    MessageSource messageSource;
    private EncounterScheduled testEncounterScheduled;

    @Autowired
    private EncounterScheduledService encounterScheduledService;

    @Autowired
    private BundleService bundleService;

    public EncounterScheduledTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new {@link EncounterScheduled}
     *
     * @return Returns a valid new {@link EncounterScheduled}
     */
    public static EncounterScheduled getNewValidEncounterScheduled() {
        String caseNumber = Helper.getRandomAlphanumericString(random.nextInt(255) + 1);
        Bundle bundle = BundleTest.getNewValidBundle();
        Date startDate = new Date(System.currentTimeMillis());
        EncounterScheduledSerialType encounterScheduledSerialType = Helper.getRandomEnum(
            EncounterScheduledSerialType.class);
        Date endDate = new Date(System.currentTimeMillis() + random.nextInt(5000));
        Integer repeatPeriod = random.nextInt(50) + 1;
        String email = Helper.getRandomMailAddress();
        String locale = Helper.getRandomLocale();
        String personalText = Helper.getRandomString(random.nextInt(500));
        String replyMail = Helper.getRandomMailAddress();

        EncounterScheduled encounterScheduled = new EncounterScheduled(caseNumber, bundle,
            startDate, encounterScheduledSerialType, endDate, repeatPeriod, email, locale,
            personalText, replyMail);
        return encounterScheduled;
    }

    /**
     * Returns a valid new {@link EncounterScheduled}
     *
     * @param date endDate of the new (@link Encou8nterScheduled)
     * @return Returns a valid new {@link EncounterScheduled}
     */
    public static EncounterScheduled getNewValidEncounterScheduledWithDate(Date date) {
        String caseNumber = Helper.getRandomAlphanumericString(random.nextInt(255) + 1);
        Bundle bundle = BundleTest.getNewValidBundle();
        EncounterScheduledSerialType encounterScheduledSerialType = Helper.getRandomEnum(
            EncounterScheduledSerialType.class);
        Date startDate;
        Date endDate = null;
        if (encounterScheduledSerialType.equals(EncounterScheduledSerialType.UNIQUELY)) {
            startDate = date;
        } else {
            endDate = new Date(date.getTime() + (long) (random.nextInt(10) + 1) * 86400000L);
            startDate = new Date(date.getTime() - (long) (random.nextInt(10) + 1) * 86400000L);
        }
        Integer repeatPeriod = random.nextInt(50) + 1;
        String email = Helper.getRandomMailAddress();
        String locale = Helper.getRandomLocale();
        String personalText = Helper.getRandomString(random.nextInt(500));
        String replyMail = Helper.getRandomMailAddress();

        EncounterScheduled encounterScheduled = new EncounterScheduled(caseNumber, bundle,
            startDate, encounterScheduledSerialType, endDate, repeatPeriod, email, locale,
            personalText, replyMail);
        return encounterScheduled;
    }

    /**
     * Returns a valid new {@link EncounterScheduled}
     *
     * @param date endDate of the new (@link Encou8nterScheduled)
     * @return Returns a valid new {@link EncounterScheduled}
     */
    public static EncounterScheduled getNewValidEncounterScheduledWithEndDate(Date date) {
        String caseNumber = Helper.getRandomAlphanumericString(random.nextInt(255) + 1);
        Bundle bundle = BundleTest.getNewValidBundle();
        EncounterScheduledSerialType encounterScheduledSerialType = Helper.getRandomEnum(
            EncounterScheduledSerialType.class);
        Date startDate;
        Date endDate = null;
        if (encounterScheduledSerialType.equals(EncounterScheduledSerialType.UNIQUELY)) {
            startDate = date;
        } else {
            endDate = date;
            startDate = new Date(endDate.getTime() - random.nextInt(5000));
        }
        Integer repeatPeriod = random.nextInt(50) + 1;
        String email = Helper.getRandomMailAddress();
        String locale = Helper.getRandomLocale();
        String personalText = Helper.getRandomString(random.nextInt(500));
        String replyMail = Helper.getRandomMailAddress();

        EncounterScheduled encounterScheduled = new EncounterScheduled(caseNumber, bundle,
            startDate, encounterScheduledSerialType, endDate, repeatPeriod, email, locale,
            personalText, replyMail);
        return encounterScheduled;
    }

    @Before
    public void setUp() {
        testEncounterScheduled = getNewValidEncounterScheduled();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link EncounterScheduled#getLocale} and {@link EncounterScheduled#setLocale}.<br>
     * Valid input: random locale String
     */
    @Test
    public void testGetAndSetLocale() {
        String testLocale = Helper.getRandomLocale();
        testEncounterScheduled.setLocale(testLocale);
        assertEquals("The getting locale was not the expected one", testLocale,
            testEncounterScheduled.getLocale());
    }

    /**
     * Test of {@link EncounterScheduled#getPersonalText} and
     * {@link EncounterScheduled#setPersonalText}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetPersonalText() {
        String testPersonalText = Helper.getRandomString(random.nextInt(500));
        testEncounterScheduled.setPersonalText(testPersonalText);
        assertEquals("The getting personalText was not the expected one", testPersonalText,
            testEncounterScheduled.getPersonalText());
    }

    /**
     * Test of {@link EncounterScheduled#getCaseNumber} and
     * {@link EncounterScheduled#setCaseNumber}.<br> Invalid input: <code>null</code><br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetCaseNumber() {
        String testCaseNumber = null;
        Throwable e = null;
        try {
            testEncounterScheduled.setCaseNumber(testCaseNumber);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the caseNumber", e instanceof AssertionError);

        testCaseNumber = "          ";
        e = null;
        try {
            testEncounterScheduled.setCaseNumber(testCaseNumber);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a caseNumber that is empty after trimming",
            e instanceof AssertionError);

        testCaseNumber = Helper.getRandomAlphanumericString(random.nextInt(500) + 1);
        testEncounterScheduled.setCaseNumber(testCaseNumber);
        assertEquals("The getting caseNumber was not the expected one", testCaseNumber,
            testEncounterScheduled.getCaseNumber());
    }

    /**
     * Test of {@link EncounterScheduled#getEmail} and {@link EncounterScheduled#setEmail}.<br>
     * Invalid input: <code>null</code><br> Valid input: random mail address
     */
    @Test
    public void testGetAndSetEmail() {
        String testEmail = null;
        Throwable e = null;
        try {
            testEncounterScheduled.setEmail(testEmail);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the email", e instanceof AssertionError);

        testEmail = Helper.getRandomMailAddress();
        testEncounterScheduled.setEmail(testEmail);
        assertEquals("The getting email was not the expected one", testEmail,
            testEncounterScheduled.getEmail());
    }

    /**
     * Test of {@link EncounterScheduled#getBundle} and {@link EncounterScheduled#setBundle}.<br>
     * Invalid input: <code>null</code><br> Valid input: random {@link Bundle}
     */
    @Test
    public void testGetAndSetBundle() {
        Bundle testBundle = null;
        Throwable e = null;
        try {
            testEncounterScheduled.setBundle(testBundle);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the bundle", e instanceof AssertionError);

        testBundle = BundleTest.getNewValidBundle();
        testEncounterScheduled.setBundle(testBundle);
        assertEquals("The getting bundle was not the expected one", testBundle,
            testEncounterScheduled.getBundle());
    }

    /**
     * Test of {@link EncounterScheduled#getStartDate} and
     * {@link EncounterScheduled#setStartDate}.<br> Valid input: random Date
     */
    @Test
    public void testGetAndSetStartDate() {
        Date testStartDate = new Date(System.currentTimeMillis() + random.nextLong());
        testEncounterScheduled.setStartDate(testStartDate);
        assertEquals("The getting startDate was not the expected one", testStartDate,
            testEncounterScheduled.getStartDate());
    }

    /**
     * Test of {@link EncounterScheduled#getEndDate} and {@link EncounterScheduled#setEndDate}.<br>
     * Valid input: random Date
     */
    @Test
    public void testGetAndSetEndDate() {
        Date testEndDate = new Date(System.currentTimeMillis() + random.nextLong());
        testEncounterScheduled.setEndDate(testEndDate);
        assertEquals("The getting endDate was not the expected one", testEndDate,
            testEncounterScheduled.getEndDate());
    }

    /**
     * Test of {@link EncounterScheduled#getRepeatPeriod} and
     * {@link EncounterScheduled#setRepeatPeriod}.<br> Valid input: random Integer
     */
    @Test
    public void testGetAndSetRepeatPeriod() {
        Integer testRepeatPeriod = Math.abs(random.nextInt()) * -1;
        Throwable e = null;
        try {
            testEncounterScheduled.setRepeatPeriod(testRepeatPeriod);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative repeatPeriod", e instanceof AssertionError);

        testRepeatPeriod = Math.abs(random.nextInt()) + 1;
        testEncounterScheduled.setRepeatPeriod(testRepeatPeriod);
        assertEquals("The getting repeatPeriod was not the expected one", testRepeatPeriod,
            testEncounterScheduled.getRepeatPeriod());
    }

    /**
     * Test of {@link EncounterScheduled#getEncounterScheduledSerialType} and
     * {@link EncounterScheduled#setEncounterScheduledSerialType}.<br> Valid input: random
     * {@link EncounterScheduledSerialType}
     */
    @Test
    public void testGetAndSetEncounterScheduledSerialType() {
        EncounterScheduledSerialType encounterScheduledSerialType = Helper.getRandomEnum(
            EncounterScheduledSerialType.class);
        testEncounterScheduled.setEncounterScheduledSerialType(encounterScheduledSerialType);
        assertEquals("The getting encounterScheduledSerialType was not the expected one",
            encounterScheduledSerialType, testEncounterScheduled.getEncounterScheduledSerialType());
    }

    /**
     * Test of {@link EncounterScheduled#getEncounters} and
     * {@link EncounterScheduled#setEncounters}.<br> Valid input: random number of
     * {@link Encounter Encounters}
     */
    @Test
    public void testGetAndSetEncounters() {
        Set<Encounter> testSet = new HashSet<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testSet.add(EncounterTest.getNewValidEncounter());
        }
        testEncounterScheduled.setEncounters(testSet);
        assertEquals("The getting set of encounters was not the expected one", testSet,
            testEncounterScheduled.getEncounters());
    }

    /**
     * Test of {@link EncounterScheduled#addEncounter}.<br> Invalid input: <code>null</code><br>
     * Valid input: random number of {@link Encounter Encounters}
     */
    @Test
    public void testAddEncounter() {
        Encounter testEncounter = null;
        Throwable e = null;
        try {
            testEncounterScheduled.addEncounter(testEncounter);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the set of encounters",
            e instanceof AssertionError);

        Set<Encounter> testSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testEncounter = EncounterTest.getNewValidEncounter();
            testEncounterScheduled.addEncounter(testEncounter);
            testSet.add(testEncounter);
        }
        // Add the last Encounter twice
        testEncounterScheduled.addEncounter(testEncounter);
        // Add an Encounter with anonther EncounterScheduled
        testEncounter = EncounterTest.getNewValidEncounter();
        getNewValidEncounterScheduled().addEncounter(testEncounter);
        testEncounterScheduled.addEncounter(testEncounter);
        testSet.add(testEncounter);

        assertEquals("The getting set of encounters was not the expected one", testSet,
            testEncounterScheduled.getEncounters());
    }

    /**
     * Test of {@link EncounterScheduled#getMailStatus} and
     * {@link EncounterScheduled#sSetMailStatus}.<br> Valid input: random
     * {@link EncounterScheduledMailStatus}
     */
    @Test
    public void testGetAndSetMailStatus() {
        EncounterScheduledMailStatus mailStatus = Helper.getRandomEnum(
            EncounterScheduledMailStatus.class);
        testEncounterScheduled.setMailStatus(mailStatus);
        assertEquals("The getting mailStatus was not the expected one", mailStatus,
            testEncounterScheduled.getMailStatus());
    }

    /**
     * Test of {@link EncounterScheduled#getReplyMail} and
     * {@link EncounterScheduled#setReplyMail}.<br> Valid input: random mail address
     */
    @Test
    public void testGetAndSetReplyMail() {
        String testReplyMail = Helper.getRandomMailAddress();
        testEncounterScheduled.setReplyMail(testReplyMail);
        assertEquals("The getting replyMail was not the expected one", testReplyMail,
            testEncounterScheduled.getReplyMail());
    }

    /**
     * Test of {@link EncounterScheduled#equals}.<br> Invalid input: the same
     * {@link EncounterScheduled} twice in a HashSet
     */
    @Test
    public void testEquals() {
        HashSet<EncounterScheduled> testSet = new HashSet<>();
        testSet.add(testEncounterScheduled);
        testSet.add(testEncounterScheduled);
        assertEquals("It was possible to add the same EncounterScheduled twice to one set", 1,
            testSet.size());

        assertEquals("The EncounterScheduled was not equal to itself", testEncounterScheduled,
            testEncounterScheduled);
        assertNotEquals("The EncounterScheduled was equal to null", null, testEncounterScheduled);
        EncounterScheduled otherEncounterScheduled = getNewValidEncounterScheduled();
        assertNotEquals("The EncounterScheduled was equal to a different EncounterScheduled",
            testEncounterScheduled, otherEncounterScheduled);
        Object otherObject = new Object();
        assertNotEquals("The EncounterScheduled was equal to a different Object",
            testEncounterScheduled, otherObject);
    }

    /**
     * Test of {@link EncounterScheduled#toEncounterScheduledDTO}.<br> Valid input: random
     * {@link EncounterScheduled} with random number of {@link Encounter Encounters}
     */
    @Test
    public void testToEncounterScheduledDTO() {
        EncounterScheduled spyEncounterScheduled = spy(testEncounterScheduled);
        Mockito.when(spyEncounterScheduled.getId()).thenReturn(Math.abs(random.nextLong()));

        Bundle spyBundle = spy(BundleTest.getNewValidBundle());
        Mockito.when(spyBundle.getId()).thenReturn(Math.abs(random.nextLong()));
        spyBundle.setLocalizedFinalText(new TreeMap<>());
        spyBundle.setLocalizedWelcomeText(new TreeMap<>());
        spyEncounterScheduled.setBundle(spyBundle);

        int count = random.nextInt(250);
        for (int i = 0; i < count; i++) {
            Encounter spyEncounter = spy(EncounterTest.getNewValidEncounter(spyBundle));
            Mockito.when(spyEncounter.getId()).thenReturn(Math.abs(random.nextLong()));
            if (i % 2 == 0) {
                spyEncounter.setEndTime(
                    new Timestamp(spyEncounter.getStartTime().getTime() + 5000));
            }
            testEncounterScheduled.addEncounter(spyEncounter);
        }

        EncounterScheduledDTO testEncounterScheduledDTO = encounterScheduledService.toEncounterScheduledDTO(
            spyEncounterScheduled);

        assertEquals("The getting startDate was not the expected one",
            spyEncounterScheduled.getStartDate(), testEncounterScheduledDTO.getStartDate());
        assertEquals("The getting email was not the expected one", spyEncounterScheduled.getEmail(),
            testEncounterScheduledDTO.getEmail());
        assertEquals("The getting endDate was not the expected one",
            spyEncounterScheduled.getEndDate(), testEncounterScheduledDTO.getEndDate());
        assertEquals("The getting repeatPeriod was not the expected one",
            (long) spyEncounterScheduled.getRepeatPeriod(),
            (long) testEncounterScheduledDTO.getRepeatPeriod());
        assertEquals("The getting caseNumber was not the expected one",
            spyEncounterScheduled.getCaseNumber(), testEncounterScheduledDTO.getCaseNumber());
        assertEquals("The getting ID was not the expected one", spyEncounterScheduled.getId(),
            testEncounterScheduledDTO.getId());
        assertEquals("The getting UUID was not the expected one", spyEncounterScheduled.getUUID(),
            testEncounterScheduledDTO.getUuid());
        assertEquals("The getting EncounterScheduledSerialType was not the expected one",
            spyEncounterScheduled.getEncounterScheduledSerialType(),
            testEncounterScheduledDTO.getEncounterScheduledSerialType());
        assertEquals("The getting mailStatus was not the expected one",
            spyEncounterScheduled.getMailStatus(), testEncounterScheduledDTO.getMailStatus());
        assertEquals("The getting  was not the expected one",
            spyEncounterScheduled.getEncounters().size(),
            testEncounterScheduledDTO.getEncounterDTOs().size());
        assertEquals("The getting  was not the expected one",
            bundleService.toBundleDTO(true, spyEncounterScheduled.getBundle()),
            testEncounterScheduledDTO.getBundleDTO());
    }

    /**
     * Test of {@link EncounterScheduled#sendReactivationMail}.<br> Invalid input: <br> Valid
     * input:
     */
    @Test
    public void testSendReactivationMail() {
        String baseURL = "http://localhost/";
        // Enddate before now
        testEncounterScheduled.setEndDate(new Date(System.currentTimeMillis() - 1000));
        assertFalse(
            "The sendReactivationMail method returned true although the Endtime was before now",
            testEncounterScheduled.sendReactivationMail(applicationMailer, messageSource, baseURL));

        // Enddate after now, mailStatus != DEACTIVATED_PATIENT
        testEncounterScheduled.setEndDate(new Date(System.currentTimeMillis() + 1000));
        testEncounterScheduled.setMailStatus(EncounterScheduledMailStatus.ACTIVE);
        assertFalse(
            "The sendReactivationMail method returned true although mailStatus was not DEACTIVATED_PATIENT",
            testEncounterScheduled.sendReactivationMail(applicationMailer, messageSource, baseURL));

        // Enddate null and mailStatus == DEACTIVATED_PATIENT
        testEncounterScheduled.setEndDate(null);
        testEncounterScheduled.setMailStatus(EncounterScheduledMailStatus.DEACTIVATED_PATIENT);
        assertTrue(
            "The sendReactivationMail method returned false although the Enddate was null and the mailStatus was DEACTIVATED_PATIENT",
            testEncounterScheduled.sendReactivationMail(applicationMailer, messageSource, baseURL));
    }
}
