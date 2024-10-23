package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.QuestionnaireVersionGroupDao;
import de.imi.mopat.helper.model.QuestionnaireGroupDTOMapper;

import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireVersionGroup;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireVersionGroupDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuestionnaireVersionGroupServiceTest {

    @Mock
    private QuestionnaireVersionGroupDao mockQuestionnaireVersionGroupDao;

    @Mock
    private QuestionnaireGroupDTOMapper questionnaireGroupDTOMapper;

    @InjectMocks
    private QuestionnaireVersionGroupService questionnaireVersionGroupService;

    private List<QuestionnaireVersionGroup> questionnaireVersionGroups;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        questionnaireVersionGroups = new ArrayList<>();
        when(mockQuestionnaireVersionGroupDao.getAllElements()).thenReturn(questionnaireVersionGroups);
        doAnswer(invocation -> {
            QuestionnaireVersionGroup group = invocation.getArgument(0);
            if (!questionnaireVersionGroups.contains(group)) {
                questionnaireVersionGroups.add(group);
            }
            return null;
        }).when(mockQuestionnaireVersionGroupDao).merge(any(QuestionnaireVersionGroup.class));
    }

    @Test
    public void testCreateQuestionnaireGroup() {
        // Act
        QuestionnaireVersionGroup createdGroup = questionnaireVersionGroupService.createQuestionnaireGroup("Test Group");

        // Assert
        assertEquals("Test Group", createdGroup.getName());
        verify(mockQuestionnaireVersionGroupDao, times(1)).merge(createdGroup);
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
        verify(mockQuestionnaireVersionGroupDao, times(1)).merge(group);
    }

    @Test
    public void testCreateOrFindQuestionnaireGroup_ExistingGroup() {
        // Arrange
        QuestionnaireVersionGroupDTO existingGroupDTO = spy(new QuestionnaireVersionGroupDTO());
        QuestionnaireDTO questionnaireDTO = new QuestionnaireDTO();
        questionnaireDTO.setName("Existing Questionnaire");
        questionnaireDTO.setQuestionnaireGroupDTO(existingGroupDTO);

        doReturn(1L).when(existingGroupDTO).getGroupId();
        QuestionnaireVersionGroup existingGroup = spy(new QuestionnaireVersionGroup());
        doReturn(1L).when(existingGroup).getId();
        existingGroup.setName("Existing Group");
        questionnaireVersionGroups.add(existingGroup);

        when(mockQuestionnaireVersionGroupDao.getAllElements()).thenReturn(questionnaireVersionGroups);

        // Act
        QuestionnaireVersionGroup group = questionnaireVersionGroupService.createOrFindQuestionnaireGroup(questionnaireDTO);

        // Assert
        assertEquals(existingGroup.getId(), group.getId());
        verify(mockQuestionnaireVersionGroupDao, times(0)).merge(any(QuestionnaireVersionGroup.class));
    }

    @Test
    public void testGetQuestionnaireGroupById() {
        // Arrange
        QuestionnaireVersionGroup group = spy(new QuestionnaireVersionGroup());
        doReturn(1L).when(group).getId();

        questionnaireVersionGroups.add(group);

        // Act
        Optional<QuestionnaireVersionGroup> result = questionnaireVersionGroupService.getQuestionnaireGroupById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(group.getId(), result.get().getId());
    }

    @Test
    public void testFindGroupForQuestionnaire() {
        // Arrange
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        QuestionnaireVersionGroup group = new QuestionnaireVersionGroup();
        group.getQuestionnaires().add(questionnaire);
        questionnaireVersionGroups.add(group);

        when(mockQuestionnaireVersionGroupDao.getAllElements()).thenReturn(questionnaireVersionGroups);

        // Act
        Optional<QuestionnaireVersionGroup> result = questionnaireVersionGroupService.findGroupForQuestionnaire(questionnaire);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(group.getId(), result.get().getId());
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
        questionnaireVersionGroups.add(group);

        // Act
        int maxVersion = questionnaireVersionGroupService.findMaxVersionInGroup(group);

        // Assert
        assertEquals(2, maxVersion);
    }
}