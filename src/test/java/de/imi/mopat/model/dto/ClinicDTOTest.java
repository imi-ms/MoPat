package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
public class ClinicDTOTest {

    private static final Random random = new Random();
    private ClinicDTO testClinicDTO;

    public ClinicDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testClinicDTO = new ClinicDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ClinicDTO#getId} and {@link ClinicDTO#setId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testClinicDTO.setId(testID);
        assertEquals("The getting ID was not thoe expected one", testID, testClinicDTO.getId());
    }

    /**
     * Test of {@link ClinicDTO#getName} and {@link ClinicDTO#setName}.<br> Valid input: random
     * String
     */
    @Test
    public void testGetAndSetName() {
        String testName = Helper.getRandomString(random.nextInt(100));
        testClinicDTO.setName(testName);
        assertEquals("The getting name was not the expected one", testName,
            testClinicDTO.getName());
    }

    /**
     * Test of {@link ClinicDTO#getDescription} and {@link ClinicDTO#setDescription}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetDescription() {
        String testDescription = Helper.getRandomString(random.nextInt(100));
        testClinicDTO.setDescription(testDescription);
        assertEquals("The getting description was not the expected one", testDescription,
            testClinicDTO.getDescription());
    }

    /**
     * Test of {@link ClinicDTO#getAssignedUserDTOs} and {@link ClinicDTO#setAssignedUserDTOs}.<br>
     * Valid input: list of random {@link UserDTO UserDTOs}
     */
    @Test
    public void testGetAndSetAssignedUserDTOs() {
        List<UserDTO> testUserDTOs = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            UserDTO testUserDTO = new UserDTO();
            testUserDTO.setId(Math.abs(random.nextLong()));
            testUserDTOs.add(testUserDTO);
        }
        testClinicDTO.setAssignedUserDTOs(testUserDTOs);
        assertEquals("The getting list of UserDTOs was not the expected one", testUserDTOs,
            testClinicDTO.getAssignedUserDTOs());
    }

    /**
     * Test of {@link ClinicDTO#getBundleClinicDTOs} and {@link ClinicDTO#setBundleClinicDTOs}.<br>
     * Valid input: list of random {@link BundleClinicDTO BundleClinicDTOs}
     */
    @Test
    public void testGetAndSetBundleClinicDTOs() {
        List<BundleClinicDTO> testBundleClinicDTOs = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            BundleClinicDTO testBundleClinicDTO = new BundleClinicDTO();
            testBundleClinicDTO.setClinicDTO(testClinicDTO);
        }
        testClinicDTO.setBundleClinicDTOs(testBundleClinicDTOs);
        assertEquals("The getting list of BundleClinicDTOs was not the expected one",
            testBundleClinicDTOs, testClinicDTO.getBundleClinicDTOs());
    }

    /**
     * Test of {@link ClinicDTO#getEmail} and {@link ClinicDTO#setEmail}.<br> Valid input: random
     * email address
     */
    @Test
    public void testGetAndSetEmail() {
        String testMail = Helper.getRandomMailAddress();
        testClinicDTO.setEmail(testMail);
        assertEquals("The getting Mailaddress was not the expected one", testMail,
            testClinicDTO.getEmail());
    }

    /**
     * Test of {@link BundleDTO#hashCode}.<br> Valid input: Id = <code>null</code>, random long
     */
    @Test
    public void testHashCode() {
        testClinicDTO.setId(null);
        assertEquals("The getting hashCode was not the expected one", -1, testClinicDTO.hashCode());
        Long testID = Math.abs(random.nextLong());
        testClinicDTO.setId(testID);
        assertEquals("The getting hashCode was not the expected one", testID.hashCode(),
            testClinicDTO.hashCode());
    }

    /**
     * Test of {@link ClinicDTO#equals}.<br> Valid input: The same {@link ClinicDTO} in one HashSet
     */
    @Test
    public void testEquals() {
        testClinicDTO.setId(Math.abs(random.nextLong()));
        Set<ClinicDTO> testSet = new HashSet<>();
        testSet.add(testClinicDTO);
        testSet.add(testClinicDTO);
        assertEquals("It was possible to add the same ClinicDTO twice in a set", 1, testSet.size());
        assertEquals("A ClinicDTO was not equal to itself", testClinicDTO, testClinicDTO);
        Object otherObject = new Object();
        assertNotEquals("A ClinicDTO was equal to another Object", testClinicDTO, otherObject);
        ClinicDTO otherClinicDTO = null;
        assertNotEquals("A ClinicDTO was equal to null", testClinicDTO, otherClinicDTO);
        otherClinicDTO = new ClinicDTO();
        otherClinicDTO.setId(null);
        assertNotEquals("A ClinicDTO was equal to different ClinicDTO with ID null", testClinicDTO,
            otherClinicDTO);
        otherClinicDTO.setId(Math.abs(random.nextLong()));
        testClinicDTO.setId(null);
        assertNotEquals("A ClinicDTO was equal to different ClinicDTO altough its ID was null",
            testClinicDTO, otherClinicDTO);
        testClinicDTO.setId(Math.abs(random.nextLong()));
        assertNotEquals("A ClinicDTO was equal to different ClinicDTO", testClinicDTO,
            otherClinicDTO);
    }
}
