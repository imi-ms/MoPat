package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.helper.controller.BundleService;
import de.imi.mopat.helper.model.BundleDTOMapper;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.utils.Helper;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import org.junit.Before;
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
public class BundleTest {

    private static final Random random = new Random();
    private Bundle testBundle;

    @Autowired
    private BundleService bundleService;

    @Autowired
    private BundleDTOMapper bundleDTOMapper;

    public BundleTest() {
    }

    /**
     * Returns a valid new Bundle
     *
     * @return Returns a valid new Bundle
     */
    public static Bundle getNewValidBundle() {
        String name = Helper.getRandomAlphabeticString(random.nextInt(253) + 3);
        String description = Helper.getRandomAlphabeticString(random.nextInt(253) + 3);
        Long changedBy = Math.abs(random.nextLong()) + 1;
        Boolean isPublished = random.nextBoolean();
        Boolean showProgressPerBundle = random.nextBoolean();
        Boolean deactivateProgressAndNameDuringSurvey = random.nextBoolean();

        Bundle bundle = new Bundle(name, description, changedBy, isPublished, showProgressPerBundle,
            deactivateProgressAndNameDuringSurvey);

        Map<String, String> availableLanguages = new HashMap<>();
        int countAvailableLanguages = Math.abs(random.nextInt(50) + 1);
        for (int i = 0; i < countAvailableLanguages; i++) {
            availableLanguages.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(500) + 1));
        }
        for (BundleQuestionnaire testBundleQuestionnaire : bundle.getBundleQuestionnaires()) {
            Map<String, String> questionnaireLanguages = new HashMap<>();
            int countQuestionnaireLanguages = Math.abs(random.nextInt(50) + 1);
            for (int j = 0; j < countQuestionnaireLanguages; j++) {
                questionnaireLanguages.put(Helper.getRandomLocale(),
                    Helper.getRandomString(random.nextInt(500) + 1));
            }
            questionnaireLanguages.putAll(availableLanguages);
            testBundleQuestionnaire.getQuestionnaire()
                .setLocalizedDisplayName(questionnaireLanguages);
            int countQuestionsInQuestionnaire = Math.abs(random.nextInt(50) + 1);
            for (int j = 0; j < countQuestionsInQuestionnaire; j++) {
                int countLanguagesForQuestion = Math.abs(random.nextInt(50));
                Map<String, String> questionLanguages = new HashMap<>();
                for (int k = 0; k < countLanguagesForQuestion; k++) {
                    questionLanguages.put(Helper.getRandomLocale(),
                        Helper.getRandomString(random.nextInt(500) + 1));
                }
                questionLanguages.putAll(availableLanguages);
                QuestionTest.getNewValidQuestion(questionLanguages,
                    testBundleQuestionnaire.getQuestionnaire());
            }
        }
        bundle.setLocalizedWelcomeText(availableLanguages);
        bundle.setLocalizedFinalText(availableLanguages);

        return bundle;
    }

    @Before
    public void setUp() {
        testBundle = getNewValidBundle();
    }

    /**
     * Test of {@link Bundle#setBundleClinics}.<br> Valid input: random set of
     * {@link Clinic clinics}
     */
    @Test
    public void testGetBundleClinics() {
        Set<BundleClinic> testSet = new HashSet<>();

        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            BundleClinic testBundleClinic = BundleClinicTest.getNewValidBundleClinic();
            testBundleClinic.setBundle(testBundle);
            testSet.add(testBundleClinic);
        }

        assertNotNull("The getting set was null", testBundle.getBundleClinics());
        assertEquals("The getting set of BundleClinics was not the expected one", testSet,
            testBundle.getBundleClinics());
    }

    /**
     * Test of {@link Bundle#addBundleClinics}.<br> Invalid input: <code>null</code><br> Valid
     * input: a set of valid {@link BundleClinic BundleClinics}
     */
    @Test
    public void testAddBundleClinics() {
        // Check if AssertionError is thrown correctly if BundleClinic is null
        Set<BundleClinic> testSet = null;
        Throwable e = null;
        try {
            testBundle.addBundleClinics(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the BundleClinics", e instanceof AssertionError);

        // Check if a Set of BundleClinics is added correctly
        testSet = new HashSet<>();
        BundleClinic testBundleClinic;
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testBundleClinic = BundleClinicTest.getNewValidBundleClinic();
            testSet.add(testBundleClinic);
        }
        // Add a BundleClinic without Bundle
        testSet.add(new BundleClinic());
        testBundle.addBundleClinics(testSet);

        assertNotNull("The getting set was null", testBundle.getBundleClinics());
        assertEquals("The getting set of BundleClinics was not the expected one", testSet,
            testBundle.getBundleClinics());
    }

    /**
     * Test of {@link Bundle#addBundleClinic}.<br> Invalid input: <code>null</code><br> Valid input:
     * valid {@link BundleClinic}
     */
    @Test
    public void testAddBundleClinic() {
        // Check if AssertionError is thrown correctly if BundleClinic is null
        BundleClinic testBundleClinic = null;
        Throwable e = null;
        try {
            testBundle.addBundleClinic(testBundleClinic);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the BundleClinics", e instanceof AssertionError);

        // Check if single BundleClinics are added correctly
        Set<BundleClinic> testSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testBundleClinic = BundleClinicTest.getNewValidBundleClinic();
            testBundle.addBundleClinic(testBundleClinic);
            testSet.add(testBundleClinic);
        }
        // Check if the same BundleClinic is not added twice
        testBundle.addBundleClinic(testBundleClinic);

        assertEquals("The same BundleClinic was added twice", testSet.size(),
            testBundle.getBundleClinics().size());
        assertNotNull("The getting set was null", testBundle.getBundleClinics());
        assertEquals("The getting set of BundleClinics was not the expected one", testSet,
            testBundle.getBundleClinics());
    }

    /**
     * Test of {@link Bundle#removeBundleClinic}.<br> Invalid input: <code>null</code><br> Valid
     * input: a random {@link BundleClinic} contained in set of {@link BundleClinic BundleClinics}
     */
    @Test
    public void testRemoveBundleClinic() {
        BundleClinic testBundleClinic = null;
        Throwable e = null;
        try {
            testBundle.removeBundleClinic(testBundleClinic);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the BundleClinics",
            e instanceof AssertionError);

        Set<BundleClinic> testSet = new HashSet<>();
        Set<BundleClinic> removeSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testBundleClinic = BundleClinicTest.getNewValidBundleClinic();
            testSet.add(testBundleClinic);
            if (random.nextBoolean()) {
                removeSet.add(testBundleClinic);
            }
        }
        testBundle.addBundleClinics(testSet);
        assertEquals("The getting set of BundleClinics was not the expected one", testSet,
            testBundle.getBundleClinics());
        // Add a BundleClinic without Bundle to the removeSet
        removeSet.add(new BundleClinic());
        for (BundleClinic bundleClinic : removeSet) {
            testBundle.removeBundleClinic(bundleClinic);
        }
        testSet.removeAll(removeSet);
        assertEquals("The getting set of BundleClinics after removing was not the expected one",
            testSet, testBundle.getBundleClinics());
        testBundle.removeBundleClinic(BundleClinicTest.getNewValidBundleClinic());
        assertEquals(
            "The getting set of BundleClinics after removing a BundleClinic, that was not in the Set before, was altered",
            testSet, testBundle.getBundleClinics());
    }

    /**
     * Test of {@link Bundle#removeAllBundleClinics}.<br> Valid input: random number of
     * {@link BundleClinic BundleClinics}
     */
    @Test
    public void testRemoveAllBundleClinics() {
        BundleClinic testBundleClinic;
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testBundleClinic = BundleClinicTest.getNewValidBundleClinic();
            testBundle.addBundleClinic(testBundleClinic);
        }
        assertTrue("The BundleClinics were not correctly added",
            testBundle.getBundleClinics().size() > 0);
        testBundle.removeAllBundleClinics();
        assertTrue("The BundleClinics were not correctly added",
            testBundle.getBundleClinics().isEmpty());
    }

    /**
     * Test of {@link Bundle#getBundleQuestionnaires}.<br> Valid input: random set of valid
     * {@link BundleQuestionnaire BundleQuestionnaires}
     */
    @Test
    public void testGetBundleQuestionnaires() {
        Set<BundleQuestionnaire> testSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            BundleQuestionnaire testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
            testBundleQuestionnaire.setBundle(testBundle);
            testSet.add(testBundleQuestionnaire);
        }
        assertNotNull("The getting set was null", testBundle.getBundleQuestionnaires());
        assertEquals("The getting set of BundleQuestionnaires was not the expected one", testSet,
            testBundle.getBundleQuestionnaires());
    }

    /**
     * Test of {@link Bundle#addBundleQuestionnaire}.<br> Invalid input: <code>null</code><br> Valid
     * input: random set of valid {@link BundleQuestionnaire BundleQuestionnaires}
     */
    @Test
    public void testAddBundleQuestionnaires() {
        // Check if AssertionError is thrown correctly if BundleQuestionnaire is null
        Set<BundleQuestionnaire> testSet = null;
        Throwable e = null;
        try {
            testBundle.addBundleQuestionnaires(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the BundleQuestionnaires",
            e instanceof AssertionError);

        // Check if a Set of BundleQuestionnaires is added correctly
        testSet = new HashSet<>();
        BundleQuestionnaire testBundleQuestionnaire;
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
            testSet.add(testBundleQuestionnaire);
        }
        testBundle.addBundleQuestionnaires(testSet);

        assertNotNull("The getting set was null", testBundle.getBundleQuestionnaires());
        assertEquals("The getting set of BundleQuestionnaires was not the expected one", testSet,
            testBundle.getBundleQuestionnaires());
    }

    /**
     * Test of {@link Bundle#addBundleQuestionnaire}.<br> Invalid input: <code>null</code><br> Valid
     * input: random number of {@link BundleQuestionnaire BundleQuestionnaires}
     */
    @Test
    public void testAddBundleQuestionnaire() {
        // Check if AssertionError is thrown correctly if BundleQuestionnaire is null
        BundleQuestionnaire testBundleQuestionnaire = null;
        Throwable e = null;
        try {
            testBundle.addBundleQuestionnaire(testBundleQuestionnaire);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the BundleQuestionnaires",
            e instanceof AssertionError);

        // Check if single BundleQuestionnaires are added correctly
        Set<BundleQuestionnaire> testSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
            testBundle.addBundleQuestionnaire(testBundleQuestionnaire);
            testSet.add(testBundleQuestionnaire);
        }
        // Check if the same BundleQuestionnaire is not added twice
        testBundle.addBundleQuestionnaire(testBundleQuestionnaire);
        assertEquals("The same BundleQuestionnaire was added twice", testSet.size(),
            testBundle.getBundleQuestionnaires().size());

        // Add a BundleQuestionnaire without Bundle
        testBundleQuestionnaire = new BundleQuestionnaire();
        testBundleQuestionnaire.setPosition(Math.abs(random.nextInt()) + 1);
        testSet.add(testBundleQuestionnaire);
        testBundle.addBundleQuestionnaire(testBundleQuestionnaire);

        assertNotNull("The getting set was null", testBundle.getBundleQuestionnaires());
        assertEquals("The getting set of BundleQuestionnaires was not the expected one", testSet,
            testBundle.getBundleQuestionnaires());
    }

    /**
     * Test of {@link Bundle#removeBundleQuestionnaire}.<br> Invalid input: <code>null</code><br>
     * Valid input: a random {@link BundleQuestionnaire} contained in set of
     * {@link BundleQuestionnaire BundleQuestionnaires}
     */
    @Test
    public void testRemoveBundleQuestionnaire() {
        BundleQuestionnaire testBundleQuestionnaire = null;
        Throwable e = null;
        try {
            testBundle.removeBundleQuestionnaire(testBundleQuestionnaire);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the BundleQuestionnaires",
            e instanceof AssertionError);

        Set<BundleQuestionnaire> testSet = new HashSet<>();
        Set<BundleQuestionnaire> removeSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
            testSet.add(testBundleQuestionnaire);
            if (random.nextBoolean()) {
                removeSet.add(testBundleQuestionnaire);
            }
        }
        testBundle.addBundleQuestionnaires(testSet);
        assertEquals("The getting set of BundleQuestionnaires was not the expected one", testSet,
            testBundle.getBundleQuestionnaires());

        BundleQuestionnaire testBundleQuestionnaireWithoutBundle = new BundleQuestionnaire();
        testBundle.removeBundleQuestionnaire(testBundleQuestionnaireWithoutBundle);
        assertEquals(
            "The getting set of BundleQuestionnaires was altered after removing a BundleQuestionnaire without a Bundle",
            testSet, testBundle.getBundleQuestionnaires());

        BundleQuestionnaire testBundleQuestionnaireWithOtherBundle = new BundleQuestionnaire();
        testBundleQuestionnaireWithOtherBundle.setBundle(BundleTest.getNewValidBundle());
        testBundle.removeBundleQuestionnaire(testBundleQuestionnaireWithOtherBundle);
        assertEquals(
            "The getting set of BundleQuestionnaires was altered after removing a BundleQuestionnaire with another Bundle",
            testSet, testBundle.getBundleQuestionnaires());

        for (BundleQuestionnaire bundleQuestionnaire : removeSet) {
            testBundle.removeBundleQuestionnaire(bundleQuestionnaire);
        }
        testSet.removeAll(removeSet);
        assertEquals(
            "The getting set of BundleQuestionnaires after removing was not the expected one",
            testSet, testBundle.getBundleQuestionnaires());
    }

    /**
     * Test of {@link Bundle#removeAllBundleQuestionnaires}.<br> Valid input: random number of
     * {@link BundleQuestionnaire BundleQiestionnaires}
     */
    @Test
    public void testRemoveAllBundleQuestionnaires() {
        BundleQuestionnaire testBundleQuestionnaire;
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
            testBundle.addBundleQuestionnaire(testBundleQuestionnaire);
        }
        assertTrue("The BundleQuestionnaires were not correctly added",
            testBundle.getBundleQuestionnaires().size() > 0);
        testBundle.removeAllBundleQuestionnaires();
        assertTrue("The BundleQuestionnaires were not correctly added",
            testBundle.getBundleQuestionnaires().isEmpty());
    }

    /**
     * Test of {@link Bundle#getEncounters}.<br> Valid input: random number of
     * {@link Encounter Encounters}
     */
    @Test
    public void testGetEncounters() {
        Set<Encounter> testSet = new HashSet<>();
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            testSet.add(EncounterTest.getNewValidEncounter(testBundle));
        }
        assertEquals("The getting set of encounters was not the expected one", testSet,
            testBundle.getEncounters());
    }

    /**
     * Test of {@link Bundle#addEncounters}.<br> Invalid input: <code>null</code><br> Valid input:
     * set of {@link Encounter Encounters}
     */
    @Test
    public void testAddEncounters() {
        // Check if it is possible to add null;
        Set<Encounter> testSet = null;
        Throwable e = null;
        try {
            testBundle.addEncounters(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the Encounters", e instanceof AssertionError);

        // Check if it possible to add a valid set of encounters
        testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(50));
        for (int i = 0; i < count; i++) {
            testSet.add(EncounterTest.getNewValidEncounter());
        }
        testBundle.addEncounters(testSet);
        assertEquals("The getting set of encounters was not the expected one", testSet,
            testBundle.getEncounters());
    }

    /**
     * Test of {@link Bundle#addEncounter}.<br> Invalid input: <code>null</code><br> Valid input:
     * random number of {@link Encounter Encounters}
     */
    @Test
    public void testAddEncounter() {
        // Check if it is possible to add null
        Encounter testEncounter = null;
        Throwable e = null;
        try {
            testBundle.addEncounter(testEncounter);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the Encounters", e instanceof AssertionError);

        // Check if it possible to add a valid encounter
        Set<Encounter> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(50));
        for (int i = 0; i < count; i++) {
            testEncounter = EncounterTest.getNewValidEncounter();
            testBundle.addEncounter(testEncounter);
            testSet.add(testEncounter);
        }
        assertEquals("The getting set of encounters was not the expected one", testSet,
            testBundle.getEncounters());

        Encounter testEncounterWithBundleNull = new Encounter();
        testBundle.addEncounter(testEncounterWithBundleNull);
        testSet.add(testEncounterWithBundleNull);
        assertEquals(
            "The getting set of encounters was not the expected one after adding an Encounter without a Bundle",
            testSet, testBundle.getEncounters());
    }

    /**
     * Test of {@link Bundle#removeEncounter}.<br> Invalid input: <code>null</code><br> Valid input:
     * random number of {@link Encounter Encounters}
     */
    @Test
    public void testRemoveEncounter() {
        Encounter testEncounter = null;
        Throwable e = null;
        try {
            testBundle.removeEncounter(testEncounter);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the Encounters",
            e instanceof AssertionError);

        Set<Encounter> testSet = new HashSet<>();
        Set<Encounter> removeSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testEncounter = EncounterTest.getNewValidEncounter();
            testSet.add(testEncounter);
            if (random.nextBoolean()) {
                removeSet.add(testEncounter);
            }
        }
        testBundle.addEncounters(testSet);
        assertEquals("The getting set of Encounters was not the expected one", testSet,
            testBundle.getEncounters());

        testBundle.removeEncounter(EncounterTest.getNewValidEncounter());
        assertEquals(
            "The getting set of Encounters was altered after removing an Encounter with another Bundle",
            testSet, testBundle.getEncounters());

        for (Encounter encounter : removeSet) {
            testBundle.removeEncounter(encounter);
        }
        testSet.removeAll(removeSet);
        assertEquals("The getting set of Encounters after removing was not the expected one",
            testSet, testBundle.getEncounters());
    }

    /**
     * Test of {@link Bundle#getName} and {@link Bundle#setName}.<br> Invalid input:
     * <code>null</code>, shorter than 3 letters, longer than 255 letters<br> Valid input: random
     * String
     */
    @Test
    public void testGetAndSetName() {
        // Check if it is possible toset the name as null
        String newName = null;
        Throwable e = null;
        try {
            testBundle.setName(newName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the name as null", e instanceof AssertionError);

        // Check if it is possible to set a shorter name than 3 letters
        newName = Helper.getRandomString(random.nextInt(3));
        e = null;
        try {
            testBundle.setName(newName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a name with less than 3 letters",
            e instanceof AssertionError);

        // Check if it is possible to set a longer name than 255 letters
        newName = Helper.getRandomString(random.nextInt(200) + 256);
        e = null;
        try {
            testBundle.setName(newName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a name with more than 255 letters",
            e instanceof AssertionError);

        // Check if it is generally possible to set a valid name
        newName = Helper.getRandomAlphabeticString(random.nextInt(253) + 3);
        testBundle.setName(newName);
        assertEquals("The name was not set correctly", newName, testBundle.getName());
    }

    /**
     * Test of {@link Bundle#getDescription} and {@link Bundle#setDescription}.<br> Invalid input:
     * <code>null</code>, shorter than 3 letters, longer than 255 letters<br> Valid input: random
     * String
     */
    @Test
    public void testGetAndSetDescription() {
        // Check if it is possible toset the description as null
        String newDescription = null;
        Throwable e = null;
        try {
            testBundle.setDescription(newDescription);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the description as null", e instanceof AssertionError);

        // Check if it is possible to set a shorter descrition than 3 letters
        newDescription = Helper.getRandomString(random.nextInt(3));
        e = null;
        try {
            testBundle.setDescription(newDescription);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a description with less than 3 letters",
            e instanceof AssertionError);

        // Check if it is possible to set a longer description than 255 letters
        newDescription = Helper.getRandomString(random.nextInt(200) + 256);
        e = null;
        try {
            testBundle.setDescription(newDescription);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a description with more than 255 letters",
            e instanceof AssertionError);

        // Check if it is generally possible to set a valid description
        newDescription = Helper.getRandomAlphabeticString(random.nextInt(253) + 3);
        testBundle.setDescription(newDescription);
        assertEquals("The description was not set correctly", newDescription,
            testBundle.getDescription());
    }

    /**
     * Test of {@link Bundle#getCreatedAt}.<br>
     */
    @Test
    public void testGetCreatedAt() {
        assertTrue("The getting createdAt was not equal to now",
            System.currentTimeMillis() - testBundle.getCreatedAt().getTime() < 100);
    }

    /**
     * Test of {@link Bundle#getChangedBy} and {@link Bundle#setChangedBy}.<br> Invalid input:
     * <code>null</code>, negative value<br> Valid input: random positive Long
     */
    @Test
    public void testGetAndSetChangedBy() {
        // Check if it is possible to set the changedBy as null
        Long newChangedBy = null;
        Throwable e = null;
        try {
            testBundle.setChangedBy(newChangedBy);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the changedBy as null", e instanceof AssertionError);

        // Check if it is possible to set a negative value
        newChangedBy = (Math.abs(random.nextLong()) + 1) * -1;
        e = null;
        try {
            testBundle.setChangedBy(newChangedBy);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative changedBy", e instanceof AssertionError);

        // Check if it is possible to set a valid changedBy
        newChangedBy = Math.abs(random.nextLong()) + 1;
        testBundle.setChangedBy(newChangedBy);
        assertEquals("The changedBy was not set correctly", newChangedBy,
            testBundle.getChangedBy());
    }

    /**
     * Test of {@link Bundle#getUpdatedAt} and {@link Bundle#setUpdatedAt}.<br> Invalid input:
     * <code>null</code>, Timestamp in the future<br> Valid input: current Timestamp
     */
    @Test
    public void testGetAndSetUpdatedAt() {
        // Check if it is possible to set the updatedAt as null
        Timestamp newUpdatedAt = null;
        Throwable e = null;
        try {
            testBundle.setUpdatedAt(newUpdatedAt);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the updatedAt as null", e instanceof AssertionError);

        // Check if it is possible to set a vlaue in the future
        newUpdatedAt = new Timestamp(System.currentTimeMillis() + 1000000);
        e = null;
        try {
            testBundle.setUpdatedAt(newUpdatedAt);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the updatedAt in th future",
            e instanceof AssertionError);

        // Check if it is possible to set a valid updatedAt
        newUpdatedAt = new Timestamp(System.currentTimeMillis());
        testBundle.setUpdatedAt(newUpdatedAt);
        assertEquals("The updatedAt was not set correctly", newUpdatedAt,
            testBundle.getUpdatedAt());
    }

    /**
     * Test of {@link Bundle#setDeactivateProgressAndNameDuringSurvey} and
     * {@link Bundle#setDeactivateProgressAndNameDuringSurvey}.<br> Invalid input:
     * <code>null</code><br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetDeactivateProgressAndNameDuringSurvey() {
        Boolean newDeactivateProgressAndNameDuringSurvey = null;
        Throwable e = null;
        try {
            testBundle.setDeactivateProgressAndNameDuringSurvey(
                newDeactivateProgressAndNameDuringSurvey);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the deactivateProgressAndNameDuringSurvey as null",
            e instanceof AssertionError);

        // Check if it is possible to set a valid deactivateProgressAndNameDuringSurvey
        newDeactivateProgressAndNameDuringSurvey = random.nextBoolean();
        testBundle.setDeactivateProgressAndNameDuringSurvey(
            newDeactivateProgressAndNameDuringSurvey);
        assertEquals("The deactivateProgressAndNameDuringSurvey was not set correctly",
            newDeactivateProgressAndNameDuringSurvey,
            testBundle.getDeactivateProgressAndNameDuringSurvey());
    }

    /**
     * Test of {@link Bundle#getIsPublished} and {@link Bundle#setIsPublished}.<br> Invalid input:
     * <code>null</code><br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetIsPublished() {
        // Check if it is possible to set isPublished as null
        Boolean newIsPublished = null;
        Throwable e = null;
        try {
            testBundle.setIsPublished(newIsPublished);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the isPublished as null", e instanceof AssertionError);

        // Check if it possible to set a valid isPublished
        newIsPublished = random.nextBoolean();
        testBundle.setIsPublished(newIsPublished);
        assertEquals("The isPublished was not set correctly", newIsPublished,
            testBundle.getIsPublished());
    }

    /**
     * Test of {@link Bundle#getShowProgressPerBundle} and
     * {@link Bundle#setShowProgressPerBundle}.<br> Invalid input: <code>null</code><br> Valid
     * input: random Boolean
     */
    @Test
    public void testGetAndSetShowProgressPerBundle() {
        // Check if it is possible to set showProgressPerBundle as null
        Boolean newShowProgressPerBundle = null;
        Throwable e = null;
        try {
            testBundle.setShowProgressPerBundle(newShowProgressPerBundle);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the showProgressPerBundle as null",
            e instanceof AssertionError);

        // Check if it is possible to set a valid showProgressPerBundle
        newShowProgressPerBundle = random.nextBoolean();
        testBundle.setShowProgressPerBundle(newShowProgressPerBundle);
        assertEquals("The showProgressPerBundle was not set correctly", newShowProgressPerBundle,
            testBundle.getShowProgressPerBundle());
    }

    /**
     * Test of {@link Bundle#isHasConditions} and {@link Bundle#setHasConditions}.<br> Valid input:
     * random Boolean
     */
    @Test
    public void testSetAndIsHasConditions() {
        Boolean newHasConditions = random.nextBoolean();
        testBundle.setHasConditions(newHasConditions);
        assertEquals("The getting hasConditions was not the expected one", newHasConditions,
            testBundle.isHasConditions());
    }

    /**
     * Test of {@link Bundle#getLocalizedWelcomeText} and
     * {@link Bundle#setLocalizedWelcomeText}.<br> Valid input: random map of LocalizedWelcomeTexts
     */
    @Test
    public void testGetAndSetLocalizedWelcomeText() {
        Map<String, String> newLocalizedWelcomeText = new HashMap<>();
        int count = Math.abs(random.nextInt(50));
        for (int i = 0; i < count; i++) {
            newLocalizedWelcomeText.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(500) + 1));
        }
        testBundle.setLocalizedWelcomeText(newLocalizedWelcomeText);
        assertEquals("The getting LocalizedWelcomeText was not the expected one",
            newLocalizedWelcomeText, testBundle.getLocalizedWelcomeText());
    }

    /**
     * Test of {@link Bundle#getLocalizedFinalText} and {@link Bundle#setLocalizedFinalText}.<br>
     * Valid input: random mpa of LocalizedFinalTexts
     */
    @Test
    public void testGetAndSetLocalizedFinalText() {
        Map<String, String> newLocalizedFinalText = new HashMap<>();
        int count = Math.abs(random.nextInt(50));
        for (int i = 0; i < count; i++) {
            newLocalizedFinalText.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(500) + 1));
        }
        testBundle.setLocalizedFinalText(newLocalizedFinalText);
        assertEquals("The getting LocalizedFinalText was not the expected one",
            newLocalizedFinalText, testBundle.getLocalizedFinalText());
    }

    /**
     * Test of {@link Bundle#getAvailableLanguages}.<br> Valid input: random languages in every
     * {@link Questionnaire} and their associated {@link Question Questions}
     */
    @Test
    public void testGetAvailableLanguages() {
        Map<String, String> availableLanguages = new HashMap<>();
        int countAvailableLanguages = Math.abs(random.nextInt(50) + 1);
        for (int i = 0; i < countAvailableLanguages; i++) {
            availableLanguages.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(500) + 1));
        }

        BundleQuestionnaire testBundleQuestionnaire;
        int countBundleQuestionnaires = Math.abs(random.nextInt(50) + 1);
        for (int i = 0; i < countBundleQuestionnaires; i++) {
            testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
            testBundleQuestionnaire.setBundle(testBundle);
            Map<String, String> questionnaireLanguages = new HashMap<>();
            int countQuestionnaireLanguages = Math.abs(random.nextInt(50) + 1);
            for (int j = 0; j < countQuestionnaireLanguages; j++) {
                questionnaireLanguages.put(Helper.getRandomLocale(),
                    Helper.getRandomString(random.nextInt(500) + 1));
            }
            questionnaireLanguages.putAll(availableLanguages);
            testBundleQuestionnaire.getQuestionnaire()
                .setLocalizedDisplayName(questionnaireLanguages);

            int countQuestionsInQuestionnaire = Math.abs(random.nextInt(50) + 1);
            for (int j = 0; j < countQuestionsInQuestionnaire; j++) {
                int countLanguagesForQuestion = Math.abs(random.nextInt(50));
                Map<String, String> questionLanguages = new HashMap<>();
                for (int k = 0; k < countLanguagesForQuestion; k++) {
                    questionLanguages.put(Helper.getRandomLocale(),
                        Helper.getRandomString(random.nextInt(500) + 1));
                }
                questionLanguages.putAll(availableLanguages);
                QuestionTest.getNewValidQuestion(questionLanguages,
                    testBundleQuestionnaire.getQuestionnaire());
            }
        }

        // Add null and an empty string to the map to test if they are ignored
        Map<String, String> availableLanguagesWithEmptyAndNull = new HashMap<>(availableLanguages);
        availableLanguagesWithEmptyAndNull.put(Helper.getRandomLocale(), null);
        availableLanguagesWithEmptyAndNull.put(Helper.getRandomLocale(), "");

        testBundle.setLocalizedWelcomeText(availableLanguagesWithEmptyAndNull);
        testBundle.setLocalizedFinalText(availableLanguagesWithEmptyAndNull);

        List<String> testList = new ArrayList<>(availableLanguages.keySet());
        assertTrue("The getting map of available languages was not the expected one",
            testList.containsAll(testBundle.getAvailableLanguages())
                && testBundle.getAvailableLanguages().containsAll(testList));
    }

    /**
     * Test of {@link Bundle#getNumberOfAssignedExportTemplate}.<br> Valid input: random number of
     * {@link ExportTemplate ExportTemplates
     */
    @Test
    public void testGetNumberOfAssignedExportTemplate() {
        int countExportTemplates = 0;
        int countBundleQuestionnaires = Math.abs(random.nextInt(50));
        for (int i = 0; i < countBundleQuestionnaires; i++) {
            BundleQuestionnaire testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
            testBundleQuestionnaire.setBundle(testBundle);
            int countBundleQuestionnairesExportTemplates = Math.abs(random.nextInt(50));
            for (int j = 0; j < countBundleQuestionnairesExportTemplates; j++) {
                testBundleQuestionnaire.addExportTemplate(
                    ExportTemplateTest.getNewValidExportTemplate());
            }
            countExportTemplates += countBundleQuestionnairesExportTemplates;
        }
        assertEquals("The getting NumberOfAssignedExportTemplate was not the expected one",
            countExportTemplates, testBundle.getNumberOfAssignedExportTemplate());
    }

    /**
     * Test of {@link Bundle#getAllAssignedExportTemplates}.<br> Valid input: random
     * {@link ExportTemplate ExportTemplates}
     */
    @Test
    public void testGetAllAssignedExportTemplates() {
        Set<ExportTemplate> testSet = new HashSet<>();
        ExportTemplate testExportTemplate;
        int countBundleQuestionnaires = Math.abs(random.nextInt(50));
        for (int i = 0; i < countBundleQuestionnaires; i++) {
            BundleQuestionnaire testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
            testBundleQuestionnaire.setBundle(testBundle);
            int countBundleQuestionnairesExportTemplates = Math.abs(random.nextInt(50));
            for (int j = 0; j < countBundleQuestionnairesExportTemplates; j++) {
                testExportTemplate = ExportTemplateTest.getNewValidExportTemplate();
                testBundleQuestionnaire.addExportTemplate(testExportTemplate);
                testSet.add(testExportTemplate);
            }
        }
        assertEquals("The getting set of allAssiginedExportTemplate was not the expected one",
            testSet, testBundle.getAllAssignedExportTemplates());
    }

    /**
     * Test of {@link Bundle#isDeletable}.<br> Invalid input: {@link Bundle} with an
     * {@link Encounter}<br> Valid input: new {@link Bundle}
     */
    @Test
    public void testIsDeletable() {
        assertTrue("A new bundle was not deletable", testBundle.isDeletable());
        testBundle.addEncounter(EncounterTest.getNewValidEncounter());
        assertFalse("A bundle with an encounter was deleteable", testBundle.isDeletable());
    }

    /**
     * Test of {@link Bundle#isModifiable}.<br> Invalid input: {@link Bundle} with an unfinished
     * {@link Encounter}<br> Valid input: new {@link Bundle}, {@link Bundle} with an completed
     * {@link Encounter} (Endtime = current time + 30 minutes)
     */
    @Test
    public void testIsModifiable() {
        assertTrue("A new bundle was not modifiable", testBundle.isModifiable());
        Encounter testEncounter = EncounterTest.getNewValidEncounter();
        testBundle.addEncounter(testEncounter);
        assertFalse("A bundle with an unfinished encounter was modifiable",
            testBundle.isModifiable());
        testEncounter.setEndTime(new Timestamp(System.currentTimeMillis() + 1800000));
        assertTrue("A bundle with an completed encounter was not modifiable",
            testBundle.isModifiable());
    }

    /**
     * Test of {@link Bundle#usedInClinics}.<br> Invalid input: new {@link Bundle}<br> Valid input:
     * {@link Bundle} with a {@link Clinic}
     */
    @Test
    public void testUsedInClinics() {
        assertFalse("A new bundle was used in clinics", testBundle.usedInClinics());
        testBundle.addBundleClinic(BundleClinicTest.getNewValidBundleClinic());
        assertTrue("A bundle with a clinic was not used in clinics", testBundle.usedInClinics());
    }

    /**
     * Test of {@link Bundle#toBundleDTO}.
     */
    @Test
    public void testToBundleDTO() {
        Bundle spyBundle = spy(getNewValidBundle());
        Mockito.when(spyBundle.getId()).thenReturn(Math.abs(random.nextLong()));

        // Add some Questionnaires
        int count = random.nextInt(50);
        for (int i = 0; i < count; i++) {
            Questionnaire spyQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
            Mockito.when(spyQuestionnaire.getId()).thenReturn(Math.abs(random.nextLong()));
            Mockito.when(spyQuestionnaire.getLocalizedWelcomeText()).thenReturn(new TreeMap<>());
            Mockito.when(spyQuestionnaire.getLocalizedFinalText()).thenReturn(new TreeMap<>());
            BundleQuestionnaire testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire(
                spyQuestionnaire);
            testBundleQuestionnaire.setBundle(spyBundle);
        }
        // Add a Questionnaire with ID = null
        BundleQuestionnaireTest.getNewValidBundleQuestionnaire(
            QuestionnaireTest.getNewValidQuestionnaire()).setBundle(spyBundle);

        // Set Languages
        Map<String, String> availableLanguages = new HashMap<>();
        int countAvailableLanguages = Math.abs(random.nextInt(50) + 1);
        for (int i = 0; i < countAvailableLanguages; i++) {
            availableLanguages.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(500) + 1));
        }
        for (BundleQuestionnaire testBundleQuestionnaire : spyBundle.getBundleQuestionnaires()) {
            Map<String, String> questionnaireLanguages = new HashMap<>();
            int countQuestionnaireLanguages = Math.abs(random.nextInt(50) + 1);
            for (int j = 0; j < countQuestionnaireLanguages; j++) {
                questionnaireLanguages.put(Helper.getRandomLocale(),
                    Helper.getRandomString(random.nextInt(500) + 1));
            }
            questionnaireLanguages.putAll(availableLanguages);
            testBundleQuestionnaire.getQuestionnaire()
                .setLocalizedDisplayName(questionnaireLanguages);
            int countQuestionsInQuestionnaire = Math.abs(random.nextInt(50) + 1);
            for (int j = 0; j < countQuestionsInQuestionnaire; j++) {
                int countLanguagesForQuestion = Math.abs(random.nextInt(50));
                Map<String, String> questionLanguages = new HashMap<>();
                for (int k = 0; k < countLanguagesForQuestion; k++) {
                    questionLanguages.put(Helper.getRandomLocale(),
                        Helper.getRandomString(random.nextInt(500) + 1));
                }
                questionLanguages.putAll(availableLanguages);
                QuestionTest.getNewValidQuestion(questionLanguages,
                    testBundleQuestionnaire.getQuestionnaire());
            }
        }
        spyBundle.setLocalizedWelcomeText(availableLanguages);
        spyBundle.setLocalizedFinalText(availableLanguages);

        BundleDTO testBundleDTO = bundleDTOMapper.apply(Boolean.TRUE, spyBundle);

        assertEquals("The getting ID was not the expected one", spyBundle.getId(),
            testBundleDTO.getId());
        assertEquals("The getting name was not the expected one", spyBundle.getName(),
            testBundleDTO.getName());
        assertEquals("The getting set of available languages was not the expected one",
            spyBundle.getAvailableLanguages(), testBundleDTO.getAvailableLanguages());
        assertEquals("The getting isPublished was not the expected one", spyBundle.getIsPublished(),
            testBundleDTO.getIsPublished());
        assertEquals("The getting isModifiable was not the expected one", spyBundle.isModifiable(),
            testBundleDTO.getIsModifiable());
        assertEquals("The getting description was not the expected one", spyBundle.getDescription(),
            testBundleDTO.getDescription());
        assertEquals("The getting LocalizedWelcomeText was not the expected one",
            spyBundle.getLocalizedWelcomeText(), testBundleDTO.getLocalizedWelcomeText());
        assertEquals("The getting LocalizedFinaltext was not the expected one",
            spyBundle.getLocalizedFinalText(), testBundleDTO.getLocalizedFinalText());
        assertEquals("The getting deactivateProgressAndNameDuringSurvey was not the expected one",
            spyBundle.getDeactivateProgressAndNameDuringSurvey(),
            testBundleDTO.getdeactivateProgressAndNameDuringSurvey());
        assertEquals("The getting ShowProgressPerBundle was not the expected one",
            spyBundle.getShowProgressPerBundle(), testBundleDTO.getShowProgressPerBundle());
        assertEquals("The getting BundleQuestionnaires size was not the expected one",
            spyBundle.getBundleQuestionnaires().size() - 1,
            testBundleDTO.getBundleQuestionnaireDTOs().size());
    }

    /**
     * Test of {@link Bundle#equals}.<br> Invalid input: the same Bundle twice in a HashSet
     */
    @Test
    public void testEquals() {
        HashSet<Bundle> testSet = new HashSet<>();
        testSet.add(testBundle);
        testSet.add(testBundle);
        assertEquals("It was possible to add the same Bundle twice to one set", 1, testSet.size());

        assertEquals("The Bundle was not equal to itself", testBundle, testBundle);
        assertNotEquals("The Bundle was equal to null", null, testBundle);
        Bundle otherBundle = getNewValidBundle();
        assertNotEquals("The Bundle was equal to a different Bundle", testBundle, otherBundle);
        Object otherObject = new Object();
        assertNotEquals("The Bundle was equal to a different Object", testBundle, otherObject);
    }
}
