package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.helper.controller.ClinicService;
import de.imi.mopat.model.dto.ClinicDTO;
import de.imi.mopat.utils.Helper;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class ClinicTest {

    private static final Random random = new Random();
    private Clinic testClinic;

    @Autowired
    private ClinicService clinicService;

    public ClinicTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new Clinic
     *
     * @return a valid new Clinic
     */
    public static Clinic getNewValidClinic() {
        String name = Helper.getRandomAlphabeticString(random.nextInt(252) + 3);
        String description = Helper.getRandomAlphabeticString(random.nextInt(252) + 3);

        Clinic clinic = new Clinic(name, description);
        return clinic;
    }

    @Before
    public void setUp() {
        testClinic = ClinicTest.getNewValidClinic();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link Clinic#equals} method.<br> Valid input: the same {@link Clinic}, another
     * {@link Clinic} and another Object
     */
    @Test
    public void testEquals() {
        HashSet<Clinic> testSet = new HashSet<>();
        testSet.add(testClinic);
        testSet.add(testClinic);
        assertEquals("It was possible to set the same Clinic in one set", 1, testSet.size());

        assertEquals("The Clinic was not equal to itself", testClinic, testClinic);
        assertNotEquals("The Clinic was equal to null", null, testClinic);
        Clinic otherClinic = getNewValidClinic();
        assertNotEquals("The Clinic was equal to a different Bundle", testClinic, otherClinic);
        Object otherObject = new Object();
        assertNotEquals("The Clinic was equal to a different Object", testClinic, otherObject);
    }

    /**
     * Test of {@link Clinic#addBundleClinic} method.<br> Invalid input: <code>null</code><br> Valid
     * input: random number of {@link BundleClinic BundleClinics}
     */
    @Test
    public void testAddBundleClinic() {
        BundleClinic testBundleClinic = null;
        Throwable e = null;
        try {
            testClinic.addBundleClinic(testBundleClinic);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the BundleClinics", e instanceof AssertionError);

        Set<BundleClinic> testSet = new HashSet<>();
        Integer size = random.nextInt(50);

        for (int i = 0; i < size; i++) {
            testBundleClinic = BundleClinicTest.getNewValidBundleClinic();
            testClinic.addBundleClinic(testBundleClinic);
            testSet.add(testBundleClinic);
        }
        // Add a BundleClinic with clinic = null
        testBundleClinic = new BundleClinic();
        testBundleClinic.setPosition(Math.abs(random.nextInt()));
        testClinic.addBundleClinic(testBundleClinic);
        testSet.add(testBundleClinic);
        assertEquals("The getting set of BundleClinics was not he expected one", testSet,
            testClinic.getBundleClinics());
    }

    /**
     * Test of {@link Clinic#getBundleClinics} and {@link Clinic#addBundleClinics(java.util.Set)}
     * method.<br> Invalid input: <code>null</code><br> Valid input: set of random
     * {@link BundleClinic BundleClinics}
     */
    @Test
    public void testGetAndAddBundleClinics() {
        Set<BundleClinic> testSet = null;
        Throwable e = null;
        try {
            testClinic.addBundleClinics(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the BundleClinics", e instanceof AssertionError);

        testSet = new HashSet<>();
        Integer size = random.nextInt(25) + 1;
        for (int i = 0; i < size; i++) {
            testSet.add(BundleClinicTest.getNewValidBundleClinic());
        }
        testClinic.addBundleClinics(testSet);
        assertEquals(
            "Adding set of bundleClinics failed. The returned value didn't match the expected value.",
            testSet, testClinic.getBundleClinics());
    }

    /**
     * Test of {@link Clinic#removeBundleClinic} method.<br> Invalid input: <code>null</code><br>
     * Valid input: random existing {@link BundleClinic}
     */
    @Test
    public void testRemoveBundleClinic() {
        BundleClinic bundleClinic = null;
        Throwable e = null;
        try {
            testClinic.removeBundleClinic(bundleClinic);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the BundleClinics",
            e instanceof AssertionError);

        bundleClinic = BundleClinicTest.getNewValidBundleClinic();
        testClinic.addBundleClinic(bundleClinic);
        testClinic.removeBundleClinic(bundleClinic);
        assertFalse(
            "Removing bundle clinic failed. The returned set contained the bundle clinic although it wasn't expect to do so.",
            testClinic.getBundleClinics().contains(bundleClinic));
        // Try to remove a  BundleClinic, that is not associated to this Clinic
        int countBundleClinics = testClinic.getBundleClinics().size();
        testClinic.removeBundleClinic(BundleClinicTest.getNewValidBundleClinic());
        assertEquals(
            "The set of BundleClinics was altered after trying to remove a not associated BundleClinic",
            countBundleClinics, testClinic.getBundleClinics().size());
    }

    /**
     * Test of {@link Clinic#removeBundleClinics(java.util.Collection)} method.<br> Invalid input:
     * <code>null</code><br> Valid input: random set of existing {@link BundleClinic BundleClinics}
     */
    @Test
    public void testRemoveBundleClinics() {
        Set<BundleClinic> bundleClinics = null;
        Throwable e = null;
        try {
            testClinic.removeBundleClinics(bundleClinics);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the BundleClinics",
            e instanceof AssertionError);

        bundleClinics = new HashSet<>();
        Set<BundleClinic> bundleClinicsToRemove = new HashSet<>();
        Integer size = random.nextInt(200) + 1;
        bundleClinics.add(BundleClinicTest.getNewValidBundleClinic());

        for (int i = 0; i < size; i++) {
            BundleClinic bundleClinic = BundleClinicTest.getNewValidBundleClinic();
            bundleClinics.add(bundleClinic);
            if (random.nextBoolean()) {
                bundleClinicsToRemove.add(bundleClinic);
            }
        }
        testClinic.addBundleClinics(bundleClinics);
        testClinic.removeBundleClinics(bundleClinicsToRemove);
        bundleClinics.removeAll(bundleClinicsToRemove);
        assertEquals(
            "Removing set of bundelClinics failed. The compared sets did not equal although they were expected to do so.",
            bundleClinics, testClinic.getBundleClinics());
    }

    /**
     * Test of {@link Clinic#removeAllBundleClinics} method.
     */
    @Test
    public void testRemoveAllBundleClinics() {
        Clinic clinic = ClinicTest.getNewValidClinic();
        Integer size = random.nextInt(25);

        for (int i = 0; i < size; i++) {
            clinic.addBundleClinic(BundleClinicTest.getNewValidBundleClinic());
        }

        clinic.removeAllBundleClinics();
        Set<BundleClinic> testBundleClinics = clinic.getBundleClinics();
        assertTrue(
            "Removing all bundle clinics failed. The returned set wasn't empty although it was expected to be so.",
            testBundleClinics.isEmpty());
    }

    /**
     * Test of {@link Clinic#getName} and {@link Clinic#setName} method.<br> Invalid input:
     * <code>null</code>, empty String, less than 3 characters, more than 255 characers<br> Valid
     * input: String between 3 and 255 letters after trimmimg
     */
    @Test
    public void testGetAndSetName() {
        String testName = null;
        Throwable e = null;
        try {
            testClinic.setName(testName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the name", e instanceof AssertionError);

        testName = "";
        e = null;
        try {
            testClinic.setName(testName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set an empty name", e instanceof AssertionError);

        testName = Helper.getRandomAlphabeticString(random.nextInt(3));
        e = null;
        try {
            testClinic.setName(testName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a name with less than 3 characters",
            e instanceof AssertionError);

        testName = Helper.getRandomAlphabeticString(random.nextInt(50) + 256);
        e = null;
        try {
            testClinic.setName(testName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a name with more than 255 characters",
            e instanceof AssertionError);

        testName = Helper.getRandomAlphabeticString(random.nextInt(252) + 3);
        testClinic.setName(testName);
        assertEquals("Getting the name failed. The returned name didn't match the expected value",
            testName, testClinic.getName());
    }

    /**
     * Test of {@link Clinic#getDescription} and {@link Clinic#setDescription} method.<br> Invalid
     * input: <code>null</code>, empty String, less than 3 characters, more than 255 characers<br>
     * Valid input: String between 3 and 255 letters after trimmimg
     */
    @Test
    public void testGetDescription() {
        String testDescription = null;
        Throwable e = null;
        try {
            testClinic.setDescription(testDescription);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the description", e instanceof AssertionError);

        testDescription = "";
        e = null;
        try {
            testClinic.setDescription(testDescription);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set an empty description", e instanceof AssertionError);

        testDescription = Helper.getRandomAlphabeticString(random.nextInt(3));
        e = null;
        try {
            testClinic.setDescription(testDescription);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a description with less than 3 characters",
            e instanceof AssertionError);

        testDescription = Helper.getRandomAlphabeticString(random.nextInt(50) + 256);
        e = null;
        try {
            testClinic.setDescription(testDescription);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a description with more than 255 characters",
            e instanceof AssertionError);

        testDescription = Helper.getRandomAlphabeticString(random.nextInt(252) + 3);
        testClinic.setDescription(testDescription);
        assertEquals(
            "Getting the description failed. The returned description didn't match the expected value",
            testDescription, testClinic.getDescription());
    }

    /**
     * Test of {@link Clinic#getEmail} and {@link Clinic#setEmail(java.lang.String)} methods.<br>
     * Valid input: random mail address as String
     */
    public void testSetGetEmail() {
        String testEmail = Helper.getRandomMailAddress();
        testClinic.setEmail(testEmail);
        Assert.assertNotNull(
            "The description field was null although a non-null object was set before",
            testClinic.getEmail());
        Assert.assertEquals("The description field received didn't match the one set", testEmail,
            testClinic.getEmail());
    }

    /**
     * Test of {@link Clinic#toCLinicDTO} method.<br> Valid input: random {@link Clinic}
     */
    @Test
    public void testToClinicDTO() {
        Clinic spyClinic = spy(testClinic);
        Mockito.when(spyClinic.getId()).thenReturn(Math.abs(random.nextLong()));
        Bundle bundle = spy(BundleTest.getNewValidBundle());
        Mockito.when(bundle.getId()).thenReturn(Math.abs(random.nextLong()));
        bundle.setLocalizedFinalText(new TreeMap<>());
        bundle.setLocalizedWelcomeText(new TreeMap<>());
        BundleClinic bundleClinic = BundleClinicTest.getNewValidBundleClinic(spyClinic, bundle);
        spyClinic.addBundleClinic(bundleClinic);
        spyClinic.setEmail(Helper.getRandomMailAddress());
        ClinicDTO clinicDTO = clinicService.toClinicDTO(spyClinic);
        assertEquals(
            "Converting the clinic to DTO failed. The returned id didn't match the expected value.",
            spyClinic.getId(), clinicDTO.getId());
        assertEquals(
            "Converting the clinic to DTO failed. The returned description didn't match the expected value.",
            spyClinic.getDescription(), clinicDTO.getDescription());
        assertEquals(
            "Converting the clinic to DTO failed. The returned email didn't match the expected value.",
            spyClinic.getEmail(), clinicDTO.getEmail());
        assertEquals(
            "Converting the clinic to DTO failed. The returned name didn't match the expected value.",
            spyClinic.getName(), clinicDTO.getName());
        assertEquals(
            "Converting the clinic to DTO failed. The returned set of bundleClinics didn't match the expected value.",
            spyClinic.getBundleClinics().size(), clinicDTO.getBundleClinicDTOs().size());
    }
}
