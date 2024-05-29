package de.imi.mopat.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.model.enumeration.PermissionType;
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
public class AclEntryTest {

    private static final Random random = new Random();
    private AclEntry testAclEntry;

    public AclEntryTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new AclEntry
     *
     * @return Returns a valid new AclEntry
     */
    public static AclEntry getNewValidAclEntry() {
        User user = UserTest.getNewValidUser();
        AclObjectIdentity aclObjectIdentity = AclObjectIdentityTest.getNewValidAclObjectIdentity();
        int aceOrder = random.nextInt();
        PermissionType permissionType = Helper.getRandomEnum(PermissionType.class);
        boolean granting = random.nextBoolean();
        boolean auditSuccess = random.nextBoolean();
        boolean auditFailure = random.nextBoolean();

        return new AclEntry(user, aclObjectIdentity, aceOrder, permissionType, granting,
            auditSuccess, auditFailure);
    }

    @Before
    public void setUp() {
        testAclEntry = getNewValidAclEntry();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link AclEntry#getAceOrder} and {@link AclEntry#setAceOrder}.<br> Invalid input:
     * <code>null</code><br> Valid input: random Integer
     */
    @Test
    public void testGetAndSetAceOrder() {
        Integer testAceOrder = null;
        Throwable e = null;
        try {
            testAclEntry.setAceOrder(testAceOrder);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the AceOrder", e instanceof AssertionError);

        testAceOrder = random.nextInt();
        testAclEntry.setAceOrder(testAceOrder);
        assertEquals("The getting AceOrder was not the expected one", testAceOrder,
            testAclEntry.getAceOrder());
    }

    /**
     * Test of {@link AclEntry#getPermissionType} and {@link AclEntry#setPermissionType}.<br>
     * Invalid input: <code>null</code><br> Valid input: random {@link PermissionType}
     */
    @Test
    public void testGetAndSetPermissionType() {
        PermissionType testpPermissionType = null;
        Throwable e = null;
        try {
            testAclEntry.setPermissionType(testpPermissionType);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the PermissionType",
            e instanceof AssertionError);

        testpPermissionType = Helper.getRandomEnum(PermissionType.class);
        testAclEntry.setPermissionType(testpPermissionType);
        assertEquals("The getting PermissionType was not the expected one", testpPermissionType,
            testAclEntry.getPermissionType());
    }

    /**
     * Test of {@link AclEntry#getGranting} and {@link AclEntry#setGranting}.<br> Invalid input:
     * <code>null</code><br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetGranting() {
        Boolean testGranting = null;
        Throwable e = null;
        try {
            testAclEntry.setGranting(testGranting);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Granting", e instanceof AssertionError);

        testGranting = random.nextBoolean();
        testAclEntry.setGranting(testGranting);
        assertEquals("The getting Granting was not the expected one", testGranting,
            testAclEntry.getGranting());
    }

    /**
     * Test of {@link AclEntry#getAuditSuccess} and {@link AclEntry#setAuditSuccess}.<br> Invalid
     * input: <code>null</code><br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetAuditSuccess() {
        Boolean testAuditSuccess = null;
        Throwable e = null;
        try {
            testAclEntry.setAuditSuccess(testAuditSuccess);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the AuditSuccess", e instanceof AssertionError);

        testAuditSuccess = random.nextBoolean();
        testAclEntry.setAuditSuccess(testAuditSuccess);
        assertEquals("The getting AuditSuccess was not the expected one", testAuditSuccess,
            testAclEntry.getAuditSuccess());
    }

    /**
     * Test of {@link AclEntry#getAuditFailure} and {@link AclEntry#setAuditFailure}.<br> Invalid
     * input: <code>null</code><br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetAuditFailure() {
        Boolean testAuditFailure = null;
        Throwable e = null;
        try {
            testAclEntry.setAuditFailure(testAuditFailure);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the AuditFailure", e instanceof AssertionError);

        testAuditFailure = random.nextBoolean();
        testAclEntry.setAuditFailure(testAuditFailure);
        assertEquals("The getting AuditFailure was not the expected one", testAuditFailure,
            testAclEntry.getAuditFailure());
    }

    /**
     * Test of {@link AclEntry#getUser} and {@link AclEntry#setUser}.<br> Invalid input:
     * <code>null</code><br> Valid input: random {@link User}
     */
    @Test
    public void testGetAndSetUser() {
        User testUser = null;
        Throwable e = null;
        try {
            testAclEntry.setUser(testUser);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the User", e instanceof AssertionError);

        testUser = UserTest.getNewValidUser();
        testAclEntry.setUser(testUser);
        assertEquals("The getting User was not the expected one", testUser, testAclEntry.getUser());
        testAclEntry.setUser(testUser);
        assertEquals("The getting User was not the expected one after setting it twice", testUser,
            testAclEntry.getUser());
    }

    /**
     * Test of {@link AclEntry#getAclObjectIdentity} and {@link AclEntry#setAclObjectIdentity}.<br>
     * Invalid input: <code>null</code><br> Valid input: random {@link AclObjectIdentity}
     */
    @Test
    public void testGetAndSetAclObjectIdentity() {
        AclObjectIdentity testAclObjectIdentity = null;
        Throwable e = null;
        try {
            testAclEntry.setAclObjectIdentity(testAclObjectIdentity);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the AclObjectIdentity",
            e instanceof AssertionError);

        testAclObjectIdentity = AclObjectIdentityTest.getNewValidAclObjectIdentity();
        testAclEntry.setAclObjectIdentity(testAclObjectIdentity);
        assertEquals("The getting AclObjectIdentity was not the expected one",
            testAclObjectIdentity, testAclEntry.getAclObjectIdentity());
        assertTrue(
            "The AclEntry was not in the set of AclEntries after setting the AclObjectIdentity",
            testAclObjectIdentity.getAclEntries().contains(testAclEntry));
        testAclEntry.setAclObjectIdentity(AclObjectIdentityTest.getNewValidAclObjectIdentity());
        assertFalse(
            "The AclEntry was in the set of AclEntries after changing the AclObjectIdentity",
            testAclObjectIdentity.getAclEntries().contains(testAclEntry));
    }

    /**
     * Test of {@link AclEntry#equals}.<br> Valid input: the same {@link AclEntry} twice in a
     * HashSet, the same {@link AclEntry}, <code>null</code>, another {@link AclEntry}, another
     * Object
     */
    @Test
    public void testEquals() {
        HashSet<AclEntry> testSet = new HashSet<>();
        testSet.add(testAclEntry);
        testSet.add(testAclEntry);
        assertEquals("It was possible to set the same AclEntry in one set", 1, testSet.size());

        assertEquals("The AclEntry was not equal to itself", testAclEntry, testAclEntry);
        assertNotEquals("The AclEntry was equal to null", null, testAclEntry);
        AclEntry otherAclEntry = getNewValidAclEntry();
        assertNotEquals("The AclEntry was equal to a different AclEntry", testAclEntry,
            otherAclEntry);
        Object otherObject = new Object();
        assertNotEquals("The AclEntry was equal to a different Object", testAclEntry, otherObject);
    }

    /**
     * Test of {@link AclEntry#toString}.<br> Valid input: random {@link AclEntry} with mocked Id
     */
    @Test
    public void testToString() {
        Long testId = Math.abs(random.nextLong());

        AclEntry spyAclEntry = spy(testAclEntry);
        Mockito.when(spyAclEntry.getId()).thenReturn(testId);

        String testString = "de.imi.mopat.model.AclEntry[ id=" + testId + " ]";
        assertEquals("The getting String was not the expected one", testString,
            spyAclEntry.toString());

    }

}
