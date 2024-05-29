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
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.utils.Helper;
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
public class QuestionnaireDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    QuestionnaireDao testQuestionnaireDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    BundleQuestionnaireDao bundleQuestionnaireDao;

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
     * Test of {@link QuestionnaireDaoImpl#isQuestionnaireNameUnused}.<br> Valid input: random name
     * and random Id, existing name and null, existing name and random Id, existing name and
     * associated Id
     */
    @Test
    public void testIsQuestionnaireNameUnused() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        String testName = Helper.getRandomAlphabeticString(random.nextInt(253) + 3);

        assertTrue("A random new name was already used",
            testQuestionnaireDao.isQuestionnaireNameUnused(testName, random.nextLong()));

        testQuestionnaire.setName(testName);
        testQuestionnaireDao.merge(testQuestionnaire);

        assertFalse("The method returned true although the given Id was null",
            testQuestionnaireDao.isQuestionnaireNameUnused(testName, null));
        assertFalse("The method returned true although the given Id was random",
            testQuestionnaireDao.isQuestionnaireNameUnused(testName, random.nextLong()));
        assertTrue("The name was already used in another Bundle",
            testQuestionnaireDao.isQuestionnaireNameUnused(testName, testQuestionnaire.getId()));
    }

    /**
     * Deletes all {@link BundleQuestionnaire BundleQuestionnaires} and
     * {@link Questionnaire Questionnaires} from the database.
     */
    private void clearTable() {
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
