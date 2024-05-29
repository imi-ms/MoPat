package de.imi.mopat.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class ForgotPasswordTokenTest {

    private static final Random random = new Random();
    private ForgotPasswordToken testForgotPasswordToken;

    public ForgotPasswordTokenTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new ForgotPasswordToken
     *
     * @return Returns a valid new ForgotPasswordToken
     */
    public static ForgotPasswordToken getNewValidForgotPasswordToken() {
        User user = UserTest.getNewValidUser();
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(user);
        return forgotPasswordToken;
    }

    @Before
    public void setUp() {
        testForgotPasswordToken = getNewValidForgotPasswordToken();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ForgotPasswordToken#getExpirationDate}.<br> Valid input: Check if the
     * expiration date is between 6 and 8 days
     */
    @Test
    public void testGetExpirationDate() {
        Calendar in8days = Calendar.getInstance();
        in8days.set(Calendar.DAY_OF_YEAR, in8days.get(Calendar.DAY_OF_YEAR) + 8);
        Calendar in6days = Calendar.getInstance();
        in6days.set(Calendar.DAY_OF_YEAR, in6days.get(Calendar.DAY_OF_YEAR) + 6);
        assertTrue("The expiration date was not between 6 and 8 days",
            testForgotPasswordToken.getExpirationDate().before(in8days.getTime())
                && testForgotPasswordToken.getExpirationDate().after(in6days.getTime()));
    }

    /**
     * Test of {@link ForgotPasswordToken#getUser} and {@link ForgotPasswordToken#setUser}.<br>
     * Invalid input: <code>null</code><br> Valid input: random {@link User}
     */
    @Test
    public void testGetAndSetUser() {
        User testUser = null;
        Throwable e = null;
        try {
            testForgotPasswordToken.setUser(testUser);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the User", e instanceof AssertionError);

        testUser = UserTest.getNewValidUser();
        testForgotPasswordToken.setUser(testUser);
        assertEquals("The getting User was not the expected one", testUser,
            testForgotPasswordToken.getUser());
    }

    /**
     * Test of {@link ForgotPasswordToken#isActive}.<br>
     */
    @Test
    public void testIsActive() {
        assertTrue("The new ForgotPasswordToken was not active",
            testForgotPasswordToken.isActive());

        Calendar yesterday = Calendar.getInstance();
        yesterday.set(Calendar.DAY_OF_YEAR, yesterday.get(Calendar.DAY_OF_YEAR) - 1);
        ForgotPasswordToken spyForgotPasswordToken = spy(testForgotPasswordToken);
        Mockito.when(spyForgotPasswordToken.getExpirationDate()).thenReturn(yesterday.getTime());

        assertFalse("A ForgetpasswordToken with expirationdate yesterday was still active",
            spyForgotPasswordToken.isActive());
    }

    /**
     * Test of {@link ForgotPasswordToken#equals}.<br> Valid input: the same
     * {@link ForgotPasswordToken} twice in a HashSet, the same {@link ForgotPasswordToken},
     * <code>null</code>, another {@link ForgotPasswordToken}, another Object
     */
    @Test
    public void testEquals() {
        HashSet<ForgotPasswordToken> testSet = new HashSet<>();
        testSet.add(testForgotPasswordToken);
        testSet.add(testForgotPasswordToken);
        assertEquals("It was possible to set the same ForgotPasswordToken in one set", 1,
            testSet.size());

        assertEquals("The ForgotPasswordToken was not equal to itself", testForgotPasswordToken,
            testForgotPasswordToken);
        assertNotEquals("The ForgotPasswordToken was equal to null", null, testForgotPasswordToken);
        ForgotPasswordToken otherForgotPasswordToken = getNewValidForgotPasswordToken();
        assertNotEquals("The ForgotPasswordToken was equal to a different ForgotPasswordToken",
            testForgotPasswordToken, otherForgotPasswordToken);
        Object otherObject = new Object();
        assertNotEquals("The ForgotPasswordToken was equal to a different Object",
            testForgotPasswordToken, otherObject);
    }
}
