package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
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
public class InvitationDTOTest {

    private static final Random random = new Random();
    private InvitationDTO testInvitationDTO;

    public InvitationDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testInvitationDTO = new InvitationDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link InvitationDTO#getAssignedClinics} and
     * {@link InvitationDTO#setAssignedClinics}.<br> Valid input: random list of
     * {@link ClinicDTO ClinicDTOs}
     */
    @Test
    public void testGetAndSetAssignedClinics() {
        List<ClinicDTO> testClinicDTOs = new ArrayList<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testClinicDTOs.add(new ClinicDTO());
        }
        testInvitationDTO.setAssignedClinics(testClinicDTOs);
        assertEquals("The getting list of ClinicDTOs was not the expected one", testClinicDTOs,
            testInvitationDTO.getAssignedClinics());
    }

    /**
     * Test of {@link InvitationDTO#getUuid} and {@link InvitationDTO#setUuid}.<br> Valid input:
     * random UUID as String
     */
    @Test
    public void testGetAndSetUuid() {
        String testUUID = UUID.randomUUID().toString();
        testInvitationDTO.setUuid(testUUID);
        assertEquals("The getting UUID was not the expected one", testUUID,
            testInvitationDTO.getUuid());
    }

    /**
     * Test of {@link InvitationDTO#getId} and {@link InvitationDTO#setId}.<br> Valid input: random
     * Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testInvitationDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID, testInvitationDTO.getId());
    }

    /**
     * Test of {@link InvitationDTO#getRole} and {@link InvitationDTO#aetRole}.<br> Valid input:
     * random String
     */
    @Test
    public void testGetAndSetRole() {
        String testRole = Helper.getRandomAlphabeticString(random.nextInt(50) + 1);
        testInvitationDTO.setRole(testRole);
        assertEquals("The getting Role was not the expected one", testRole,
            testInvitationDTO.getRole());
    }

    /**
     * Test of {@link InvitationDTO#getPersonalText} and {@link InvitationDTO#setPersonalText}.<br>
     * Valid input: random String
     */
    @Test
    public void testGetAndSetPersonalText() {
        String testPersonalText = Helper.getRandomString(random.nextInt(200) + 1);
        testInvitationDTO.setPersonalText(testPersonalText);
        assertEquals("The getting PersonalText was not the expected one", testPersonalText,
            testInvitationDTO.getPersonalText());
    }

    /**
     * Test of {@link InvitationDTO#getLocale} and {@link InvitationDTO#setLocale}.<br> Valid input:
     * random locale as String
     */
    @Test
    public void testGetAndSetLocale() {
        String testLocale = Helper.getRandomLocale();
        testInvitationDTO.setLocale(testLocale);
        assertEquals("The getting Locale was not the expected one", testLocale,
            testInvitationDTO.getLocale());
    }
}
