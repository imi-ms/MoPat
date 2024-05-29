package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.BundleQuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.BundleTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
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
public class BundleQuestionnaireDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    BundleQuestionnaireDao testBundleQuestionnaireDao;
    @Autowired
    BundleDao bundleDao;
    @Autowired
    QuestionnaireDao questionnaireDao;

    /**
     * Test of {@link BundleQuestionnaireDaoImpl#getBundleQuestionnaire}.<br> Valid input: not
     * merged {@link Bundle} and {@link Questionnaire}, merged {@link Bundle} and
     * {@link Questionnaire}
     */
    @Test
    public void testGetBundleQuestionnaire() {
        Bundle testBundle = BundleTest.getNewValidBundle();
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        assertNull(
            "The method returned not null although the new Bundle and Questionnaire were not merged before",
            testBundleQuestionnaireDao.getBundleQuestionnaire(testBundle, testQuestionnaire));
        bundleDao.merge(testBundle);
        questionnaireDao.merge(testQuestionnaire);
        BundleQuestionnaire testBundleQuestionnaire = new BundleQuestionnaire(testBundle,
            testQuestionnaire, Math.abs(new Random().nextInt()) + 1, true, false);
        bundleDao.merge(testBundle);
        assertEquals("The getting BundleQuestionnaire was not the expected one",
            testBundleQuestionnaire,
            testBundleQuestionnaireDao.getBundleQuestionnaire(testBundle, testQuestionnaire));
    }
}
