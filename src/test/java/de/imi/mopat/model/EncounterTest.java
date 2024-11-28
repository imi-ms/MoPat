package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.helper.model.EncounterDTOMapper;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.enumeration.EncounterScheduledMailStatus;
import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;
import de.imi.mopat.model.enumeration.ExportStatus;
import de.imi.mopat.utils.Helper;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
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
public class EncounterTest {

    private static final Random random = new Random();
    @Autowired
    ApplicationMailer applicationMailer;
    @Autowired
    @Qualifier("messageSource")
    MessageSource messageSource;
    private Encounter testEncounter;
    @Autowired
    private EncounterDTOMapper encounterDTOMapper;

    public EncounterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new Encounter
     *
     * @return Returns a valid new Encounter
     */
    public static Encounter getNewValidEncounter() {
        Bundle bundle = BundleTest.getNewValidBundle();
        String caseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);

        Encounter encounter = new Encounter(bundle, caseNumber);

        return encounter;
    }

    /**
     * Returns a valid new Encounter to a given Bundle
     *
     * @param bundle Bundle associated to this Encounter
     * @return Returns a valid new Encounter to a given Bundle
     */
    public static Encounter getNewValidEncounter(Bundle bundle) {
        String caseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);

        Encounter encounter = new Encounter(bundle, caseNumber);

        return encounter;
    }

    /**
     * Returns a valid new Encounter
     *
     * @param caseNumber case numer of this Encounter
     * @return Returns a valid new Encounter
     */
    public static Encounter getNewValidEncounter(String caseNumber) {
        Bundle bundle = BundleTest.getNewValidBundle();

        Encounter encounter = new Encounter(bundle, caseNumber);

        return encounter;
    }

    @Before
    public void setUp() {
        testEncounter = getNewValidEncounter();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link Encounter#Encounter(java.lang.String)} and
     * {@link Encounter#Encounter(de.imi.mopat.model.Bundle, java.lang.String) }.<br> Invalid
     * input:
     * <code>null</code><br> Valid input: valid {@link Bundle} and valid case number as String
     */
    @Test
    public void testConstructor() {
        String testCaseNumber = null;
        Bundle testBundle = null;
        Throwable e = null;
        try {
            testEncounter = new Encounter(testBundle, testCaseNumber);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the bundle and caseNumber",
            e instanceof AssertionError);

        testCaseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testBundle = BundleTest.getNewValidBundle();
        testEncounter = new Encounter(testBundle, testCaseNumber);
        assertEquals("The getting caseNumber was not the expected one", testCaseNumber,
            testEncounter.getCaseNumber());
        assertEquals("The getting Bundle was not the expected one", testBundle,
            testEncounter.getBundle());
    }

    /**
     * Test of {@link Encounter#getResponses} and {@link Encounter#setResposes}.<br> Invalid input:
     * <code>null</code><br> Valid input: random set of responses with 1 to 50 entries
     */
    @Test
    public void testGetAndSetResponses() {
        Set<Response> testSet = null;
        Throwable e = null;
        try {
            testEncounter.setResponses(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Responses", e instanceof AssertionError);

        testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            testSet.add(ResponseTest.getNewValidResponse());
        }
        testEncounter.setResponses(testSet);
        assertEquals("The getting set of Responses was not the expected one", testSet,
            testEncounter.getResponses());
    }

    /**
     * Test of {@link Encounter#addResponses}.<br> Invalid input: <code>null</code><br> Valid input:
     * random responses to an empty and to an existing set of responses
     */
    @Test
    public void testAddResponses() {
        Set<Response> testSet = null;
        Throwable e = null;
        try {
            testEncounter.addResponses(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the Responses", e instanceof AssertionError);

        testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            testSet.add(ResponseTest.getNewValidResponse());
        }
        testEncounter.addResponses(testSet);
        assertEquals("The getting set of Responses was not the expected one", testSet,
            testEncounter.getResponses());
        count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            testSet.add(ResponseTest.getNewValidResponse());
        }
        testEncounter.addResponses(testSet);
        assertEquals("The getting set of Responses was not the expected one", testSet,
            testEncounter.getResponses());
    }

    /**
     * Test of {@link Encounter#addResponse}.<br> Invalid input: <code>null</code><br> Valid input:
     * random generated responses
     */
    @Test
    public void testAddResponse() {
        Response testResponse = null;
        Throwable e = null;
        try {
            testEncounter.addResponse(testResponse);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the Responses", e instanceof AssertionError);

        Set<Response> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            testResponse = ResponseTest.getNewValidResponse();
            testSet.add(testResponse);
            testEncounter.addResponse(testResponse);
        }
        // Add a Response without an Encounter
        testResponse = new Response();
        testSet.add(testResponse);
        testEncounter.addResponse(testResponse);
        assertEquals("The getting set of Responses was not the expected one", testSet,
            testEncounter.getResponses());
    }

    /**
     * Test of {@link Encounter#getPatientID} and {@link Encounter#setPatientID}.<br> Invalid input:
     * Long lower than 1<br> Valid input: Long greater than 0
     */
    @Test
    public void testGetAndSetPatientID() {
        Long testID = Math.abs(random.nextLong()) * -1;
        Throwable e = null;
        try {
            testEncounter.setPatientID(testID);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a value lower than 1 as the patientID",
            e instanceof AssertionError);

        testID = testID * -1;
        testEncounter.setPatientID(testID);
        assertEquals("The getting patientID was not the expected one", testID,
            testEncounter.getPatientID());
    }

    /**
     * Test of {@link Encounter#getBundleLanguage} and {@link Encounter#setBundleLanguage}.<br>
     * Invalid input: <code>null</code>, empty String<br> Valid input: not empty String
     */
    @Test
    public void testGetAndSetBundleLanguage() {
        String testLanguage = null;
        Throwable e = null;
        try {
            testEncounter.setBundleLanguage(testLanguage);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the BundleLanguage",
            e instanceof AssertionError);

        testLanguage = "     ";
        e = null;
        try {
            testEncounter.setBundleLanguage(testLanguage);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a empty String as the BundleLanguage",
            e instanceof AssertionError);

        testLanguage = UUID.randomUUID().toString();
        testEncounter.setBundleLanguage(testLanguage);
        assertEquals("The getting BundleLanguage was not the expected one", testLanguage,
            testEncounter.getBundleLanguage());
    }

    /**
     * Test of {@link Encounter#getBundle} and {@link Encounter#setBundle}.<br> Invalid input:
     * <code>null</code><br> Valid input: Valid Bundle from {@link BundleTest#getNewValidBundle}
     */
    @Test
    public void testGetAndSetBundle() {
        Bundle testBundle = null;
        Throwable e = null;
        try {
            testEncounter.setBundle(testBundle);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Bundle", e instanceof AssertionError);

        testBundle = BundleTest.getNewValidBundle();
        testEncounter.setBundle(testBundle);
        assertEquals("The getting Bundle was not the expected one", testBundle,
            testEncounter.getBundle());
    }

    /**
     * Test of {@link Encounter#getStartTime} and {@link Encounter#setStartTime}.<br> Invalid input:
     * StartTime after EndTime <br> Valid input: StartTime before EndTime
     */
    @Test
    public void testGetAnSetStartTime() {
        testEncounter.setStartTime(null);
        Timestamp testEndTime = new Timestamp(System.currentTimeMillis());
        Timestamp testStartTime = new Timestamp(System.currentTimeMillis() + 1000);
        Throwable e = null;
        try {
            testEncounter.setEndTime(testEndTime);
            testEncounter.setStartTime(testStartTime);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the StartTime after the EndTime",
            e instanceof AssertionError);

        // Add Timestamps in chronological order
        testEncounter.setEndTime(testStartTime);
        testEncounter.setStartTime(testEndTime);
        assertEquals("The getting StartTime was not the expected one", testEndTime,
            testEncounter.getStartTime());

        testEncounter.setEndTime(null);
        testStartTime = new Timestamp(System.currentTimeMillis());
        testEncounter.setStartTime(testStartTime);
        assertEquals("The getting StartTime was not the expected one", testStartTime,
            testEncounter.getStartTime());
    }

    /**
     * Test of {@link Encounter#getEndTime} and {@link Encounter#setEndTime}.
     * <br>
     * Invalid input:EndTime before StartTime<br> Valid input: EndTime after StartTime
     */
    @Test
    public void testGetAndSetEndTime() {
        Timestamp testEndTime = new Timestamp(System.currentTimeMillis());
        Timestamp testStartTime = new Timestamp(System.currentTimeMillis() + 1000);
        Throwable e = null;
        try {
            testEncounter.setStartTime(testStartTime);
            testEncounter.setEndTime(testEndTime);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the EndTime before the StartTime",
            e instanceof AssertionError);

        // Add Timestamps in chronological order
        testEncounter.setStartTime(testEndTime);
        testEncounter.setEndTime(testStartTime);
        assertEquals("The getting StartTime was not the expected one", testStartTime,
            testEncounter.getEndTime());

        testEncounter.setStartTime(null);
        testEndTime = new Timestamp(System.currentTimeMillis() + 1800000);
        testEncounter.setEndTime(testEndTime);
        assertEquals("The getting EndTime was not the expected one", testEndTime,
            testEncounter.getEndTime());
    }

    /**
     * Test of {@link Encounter#getCaseNumber} and {@link Encounter#setCaseNumber}.<br> Invalid
     * input:
     * <code>null</code>, empty String<br> Valid input: random not empty String
     */
    @Test
    public void testGetAndSetCaseNumber() {
        String testCaseNumber = null;
        Throwable e = null;
        try {
            testEncounter.setCaseNumber(testCaseNumber);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the CaseNumber", e instanceof AssertionError);

        testCaseNumber = "   ";
        e = null;
        try {
            testEncounter.setCaseNumber(testCaseNumber);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a empty CaseNumber", e instanceof AssertionError);

        testCaseNumber = UUID.randomUUID().toString();
        testEncounter.setCaseNumber(testCaseNumber);
        assertEquals("The getting CaseNumber was not the expected one", testCaseNumber,
            testEncounter.getCaseNumber());
    }

    /**
     * Test of {@link Encounter#getActiveQuestionnaires} and
     * {@link Encounter#setActiveQuestionnaires}.<br> Invalid input: <code>null</code><br> Valid
     * input: random List of Longs
     */
    @Test
    public void testGetActiveQuestionnaires() {
        List<Long> testList = null;
        Throwable e = null;
        try {
            testEncounter.setActiveQuestionnaires(testList);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the ActiveQuestionnaires",
            e instanceof AssertionError);

        testList = new ArrayList<>();
        int countActiveQuestionnaires = Math.abs(random.nextInt(50) + 1);
        for (int i = 1; i < countActiveQuestionnaires; i++) {
            testList.add(random.nextLong());
        }
        testEncounter.setActiveQuestionnaires(testList);
        assertEquals("The getting list of ActiveQuestionnaires was not the expected one", testList,
            testEncounter.getActiveQuestionnaires());
    }

    /**
     * Test of {@link Encounter#equals}.<br> Invalid input: the same {@link Encounter} twice in a
     * HashSet
     */
    @Test
    public void testEquals() {
        HashSet<Encounter> testSet = new HashSet<>();
        testSet.add(testEncounter);
        testSet.add(testEncounter);
        assertEquals("It was possible to set the same Encounter in one set", 1, testSet.size());

        assertEquals("The Encounter was not equal to itself", testEncounter, testEncounter);
        assertNotEquals("The Encounter was equal to null", null, testEncounter);
        Encounter otherEncounter = getNewValidEncounter();
        assertNotEquals("The Encounter was equal to a different Encounter", testEncounter,
            otherEncounter);
        Object otherObject = new Object();
        assertNotEquals("The Encounter was equal to a different Object", testEncounter,
            otherObject);
    }

    /**
     * Test of {@link Encounter#removeBundle}.<br> Valid input: {@link Encounter} with
     * {@link Bundle}
     */
    @Test
    public void testRemoveBundle() {
        Bundle testBundle = BundleTest.getNewValidBundle();
        Encounter testEncounter = getNewValidEncounter(testBundle);
        assertNotNull("Bundle of an Encounter with Bundle was null", testEncounter.getBundle());
        testEncounter.removeBundle();
        assertNull("Bundle was not null after removing", testEncounter.getBundle());
        testEncounter.removeBundle();
        assertNull("Bundle was not null after removing it twice", testEncounter.getBundle());
    }

    /**
     * Test of {@link Encounter#removeResponses}.<br> Valid input: random set of
     * {@link Response Responses}
     */
    @Test
    public void testRemoveResponses() {
        int countResponses = Math.abs(random.nextInt(50) + 1);
        for (int i = 0; i < countResponses; i++) {
            testEncounter.addResponse(ResponseTest.getNewValidResponse());
        }
        assertTrue("The getting set of Responses was empty",
            testEncounter.getResponses().size() > 0);
        testEncounter.removeResponses();
        assertTrue("The set of Responses was not empty after removing",
            testEncounter.getResponses().isEmpty());
    }

    /**
     * Test of {@link Encounter#removeResponse}<br> Invalid input: <code>null</code><br> Valid
     * input: random {@link Response}
     */
    @Test
    public void testRemoveResponse() {
        Response testResponse = null;
        Throwable e = null;
        try {
            testEncounter.removeResponse(testResponse);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the Responses",
            e instanceof AssertionError);

        Set<Response> testSet = new HashSet<>();
        testResponse = ResponseTest.getNewValidResponse();
        testSet.add(testResponse);
        int countResponses = Math.abs(random.nextInt(50) + 1);
        for (int i = 0; i < countResponses; i++) {
            testSet.add(ResponseTest.getNewValidResponse());
        }
        testEncounter.setResponses(testSet);
        assertEquals("The getting set of Resposes was not the expected one", testSet,
            testEncounter.getResponses());
        testEncounter.removeResponse(testResponse);
        testSet.remove(testResponse);
        assertEquals("The Respinse was not removed correctly", testSet,
            testEncounter.getResponses());
    }

    /**
     * Test of {@link Encounter#getLastSeenQuestionId} and
     * {@link Encounter#setLastSeenQuestionId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetLastSeenQuestionId() {
        Long testLastSeenQuestionId = random.nextLong();
        testEncounter.setLastSeenQuestionId(testLastSeenQuestionId);
        assertEquals("The getting LastSeenQuestionId was not the expected one",
            testLastSeenQuestionId, testEncounter.getLastSeenQuestionId());
    }

    /**
     * Test of {@link Encounter#getLastReminderDate} and {@link Encounter#setLastReminderDate}.<br>
     * Valid input: random Timestamp
     */
    @Test
    public void testGetAndSetLastReminderDate() {
        Timestamp testReminderDate = new Timestamp(random.nextLong());
        testEncounter.setLastReminderDate(testReminderDate);
        assertEquals("The getting LastReminderDate was not the expected one", testReminderDate,
            testEncounter.getLastReminderDate());
    }

    /**
     * Test of {@link Encounter#addEncounterExportTemplates}.<br> Invalid input:
     * <code>null</code><br> Valid input: random set of
     * {@link EncounterExportTemplate EncounterExporttemplpates}
     */
    @Test
    public void testAddEncounterExportTemplates() {
        Set<EncounterExportTemplate> testSet = null;
        Throwable e = null;
        try {
            testEncounter.addEncounterExportTemplates(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the EncounterExportTemplates",
            e instanceof AssertionError);

        testSet = new HashSet<>();
        int countEncounterExportteplates = Math.abs(random.nextInt(50) + 1);
        for (int i = 0; i < countEncounterExportteplates; i++) {
            testSet.add(EncounterExportTemplateTest.getNewValidEncounterExportTemplate());
        }
        testEncounter.addEncounterExportTemplates(testSet);
        assertEquals("The getting set of EncounterExportTemplates was not the expected one",
            testSet, testEncounter.getEncounterExportTemplates());
    }

    /**
     * Test of {@link Encounter#addEncounterExportTemplate}.<br> Valid input: random number of
     * {@link EncounterExportTemplate EncounterExporttemplates}
     */
    @Test
    public void testAddEncounterExportTemplate() {
        Set<EncounterExportTemplate> testSet = new HashSet<>();
        EncounterExportTemplate testEncounterExportTemplate = null;
        int countEncounterExportteplates = Math.abs(random.nextInt(50) + 1);
        for (int i = 0; i < countEncounterExportteplates; i++) {
            testEncounterExportTemplate = EncounterExportTemplateTest.getNewValidEncounterExportTemplate();
            testEncounter.addEncounterExportTemplate(testEncounterExportTemplate);
            testSet.add(testEncounterExportTemplate);
        }
        // Add the last Encounter twice
        testEncounter.addEncounterExportTemplate(testEncounterExportTemplate);
        // Add an EncounterExportTemplate without Encounter
        testEncounterExportTemplate = new EncounterExportTemplate();
        testEncounter.addEncounterExportTemplate(testEncounterExportTemplate);
        testSet.add(testEncounterExportTemplate);

        assertEquals("The getting set of EncounterExportTemplates was not the expected one",
            testSet, testEncounter.getEncounterExportTemplates());
    }

    /**
     * Test of {@link Encounter#removeEncounterExportTemplate}.<br> Valid input: random
     * {@link EncounterExportTemplate}
     */
    @Test
    public void testRemoveEncounterExportTemplate() {
        EncounterExportTemplate testEncounterExportTemplate = EncounterExportTemplateTest.getNewValidEncounterExportTemplate();
        Set<EncounterExportTemplate> testSet = new HashSet<>();
        testSet.add(testEncounterExportTemplate);
        int countEncounterExportteplates = Math.abs(random.nextInt(50) + 1);
        for (int i = 0; i < countEncounterExportteplates; i++) {
            testSet.add(EncounterExportTemplateTest.getNewValidEncounterExportTemplate());
        }
        testEncounter.addEncounterExportTemplates(testSet);
        assertEquals("The getting set of EncounterExportTemplates was not the expected one",
            testSet, testEncounter.getEncounterExportTemplates());
        testEncounter.removeEncounterExportTemplate(testEncounterExportTemplate);
        testSet.remove(testEncounterExportTemplate);
        assertEquals("The EncounterExportTemplate was not removed correctly", testSet,
            testEncounter.getEncounterExportTemplates());
    }

    /**
     * Test of {@link Encounter#getEncounterExportTemplates}.<br> Valid input: random set of
     * {@link EncounterExportTemplate EncounterExportTemplates}
     */
    @Test
    public void testGetEncounterExportTemplates() {
        EncounterExportTemplate testEncounterExportTemplate = EncounterExportTemplateTest.getNewValidEncounterExportTemplate();
        Set<EncounterExportTemplate> testSet = new HashSet<>();
        testSet.add(testEncounterExportTemplate);
        int countEncounterExportteplates = Math.abs(random.nextInt(50) + 1);
        for (int i = 0; i < countEncounterExportteplates; i++) {
            testSet.add(EncounterExportTemplateTest.getNewValidEncounterExportTemplate());
        }
        testEncounter.addEncounterExportTemplates(testSet);
        assertEquals("The getting set of EncounterExportTemplates was not the expected one",
            testSet, testEncounter.getEncounterExportTemplates());
    }

    /**
     * Test of {@link Encounter#getEncounterScheduled} and
     * {@link Encounter#setEncounterScheduled}.<br> Valid input: random {@link EncounterScheduled}
     */
    @Test
    public void testGetAndSetEncounterScheduled() {
        EncounterScheduled testEncounterScheduled = EncounterScheduledTest.getNewValidEncounterScheduled();
        testEncounter.setEncounterScheduled(testEncounterScheduled);
        assertEquals("The getting EncounterScheduled was not the expected one",
            testEncounterScheduled, testEncounter.getEncounterScheduled());
        // Add same Encounter again and check set size
        int testEncounterSize = testEncounterScheduled.getEncounters().size();
        testEncounter.setEncounterScheduled(testEncounterScheduled);
        assertEquals("After adding the same EncounterScheduled twice the set was altered",
            testEncounterSize, testEncounterScheduled.getEncounters().size());
    }

    /**
     * Test of {@link Encounter#getNumberOfAssignedAndSuccessfullyExportedExportTemplates}.<br>
     * Valid input: random number of ExportTemplates
     */
    @Test
    public void testGetNumberOfAssignedAndSuccessfullyExportedExportTemplates() {
        Bundle testBundle = BundleTest.getNewValidBundle();
        testEncounter.setBundle(testBundle);
        BundleQuestionnaire testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        testBundle.addBundleQuestionnaire(testBundleQuestionnaire);

        Set<EncounterExportTemplate> testSet = new HashSet<>();
        int countSuccessfullyExported = random.nextInt(50) + 1;
        int countFailedExports = random.nextInt(50) + 1;

        for (int i = 0; i < countSuccessfullyExported; i++) {
            ExportTemplate testExportTemplate = ExportTemplateTest.getNewValidExportTemplate();
            testBundleQuestionnaire.addExportTemplate(testExportTemplate);
            testSet.add(
                EncounterExportTemplateTest.getNewValidEncounterExportTemplate(testExportTemplate,
                    ExportStatus.SUCCESS));
        }
        for (int i = 0; i < countFailedExports; i++) {
            ExportTemplate testExportTemplate = ExportTemplateTest.getNewValidExportTemplate();
            testBundleQuestionnaire.addExportTemplate(testExportTemplate);
            testSet.add(
                EncounterExportTemplateTest.getNewValidEncounterExportTemplate(testExportTemplate,
                    ExportStatus.FAILURE));
        }
        // Add a not assigned ExportTemplate
        ExportTemplate testExportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        testSet.add(
            EncounterExportTemplateTest.getNewValidEncounterExportTemplate(testExportTemplate,
                ExportStatus.FAILURE));

        testEncounter.addEncounterExportTemplates(testSet);
        assertEquals(
            "The getting NumberOfAssignedAndSuccessfullyExportedExportTemplates was not the expected one",
            countSuccessfullyExported,
            testEncounter.getNumberOfAssignedAndSuccessfullyExportedExportTemplates());
    }

    /**
     * Test of getNoLongerAssignedEncounterExportTemplates method, of class Encounter.<br> Valid
     * input: random {@link ExportTemplate ExportTemplates} in {@link Encounter} and
     * {@link BundleQuestionnaire}
     */
    @Test
    public void testGetNoLongerAssignedEncounterExportTemplates() {
        Bundle testBundle = BundleTest.getNewValidBundle();
        testEncounter.setBundle(testBundle);
        BundleQuestionnaire testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        testBundle.addBundleQuestionnaire(testBundleQuestionnaire);

        Set<EncounterExportTemplate> testSet = new HashSet<>();
        int countNoLongerAssigned = random.nextInt(50) + 1;
        int countAssigned = random.nextInt(50) + 1;

        for (int i = 0; i < countNoLongerAssigned; i++) {
            EncounterExportTemplate testEncounterExportTemplate = EncounterExportTemplateTest.getNewValidEncounterExportTemplate(
                ExportTemplateTest.getNewValidExportTemplate(), ExportStatus.SUCCESS);
            testEncounter.addEncounterExportTemplate(testEncounterExportTemplate);
            testSet.add(testEncounterExportTemplate);
        }
        for (int i = 0; i < countAssigned; i++) {
            ExportTemplate testExportTemplate = ExportTemplateTest.getNewValidExportTemplate();
            testBundleQuestionnaire.addExportTemplate(testExportTemplate);
            testEncounter.addEncounterExportTemplate(
                EncounterExportTemplateTest.getNewValidEncounterExportTemplate(testExportTemplate,
                    ExportStatus.FAILURE));
        }
        assertEquals(
            "The getting NoLongerAssignedEncounterExportTemplates was not the expected one",
            testSet, new HashSet<>(testEncounter.getNoLongerAssignedEncounterExportTemplates()));
    }

    /**
     * Test of {@link Encounter#getEncounterExportTemplatesByExportTemplate}.<br> Valid input:
     * random number of ExportTemplates
     */
    @Test
    public void testGetEncounterExportTemplatesByExportTemplate() {
        Set<EncounterExportTemplate> testSet = new HashSet<>();
        ExportTemplate testExportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        int countTemplates = random.nextInt(5) + 5;
        for (int i = 0; i < countTemplates; i++) {
            if (random.nextBoolean()) {
                EncounterExportTemplate testEncounterExportTemplate = EncounterExportTemplateTest.getNewValidEncounterExportTemplate(
                    testExportTemplate, ExportStatus.SUCCESS);
                testEncounter.addEncounterExportTemplate(testEncounterExportTemplate);
                testSet.add(testEncounterExportTemplate);
            } else {
                testEncounter.addEncounterExportTemplate(
                    EncounterExportTemplateTest.getNewValidEncounterExportTemplate());
            }
        }
        assertEquals("The getting set of EncounterExportTemplates was not the expected one",
            testSet, new HashSet<>(
                testEncounter.getEncounterExportTemplatesByExportTemplate(testExportTemplate)));
    }

    /**
     * Test of {@link Encounter#toString}.<br> Valid input: random {@link Encounter} with case
     * number, start time, end time and patient id
     */
    @Test
    public void testToString() {
        long testID = Math.abs(random.nextLong());
        String testCaseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        Timestamp testStartTime = new Timestamp(System.currentTimeMillis() - 1000);
        Timestamp testEndTime = new Timestamp(System.currentTimeMillis() + 1000);
        long testPatientID = Math.abs(random.nextLong());

        Encounter spyEncounter = spy(testEncounter);
        Mockito.when(spyEncounter.getId()).thenReturn(testID);

        spyEncounter.setCaseNumber(testCaseNumber);
        spyEncounter.setStartTime(testStartTime);
        spyEncounter.setEndTime(testEndTime);
        spyEncounter.setPatientID(testPatientID);

        String testString =
            "ID:" + testID + " Case Number:" + testCaseNumber + " Starttime:" + testStartTime
                + " Endtime: " + testEndTime + ". PatientId: " + testPatientID;
        assertEquals("The getting String was not the expected one", testString,
            spyEncounter.toString());
    }

    /**
     * Test of {@link Encounter#toEncounterDTO}.<br> Valid input: valid Encounter with valid bundle
     */
    @Test
    public void testToEncounterDTO() {
        testEncounter = new Encounter();
        Encounter spyEncounter = spy(testEncounter);
        Mockito.when(spyEncounter.getId()).thenReturn(Math.abs(random.nextLong()));
        spyEncounter.setCaseNumber(Helper.getRandomAlphanumericString(random.nextInt(50) + 1));
        spyEncounter.setStartTime(new Timestamp(System.currentTimeMillis()));
        if (random.nextBoolean()) {
            spyEncounter.setEndTime(new Timestamp(System.currentTimeMillis() + 10000));
        }
        List<Long> testActiveQuestionnaireIds = new ArrayList<>();
        int countActiveQuestionnaireIds = random.nextInt(25);
        for (int i = 0; i < countActiveQuestionnaireIds; i++) {
            testActiveQuestionnaireIds.add(Math.abs(random.nextLong()));
        }
        spyEncounter.setActiveQuestionnaires(testActiveQuestionnaireIds);
        EncounterDTO testEncounterDTO = encounterDTOMapper.apply(Boolean.FALSE,
            spyEncounter);
        assertEquals("The getting ID was not the expected one", spyEncounter.getId(),
            testEncounterDTO.getId());
        assertEquals("The getting UUID was not the expected one", spyEncounter.getUUID(),
            testEncounterDTO.getUuid());
        assertEquals("The getting CaseNumber was not the expected one",
            spyEncounter.getCaseNumber(), testEncounterDTO.getCaseNumber());
        assertEquals("The getting isCompleted was not the expected one",
            spyEncounter.getEndTime() != null, testEncounterDTO.getIsCompleted());
        assertEquals("The getting StartTime was not the expected one", spyEncounter.getStartTime(),
            testEncounterDTO.getStartTime());
        assertEquals("The getting EndTime was not the expected one", spyEncounter.getEndTime(),
            testEncounterDTO.getEndTime());
        assertEquals("The getting activeQuestionnaires was not the expected one",
            spyEncounter.getActiveQuestionnaires(), testEncounterDTO.getActiveQuestionnaireIds());
        assertEquals("The getting successfulExports was not the expected one", "-",
            testEncounterDTO.getSuccessfullExports());

        Bundle testBundle = BundleTest.getNewValidBundle();
        Map<String, String> availableLanguages = new HashMap<>();
        int countAvailableLanguages = Math.abs(random.nextInt(50) + 1);
        for (int i = 0; i < countAvailableLanguages; i++) {
            availableLanguages.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        }
        BundleQuestionnaire testBundleQuestionnaire;
        int countBundleQuestionnaires = random.nextInt(50) + 1;
        for (int i = 0; i < countBundleQuestionnaires; i++) {
            testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
            testBundleQuestionnaire.setBundle(testBundle);
            Map<String, String> questionnaireLanguages = new HashMap<>();
            int countQuestionnaireLanguages = Math.abs(random.nextInt(50) + 1);
            for (int j = 0; j < countQuestionnaireLanguages; j++) {
                questionnaireLanguages.put(UUID.randomUUID().toString(),
                    UUID.randomUUID().toString());
            }
            questionnaireLanguages.putAll(availableLanguages);
            testBundleQuestionnaire.getQuestionnaire()
                .setLocalizedDisplayName(questionnaireLanguages);
            int countQuestionsInQuestionnaire = Math.abs(random.nextInt(50) + 1);
            for (int j = 0; j < countQuestionsInQuestionnaire; j++) {
                int countLanguagesForQuestion = Math.abs(random.nextInt(50));
                Map<String, String> questionLanguages = new HashMap<>();
                for (int k = 0; k < countLanguagesForQuestion; k++) {
                    questionLanguages.put(UUID.randomUUID().toString(),
                        UUID.randomUUID().toString());
                }
                questionLanguages.putAll(availableLanguages);
                QuestionTest.getNewValidQuestion(questionLanguages,
                    testBundleQuestionnaire.getQuestionnaire());
            }
        }
        testBundle.setLocalizedWelcomeText(availableLanguages);
        testBundle.setLocalizedFinalText(availableLanguages);
        Bundle spyBundle = spy(testBundle);
        Mockito.when(spyBundle.getId()).thenReturn(Math.abs(random.nextLong()));
        spyEncounter.setBundle(spyBundle);
        testEncounterDTO = encounterDTOMapper.apply(Boolean.FALSE, spyEncounter);
        assertEquals("The getting Bundle was not the expected one", spyBundle.getId(),
            testEncounterDTO.getBundleDTO().getId());
        assertNotEquals("The getting successfulExports was '-' although there is a Bundle", "-",
            testEncounterDTO.getSuccessfullExports());

        testEncounter = new Encounter();
        spyEncounter = spy(testEncounter);
        Mockito.when(spyEncounter.getId()).thenReturn(Math.abs(random.nextLong()));
        spyEncounter.setCaseNumber(Helper.getRandomAlphanumericString(random.nextInt(50) + 1));
        spyEncounter.setStartTime(new Timestamp(System.currentTimeMillis()));
        if (random.nextBoolean()) {
            spyEncounter.setEndTime(new Timestamp(System.currentTimeMillis() + 10000));
        }
        spyEncounter.setActiveQuestionnaires(testActiveQuestionnaireIds);
        spyEncounter.setPatientID(Math.abs(random.nextLong()));
        spyEncounter.setLastSeenQuestionId(Math.abs(random.nextLong()));
        spyEncounter.setBundleLanguage(Helper.getRandomLocale());
        testEncounterDTO = encounterDTOMapper.apply(Boolean.TRUE, spyEncounter);
        assertEquals("The getting ID was not the expected one", spyEncounter.getId(),
            testEncounterDTO.getId());
        assertEquals("The getting UUID was not the expected one", spyEncounter.getUUID(),
            testEncounterDTO.getUuid());
        assertEquals("The getting CaseNumber was not the expected one",
            spyEncounter.getCaseNumber(), testEncounterDTO.getCaseNumber());
        assertEquals("The getting isCompleted was not the expected one",
            spyEncounter.getEndTime() != null, testEncounterDTO.getIsCompleted());
        assertEquals("The getting StartTime was not the expected one", spyEncounter.getStartTime(),
            testEncounterDTO.getStartTime());
        assertEquals("The getting EndTime was not the expected one", spyEncounter.getEndTime(),
            testEncounterDTO.getEndTime());
        assertEquals("The getting activeQuestionnaires was not the expected one",
            spyEncounter.getActiveQuestionnaires(), testEncounterDTO.getActiveQuestionnaireIds());
        assertEquals("The getting PatientID was not the expected one", spyEncounter.getPatientID(),
            testEncounterDTO.getPatientID());
        assertEquals("The getting LastSeenQuetionID was not the expected one",
            spyEncounter.getLastSeenQuestionId(), testEncounterDTO.getLastSeenQuestionId());
        assertEquals("The getting BundleLanguage was not the expected one",
            spyEncounter.getBundleLanguage(), testEncounterDTO.getBundleLanguage());
        assertFalse("IsTest was true in active survey", testEncounterDTO.getIsTest());
        assertEquals(
            "The getting ResponseDTOs were not about the same size as the Encounters Resposes",
            spyEncounter.getResponses().size(), testEncounterDTO.getResponses().size());
        assertNull("The getting BundleDTO was not null although it was not set before",
            testEncounterDTO.getBundleDTO());
        assertFalse("The getting isAtHome was not false although there is no EncounterScheduled",
            testEncounterDTO.isIsAtHome());

        int countResponses = random.nextInt(50) + 1;
        for (int i = 0; i < countResponses; i++) {
            Answer spyAnswer = spy(AnswerTest.getNewValidRandomAnswer());
            Mockito.when(spyAnswer.getId()).thenReturn(Math.abs(random.nextLong()));
            Response testResponse = ResponseTest.getNewValidResponse(spyAnswer);
            spyEncounter.addResponse(testResponse);
        }
        spyEncounter.setBundle(spyBundle);
        spyEncounter.setEncounterScheduled(EncounterScheduledTest.getNewValidEncounterScheduled());
        testEncounterDTO = encounterDTOMapper.apply(Boolean.TRUE, spyEncounter);
        assertEquals("The size of the Responses was not the same",
            spyEncounter.getResponses().size(), testEncounterDTO.getResponses().size());
        assertEquals("The getting bundle was not the expected one", spyBundle.getId(),
            testEncounterDTO.getBundleDTO().getId());
        assertTrue("The getting isAtHome was not true although there is an EncounterScheduled",
            testEncounterDTO.isIsAtHome());
    }

    /**
     * Test of {@link Encounter#getJSON}.<br> Valid input: random {@link Encounter} with
     * bundleLanguage, endTime, {@link Repsonse Responses}, lastSeenQuestionId and lastReminderDate
     */
    @Test
    public void testGetJSON() {
        testEncounter.setBundleLanguage(Helper.getRandomLocale());
        testEncounter.setEndTime(
            new Timestamp(testEncounter.getStartTime().getTime() + Math.abs(random.nextLong())));
        SliderAnswer testAnswer = SliderAnswerTest.getNewValidSliderAnswer();
        int countResponses = random.nextInt(5) + 1;
        for (int i = 0; i < countResponses; i++) {
            testEncounter.addResponse(ResponseTest.getNewValidResponse(testAnswer));
        }
        testEncounter.setLastSeenQuestionId(Math.abs(random.nextLong()));
        testEncounter.setLastReminderDate(new Timestamp(random.nextLong()));

        StringBuilder testJSON = new StringBuilder();
        testJSON.append("{\"id\":").append(testEncounter.getId());
        testJSON.append(",\"bundleLanguage\":\"").append(testEncounter.getBundleLanguage());
        testJSON.append("\",\"startTime\":").append(testEncounter.getStartTime().getTime());
        testJSON.append(",\"endTime\":").append(testEncounter.getEndTime().getTime());
        testJSON.append(",\"responses\":[");
        for (Response testResponse : testEncounter.getResponses()) {
            testJSON.append("{\"customtext\":").append(testResponse.getCustomtext());
            testJSON.append(",\"value\":").append(testResponse.getValue());
            testJSON.append(",\"date\":").append(testResponse.getDate());
            testJSON.append(",\"pointsOnImage\":").append(testResponse.getPointsOnImage());
            testJSON.append(",\"answer\":{\"id\":").append(testResponse.getAnswer().getId());
            testJSON.append(",\"isEnabled\":").append(testResponse.getAnswer().getIsEnabled());
            testJSON.append(",\"conditions\":").append(testResponse.getAnswer().getConditions());
            testJSON.append(",\"minValue\":")
                .append(((SliderAnswer) testResponse.getAnswer()).getMinValue());
            testJSON.append(",\"maxValue\":")
                .append(((SliderAnswer) testResponse.getAnswer()).getMaxValue());
            testJSON.append(",\"stepsize\":")
                .append(((SliderAnswer) testResponse.getAnswer()).getStepsize());
            testJSON.append(",\"localizedMinimumText\":")
                .append(((SliderAnswer) testResponse.getAnswer()).getLocalizedMinimumText());
            testJSON.append(",\"localizedMaximumText\":")
                .append(((SliderAnswer) testResponse.getAnswer()).getLocalizedMaximumText());
            testJSON.append(",\"showValueOnButton\":")
                .append(((SliderAnswer) testResponse.getAnswer()).getShowValueOnButton());
            testJSON.append(",\"showIcons\":")
                .append(((SliderAnswer) testResponse.getAnswer()).getShowIcons());
            testJSON.append(",\"vertical\":")
                .append(((SliderAnswer) testResponse.getAnswer()).getVertical());
            testJSON.append(",\"icons\":")
                .append(((SliderAnswer) testResponse.getAnswer()).getIcons());
            testJSON.append("}},");
        }
        testJSON.deleteCharAt(testJSON.length() - 1);
        testJSON.append("],\"lastSeenQuestionId\":").append(testEncounter.getLastSeenQuestionId());
        testJSON.append(",\"lastReminderDate\":")
            .append(testEncounter.getLastReminderDate().getTime());
        testJSON.append(",\"clinic\":")
            .append("null");
        testJSON.append("}");

        assertEquals("The getting JSON was not the expected one", testJSON.toString(),
            testEncounter.getJSON());
    }

    /**
     * Test of {@link Encounter#sendMail}.<br>
     * <p>
     * Test with given valid input is always true, even if the mail could not be send! This is
     * beacause of the @Async-Tag of the {@link ApplicationMailer#sendMail}.<br>
     * <p>
     * Valid input: valid {@link Encounter} with valid {@link EncounterScheduled} and
     * {@link EncounterScheduled#mailStatus} = {@link EncounterScheduledMailStatus#ACTIVE}<br>
     * Invalid input: Endtime not <code>null</code>, {@link EncounterScheduled} = <code>null</code>
     * or {@link EncounterScheduled#mailStatus} not {@link EncounterScheduledMailStatus#ACTIVE}
     */
    @Test
    public void testSendMail() {
        String baseURL = "http://localhost/";
        // Endtime not null, EncounterScheduled and MailStatus = null
        testEncounter.setEndTime(new Timestamp(System.currentTimeMillis() + 100));
        assertFalse(
            "The sendMail method returned true although the Endtime was not null and the EncounterScheduled was null",
            testEncounter.sendMail(applicationMailer, messageSource, baseURL));

        // Endtime null
        testEncounter.setEndTime(null);
        assertFalse("The sendMail method returned true although the EncounterScheduled was null",
            testEncounter.sendMail(applicationMailer, messageSource, baseURL));

        // EncounterScheduled not null, but MailStatus not ACTIVE
        EncounterScheduled testEncounterScheduled = EncounterScheduledTest.getNewValidEncounterScheduled();
        testEncounterScheduled.setMailStatus(EncounterScheduledMailStatus.DEACTIVATED_PATIENT);
        testEncounter.setEncounterScheduled(testEncounterScheduled);
        assertFalse(
            "The sendMail method returned true although the EncounterScheduleds' MailStatus was not ACTIVE",
            testEncounter.sendMail(applicationMailer, messageSource, baseURL));

        // MailStatus ACTIVE and german locale
        testEncounterScheduled.setMailStatus(EncounterScheduledMailStatus.ACTIVE);
        testEncounterScheduled.setLocale("de_DE");
        testEncounterScheduled.setEncounterScheduledSerialType(
            EncounterScheduledSerialType.UNIQUELY);
        testEncounter.setEncounterScheduled(testEncounterScheduled);
        assertTrue("It was not possible to send a valid mail with german locale",
            testEncounter.sendMail(applicationMailer, messageSource, baseURL));

        // Not Unique and english locale
        testEncounterScheduled.setMailStatus(EncounterScheduledMailStatus.ACTIVE);
        testEncounterScheduled.setLocale("en_GB");
        testEncounterScheduled.setEncounterScheduledSerialType(EncounterScheduledSerialType.WEEKLY);
        testEncounterScheduled.setRepeatPeriod(7);
        testEncounterScheduled.setStartDate(new Date());
        testEncounterScheduled.setEndDate(
            new Date(System.currentTimeMillis() + (long) (6 * Math.pow(10, 9)))); //ca. 10 weeks
        testEncounter.setEncounterScheduled(testEncounterScheduled);
        assertTrue("It was not possible to send a valid mail with english locale",
            testEncounter.sendMail(applicationMailer, messageSource, baseURL));

        // Personaltest null
        testEncounterScheduled.setPersonalText(null);
        assertTrue("It was not possible to send a valid mail with english locale",
            testEncounter.sendMail(applicationMailer, messageSource, baseURL));

        // Personaltext empty
        testEncounterScheduled.setPersonalText("");
        assertTrue("It was not possible to send a valid mail with english locale",
            testEncounter.sendMail(applicationMailer, messageSource, baseURL));
    }
}
