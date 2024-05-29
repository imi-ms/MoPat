package de.imi.mopat.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.utils.Helper;
import java.util.HashSet;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class AclClassTest {

    private static final Random random = new Random();
    private AclClass testAclClass;

    public AclClassTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new AclClass
     *
     * @return Returns a valid new AclClass
     */
    public static AclClass getNewValidAclClass() {
        String classname =
            "de.imi.mopat.model." + Helper.getRandomAlphabeticString(random.nextInt(50) + 1);
        AclClass aclClass = new AclClass(classname);
        return aclClass;
    }

    @Before
    public void setUp() {
        testAclClass = getNewValidAclClass();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link AclClass#getClassName} and {@link AclClass#setClassName}.<br> Invalid input:
     * <code>null</code>, less than 23 characters, does not start with 'de.imi.mopat.model.'<br>
     * Valid input: String that starts with 'de.imi.mopat.model.'
     */
    @Test
    public void testGetAndSetClassName() {
        String testClassname = null;
        Throwable e = null;
        try {
            testAclClass = new AclClass(testClassname);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the classname as null", e instanceof AssertionError);

        testClassname = Helper.getRandomAlphabeticString(random.nextInt(21));
        e = null;
        try {
            testAclClass = new AclClass(testClassname);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a classname with less than 21 characters",
            e instanceof AssertionError);

        testClassname = Helper.getRandomAlphabeticString(random.nextInt(50) + 21);
        e = null;
        try {
            testAclClass = new AclClass(testClassname);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a classname that starts not with 'de.imi.mopat.model.'",
            e instanceof AssertionError);

        testClassname =
            "de.imi.mopat.model." + Helper.getRandomAlphabeticString(random.nextInt(50) + 1);
        testAclClass = new AclClass(testClassname);
        assertEquals("The getting classname was not the expected one", testClassname,
            testAclClass.getClassName());
    }

    /**
     * Test of {@link AclClass#equals}.<br> Invalid input: <br> Valid input:
     */
    @Test
    public void testEquals() {
        HashSet<AclClass> testSet = new HashSet<>();
        testSet.add(testAclClass);
        testSet.add(testAclClass);
        assertEquals("It was possible to set the same AclClass in one set", 1, testSet.size());

        assertEquals("The AclClass was not equal to itself", testAclClass, testAclClass);
        assertNotEquals("The AclClass was equal to null", null, testAclClass);
        AclClass otherAclClass = getNewValidAclClass();
        assertNotEquals("The AclClass was equal to a different AclClass", testAclClass,
            otherAclClass);
        Object otherObject = new Object();
        assertNotEquals("The AclClass was equal to a different Object", testAclClass, otherObject);
    }
}
