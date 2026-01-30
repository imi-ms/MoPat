package de.imi.mopat.dao.impl;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireVersionGroupDao;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.QuestionnaireVersionGroup;
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

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class, MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class QuestionnaireVersionGroupDaoImplTest {

    @Autowired
    QuestionnaireVersionGroupDao questionnaireVersionGroupDao;

    @Autowired
    QuestionnaireDao questionnaireDao;

    @Before
    public void setUp() {
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    @Transactional("MoPat")
    public void testMerge() {
        List<QuestionnaireVersionGroup> groupsBefore = questionnaireVersionGroupDao.getAllElements();
        List<Questionnaire> questionnairesBefore = questionnaireDao.getAllElements();

        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(questionnaire);

        List<Questionnaire> questionnairesAfter = questionnaireDao.getAllElements();
        assertTrue("Questionnaires should be larger than before.", questionnairesBefore.size() < questionnairesAfter.size());

        QuestionnaireVersionGroup questionnaireVersionGroup = new QuestionnaireVersionGroup();

        questionnaireVersionGroup.setName(questionnaire.getName());
        questionnaireVersionGroup.addQuestionnaire(questionnaire);

        // Act
        questionnaireVersionGroupDao.merge(questionnaireVersionGroup);
        List<QuestionnaireVersionGroup> groupsAfter = questionnaireVersionGroupDao.getAllElements();

        // Assert
        assertNotNull("QuestionnaireVersionGroup list should not be null", groupsAfter);
        //2, as the questionnaire stores its own version group
        assertTrue("QuestionnaireVersionGroup list size should be larger than before", groupsBefore.size() < groupsAfter.size());

        // Filter by id and assert the equality
        long versionGroupId = questionnaire.getQuestionnaireVersionGroup().getId();
        QuestionnaireVersionGroup fetchedVersionGroup = groupsAfter.stream().filter(group -> group.getId() == versionGroupId).findFirst().orElse(null);

        assertEquals("Stored QuestionnaireVersionGroup should match the original", questionnaire.getQuestionnaireVersionGroup(), fetchedVersionGroup);
    }


}
