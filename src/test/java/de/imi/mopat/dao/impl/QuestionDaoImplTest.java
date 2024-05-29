package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.SelectAnswerTest;
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
public class QuestionDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    QuestionDao testQuestionDao;
    @Autowired
    QuestionnaireDao questionnaireDao;

    /**
     * Test of {@link QuestionDaoImpl#remove}.<br> Valid input: random number of
     * {@link Question Questions} randomly with {@link Answer Answers}, where a part will be
     * removed
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testRemove() {
        clearTable();
        List<Question> testQuestions = new ArrayList<>();
        List<Question> removeQuestions = new ArrayList<>();
        int countQuestions = random.nextInt(50) + 1;
        for (int i = 0; i < countQuestions; i++) {
            Question testQuestion = QuestionTest.getNewValidQuestion();
            if (random.nextBoolean()) {
                int countAnswers = random.nextInt(25) + 1;
                for (int j = 0; j < countAnswers; j++) {
                    Answer testAnswer = SelectAnswerTest.getNewValidSelectAnswer();
                    testQuestion.addAnswer(testAnswer);
                }
            }
            questionnaireDao.merge(testQuestion.getQuestionnaire());
            testQuestionDao.merge(testQuestion);
            testQuestions.add(testQuestion);
            if (random.nextBoolean()) {
                removeQuestions.add(testQuestion);
            }
        }

        assertEquals("The getting list of Questions was not the expected one", testQuestions,
            testQuestionDao.getAllElements());

        for (Question removeQuestion : removeQuestions) {
            testQuestionDao.remove(removeQuestion);
        }

        testQuestions.removeAll(removeQuestions);
        assertEquals(
            "The getting list of Questions was not the expected one after removing some items",
            testQuestions, testQuestionDao.getAllElements());
    }

    /**
     * Deletes all {@link Question Questions} from the database.
     */
    private void clearTable() {
        List<Question> allQuestions = testQuestionDao.getAllElements();
        for (Question question : allQuestions) {
            testQuestionDao.remove(question);
        }
    }
}
