package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.model.enumeration.ExportEncounterFieldType;
import de.imi.mopat.model.enumeration.ExportScoreFieldType;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.ScoreTest;
import de.imi.mopat.utils.Helper;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
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
public class ExportTemplateTest {

    private static final Random random = new Random();
    @Autowired
    ConfigurationGroupDao testConfigurationGroupDao;
    @Autowired
    ExportTemplateDao testExportTemplateDao;
    private ExportTemplate testExportTemplate;

    public ExportTemplateTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new ExportTemplate
     *
     * @return Returns a valid new ExportTemplate
     */
    public static ExportTemplate getNewValidExportTemplate() {
        String name = Helper.getRandomAlphanumericString(random.nextInt(50) + 3);
        ExportTemplateType exportTemplateType = Helper.getRandomEnum(ExportTemplateType.class);
        String filename = Helper.getRandomAlphanumericString(random.nextInt(50) + 3);
        ConfigurationGroup configurationGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        ExportTemplate exportTemplate = new ExportTemplate(name, exportTemplateType, filename,
            configurationGroup, questionnaire);
        return exportTemplate;
    }

    @Before
    public void setUp() {
        testExportTemplate = getNewValidExportTemplate();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ExportTemplate#createExportTemplates}.<br>
     */
    @Test
    public void testCreateExportTemplates() {
        ExportTemplateType testExportTemplateType = ExportTemplateType.ODM;
        String testName = Helper.getRandomAlphabeticString(random.nextInt(20) + 5);

        List<ExportTemplate> testCreatedExportTemplates = ExportTemplate.createExportTemplates(
            testName, testExportTemplateType, null, testConfigurationGroupDao,
            testExportTemplateDao);

        assertEquals("The count of the created export templates was not the expected one.",
            testConfigurationGroupDao.getConfigurationGroups(
                testExportTemplateType.getConfigurationMessageCode()).size(),
            testCreatedExportTemplates.size());
        for (ExportTemplate currentExportTemplate : testCreatedExportTemplates) {
            assertEquals("The getting filename was not the expected one.", testName,
                currentExportTemplate.getFilename());
            assertEquals("The getting original filename was not the expected one.", testName,
                currentExportTemplate.getOriginalFilename());
            assertEquals(
                "The getting ExportTemplateType was not ODM although the labelMessageCode contains ODM.",
                ExportTemplateType.ODM, currentExportTemplate.getExportTemplateType());
            testExportTemplateDao.remove(currentExportTemplate);
        }

        testExportTemplateType = ExportTemplateType.FHIR;
        testName = Helper.getRandomAlphabeticString(random.nextInt(20) + 5);

        testCreatedExportTemplates = ExportTemplate.createExportTemplates(testName,
            testExportTemplateType, null, testConfigurationGroupDao, testExportTemplateDao);

        assertEquals("The count of the created export templates was not the expected one.",
            testConfigurationGroupDao.getConfigurationGroups(
                testExportTemplateType.getConfigurationMessageCode()).size(),
            testCreatedExportTemplates.size());
        for (ExportTemplate currentExportTemplate : testCreatedExportTemplates) {
            assertEquals("The getting filename was not the expected one.", testName,
                currentExportTemplate.getFilename());
            assertEquals("The getting original filename was not the expected one.", testName,
                currentExportTemplate.getOriginalFilename());
            assertEquals(
                "The getting ExportTemplateType was not FHIR although the labelMessageCode contains FHIR.",
                ExportTemplateType.FHIR, currentExportTemplate.getExportTemplateType());
            testExportTemplateDao.remove(currentExportTemplate);
        }
    }

    /**
     * Test of {@link ExportTemplate#getName} and {@link ExportTemplate#setName}.<br> Valid input:
     * random String
     */
    @Test
    public void testGetAndSetName() {
        String testName = Helper.getRandomString(random.nextInt(50) + 1);
        testExportTemplate.setName(testName);
        assertEquals("The getting name was not the expected one", testName,
            testExportTemplate.getName());
    }

    /**
     * Test of {@link ExportTemplate#getExportTemplateType} and
     * {@link ExportTemplate#setExportTemplateType}.<br> Invalid input: <code>null</code><br> Valid
     * input: random {@link ExportTemplateType}
     */
    @Test
    public void testGetAndSetExportTemplateType() {
        ExportTemplateType testExportTemplateType = null;
        Throwable e = null;
        try {
            testExportTemplate.setExportTemplateType(testExportTemplateType);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the ExportTemplateType",
            e instanceof AssertionError);

        testExportTemplateType = Helper.getRandomEnum(ExportTemplateType.class);
        testExportTemplate.setExportTemplateType(testExportTemplateType);
        assertEquals("The getting ExportTemplateType was not the expected one",
            testExportTemplateType, testExportTemplate.getExportTemplateType());
    }

    /**
     * Test of {@link ExportTemplate#getFilename} and {@link ExportTemplate#setFilename}.<br>
     * Invalid input: <code>null</code><br> Valid input: random String
     */
    @Test
    public void testGetAndSetFilename() {
        String testFilename = null;
        Throwable e = null;
        try {
            testExportTemplate.setFilename(testFilename);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Filename", e instanceof AssertionError);
        testFilename = Helper.getRandomString(random.nextInt(50) + 1);
        testExportTemplate.setFilename(testFilename);
        assertEquals("The getting Filename was not the expected one", testFilename,
            testExportTemplate.getFilename());
    }

    /**
     * Test of {@link ExportTemplate#getOriginalFilename} and
     * {@link ExportTemplate#setOriginalFilename}.<br> Invalid input: <code>null</code><br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetOriginalFilename() {
        String testOriginalFilename = null;
        Throwable e = null;
        try {
            testExportTemplate.setOriginalFilename(testOriginalFilename);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the OriginalFilename",
            e instanceof AssertionError);
        testOriginalFilename = Helper.getRandomString(random.nextInt(50) + 1);
        testExportTemplate.setOriginalFilename(testOriginalFilename);
        assertEquals("The getting OriginalFilename was not the expected one", testOriginalFilename,
            testExportTemplate.getOriginalFilename());
    }

    /**
     * Test of {@link ExportTemplate#getConfigurationGroup} and
     * {@link ExportTemplate#setConfigurationGroup}.<br> Valid input: random
     * {@link ConfigurationGroup}
     */
    @Test
    public void testGetAndSetConfigurationGroup() {
        ConfigurationGroup testConfigurationGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();
        testExportTemplate.setConfigurationGroup(testConfigurationGroup);
        assertEquals("The gettinConfigurationGroup was not the expected one",
            testConfigurationGroup, testExportTemplate.getConfigurationGroup());
    }

    /**
     * Test of {@link ExportTemplate#getQuestionnaire} and
     * {@link ExportTemplate#setQuestionnaire}.<br> Invalid input: <code>null</code><br> Valid
     * input: random {@link Questionnaire}
     */
    @Test
    public void testGetAnSetQuestionnaire() {
        Questionnaire testQuestionnaire = null;
        Throwable e = null;
        try {
            testExportTemplate.setQuestionnaire(testQuestionnaire);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Questionnaire", e instanceof AssertionError);

        testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        testExportTemplate.setQuestionnaire(testQuestionnaire);
        assertEquals("The getting Questionnaire was not the expected one", testQuestionnaire,
            testExportTemplate.getQuestionnaire());
        // Add the same Questionnaire again
        testExportTemplate.setQuestionnaire(testQuestionnaire);
        assertEquals("The getting Questionnaire was altered after setting it again",
            testQuestionnaire, testExportTemplate.getQuestionnaire());
    }

    /**
     * Test of {@link ExportTemplate#addExportRules} and {@link ExportTemplate#getExportRules}.<br>
     * Invalid input:<code>null</code><br> Valid input: random number of
     * {@link ExportRuleEncounter}
     */
    @Test
    public void testAddExportRules() {
        Set<ExportRule> testSet = null;
        Throwable e = null;
        try {
            testExportTemplate.addExportRules(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the ExportRules", e instanceof AssertionError);

        testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            ExportRule testExportRule = ExportRuleEncounterTest.getNewValidExportRuleEncounter();
            testSet.add(testExportRule);
        }
        testExportTemplate.addExportRules(testSet);
        assertEquals("The getting set of ExportRules was not the expected one", testSet,
            testExportTemplate.getExportRules());
    }

    /**
     * Test of {@link ExportTemplate#addExportRule} and {@link ExportTemplate#getExportRules}.<br>
     * Invalid input: <code>null</code><br> Valid input: random Number of
     * {@link ExportRuleEncounter}
     */
    @Test
    public void testAddExportRule() {
        ExportRule testExportRule = null;
        Throwable e = null;
        try {
            testExportTemplate.addExportRule(testExportRule);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the ExportRules", e instanceof AssertionError);

        Set<ExportRule> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            testExportRule = ExportRuleTest.getNewValidRandomExportRule();
            testSet.add(testExportRule);
            testExportTemplate.addExportRule(testExportRule);
        }
        // Add the last ExportRule again
        testExportTemplate.addExportRule(testExportRule);
        // Add an ExportRule without an ExportTemplate
        testExportRule = new ExportRuleEncounter();
        testExportTemplate.addExportRule(testExportRule);
        testSet.add(testExportRule);

        assertEquals("The getting set of ExportRules was not the expected one", testSet,
            testExportTemplate.getExportRules());
    }

    /**
     * Test of {@link ExportTemplate#removeExportRule}.<br> Invalid input: <code>null</code><br>
     * Valid input: one existing {@link ExportRule}
     */
    @Test
    public void testRemoveExportRule() {
        ExportRule testExportRule = null;
        Throwable e = null;
        try {
            testExportTemplate.removeExportRule(testExportRule);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the ExportRules",
            e instanceof AssertionError);

        Set<ExportRule> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            testExportRule = ExportRuleEncounterTest.getNewValidExportRuleEncounter();
            testSet.add(testExportRule);
            testExportTemplate.addExportRule(testExportRule);
        }
        assertEquals("The getting set of ExportRules was not the expected one", testSet,
            testExportTemplate.getExportRules());
        testExportTemplate.removeExportRule(testExportRule);
        testSet.remove(testExportRule);
        assertEquals("The getting set after removing an ExportRule was not the expected one",
            testSet, testExportTemplate.getExportRules());

        // Try to remove an ExportRule which is not associated
        testExportRule = ExportRuleTest.getNewValidRandomExportRule();
        getNewValidExportTemplate().addExportRule(testExportRule);
        testExportTemplate.removeExportRule(testExportRule);
        assertEquals(
            "The getting set after removing a not associated ExportRule was not the expected one",
            testSet, testExportTemplate.getExportRules());
    }

    /**
     * Test of {@link ExportTemplate#getExportRulesByEncounterField}.<br> Valid input: set of
     * {@link ExportRuleEncounter}
     */
    @Test
    public void testGetExportRulesByEncounterField() {
        ExportEncounterFieldType testEncounterField = Helper.getRandomEnum(
            ExportEncounterFieldType.class);
        Set<ExportRule> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(450)) + 50;
        for (int i = 0; i < count; i++) {
            ExportRule testExportRule = ExportRuleTest.getNewValidRandomExportRule();
            testExportTemplate.addExportRule(testExportRule);
            if (testExportRule instanceof ExportRuleEncounter
                && ((ExportRuleEncounter) testExportRule).getEncounterField()
                .equals(testEncounterField)) {
                testSet.add(testExportRule);
            }
        }
        assertEquals("The getting set of ExportRules was not the expected one", testSet,
            testExportTemplate.getExportRulesByEncounterField(testEncounterField));
    }

    /**
     * Test of {@link ExportTemplate#getExportFieldsByEncounterField}.<br> Valid input: set of
     * {@link ExportRuleEncounter}
     */
    @Test
    public void testGetExportFieldsByEncounterField() {
        ExportEncounterFieldType testEncounterField = Helper.getRandomEnum(
            ExportEncounterFieldType.class);
        Set<String> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(450)) + 50;
        for (int i = 0; i < count; i++) {
            ExportRule testExportRule = ExportRuleTest.getNewValidRandomExportRule();
            testExportTemplate.addExportRule(testExportRule);
            if (testExportRule instanceof ExportRuleEncounter
                && ((ExportRuleEncounter) testExportRule).getEncounterField()
                .equals(testEncounterField)) {
                testSet.add(testExportRule.getExportField());
            }
        }
        assertEquals("The getting set of ExportFields was not the expected one", testSet,
            testExportTemplate.getExportFieldsByEncounterField(testEncounterField));
    }

    /**
     * Test of {@link ExportTemplate#getExportRuleFormatFromEncounterField}.<br> Valid input: random
     * {@link ExportTemplate}
     */
    @Test
    public void testGetExportRuleFormatFromEncounterField() {
        ExportEncounterFieldType testEncounterField = Helper.getRandomEnum(
            ExportEncounterFieldType.class);
        assertNull(
            "The ExportRuleFormat was not null altough the ExportTemplate had no ExportRuleEncounters",
            testExportTemplate.getExportRuleFormatFromEncounterField(testEncounterField));
        ExportRuleFormat testExportRuleFormat = null;
        boolean ExportRuleEncounterAdded = false;
        int count = Math.abs(random.nextInt(450)) + 50;
        for (int i = 0; i < count; i++) {
            ExportRule testExportRule = ExportRuleTest.getNewValidRandomExportRule();
            if (testExportRule instanceof ExportRuleEncounter
                && ((ExportRuleEncounter) testExportRule).getEncounterField()
                == testEncounterField) {
                if (!ExportRuleEncounterAdded) {
                    testExportTemplate.addExportRule(testExportRule);
                    testExportRuleFormat = testExportRule.getExportRuleFormat();
                    ExportRuleEncounterAdded = true;
                }
            } else {
                testExportTemplate.addExportRule(testExportRule);
            }
        }
        assertEquals("The getting set of ExportRuleFormat was not the expected one",
            testExportRuleFormat,
            testExportTemplate.getExportRuleFormatFromEncounterField(testEncounterField));
    }

    /**
     * Test of {@link ExportTemplate#getExportRuleFormatFromScoreField}.<br> Not possible to be
     * sure, that the searched {@link ExportRule} is not the first in the set.<br> Valid input:
     * random {@link ExportTemplate}
     */
    @Test
    public void testGetExportRuleFormatFromScoreField() {
        ExportScoreFieldType testScoreField = Helper.getRandomEnum(ExportScoreFieldType.class);
        Long testId = random.nextLong();
        assertNull(
            "The ExportScoreFieldType was not null altough the ExportTemplate had no ExportRuleScores",
            testExportTemplate.getExportRuleFormatFromScoreField(testScoreField, testId));
        ExportRuleFormat testExportRuleFormat = null;
        boolean ExportRuleFormatAdded = false;
        int count = Math.abs(random.nextInt(450)) + 50;
        for (int i = 0; i < count; i++) {
            ExportRule testExportRule = ExportRuleTest.getNewValidRandomExportRule();
            if (testExportRule instanceof ExportRuleScore testExportRuleScore) {
                Score testScore = spy(ScoreTest.getNewValidScore());
                Mockito.when(testScore.getId()).thenReturn(testId);
                testExportRuleScore.setScore(testScore);
                if (!ExportRuleFormatAdded && testExportRuleScore.getScore().getId().equals(testId)
                    && testExportRuleScore.getScoreField() == testScoreField) {
                    testExportTemplate.addExportRule(testExportRule);
                    testExportRuleFormat = testExportRule.getExportRuleFormat();
                    ExportRuleFormatAdded = true;
                }
            } else {
                testExportTemplate.addExportRule(testExportRule);
            }
        }
        assertEquals("The getting ExportRuleFormat was not the expected one", testExportRuleFormat,
            testExportTemplate.getExportRuleFormatFromScoreField(testScoreField, testId));
    }

    /**
     * Test of {@link ExportTemplate#getExportFieldsByScoreField}.<br> Valid input: random
     * {@link ExportTemplate}
     */
    @Test
    public void testGetExportFieldsByScoreField() {
        ExportScoreFieldType testScoreField = Helper.getRandomEnum(ExportScoreFieldType.class);
        Set<String> testSet = new HashSet<>();
        Long testId = random.nextLong();
        int count = Math.abs(random.nextInt(450)) + 50;
        for (int i = 0; i < count; i++) {
            ExportRule testExportRule = ExportRuleTest.getNewValidRandomExportRule();
            if (testExportRule instanceof ExportRuleScore testExportRuleScore) {
                Score testScore = spy(ScoreTest.getNewValidScore());
                if (random.nextBoolean()) {
                    Mockito.when(testScore.getId()).thenReturn(testId);
                } else {
                    Mockito.when(testScore.getId()).thenReturn(random.nextLong());
                }
                testExportRuleScore.setScore(testScore);
                if (testExportRuleScore.getScore().getId().equals(testId)
                    && testExportRuleScore.getScoreField() == testScoreField) {
                    testExportTemplate.addExportRule(testExportRule);
                    testSet.add(testExportRule.getExportField());
                } else {
                    testExportTemplate.addExportRule(testExportRule);
                }
            } else {
                testExportTemplate.addExportRule(testExportRule);
            }
        }
        assertEquals("The getting ExportRuleFormat was not the expected one", testSet,
            testExportTemplate.getExportFieldsByScoreField(testScoreField, testId));
    }

    /**
     * Test of {@link ExportTemplate#getExportRulesByScoreField}.<br> Valid input: random
     * {@link ExportTemplate}
     */
    @Test
    public void testGetExportRulesByScoreField() {
        ExportScoreFieldType testScoreField = Helper.getRandomEnum(ExportScoreFieldType.class);
        Set<ExportRule> testSet = new HashSet<>();
        Long testId = random.nextLong();
        int count = Math.abs(random.nextInt(450)) + 50;
        for (int i = 0; i < count; i++) {
            ExportRule testExportRule = ExportRuleTest.getNewValidRandomExportRule();
            if (testExportRule instanceof ExportRuleScore testExportRuleScore) {
                Score testScore = spy(ScoreTest.getNewValidScore());
                if (random.nextBoolean()) {
                    Mockito.when(testScore.getId()).thenReturn(testId);
                } else {
                    Mockito.when(testScore.getId()).thenReturn(random.nextLong());
                }
                testExportRuleScore.setScore(testScore);
                if (testExportRuleScore.getScore().getId().equals(testId)
                    && testExportRuleScore.getScoreField() == testScoreField) {
                    testExportTemplate.addExportRule(testExportRule);
                    testSet.add(testExportRule);
                } else {
                    testExportTemplate.addExportRule(testExportRule);
                }
            } else {
                testExportTemplate.addExportRule(testExportRule);
            }
        }
        assertEquals("The getting ExportRuleFormat was not the expected one", testSet,
            testExportTemplate.getExportRulesByScoreField(testScoreField, testId));
    }

    /**
     * Test of {@link ExportTemplate#getExportFieldsByAnswer}.<br> Valid input: random
     * {@link ExportTemplate} and random {@link Answer}
     */
    @Test
    public void testGetExportFieldsByAnswer() {
        Answer testAnswer = AnswerTest.getNewValidRandomAnswer();
        Set<String> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(450)) + 50;
        for (int i = 0; i < count; i++) {
            if (random.nextBoolean()) {
                ExportRuleAnswer testExportRule = ExportRuleAnswerTest.getNewValidExportRuleAnswer(
                    testAnswer);
                testExportTemplate.addExportRule(testExportRule);
                testSet.add(testExportRule.getExportField());

            } else {
                ExportRule testExportRule = ExportRuleTest.getNewValidRandomExportRule();
                testExportTemplate.addExportRule(testExportRule);
            }
        }
        assertEquals("The getting set of ExportFields was not the expected one", testSet,
            new HashSet<>(testExportTemplate.getExportFieldsByAnswer(testAnswer)));
    }

    /**
     * Test of {@link ExportTemplate#getExportFieldsByAnswer}.<br> Valid input: random
     * {@link ExportTemplate} and random {@link Answer} with random freetext value
     */
    @Test
    public void testGetExportFieldsByAnswerWithFreetextValue() {
        Answer testAnswer = AnswerTest.getNewValidRandomAnswer();
        Set<String> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(450)) + 50;
        for (int i = 0; i < count; i++) {
            if (random.nextBoolean()) {
                if (random.nextBoolean()) {
                    ExportRuleAnswer testExportRule = ExportRuleAnswerTest.getNewValidExportRuleAnswer(
                        testAnswer, Boolean.TRUE);
                    testExportTemplate.addExportRule(testExportRule);
                    testSet.add(testExportRule.getExportField());
                } else {
                    ExportRuleAnswer testExportRule = ExportRuleAnswerTest.getNewValidExportRuleAnswer(
                        testAnswer);
                    testExportTemplate.addExportRule(testExportRule);
                }
            } else {
                ExportRule testExportRule = ExportRuleTest.getNewValidRandomExportRule();
                testExportTemplate.addExportRule(testExportRule);
            }
        }
        assertEquals("The getting set of ExportFields was not the expected one", testSet,
            new HashSet<>(testExportTemplate.getExportFieldsByAnswer(testAnswer, true)));
    }

    /**
     * Test of {@link ExportTemplate#removeBundleQuestionnaire}.<br> Invalid input:
     * <code>null</code><br> Valid input: random number of associated
     * {@link BundleQuestionnaire BundleQuestionnaires}
     */
    @Test
    public void testRemoveBundleQuestionnaire() {
        BundleQuestionnaire testBundleQuestionnaire = null;
        Throwable e = null;
        try {
            testExportTemplate.removeBundleQuestionnaire(testBundleQuestionnaire);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the BundleQuestionnaires",
            e instanceof AssertionError);

        Set<BundleQuestionnaire> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(100)) + 1;
        for (int i = 0; i < count; i++) {
            testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
            testExportTemplate.addBundleQuestionnaire(testBundleQuestionnaire);
            testSet.add(testBundleQuestionnaire);
        }
        assertEquals("The getting set of BundleQuestionnaires was not the expected one", testSet,
            testExportTemplate.getBundleQuestionnaires());
        testExportTemplate.removeBundleQuestionnaire(testBundleQuestionnaire);
        testSet.remove(testBundleQuestionnaire);
        assertEquals(
            "The getting set after removing of a BundleQuestionnaire was not the expected one",
            testSet, testExportTemplate.getBundleQuestionnaires());
    }

    /**
     * Test of {@link ExportTemplate#addBundleQuestionnaire} and
     * {@link ExportTemplate#getBundleQuestionnaires}.<br> Invalid input: <code>null</code><br>
     * Valid input: random number of {@link BundleQuestionnaire BundleQuestionnaires}
     */
    @Test
    public void testAddBundleQuestionnaire() {
        BundleQuestionnaire testBundleQuestionnaire = null;
        Throwable e = null;
        try {
            testExportTemplate.addBundleQuestionnaire(testBundleQuestionnaire);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the BundleQuestionnaires",
            e instanceof AssertionError);

        Set<BundleQuestionnaire> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(100)) + 1;
        for (int i = 0; i < count; i++) {
            testBundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
            testExportTemplate.addBundleQuestionnaire(testBundleQuestionnaire);
            testSet.add(testBundleQuestionnaire);
        }
        // Add the last BundleQuestionnaire again
        testExportTemplate.addBundleQuestionnaire(testBundleQuestionnaire);

        assertEquals("The getting set of BundleQuestionnaires was not the expected one", testSet,
            testExportTemplate.getBundleQuestionnaires());
    }

    /**
     * Test of {@link ExportTemplate#addEncounterExportTemplates} and
     * {@link ExportTemplate#getEncounterExportTemplates}.<br> Invalid input: <code>null</code><br>
     * Valid input: random number of {@link EncounterExportTemplate EncounterExportTemplates}
     */
    @Test
    public void testAddEncounterExportTemplates() {
        Set<EncounterExportTemplate> testSet = null;
        EncounterExportTemplate testEncounterExportTemplate;
        Throwable e = null;
        try {
            testExportTemplate.addEncounterExportTemplates(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the EncounterExportTemplates",
            e instanceof AssertionError);

        testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(100)) + 1;
        for (int i = 0; i < count; i++) {
            testEncounterExportTemplate = EncounterExportTemplateTest.getNewValidEncounterExportTemplate();
            testSet.add(testEncounterExportTemplate);
        }
        testExportTemplate.addEncounterExportTemplates(testSet);
        assertEquals("The getting set of EncounterExportTemplates was not the expected one",
            testSet, testExportTemplate.getEncounterExportTemplates());
    }

    /**
     * Test of {@link ExportTemplate#addEncounterExportTemplate} and
     * {@link ExportTemplate#getEncounterExportTemplates}.<br> Invalid input: <code>null</code><br>
     * Valid input: random number of {@link EncounterExportTemplate EncounterExportTemplates}
     */
    @Test
    public void testAddEncounterExportTemplate() {
        EncounterExportTemplate testEncounterExportTemplate = null;
        Throwable e = null;
        try {
            testExportTemplate.addEncounterExportTemplate(testEncounterExportTemplate);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the EncounterExportTemplates",
            e instanceof AssertionError);

        Set<EncounterExportTemplate> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(100)) + 1;
        for (int i = 0; i < count; i++) {
            testEncounterExportTemplate = EncounterExportTemplateTest.getNewValidEncounterExportTemplate();
            testExportTemplate.addEncounterExportTemplate(testEncounterExportTemplate);
            testSet.add(testEncounterExportTemplate);
        }
        // Add the last EncouterExporttemplate again
        testExportTemplate.addEncounterExportTemplate(testEncounterExportTemplate);
        // Add an EncounterExporttemplate without an ExportTemplate
        testEncounterExportTemplate = new EncounterExportTemplate();
        testExportTemplate.addEncounterExportTemplate(testEncounterExportTemplate);
        testSet.add(testEncounterExportTemplate);

        assertEquals("The getting set of EncounterExportTemplates was not the expected one",
            testSet, testExportTemplate.getEncounterExportTemplates());
    }

    /**
     * Test of {@link ExportTemplate#equals}.<br> Invalid input: the same {@link ExportTemplate}
     * twice in a HashSet<br>
     */
    @Test
    public void testEquals() {
        HashSet<ExportTemplate> testSet = new HashSet<>();
        testSet.add(testExportTemplate);
        testSet.add(testExportTemplate);
        assertEquals("It was possible to set the same ExportTemplate in one set", 1,
            testSet.size());

        assertEquals("The ExportTemplate was not equal to itself", testExportTemplate,
            testExportTemplate);
        assertNotEquals("The ExportTemplate was equal to null", null, testExportTemplate);
        ExportTemplate otherExportTemplate = getNewValidExportTemplate();
        assertNotEquals("The ExportTemplate was equal to a different ExportTemplate",
            testExportTemplate, otherExportTemplate);
        Object otherObject = new Object();
        assertNotEquals("The ExportTemplate was equal to a different Object", testExportTemplate,
            otherObject);
    }

    /**
     * Test of {@link ExportTemplate#toString}.<br>
     */
    @Test
    public void testToString() {
        Long testID = Math.abs(random.nextLong());
        ExportTemplate spyExportTemplate = spy(testExportTemplate);
        Mockito.when(spyExportTemplate.getId()).thenReturn(testID);

        String testString = "de.imi.mopat.model.ExportTemplate[ id=" + testID + " ]";
        assertEquals("The getting String was not the expected one", testString,
            spyExportTemplate.toString());
    }
}
