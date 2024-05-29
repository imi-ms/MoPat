package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.model.enumeration.Gender;
import de.imi.mopat.utils.Helper;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class EncounterDTOTest {

    private static final Random random = new Random();
    private EncounterDTO testEncounterDTO;

    public EncounterDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new EncounterDTO
     *
     * @return Returns a valid new EncounterDTO
     */
    public static EncounterDTO getNewValidEncounterDTO() {
        boolean isTest = random.nextBoolean();
        String caseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);

        EncounterDTO encounterDTO = new EncounterDTO(isTest, caseNumber);

        return encounterDTO;
    }

    @Before
    public void setUp() {
        testEncounterDTO = getNewValidEncounterDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link EncounterDTO#getId} and {@link EncounterDTO#setId}.<br> Valid input: random
     * Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testEncounterDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID, testEncounterDTO.getId());
    }

    /**
     * Test of {@link EncounterDTO#getUuid} and {@link EncounterDTO#setUuid}.<br> Valid input:
     * random String
     */
    @Test
    public void testGetAndSetUuid() {
        String testUUID = UUID.randomUUID().toString();
        testEncounterDTO.setUuid(testUUID);
        assertEquals("The getting UUID was not the expected one", testUUID,
            testEncounterDTO.getUuid());
    }

    /**
     * Test of {@link EncounterDTO#getResponses} and {@link EncounterDTO#setResponses}.<br> Valid
     * input: random list of {@link ResponseDTO ResponseDTOs}
     */
    @Test
    public void testGetAndSetResponses() {
        List<ResponseDTO> testResponses = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testResponses.add(new ResponseDTO());
        }
        testEncounterDTO.setResponses(testResponses);
        assertEquals("The getting list of responses was not the expected one", testResponses,
            testEncounterDTO.getResponses());
    }

    /**
     * Test of {@link EncounterDTO#getLastSeenQuestionId} and
     * {@link EncounterDTO#setLastSeenQuestionId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetLastSeenQuestionId() {
        Long testLastSeenQuestionId = Math.abs(random.nextLong());
        testEncounterDTO.setLastSeenQuestionId(testLastSeenQuestionId);
        assertEquals("The getting LastSeenQuestionId was not the expected one",
            testLastSeenQuestionId, testEncounterDTO.getLastSeenQuestionId());
    }

    /**
     * Test of {@link EncounterDTO#getIsCompleted} and {@link EncounterDTO#setIsCompleted}.<br>
     * Valid input: random boolean
     */
    @Test
    public void testGetAndSetIsCompleted() {
        boolean testIsCompleted = random.nextBoolean();
        testEncounterDTO.setIsCompleted(testIsCompleted);
        assertEquals("The getting isCompleted was not the expected one", testIsCompleted,
            testEncounterDTO.getIsCompleted());
    }

    /**
     * Test of {@link EncounterDTO#getIsTest} and {@link EncounterDTO#setIsTest}.<br> Valid input:
     * random boolean
     */
    @Test
    public void testGetAndSetIsTest() {
        boolean testIsTest = random.nextBoolean();
        testEncounterDTO.setIsTest(testIsTest);
        assertEquals("The getting isTest was not the expected one", testIsTest,
            testEncounterDTO.getIsTest());
    }

    /**
     * Test of {@link EncounterDTO#getBundleDTO} and {@link EncounterDTO#setBundleDTO}.<br> Valid
     * input: random {@link BundleDTO}
     */
    @Test
    public void testGetAndSetBundleDTO() {
        BundleDTO testBundleDTO = new BundleDTO();
        testBundleDTO.setId(Math.abs(random.nextLong()));
        testEncounterDTO.setBundleDTO(testBundleDTO);
        assertEquals("The getting BundleDTO was not the expected one", testBundleDTO,
            testEncounterDTO.getBundleDTO());
    }

    /**
     * Test of {@link EncounterDTO#getBundleLanguage} and
     * {@link EncounterDTO#setBundleLanguage}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetBundleLanguage() {
        String testBundleLanguage = Helper.getRandomLocale();
        testEncounterDTO.setBundleLanguage(testBundleLanguage);
        assertEquals("The getting bundleLanguage was not the expected one", testBundleLanguage,
            testEncounterDTO.getBundleLanguage());
    }

    /**
     * Test of {@link EncounterDTO#getActiveQuestionnaireIds} and
     * {@link EncounterDTO#setActiveQuestionnaireIds}.<br> Valid input: random list of Longs
     */
    @Test
    public void testGetAndSetActiveQuestionnaireIds() {
        List<Long> testActiveQuestionnaireIds = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testActiveQuestionnaireIds.add(Math.abs(random.nextLong()));
        }
        testEncounterDTO.setActiveQuestionnaireIds(testActiveQuestionnaireIds);
        assertEquals("The getting list of activeQuestionnaireIds was not the expected one",
            testActiveQuestionnaireIds, testEncounterDTO.getActiveQuestionnaireIds());
    }

    /**
     * Test of {@link EncounterDTO#getStartTime} and {@link EncounterDTO#setStartTime}.<br> Valid
     * input: random Timestamp
     */
    @Test
    public void testGetAndSetStartTime() {
        Timestamp testStartTime = new Timestamp(random.nextLong());
        testEncounterDTO.setStartTime(testStartTime);
        assertEquals("The getting startTime was not th expected one", testStartTime,
            testEncounterDTO.getStartTime());
    }

    /**
     * Test of {@link EncounterDTO#getEndTime} and {@link EncounterDTO#setEndTime}.<br> Valid input:
     * random Timestamp
     */
    @Test
    public void testGetAndSetEndTime() {
        Timestamp testEndTime = new Timestamp(random.nextLong());
        testEncounterDTO.setEndTime(testEndTime);
        assertEquals("The getting endTime was not th expected one", testEndTime,
            testEncounterDTO.getEndTime());
    }

    /**
     * DTO#setPatientID}.<br> Valid input: random Long Test of {@link EncounterDTO#getPatientID} and
     * {@link Encounter
     */
    @Test
    public void testGetAndSetPatientID() {
        Long testPatientID = Math.abs(random.nextLong());
        testEncounterDTO.setPatientID(testPatientID);
        assertEquals("The getting patientID was not the expected one", testPatientID,
            testEncounterDTO.getPatientID());
    }

    /**
     * Test of {@link EncounterDTO#getCaseNumber} and {@link EncounterDTO#setCaseNumber}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetCaseNumber() {
        String testCaseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testEncounterDTO.setCaseNumber(testCaseNumber);
        assertEquals("The getting caseNumber was not the expected one", testCaseNumber,
            testEncounterDTO.getCaseNumber());
    }

    /**
     * Test of {@link EncounterDTO#getFirstname} and {@link EncounterDTO#setFirstname}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetFirstname() {
        String testFirstname = Helper.getRandomAlphabeticString(random.nextInt(50) + 1);
        testEncounterDTO.setFirstname(testFirstname);
        assertEquals("The getting firstname was not the expected one", testFirstname,
            testEncounterDTO.getFirstname());
    }

    /**
     * Test of {@link EncounterDTO#getLastname} and {@link EncounterDTO#setLastname}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetLastname() {
        String testLastname = Helper.getRandomAlphabeticString(random.nextInt(50) + 1);
        testEncounterDTO.setLastname(testLastname);
        assertEquals("The getting lastname was not the expected one", testLastname,
            testEncounterDTO.getLastname());
    }

    /**
     * Test of {@link EncounterDTO#getBirthdate} and {@link EncounterDTO#setBirthdate}.<br> Valid
     * input: random Date
     */
    @Test
    public void testGetAndSetBirthdate() {
        Date testBirthdate = new Date(random.nextLong());
        testEncounterDTO.setBirthdate(testBirthdate);
        assertEquals("The getting birthdate was not the expected one", testBirthdate,
            testEncounterDTO.getBirthdate());
    }

    /**
     * Test of {@link EncounterDTO#getGender} and {@link EncounterDTO#setGender}.<br> Valid input:
     * random {@link Gender}
     */
    @Test
    public void testGetAndSetGender() {
        Gender testGender = Helper.getRandomEnum(Gender.class);
        testEncounterDTO.setGender(testGender);
        assertEquals("The getting gender was not the expected one", testGender,
            testEncounterDTO.getGender());
    }

    /**
     * Test of {@link EncounterDTO#getSuccessfullExports} and
     * {@link EncounterDTO#setSuccessfullExports}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetSuccessfullExports() {
        String testSuccessfulExports = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testEncounterDTO.setSuccessfullExports(testSuccessfulExports);
        assertEquals("The getting successfulExports was not the expected one",
            testSuccessfulExports, testEncounterDTO.getSuccessfullExports());
    }

    /**
     * Test of {@link EncounterDTO#getEncounterScheduledDTO} and
     * {@link EncounterDTO#setEncounterScheduledDTO}.<br> Valid input: <code>null</code>, random
     * {@link EncounterScheduledDTO}
     */
    @Test
    public void testGetAndSetEncounterScheduledDTO() {
        EncounterScheduledDTO testEncounterScheduledDTO = null;
        testEncounterDTO.setEncounterScheduledDTO(testEncounterScheduledDTO);
        assertFalse("The getting isAtHome was not false although there is no EncounterScheduled",
            testEncounterDTO.isIsAtHome());
        assertNull("The getting EnocunterScheduled was not null after setting it so",
            testEncounterDTO.getEncounterScheduledDTO());

        testEncounterScheduledDTO = new EncounterScheduledDTO();
        testEncounterDTO.setEncounterScheduledDTO(testEncounterScheduledDTO);
        assertTrue("The getting isAtHome was not true although there is an EncounterScheduled",
            testEncounterDTO.isIsAtHome());
        assertEquals("The getting EncounterScheduledDTO was not the expected one",
            testEncounterScheduledDTO, testEncounterDTO.getEncounterScheduledDTO());
    }

    /**
     * Test of {@link EncounterDTO#getLastReminderDate} and
     * {@link EncounterDTO#setLastReminderDate}.<br> Valid input: random Timestamp
     */
    @Test
    public void testGetAndSetLastReminderDate() {
        Timestamp testLastReminderDate = new Timestamp(random.nextLong());
        testEncounterDTO.setLastReminderDate(testLastReminderDate);
        assertEquals("The getting lastReminderDate was not th expected one", testLastReminderDate,
            testEncounterDTO.getLastReminderDate());
    }

    /**
     * Test of {@link EncounterDTO#removeDemographics}.<br>
     */
    @Test
    public void testRemoveDemographics() {
        testEncounterDTO.setBirthdate(new Date(random.nextLong()));
        testEncounterDTO.setCaseNumber(Helper.getRandomAlphanumericString(random.nextInt(50) + 1));
        testEncounterDTO.setFirstname(Helper.getRandomAlphanumericString(random.nextInt(50) + 1));
        testEncounterDTO.setGender(Helper.getRandomEnum(Gender.class));
        testEncounterDTO.setLastname(Helper.getRandomAlphanumericString(random.nextInt(50) + 1));
        testEncounterDTO.setPatientID(Math.abs(random.nextLong()));
        assertNotNull("The birtdate was null after setting it", testEncounterDTO.getBirthdate());
        assertNotNull("The caseNumber was null after setting it", testEncounterDTO.getCaseNumber());
        assertNotNull("The firstname was null after setting it", testEncounterDTO.getFirstname());
        assertNotNull("The gender was null after setting it", testEncounterDTO.getGender());
        assertNotNull("The lastname was null after setting it", testEncounterDTO.getLastname());
        assertNotNull("The patientID was null after setting it", testEncounterDTO.getPatientID());
        testEncounterDTO.removeDemographics();
        assertNull("The birtdate was not null after removing", testEncounterDTO.getBirthdate());
        assertNull("The caseNumber was not null after removing", testEncounterDTO.getCaseNumber());
        assertNull("The firstname was not null after removing", testEncounterDTO.getFirstname());
        assertNull("The gender was not null after removing", testEncounterDTO.getGender());
        assertNull("The lastname was not null after removing", testEncounterDTO.getLastname());
        assertNull("The patientID was not null after removing", testEncounterDTO.getPatientID());
    }

    /**
     * Test of {@link EncounterDTO#getLoggingAttributes}.<br> Valid input: valid
     * {@link EncounterDTO} with birthdate, firtstname, gender, lastname and patientID either set or
     * null
     */
    @Test
    public void testGetLoggingAttributes() {
        testEncounterDTO.setBirthdate(new Date(random.nextLong()));
        testEncounterDTO.setFirstname(Helper.getRandomAlphabeticString(random.nextInt(20) + 5));
        testEncounterDTO.setGender(Helper.getRandomEnum(Gender.class));
        testEncounterDTO.setLastname(Helper.getRandomAlphabeticString(random.nextInt(20) + 5));
        testEncounterDTO.setPatientID(Math.abs(random.nextLong()));

        StringBuilder testLoggingAtttributes = new StringBuilder();
        testLoggingAtttributes.append("Date of birth").append(", First name").append(", gender")
            .append(", last name").append(", patient ID");
        assertEquals("The getting LoggingAttributes was not the expected one",
            testLoggingAtttributes.toString(), testEncounterDTO.getLoggingAttributes());

        testEncounterDTO.setBirthdate(null);
        testLoggingAtttributes = new StringBuilder();
        testLoggingAtttributes.append("First name").append(", gender").append(", last name")
            .append(", patient ID");
        assertEquals("The getting LoggingAttributes was not the expected one",
            testLoggingAtttributes.toString(), testEncounterDTO.getLoggingAttributes());

        testEncounterDTO.setFirstname(null);
        testLoggingAtttributes = new StringBuilder();
        testLoggingAtttributes.append("Gender").append(", last name").append(", patient ID");
        assertEquals("The getting LoggingAttributes was not the expected one",
            testLoggingAtttributes.toString(), testEncounterDTO.getLoggingAttributes());

        testEncounterDTO.setGender(null);
        testLoggingAtttributes = new StringBuilder();
        testLoggingAtttributes.append("Last name").append(", patient ID");
        assertEquals("The getting LoggingAttributes was not the expected one",
            testLoggingAtttributes.toString(), testEncounterDTO.getLoggingAttributes());

        testEncounterDTO.setLastname(null);
        testLoggingAtttributes = new StringBuilder();
        testLoggingAtttributes.append("Patient ID");
        assertEquals("The getting LoggingAttributes was not the expected one",
            testLoggingAtttributes.toString(), testEncounterDTO.getLoggingAttributes());

        testEncounterDTO.setPatientID(null);
        testLoggingAtttributes = new StringBuilder();
        assertEquals("The getting LoggingAttributes was not the expected one",
            testLoggingAtttributes.toString(), testEncounterDTO.getLoggingAttributes());
    }

    /**
     * Test of {@link EncounterDTO#getJSON}.<br> Valid input: valid {@link EncounterDTO} with random
     * Id, Uuid, {@link ResponseDTO ResponseDTOs}, lasSeenQuestionId, isCompleted, isTest,
     * {@link BundleDTO}, bundleLanguage, activeQuestionnaireIds, startTime, endTime,
     * LastreminderDate, successfulExports and {@link EncounterScheduledDTO}
     */
    @Test
    public void testGetJSON() {
        testEncounterDTO.setId(Math.abs(random.nextLong()));
        testEncounterDTO.setUuid(UUID.randomUUID().toString());
        List<ResponseDTO> testResponseDTOs = new ArrayList<>();
        int countEncounterDTOs = random.nextInt(5) + 1;
        for (int i = 0; i < countEncounterDTOs; i++) {
            ResponseDTO testResponseDTO = new ResponseDTO();
            testResponseDTOs.add(testResponseDTO);
        }
        testEncounterDTO.setResponses(testResponseDTOs);
        testEncounterDTO.setLastSeenQuestionId(Math.abs(random.nextLong()));
        testEncounterDTO.setIsCompleted(random.nextBoolean());
        testEncounterDTO.setIsAtHome(random.nextBoolean());
        testEncounterDTO.setIsTest(random.nextBoolean());
        testEncounterDTO.setBundleDTO(new BundleDTO());
        testEncounterDTO.setBundleLanguage(Helper.getRandomLocale());
        List<Long> testActiveQuestionnaireIds = new ArrayList<>();
        int countActiveQuestionnaireIds = random.nextInt(5) + 1;
        for (int i = 0; i < countActiveQuestionnaireIds; i++) {
            testActiveQuestionnaireIds.add(Math.abs(random.nextLong()));
        }
        testEncounterDTO.setActiveQuestionnaireIds(testActiveQuestionnaireIds);
        testEncounterDTO.setStartTime(new Timestamp(random.nextLong()));
        testEncounterDTO.setEndTime(new Timestamp(random.nextLong()));
        testEncounterDTO.setLastReminderDate(new Timestamp(random.nextLong()));
        testEncounterDTO.setSuccessfullExports(
            Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        testEncounterDTO.setEncounterScheduledDTO(new EncounterScheduledDTO());

        StringBuilder testJSON = new StringBuilder();
        testJSON.append("{\"id\":").append(testEncounterDTO.getId());
        testJSON.append(",\"uuid\":\"").append(testEncounterDTO.getUuid());
        testJSON.append("\",\"responses\":[");
        for (ResponseDTO responseDTO : testEncounterDTO.getResponses()) {
            testJSON.append("{\"answerId\":").append(responseDTO.getAnswerId());
            testJSON.append(",\"customtext\":").append(responseDTO.getCustomtext());
            testJSON.append(",\"value\":").append(responseDTO.getValue());
            testJSON.append(",\"date\":").append(responseDTO.getDate());
            testJSON.append(",\"enabled\":").append(responseDTO.isEnabled());
            testJSON.append(",\"pointsOnImage\":").append(responseDTO.getPointsOnImage());
            testJSON.append("},");
        }
        testJSON.deleteCharAt(testJSON.length() - 1);
        testJSON.append("],\"lastSeenQuestionId\":")
            .append(testEncounterDTO.getLastSeenQuestionId());
        testJSON.append(",\"isCompleted\":").append(testEncounterDTO.getIsCompleted());
        testJSON.append(",\"isTest\":").append(testEncounterDTO.getIsTest());
        testJSON.append(",\"isAtHome\":").append(testEncounterDTO.isIsAtHome());
        testJSON.append(",\"bundleDTO\":{\"id\":").append(testEncounterDTO.getBundleDTO().getId());
        testJSON.append(",\"name\":").append(testEncounterDTO.getBundleDTO().getName());
        testJSON.append(",\"localizedWelcomeText\":")
            .append(testEncounterDTO.getBundleDTO().getLocalizedWelcomeText());
        testJSON.append(",\"localizedFinalText\":")
            .append(testEncounterDTO.getBundleDTO().getLocalizedFinalText());
        testJSON.append(",\"showProgressPerBundle\":")
            .append(testEncounterDTO.getBundleDTO().getShowProgressPerBundle());
        testJSON.append(",\"deactivateProgressAndNameDuringSurvey\":")
            .append(testEncounterDTO.getBundleDTO().getdeactivateProgressAndNameDuringSurvey());
        testJSON.append(",\"bundleQuestionnaireDTOs\":")
            .append(testEncounterDTO.getBundleDTO().getBundleQuestionnaireDTOs());
        testJSON.append("},\"bundleLanguage\":\"").append(testEncounterDTO.getBundleLanguage());
        testJSON.append("\",\"activeQuestionnaireIds\":[");
        for (Long bundleQuestionnaireId : testEncounterDTO.getActiveQuestionnaireIds()) {
            testJSON.append(bundleQuestionnaireId).append(",");
        }
        testJSON.deleteCharAt(testJSON.length() - 1);
        testJSON.append("],\"startTime\":").append(testEncounterDTO.getStartTime().getTime());
        testJSON.append(",\"endTime\":").append(testEncounterDTO.getEndTime().getTime());
        testJSON.append(",\"lastReminderDate\":")
            .append(testEncounterDTO.getLastReminderDate().getTime());
        testJSON.append(",\"successfullExports\":\"")
            .append(testEncounterDTO.getSuccessfullExports());
        testJSON.append("\",\"encounterScheduledDTO\":{\"id\":")
            .append(testEncounterDTO.getEncounterScheduledDTO().getId());
        testJSON.append(",\"uuid\":").append(testEncounterDTO.getEncounterScheduledDTO().getUuid());
        testJSON.append(",\"replyMail\":")
            .append(testEncounterDTO.getEncounterScheduledDTO().getReplyMail());
        testJSON.append(",\"startDate\":")
            .append(testEncounterDTO.getEncounterScheduledDTO().getStartDate());
        testJSON.append(",\"endDate\":")
            .append(testEncounterDTO.getEncounterScheduledDTO().getEndDate());
        testJSON.append(",\"locale\":")
            .append(testEncounterDTO.getEncounterScheduledDTO().getLocale());
        testJSON.append(",\"mailStatus\":")
            .append(testEncounterDTO.getEncounterScheduledDTO().getMailStatus());
        testJSON.append(",\"encounterDTOs\":")
            .append(testEncounterDTO.getEncounterScheduledDTO().getEncounterDTOs());
        testJSON.append(",\"replyMails\":")
            .append(testEncounterDTO.getEncounterScheduledDTO().getReplyMails());
        testJSON.append(",\"completed\":")
            .append(testEncounterDTO.getEncounterScheduledDTO().isCompleted());
        testJSON.append("},\"frontImage\":").append(testEncounterDTO.getFrontImage());
        testJSON.append(",\"backImage\":").append(testEncounterDTO.getBackImage());
        testJSON.append("}");

        assertEquals("The getting JSON was not the expected one", testJSON.toString(),
            testEncounterDTO.getJSON());
    }
}
