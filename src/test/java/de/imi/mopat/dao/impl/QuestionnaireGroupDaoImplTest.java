package de.imi.mopat.dao.impl;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireGroupDao;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireGroup;
import de.imi.mopat.model.QuestionnaireTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
        MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class QuestionnaireGroupDaoImplTest {

    @Autowired
    QuestionnaireGroupDao questionnaireGroupDao;

    @Autowired
    QuestionnaireDao questionnaireDao;

    @Before
    public void setUp() {
        clearTable();
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    @Transactional("MoPat")
    public void testMerge() {
        // Arrange
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(questionnaire);  // Ensure the questionnaire is persisted

        // Check if the questionnaire is properly stored
        List<Questionnaire> allElements = questionnaireDao.getAllElements();
        assertEquals("Expected one questionnaire", 1, allElements.size());

        QuestionnaireGroup questionnaireGroup = new QuestionnaireGroup();

        questionnaireGroup.setName(questionnaire.getName());
        questionnaireGroup.addQuestionnaire(questionnaire);

        // Act
        questionnaireGroupDao.merge(questionnaireGroup);
        List<QuestionnaireGroup> groups = questionnaireGroupDao.getAllElements();

        // Assert
        assertNotNull("QuestionnaireGroup list should not be null", groups);
        assertEquals("QuestionnaireGroup list size should be 1", 1, groups.size());
        assertEquals("Stored QuestionnaireGroup should match the original", questionnaireGroup, groups.get(0));
    }

    public void clearTable() {
        List<QuestionnaireGroup> allGroups = questionnaireGroupDao.getAllElements();
        for (QuestionnaireGroup group : allGroups) {
            questionnaireGroupDao.remove(group);
        }
        List<Questionnaire> allQuestionnaires = questionnaireDao.getAllElements();
        for (Questionnaire questionnaire : allQuestionnaires) {
            questionnaireDao.remove(questionnaire);
        }
    }
}
