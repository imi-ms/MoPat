package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.QuestionnaireGroupDao;
import de.imi.mopat.helper.model.QuestionnaireGroupDTOMapper;

import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireGroup;
import de.imi.mopat.model.QuestionnaireGroupMember;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.dto.QuestionnaireGroupDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    public void testSaveGroupInformation_NewOriginal() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        doReturn(1).when(original).getVersion(); // Original questionnaire
        Questionnaire duplicate = spy(QuestionnaireTest.getNewValidQuestionnaire());

        // Act
        questionnaireGroupService.saveGroupInformation(duplicate, original);

        // Assert
        verify(mockQuestionnaireGroupDao, times(2)).merge(any(QuestionnaireGroup.class));
        assertEquals("Expected one group to be created", 1, questionnaireGroups.size());
        assertEquals("Expected two questionnaires in the group", 2, questionnaireGroups.get(0).getQuestionnaireGroupMembers().size());
    }

    @Test
    public void testSaveGroupInformation_ExistingOriginalWithDuplicates() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        doReturn(1).when(original).getVersion(); // Original questionnaire
        Questionnaire duplicate1 = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate2 = spy(QuestionnaireTest.getNewValidQuestionnaire());

        QuestionnaireGroup group = new QuestionnaireGroup();
        group.setName(original.getName());
        QuestionnaireGroupMember member = new QuestionnaireGroupMember();
        member.setQuestionnaire(original);
        group.addMember(member);
        questionnaireGroups.add(group);

        when(mockQuestionnaireGroupDao.getAllElements()).thenReturn(questionnaireGroups);

        // Act
        questionnaireGroupService.saveGroupInformation(duplicate1, original);
        questionnaireGroupService.saveGroupInformation(duplicate2, original);

        // Assert
        verify(mockQuestionnaireGroupDao, times(2)).merge(any(QuestionnaireGroup.class));
        assertEquals("Expected one group", 1, questionnaireGroups.size());
        assertEquals("Expected three questionnaires in the group", 3, questionnaireGroups.get(0).getQuestionnaireGroupMembers().size());
    }

    @Test
    public void testIsQuestionnaireInGroup() {
        // Arrange
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        QuestionnaireGroup group = new QuestionnaireGroup();
        QuestionnaireGroupMember member = new QuestionnaireGroupMember();
        member.setQuestionnaire(questionnaire);
        group.addMember(member);
        questionnaireGroups.add(group);

        when(mockQuestionnaireGroupDao.getAllElements()).thenReturn(questionnaireGroups);

        // Act
        boolean result = questionnaireGroupService.isQuestionnaireInGroup(questionnaire);

        // Assert
        assertTrue("Expected questionnaire to be in the group", result);
    }

    @Test
    public void testFindGroupForQuestionnaire() {
        // Arrange
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        QuestionnaireGroup group = new QuestionnaireGroup();
        QuestionnaireGroupMember member = new QuestionnaireGroupMember();
        member.setQuestionnaire(questionnaire);
        group.addMember(member);
        questionnaireGroups.add(group);

        when(mockQuestionnaireGroupDao.getAllElements()).thenReturn(questionnaireGroups);

        // Act
        Optional<QuestionnaireGroup> result = questionnaireGroupService.findGroupForQuestionnaire(questionnaire);

        // Assert
        assertTrue("Expected to find a group for the questionnaire", result.isPresent());
        assertEquals("Expected group ID to match", group.getId(), result.get().getId());
    }

    @Test
    public void testFindMaxVersionInGroup() {
        // Arrange
        Questionnaire questionnaire1 = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaire1.setVersion(1);

        Questionnaire questionnaire2 = new Questionnaire();
        questionnaire2.setVersion(2);

        QuestionnaireGroup group = new QuestionnaireGroup();
        QuestionnaireGroupMember member1 = new QuestionnaireGroupMember();
        member1.setQuestionnaire(questionnaire1);
        QuestionnaireGroupMember member2 = new QuestionnaireGroupMember();
        member2.setQuestionnaire(questionnaire2);
        group.addMember(member1);
        group.addMember(member2);
        questionnaireGroups.add(group);

        // Act
        int maxVersion = questionnaireGroupService.findMaxVersionInGroup(group);

        // Assert
        assertEquals("Expected max version to be 2", 2, maxVersion);
    }

    @Test
    public void testGetGroupById() {
        // Arrange
        QuestionnaireGroup group = spy(new QuestionnaireGroup());
        questionnaireGroups.add(group);

        doReturn(1L).when(group).getId();
        when(mockQuestionnaireGroupDao.getAllElements()).thenReturn(questionnaireGroups);

        // Act
        Optional<QuestionnaireGroup> result = questionnaireGroupService.getGroupById(1L);

        // Assert
        assertTrue("Expected to find a group by ID", result.isPresent());
        assertEquals("Expected group ID to match", group.getId(), result.get().getId());
    }

    @Test
    public void testGetAllUniqueGroupIds() {
        // Arrange
        Long groupId1 = 1L;
        Long groupId2 = 2L;
        QuestionnaireGroup group1 = spy(new QuestionnaireGroup());
        QuestionnaireGroup group2 = spy(new QuestionnaireGroup());

        questionnaireGroups.add(group1);
        questionnaireGroups.add(group2);

        doReturn(groupId1).when(group1).getId();
        doReturn(groupId2).when(group2).getId();
        when(mockQuestionnaireGroupDao.getAllElements()).thenReturn(questionnaireGroups);

        // Act
        Set<Long> uniqueGroupIds = questionnaireGroupService.getAllUniqueGroupIds();

        // Assert
        assertEquals("Expected two unique group IDs", 2, uniqueGroupIds.size());
        assertTrue("Expected unique group IDs to contain 1L", uniqueGroupIds.contains(1L));
        assertTrue("Expected unique group IDs to contain 2L", uniqueGroupIds.contains(2L));
    }

    @Test
    public void testGetAllQuestionnaireGroupDTOs() {
        // Arrange
        QuestionnaireGroup group = new QuestionnaireGroup();

        QuestionnaireGroupDTO dto = new QuestionnaireGroupDTO();
        dto.setGroupName("Test Group");

        questionnaireGroups.add(group);
        when(mockQuestionnaireGroupDao.getAllElements()).thenReturn(questionnaireGroups);
        when(questionnaireGroupDTOMapper.apply(group)).thenReturn(dto);

        // Act
        List<QuestionnaireGroupDTO> dtos = questionnaireGroupService.getAllQuestionnaireGroupDTOs();

        // Assert
        assertEquals("Expected one DTO", 1, dtos.size());
        assertEquals("Expected DTO group name to match", "Test Group", dtos.get(0).getGroupName());
    }

    @Test
    public void testSaveGroupInformation_NewOriginalAndExistingGroup() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        doReturn(1).when(original).getVersion(); // Original questionnaire
        Questionnaire duplicate = spy(QuestionnaireTest.getNewValidQuestionnaire());

        QuestionnaireGroup existingGroup = new QuestionnaireGroup();
        existingGroup.setName("Existing Group");
        QuestionnaireGroupMember existingMember = new QuestionnaireGroupMember();
        existingMember.setQuestionnaire(original);
        existingGroup.addMember(existingMember);
        questionnaireGroups.add(existingGroup);

        when(mockQuestionnaireGroupDao.getAllElements()).thenReturn(questionnaireGroups);

        // Act
        questionnaireGroupService.saveGroupInformation(duplicate, original);

        // Assert
        verify(mockQuestionnaireGroupDao, times(1)).merge(any(QuestionnaireGroup.class));
        assertEquals("Expected one group", 1, questionnaireGroups.size());
        assertEquals("Expected two questionnaires in the group", 2, questionnaireGroups.get(0).getQuestionnaireGroupMembers().size());
    }

    @Test
    public void testSaveGroupInformation_DuplicateQuestionnaire() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        doReturn(1).when(original).getVersion(); // Original questionnaire
        Questionnaire duplicate = spy(QuestionnaireTest.getNewValidQuestionnaire());

        QuestionnaireGroup group = new QuestionnaireGroup();
        group.setName(original.getName());
        QuestionnaireGroupMember member = new QuestionnaireGroupMember();
        member.setQuestionnaire(original);
        group.addMember(member);
        questionnaireGroups.add(group);

        when(mockQuestionnaireGroupDao.getAllElements()).thenReturn(questionnaireGroups);

        // Act
        questionnaireGroupService.saveGroupInformation(duplicate, original);

        // Assert
        verify(mockQuestionnaireGroupDao, times(1)).merge(any(QuestionnaireGroup.class));
        assertEquals("Expected one group", 1, questionnaireGroups.size());
        assertEquals("Expected two questionnaires in the group", 2, questionnaireGroups.get(0).getQuestionnaireGroupMembers().size());
    }
}