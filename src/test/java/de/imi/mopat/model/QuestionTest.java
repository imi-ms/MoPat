package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.helper.controller.ContextLoaderListener;
import de.imi.mopat.helper.model.QuestionDTOMapper;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTest;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.utils.Helper;
import jakarta.servlet.ServletContextEvent;
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
import org.springframework.mock.web.MockServletContext;
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
public class QuestionTest {

    private static final Random random = new Random();

    @Autowired
    private QuestionDTOMapper questionDTOMapper;
    private Question testQuestion;

    public QuestionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        // Mock the ServletContext
        MockServletContext mockServletContext = new MockServletContext();
        new ContextLoaderListener().contextInitialized(new ServletContextEvent(mockServletContext));
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new Question
     *
     * @return Returns a valid new Question
     */
    public static Question getNewValidQuestion() {
        Boolean isRequired = random.nextBoolean();
        Boolean isEnabled = random.nextBoolean();
        QuestionType questiontype = Helper.getRandomEnum(QuestionType.class);
        Integer position = Math.abs(random.nextInt());
        Map<String, String> localizedQuestionText = new HashMap<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            localizedQuestionText.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(50) + 3));
        }
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        Question question = new Question(localizedQuestionText, isRequired, isEnabled, questiontype,
            position, questionnaire);

        return question;
    }

    /**
     * Returns a valid new Question with given Questionnaire
     *
     * @param questionnaire Questionnaire this Question is associated to
     * @return Returns a valid new Question with given Questionnaire
     */
    public static Question getNewValidQuestion(Questionnaire questionnaire) {
        Boolean isRequired = random.nextBoolean();
        Boolean isEnabled = random.nextBoolean();
        QuestionType questiontype = Helper.getRandomEnum(QuestionType.class);
        Integer position = Math.abs(random.nextInt());
        Map<String, String> localizedQuestionText = new HashMap<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            localizedQuestionText.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(50) + 1));
        }

        Question question = new Question(localizedQuestionText, isRequired, isEnabled, questiontype,
            position, questionnaire);

        return question;
    }

    /**
     * Returns a valid new Question with given localizedQuestiontext and Questionnaire
     *
     * @param localizedQuestionText localized question text of this question as a map
     * @param questionnaire         Questionnaire this Question is associated to
     * @return Returns a valid new Question with given localizedQuestiontext and Questionnaire
     */
    public static Question getNewValidQuestion(Map<String, String> localizedQuestionText,
        Questionnaire questionnaire) {
        Boolean isRequired = random.nextBoolean();
        Boolean isEnabled = random.nextBoolean();
        QuestionType questiontype = Helper.getRandomEnum(QuestionType.class);
        Integer position = Math.abs(random.nextInt());

        Question question = new Question(localizedQuestionText, isRequired, isEnabled, questiontype,
            position, questionnaire);

        return question;
    }

    @Before
    public void setUp() {
        testQuestion = getNewValidQuestion();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link Question#getLocalizedQuestionText} and
     * {@link Question#setLocalizedQuestionText}.<br> Valid input: random map of Strings
     */
    @Test
    public void testGetAndSetLocalizedQuestionText() {
        Map<String, String> testLocalizedQuestionText = new HashMap<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            testLocalizedQuestionText.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(50) + 1));
        }
        testQuestion.setLocalizedQuestionText(testLocalizedQuestionText);
        assertEquals("The getting map pf LocalizedQuestionTexts was not the expected one",
            testLocalizedQuestionText, testQuestion.getLocalizedQuestionText());
    }

    /**
     * Test of {@link Question#getIsRequired} and {@link Question#setIsRequired}.<br> Invalid
     * input:
     * <code>null</code><br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetIsRequired() {
        Boolean testIsRequired = null;
        Throwable e = null;
        try {
            testQuestion.setIsRequired(testIsRequired);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set isRequired to null", e instanceof AssertionError);

        testIsRequired = random.nextBoolean();
        testQuestion.setIsRequired(testIsRequired);
        assertEquals("The getting isRequired was not the expected one", testIsRequired,
            testQuestion.getIsRequired());
    }

    /**
     * Test of {@link Question#getIsEnabled} and {@link Question#getIsEnabled}.<br> Invalid input:
     * <code>null</code><br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetIsEnabled() {
        Boolean testIsEnabled = null;
        Throwable e = null;
        try {
            testQuestion.setIsEnabled(testIsEnabled);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set isEnabled to null", e instanceof AssertionError);

        testIsEnabled = random.nextBoolean();
        testQuestion.setIsEnabled(testIsEnabled);
        assertEquals("The getting isEnabled was not the expected one", testIsEnabled,
            testQuestion.getIsEnabled());
    }

    /**
     * Test of {@link Question#getQuestionType} and {@link Question#setQuestionType}.<br> Invalid
     * input: <code>null</code><br> Valid input: random {@link QuestionType}
     */
    @Test
    public void testGetAndSetQuestionType() {
        QuestionType testQuestionType = null;
        Throwable e = null;
        try {
            testQuestion.setQuestionType(testQuestionType);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the QuestionType to null", e instanceof AssertionError);

        testQuestionType = Helper.getRandomEnum(QuestionType.class);
        testQuestion.setQuestionType(testQuestionType);
        assertEquals("The getting QuestionType was not the expected one", testQuestionType,
            testQuestion.getQuestionType());
    }

    /**
     * Test of {@link Question#getPosition} and {@link Question#setPosition}.<br> Invalid input:
     * <code>null</code>, negative Integer<br> Valid input: positive Integer
     */
    @Test
    public void testGetAndSetPosition() {
        Integer testPosition = null;
        Throwable e = null;
        try {
            testQuestion.setPosition(testPosition);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the position to null", e instanceof AssertionError);

        testPosition = Math.abs(random.nextInt()) * -1;
        e = null;
        try {
            testQuestion.setPosition(testPosition);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative position", e instanceof AssertionError);

        testPosition *= -1;
        testQuestion.setPosition(testPosition);
        assertEquals("The getting position was not the expected one", testPosition,
            testQuestion.getPosition());
    }

    /**
     * Test of {@link Question#getMinNumberAnswers} and {@link Question#setMinNumberAnswers}.<br>
     * Invalid input: negative Integer, min greater than max<br> Valid input: positive Integer, min
     * less than max
     */
    @Test
    public void testGetAndSetMinNumberAnswers() {
        Integer testMin = random.nextInt(Integer.MAX_VALUE / 2) * -1;
        Throwable e = null;
        try {
            testQuestion.setMinNumberAnswers(testMin);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative minNumberAnswers",
            e instanceof AssertionError);

        // Set max positive and min = max*2
        Integer testMax = testMin * (-1) + 1;
        testMin = testMax * 2;
        e = null;
        try {
            testQuestion.setMaxNumberAnswers(testMax);
            testQuestion.setMinNumberAnswers(testMin);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a greater minNumberAnswers than maxNumberAnswers",
            e instanceof AssertionError);

        testMin = random.nextInt(Integer.MAX_VALUE / 2) + 1;
        testMax = testMin * 2;
        testQuestion.setMaxNumberAnswers(testMax);
        testQuestion.setMinNumberAnswers(testMin);
        assertEquals("The getting minNumberAnswers was not the expected one", testMin,
            testQuestion.getMinNumberAnswers());

        testMin = Math.abs(random.nextInt()) + 1;
        testQuestion.setMaxNumberAnswers(null);
        testQuestion.setMinNumberAnswers(testMin);
        assertEquals("The getting minNumberAnswers was not the expected one", testMin,
            testQuestion.getMinNumberAnswers());
    }

    /**
     * Test of {@link Question#getMaxNumberAnswers} and {@link Question#setMaxNumberAnswers}.<br>
     * Invalid input: negative Integer, max less than min<br> Valid input: positive Integer
     */
    @Test
    public void testGetAndSetMaxNumberAnswers() {
        Integer testMax = Math.abs(random.nextInt()) * -1;
        Throwable e = null;
        try {
            testQuestion.setMaxNumberAnswers(testMax);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative maxNumberAnswers",
            e instanceof AssertionError);

        Integer testMin = testMax * (-1) + 2;
        testMax = testMin / 2;
        e = null;
        try {
            testQuestion.setMinNumberAnswers(testMin);
            testQuestion.setMaxNumberAnswers(testMax);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a greater minNumberAnswers than maxNumberAnswers",
            e instanceof AssertionError);

        testMax = Math.abs(random.nextInt()) + 2;
        testMin = testMax / 2;
        testQuestion.setMinNumberAnswers(testMin);
        testQuestion.setMaxNumberAnswers(testMax);
        assertEquals("The getting maxNumberAnswers was not the expected one", testMax,
            testQuestion.getMaxNumberAnswers());

        testMax = Math.abs(random.nextInt()) + 1;
        testQuestion.setMinNumberAnswers(null);
        testQuestion.setMaxNumberAnswers(testMax);
        assertEquals("The getting maxNumberAnswers was not the expected one", testMax,
            testQuestion.getMaxNumberAnswers());
    }

    /**
     * Test of {@link Question#setMinMaxNumberAnswers}.<br> Invalid input: megative min, negative
     * max, min greater than max<br> Valid input: positive min less than max
     */
    @Test
    public void testSetMinMaxNumberAnswers() {
        Integer negative = Math.abs(random.nextInt()) * -1;
        Throwable e = null;
        try {
            testQuestion.setMinMaxNumberAnswers(null, negative);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative maxNumberAnswers",
            e instanceof AssertionError);

        e = null;
        try {
            testQuestion.setMinMaxNumberAnswers(negative, null);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative minNumberAnswers",
            e instanceof AssertionError);

        Integer testMax = Math.abs(random.nextInt());
        Integer testMin = Math.abs(random.nextInt(testMax));
        e = null;
        try {
            testQuestion.setMinMaxNumberAnswers(testMax, testMin);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a greater minNumberAnswers than maxNumberAnswers",
            e instanceof AssertionError);

        // Test all combinations
        testQuestion.setMinMaxNumberAnswers(null, null);
        assertNull("MinNumberAnswers was not null after setting it so",
            testQuestion.getMinNumberAnswers());
        assertNull("MaxNumberAnswers was not null after setting it so",
            testQuestion.getMaxNumberAnswers());

        testQuestion.setMinMaxNumberAnswers(testMin, null);
        assertEquals("The gettin minNumberAnswers was not the expected one", testMin,
            testQuestion.getMinNumberAnswers());
        assertNull("MaxNumberAnswers was not null after setting it so",
            testQuestion.getMaxNumberAnswers());

        testQuestion.setMinMaxNumberAnswers(null, testMax);
        assertNull("MinNumberAnswers was not null after setting it so",
            testQuestion.getMinNumberAnswers());
        assertEquals("The getting maxNumberAnswers was not the expected one", testMax,
            testQuestion.getMaxNumberAnswers());

        testQuestion.setMinMaxNumberAnswers(testMin, testMax);
        assertEquals("The getting minNumberAnswers was not the expected one", testMin,
            testQuestion.getMinNumberAnswers());
        assertEquals("The getting maxNumberAnswers was not the expected one", testMax,
            testQuestion.getMaxNumberAnswers());
    }

    /**
     * Test of {@link Question#addAnswers} and {@link Question#getAnswers}.<br> Invalid input:
     * <code>null</code><br> Valid input: random set of {@link Answer Answers}
     */
    @Test
    public void testAddAnswers() {
        Set<Answer> testSet = null;
        Answer testAnswer;
        Throwable e = null;
        try {
            testQuestion.addAnswers(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null as a Answer", e instanceof AssertionError);

        testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            testAnswer = AnswerTest.getNewValidRandomAnswer();
            testSet.add(testAnswer);
        }
        testQuestion.addAnswers(testSet);
        assertEquals("The getting list of Answers was not the expected one", testSet,
            new HashSet<>(testQuestion.getAnswers()));
    }

    /**
     * Test of {@link Question#addAnswer} and {@link Question#getAnswers}.<br> Invalid input:
     * <code>null</code><br> Valid input: random number of {@link Answer Answers}
     */
    @Test
    public void testAddAnswer() {
        Answer testAnswer = null;
        Throwable e = null;
        try {
            testQuestion.addAnswer(testAnswer);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null as a Answer", e instanceof AssertionError);

        Set<Answer> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            testAnswer = AnswerTest.getNewValidRandomAnswer();
            testSet.add(testAnswer);
            testQuestion.addAnswer(testAnswer);
        }
        // Add Answer without Question
        testAnswer = new SliderAnswer();
        testSet.add(testAnswer);
        testQuestion.addAnswer(testAnswer);
        assertEquals("The getting list of Answers was not the expected one", testSet,
            new HashSet<>(testQuestion.getAnswers()));
    }

    /**
     * Test of {@link Question#removeAnswer}.<br> Invalid input: <code>null</code><br> Valid input:
     * existing {@link Answer}
     */
    @Test
    public void testRemoveAnswer() {
        Answer testAnswer = null;
        Throwable e = null;
        try {
            testQuestion.removeAnswer(testAnswer);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the Answers", e instanceof AssertionError);

        Set<Answer> testSet = new HashSet<>();
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            testAnswer = AnswerTest.getNewValidRandomAnswer();
            testSet.add(testAnswer);
            testQuestion.addAnswer(testAnswer);
        }
        assertEquals("The getting list of Answers was not the expected one", testSet,
            new HashSet<>(testQuestion.getAnswers()));
        testQuestion.removeAnswer(testAnswer);
        testSet.remove(testAnswer);
        assertEquals("The getting list after removing an Answer was not the expected one", testSet,
            new HashSet<>(testQuestion.getAnswers()));

        // Try to remove an Answer which is not associated
        testAnswer = AnswerTest.getNewValidRandomAnswer();
        getNewValidQuestion().addAnswer(testAnswer);
        testQuestion.removeAnswer(testAnswer);
        assertEquals(
            "The getting list after removing a not associated Answer was not the expected one",
            testSet, new HashSet<>(testQuestion.getAnswers()));
    }

    /**
     * Test of {@link Question#removeAllAnswers}.<br> A previously added set of random
     * {@link Answer Answers} is removed.
     */
    @Test
    public void testRemoveAllAnswers() {
        Set<Answer> testSet = new HashSet<>();
        Answer testAnswer;
        int count = Math.abs(random.nextInt(50)) + 1;
        for (int i = 0; i < count; i++) {
            testAnswer = AnswerTest.getNewValidRandomAnswer();
            testSet.add(testAnswer);
        }
        testQuestion.addAnswers(testSet);
        assertEquals("The getting list of Answers was not the expected one", testSet,
            new HashSet<>(testQuestion.getAnswers()));
        testQuestion.removeAllAnswers();
        assertTrue("The Answers were not removed correctly", testQuestion.getAnswers().isEmpty());
    }

    /**
     * Test of {@link Question#getQuestionnaire} and {@link Question#setQuestionnaire}.<br> Invalid
     * input: <code>null</code><br> Valid input: random {@link Questionnaire}
     */
    @Test
    public void testGetAndSetQuestionnaire() {
        Questionnaire testQuestionnaire = null;
        Throwable e = null;
        try {
            testQuestion.setQuestionnaire(testQuestionnaire);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the Questionnaire as null", e instanceof AssertionError);

        testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        testQuestion.setQuestionnaire(testQuestionnaire);
        assertEquals("The getting Questionnaire was not the expected one", testQuestionnaire,
            testQuestion.getQuestionnaire());
    }

    /**
     * Test of {@link Question#removeQuestionnaire}.
     */
    @Test
    public void testRemoveQuestionnaire() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        testQuestion.setQuestionnaire(testQuestionnaire);
        assertEquals("The getting Questionnaire was not the expected one", testQuestionnaire,
            testQuestion.getQuestionnaire());
        testQuestion.removeQuestionnaire();
        assertNull("The Questionnaire was not null after removing",
            testQuestion.getQuestionnaire());
        testQuestion.removeQuestionnaire();
        assertNull("The Questionnaire was not null after removing it again",
            testQuestion.getQuestionnaire());
    }

    /**
     * Test of {@link Question#addCondition} and {@link Question#getConditions}.<br> Invalid input:
     * <code>null</code><br> Valid input: random number of {@link Condition Conditions}S
     */
    @Test
    public void testAddCondition() {
        Condition testCondition = null;
        Throwable e = null;
        try {
            testQuestion.addCondition(testCondition);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the Conditions", e instanceof AssertionError);

        Set<Condition> testSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testCondition = ConditionTest.getNewValidCondition();
            testQuestion.addCondition(testCondition);
            testSet.add(testCondition);
        }
        // Add the last Condition again
        testQuestion.addCondition(testCondition);
        // Add Condition without Trigger
        testCondition = new SelectAnswerCondition();
        testQuestion.addCondition(testCondition);
        testSet.add(testCondition);
        assertEquals("The getting set of conditions was not the expected one", testSet,
            testQuestion.getConditions());
    }

    /**
     * Test of {@link Question#addConditions} and {@link Question#getConditions}.<br> Invalid
     * input:
     * <code>null</code><br> Valid input: random number of {@link Condition Conditions}
     */
    @Test
    public void testAddConditions() {
        Set<Condition> testSet = null;

        Throwable e = null;
        try {
            testQuestion.addConditions(testSet);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the Conditions", e instanceof AssertionError);

        testSet = new HashSet<>();
        Condition testCondition;
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testCondition = ConditionTest.getNewValidCondition();
            testSet.add(testCondition);
        }
        testQuestion.addConditions(testSet);
        assertEquals("The getting set of conditions was not the expected one", testSet,
            testQuestion.getConditions());
    }

    /**
     * Test of {@link Question#contains}.<br> Invalid input: <code>null</code><br> Valid input:
     * valid condition (not) in set
     */
    @Test
    public void testContains() {
        Condition testCondition = null;
        Throwable e = null;
        try {
            testQuestion.contains(testCondition);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to to check if the conditions contais null",
            e instanceof AssertionError);

        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testCondition = ConditionTest.getNewValidCondition();
            assertFalse("contains returned true even though the condition was not added before",
                testQuestion.contains(testCondition));
            testQuestion.addCondition(testCondition);
            assertTrue("contains returned false even though the condition was added before",
                testQuestion.contains(testCondition));
        }
    }

    /**
     * Test of {@link Question#removeCondition}.<br> Invalid input: <code>null</code><br> Valid
     * input: random {@link Condtion}
     */
    @Test
    public void testRemoveCondition() {
        Condition testRemoveCondition = null;
        Throwable e = null;
        try {
            testQuestion.removeCondition(testRemoveCondition);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the Conditions",
            e instanceof AssertionError);

        Set<Condition> testSet = new HashSet<>();
        Condition testCondition;
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testCondition = ConditionTest.getNewValidCondition();
            testQuestion.addCondition(testCondition);
            testSet.add(testCondition);
            if (testRemoveCondition == null && random.nextBoolean() || i == count - 1) {
                testRemoveCondition = testCondition;
            }
        }
        assertEquals("The getting set of conditions was not the expected one", testSet,
            testQuestion.getConditions());
        testQuestion.removeCondition(testRemoveCondition);
        testSet.remove(testRemoveCondition);
        assertEquals("The getting set of conditions after removing was not the expected one",
            testSet, testQuestion.getConditions());
        // Remove with no Trigger
        testQuestion.removeCondition(new SliderAnswerThresholdCondition());
        assertEquals(
            "The getting set of conditions was altered after removing a Condition with no Trigger",
            testSet, testQuestion.getConditions());
        //Remove with other Trigger
        testQuestion.removeCondition(ConditionTest.getNewValidCondition());
        assertEquals(
            "The getting set of conditions was altered after removing a Condition with other Trigger",
            testSet, testQuestion.getConditions());
    }

    /**
     * Test of {@link Question#removeConditions}.<br> Invalid input: <code>null</code><br> Valid
     * input: random number of {@link Condition Conditions}
     */
    @Test
    public void testRemoveConditions() {
        Set<Condition> testRemoveConditions = null;
        Throwable e = null;
        try {
            testQuestion.removeConditions(testRemoveConditions);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the Conditions",
            e instanceof AssertionError);

        Set<Condition> testSet = new HashSet<>();
        testRemoveConditions = new HashSet<>();
        Condition testCondition;
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testCondition = ConditionTest.getNewValidCondition();
            testQuestion.addCondition(testCondition);
            testSet.add(testCondition);
            if (random.nextBoolean()) {
                testRemoveConditions.add(testCondition);
            }
        }
        assertEquals("The getting set of conditions was not the expected one", testSet,
            testQuestion.getConditions());
        testQuestion.removeConditions(testRemoveConditions);
        testSet.removeAll(testRemoveConditions);
        assertEquals("The getting set of conditions after removing was not the expected one",
            testSet, testQuestion.getConditions());
    }

    /**
     * Test of {@link Question#hasConditionsAsTrigger}.<br> Valid input: {@link Question} with
     * {@link Condition}, {@link Question} with {@link SelectAnswer} or {@link SliderAnswer} with
     * {@link Condition}
     */
    @Test
    public void testHasConditionsAsTrigger() {
        assertFalse(
            "hasConditionAsTrigger returned true even though the question or any Answers has no Conditions",
            testQuestion.hasConditionsAsTrigger());
        Condition testCondition = ConditionTest.getNewValidCondition();
        testQuestion.addCondition(testCondition);
        assertTrue("hasConditionAsTrigger returned false even though the question has a Condition",
            testQuestion.hasConditionsAsTrigger());

        testQuestion.removeCondition(testCondition);
        int countSelectAnswers = random.nextInt(25) + 1;
        for (int i = 0; i < countSelectAnswers; i++) {
            testQuestion.addAnswer(SelectAnswerTest.getNewValidSelectAnswer());
            if (random.nextBoolean()) {
                testQuestion.addAnswer(AnswerTest.getNewValidRandomAnswer());
            }
        }
        assertFalse(
            "hasConditionAsTrigger returned true even though the question or any Answers has no Conditions",
            testQuestion.hasConditionsAsTrigger());
        Answer testAnswer = SelectAnswerTest.getNewValidSelectAnswer();
        testAnswer.addCondition(testCondition);
        testQuestion.addAnswer(testAnswer);
        assertTrue(
            "hasConditionAsTrigger returned false even though a SelectAnswer has a Condition",
            testQuestion.hasConditionsAsTrigger());

        testQuestion.removeAllAnswers();
        int countSliderAnswers = random.nextInt(25) + 1;
        for (int i = 0; i < countSliderAnswers; i++) {
            testQuestion.addAnswer(SliderAnswerTest.getNewValidSliderAnswer());
            if (random.nextBoolean()) {
                testQuestion.addAnswer(AnswerTest.getNewValidRandomAnswer());
            }
        }
        assertFalse(
            "hasConditionAsTrigger returned true even though the question or any Answers has no Conditions",
            testQuestion.hasConditionsAsTrigger());
        testAnswer = SliderAnswerTest.getNewValidSliderAnswer();
        testAnswer.addCondition(testCondition);
        testQuestion.addAnswer(testAnswer);
        assertTrue(
            "hasConditionAsTrigger returned false even though a SliderAnswer has a Condition",
            testQuestion.hasConditionsAsTrigger());
    }

    /**
     * Test of {@link Question#hasConditionsAsTarget} and
     * {@link Question#setHasConditionsAsTarget}.<br> Valid input: random Boolean
     */
    @Test
    public void testHasAndSetHasConditionsAsTarget() {
        Boolean testHasConditionsAsTarget = random.nextBoolean();
        testQuestion.setHasConditionsAsTarget(testHasConditionsAsTarget);
        assertEquals("The getting HasConditionsAsTarget is not the expected one",
            testHasConditionsAsTarget, testQuestion.hasConditionsAsTarget());
    }

    /**
     * Test of {@link Question#hasScores} and {@link Question#setHasScores}.<br> Valid input: random
     * Boolean
     */
    @Test
    public void testHasAndSetHasScores() {
        Boolean testHasScores = random.nextBoolean();
        testQuestion.setHasScores(testHasScores);
        assertEquals("The getting HasScores is not the expected one", testHasScores,
            testQuestion.hasScores());
    }

    /**
     * Test of {@link Question#hasExportRule}.<br> Valid input: {@link Question} without
     * {@link Answer Answers}, {@link Question} with {@link Answer Answers} without
     * {@link ExportRule ExportRules} and {@link Question} with {@link Answer Answers} with
     * {@link ExportRule ExportRules}
     */
    @Test
    public void testHasExportRule() {
        Answer testAnswer = AnswerTest.getNewValidRandomAnswer();
        assertFalse("A Question without any Answers has ExportRules", testQuestion.hasExportRule());
        testQuestion.addAnswer(testAnswer);
        assertFalse("A Question with an Answer without ExportRule has ExportRules",
            testQuestion.hasExportRule());
        Boolean hasExportRules = false;
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testAnswer = AnswerTest.getNewValidRandomAnswer();
            if (random.nextBoolean()) {
                hasExportRules = true;
                testAnswer.addExportRule(
                    ExportRuleAnswerTest.getNewValidExportRuleAnswer(testAnswer));
                testQuestion.addAnswer(testAnswer);
            }
        }
        assertEquals("The getting hasExportRule was not the expected one", hasExportRules,
            testQuestion.hasExportRule());
    }

    /**
     * Test of {@link Question#getExportRuleFormatFromAnswers}.<br> Valid input: new
     * {@link Question} without {@link Answer Answers}, {@link Question} with {@link Answer} with
     * {@link ExportRuleAnswer}
     */
    @Test
    public void testGetExportRuleFormatFromAnswers() {
        assertNull("A new Question had an ExportRuleFormat from an Answers",
            testQuestion.getExportRuleFormatFromAnswers(
                ExportTemplateTest.getNewValidExportTemplate()));
        //Add some Answers without ExportRules
        int countExportRules = random.nextInt(50) + 1;
        for (int i = 0; i < countExportRules; i++) {
            testQuestion.addAnswer(AnswerTest.getNewValidRandomAnswer());
        }
        // Add ExportRule with Template and Format which is looked for
        Answer testAnswer = AnswerTest.getNewValidRandomAnswer();
        ExportRuleAnswer testExportRule = ExportRuleAnswerTest.getNewValidExportRuleAnswer();
        testExportRule.setExportRuleFormat(ExportRuleFormatTest.getNewValidExportRuleFormat());
        testAnswer.addExportRule(testExportRule);
        testQuestion.addAnswer(testAnswer);
        assertEquals("The getting ExportRuleFormat was not the expected one",
            testExportRule.getExportRuleFormat(),
            testQuestion.getExportRuleFormatFromAnswers(testExportRule.getExportTemplate()));
    }

    /**
     * Test of {@link Question#getLocalizedQuestionTextGroupedByCountry}.<br> Valid input: valid
     * {@link Question}
     */
    @Test
    public void testGetLocalizedQuestionTextGroupedByCountry() {
        SortedMap<String, Map<String, String>> testMap = new TreeMap<>();
        Map<String, String> testLocalizedQuestionText = new HashMap<>();
        // Creating some Locales
        int countLocales = random.nextInt(10) + 5;
        String[] testLocales = new String[countLocales];
        for (int i = 0; i < countLocales; i++) {
            String currentLocale = Helper.getRandomLocale();
            while (currentLocale.split("_")[1].equals("CH")) {
                currentLocale = Helper.getRandomLocale();
            }
            if (random.nextBoolean()) {
                testLocales[i] = currentLocale;
            } else {
                testLocales[i] = currentLocale.split("_")[1];
            }
        }
        // Creating some labels
        int countLabels = random.nextInt(200) + 200;
        for (int i = 0; i < countLabels; i++) {
            int positionLocale = random.nextInt(countLocales);
            String testLocale = testLocales[positionLocale];
            String testQuestiontext = Helper.getRandomString(random.nextInt(50) + 1);
            testLocalizedQuestionText.put(testLocale, testQuestiontext);
            String testCountry = testLocale;
            if (testCountry.contains("_")) {
                testCountry = testCountry.split("_")[1];
            }
            if (testMap.containsKey(testCountry)) {
                testMap.get(testCountry).put(testLocale, testQuestiontext);
            } else {
                Map<String, String> testLocaleQuestionTextMap = new HashMap<>();
                testLocaleQuestionTextMap.put(testLocale, testQuestiontext);
                testMap.put(testCountry, testLocaleQuestionTextMap);
            }
        }
        Map<String, String> testLocaleQuestionTextMap = new HashMap<>();
        String testQuestiontext = Helper.getRandomString(random.nextInt(50) + 1);
        testLocalizedQuestionText.put("de_CH", testQuestiontext);
        testLocaleQuestionTextMap.put("de_CH", testQuestiontext);
        testQuestiontext = Helper.getRandomString(random.nextInt(50) + 1);
        testLocalizedQuestionText.put("fr_CH", testQuestiontext);
        testLocaleQuestionTextMap.put("fr_CH", testQuestiontext);
        testQuestiontext = Helper.getRandomString(random.nextInt(50) + 1);
        testLocalizedQuestionText.put("it_CH", testQuestiontext);
        testLocaleQuestionTextMap.put("it_CH", testQuestiontext);
        testMap.put("CH", testLocaleQuestionTextMap);

        testQuestion.setLocalizedQuestionText(testLocalizedQuestionText);
        assertEquals("The getting localizedAnswerLabelGroupedByCountry was not the expected one",
            testMap, testQuestion.getLocalizedQuestionTextGroupedByCountry());
    }

    /**
     * Test of {@link Question#equals}.<br> Invalid input: the same {@link Question} twice in a set
     */
    @Test
    public void testEquals() {
        HashSet<Question> testSet = new HashSet<>();
        testSet.add(testQuestion);
        testSet.add(testQuestion);
        assertEquals("It was possible to add the same Question twice in one set", 1,
            testSet.size());

        assertEquals("The Question was not equal to itself", testQuestion, testQuestion);
        assertNotEquals("The Question was equal to null", null, testQuestion);
        Question otherQuestion = getNewValidQuestion();
        assertNotEquals("The Question was equal to a different Question", testQuestion,
            otherQuestion);
        Object otherObject = new Object();
        assertNotEquals("The Question was equal to a different Object", testQuestion, otherObject);
    }

    /**
     * Test of {@link Question#compareTo}.<br> Valid input: lower, greater and same position
     */
    @Test
    public void testCompareTo() {
        Question testQuestion1 = getNewValidQuestion();
        Question testQuestion2 = getNewValidQuestion();
        int greater = Math.abs(random.nextInt()) + 1;
        int lower = random.nextInt(greater);
        if (lower == 0) {
            lower += 1;
        }
        testQuestion1.setPosition(greater);
        testQuestion2.setPosition(lower);
        assertTrue("The compareTo to a lower position was not right",
            testQuestion1.compareTo(testQuestion2) > 0);
        assertTrue("The compareTo to a greater position was not right",
            testQuestion2.compareTo(testQuestion1) < 0);
        assertEquals("The compareTo to the same position", 0,
            testQuestion1.compareTo(testQuestion1));
    }

    /**
     * Test of {@link Question#isModifiable}.<br> Valid input: new {@link Question},
     * {@link Question} with {@link Answer Answers} without {@link Response Responses},
     * {@link Question} with {@link Answer Answers} with {@link Response Responses}
     */
    @Test
    public void testIsModifiable() {
        assertTrue("A new Question was not modifiable", testQuestion.isModifiable());
        Answer testAnswer1 = AnswerTest.getNewValidRandomAnswer();
        testQuestion.addAnswer(testAnswer1);
        assertTrue("A Question with an Answer without Responses was not modifiable",
            testQuestion.isModifiable());
        Answer testAnswer2 = AnswerTest.getNewValidRandomAnswer();
        testAnswer2.addResponse(ResponseTest.getNewValidResponse());
        testQuestion.addAnswer(testAnswer2);
        assertFalse("A Question with an Answer with Response was modifiable",
            testQuestion.isModifiable());
    }

    /**
     * Test of {@link Question#isDeletable}.<br> Valid input: new
     * {@link Question without {@link Answer Answers}, {@link Question} with {@link Answer} with
     * {@link Response}
     */
    @Test
    public void testIsDeletable() {
        // New Question
        assertTrue("A new Question was not deletable", testQuestion.isDeletable());
        // Question with Answers without Responses
        int countAnswers = random.nextInt(50) + 1;
        for (int i = 0; i < countAnswers; i++) {
            testQuestion.addAnswer(AnswerTest.getNewValidRandomAnswer());
        }
        assertTrue("A Question with Answers without Responses was not deletable",
            testQuestion.isDeletable());
        // Question with Answer with Response
        Answer testAnswer = AnswerTest.getNewValidRandomAnswer();
        testAnswer.addResponse(ResponseTest.getNewValidResponse());
        testQuestion.addAnswer(testAnswer);
        assertFalse("A Question with an Answer with Response was deletable",
            testQuestion.isDeletable());
    }

    /**
     * Test of {@link Question#cloneWithAnswersAndReferenceToQuestionnaire}.<br> Valid input: valid
     * {@link Question}
     */
    @Test
    public void testCloneWithAnswersAndReferenceToQuestionnaire() {
        int countAnswers = random.nextInt(50) + 1;
        for (int i = 0; i < countAnswers; i++) {
            testQuestion.addAnswer(AnswerTest.getNewValidRandomAnswer());
        }
        Question testClone = testQuestion.cloneWithAnswersAndReferenceToQuestionnaire();
        assertEquals("The getting LocalizedQuestionText was not the expected one",
            testQuestion.getLocalizedQuestionText(), testClone.getLocalizedQuestionText());
        assertEquals("The getting isRequired was not the expected one",
            testQuestion.getIsRequired(), testClone.getIsRequired());
        assertEquals("The getting isEnabled was not the expected one", testQuestion.getIsEnabled(),
            testClone.getIsEnabled());
        assertEquals("The getting QuestionType was not the expected one",
            testQuestion.getQuestionType(), testClone.getQuestionType());
        assertEquals("The getting Position was not the expected one",
            testQuestion.getQuestionnaire().getQuestions().size(), (long) testClone.getPosition());
        assertEquals("The getting Questionnaire was not the expected one",
            testQuestion.getQuestionnaire(), testClone.getQuestionnaire());
        assertEquals("The getting MinNumberAnswers was not the expected one",
            testQuestion.getMinNumberAnswers(), testClone.getMinNumberAnswers());
        assertEquals("The getting MaxNumberAnswers was not the expected one",
            testQuestion.getMaxNumberAnswers(), testClone.getMaxNumberAnswers());
        assertEquals("The getting number of Answers was not the expected one", countAnswers,
            testClone.getAnswers().size());
    }

    /**
     * Test of {@link Question#toQuestionDTO}.<br> Valid input: valid {@link Question} with random
     * number of {@link Answer Answers}
     */
    @Test
    public void testToQuestionDTO() {
        Questionnaire testQuestionnaire = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Mockito.when(testQuestionnaire.getId()).thenReturn(Math.abs(random.nextLong()));
        Question spyQuestion = spy(getNewValidQuestion(testQuestionnaire));
        Mockito.when(spyQuestion.getId()).thenReturn(Math.abs(random.nextLong()));

        // Add random Answers
        int countAnswers = random.nextInt(500);
        for (int i = 0; i < countAnswers; i++) {
            Answer testAnswer = spy(AnswerTest.getNewValidRandomAnswer());
            Mockito.when(testAnswer.getId()).thenReturn(Math.abs(random.nextLong()));
            if (testAnswer instanceof SliderAnswer) {
                Map<String, String> localizedTexts = new HashMap<>();
                localizedTexts.put(Helper.getRandomLocale(),
                    Helper.getRandomString(random.nextInt(50) + 1));
                ((SliderAnswer) testAnswer).setLocalizedMinimumText(localizedTexts);
                ((SliderAnswer) testAnswer).setLocalizedMaximumText(localizedTexts);
            } else if (testAnswer instanceof SelectAnswer) {
                if (random.nextBoolean()) {
                    ((SelectAnswer) testAnswer).setValue(random.nextDouble());
                }
            } else if (testAnswer instanceof SliderFreetextAnswer) {
                Map<String, String> localizedTexts = new HashMap<>();
                localizedTexts.put(Helper.getRandomLocale(),
                    Helper.getRandomString(random.nextInt(50) + 1));
                ((SliderFreetextAnswer) testAnswer).setLocalizedMinimumText(localizedTexts);
                ((SliderFreetextAnswer) testAnswer).setLocalizedMaximumText(localizedTexts);
            } else if (testAnswer instanceof DateAnswer) {
                if (random.nextBoolean()) {
                    ((DateAnswer) testAnswer).setStartDate(null);
                }
                if (random.nextBoolean()) {
                    ((DateAnswer) testAnswer).setEndDate(null);
                }
            } else if (testAnswer instanceof NumberInputAnswer) {
                if (random.nextBoolean()) {
                    ((NumberInputAnswer) testAnswer).setStepsize(null);
                }
            } else if (testAnswer instanceof ImageAnswer) {
                ((ImageAnswer) testAnswer).setImagePath("/testimage.png");
            }
            // Randomly add a Response
            if (random.nextBoolean()) {
                testAnswer.addResponse(ResponseTest.getNewValidResponse());
            }
            // Ramdomly add random Conditions
            if (random.nextBoolean()) {
                int countConditions = random.nextInt(50) + 1;
                for (int j = 0; j < countConditions; j++) {
                    Condition testCondition = spy(ConditionTest.getNewValidCondition());
                    Mockito.when(testCondition.getId()).thenReturn(Math.abs(random.nextLong()));
                    testAnswer.addCondition(testCondition);
                }
            }
            // Randomly add ExportRules
            if (random.nextBoolean()) {
                testAnswer.addExportRule(ExportRuleAnswerTest.getNewValidExportRuleAnswer());
            }
            spyQuestion.addAnswer(testAnswer);
        }

        QuestionDTO testQuestionDTO = questionDTOMapper.apply(spyQuestion);

        assertEquals("The getting LocalizedQuestionText was not the expected one",
            spyQuestion.getId(), testQuestionDTO.getId());
        assertEquals("The getting LocalizedQuestionText was not the expected one",
            spyQuestion.getLocalizedQuestionText(), testQuestionDTO.getLocalizedQuestionText());
        assertEquals("The getting isRequired was not the expected one", spyQuestion.getIsRequired(),
            testQuestionDTO.getIsRequired());
        assertEquals("The getting isEnabled was not the expected one", spyQuestion.getIsEnabled(),
            testQuestionDTO.getIsEnabled());
        assertEquals("The getting QuestionType was not the expected one",
            spyQuestion.getQuestionType(), testQuestionDTO.getQuestionType());
        assertEquals("The getting Position was not the expected one", spyQuestion.getPosition(),
            testQuestionDTO.getPosition());
        assertEquals("The getting Questionnaire was not the expected one",
            spyQuestion.getQuestionnaire().getId(), testQuestionDTO.getQuestionnaireId());
        assertEquals("The getting MinNumberAnswers was not the expected one",
            spyQuestion.getMinNumberAnswers(), testQuestionDTO.getMinNumberAnswers());
        assertEquals("The getting MaxNumberAnswers was not the expected one",
            spyQuestion.getMaxNumberAnswers(), testQuestionDTO.getMaxNumberAnswers());
        assertEquals("The getting set of Answers was not the expected one",
            spyQuestion.getAnswers().size(), testQuestionDTO.getAnswers().size());
    }
}
