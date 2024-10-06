package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.helper.controller.QuestionnaireService;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.ScoreTest;
import de.imi.mopat.model.score.UnaryExpression;
import de.imi.mopat.utils.Helper;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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
public class QuestionnaireTest {

    private static final Random random = new Random();
    private Questionnaire testQuestionnaire;
    @Autowired
    private QuestionnaireService questionnairService;
    @Autowired
    QuestionnaireDTOMapper questionnaireDTOMapper;

    public QuestionnaireTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * creates a new, randomly filled, but valid {@link Questionnaire} object
     *
     * @return the freshly created {@link Questionnaire}. Is never
     * <code>null</code>, is valid due to these test cases
     */
    public static Questionnaire getNewValidQuestionnaire() {
        String name = Helper.getRandomAlphanumericString(random.nextInt(252) + 3);
        String description = Helper.getRandomAlphanumericString(random.nextInt(252) + 3);
        Long changedBy = Math.abs(random.nextLong());

        Boolean isPublished = random.nextBoolean();

        Questionnaire questionnaire = new Questionnaire(name, description, changedBy, isPublished);

        return questionnaire;

    }

    @Before
    public void setUp() {
        testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link Questionnaire#addExportTemplate(de.imi.mopat.model.ExportTemplate)}
     * method.<br> Invalid input: <code>null</code><br> Valid input: random {@link ExportTemplate}
     */
    @Test
    public void testAddExportTemplate() {
        ExportTemplate exportTemplate = null;
        Throwable e = null;
        try {
            testQuestionnaire.addExportTemplate(exportTemplate);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the ExportTemplates",
            e instanceof AssertionError);

        exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        testQuestionnaire.addExportTemplate(exportTemplate);
        Set<ExportTemplate> testExportTemplateSet = testQuestionnaire.getExportTemplates();
        assertTrue(
            "Adding export template failed. The questionnaire's set of exporttemplates didn't contain the expected object.",
            testExportTemplateSet.contains(exportTemplate));
    }

    /**
     * Test of {@link Questionnaire#getExportTemplates()} method.
     */
    @Test
    public void testGetExportTemplates() {
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Set<ExportTemplate> exportTemplateSet = new HashSet<>();
        Integer size = random.nextInt(25) + 1;

        while (size > exportTemplateSet.size()) {
            ExportTemplate exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
            exportTemplateSet.add(exportTemplate);
            questionnaire.addExportTemplate(exportTemplate);
        }

        Set<ExportTemplate> testExportTemplateSet = questionnaire.getExportTemplates();
        assertNotNull(
            "Getting exportTemplates failed. The returned value was null although a not-null value was expected.",
            testExportTemplateSet);
        assertEquals(
            "Getting exportTemplates failed. The returned set didn't match the expected value.",
            exportTemplateSet, testExportTemplateSet);
    }

    /**
     * Test of {@link Questionnaire#removeExportTemplate(de.imi.mopat.model.ExportTemplate)}
     * method.<br> Invalid input: <code>null</code><br> Valid input: random {@link ExportTemplate}
     * that is associated to this {@link Questionnaire}
     */
    @Test
    public void testRemoveExportTemplate() {
        ExportTemplate exportTemplate = null;
        Throwable e = null;
        try {
            testQuestionnaire.removeExportTemplate(exportTemplate);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the ExportTemplates",
            e instanceof AssertionError);

        testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        testQuestionnaire.addExportTemplate(exportTemplate);
        testQuestionnaire.removeExportTemplate(exportTemplate);
        Set<ExportTemplate> testExportTemplateSet = testQuestionnaire.getExportTemplates();
        assertTrue(
            "Removing exportTemplate failed. The questionnaire's set of exportTemplates wasn't empty although it was expected to be so.",
            testExportTemplateSet.isEmpty());
    }

    /**
     * Test of {@link Questionnaire#getScores()} and {@link Questionnaire#setScores(java.util.Set)}
     * methods.<br> Valid input: random number of random {@link Score Scores}
     */
    @Test
    public void testSetGetScores() {
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Set<Score> scoreSet = new HashSet<>();
        Integer size = random.nextInt(25) + 1;

        while (size > scoreSet.size()) {
            Score score = ScoreTest.getNewValidScore();
            scoreSet.add(score);
        }

        questionnaire.setScores(scoreSet);
        Set<Score> testScoreSet = questionnaire.getScores();
        assertNotNull(
            "Setting the scores failed. The returned set was null although a null value was expected.",
            testScoreSet);
        assertEquals("Setting the scores failed. The returned set didn't match the expected set.",
            testScoreSet, scoreSet);
    }

    /**
     * Test of {@link Questionnaire#addScore(de.imi.mopat.model.score.Score)} method.<br> Invalid
     * input: <code>null</code><br> Valid input: random set of {@link Score Scores}
     */
    @Test
    public void testAddScore() {
        Score testScore = null;
        Throwable e = null;
        try {
            testQuestionnaire.addScore(testScore);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the Scores", e instanceof AssertionError);

        Set<Score> testSet = new HashSet<>();
        int countScores = random.nextInt(50) + 1;
        for (int i = 0; i < countScores; i++) {
            testScore = ScoreTest.getNewValidScore();
            testScore.setQuestionnaire(testQuestionnaire);
            testQuestionnaire.addScore(testScore);
            testSet.add(testScore);
        }
        // Add the last Score twice
        testQuestionnaire.addScore(testScore);
        // Add Score without Questionnaire
        testScore = ScoreTest.getNewValidScore();
        testQuestionnaire.addScore(testScore);
        testSet.add(testScore);
        // Add Score with another Questionniare
        testScore = ScoreTest.getNewValidScore();
        testScore.setQuestionnaire(QuestionnaireTest.getNewValidQuestionnaire());
        testQuestionnaire.addScore(testScore);
        testSet.add(testScore);

        assertEquals("The getting set of Scores was not the expected one", testSet,
            testQuestionnaire.getScores());
    }

    /**
     * Test of {@link Questionnaire#removeScore(de.imi.mopat.model.score.Score)} method.<br> Invalid
     * input: <code>null</code>
     */
    @Test
    public void testRemoveScore() {
        Score testScore = null;
        Throwable e = null;
        try {
            testQuestionnaire.removeScore(testScore);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the Scores", e instanceof AssertionError);

        testScore = ScoreTest.getNewValidScore();
        testQuestionnaire.addScore(testScore);
        testQuestionnaire.removeScore(testScore);
        Set<Score> testScoreSet = testQuestionnaire.getScores();
        assertTrue(
            "Removing score failed. The returned set of scores wasn't empty although it was expected to be so.",
            testScoreSet.isEmpty());
    }

    /**
     * Test of {@link Questionnaire#toQuestionnaireDTO()} method.<br> Valid input: random valid
     * {@link Questionnaire}
     */
    @Test
    public void testToQuestionnaireDTO() {
        Questionnaire spyQuestionnaire = spy(testQuestionnaire);
        Mockito.when(spyQuestionnaire.getId()).thenReturn(Math.abs(random.nextLong()));
        Set<Question> questions = new HashSet<>();
        Map<String, String> stringMap = new TreeMap<>();

        Integer size = Math.abs(random.nextInt(25));

        for (int i = 0; i < size; i++) {
            Question question = spy(QuestionTest.getNewValidQuestion());
            Mockito.when(question.getId()).thenReturn(Math.abs(random.nextLong()));
            questions.add(question);
            stringMap.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        }

        spyQuestionnaire.addQuestions(questions);
        spyQuestionnaire.addExportTemplate(ExportTemplateTest.getNewValidExportTemplate());
        spyQuestionnaire.setLogo(Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        spyQuestionnaire.setLocalizedDisplayName(stringMap);
        spyQuestionnaire.setLocalizedFinalText(stringMap);
        spyQuestionnaire.setLocalizedWelcomeText(stringMap);
        QuestionnaireDTO questionnaireDTO = questionnaireDTOMapper.apply(spyQuestionnaire);
        assertNotNull(
            "Converting the questionnaire to DTO failed. The returned value was null although a not-null value was expected.",
            questionnaireDTO);
        assertEquals(
            "Converting the questionnaire to DTO failed. The returned id didn't match the expected value.",
            spyQuestionnaire.getId(), questionnaireDTO.getId());
        assertEquals(
            "Converting the questionnaire to DTO failed. The returned description didn't match the expected value.",
            spyQuestionnaire.getDescription(), questionnaireDTO.getDescription());
        assertEquals(
            "Converting the questionnaire to DTO failed. The returned name didn't match the expected value.",
            spyQuestionnaire.getName(), questionnaireDTO.getName());
        assertEquals(
            "Converting the questionnaire to DTO failed. The returned question set didn't match the expected value.",
            spyQuestionnaire.getQuestions().size(), questionnaireDTO.getQuestionDTOs().size());
        assertEquals(
            "Converting the questionnaire to DTO failed. The returned exportTemplate set didn't match the expected value.",
            spyQuestionnaire.getExportTemplates(), questionnaireDTO.getExportTemplates());
        assertEquals(
            "Converting the questionnaire to DTO failed. The returned logo didn't match the expected value.",
            spyQuestionnaire.getLogo(), questionnaireDTO.getLogo());
        assertEquals(
            "Converting the questionnaire to DTO failed. The returned display name didn't match the expected value.",
            spyQuestionnaire.getLocalizedDisplayName(), questionnaireDTO.getLocalizedDisplayName());
        assertEquals(
            "Converting the questionnaire to DTO failed. The returned final text didn't match the expected value.",
            spyQuestionnaire.getLocalizedFinalText(), questionnaireDTO.getLocalizedFinalText());
        assertEquals(
            "Converting the questionnaire to DTO failed. The returned welcome text didn't match the expected value.",
            spyQuestionnaire.getLocalizedWelcomeText(), questionnaireDTO.getLocalizedWelcomeText());
    }

    /**
     * Test of {@link Questionnaire#getAvailableLanguages()} method.<br> Valid input:
     * {@link Question Questions} with random set of localized texts
     */
    @Test
    public void testGetAvailableLanguages() {
        Map<String, String> availableLanguages = new HashMap<>();
        Set<Question> questions = new HashSet<>();
        Integer size = random.nextInt(25) + 1;

        while (availableLanguages.size() < size) {
            availableLanguages.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        }

        while (questions.size() < size) {
            Question question = QuestionTest.getNewValidQuestion();
            question.setLocalizedQuestionText(availableLanguages);
            questions.add(question);
            testQuestionnaire.addQuestion(question);
        }

        Set<String> languages = availableLanguages.keySet();
        testQuestionnaire.setLocalizedDisplayName(availableLanguages);
        Set<String> testLanguages = new HashSet<>();

        for (String language : testQuestionnaire.getAvailableLanguages()) {
            testLanguages.add(language);
        }

        assertNotNull(
            "Getting available languages failed. The returned value was null although not-null value was expected.",
            testLanguages);
        assertEquals(
            "Getting available languages failed. The returned set didn't match the expected value",
            languages, testLanguages);
    }

    /**
     * Test of {@link Questionnaire#getAvailableQuestionLanguages()} method.<br> Valid input:
     * {@link Question Questions} with random set of localized texts
     */
    @Test
    public void testGetAvailableQuestionLanguages() {
        Map<String, String> availableLanguages = new HashMap<>();
        Set<Question> questions = new HashSet<>();
        Integer size = random.nextInt(25) + 1;

        while (availableLanguages.size() < size) {
            availableLanguages.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        }
        Set<String> languages = availableLanguages.keySet();

        while (questions.size() < size) {
            Question question = QuestionTest.getNewValidQuestion();
            question.setLocalizedQuestionText(availableLanguages);
            questions.add(question);
            testQuestionnaire.addQuestion(question);
        }

        Set<String> testLanguages = new HashSet<>();
        for (String language : testQuestionnaire.getAvailableQuestionLanguages()) {
            testLanguages.add(language);
        }

        assertNotNull(
            "Getting available question languages failed. The returned value was null although not-null value was expected.",
            testLanguages);
        assertEquals(
            "Getting available question languages failed. The returned set didn't match the expected value",
            languages, testLanguages);
    }

    /**
     * Test of {@link Questionnaire#isDeletable()} method.<br> Valid input: {@link Questionaire}
     * with {@link Answer Answers} with {@link Response Responses} and {@link Questionaire} with
     * {@link Answer Answers} without {@link Response Responses}
     */
    @Test
    public void testIsDeletable() {
        Questionnaire questionnaireDeletable = QuestionnaireTest.getNewValidQuestionnaire();
        Questionnaire questionnaireNotDeletable = QuestionnaireTest.getNewValidQuestionnaire();
        Set<Response> responses = new HashSet<>();
        Set<Answer> answersWithResponses = new HashSet<>();
        Set<Question> deletableQuestions = new HashSet<>();
        Set<Question> notDeletableQuestions = new HashSet<>();
        Integer size = random.nextInt(10) + 1;

        while (responses.size() < size) {
            responses.add(ResponseTest.getNewValidResponse());
            answersWithResponses.add(AnswerTest.getNewValidRandomAnswer());
            deletableQuestions.add(QuestionTest.getNewValidQuestion());
            notDeletableQuestions.add(QuestionTest.getNewValidQuestion());
        }

        //fill notDeletableQuestions with questions containing answers with responses
        for (Question question : notDeletableQuestions) {
            for (Answer answer : answersWithResponses) {
                answer.addResponses(responses);
            }
            question.addAnswers(answersWithResponses);
        }

        questionnaireDeletable.addQuestions(deletableQuestions);
        questionnaireNotDeletable.addQuestions(notDeletableQuestions);

        assertTrue(
            "Getting isDeletable failed. The returned value was false although true was expected.",
            questionnaireDeletable.isDeletable());
        assertFalse(
            "Getting isDeletable failed. The retunred value was true although false was expected.",
            questionnaireNotDeletable.isDeletable());
    }

    /**
     * Test of {@link Questionnaire#setHasConditions()} and {@link Questionnaire#isHasConditions()}
     * method.<br> Valid input: random Boolean
     */
    @Test
    public void testSetAndIsHasConditions() {
        Boolean hasConditions = random.nextBoolean();
        testQuestionnaire.setHasConditions(hasConditions);
        Boolean testHasConditions = testQuestionnaire.isHasConditions();
        assertNotNull(
            "IsHasCondition failed. The returned value was null although a not-null value was expected.",
            testHasConditions);
        assertEquals("IsHasCondition failed. The returned value didn't match the expected value",
            hasConditions, testHasConditions);
    }

    /**
     * Test of {@link Questionnaire#getLocalizedDisplayName()} and
     * {@link Questionnaire#setLocalizedDisplayName(java.util.Map)} method.<br> Valid input: random
     * Map of Strings
     */
    @Test
    public void testSetGetLocalizedDisplayName() {
        Map<String, String> localizedDisplayName = new HashMap<>();
        Integer size = random.nextInt(10) + 1;

        while (localizedDisplayName.size() < size) {
            localizedDisplayName.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        }

        testQuestionnaire.setLocalizedDisplayName(localizedDisplayName);
        Map<String, String> testLocalizedDisplayName = testQuestionnaire.getLocalizedDisplayName();
        assertNotNull(
            "Getting localizedDisplayName failed. The returned value was null although a not-null value was expected.",
            testLocalizedDisplayName);
        assertEquals(
            "Getting localzedDisplayName failed. The returned value didn't match the expected value.",
            localizedDisplayName, testLocalizedDisplayName);
    }

    /**
     * Test of {@link Questionnaire#getLocalizedDisplayNamesGroupedByCountry()} method.<br> Valid
     * input: random SortedMap of Strings and Maps of Strings
     */
    @Test
    public void testGetLocalizedDisplayNamesByCountry() {
        SortedMap<String, Map<String, String>> testMap = new TreeMap<>();
        Map<String, String> testLocalizedDisplayNameByCountry = new HashMap<>();
        // Creating some Locales
        int countLocales = random.nextInt(10) + 1;
        String[] testLocales = new String[countLocales];
        for (int i = 0; i < countLocales; i++) {
            do {
                if (random.nextBoolean()) {
                    testLocales[i] = Helper.getRandomLocale();
                } else {
                    testLocales[i] = Helper.getRandomLocale().split("_")[1];
                }
            } while (testLocales[i].contains("CH"));
        }
        // Creating some labels
        int countLabels = random.nextInt(200);
        for (int i = 0; i < countLabels; i++) {
            int positionLocale = random.nextInt(countLocales);
            String testLocale = testLocales[positionLocale];
            String testDisplayName = Helper.getRandomAlphabeticString(random.nextInt(50) + 1);
            testLocalizedDisplayNameByCountry.put(testLocale, testDisplayName);
            String testCountry = testLocale;
            if (testCountry.contains("_")) {
                testCountry = testCountry.split("_")[1];
            }
            if (testMap.containsKey(testCountry)) {
                testMap.get(testCountry).put(testLocale, testDisplayName);
            } else {
                Map<String, String> testLocaleQuestionTextMap = new HashMap<>();
                testLocaleQuestionTextMap.put(testLocale, testDisplayName);
                testMap.put(testCountry, testLocaleQuestionTextMap);
            }
        }
        Map<String, String> testLocaleQuestionTextMap = new HashMap<>();
        String testQuestiontext = Helper.getRandomString(random.nextInt(50) + 1);
        testLocalizedDisplayNameByCountry.put("de_CH", testQuestiontext);
        testLocaleQuestionTextMap.put("de_CH", testQuestiontext);
        testQuestiontext = Helper.getRandomString(random.nextInt(50) + 1);
        testLocalizedDisplayNameByCountry.put("fr_CH", testQuestiontext);
        testLocaleQuestionTextMap.put("fr_CH", testQuestiontext);
        testQuestiontext = Helper.getRandomString(random.nextInt(50) + 1);
        testLocalizedDisplayNameByCountry.put("it_CH", testQuestiontext);
        testLocaleQuestionTextMap.put("it_CH", testQuestiontext);
        testMap.put("CH", testLocaleQuestionTextMap);
        testQuestionnaire.setLocalizedDisplayName(testLocalizedDisplayNameByCountry);
        assertEquals("The getting localizedDisplayNamesGroupedByCountry was not the expected one",
            testMap, testQuestionnaire.getLocalizedDisplayNamesGroupedByCountry());
    }

    /**
     * Test of {@link Questionnaire#getBundleQuestionnaires()} and
     * {@link Questionnaire#addBundleQuestionnaires(java.util.Set)} method.<br> Valid input: Set
     * filled with random {@link BundleQuestionnaire BundleQuestionnaires}.<br> Invalid input:
     * <code>null</code>.
     */
    @Test
    public void testGetAndAddBundleQuestionnaires() {
        Set<BundleQuestionnaire> bundleQuestionnaireSet = null;
        Throwable e = null;
        try {
            testQuestionnaire.addBundleQuestionnaires(bundleQuestionnaireSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the BundleQuestionnaires",
            e instanceof AssertionError);

        bundleQuestionnaireSet = new HashSet<>();
        int countBundleQuestionnaires = random.nextInt(50) + 1;
        for (int i = 0; i < countBundleQuestionnaires; i++) {
            bundleQuestionnaireSet.add(BundleQuestionnaireTest.getNewValidBundleQuestionnaire());
        }
        testQuestionnaire.addBundleQuestionnaires(bundleQuestionnaireSet);
        assertNotNull(
            "Adding bundleQuestionnaires failed. The returned value of getBundleQuestionnaires method was null although not-null value was expected.",
            testQuestionnaire.getBundleQuestionnaires());
        assertEquals("Adding bundleQuestionnaires failed. Returned size wasn't the expected value.",
            bundleQuestionnaireSet, testQuestionnaire.getBundleQuestionnaires());
    }

    /**
     * Test of {@link Questionnaire#addBundleQuestionnaire(de.imi.mopat.model.BundleQuestionnaire)}
     * method.<br> Invalid input: <code>null</code>.<br> Valid input: random
     * {@link BundleQuestionnaire}.
     */
    @Test
    public void testAddBundleQuestionnaire() {
        BundleQuestionnaire bundleQuestionnaire = null;
        Throwable e = null;
        try {
            testQuestionnaire.addBundleQuestionnaire(bundleQuestionnaire);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the BundleQuestionnaires",
            e instanceof AssertionError);

        bundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        testQuestionnaire.addBundleQuestionnaire(bundleQuestionnaire);
        Set<BundleQuestionnaire> testBundleQuestionnaire = testQuestionnaire.getBundleQuestionnaires();
        assertNotNull("Adding bundleQuestionnaire failed.", testBundleQuestionnaire);
        assertTrue("AddBundleQuestionnaire failed.",
            testBundleQuestionnaire.contains(bundleQuestionnaire));
        testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        bundleQuestionnaire = new BundleQuestionnaire();
        testQuestionnaire.addBundleQuestionnaire(bundleQuestionnaire);
        assertEquals("The getting BundleQuestionnaire was not the expected one",
            bundleQuestionnaire, testQuestionnaire.getBundleQuestionnaires().iterator().next());
    }

    /**
     * Test of
     * {@link Questionnaire#removeBundleQuestionnaire(de.imi.mopat.model.BundleQuestionnaire)}
     * method. <br> Invalid input: <code>null</code>. <br> Valid input: random
     * {@link BundleQuestionnaire}.
     */
    @Test
    public void testRemoveBundleQuestionnaire() {
        BundleQuestionnaire bundleQuestionnaire = null;
        Throwable e = null;
        try {
            testQuestionnaire.removeBundleQuestionnaire(bundleQuestionnaire);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the BundleQuestionnaires",
            e instanceof AssertionError);

        bundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        Set<BundleQuestionnaire> bundleQuestionnaireSet = new HashSet<>();
        bundleQuestionnaireSet.add(bundleQuestionnaire);
        testQuestionnaire.addBundleQuestionnaires(bundleQuestionnaireSet);
        Set<BundleQuestionnaire> testBundleQuestionnaireSet = testQuestionnaire.getBundleQuestionnaires();
        assertEquals("The returned set of bundleQuestionnaires wasn't the expected one.",
            bundleQuestionnaireSet, testBundleQuestionnaireSet);
        testQuestionnaire.removeBundleQuestionnaire(
            BundleQuestionnaireTest.getNewValidBundleQuestionnaire());
        assertEquals(
            "The returned set of bundleQuestionnaires wasn't the expected one after trying to delete a not associarted BundleQuestionnaire.",
            bundleQuestionnaireSet, testBundleQuestionnaireSet);
        testQuestionnaire.removeBundleQuestionnaire(bundleQuestionnaire);
        assertFalse(
            "Removing bundleQuestionnaire failed. The questionnaire's bundleQuestionnaire set contains bundleQuestionnaire except it wasn't expected to.",
            testQuestionnaire.getBundleQuestionnaires().contains(bundleQuestionnaire));
    }

    /**
     * Test {@link Questionnaire#removeAllBundleQuestionnaires()} method.
     */
    @Test
    public void testRemoveAllBundleQuestionnaires() {
        Set<BundleQuestionnaire> bundleQuestionnaireSet = new HashSet<>();
        Integer size = random.nextInt(50) + 1;

        while (testQuestionnaire.getBundleQuestionnaires().size() < size) {
            testQuestionnaire.addBundleQuestionnaire(
                BundleQuestionnaireTest.getNewValidBundleQuestionnaire());
        }

        testQuestionnaire.addBundleQuestionnaires(bundleQuestionnaireSet);
        testQuestionnaire.removeAllBundleQuestionnaires();
        assertTrue(
            "RemoveAllBundleQuestionnaires failed. The questionnaire's bundleQuestionnaire set wasn't empty except it was expected",
            testQuestionnaire.getBundleQuestionnaires().isEmpty());
    }

    /**
     * Test of {@link Questionnaire#getQuestions()} and
     * {@link Questionnaire#addQuestions(java.util.Collection)} method.<br> Invalid input:
     * <code>null</code>.<br> Valid input: random {@link Question}.
     */
    @Test
    public void testGetAndAddQuestions() {
        Set<Question> testSet = null;
        Throwable e = null;
        try {
            testQuestionnaire.addQuestions(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the Questions", e instanceof AssertionError);

        testSet = new HashSet<>();
        Integer size = random.nextInt(50) + 1;

        while (testSet.size() < size) {
            Question question = QuestionTest.getNewValidQuestion();
            testSet.add(question);
        }

        testQuestionnaire.addQuestions(testSet);

        assertNotNull(
            "The returned set of questions was null although not-null value was expected.",
            testQuestionnaire.getQuestions());
        assertFalse("The returned set was empty although it was expect to be filled.",
            testQuestionnaire.getQuestions().isEmpty());
        assertEquals("The returned set of questions didn't match the expected set.", testSet,
            testQuestionnaire.getQuestions());
    }

    /**
     * Test of {@link Questionnaire#addQuestion(de.imi.mopat.model.Question)} method.<br> Invalid
     * input: <code>null</code>.<br> Valid input: Set filled with random
     * {@link Question Questions}.
     */
    @Test
    public void testAddQuestion() {
        Question testQuestion = null;
        Throwable e = null;
        try {
            testQuestionnaire.addQuestion(testQuestion);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null as the Question", e instanceof AssertionError);

        Set<Question> testSet = new HashSet<>();
        int countQuestions = random.nextInt(50) + 1;
        for (int i = 0; i < countQuestions; i++) {
            testQuestion = QuestionTest.getNewValidQuestion();
            testQuestionnaire.addQuestion(testQuestion);
            testSet.add(testQuestion);
        }
        // Add last Question twice
        testQuestionnaire.addQuestion(testQuestion);
        // Add Question without Questionnaire
        testQuestion = new Question();
        testQuestion.setPosition(Math.abs(random.nextInt()) + 1);
        testQuestionnaire.addQuestion(testQuestion);
        testSet.add(testQuestion);

        assertEquals("The getting set of Questions was not the expected one", testSet,
            testQuestionnaire.getQuestions());
    }

    /**
     * Test of {@link Questionnaire#removeQuestion(de.imi.mopat.model.Question)} method.<br> Invalid
     * input: <code>null</code>.<br> Valid input: random {@link Question}.
     */
    @Test
    public void testRemoveQuestion() {
        Question testQuestion = null;
        Throwable e = null;
        try {
            testQuestionnaire.removeQuestion(testQuestion);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the Questions",
            e instanceof AssertionError);

        testQuestion = QuestionTest.getNewValidQuestion();
        // Add a Question
        testQuestionnaire.addQuestion(testQuestion);
        assertEquals(
            "Adding question failed. The returned value was null although an empty set was expected.",
            1, testQuestionnaire.getQuestions().size());
        // Remove another Question
        testQuestionnaire.removeQuestion(QuestionTest.getNewValidQuestion());
        assertEquals("After removing a not associated Question the set of Questions was altered.",
            1, testQuestionnaire.getQuestions().size());
        //Remove the added Question
        testQuestionnaire.removeQuestion(testQuestion);
        assertTrue(
            "Removing question failed. The returned set wasn't empty although it was expected to be so.",
            testQuestionnaire.getQuestions().isEmpty());
    }

    /**
     * Test of {@link Questionnaire#removeAllQuestions()} method.
     */
    @Test
    public void testRemoveAllQuestions() {
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Integer size = random.nextInt(50) + 1;

        while (questionnaire.getQuestions().size() < size) {
            questionnaire.addQuestion(QuestionTest.getNewValidQuestion());
        }

        questionnaire.removeAllQuestions();
        Set<Question> testQuestionSet = questionnaire.getQuestions();

        assertNotNull(
            "Removing all questions failed. The returned value was null although it was not-null value expected",
            testQuestionSet);
        assertTrue(
            "Removing all questions failed. The returned set wasn't empty although it was expected to be so.",
            testQuestionSet.isEmpty());
    }

    /**
     * Test of {@link Questionnaire#getName()} and {@link Questionnaire#setName(java.lang.String)}
     * method.<br> Invalid input: <code>null</code>, a string with less than 3 or with more than 255
     * characters. <br> Valid input: A string with length greater than 3 and less than 255
     * characters.
     */
    @Test
    public void testGetAndSetName() {
        String testName = null;
        Throwable e = null;
        try {
            testQuestionnaire.setName(testName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the name", e instanceof AssertionError);

        testName = "";
        e = null;
        try {
            testQuestionnaire.setName(testName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set an empty name", e instanceof AssertionError);

        testName = Helper.getRandomAlphabeticString(random.nextInt(3));
        e = null;
        try {
            testQuestionnaire.setName(testName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a name with less than 3 characters",
            e instanceof AssertionError);

        testName = Helper.getRandomAlphabeticString(random.nextInt(50) + 256);
        e = null;
        try {
            testQuestionnaire.setName(testName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a name with more than 255 characters",
            e instanceof AssertionError);

        testName = Helper.getRandomAlphabeticString(random.nextInt(252) + 3);
        testQuestionnaire.setName(testName);
        assertEquals("Getting the name failed. The returned name didn't match the expected value",
            testName, testQuestionnaire.getName());
    }

    /**
     * Test of {@link Questionnaire#getDescription()} and
     * {@link Questionnaire#setDescription(java.lang.String)} method. <br> Invalid input:
     * <code>null</code>, a string with less than 3 or with more than 255 characters. <br> Valid
     * input: A string with length greater than 3 and less than 255 characters.
     */
    @Test
    public void testGetAndSetDescription() {
        String testDescription = null;
        Throwable e = null;
        try {
            testQuestionnaire.setDescription(testDescription);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the description", e instanceof AssertionError);

        testDescription = "";
        e = null;
        try {
            testQuestionnaire.setDescription(testDescription);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set an empty description", e instanceof AssertionError);

        testDescription = Helper.getRandomAlphabeticString(random.nextInt(3));
        e = null;
        try {
            testQuestionnaire.setDescription(testDescription);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a description with less than 3 characters",
            e instanceof AssertionError);

        testDescription = Helper.getRandomAlphabeticString(random.nextInt(50) + 256);
        e = null;
        try {
            testQuestionnaire.setDescription(testDescription);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a description with more than 255 characters",
            e instanceof AssertionError);

        testDescription = Helper.getRandomAlphabeticString(random.nextInt(252) + 3);
        testQuestionnaire.setDescription(testDescription);
        assertEquals(
            "Getting the description failed. The returned name didn't match the expected value",
            testDescription, testQuestionnaire.getDescription());
    }

    /**
     * Test of {@link Questionnaire#getCreatedAt()} method.
     */
    @Test
    public void testGetCreatedAt() {
        Long testCurrentTimeMillis = System.currentTimeMillis();
        Timestamp testTimestampPlus10 = new Timestamp(testCurrentTimeMillis + 10);
        Timestamp testTimestampMinus10 = new Timestamp(testCurrentTimeMillis - 10);

        assertNotNull(
            "Getting createdAt failed. The returned value was null although it was not-null expected.",
            testQuestionnaire.getCreatedAt());
        assertTrue("Getting createdAt faild. The returned value didn't match the expected value.",
            testQuestionnaire.getCreatedAt().before(testTimestampPlus10)
                && testQuestionnaire.getCreatedAt().after(testTimestampMinus10));
    }

    /**
     * Test of @link Questionnaire#getChangedBy()} and @link
     * Questionnaire#setChangedBy(java.lang.Long)} method.<br> Invalid input: <code>null</code> or a
     * long value less or equal than 0.<br> Valid input: Long value greater than 0.
     */
    @Test
    public void testGetAndSetChangedBy() {
        Long testChangedBy = null;
        Throwable e = null;
        try {
            testQuestionnaire.setChangedBy(testChangedBy);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set changedBy as null", e instanceof AssertionError);

        testChangedBy = Math.abs(random.nextLong()) * (-1);
        e = null;
        try {
            testQuestionnaire.setChangedBy(testChangedBy);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative changedBy", e instanceof AssertionError);

        testChangedBy = Math.abs(random.nextLong()) + 1;
        testQuestionnaire.setChangedBy(testChangedBy);
        assertNotNull(
            "Getting changedBy failed. The returned value was null although a not-null value was expected.",
            testQuestionnaire.getChangedBy());
        assertEquals(
            "Getting changedBy failed. The returned value didn't match the expected value.",
            testChangedBy, testQuestionnaire.getChangedBy());
    }

    /**
     * Test of {@link Questionnaire#setUpdatedAt(java.sql.Timestamp)} and
     * {@link Questionnaire#getUpdatedAt()} method.<br> Invalid input: <code>null</code> or a
     * {@link Timestamp} object in the future.<br> Valid input: A Timestamp now or in the past.
     */
    @Test
    public void testSetGetUpdatedAt() {
        Timestamp testUpdatedAt = null;
        Throwable e = null;
        try {
            testQuestionnaire.setUpdatedAt(testUpdatedAt);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set updatedAt as null", e instanceof AssertionError);

        testUpdatedAt = new Timestamp(System.currentTimeMillis() + Math.abs(random.nextLong()));
        e = null;
        try {
            testQuestionnaire.setUpdatedAt(testUpdatedAt);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set updatedAt in the future", e instanceof AssertionError);

        testUpdatedAt = new Timestamp(System.currentTimeMillis() - Math.abs(random.nextLong()));
        testQuestionnaire.setUpdatedAt(testUpdatedAt);

        assertNotNull(
            "Setting or getting updatedAt failed. The returned value of updatedAt was null although it was not-null value expected.",
            testQuestionnaire.getUpdatedAt());
        assertEquals(
            "Setting or getting updatedAt failed. The returned value didn't match the expected value.",
            testUpdatedAt, testQuestionnaire.getUpdatedAt());
    }

    /**
     * Test of {@link Questionnaire#isPublished()} and
     * {@link Questionnaire#setPublished(java.lang.Boolean)} method.<br> Invalid input:
     * <code>null</code>.<br> Valid input: random Boolean
     */
    @Test
    public void testSetAndIsPublished() {
        Boolean testIsPublished = null;
        Throwable e = null;
        try {
            testQuestionnaire.setPublished(testIsPublished);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set published as null", e instanceof AssertionError);

        testIsPublished = random.nextBoolean();
        testQuestionnaire.setPublished(testIsPublished);
        assertNotNull("The returned value isPublished was null", testQuestionnaire.isPublished());
        assertEquals("The returned value isPublished value is wrong", testIsPublished,
            testQuestionnaire.isPublished());
    }

    /**
     * Test of {@link Questionnaire#getLocalizedWelcomeText()}and
     * {@link Questionnaire#setLocalizedWelcomeText(java.util.Map)} method.<br> Valid input: random
     * Map of Strings
     */
    @Test
    public void testGetAndSetLocalizedWelcomeText() {
        Map<String, String> testWelcomeText = new HashMap<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testWelcomeText.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(200)));
        }
        testQuestionnaire.setLocalizedWelcomeText(testWelcomeText);
        assertEquals("The getting map of localizedWelcomeTexts was not the expected one",
            testWelcomeText, testQuestionnaire.getLocalizedWelcomeText());
        testQuestionnaire.setLocalizedWelcomeText(null);
        assertNull("The localizedWelcomeText was not null after setting it so",
            testQuestionnaire.getLocalizedWelcomeText());
    }

    /**
     * Test of {@link Questionnaire#getLocalizedFinalText()} and
     * {@link Questionnaire#setLocalizedFinalText(java.util.Map)} methods.<br> Valid input: random
     * Map of Strings
     */
    @Test
    public void testSetAndGetLocalizedFinalText() {
        Map<String, String> testFinalText = new HashMap<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testFinalText.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(200)));
        }
        testQuestionnaire.setLocalizedFinalText(testFinalText);
        assertEquals("The getting map of localizedFinalTexts was not the expected one",
            testFinalText, testQuestionnaire.getLocalizedFinalText());
        testQuestionnaire.setLocalizedFinalText(null);
        assertNull("The localizedFinalText was not null after setting it so",
            testQuestionnaire.getLocalizedFinalText());
    }

    /**
     * Test of {@link Questionnaire#getLogo()} and {@link Questionnaire#setLogo(java.lang.String)}
     * methods.<br> Valid input: random String
     */
    @Test
    public void testSetGetLogo() {
        String testLogo = Helper.getRandomAlphanumericString(random.nextInt(200) + 1);
        testQuestionnaire.setLogo(testLogo);
        assertNotNull("The returned logo was null although a not-null value was expected.",
            testQuestionnaire.getLogo());
        assertEquals("The returned logo didn't match the expected value.", testLogo,
            testQuestionnaire.getLogo());
    }

    /**
     * Test of {@link Questionnaire#equals}.<br> Valid input: <code>null</code>, the same
     * {@link Questionnaire}, another {@link Questionnaire} and another {@link Object}
     */
    @Test
    public void testEquals() {
        Questionnaire otherQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Object otherObject = new Object();
        assertEquals(
            "The equals method failed. The two inputs didn't equal although they were expected to do so.",
            testQuestionnaire, testQuestionnaire);
        assertNotEquals("The equals method failed. The questionnaire were equal to a null value.",
            null, testQuestionnaire);
        assertNotEquals(
            "The equals method failed. The two inputs were equal although they weren't expected to do so.",
            testQuestionnaire, otherQuestionnaire);
        assertNotEquals(
            "The equals method failed. The two inputs were equal although they were instances of differents classes.",
            testQuestionnaire, otherObject);
    }

    /**
     * Test of {@link Questionnaire#getAvailableQuestionsForScore}.<br> Valid input: random
     * {@link Question Questions}
     */
    @Test
    public void testGetAvailableQuestionsForScore() {
        Set<Question> testSet = new HashSet<>();
        Question testQuestion;
        int countQuestions = random.nextInt(150) + 100;
        for (int i = 0; i < countQuestions; i++) {
            testQuestion = QuestionTest.getNewValidQuestion();
            if (testQuestion.getQuestionType() == QuestionType.NUMBER_INPUT
                || testQuestion.getQuestionType() == QuestionType.SLIDER
                || testQuestion.getQuestionType() == QuestionType.NUMBER_CHECKBOX
                || testQuestion.getQuestionType() == QuestionType.NUMBER_CHECKBOX_TEXT) {
                testSet.add(testQuestion);
            } else if (testQuestion.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                || testQuestion.getQuestionType() == QuestionType.DROP_DOWN) {
                testQuestion.setMaxNumberAnswers(random.nextInt(4));
                if (testQuestion.getMaxNumberAnswers() == 1) {
                    testSet.add(testQuestion);
                }
            }
            testQuestionnaire.addQuestion(testQuestion);
        }
        assertEquals("The getting list of available questions for score was not the expected one",
            testSet, new HashSet<>(testQuestionnaire.getAvailableQuestionsForScore()));
    }

    /**
     * Test of {@link Questionnaire#getAvailableScoresForScore}.<br> Valid input:
     */
    @Test
    public void testGetAvailableScoresForScore() {
        Score testScore1 = ScoreTest.getNewValidScore();
        Score testScore2 = ScoreTest.getNewValidScore();
        Score testScore3 = ScoreTest.getNewValidScore();
        Score testScore4 = ScoreTest.getNewValidScore();

        testQuestionnaire.addScore(testScore1);
        testQuestionnaire.addScore(testScore2);
        testQuestionnaire.addScore(testScore3);
        testQuestionnaire.addScore(testScore4);

        UnaryExpression testExpression = new UnaryExpression();
        testExpression.setScore(testScore1);
        testScore4.setExpression(testExpression);

        Set<Score> testSet = new HashSet<>();
        testSet.add(testScore2);
        testSet.add(testScore3);

        assertEquals("The getting list of available scores for score was not the expected one",
            testSet, new HashSet<>(testQuestionnaire.getAvailableScoresForScore(testScore1)));
    }
}
