package de.imi.mopat.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class AclObjectIdentityTest {

    private static final Random random = new Random();
    private AclObjectIdentity testAclObjectIdentity;

    public AclObjectIdentityTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new AclObjectIdentity
     *
     * @return Returns a valid new AclObjectIdentity
     */
    public static AclObjectIdentity getNewValidAclObjectIdentity() {
        Long objectIdIdentity = Math.abs(random.nextLong()) + 1;
        Boolean entriesInheriting = random.nextBoolean();
        AclClass objectIdClass = AclClassTest.getNewValidAclClass();
        User owner = UserTest.getNewValidUser();
        AclObjectIdentity parentObject = null;

        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity(objectIdIdentity,
            entriesInheriting, objectIdClass, owner, parentObject);

        return aclObjectIdentity;
    }

    @Before
    public void setUp() {
        testAclObjectIdentity = getNewValidAclObjectIdentity();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link AclObjectIdentity#getObjectIdIdentity} and
     * {@link AclObjectIdentity#setObjectIdIdentity}.<br> Invalid input: <code>null</code>, Long
     * less than 1<br> Valid input: Long greater than 0
     */
    @Test
    public void testGetAndSetObjectIdIdentity() {
        Long testObjectIdIdentity = null;
        Throwable e = null;
        try {
            testAclObjectIdentity.setObjectIdIdentity(testObjectIdIdentity);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the ObjectIdIdentity",
            e instanceof AssertionError);

        testObjectIdIdentity = Math.abs(random.nextLong()) * (-1);
        e = null;
        try {
            testAclObjectIdentity.setObjectIdIdentity(testObjectIdIdentity);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a value less than 1 as the ObjectIdIdentity",
            e instanceof AssertionError);

        testObjectIdIdentity = Math.abs(random.nextLong()) + 1;
        testAclObjectIdentity.setObjectIdIdentity(testObjectIdIdentity);
        assertEquals("The getting ObjectIdIdentity was not the expected one", testObjectIdIdentity,
            testAclObjectIdentity.getObjectIdIdentity());
    }

    /**
     * Test of {@link AclObjectIdentity#getEntriesInheriting} and
     * {@link AclObjectIdentity#setEntriesInheriting}.<br> Invalid input: <code>null</code><br>
     * Valid input: random Boolean
     */
    @Test
    public void testGetAndSetEntriesInheriting() {
        Boolean testEntriesInheriting = null;
        Throwable e = null;
        try {
            testAclObjectIdentity.setEntriesInheriting(testEntriesInheriting);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the EntriesInheriting",
            e instanceof AssertionError);

        testEntriesInheriting = random.nextBoolean();
        testAclObjectIdentity.setEntriesInheriting(testEntriesInheriting);
        assertEquals("The getting EntriesInheriting was not the expected one",
            testEntriesInheriting, testAclObjectIdentity.getEntriesInheriting());
    }

    /**
     * Test of {@link AclObjectIdentity#getOwner} and {@link AclObjectIdentity#setOwner}.<br>
     * Invalid input: <code>null</code><br> Valid input: random {@link User}
     */
    @Test
    public void testGetAndSetOwner() {
        User testOwner = null;
        Throwable e = null;
        try {
            testAclObjectIdentity.setOwner(testOwner);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Owner", e instanceof AssertionError);

        testOwner = UserTest.getNewValidUser();
        testAclObjectIdentity.setOwner(testOwner);
        assertEquals("The getting Owner was not the expected one", testOwner,
            testAclObjectIdentity.getOwner());
    }

    /**
     * Test of {@link AclObjectIdentity#getObjectIdClass} and
     * {@link AclObjectIdentity#setObjectIdClass}.<br> Invalid input: <code>null</code><br> Valid
     * input: random {@link AclClass}
     */
    @Test
    public void testGetAndSetObjectIdClass() {
        AclClass testObjectIdClass = null;
        Throwable e = null;
        try {
            testAclObjectIdentity.setObjectIdClass(testObjectIdClass);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the ObjectIdClass", e instanceof AssertionError);

        testObjectIdClass = AclClassTest.getNewValidAclClass();
        testAclObjectIdentity.setObjectIdClass(testObjectIdClass);
        assertEquals("The getting ObjectIdClass was not the expected one", testObjectIdClass,
            testAclObjectIdentity.getObjectIdClass());
    }

    /**
     * Test of {@link AclObjectIdentity#getParentObject} and
     * {@link AclObjectIdentity#setParentObject}.<br> Valid input: random {@link AclObjectIdentity}
     */
    @Test
    public void testGetAndSetParentObject() {
        AclObjectIdentity testParent = getNewValidAclObjectIdentity();
        testAclObjectIdentity.setParentObject(testParent);
        assertEquals("The getting parent was not the expected one", testParent,
            testAclObjectIdentity.getParentObject());
    }

    /**
     * Test of {@link AclObjectIdentity#getAclEntries}, {@link AclObjectIdentity#addAclEntries} and
     * {@link AclObjectIdentity#removeAclEntries}.<br> Valid input: random number of
     * {@link AclEntry AclEntries}
     */
    @Test
    public void testGetAddAndRemoveAclEntries() {
        AclEntry testEntry;
        Set<AclEntry> testSet = new HashSet<>();
        Set<AclEntry> removeSet = new HashSet<>();
        int countEntries = random.nextInt(250) + 1;
        for (int i = 0; i < countEntries; i++) {
            testEntry = AclEntryTest.getNewValidAclEntry();
            testAclObjectIdentity.addAclEntry(testEntry);
            testSet.add(testEntry);
            if (random.nextBoolean()) {
                removeSet.add(testEntry);
            }
        }
        assertEquals("The getting set of AclEntries was not the expected one", testSet,
            testAclObjectIdentity.getAclEntries());

        for (AclEntry removeEntry : removeSet) {
            testAclObjectIdentity.removeAclEntry(removeEntry);
        }
        testSet.removeAll(removeSet);
        assertEquals(
            "The getting set of AclEntries was not the expected one after removing some entries",
            testSet, testAclObjectIdentity.getAclEntries());
    }

    /**
     * Test of {@link AclObjectIdentity#equals}.<br> Valid input: the same {@link AclObjectIdentity}
     * twice in a HashSet, the same {@link AclObjectIdentity}, <code>null</code>, another
     * {@link AclObjectIdentity}, another Object
     */
    @Test
    public void testEquals() {
        HashSet<AclObjectIdentity> testSet = new HashSet<>();
        testSet.add(testAclObjectIdentity);
        testSet.add(testAclObjectIdentity);
        assertEquals("It was possible to set the same AclObjectIdentity in one set", 1,
            testSet.size());

        assertEquals("The AclObjectIdentity was not equal to itself", testAclObjectIdentity,
            testAclObjectIdentity);
        assertNotEquals("The AclObjectIdentity was equal to null", null, testAclObjectIdentity);
        Object otherObject = new Object();
        assertNotEquals("The AclObjectIdentity was equal to a different Object",
            testAclObjectIdentity, otherObject);
        AclObjectIdentity otherAclObjectIdentity = getNewValidAclObjectIdentity();
        otherAclObjectIdentity.setObjectIdClass(testAclObjectIdentity.getObjectIdClass());
        otherAclObjectIdentity.setObjectIdIdentity(testAclObjectIdentity.getObjectIdIdentity() + 1);
        assertNotEquals(
            "The AclObjectIdentity was equal to a different AclObjectIdentity with another ObjectIdIdentity",
            testAclObjectIdentity, otherAclObjectIdentity);
        otherAclObjectIdentity.setObjectIdClass(AclClassTest.getNewValidAclClass());
        assertNotEquals(
            "The AclObjectIdentity was equal to a different AclObjectIdentity with another ObjectIdClass",
            testAclObjectIdentity, otherAclObjectIdentity);
    }

    /**
     * Test of {@link AclObjectIdentity#toString}.<br> Valid input: random {@link AclObjectIdentity}
     * with mocked Id
     */
    @Test
    public void testToString() {
        Long testId = Math.abs(random.nextLong());

        AclObjectIdentity spyAclObjectIdentity = spy(testAclObjectIdentity);
        Mockito.when(spyAclObjectIdentity.getId()).thenReturn(testId);

        String testString = "de.imi.mopat.model.AclObjectIdentity[ id=" + testId + " ]";
        assertEquals("The getting String was not the expected one", testString,
            spyAclObjectIdentity.toString());
    }

    /**
     * Test of {@link AclObjectIdentity#getInvitation} and
     * {@link AclObjectIdentity#setInvitation}.<br> Valid input: set with random number of
     * {@link Invitation Invitations}
     */
    @Test
    public void testGetAndSetInvitation() {
        Set<Invitation> testSet = new HashSet<>();
        int countInvitations = random.nextInt(50);
        for (int i = 0; i < countInvitations; i++) {
            testSet.add(InvitationTest.getNewValidInvitation());
        }
        testAclObjectIdentity.setInvitation(testSet);
        assertEquals("The getting set of Invitations was not the expected one", testSet,
            testAclObjectIdentity.getInvitation());
    }
}
