package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.AnswerTest;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTest;
import java.lang.reflect.Field;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class QuestionServiceTest {

    private Random random;

    @InjectMocks
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

        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        // Act
        Set<Question> copiedQuestions = new HashSet<>(questionService.duplicateQuestionsToNewQuestionnaire(originalQuestions, newQuestionnaire).questionMap().values());

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
        Question question1 = QuestionTest.getNewValidQuestion(originalQuestionnaire);
        Answer answer1 = AnswerTest.getNewValidRandomAnswer();

        answer1.addCondition(newCondition);
        question1.addAnswer(answer1);
        Set<Question> originalQuestions = new HashSet<>();
        originalQuestions.add(question1);
        originalQuestions.add((Question) newCondition.getTarget());

        Set<Condition> conditions =  new HashSet<>();
        conditions.add(newCondition);

        Questionnaire newQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        // Act
        MapHolder questionCopyMaps = questionService.duplicateQuestionsToNewQuestionnaire(originalQuestions, newQuestionnaire);

        Set<Condition> copiedConditions = questionService.cloneConditions(questionCopyMaps.oldQuestionToNewAnswerMap(), questionCopyMaps.questionMap());

        // Assert
        assertEquals("The number of copied questions should match the original", conditions.size(), copiedConditions.size());
    }
}