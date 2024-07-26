package de.imi.mopat.helper.controller;

import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireGroup;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.utils.MockQuestionnaireGroupDao;
import de.imi.mopat.utils.TestConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class QuestionnaireGroupServiceTest {

    private Random random;

    @Autowired
    private QuestionnaireGroupService questionnaireGroupService;

    @Autowired
    private MockQuestionnaireGroupDao mockQuestionnaireGroupDao;

    private Long getPositiveId() {
        return random.nextLong() & Long.MAX_VALUE;
    }

    @Before
    public void setUp() {
        random = new Random();
        mockQuestionnaireGroupDao.clear(); // Clear the mock storage before each test
    }

    // Helper method to set private fields using reflection
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testSaveGroupInformation_NewOriginal() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate = spy(QuestionnaireTest.getNewValidQuestionnaire());

        doReturn(getPositiveId()).when(original).getId();
        doReturn(getPositiveId()).when(duplicate).getId();
        original.setVersion(1);


        // Act
        questionnaireGroupService.saveGroupInformation(duplicate, original);

        // Assert
        List<QuestionnaireGroup> groups = mockQuestionnaireGroupDao.getAllElements();
        groups.forEach(System.out::println);
        Assert.assertEquals(2, groups.size());
        Assert.assertEquals(groups.get(0).getGroupId(), groups.get(1).getGroupId());
        Assert.assertEquals(original, groups.get(0).getQuestionnaire());
        Assert.assertEquals(duplicate, groups.get(1).getQuestionnaire());
    }

    @Test
    public void testSaveGroupInformation_ExistingOriginalWithDuplicates() throws Exception {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate1 = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate2 = spy(QuestionnaireTest.getNewValidQuestionnaire());

        setField(original, "id", getPositiveId());
        setField(duplicate1, "id", getPositiveId());
        setField(duplicate2, "id", getPositiveId());
        original.setVersion(1);

        // Act
        questionnaireGroupService.saveGroupInformation(duplicate1, original);
        questionnaireGroupService.saveGroupInformation(duplicate2, original);
        List<QuestionnaireGroup> finalGroups = mockQuestionnaireGroupDao.getAllElements();

        printGroups();

        // Assert
        Assert.assertEquals(3, finalGroups.size());
        Assert.assertEquals(finalGroups.get(0).getGroupId(), finalGroups.get(1).getGroupId());
        Assert.assertEquals(finalGroups.get(1).getGroupId(), finalGroups.get(2).getGroupId());
        Assert.assertEquals(original, finalGroups.get(0).getQuestionnaire());
        Assert.assertEquals(duplicate1, finalGroups.get(1).getQuestionnaire());
        Assert.assertEquals(duplicate2, finalGroups.get(2).getQuestionnaire());
    }

    private void printGroups() {
        List<QuestionnaireGroup> groupsAfterFirstOperation = new ArrayList<>(mockQuestionnaireGroupDao.getAllElements());
        System.out.println("Final operation groups: ");
        for (QuestionnaireGroup questionnaireGroup : groupsAfterFirstOperation){
            System.out.println("    "+questionnaireGroup);
        }
    }

    @Test
    public void testSaveGroupInformation_DuplicateOfDuplicate() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate1 = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate2 = spy(QuestionnaireTest.getNewValidQuestionnaire());

        doReturn(getPositiveId()).when(original).getId();
        doReturn(getPositiveId()).when(duplicate1).getId();
        doReturn(getPositiveId()).when(duplicate2).getId();
        original.setVersion(1);

        // First duplicate to establish group
        questionnaireGroupService.saveGroupInformation(duplicate1, original);

        // Act
        questionnaireGroupService.saveGroupInformation(duplicate2, duplicate1);

        printGroups();

        // Assert
        List<QuestionnaireGroup> groups = mockQuestionnaireGroupDao.getAllElements();
        Assert.assertEquals(3, groups.size());
        Assert.assertEquals(groups.get(0).getGroupId(), groups.get(1).getGroupId());
        Assert.assertEquals(groups.get(1).getGroupId(), groups.get(2).getGroupId());
        Assert.assertEquals(original, groups.get(0).getQuestionnaire());
        Assert.assertEquals(duplicate1, groups.get(1).getQuestionnaire());
        Assert.assertEquals(duplicate2, groups.get(2).getQuestionnaire());
    }


    @Test
    public void testSaveGroupInformation_NewDuplicate() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate = spy(QuestionnaireTest.getNewValidQuestionnaire());

        doReturn(getPositiveId()).when(original).getId();
        doReturn(getPositiveId()).when(duplicate).getId();
        original.setVersion(1);

        // Act
        questionnaireGroupService.saveGroupInformation(duplicate, original);

        // Assert
        List<QuestionnaireGroup> groups = mockQuestionnaireGroupDao.getAllElements();
        Assert.assertEquals(2, groups.size());
        Assert.assertEquals(groups.get(0).getGroupId(), groups.get(1).getGroupId());
        Assert.assertEquals(original, groups.get(0).getQuestionnaire());
        Assert.assertEquals(duplicate, groups.get(1).getQuestionnaire());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveGroupInformation_NullOriginal() {
        // Arrange
        Questionnaire duplicate = spy(QuestionnaireTest.getNewValidQuestionnaire());
        doReturn(getPositiveId()).when(duplicate).getId();

        // Act
        questionnaireGroupService.saveGroupInformation(duplicate, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveGroupInformation_NullDuplicate() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        doReturn(getPositiveId()).when(original).getId();
        original.setVersion(1);

        // Act
        questionnaireGroupService.saveGroupInformation(null, original);
    }
}