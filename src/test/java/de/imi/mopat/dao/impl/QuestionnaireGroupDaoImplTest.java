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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
        MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class QuestionnaireGroupDaoImplTest {

    private static final Random random = new Random();

    @Autowired
    QuestionnaireGroupDao questionnaireGroupDao;

    @Autowired
    QuestionnaireDao questionnaireDao;

    @PersistenceContext(unitName = "MoPat")
    protected EntityManager moPatEntityManager;

    @Before
    public void setUp() {
        clearTable();
    }

    /**
     * Test of {@link QuestionnaireGroupDaoImpl#getNextGroupId}.<br>
     * Valid input: Verify that the next group ID is generated correctly.
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetNextGroupId() {
        // Act
        Long firstGroupId = questionnaireGroupDao.getNextGroupId();
        Long secondGroupId = questionnaireGroupDao.getNextGroupId();

        // Assert
        assertNotNull("First group ID should not be null", firstGroupId);
        assertNotNull("Second group ID should not be null", secondGroupId);
        assertEquals("Second group ID should be one greater than the first", firstGroupId + 1, secondGroupId.longValue());
    }

    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testMerge() {
        // Arrange
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);  // Ensure the questionnaire is persisted

        Long groupId = questionnaireGroupDao.getNextGroupId();
        QuestionnaireGroup questionnaireGroup = new QuestionnaireGroup();
        questionnaireGroup.setGroupId(groupId);
        questionnaireGroup.setQuestionnaire(testQuestionnaire);

        // Act
        questionnaireGroupDao.merge(questionnaireGroup);
        List<QuestionnaireGroup> groups = questionnaireGroupDao.getAllElements();

        groups.forEach(System.out::println);
        // Assert
        assertNotNull("QuestionnaireGroup list should not be null", groups);
        assertEquals("QuestionnaireGroup list size should be 1", 1, groups.size());
        assertEquals("Stored QuestionnaireGroup should match the original", questionnaireGroup, groups.get(0));
    }


    @Transactional
    public void clearTable() {
        List<QuestionnaireGroup> allGroups = questionnaireGroupDao.getAllElements();
        for (QuestionnaireGroup group : allGroups) {
            questionnaireGroupDao.remove(group);
        }
    }
}

