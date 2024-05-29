package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.utils.Helper;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class UserDTOTest {

    private static final Random random = new Random();
    private UserDTO testUserDTO;

    public UserDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testUserDTO = new UserDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link UserDTO#getId} and {@link UserDTO#setId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testUserDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID, testUserDTO.getId());
    }

    /**
     * Test of {@link UserDTO#getUsername} and {@link UserDTO#setUsername}.<br> Valid input: random
     * String
     */
    @Test
    public void testGetAndSetUsername() {
        String testUsername = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testUserDTO.setUsername(testUsername);
        assertEquals("The getting Username was not the expected one", testUsername,
            testUserDTO.getUsername());
    }

    /**
     * Test of {@link UserDTO#getFirstname} and {@link UserDTO#setFirstname}.<br> Valid input:
     * random String
     */
    @Test
    public void testGetAndSetFirstname() {
        String testFirstname = Helper.getRandomAlphabeticString(random.nextInt(50) + 1);
        testUserDTO.setFirstname(testFirstname);
        assertEquals("The getting Firstname was not the expected one", testFirstname,
            testUserDTO.getFirstname());
    }

    /**
     * Test of {@link UserDTO#getLastname} and {@link UserDTO#setLastname}.<br> Valid input: random
     * String
     */
    @Test
    public void testGetAndSetLastname() {
        String testLastname = Helper.getRandomAlphabeticString(random.nextInt(50) + 1);
        testUserDTO.setLastname(testLastname);
        assertEquals("The getting Lastname was not the expected one", testLastname,
            testUserDTO.getLastname());
    }

    /**
     * Test of {@link UserDTO#} and {@link UserDTO#}.<br> Valid input: random mail address as
     * String
     */
    @Test
    public void testGetAndSetEmail() {
        String testEmail = Helper.getRandomMailAddress();
        testUserDTO.setEmail(testEmail);
        assertEquals("The getting Email address was not the expected one", testEmail,
            testUserDTO.getEmail());
    }
}
