package de.imi.mopat.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.model.dto.UserDTO;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 *
 */
public class UserTest {

    private static final Random random = new Random();
    private User testUser;

    public UserTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
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

    @Before
    public void setUp() {
        testUser = getNewValidUser();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link User#toUserDTO}.<br> Valid input: {@link User} with random username, password,
     * firstname, lastname, email and mocked id
     */
    @Test
    public void testToUserDTO() {
        testUser.setFirstname(Helper.getRandomAlphabeticString(random.nextInt(15) + 3));
        testUser.setLastname(Helper.getRandomAlphabeticString(random.nextInt(15) + 3));
        testUser.setEmail(Helper.getRandomMailAddress());

        UserDTO testUserDTO = testUser.toUserDTO();
        assertEquals("The getting Id was not the expected one", testUser.getId(),
            testUserDTO.getId());
        assertEquals("The getting Username was not the expected one", testUser.getUsername(),
            testUserDTO.getUsername());
        assertEquals("The getting Firstname was not the expected one", testUser.getFirstname(),
            testUserDTO.getFirstname());
        assertEquals("The getting Lastname was not the expected one", testUser.getLastname(),
            testUserDTO.getLastname());
        assertEquals("The getting Email was not the expected one", testUser.getEmail(),
            testUserDTO.getEmail());
    }

    /**
     * Test of {@link User#getAuthorities} and {@link User#addAuthority}.<br> Invalid input:
     * <code>null</code><br> Valid input: random number of random {@link Authority Authorities}
     */
    @Test
    public void testAddAuthorityAndGetAuthorities() {
        Authority testAuthority = null;
        Throwable e = null;
        try {
            testUser.addAuthority(testAuthority);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the Authorities", e instanceof AssertionError);

        List<GrantedAuthority> testList = new ArrayList<>();
        int countAuthorities = random.nextInt(3) + 1;
        for (int i = 0; i < countAuthorities; i++) {
            testAuthority = AuthorityTest.getNewValidAuthority();
            testUser.addAuthority(testAuthority);
            testList.add(new SimpleGrantedAuthority(testAuthority.getAuthority()));
        }
        testAuthority = new Authority();
        testAuthority.setAuthority(Helper.getRandomEnum(UserRole.class));
        testUser.addAuthority(testAuthority);
        testUser.addAuthority(testAuthority);
        testList.add(new SimpleGrantedAuthority(testAuthority.getAuthority()));
        assertTrue("The getting Collection of Authorities was not the expected one",
            Collections.unmodifiableCollection(testList).containsAll(testUser.getAuthorities())
                && testUser.getAuthorities()
                .containsAll(Collections.unmodifiableCollection(testList)));
    }

    /**
     * Test of {@link User#getAuthority} and {@link User#setAuthority}.<br> Invalid input:
     * <code>null</code><br> Valid input: set with random number of random
     * {@link Authority Authorities}
     */
    @Test
    public void testGetAndSetAuthority() {
        Set<Authority> testAuthorities = null;
        Throwable e = null;
        try {
            testUser.setAuthority(testAuthorities);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Authorities", e instanceof AssertionError);

        testAuthorities = new HashSet<>();
        int countAuthorities = random.nextInt(3) + 1;
        for (int i = 0; i < countAuthorities; i++) {
            testAuthorities.add(AuthorityTest.getNewValidAuthority());
        }
        testUser.setAuthority(testAuthorities);
        assertEquals("The getting set of Authorities was not the expected one", testAuthorities,
            testUser.getAuthority());
    }

    /**
     * Test of {@link User#getPassword} and {@link User#setPassword}.<br> Invalid input:
     * <code>null</code><br> Valid input: random String
     */
    @Test
    public void testGetAndSetPassword() {
        String testPassword = null;
        Throwable e = null;
        try {
            testUser.setPassword(testPassword);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Password", e instanceof AssertionError);

        testPassword = Helper.getRandomString(random.nextInt(15) + 6);
        testUser.setPassword(testPassword);
        assertEquals("The getting Password was not the expected one", testPassword,
            testUser.getPassword());
    }

    /**
     * Test of {@link User#getPasswordCheck} and {@link User#setPasswordCheck}.<br> Invalid input:
     * <code>null</code><br> Valid input: random String
     */
    @Test
    public void testGetAndSetPasswordCheck() {
        String testPasswordCheck = null;
        Throwable e = null;
        try {
            testUser.setPasswordCheck(testPasswordCheck);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the PasswordCheck", e instanceof AssertionError);

        testPasswordCheck = Helper.getRandomString(random.nextInt(15) + 6);
        testUser.setPasswordCheck(testPasswordCheck);
        assertEquals("The getting PasswordCheck was not the expected one", testPasswordCheck,
            testUser.getPasswordCheck());
    }

    /**
     * Test of {@link User#getNewPassword} and {@link User#setNewPassword}.<br> Valid input: random
     * String
     */
    @Test
    public void testGetAndSetNewPassword() {
        String testNewPassword = Helper.getRandomString(random.nextInt(15) + 6);
        testUser.setNewPassword(testNewPassword);
        assertEquals("The getting NewPassword was not the expected one", testNewPassword,
            testUser.getNewPassword());
    }

    /**
     * Test of {@link User#getOldPassword} and {@link User#setOldPassword}.<br> Valid input: random
     * String
     */
    @Test
    public void testGetAndSetOldPassword() {
        String testOldPassword = Helper.getRandomString(random.nextInt(15) + 6);
        testUser.setOldPassword(testOldPassword);
        assertEquals("The getting OldPassword was not the expected one", testOldPassword,
            testUser.getOldPassword());
    }

    /**
     * Test of {@link User#getUsername} and {@link User#setUsername}.<br> Invalid input:
     * <code>null</code><br> Valid input: random String
     */
    @Test
    public void testGetAndSetUsername() {
        String testUsername = null;
        Throwable e = null;
        try {
            testUser.setUsername(testUsername);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Username", e instanceof AssertionError);

        testUsername = Helper.getRandomAlphanumericString(random.nextInt(15) + 6);
        testUser.setUsername(testUsername);
        assertEquals("The getting Username was not the expected one", testUsername.toLowerCase(),
            testUser.getUsername());
    }

    /**
     * Test of {@link User#getFirstname} and {@link User#setFirstname}.<br> Invalid input:
     * <code>null</code><br> Valid input: random String
     */
    @Test
    public void testGetAndSetFirstname() {
        String testFirstname = null;
        Throwable e = null;
        try {
            testUser.setFirstname(testFirstname);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Firstname", e instanceof AssertionError);

        testFirstname = Helper.getRandomAlphabeticString(random.nextInt(15) + 6);
        testUser.setFirstname(testFirstname);
        assertEquals("The getting Firstname was not the expected one", testFirstname.trim(),
            testUser.getFirstname());
    }

    /**
     * Test of {@link User#getLastname} and {@link User#setLastname}.<br> Invalid input: <br> Valid
     * input:
     */
    @Test
    public void testGetAndSetLastname() {
        String testLastname = null;
        Throwable e = null;
        try {
            testUser.setLastname(testLastname);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Lastname", e instanceof AssertionError);

        testLastname = Helper.getRandomAlphabeticString(random.nextInt(15) + 6);
        testUser.setLastname(testLastname);
        assertEquals("The getting Lastname was not the expected one", testLastname.trim(),
            testUser.getLastname());
    }

    /**
     * Test of {@link User#getEmail} and {@link User#setEmail}.<br> Invalid input:
     * <code>null</code>, String that does not match the email pattern<br> Valid input: random
     * valid
     * mail address
     */
    @Test
    public void testGetAndSetEmail() {
        String testEmail = null;
        Throwable e = null;
        try {
            testUser.setEmail(testEmail);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Email", e instanceof AssertionError);

        testEmail = Helper.getRandomString(random.nextInt(20) + 1);
        e = null;
        try {
            testUser.setEmail(testEmail);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set an Email that does not match the pattern",
            e instanceof AssertionError);

        testEmail = Helper.getRandomMailAddress();
        testUser.setEmail(testEmail);
        assertEquals("The getting Email was not the expected one", testEmail, testUser.getEmail());
    }

    /**
     * Test of {@link User#getSalt} and {@link User#setSalt}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetSalt() {
        String testSalt = Helper.getRandomAlphabeticString(random.nextInt(25) + 1);
        testUser.setSalt(testSalt);
        assertEquals("The getting Salt was not the expected one", testSalt, testUser.getSalt());
    }

    /**
     * Test of {@link User#addRight} and {@link User#getRights}.<br> Invalid input:
     * <code>null</code><br> Valid input: random number of random {@link AclEntry AclEntries}
     */
    @Test
    public void testAddRightAndGetRights() {
        AclEntry testRight = null;
        Throwable e = null;
        try {
            testUser.addRight(testRight);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the Rights", e instanceof AssertionError);

        Set<AclEntry> testSet = new HashSet<>();
        int countAuthorities = random.nextInt(3) + 1;
        for (int i = 0; i < countAuthorities; i++) {
            testRight = AclEntryTest.getNewValidAclEntry();
            testUser.addRight(testRight);
            testSet.add(testRight);
        }
        testRight = new AclEntry();
        testUser.addRight(testRight);
        testUser.addRight(testRight);
        testSet.add(testRight);
        assertEquals("The getting set of Rights was not the expected one", testSet,
            testUser.getRights());

    }

    /**
     * Test of {@link User#isAccountNonExpired}.
     */
    @Test
    public void testIsAccountNonExpired() {
        assertTrue("IsAccountNonExpired was not true", testUser.isAccountNonExpired());
    }

    /**
     * Test of {@link User#isAccountNonLocked}.
     */
    @Test
    public void testisAccountNonLocked() {
        assertTrue("isAccountNonLocked was not true", testUser.isAccountNonLocked());
    }

    /**
     * Test of {@link User#isCredentialsNonExpired}.
     */
    @Test
    public void testIsCredentialsNonExpired() {
        assertTrue("isCredentialsNonExpired was not true", testUser.isCredentialsNonExpired());
    }

    /**
     * Test of {@link User#getIsEnabled}, {@link User#setIsEnabled} and {@link User#isEnabled}.<br>
     * Invalid input: <code>null</code><br> Valid input: random Boolean
     */
    @Test
    public void testGetSetAndIsEnabled() {
        Boolean testIsEnabled = null;
        Throwable e = null;
        try {
            testUser.setIsEnabled(testIsEnabled);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the isEnabled", e instanceof AssertionError);

        testIsEnabled = random.nextBoolean();
        testUser.setIsEnabled(testIsEnabled);
        assertEquals("The getting isEnabled was not the expected one", testIsEnabled,
            testUser.isEnabled());
        assertEquals("The getting getIsEnabled was not the expected one", testIsEnabled,
            testUser.getIsEnabled());
    }

    /**
     * Test of {@link User#isLdap}.<br> Valid input: {@link User} with <code>null</code>, empty, or
     * random password
     */
    @Test
    public void testIsLdap() {
        testUser = new User();
        assertTrue("The getting isLdap was not true although the password was null",
            testUser.isLdap());
        testUser.setPassword("");
        assertTrue("The getting isLdap was not true although the password was empty",
            testUser.isLdap());
        testUser.setPassword(Helper.getRandomAlphanumericString(random.nextInt(15) + 6));
        assertFalse("The getting isLdap was not false although the password was not null or empty",
            testUser.isLdap());
    }

    /**
     * Test of {@link User#equals}.<br> Valid input: the same {@link User} twice in a HashSet, the
     * same {@link User}, <code>null</code>, another {@link User}, another Object
     */
    @Test
    public void testEquals() {
        HashSet<User> testSet = new HashSet<>();
        testSet.add(testUser);
        testSet.add(testUser);
        assertEquals("It was possible to set the same User in one set", 1, testSet.size());

        assertEquals("The User was not equal to itself", testUser, testUser);
        assertNotEquals("The User was equal to null", null, testUser);
        User otherUser = getNewValidUser();
        assertNotEquals("The User was equal to a different User", testUser, otherUser);
        Object otherObject = new Object();
        assertNotEquals("The User was equal to a different Object", testUser, otherObject);
    }

    /**
     * Test of {@link User#toString}.<br> Valid input: random {@link User} with mocked Id
     */
    @Test
    public void testToString() {
        Long testId = Math.abs(random.nextLong());

        User spyUser = spy(testUser);
        Mockito.when(spyUser.getId()).thenReturn(testId);

        String testString = testId + testUser.getUsername() + testUser.getPassword();
        assertEquals("The getting String was not the expected one", testString, spyUser.toString());
    }

    /**
     * Test of {@link User#getPrincipal} and {@link User#setPrincipal}.<br> Valid input: random
     * boolean
     */
    @Test
    public void testGetAndSetPrincipal() {
        boolean testPrincipal = random.nextBoolean();
        testUser.setPrincipal(testPrincipal);
        assertEquals("The getting Principal was not the expected one", testPrincipal,
            testUser.getPrincipal());
    }

    /**
     * Test of {@link User#getInvitationCollection} and {@link User#setInvitationCollection}.<br>
     * Valid input: Collection with random number of random {@link Invitation Invitations}
     */
    @Test
    public void testGetAndSetInvitationCollection() {
        Collection<Invitation> testInvitationCollection = new HashSet<>();
        int countInvitations = random.nextInt(50) + 1;
        for (int i = 0; i < countInvitations; i++) {
            testInvitationCollection.add(InvitationTest.getNewValidInvitation());
        }
        testUser.setInvitationCollection(testInvitationCollection);
        assertEquals("The getting InvitationCollection was not the expected one",
            testInvitationCollection, testUser.getInvitationCollection());
    }

}
