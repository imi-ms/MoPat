package de.imi.mopat.helper.controller;

import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireGroup;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.utils.MockQuestionnaireGroupDao;
import de.imi.mopat.utils.TestConfig;
import de.imi.mopat.utils.Helper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class QuestionnaireGroupServiceTest {

    @Autowired
    private QuestionnaireGroupService questionnaireGroupService;

    @Autowired
    private MockQuestionnaireGroupDao mockQuestionnaireGroupDao;

    @Before
    public void setUp() {
        mockQuestionnaireGroupDao.clear(); // Clear the mock storage before each test
    }

    @Test
    public void testSaveGroupInformation_NewOriginal() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate = spy(QuestionnaireTest.getNewValidQuestionnaire());

        doReturn(Helper.generatePositiveNonZeroLong()).when(original).getId();
        doReturn(Helper.generatePositiveNonZeroLong()).when(duplicate).getId();
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
    public void testSaveGroupInformation_ExistingOriginalWithDuplicates() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate1 = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate2 = spy(QuestionnaireTest.getNewValidQuestionnaire());

        when(original.getId()).thenReturn(Helper.generatePositiveNonZeroLong());
        when(duplicate1.getId()).thenReturn(Helper.generatePositiveNonZeroLong());
        when(duplicate2.getId()).thenReturn(Helper.generatePositiveNonZeroLong());
        original.setVersion(1);

        // Act
        questionnaireGroupService.saveGroupInformation(duplicate1, original);
        questionnaireGroupService.saveGroupInformation(duplicate2, original);
        List<QuestionnaireGroup> finalGroups = mockQuestionnaireGroupDao.getAllElements();

        // Assert
        Assert.assertEquals(3, finalGroups.size());
        Assert.assertEquals(finalGroups.get(0).getGroupId(), finalGroups.get(1).getGroupId());
        Assert.assertEquals(finalGroups.get(1).getGroupId(), finalGroups.get(2).getGroupId());
        Assert.assertEquals(original, finalGroups.get(0).getQuestionnaire());
        Assert.assertEquals(duplicate1, finalGroups.get(1).getQuestionnaire());
        Assert.assertEquals(duplicate2, finalGroups.get(2).getQuestionnaire());
    }

    @Test
    public void testSaveGroupInformation_DuplicateOfDuplicate() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate1 = spy(QuestionnaireTest.getNewValidQuestionnaire());
        Questionnaire duplicate2 = spy(QuestionnaireTest.getNewValidQuestionnaire());

        doReturn(Helper.generatePositiveNonZeroLong()).when(original).getId();
        doReturn(Helper.generatePositiveNonZeroLong()).when(duplicate1).getId();
        doReturn(Helper.generatePositiveNonZeroLong()).when(duplicate2).getId();
        original.setVersion(1);

        // First duplicate to establish group
        questionnaireGroupService.saveGroupInformation(duplicate1, original);

        // Act
        questionnaireGroupService.saveGroupInformation(duplicate2, duplicate1);


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

        doReturn(Helper.generatePositiveNonZeroLong()).when(original).getId();
        doReturn(Helper.generatePositiveNonZeroLong()).when(duplicate).getId();
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
        doReturn(Helper.generatePositiveNonZeroLong()).when(duplicate).getId();

        // Act
        questionnaireGroupService.saveGroupInformation(duplicate, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveGroupInformation_NullDuplicate() {
        // Arrange
        Questionnaire original = spy(QuestionnaireTest.getNewValidQuestionnaire());
        doReturn(Helper.generatePositiveNonZeroLong()).when(original).getId();
        original.setVersion(1);

        // Act
        questionnaireGroupService.saveGroupInformation(null, original);
    }
}