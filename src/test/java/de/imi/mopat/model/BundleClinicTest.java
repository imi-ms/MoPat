package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
public class BundleClinicTest {

    private static final Random random = new Random();
    private BundleClinic testBundleClinic;

    public BundleClinicTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Initialize a valid BundleClinic Object
     *
     * @return A valid {@link BundleClinic BundleClinic} Object
     */
    public static BundleClinic getNewValidBundleClinic() {
        int position = Math.abs(random.nextInt()) + 1;
        Clinic clinic = ClinicTest.getNewValidClinic();
        Bundle bundle = BundleTest.getNewValidBundle();

        BundleClinic bundleClinic = new BundleClinic(position, clinic, bundle);
        return bundleClinic;
    }

    /**
     * Initialize a valid BundleClinic Object
     *
     * @param clinic {@link Clinic} of this {@link BundleClinic}
     * @param bundle {@link Bundle} of this {@link BundleClinic}
     * @return A valid {@link BundleClinic} Object
     */
    public static BundleClinic getNewValidBundleClinic(Clinic clinic, Bundle bundle) {
        int position = Math.abs(random.nextInt());

        BundleClinic bundleClinic = new BundleClinic(position, clinic, bundle);
        return bundleClinic;
    }

    @Before
    public void setUp() {
        testBundleClinic = getNewValidBundleClinic();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link BundleClinic#getClinic} and {@link BundleClinic#setClinic}.<br> Invalid
     * input:
     * <code>null</code><br> Valid input: random {@link Clinic}
     */
    @Test
    public void testGetAndSetClinic() {
        Clinic testClinic = null;
        Throwable e = null;
        try {
            testBundleClinic.setClinic(null);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the clinic as null", e instanceof AssertionError);

        testClinic = ClinicTest.getNewValidClinic();
        testBundleClinic.setClinic(testClinic);
        assertEquals("The getting Clinic was not the expected one", testClinic,
            testBundleClinic.getClinic());
    }

    /**
     * Test of {@link BundleClinic#getBundle} and {@link BundleClinic#setBundle}.<br> Invalid
     * input:
     * <code>null</code><br> Valid input: Valid Bundle from {@link BundleTest#getNewValidBundle}
     */
    @Test
    public void testGetAndSetBundle() {
        Bundle testBundle = null;
        Throwable e = null;
        try {
            testBundleClinic.setBundle(testBundle);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the bundle as null", e instanceof AssertionError);

        testBundle = BundleTest.getNewValidBundle();
        testBundleClinic.setBundle(testBundle);
        assertEquals("The getting bundle was not the expected one", testBundle,
            testBundleClinic.getBundle());
    }

    /**
     * Test of {@link BundleClinic#getPosition} and {@link BundleClinic#setPosition}.<br> Invalid
     * input: random negative Integer<br> Valid input: random positive Integer
     */
    @Test
    public void testGetAndSetPosition() {
        Integer position = Math.abs(random.nextInt() + 1) * -1;
        Throwable e = null;
        try {
            testBundleClinic.setPosition(position);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It ws possible to set a negative position", e instanceof AssertionError);

        position *= -1;
        testBundleClinic.setPosition(position);
        assertEquals("The position was not set correctly", position,
            testBundleClinic.getPosition());
    }

    /**
     * Test of {@link BundleClinic#removeBundle}.<br> Valid input: random valid {@link BundleClinic}
     * with random valid {@link Bundle}
     */
    @Test
    public void testRemoveBundle() {
        Bundle testBundle = testBundleClinic.getBundle();
        testBundleClinic.removeBundle();
        assertNull("The Bundle was not null after removing it", testBundleClinic.getBundle());
        assertFalse("The BundleClinic was not deleted in the Bundle",
            testBundle.getBundleClinics().contains(testBundleClinic));
        testBundleClinic.removeBundle();
        assertNull("The Bundle was altered after removing it twice", testBundleClinic.getBundle());
    }

    /**
     * Test of {@link BundleClinic#removeClinic}.<br> Valid input: random valid {@link BundleClinic}
     * with random valid {@link Clinic}
     */
    @Test
    public void testRemoveClinic() {
        Clinic testClinic = testBundleClinic.getClinic();
        testBundleClinic.removeClinic();
        assertNull("The Clinic was not null after removing it", testBundleClinic.getClinic());
        assertFalse("The BundleClinic was not deleted in the Clinic",
            testClinic.getBundleClinics().contains(testBundleClinic));
        testBundleClinic.removeClinic();
        assertNull("The Clinic was altered after removing it twice", testBundleClinic.getClinic());
    }

    /**
     * Test of {@link BundleClinic#equals}.<br> Invalid input: the same {@link BundleClinic} twice
     * in one HashSet, another {@link BundleClinic} and another Object
     */
    @Test
    public void testEquals() {
        HashSet<BundleClinic> testSet = new HashSet<>();
        testSet.add(testBundleClinic);
        testSet.add(testBundleClinic);
        assertEquals("It was possible to set the same BundleClinic twice in one set", 1,
            testSet.size());

        assertEquals("The BundleClinic was not equal to itself", testBundleClinic,
            testBundleClinic);
        assertNotEquals("The BundleClinic was equal to null", null, testBundleClinic);
        BundleClinic otherBundleClinic = getNewValidBundleClinic();
        assertNotEquals("The BundleClinic was equal to a different BundleClinic", testBundleClinic,
            otherBundleClinic);
        Object otherObject = new Object();
        assertNotEquals("The BundleClinic was equal to a different Object", testBundleClinic,
            otherObject);
    }

    /**
     * Test of {@link BundleClinic#compareTo}.<br> Valid input: same position, lower postion,
     * greater position
     */
    @Test
    public void testCompareTo() {
        BundleClinic compareBundleClinic = getNewValidBundleClinic();
        int greater = Math.abs(random.nextInt()) + 2;
        int lower = random.nextInt(greater) + 1;
        testBundleClinic.setPosition(greater);
        compareBundleClinic.setPosition(greater);
        assertEquals("CompareTo with the same position was not 0", 0,
            testBundleClinic.compareTo(compareBundleClinic));
        testBundleClinic.setPosition(greater);
        compareBundleClinic.setPosition(lower);
        assertEquals("CompareTo with a lower position was not 1", 1,
            testBundleClinic.compareTo(compareBundleClinic));
        testBundleClinic.setPosition(lower);
        compareBundleClinic.setPosition(greater);
        assertEquals("CompareTo with a lower position was not 1", -1,
            testBundleClinic.compareTo(compareBundleClinic));
    }
}
