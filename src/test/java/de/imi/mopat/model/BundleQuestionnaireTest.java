package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
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
public class BundleQuestionnaireTest {

    Random random = new Random();
    BundleQuestionnaire testBundleQuestionnaire;

    public BundleQuestionnaireTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid BundleQuestionnaire Object
     *
     * @return Returns a valid {@link BundleQuestionnaire BundleQuestionnaire}
     */
    public static BundleQuestionnaire getNewValidBundleQuestionnaire() {
        Bundle bundle = BundleTest.getNewValidBundle();
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Integer position = Math.abs(new Random().nextInt()) + 1;
        BundleQuestionnaire bundleQuestionnaire = new BundleQuestionnaire(bundle, questionnaire,
            position, true, false);

        return bundleQuestionnaire;
    }

    /**
     * Returns a valid BundleQuestionnaire Object with a given Questionnaire
     *
     * @param questionnaire Questionnaire of the BundleQiestionnaire
     * @return Returns a valid {@link BundleQuestionnaire BundleQuestionnaire}
     */
    public static BundleQuestionnaire getNewValidBundleQuestionnaire(Questionnaire questionnaire) {
        Bundle bundle = BundleTest.getNewValidBundle();
        Integer position = Math.abs(new Random().nextInt()) + 1;

        BundleQuestionnaire bundleQuestionnaire = new BundleQuestionnaire(bundle, questionnaire,
            position, true, false);

        return bundleQuestionnaire;
    }

    @Before
    public void setUp() {
        testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link BundleQuestionnaire#addExportTemplate(ExportTemplate)} and
     * {@link BundleQuestionnaire#addExportTemplate(ExportTemplate)} method.<br> Invalid input:
     * <code>null</code><br> Valid input: random {@link ExportTemplate}
     */
    @Test
    public void testAddExportTemplate() {
        ExportTemplate exportTemplate = null;
        Throwable e = null;
        try {
            testBundleQuestionnaire.addExportTemplate(exportTemplate);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the ExportTemplates",
            e instanceof AssertionError);

        exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        testBundleQuestionnaire.addExportTemplate(exportTemplate);
        assertTrue(
            "Adding exportTemplate failed. The returned set didn't contain the expected export template.",
            testBundleQuestionnaire.getExportTemplates().contains(exportTemplate));
        int countExportTemplates = testBundleQuestionnaire.getExportTemplates().size();
        testBundleQuestionnaire.addExportTemplate(exportTemplate);
        assertEquals("It was possible to add an Exporttemplate twice", countExportTemplates,
            testBundleQuestionnaire.getExportTemplates().size());
    }

    /**
     * Test of {@link BundleQuestionnaire#removeExportTemplates} method.
     */
    @Test
    public void testRemoveExportTemplates() {
        ExportTemplate testExportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testExportTemplate = ExportTemplateTest.getNewValidExportTemplate();
            testBundleQuestionnaire.addExportTemplate(testExportTemplate);
        }
        testBundleQuestionnaire.removeExportTemplates();
        assertTrue("The set of ExportTemplates was not empty after deleteting all ExportTemplates",
            testBundleQuestionnaire.getExportTemplates().isEmpty());
        assertFalse("The BundleQuestionnaire was not deleted from the ExportTemplate",
            testExportTemplate.getBundleQuestionnaires().contains(testBundleQuestionnaire));
    }

    /**
     * Test of {@link BundleQuestionnaire#getExportTemplates()} and
     * {@link BundleQuestionnaire#setExportTemplates(java.util.Set)} methods..<br> Invalid input:
     * <code>null</code><br> Valid input: random set of {@link ExportTemplate ExportTemplates}
     */
    @Test
    public void testSetGetExportTemplates() {
        Set<ExportTemplate> exportTemplateSet = null;
        Throwable e = null;
        try {
            testBundleQuestionnaire.setExportTemplates(exportTemplateSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the ExportTemplates",
            e instanceof AssertionError);

        exportTemplateSet = new HashSet<>();
        Integer size = random.nextInt(25) + 1;

        for (int i = 0; i < size; i++) {
            exportTemplateSet.add(ExportTemplateTest.getNewValidExportTemplate());
        }

        testBundleQuestionnaire.setExportTemplates(exportTemplateSet);
        Set<ExportTemplate> testExportTemplateSet = testBundleQuestionnaire.getExportTemplates();
        assertNotNull(
            "Setting the exportTemplates failed. The returned value was null although not-null value was expected.",
            testExportTemplateSet);
        assertEquals(
            "Setting the exportTemplates failed. The returned value didn't match the expected value.",
            exportTemplateSet, testExportTemplateSet);
    }

    /**
     * Test of {@link BundleQuestionnaire#getBundle()} method.<br> Invalid input:
     * <code>null</code><br> Valid input: random {@link Bundle}
     */
    @Test
    public void testGetAndSetBundle() {
        Bundle testBundle = null;
        Throwable e = null;
        try {
            testBundleQuestionnaire.setBundle(testBundle);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Bundle", e instanceof AssertionError);

        testBundle = BundleTest.getNewValidBundle();
        testBundleQuestionnaire.setBundle(testBundle);
        assertEquals(
            "Getting the bundle failed. The returned value didn't match the expected value.",
            testBundle, testBundleQuestionnaire.getBundle());
    }

    /**
     * Test of {@link BundleQuestionnaire#getQuestionnaire()} and
     * {@link BundleQuestionnaire#setQuestionnaire()} method.<br> Invalid input:
     * <code>null</code><br> Valid input: random {@link Questionnaire}
     */
    @Test
    public void testGetAndSetQuestionnaire() {
        Questionnaire testQuestionnaire = null;
        Throwable e = null;
        try {
            testBundleQuestionnaire.setQuestionnaire(testQuestionnaire);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Questionniare", e instanceof AssertionError);

        testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        testBundleQuestionnaire.setQuestionnaire(testQuestionnaire);
        assertEquals(
            "Getting the questionnaire failed. The returned value didn't match the expected value.",
            testQuestionnaire, testBundleQuestionnaire.getQuestionnaire());
    }

    /**
     * Test of {@link BundleQuestionnaire#getPosition()} and
     * {@link BundleQuestionnaire#setPosition(java.lang.Integer)} method.<br> Invalid input:
     * <code>null</code>, negative Integer<br> Valid input: positive Integer
     */
    @Test
    public void testGetAndSetPosition() {
        Integer testPosition = null;
        Throwable e = null;
        try {
            testBundleQuestionnaire.setPosition(testPosition);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the position", e instanceof AssertionError);

        testPosition = (random.nextInt(500) + 1) * (-1);
        e = null;
        try {
            testBundleQuestionnaire.setPosition(testPosition);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative position", e instanceof AssertionError);

        testPosition *= (-1);
        testBundleQuestionnaire.setPosition(testPosition);
        assertEquals(
            "Getting the position failed. The returned value didn't match the expected value.",
            testPosition, testBundleQuestionnaire.getPosition());
    }

    /**
     * Test of {@link BundleQuestionnaire#getIsEnabled()} and
     * {@link BundleQuestionnaire#setIsEnabled(java.lang.Boolean)} method.<br> Invalid input:
     * <code>null</code><br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetIsEnabled() {
        Boolean testIsEnabled = null;
        Throwable e = null;
        try {
            testBundleQuestionnaire.setIsEnabled(testIsEnabled);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the isEnabled value",
            e instanceof AssertionError);

        testIsEnabled = random.nextBoolean();
        testBundleQuestionnaire.setIsEnabled(testIsEnabled);
        assertEquals(
            "Getting the questionnaire failed. The returned value didn't match the expected value.",
            testIsEnabled, testBundleQuestionnaire.getIsEnabled());
    }

    /**
     * Test of {@link BundleQuestionnaire#getShowScores()} and
     * {@link BundleQuestionnaire#setShowScores(java.lang.Boolean)} method.<br> Invalid input:
     * <code>null</code><br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetShowScores() {
        Boolean testShowScores = null;
        Throwable e = null;
        try {
            testBundleQuestionnaire.setShowScores(testShowScores);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the isEnabled value",
            e instanceof AssertionError);

        testShowScores = random.nextBoolean();
        testBundleQuestionnaire.setShowScores(testShowScores);
        assertEquals(
            "Getting the ShowScores value failed. The returned value didn't match the expected value.",
            testShowScores, testBundleQuestionnaire.getShowScores());
    }

    /**
     * Test of {@link BundleQuestionnaire#removeBundle}.<br>
     */
    @Test
    public void testRemoveBundle() {
        Bundle testBundle = testBundleQuestionnaire.getBundle();
        testBundleQuestionnaire.removeBundle();
        assertNull("The Bundle was not null after removing it",
            testBundleQuestionnaire.getBundle());
        assertFalse("The BundleQuestionnaire was not deleted in the Bundle",
            testBundle.getBundleQuestionnaires().contains(testBundleQuestionnaire));
    }

    /**
     * Test of {@link BundleQuestionnaire#removeQuestionnaire}.<br>
     */
    @Test
    public void testRemoveQuestionnaire() {
        Questionnaire testQuestionnaire = testBundleQuestionnaire.getQuestionnaire();
        testBundleQuestionnaire.removeQuestionnaire();
        assertNull("The Questionnaire was not null after removing it",
            testBundleQuestionnaire.getQuestionnaire());
        assertFalse("The BundleQuestionnaire was not deleted in the Questionnaire",
            testQuestionnaire.getBundleQuestionnaires().contains(testBundleQuestionnaire));
    }

    /**
     * Test of {@link BundleQuestionnaire#equals(java.lang.Object)} method.<br> Valid input: the
     * same {@link BundleQuestionnaire}, another {@link BundleQuestionnaire}, <code>null<\code> and
     * another Object
     */
    @Test
    public void testEquals() {
        BundleQuestionnaire otherBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        assertEquals(
            "The equal method failed. The returned value was false although true was expected.",
            testBundleQuestionnaire, testBundleQuestionnaire);
        assertNotEquals(
            "The equal method failed. The returned value was true although false was expected. Input param was different bundleQuestionnaire.",
            testBundleQuestionnaire, otherBundleQuestionnaire);
        assertNotEquals(
            "The equal method failed. The returned value was true although false was expected. Input param was null.",
            null, testBundleQuestionnaire);
        assertNotEquals(
            "The equal method failed. The returned value was true although false was expected. Input param was different object.",
            testBundleQuestionnaire, questionnaire);
    }

    /**
     * Test of {@link BundleQuestionnaire#compareTo(de.imi.mopat.model.BundleQuestionnaire)}
     * method.<br> Valid input: the same {@link BundleQuestionnaire} and another
     * {@link BundleQuestionnaire}
     */
    @Test
    public void testCompareTo() {
        Bundle bundle = BundleTest.getNewValidBundle();
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        int position;

        do {
            position = random.nextInt();
        } while (position <= 0);

        BundleQuestionnaire bundleQuestionnaire1 = new BundleQuestionnaire(bundle, questionnaire,
            position, true, false);
        position++;
        BundleQuestionnaire bundleQuestionnaire2 = new BundleQuestionnaire(bundle, questionnaire,
            position, true, false);

        //compareTo method returns negative integer if the objects position is less than the params position
        assertTrue(bundleQuestionnaire1.compareTo(bundleQuestionnaire2) < 0);
        //returns positive integer if the objects position is greater than params position
        assertTrue(bundleQuestionnaire2.compareTo(bundleQuestionnaire1) > 0);
        //returns 0 if the positions equals
        assertEquals(0, bundleQuestionnaire1.compareTo(bundleQuestionnaire1));
    }

    /**
     * Test of {@link BundleQuestionnaire#hashCode()} method.<br> Valid input: the same
     * {@link BundleQuestionnaire} twice in one set
     */
    @Test
    public void testHashCode() {
        BundleQuestionnaire bundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        Set<BundleQuestionnaire> bundleQuestionnaireSet = new HashSet<>();

        assertTrue(bundleQuestionnaireSet.add(bundleQuestionnaire));
        assertFalse(bundleQuestionnaireSet.add(bundleQuestionnaire));
    }

    /**
     * Test of {@link BundleQuestionnaire#toBundleQuestionnaireDTO()} method.<br> Valid input: a
     * valid {@link BundleQuestionnaire} with random {@link ExportTemplate ExportTemplates}
     */
    @Test
    public void testToBundleQuestionnaireDTO() {
        Set<Long> exportTemplateIds = new HashSet<>();
        Integer size = random.nextInt(25) + 1;

        for (int i = 0; i < size; i++) {
            ExportTemplate exportTemplate = spy(ExportTemplateTest.getNewValidExportTemplate());
            Mockito.when(exportTemplate.getId()).thenReturn(Math.abs(random.nextLong()));
            exportTemplateIds.add(exportTemplate.getId());
            testBundleQuestionnaire.addExportTemplate(exportTemplate);
        }

        BundleQuestionnaireDTO bundleQuestionnaireDTO = testBundleQuestionnaire.toBundleQuestionnaireDTO();
        Long testPosition = testBundleQuestionnaire.getPosition().longValue();
        assertEquals(
            "ToBundleQuestionnaireDTO failed. The returned set didn'match the expected set.",
            bundleQuestionnaireDTO.getExportTemplates(), exportTemplateIds);
        assertEquals(
            "ToBundleQuestionnaireDTO failed. The returned position didn't match the expected value.",
            testPosition, bundleQuestionnaireDTO.getPosition());
        assertEquals(
            "ToBundleQuestionnaireDTO failed. The returned isEnabled didn't match the expected value.",
            bundleQuestionnaireDTO.getIsEnabled(), testBundleQuestionnaire.getIsEnabled());
    }
}
