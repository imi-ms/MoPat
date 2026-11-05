package de.imi.mopat.helper.controller;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireVersionGroupDao;
import de.imi.mopat.helper.model.QuestionnaireGroupDTOMapper;

import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireVersionGroup;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireVersionGroupDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
        MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class QuestionnaireVersionGroupServiceTest {

    @Autowired
    private QuestionnaireVersionGroupDao questionnaireVersionGroupDao;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Mock
    private QuestionnaireGroupDTOMapper questionnaireGroupDTOMapper;

    @Autowired
    @InjectMocks
    private QuestionnaireVersionGroupService questionnaireVersionGroupService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(new SecurityContextImpl(new UsernamePasswordAuthenticationToken("testUser", "password",List.of(new SimpleGrantedAuthority("ROLE_MODERATOR")))));
    }

    @Test
    public void testCreateOrFindQuestionnaireGroup_NewGroup() {
        // Arrange
        String name = "Test Create QuestionnaireGroup";
        Questionnaire validQuestionnaire = new Questionnaire(name, "description", 1L, false);
        questionnaireDao.merge(validQuestionnaire);

        // Act
        QuestionnaireVersionGroup createdGroup = questionnaireVersionGroupService.getOrCreateQuestionnaireGroup(validQuestionnaire);

        // Assert
        assertEquals(name, createdGroup.getName());

        questionnaireVersionGroupDao.remove(createdGroup);
        questionnaireDao.remove(validQuestionnaire);
    }

    @Test
    public void testCreateOrFindQuestionnaireGroup_ExistingGroup() {
        // Arrange
        QuestionnaireVersionGroup existingGroup = new QuestionnaireVersionGroup();
        existingGroup.setName("Existing Group");
        questionnaireVersionGroupDao.merge(existingGroup);


        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setName("Existing Questionnaire");
        questionnaire.setQuestionnaireVersionGroup(existingGroup);

        // Act
        QuestionnaireVersionGroup group = questionnaireVersionGroupService.getOrCreateQuestionnaireGroup(questionnaire);

        // Assert
        assertEquals(existingGroup.getId(), group.getId());

        questionnaireVersionGroupDao.remove(existingGroup);
    }

    @Test
    public void testGetQuestionnaireGroupById() {
        // Arrange
        QuestionnaireVersionGroup group = new QuestionnaireVersionGroup();

        questionnaireVersionGroupDao.merge(group);

        // Act
        Optional<QuestionnaireVersionGroup> result = questionnaireVersionGroupService.getQuestionnaireGroupById(group.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(group.getId(), result.get().getId());

        questionnaireVersionGroupDao.remove(group);
        questionnaireVersionGroupDao.remove(result.get());
    }

    @Test
    public void testFindGroupForQuestionnaire() {
        // Arrange
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(questionnaire);
        QuestionnaireVersionGroup group = questionnaire.getQuestionnaireVersionGroup();

        // Act
        Optional<QuestionnaireVersionGroup> result = questionnaireVersionGroupService.findGroupForQuestionnaire(questionnaire);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(group.getId(), result.get().getId());

        questionnaireDao.remove(questionnaire);
    }
}