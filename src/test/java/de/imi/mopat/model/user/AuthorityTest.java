package de.imi.mopat.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.utils.Helper;
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
public class AuthorityTest {

    private static final Random random = new Random();
    private Authority testAuthority;

    public AuthorityTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new Authority
     *
     * @return Returns a valid new Authority
     */
    public static Authority getNewValidAuthority() {
        User user = UserTest.getNewValidUser();
        UserRole userRole = Helper.getRandomEnum(UserRole.class);

        return new Authority(user, userRole);
    }

    @Before
    public void setUp() {
        testAuthority = getNewValidAuthority();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link Authority#getUser} and {@link Authority#setUser}.<br> Invalid input:
     * <code>null</code><br> Valid input: random {@link User}
     */
    @Test
    public void testGetAndSetUser() {
        User testUser = null;
        Throwable e = null;
        try {
            testAuthority.setUser(testUser);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the User", e instanceof AssertionError);

        testUser = UserTest.getNewValidUser();
        testAuthority.setUser(testUser);
        assertEquals("The getting User was not the expected one", testUser,
            testAuthority.getUser());
    }

    /**
     * Test of {@link Authority#getAuthority} and {@link Authority#setAuthority}.<br> Valid input:
     * random {@link UserRole}
     */
    @Test
    public void testGetAndSetAuthority() {
        UserRole authority = Helper.getRandomEnum(UserRole.class);
        testAuthority.setAuthority(authority);
        assertEquals("The getting Authority was not the expected one", authority.getTextValue(),
            testAuthority.getAuthority());
    }

    /**
     * Test of {@link Authority#equals}.<br> Valid input: the same {@link Authority} twice in a
     * HashSet, the same {@link Authority}, <code>null</code>, another {@link Authority}, another
     * Object
     */
    @Test
    public void testEquals() {
        HashSet<Authority> testSet = new HashSet<>();
        testSet.add(testAuthority);
        testSet.add(testAuthority);
        assertEquals("It was possible to set the same Authority in one set", 1, testSet.size());

        assertEquals("The Authority was not equal to itself", testAuthority, testAuthority);
        assertNotEquals("The Authority was equal to null", null, testAuthority);
        Authority otherAuthority = getNewValidAuthority();
        assertNotEquals("The Authority was equal to a different Authority", testAuthority,
            otherAuthority);
        Object otherObject = new Object();
        assertNotEquals("The Authority was equal to a different Object", testAuthority,
            otherObject);
    }

    /**
     * Test of {@link Authority#toString}.<br> Valid input: random {@link Authority} with mocked Id
     */
    @Test
    public void testToString() {
        Long testId = Math.abs(random.nextLong());

        Authority spyAuthority = spy(testAuthority);
        Mockito.when(spyAuthority.getId()).thenReturn(testId);

        String testString = testId + testAuthority.getAuthority();
        assertEquals("The getting String was not the expected one", testString,
            spyAuthority.toString());
    }
}
