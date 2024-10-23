package de.imi.mopat.dao.impl;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireVersionGroupDao;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireVersionGroup;
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
public class QuestionnaireVersionGroupDaoImplTest {

    @Autowired
    QuestionnaireVersionGroupDao questionnaireVersionGroupDao;

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

        QuestionnaireVersionGroup questionnaireVersionGroup = new QuestionnaireVersionGroup();

        questionnaireVersionGroup.setName(questionnaire.getName());
        questionnaireVersionGroup.addQuestionnaire(questionnaire);

        // Act
        questionnaireVersionGroupDao.merge(questionnaireVersionGroup);
        List<QuestionnaireVersionGroup> groups = questionnaireVersionGroupDao.getAllElements();

        // Assert
        assertNotNull("QuestionnaireVersionGroup list should not be null", groups);
        assertEquals("QuestionnaireVersionGroup list size should be 1", 1, groups.size());
        assertEquals("Stored QuestionnaireVersionGroup should match the original", questionnaireVersionGroup, groups.get(0));
    }

    public void clearTable() {
        List<QuestionnaireVersionGroup> allGroups = questionnaireVersionGroupDao.getAllElements();
        for (QuestionnaireVersionGroup group : allGroups) {
            questionnaireVersionGroupDao.remove(group);
        }
        List<Questionnaire> allQuestionnaires = questionnaireDao.getAllElements();
        for (Questionnaire questionnaire : allQuestionnaires) {
            questionnaireDao.remove(questionnaire);
        }
    }
}
