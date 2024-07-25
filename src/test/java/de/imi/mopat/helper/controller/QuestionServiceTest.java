package de.imi.mopat.helper.controller;

import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class QuestionServiceTest {

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
        Set<Question> copiedQuestions = new HashSet<>(questionService.duplicateQuestionsToNewQuestionnaire(originalQuestions, newQuestionnaire).values());

        // Assert
        assertEquals("The number of copied questions should match the original", originalQuestions.size(), copiedQuestions.size());

        for (Question copiedQuestion : copiedQuestions) {
            assertEquals("The copied question should reference the new questionnaire", newQuestionnaire, copiedQuestion.getQuestionnaire());
            assertNotSame("The copied question should be a new instance and not the same as the original", question1, copiedQuestion);
            assertNotSame("The copied question should be a new instance and not the same as the original", question2, copiedQuestion);
        }
    }
}