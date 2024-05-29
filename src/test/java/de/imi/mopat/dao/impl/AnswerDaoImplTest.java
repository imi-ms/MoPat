package de.imi.mopat.dao.impl;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ConditionDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.DateAnswerTest;
import de.imi.mopat.model.FreetextAnswer;
import de.imi.mopat.model.FreetextAnswerTest;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.ImageAnswerTest;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.NumberInputAnswerTest;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SelectAnswerTest;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderAnswerTest;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.SliderFreetextAnswerTest;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SelectAnswerConditionTest;
import java.util.Date;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class AnswerDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    AnswerDao testAnswerDao;
    @Autowired
    ConditionDao conditionDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    BundleDao bundleDao;
    @Autowired
    QuestionnaireDao questionnaireDao;

    /**
     * Test of {@link AnswerDaoImpl#getAnswerWhichIsTheOriginForCondition}.<br> Invalid input:
     * <code>null</code>, conditionId less or equal 0L, not valid conditionId <br> Valid input:
     * random valid conditionId as Long
     */
    @Test
    public void testGetAnswerWhichIsTheOriginForCondition() {
        // Test asserts
        Long testConditionId = null;
        Throwable e = null;
        try {
            testAnswerDao.getAnswerWhichIsTheOriginForCondition(testConditionId);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to get an Answer for ConditionId null",
            e instanceof AssertionError);

        testConditionId = Math.abs(random.nextLong()) * (-1);
        e = null;
        try {
            testAnswerDao.getAnswerWhichIsTheOriginForCondition(testConditionId);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to get an Answer for ConditionId less or equal than 0",
            e instanceof AssertionError);

        //Not valid conditionId
        testConditionId = Math.abs(random.nextLong()) + 1;
        assertNull("It was possible to get an Answer for a not valid ConditionId",
            testAnswerDao.getAnswerWhichIsTheOriginForCondition(testConditionId));

        // Test with SelectAnswer
        SelectAnswerCondition testSelectAnswerCondition = SelectAnswerConditionTest.getNewValidSelectAnswerCondition();
        SelectAnswer testSelectAnswer = (SelectAnswer) testSelectAnswerCondition.getTrigger();
        Question testConditionQuestion = (Question) testSelectAnswerCondition.getTarget();
        Question testAnswerQuestion = testSelectAnswer.getQuestion();
        Questionnaire testConditionQuestionQuestionnaire = testConditionQuestion.getQuestionnaire();
        Questionnaire testAnswerQuestionQuestionnaire = testAnswerQuestion.getQuestionnaire();
        Bundle testConditionBundle = testSelectAnswerCondition.getBundle();
        bundleDao.merge(testConditionBundle);
        questionnaireDao.merge(testConditionQuestionQuestionnaire);
        questionnaireDao.merge(testAnswerQuestionQuestionnaire);
        questionDao.merge(testConditionQuestion);
        questionDao.merge(testAnswerQuestion);
        testAnswerDao.merge(testSelectAnswer);
        conditionDao.merge(testSelectAnswerCondition);

        assertEquals("The getting Answer was not the expected one", testSelectAnswer,
            testAnswerDao.getAnswerWhichIsTheOriginForCondition(testSelectAnswerCondition.getId()));
    }

    /**
     * Test of {@link AnswerDaoImpl#getElementById}.<br> Valid input: random valid
     * {@link DateAnswer}
     */
    @Test
    public void testGetDateAnswer() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        DateAnswer testAnswer = DateAnswerTest.getNewValidDateAnswer(
            new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
        testAnswer.setQuestion(testQuestion);
        questionnaireDao.merge(testQuestionnaire);
        questionDao.merge(testQuestion);
        testAnswerDao.merge(testAnswer);
        assertEquals("The getting DateAnswer was not the expected one", testAnswer,
            testAnswerDao.getElementById(testAnswer.getId()));
        testAnswerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }

    /**
     * Test of {@link AnswerDaoImpl#getElementById}.<br> Valid input: random valid
     * {@link FreetextAnswer}
     */
    @Test
    public void testGetFreetextAnswer() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        FreetextAnswer testAnswer = FreetextAnswerTest.getNewValidFreetextAnswer();
        testAnswer.setQuestion(testQuestion);
        questionnaireDao.merge(testQuestionnaire);
        questionDao.merge(testQuestion);
        testAnswerDao.merge(testAnswer);
        assertEquals("The getting FreetextAnswer was not the expected one", testAnswer,
            testAnswerDao.getElementById(testAnswer.getId()));
        testAnswerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }

    /**
     * Test of {@link AnswerDaoImpl#getElementById}.<br> Valid input: random valid
     * {@link ImageAnswer}
     */
    @Test
    public void testGetImageAnswer() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        ImageAnswer testAnswer = ImageAnswerTest.getNewValidImageAnswer();
        testAnswer.setQuestion(testQuestion);
        questionnaireDao.merge(testQuestionnaire);
        questionDao.merge(testQuestion);
        testAnswerDao.merge(testAnswer);
        assertEquals("The getting ImageAnswer was not the expected one", testAnswer,
            testAnswerDao.getElementById(testAnswer.getId()));
        testAnswerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }

    /**
     * Test of {@link AnswerDaoImpl#getElementById}.<br> Valid input: random valid
     * {@link NumberInputAnswer}
     */
    @Test
    public void testGetNumberInputAnswer() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        NumberInputAnswer testAnswer = NumberInputAnswerTest.getNewValidNumberInputAnswer();
        testAnswer.setQuestion(testQuestion);
        questionnaireDao.merge(testQuestionnaire);
        questionDao.merge(testQuestion);
        testAnswerDao.merge(testAnswer);
        assertEquals("The getting NumberInputAnswer was not the expected one", testAnswer,
            testAnswerDao.getElementById(testAnswer.getId()));
        testAnswerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }

    /**
     * Test of {@link AnswerDaoImpl#getElementById}.<br> Valid input: random valid
     * {@link SelectAnswer}
     */
    @Test
    public void testGetSelectAnswer() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        SelectAnswer testAnswer = SelectAnswerTest.getNewValidSelectAnswer();
        testAnswer.setQuestion(testQuestion);
        questionnaireDao.merge(testQuestionnaire);
        questionDao.merge(testQuestion);
        testAnswerDao.merge(testAnswer);
        assertEquals("The getting SelectAnswer was not the expected one", testAnswer,
            testAnswerDao.getElementById(testAnswer.getId()));
        testAnswerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }

    /**
     * Test of {@link AnswerDaoImpl#getElementById}.<br> Valid input: random valid
     * {@link SliderAnswer}
     */
    @Test
    public void testGetSliderAnswer() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        SliderAnswer testAnswer = SliderAnswerTest.getNewValidSliderAnswer();
        testAnswer.setQuestion(testQuestion);
        questionnaireDao.merge(testQuestionnaire);
        questionDao.merge(testQuestion);
        testAnswerDao.merge(testAnswer);
        assertEquals("The getting SliderAnswer was not the expected one", testAnswer,
            testAnswerDao.getElementById(testAnswer.getId()));
        testAnswerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }

    /**
     * Test of {@link AnswerDaoImpl#getElementById}.<br> Valid input: random valid
     * {@link SliderFreetextAnswer}
     */
    @Test
    public void testGetSliderFreetextAnswer() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        SliderFreetextAnswer testAnswer = SliderFreetextAnswerTest.getNewValidSliderFreetextAnswer();
        testAnswer.setQuestion(testQuestion);
        questionnaireDao.merge(testQuestionnaire);
        questionDao.merge(testQuestion);
        testAnswerDao.merge(testAnswer);
        assertEquals("The getting SliderFreetextAnswer was not the expected one", testAnswer,
            testAnswerDao.getElementById(testAnswer.getId()));
        testAnswerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }
}
