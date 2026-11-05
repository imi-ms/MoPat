package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.BundleQuestionnaireDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.BundleTest;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.Response;
import de.imi.mopat.model.Review;
import de.imi.mopat.model.ReviewMessage;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SelectAnswerTest;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.UnaryExpression;
import de.imi.mopat.utils.Helper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
@Transactional("MoPat")
public class QuestionnaireDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    QuestionnaireDao testQuestionnaireDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    BundleQuestionnaireDao bundleQuestionnaireDao;
    @PersistenceContext(unitName = "MoPat")
    EntityManager entityManager;

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testClearTableWithReferences() {
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Question question = QuestionTest.getNewValidQuestion(questionnaire);
        SelectAnswer answer = SelectAnswerTest.getNewValidSelectAnswer();
        question.addAnswer(answer);
        questionnaire.addQuestion(question);
        entityManager.persist(questionnaire);

        Bundle bundle = BundleTest.getNewValidBundle();
        entityManager.persist(bundle);
        Encounter encounter = new Encounter(bundle, "cleanup-test");
        entityManager.persist(encounter);
        entityManager.persist(new Response(answer, encounter));

        Review review = new Review(questionnaire, 1L, 2L);
        review.getConversation().add(new ReviewMessage(review, 1L, 2L, "test"));
        entityManager.persist(review);

        UnaryExpression expression = new UnaryExpression();
        expression.setQuestion(question);
        Score score = new Score();
        score.setQuestionnaire(questionnaire);
        score.setExpression(expression);
        entityManager.persist(score);
        entityManager.flush();
        entityManager.clear();

        clearTable();
        entityManager.flush();
        assertTrue(testQuestionnaireDao.getAllElements().isEmpty());
    }

    /**
     * Test of {@link QuestionnaireDaoImpl#remove}.<br> Valid input: random number of
     * {@link Questionnaire Questionnaires} randomly with {@link Question Questions}, where a part
     * will be removed
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testRemove() {
        clearTable();
        List<Questionnaire> testQuestionnaires = new ArrayList<>();
        List<Questionnaire> removeQuestionnaires = new ArrayList<>();
        int countQuestionnaires = random.nextInt(50) + 1;
        for (int i = 0; i < countQuestionnaires; i++) {
            Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
            testQuestionnaireDao.merge(testQuestionnaire);
            if (random.nextBoolean()) {
                int countQuestions = random.nextInt(25) + 1;
                for (int j = 0; j < countQuestions; j++) {
                    Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
                    testQuestionnaire.addQuestion(testQuestion);
                    questionDao.merge(testQuestion);
                }
            }
            testQuestionnaireDao.merge(testQuestionnaire);
            testQuestionnaires.add(testQuestionnaire);
            if (random.nextBoolean()) {
                removeQuestionnaires.add(testQuestionnaire);
            }
        }

        assertEquals("The getting list of Questionnaires was not the expected one",
            testQuestionnaires, testQuestionnaireDao.getAllElements());

        for (Questionnaire removeQuestionnaire : removeQuestionnaires) {
            testQuestionnaireDao.remove(removeQuestionnaire);
        }

        testQuestionnaires.removeAll(removeQuestionnaires);
        assertEquals(
            "The getting list of Questionnaires was not the expected one after removing some items",
            testQuestionnaires, testQuestionnaireDao.getAllElements());
    }

    /**
     * Test of {@link QuestionnaireDaoImpl#isQuestionnaireNameUnique}.<br> Valid input: random name
     * and random Id, existing name and null, existing name and random Id, existing name and
     * associated Id
     */
    @Test
    public void testIsQuestionnaireNameUnused() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        String testName = Helper.getRandomAlphabeticString(random.nextInt(253) + 3);

        assertTrue("A random new name was already used",
            testQuestionnaireDao.isQuestionnaireNameUnique(testName, random.nextLong()));

        testQuestionnaire.setName(testName);
        testQuestionnaireDao.merge(testQuestionnaire);

        assertFalse("The method returned true although the given Id was null",
            testQuestionnaireDao.isQuestionnaireNameUnique(testName, null));
        assertFalse("The method returned true although the given Id was random",
            testQuestionnaireDao.isQuestionnaireNameUnique(testName, random.nextLong()));
        assertTrue("The name was already used in another Bundle",
            testQuestionnaireDao.isQuestionnaireNameUnique(testName, testQuestionnaire.getId()));
    }

    /**
     * Deletes all {@link BundleQuestionnaire BundleQuestionnaires} and
     * {@link Questionnaire Questionnaires} from the database.
     */
    private void clearTable() {
        DaoTestCleanup.clearQuestionReferences(entityManager);
        DaoTestCleanup.clearReviews(entityManager);
        List<BundleQuestionnaire> allBundleQuestionnaires = bundleQuestionnaireDao.getAllElements();
        for (BundleQuestionnaire bundleQuestionnaire : allBundleQuestionnaires) {
            bundleQuestionnaireDao.remove(bundleQuestionnaire);
        }
        List<Questionnaire> allQuestionnaires = testQuestionnaireDao.getAllElements();
        for (Questionnaire questionnaire : allQuestionnaires) {
            testQuestionnaireDao.remove(questionnaire);
        }
    }
}
