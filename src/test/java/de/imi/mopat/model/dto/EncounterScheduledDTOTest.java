package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.model.enumeration.EncounterScheduledMailStatus;
import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class EncounterScheduledDTOTest {

    private static final Random random = new Random();
    private EncounterScheduledDTO testEncounterScheduledDTO;

    public EncounterScheduledDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testEncounterScheduledDTO = new EncounterScheduledDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link EncounterScheduledDTO#getId} and {@link EncounterScheduledDTO#setId}.<br>
     * Valid input: random Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testEncounterScheduledDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID,
            testEncounterScheduledDTO.getId());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getUuid} and {@link EncounterScheduledDTO#setUuid}.<br>
     * Valid input: random UUID
     */
    @Test
    public void testGetAndSetUuid() {
        String testUUID = UUID.randomUUID().toString();
        testEncounterScheduledDTO.setUuid(testUUID);
        assertEquals("The getting UUID was not the expected one", testUUID,
            testEncounterScheduledDTO.getUuid());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getCaseNumber} and
     * {@link EncounterScheduledDTO#setCaseNumber}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetCaseNumber() {
        String testCaseNumber = Helper.getRandomAlphanumericString(random.nextInt(50));
        testEncounterScheduledDTO.setCaseNumber(testCaseNumber);
        assertEquals("The getting caseNumber was not the expected one", testCaseNumber,
            testEncounterScheduledDTO.getCaseNumber());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getBundleDTO} and
     * {@link EncounterScheduledDTO#setBundleDTO}.<br> Valid input: random {@link BundleDTO}
     */
    @Test
    public void testGetAndSetBundleDTO() {
        BundleDTO testBundleDTO = new BundleDTO();
        testBundleDTO.setId(Math.abs(random.nextLong()));
        testEncounterScheduledDTO.setBundleDTO(testBundleDTO);
        assertEquals("The getting BundleDTO was not the expected one", testBundleDTO,
            testEncounterScheduledDTO.getBundleDTO());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getEmail} and
     * {@link EncounterScheduledDTO#setEmail}.<br> Valid input: random email address as String
     */
    @Test
    public void testGetAndSetEmail() {
        String testMail = Helper.getRandomMailAddress();
        testEncounterScheduledDTO.setEmail(testMail);
        assertEquals("The getting Email was not the expected one", testMail,
            testEncounterScheduledDTO.getEmail());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getStartDate} and
     * {@link EncounterScheduledDTO#setStartDate}.<br> Valid input: random Date
     */
    @Test
    public void testGetAndSetStartDate() {
        Date testStartDate = new Date(Math.abs(random.nextLong()));
        testEncounterScheduledDTO.setStartDate(testStartDate);
        assertEquals("The getting startDate was not the expected one", testStartDate,
            testEncounterScheduledDTO.getStartDate());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getEncounterScheduledSerialType} and
     * {@link EncounterScheduledDTO#setEncounterScheduledSerialType}.<br> Valid input: random
     * {@link EncounterScheduledSerialType}
     */
    @Test
    public void testGetAndSetEncounterScheduledSerialType() {
        EncounterScheduledSerialType testEncounterScheduledSerialType = Helper.getRandomEnum(
            EncounterScheduledSerialType.class);
        testEncounterScheduledDTO.setEncounterScheduledSerialType(testEncounterScheduledSerialType);
        assertEquals("The getting EncounterScheduledSerialType was not the expected one",
            testEncounterScheduledSerialType,
            testEncounterScheduledDTO.getEncounterScheduledSerialType());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getEndDate} and
     * {@link EncounterScheduledDTO#setEndDate}.<br> Valid input: random Date
     */
    @Test
    public void testGetAndSetEndDate() {
        Date testEndDate = new Date(Math.abs(random.nextLong()));
        testEncounterScheduledDTO.setEndDate(testEndDate);
        assertEquals("The getting endDate was not the expected one", testEndDate,
            testEncounterScheduledDTO.getEndDate());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getRepeatPeriod} and
     * {@link EncounterScheduledDTO#setRepeatPeriod}.<br> Valid input: random Integer
     */
    @Test
    public void testGetAndSetRepeatPeriod() {
        Integer testRepeatPeriod = Math.abs(random.nextInt());
        testEncounterScheduledDTO.setRepeatPeriod(testRepeatPeriod);
        assertEquals("The getting repeatPeriod was not the expected one", testRepeatPeriod,
            testEncounterScheduledDTO.getRepeatPeriod());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getLocale} and
     * {@link EncounterScheduledDTO#setLocale}.<br> Valid input: random Locale
     */
    @Test
    public void testGetAndSetLocale() {
        Locale testLocale = LocaleHelper.getLocaleFromString(Helper.getRandomLocale());
        testEncounterScheduledDTO.setLocale(testLocale);
        assertEquals("The getting Locale was not the expected one", testLocale,
            testEncounterScheduledDTO.getLocale());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getPersonalText} and
     * {@link EncounterScheduledDTO#setPersonalText}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetPersonalText() {
        String testPersonalText = Helper.getRandomString(random.nextInt(500));
        testEncounterScheduledDTO.setPersonalText(testPersonalText);
        assertEquals("The getting personalText was not the expected one", testPersonalText,
            testEncounterScheduledDTO.getPersonalText());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getEncounterDTOs} and
     * {@link EncounterScheduledDTO#setEncounterDTOs}.<br> Valid input: random list of
     * {@link EncounterDTO EncounterDTOs}
     */
    @Test
    public void testGetAndSetEncounterDTOs() {
        List<EncounterDTO> testEncounterDTOs = new ArrayList<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testEncounterDTOs.add(EncounterDTOTest.getNewValidEncounterDTO());
        }
        testEncounterScheduledDTO.setEncounterDTOs(testEncounterDTOs);
        assertEquals("The getting list of EncounterDTOs was not the expected one",
            testEncounterDTOs, testEncounterScheduledDTO.getEncounterDTOs());

    }

    /**
     * Test of {@link EncounterScheduledDTO#getMailStatus} and
     * {@link EncounterScheduledDTO#setMailStatus}.<br> Valid input: random
     * {@link EncounterScheduledMailStatus}
     */
    @Test
    public void testGetAndSetMailStatus() {
        EncounterScheduledMailStatus testMailStatus = Helper.getRandomEnum(
            EncounterScheduledMailStatus.class);
        testEncounterScheduledDTO.setMailStatus(testMailStatus);
        assertEquals("The getting mailStatus was not the expected one", testMailStatus,
            testEncounterScheduledDTO.getMailStatus());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getReplyMail} and
     * {@link EncounterScheduledDTO#setReplyMail}.<br> Valid input: random mail address as String
     */
    @Test
    public void testGetAndSetReplyMail() {
        String testReplyMail = Helper.getRandomMailAddress();
        testEncounterScheduledDTO.setReplyMail(testReplyMail);
        assertEquals("The getting replyMail was not the expected one", testReplyMail,
            testEncounterScheduledDTO.getReplyMail());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getReplyMails} and
     * {@link EncounterScheduledDTO#setReplyMails}.<br> Valid input: random Map
     */
    @Test
    public void testGetAndSetReplyMails() {
        Map<Long, Set<String>> testReplyMails = new HashMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            Set<String> testMailAddresses = new HashSet<>();
            int countMailAddresses = random.nextInt(200) + 1;
            for (int j = 0; j < countMailAddresses; j++) {
                testMailAddresses.add(Helper.getRandomMailAddress());
            }
            testReplyMails.put(Math.abs(random.nextLong()), testMailAddresses);
        }
        testEncounterScheduledDTO.setReplyMails(testReplyMails);
        assertEquals("The getting map of replyMails was not the expected one", testReplyMails,
            testEncounterScheduledDTO.getReplyMails());
    }

    /**
     * Test of {@link EncounterScheduledDTO#isCompleted}.<br> Valid input: <code>null</code>,
     * endDate in past and EndDate in future
     */
    @Test
    public void testIsCompleted() {
        Date testEndDate = null;
        testEncounterScheduledDTO.setEndDate(testEndDate);
        assertFalse("The EncounterScheduledDTO was completed even though the endDate was null",
            testEncounterScheduledDTO.isCompleted());
        testEndDate = new Date(System.currentTimeMillis() + Math.abs(random.nextLong()));
        testEncounterScheduledDTO.setEndDate(testEndDate);
        assertFalse(
            "The EncounterScheduledDTO was completed even though the endDate was in the future",
            testEncounterScheduledDTO.isCompleted());
        testEndDate = new Date(System.currentTimeMillis() - Math.abs(random.nextLong()));
        testEncounterScheduledDTO.setEndDate(testEndDate);
        assertTrue(
            "The EncounterScheduledDTO was not completed even though the endDate was in the past",
            testEncounterScheduledDTO.isCompleted());
    }

    /**
     * Test of {@link EncounterScheduledDTO#getJSON}.<br> Valid input: valid
     * {@link EncounterScheduledDTO} with random id, uuid, replyMail, startDate, endDate, locale,
     * mailStatus, {@link EncounterDTO EncounterDTOs} and replyMails
     */
    @Test
    public void testGetJSON() {
        testEncounterScheduledDTO.setId(Math.abs(random.nextLong()));
        testEncounterScheduledDTO.setUuid(UUID.randomUUID().toString());
        testEncounterScheduledDTO.setReplyMail(Helper.getRandomMailAddress());
        testEncounterScheduledDTO.setStartDate(new Date(random.nextLong()));
        testEncounterScheduledDTO.setEndDate(new Date(random.nextLong()));
        testEncounterScheduledDTO.setLocale(
            LocaleHelper.getLocaleFromString(Helper.getRandomLocale()));
        testEncounterScheduledDTO.setMailStatus(
            Helper.getRandomEnum(EncounterScheduledMailStatus.class));
        List<EncounterDTO> testEncounterDTOs = new ArrayList<>();
        int countEncounterDTOs = random.nextInt(200) + 1;
        for (int i = 0; i < countEncounterDTOs; i++) {
            EncounterDTO testEncounterDTO = new EncounterDTO();
            testEncounterDTOs.add(testEncounterDTO);
        }
        testEncounterScheduledDTO.setEncounterDTOs(testEncounterDTOs);
        Map<Long, Set<String>> testReplyMails = new HashMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            Set<String> testMailAddresses = new HashSet<>();
            int countMailAddresses = random.nextInt(200) + 1;
            for (int j = 0; j < countMailAddresses; j++) {
                testMailAddresses.add(Helper.getRandomMailAddress());
            }
            testReplyMails.put(Math.abs(random.nextLong()), testMailAddresses);
        }
        testEncounterScheduledDTO.setReplyMails(testReplyMails);

        StringBuilder testJSON = new StringBuilder();
        testJSON.append("{\"id\":").append(testEncounterScheduledDTO.getId());
        testJSON.append(",\"uuid\":\"").append(testEncounterScheduledDTO.getUuid());
        testJSON.append("\",\"replyMail\":\"").append(testEncounterScheduledDTO.getReplyMail());
        testJSON.append("\",\"startDate\":")
            .append(testEncounterScheduledDTO.getStartDate().getTime());
        testJSON.append(",\"endDate\":").append(testEncounterScheduledDTO.getEndDate().getTime());
        testJSON.append(",\"locale\":\"").append(testEncounterScheduledDTO.getLocale().toString());
        testJSON.append("\",\"mailStatus\":\"")
            .append(testEncounterScheduledDTO.getMailStatus().toString());
        testJSON.append("\",\"encounterDTOs\":[");
        for (EncounterDTO encounterDTO : testEncounterScheduledDTO.getEncounterDTOs()) {
            testJSON.append("{\"id\":").append(encounterDTO.getId());
            testJSON.append(",\"uuid\":").append(encounterDTO.getUuid());
            testJSON.append(",\"responses\":").append(encounterDTO.getResponses());
            testJSON.append(",\"lastSeenQuestionId\":")
                .append(encounterDTO.getLastSeenQuestionId());
            testJSON.append(",\"isCompleted\":").append(encounterDTO.getIsCompleted());
            testJSON.append(",\"isTest\":").append(encounterDTO.getIsTest());
            testJSON.append(",\"isAtHome\":").append(encounterDTO.isIsAtHome());
            testJSON.append(",\"bundleDTO\":").append(encounterDTO.getBundleDTO());
            testJSON.append(",\"bundleLanguage\":").append(encounterDTO.getBundleLanguage());
            testJSON.append(",\"activeQuestionnaireIds\":")
                .append(encounterDTO.getActiveQuestionnaireIds());
            testJSON.append(",\"startTime\":").append(encounterDTO.getStartTime());
            testJSON.append(",\"endTime\":").append(encounterDTO.getEndTime());
            testJSON.append(",\"lastReminderDate\":").append(encounterDTO.getLastReminderDate());
            testJSON.append(",\"successfullExports\":")
                .append(encounterDTO.getSuccessfullExports());
            testJSON.append(",\"encounterScheduledDTO\":")
                .append(encounterDTO.getEncounterScheduledDTO());
            testJSON.append(",\"frontImage\":").append(encounterDTO.getFrontImage());
            testJSON.append(",\"backImage\":").append(encounterDTO.getBackImage());
            testJSON.append("},");
        }
        testJSON.deleteCharAt(testJSON.length() - 1);
        testJSON.append("],\"replyMails\":{");
        for (Map.Entry<Long, Set<String>> entry : testEncounterScheduledDTO.getReplyMails()
            .entrySet()) {
            testJSON.append("\"").append(entry.getKey());
            testJSON.append("\":[");
            for (String mailadress : entry.getValue()) {
                testJSON.append("\"").append(mailadress);
                testJSON.append("\",");
            }
            testJSON.deleteCharAt(testJSON.length() - 1);
            testJSON.append("],");
        }
        testJSON.deleteCharAt(testJSON.length() - 1);
        testJSON.append("},\"completed\":").append(testEncounterScheduledDTO.isCompleted());
        testJSON.append("}");

        assertEquals("The getting JSON was not the expected one", testJSON.toString(),
            testEncounterScheduledDTO.getJSON());
    }
}
