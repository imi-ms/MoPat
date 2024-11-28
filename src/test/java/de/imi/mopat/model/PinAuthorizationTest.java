package de.imi.mopat.model;

import de.imi.mopat.model.user.PinAuthorization;
import de.imi.mopat.model.user.User;
import de.imi.mopat.utils.Helper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

public class PinAuthorizationTest {

    private static final Random random = new Random();
    private PinAuthorization testPinAuth;
    private User testUser;


    public PinAuthorizationTest() {

    }

    @Before
    public void setUp() {
        testUser = getNewValidUser();
        testPinAuth = getNewValidPinAuthorization(testUser);
    }

    /**
     * Returns a valid new User
     *
     * @return Returns a valid new User
     */
    public static User getNewValidUser() {
        return new User(Helper.getRandomAlphabeticString(random.nextInt(10) + 3),
            Helper.getRandomAlphanumericString(random.nextInt(10) + 3));

    }

    /**
     * Returns a new valid PinAuthorization
     * @return new PinAuthorization
     */
    public PinAuthorization getNewValidPinAuthorization(User user) {
        return new PinAuthorization(user);
    }

    /**
     * Tests the initially set values from the setup method
     */
    @Test
    public void testInitialValues() {
        assertNotEquals("The initial date was not set", testPinAuth.getCreateDate(), null);
        assertEquals("The initial retries were not set correctly", testPinAuth.getRemainingTries(), 3);
        assertEquals("The user was not the initial one", testPinAuth.getUser(), testUser);
    }

    /**
     * Tests if the method <i>decreaseRemainingTries</i> works
     * as intended and decreases the tries by 1
     */
    @Test
    public void testDecreasingTries() {
        assertEquals("The initial value was not 3", testPinAuth.getRemainingTries(), 3);

        testPinAuth.decreaseRemainingTries();

        assertEquals("The decreased value was not correct", testPinAuth.getRemainingTries(), 2);
    }

}
