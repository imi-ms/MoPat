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
    public void testCreateQuestionnaireGroup() {
        // Act
        QuestionnaireVersionGroup createdGroup = questionnaireVersionGroupService.createQuestionnaireGroup("Test Group");

        // Assert
        assertEquals("Test Group", createdGroup.getName());

        questionnaireVersionGroupDao.remove(createdGroup);
    }

    @Test
    public void testCreateOrFindQuestionnaireGroup_NewGroup() {
        // Arrange
        QuestionnaireDTO questionnaireDTO = new QuestionnaireDTO();
        questionnaireDTO.setName("New Questionnaire");
        QuestionnaireVersionGroupDTO questionnaireVersionGroupDTO = new QuestionnaireVersionGroupDTO();
        questionnaireDTO.setQuestionnaireGroupDTO(questionnaireVersionGroupDTO);

        // Act
        QuestionnaireVersionGroup group = questionnaireVersionGroupService.createOrFindQuestionnaireGroup(questionnaireDTO);

        // Assert
        assertEquals("New Questionnaire", group.getName());

        questionnaireVersionGroupDao.remove(group);
    }

    @Test
    public void testCreateOrFindQuestionnaireGroup_ExistingGroup() {
        // Arrange
        QuestionnaireVersionGroup existingGroup = new QuestionnaireVersionGroup();
        existingGroup.setName("Existing Group");
        questionnaireVersionGroupDao.merge(existingGroup);

        QuestionnaireVersionGroupDTO existingGroupDTO = new QuestionnaireVersionGroupDTO();
        existingGroupDTO.setGroupId(existingGroup.getId());

        QuestionnaireDTO questionnaireDTO = new QuestionnaireDTO();
        questionnaireDTO.setId(existingGroup.getId());
        questionnaireDTO.setName("Existing Questionnaire");
        questionnaireDTO.setQuestionnaireGroupDTO(existingGroupDTO);

        // Act
        QuestionnaireVersionGroup group = questionnaireVersionGroupService.createOrFindQuestionnaireGroup(questionnaireDTO);

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

    @Test
    public void testFindMaxVersionInGroup() {
        // Arrange
        Questionnaire questionnaire1 = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaire1.setVersion(1);

        Questionnaire questionnaire2 = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaire2.setVersion(2);

        QuestionnaireVersionGroup group = new QuestionnaireVersionGroup();
        group.getQuestionnaires().add(questionnaire1);
        group.getQuestionnaires().add(questionnaire2);
        questionnaireDao.merge(questionnaire1);
        questionnaireDao.merge(questionnaire2);

        // Act
        int maxVersion = questionnaireVersionGroupService.findMaxVersionInGroup(group);

        // Assert
        assertEquals(2, maxVersion);

        questionnaireDao.remove(questionnaire1);
        questionnaireDao.remove(questionnaire2);
    }
}