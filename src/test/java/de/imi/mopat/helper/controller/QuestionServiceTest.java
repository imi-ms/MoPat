package de.imi.mopat.helper.controller;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.SelectAnswerTest;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTest;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class QuestionServiceTest {

    private Random random;

    @Autowired
    private QuestionnaireDao questionnaireDao;
    
    @Autowired
    private QuestionService questionService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test of {@link QuestionService#duplicateQuestionsToNewQuestionnaire}.<br>
     * Valid input: valid set of {@link Question}s and a {@link Questionnaire}.
     */
    @Test
    public void testCopyQuestionsToQuestionnaire() {
        // Arrange
        Questionnaire originalQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        Question question1 = QuestionTest.getNewValidQuestion(originalQuestionnaire);
        Question question2 = QuestionTest.getNewValidQuestion(originalQuestionnaire);
        Set<Question> originalQuestions = new HashSet<>();
        originalQuestions.add(question1);
        originalQuestions.add(question2);
        
        originalQuestionnaire.addQuestions(originalQuestions);
        
        questionnaireDao.merge(originalQuestionnaire);

        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        newQuestionnaire = questionService.duplicateQuestionsToNewQuestionnaire(originalQuestions, newQuestionnaire);
        
        Set<Question> copiedQuestions = new HashSet<>(
            questionService.getMappingForDuplicatedQuestions(
                originalQuestionnaire,
                newQuestionnaire
            ).questionMap().values());

        // Assert
        assertEquals("The number of copied questions should match the original", originalQuestions.size(), copiedQuestions.size());

        for (Question copiedQuestion : copiedQuestions) {
            assertEquals("The copied question should reference the new questionnaire", newQuestionnaire, copiedQuestion.getQuestionnaire());
            assertNotSame("The copied question should be a new instance and not the same as the original", question1, copiedQuestion);
            assertNotSame("The copied question should be a new instance and not the same as the original", question2, copiedQuestion);
        }
    }

    @Test
    public void testCloneConditions() {
        // Arrange
        Questionnaire originalQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        
        Condition newCondition = ConditionTest.getNewValidCondition();
        // Set bundle to null to prevent faulty cascading
        newCondition.setBundle(null);
        Question question1 = QuestionTest.getNewValidSelectQuestion(originalQuestionnaire);
        Answer answer1 = SelectAnswerTest.getNewValidSelectAnswer();

        answer1.addCondition(newCondition);
        question1.addAnswer(answer1);
        
        Set<Question> originalQuestions = new HashSet<>();
        originalQuestions.add(question1);
        originalQuestions.add((Question) newCondition.getTarget());

        originalQuestionnaire.addQuestions(originalQuestions);

        questionnaireDao.merge(originalQuestionnaire);
        
        Set<Condition> conditions =  new HashSet<>();
        conditions.add(newCondition);

        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        // Act
        newQuestionnaire = questionService.duplicateQuestionsToNewQuestionnaire(originalQuestions, newQuestionnaire);
        MapHolder questionCopyMaps = questionService.getMappingForDuplicatedQuestions(originalQuestionnaire, newQuestionnaire);
            
        Set<Condition> copiedConditions = questionService.cloneConditions(questionCopyMaps.oldQuestionToNewAnswerMap(), questionCopyMaps.questionMap());

        // Assert
        assertEquals("The number of copied questions should match the original", conditions.size(), copiedConditions.size());
    }
    
}