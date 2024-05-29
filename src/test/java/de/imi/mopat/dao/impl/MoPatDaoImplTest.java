package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.BundleQuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.BundleQuestionnaire;
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
public class MoPatDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    QuestionnaireDao questionnaireDao;
    @Autowired
    BundleQuestionnaireDao bundleQuestionnaireDao;
    @Autowired
    UserDao userDao;

    /**
     * Test of {@link MoPatDaoImpl#getEntityClass}.<br> Valid input: {@link QuestionnaireDao}
     */
    @Test
    public void testGetEntityClass() {
        assertEquals("The getting entity class was not the expected one", Questionnaire.class,
            questionnaireDao.getEntityClass());
    }

    /**
     * Test of {@link MoPatDaoImpl#getElementById}.<br> Valid input: existing Id of a
     * {@link Questionnaire}
     */
    @Test
    public void testGetElementById() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);
        Long testId = testQuestionnaire.getId();
        assertNull(
            "The getting element was not null although the id was bigger than the id of the last created element",
            questionnaireDao.getElementById(testId + 1));
        assertEquals("The getting element was not the expected one", testQuestionnaire,
            questionnaireDao.getElementById(testId));
    }

    /**
     * Test of {@link MoPatDaoImpl#getElementByUUID}.<br> Valid input: existing UUID of a
     * {@link Questionnaire}
     */
    @Test
    public void testGetElementByUUID() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);
        String testUUID = testQuestionnaire.getUUID();
        assertNull("The getting element was not null although the given String was no UUID",
            questionnaireDao.getElementByUUID(Helper.getRandomAlphabeticString(16)));
        assertEquals("The getting element was not the expected one", testQuestionnaire,
            questionnaireDao.getElementByUUID(testUUID));
    }

    /**
     * Test of {@link MoPatDaoImpl#getElementsById}.<br> Valid input: list of ids of existing
     * {@link Questionnaire Questionnaires}
     */
    @Test
    public void testGetElementsById() {
        List<Questionnaire> testList = new ArrayList<>();
        List<Long> testIds = new ArrayList<>();
        assertTrue(
            "The getting list of elements was not empty although the given list of ids was empty",
            questionnaireDao.getElementsById(testIds).isEmpty());
        int countQuestionnaires = random.nextInt(25) + 1;
        for (int i = 0; i < countQuestionnaires; i++) {
            Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
            questionnaireDao.merge(testQuestionnaire);
            testIds.add(testQuestionnaire.getId());
            testList.add(testQuestionnaire);
        }
        assertEquals("The getting list of elements was not the expected one", testList,
            questionnaireDao.getElementsById(testIds));
    }

    /**
     * Test of {@link MoPatDaoImpl#getAllElements} and {@link MoPatDaoImpl#getCount}.
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetAllElementsAndGetCount() {
        clearQuestionnaireTable();

        List<Questionnaire> testList = new ArrayList<>();
        int countQuestionnaires = random.nextInt(25) + 1;
        for (int i = 0; i < countQuestionnaires; i++) {
            Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
            questionnaireDao.merge(testQuestionnaire);
            testList.add(testQuestionnaire);
        }
        assertEquals("The getting list of elements was not the expected one", testList,
            questionnaireDao.getAllElements());
        assertEquals("The getting count of elements was not the expected one", countQuestionnaires,
            (long) questionnaireDao.getCount());
    }

    private void clearQuestionnaireTable() {
        List<BundleQuestionnaire> allBundleQuestionnaires = bundleQuestionnaireDao.getAllElements();
        for (BundleQuestionnaire bundleQuestionnaire : allBundleQuestionnaires) {
            bundleQuestionnaireDao.remove(bundleQuestionnaire);
        }
        List<Questionnaire> allQuestionnaires = questionnaireDao.getAllElements();
        for (Questionnaire questionnaire : allQuestionnaires) {
            questionnaireDao.remove(questionnaire);
        }
    }
}
