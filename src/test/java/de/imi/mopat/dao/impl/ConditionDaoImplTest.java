package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ConditionDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleTest;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SelectAnswerTest;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionActionType;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
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
public class ConditionDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    ConditionDao testConditionDao;
    @Autowired
    BundleDao bundleDao;
    @Autowired
    QuestionnaireDao questionnaireDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    AnswerDao answerDao;

    /**
     * Test of {@link ConditionDaoImpl#isConditionTarget}.<br> Valid input: {@link Question},
     * {@link Questionniare}, {@link Bundle} and {@link Answer} with and without
     * {@link Condition Conditions} pointing on them.
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testIsConditionTarget() {
        Bundle testBundleTarget = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundleTarget);
        Questionnaire testQuestionnaireTarget = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaireTarget);
        Questionnaire otherQuestionnaireTarget = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(otherQuestionnaireTarget);
        Question testQuestionTarget = QuestionTest.getNewValidQuestion(testQuestionnaireTarget);
        SelectAnswer testSelectAnswerTarget = SelectAnswerTest.getNewValidSelectAnswer();
        testSelectAnswerTarget.setQuestion(testQuestionTarget);
        questionDao.merge(testQuestionTarget);
        assertFalse("The bundle was a condition target although there was no condition",
            testConditionDao.isConditionTarget(testBundleTarget));
        assertFalse("The questionnaire was a condition target although there was no condition",
            testConditionDao.isConditionTarget(testQuestionnaireTarget));
        assertFalse("The question was a condition target although there was no condition",
            testConditionDao.isConditionTarget(testQuestionTarget));
        assertFalse("The answer was a condition target although there was no condition",
            testConditionDao.isConditionTarget(testSelectAnswerTarget));
        assertFalse(
            "An object was a condition target although there was no condition and it was not instance of Bundle, Questionnaire, Question or Answer",
            testConditionDao.isConditionTarget(new Object()));

        Question testQuestionTrigger = QuestionTest.getNewValidQuestion(testQuestionnaireTarget);
        SelectAnswer testSelectAnswerTrigger = SelectAnswerTest.getNewValidSelectAnswer();
        testSelectAnswerTrigger.setQuestion(testQuestionTrigger);
        questionDao.merge(testQuestionTrigger);

        SelectAnswerCondition testCondition = new SelectAnswerCondition(testSelectAnswerTrigger,
            testQuestionTarget, ConditionActionType.ENABLE, testBundleTarget);
        answerDao.merge(testSelectAnswerTrigger);
        assertTrue(
            "The bundle was not a condition target although there was a condition connected to it",
            testConditionDao.isConditionTarget(testBundleTarget));
        assertTrue(
            "The question was not a condition target although there was a condition connected to it",
            testConditionDao.isConditionTarget(testQuestionTarget));

        SelectAnswerCondition otherCondition = new SelectAnswerCondition(testSelectAnswerTrigger,
            otherQuestionnaireTarget, ConditionActionType.ENABLE, testBundleTarget);
        answerDao.merge(testSelectAnswerTrigger);
        testCondition = new SelectAnswerCondition(testSelectAnswerTrigger, testQuestionnaireTarget,
            ConditionActionType.ENABLE, testBundleTarget);
        answerDao.merge(testSelectAnswerTrigger);
        assertTrue(
            "The questionnaire was not a condition target although there was a condition connected to it",
            testConditionDao.isConditionTarget(testQuestionnaireTarget));
        testCondition = new SelectAnswerCondition(testSelectAnswerTrigger, testSelectAnswerTarget,
            ConditionActionType.ENABLE, testBundleTarget);
        answerDao.merge(testSelectAnswerTrigger);
        assertTrue(
            "The answer was not a condition target although there was a condition connected to it",
            testConditionDao.isConditionTarget(testSelectAnswerTarget));

        questionDao.remove(testQuestionTrigger);
        questionDao.remove(testQuestionTarget);
        otherQuestionnaireTarget.removeAllQuestions();
        questionnaireDao.remove(otherQuestionnaireTarget);
        testQuestionnaireTarget.removeAllQuestions();
        questionnaireDao.remove(testQuestionnaireTarget);
        bundleDao.remove(testBundleTarget);
    }

    /**
     * Test of {@link ConditionDaoImpl#getConditionsByTarget}.<br> Valid input: {@link Question},
     * {@link Questionniare}, {@link Bundle} and {@link Answer} with and without
     * {@link Condition Conditions} pointing on them.
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetConditionsByTarget() {
        Bundle testBundleTarget = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundleTarget);
        Questionnaire testQuestionnaireTarget = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaireTarget);
        Questionnaire otherQuestionnaireTarget = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(otherQuestionnaireTarget);
        Question testQuestionTarget = QuestionTest.getNewValidQuestion(testQuestionnaireTarget);
        SelectAnswer testSelectAnswerTarget = SelectAnswerTest.getNewValidSelectAnswer();
        testSelectAnswerTarget.setQuestion(testQuestionTarget);
        questionDao.merge(testQuestionTarget);

        List<Condition> testResultConditionList = new ArrayList<>();

        assertEquals("A bundle had conditions pointing on it although there was no condition",
            testResultConditionList, testConditionDao.getConditionsByTarget(testBundleTarget));
        assertEquals(
            "A questionnaire had conditions pointing on it although there was no condition",
            testResultConditionList,
            testConditionDao.getConditionsByTarget(testQuestionnaireTarget));
        assertEquals("A question had conditions pointing on it although there was no condition",
            testResultConditionList, testConditionDao.getConditionsByTarget(testQuestionTarget));
        assertEquals("An answer had conditions pointing on it although there was no condition",
            testResultConditionList,
            testConditionDao.getConditionsByTarget(testSelectAnswerTarget));
        assertEquals(
            "An object had conditions pointing on it although there was no condition and it was not instance of Bundle, Questionnaire, Question or Answer",
            testResultConditionList, testConditionDao.getConditionsByTarget(new Object()));

        Question testQuestionTrigger = QuestionTest.getNewValidQuestion(testQuestionnaireTarget);
        SelectAnswer testSelectAnswerTrigger = SelectAnswerTest.getNewValidSelectAnswer();
        testSelectAnswerTrigger.setQuestion(testQuestionTrigger);
        questionDao.merge(testQuestionTrigger);

        int count = random.nextInt(25) + 1;
        for (int i = 0; i < count; i++) {
            SelectAnswerCondition testCondition = new SelectAnswerCondition(testSelectAnswerTrigger,
                testQuestionTarget, ConditionActionType.ENABLE, testBundleTarget);
            answerDao.merge(testSelectAnswerTrigger);
            testResultConditionList.add(testCondition);
        }
        assertTrue("The getting list of Conditions was not the expected one",
            testResultConditionList.containsAll(
                testConditionDao.getConditionsByTarget(testQuestionTarget))
                && testConditionDao.getConditionsByTarget(testQuestionTarget)
                .containsAll(testResultConditionList));
        assertTrue("The getting list of Conditions was not the expected one",
            testResultConditionList.containsAll(
                testConditionDao.getConditionsByTarget(testBundleTarget))
                && testConditionDao.getConditionsByTarget(testBundleTarget)
                .containsAll(testResultConditionList));

        testResultConditionList = new ArrayList<>();
        count = random.nextInt(25) + 1;
        for (int i = 0; i < count; i++) {
            SelectAnswerCondition testCondition = new SelectAnswerCondition(testSelectAnswerTrigger,
                testQuestionnaireTarget, ConditionActionType.ENABLE, testBundleTarget);
            answerDao.merge(testSelectAnswerTrigger);
            testResultConditionList.add(testCondition);
        }
        SelectAnswerCondition otherCondition = new SelectAnswerCondition(testSelectAnswerTrigger,
            otherQuestionnaireTarget, ConditionActionType.ENABLE, testBundleTarget);
        answerDao.merge(testSelectAnswerTrigger);
        assertTrue("The getting list of Conditions was not the expected one",
            testResultConditionList.containsAll(
                testConditionDao.getConditionsByTarget(testQuestionnaireTarget))
                && testConditionDao.getConditionsByTarget(testQuestionnaireTarget)
                .containsAll(testResultConditionList));

        testResultConditionList = new ArrayList<>();
        count = random.nextInt(25) + 1;
        for (int i = 0; i < count; i++) {
            SelectAnswerCondition testCondition = new SelectAnswerCondition(testSelectAnswerTrigger,
                testSelectAnswerTarget, ConditionActionType.ENABLE, testBundleTarget);
            answerDao.merge(testSelectAnswerTrigger);
            testResultConditionList.add(testCondition);
        }
        assertTrue("The getting list of Conditions was not the expected one",
            testResultConditionList.containsAll(
                testConditionDao.getConditionsByTarget(testSelectAnswerTarget))
                && testConditionDao.getConditionsByTarget(testSelectAnswerTarget)
                .containsAll(testResultConditionList));

        questionDao.remove(testQuestionTrigger);
        questionDao.remove(testQuestionTarget);
        otherQuestionnaireTarget.removeAllQuestions();
        questionnaireDao.remove(otherQuestionnaireTarget);
        testQuestionnaireTarget.removeAllQuestions();
        questionnaireDao.remove(testQuestionnaireTarget);
        bundleDao.remove(testBundleTarget);
    }
}
