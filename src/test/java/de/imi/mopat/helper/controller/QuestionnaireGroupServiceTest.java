package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.QuestionnaireGroupDao;
import de.imi.mopat.helper.model.QuestionnaireGroupDTOMapper;

import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireGroup;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.dto.QuestionnaireDTO;
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

public class QuestionnaireGroupServiceTest {

    @Mock
    private QuestionnaireGroupDao mockQuestionnaireGroupDao;

    @Mock
    private QuestionnaireGroupDTOMapper questionnaireGroupDTOMapper;

    @InjectMocks
    private QuestionnaireGroupService questionnaireGroupService;

    private List<QuestionnaireGroup> questionnaireGroups;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        questionnaireGroups = new ArrayList<>();
        when(mockQuestionnaireGroupDao.getAllElements()).thenReturn(questionnaireGroups);
        doAnswer(invocation -> {
            QuestionnaireGroup group = invocation.getArgument(0);
            if (!questionnaireGroups.contains(group)) {
                questionnaireGroups.add(group);
            }
            return null;
        }).when(mockQuestionnaireGroupDao).merge(any(QuestionnaireGroup.class));
    }

    @Test
    public void testCreateQuestionnaireGroup() {
        // Act
        QuestionnaireGroup createdGroup = questionnaireGroupService.createQuestionnaireGroup("Test Group");

        // Assert
        assertEquals("Test Group", createdGroup.getName());
        verify(mockQuestionnaireGroupDao, times(1)).merge(createdGroup);
    }

    @Test
    public void testCreateOrFindQuestionnaireGroup_NewGroup() {
        // Arrange
        QuestionnaireDTO questionnaireDTO = new QuestionnaireDTO();
        questionnaireDTO.setName("New Questionnaire");

        // Act
        QuestionnaireGroup group = questionnaireGroupService.createOrFindQuestionnaireGroup(questionnaireDTO);

        // Assert
        assertEquals("New Questionnaire", group.getName());
        verify(mockQuestionnaireGroupDao, times(1)).merge(group);
    }

    @Test
    public void testCreateOrFindQuestionnaireGroup_ExistingGroup() {
        // Arrange
        QuestionnaireDTO questionnaireDTO = new QuestionnaireDTO();
        questionnaireDTO.setName("Existing Questionnaire");

        QuestionnaireGroup existingGroup = spy(new QuestionnaireGroup());
        doReturn(1L).when(existingGroup).getId();
        existingGroup.setName("Existing Group");
        questionnaireGroups.add(existingGroup);

        when(mockQuestionnaireGroupDao.getAllElements()).thenReturn(questionnaireGroups);

        // Act
        QuestionnaireGroup group = questionnaireGroupService.createOrFindQuestionnaireGroup(questionnaireDTO);

        // Assert
        assertEquals(existingGroup.getId(), group.getId());
        verify(mockQuestionnaireGroupDao, times(0)).merge(any(QuestionnaireGroup.class));
    }

    @Test
    public void testGetQuestionnaireGroupById() {
        // Arrange
        QuestionnaireGroup group = spy(new QuestionnaireGroup());
        doReturn(1L).when(group).getId();

        questionnaireGroups.add(group);

        // Act
        Optional<QuestionnaireGroup> result = questionnaireGroupService.getQuestionnaireGroupById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(group.getId(), result.get().getId());
    }

    @Test
    public void testFindGroupForQuestionnaire() {
        // Arrange
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        QuestionnaireGroup group = new QuestionnaireGroup();
        group.getQuestionnaires().add(questionnaire);
        questionnaireGroups.add(group);

        when(mockQuestionnaireGroupDao.getAllElements()).thenReturn(questionnaireGroups);

        // Act
        Optional<QuestionnaireGroup> result = questionnaireGroupService.findGroupForQuestionnaire(questionnaire);

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

        QuestionnaireGroup group = new QuestionnaireGroup();
        group.getQuestionnaires().add(questionnaire1);
        group.getQuestionnaires().add(questionnaire2);
        questionnaireGroups.add(group);

        // Act
        int maxVersion = questionnaireGroupService.findMaxVersionInGroup(group);

        // Assert
        assertEquals(2, maxVersion);
    }
}