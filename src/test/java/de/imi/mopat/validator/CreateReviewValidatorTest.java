package de.imi.mopat.validator;

import de.imi.mopat.controller.forms.CreateReviewForm;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Questionnaire;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.Objects;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

public class CreateReviewValidatorTest {

    @Mock
    private QuestionnaireDao questionnaireDao;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private CreateReviewValidator validator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testSupports() {
        assertTrue(validator.supports(CreateReviewForm.class));
        assertFalse(validator.supports(Object.class));
    }

    @Test
    public void testValidate_ValidReview() {
        CreateReviewForm form = new CreateReviewForm(1L, 2L, "Valid review", "message", null);
        Questionnaire mockQuestionnaire = mock(Questionnaire.class);

        when(questionnaireDao.getElementById(1L)).thenReturn(mockQuestionnaire);
        when(mockQuestionnaire.isUnderReview()).thenReturn(false);
        when(mockQuestionnaire.getVersion()).thenReturn(1);

        Errors errors = new BindException(form, "createReviewForm");
        validator.validate(form, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void testValidate_ReviewerIdMissing() {
        // Arrange
        CreateReviewForm form = new CreateReviewForm(1L, null, "Valid review", "message", null);
        Errors errors = new BindException(form, "createReviewForm");

        when(messageSource.getMessage(eq("review.error.reviewer.empty"), any(), any(), any()))
                .thenReturn("Reviewer required");

        // Act
        validator.validate(form, errors);

        // Assert
        assertTrue("Expected validation error on reviewerId", errors.hasFieldErrors("reviewerId"));
        assertEquals("Reviewer required", Objects.requireNonNull(errors.getFieldError("reviewerId")).getDefaultMessage());
    }

    @Test
    public void testValidate_QuestionnaireNotFound() {
        CreateReviewForm form = new CreateReviewForm(999L, 2L, "Valid review", "message", null);
        Errors errors = new BindException(form, "createReviewForm");

        when(questionnaireDao.getElementById(999L)).thenReturn(null);
        when(messageSource.getMessage(eq("review.error.questionnaire.not.found"), any(), any(), any())).thenReturn("Questionnaire not found");

        validator.validate(form, errors);
        assertTrue(errors.hasErrors());
        assertEquals("Questionnaire not found", Objects.requireNonNull(errors.getFieldError("questionnaireId")).getDefaultMessage());
    }

    @Test
    public void testValidate_QuestionnaireAlreadyUnderReview() {
        CreateReviewForm form = new CreateReviewForm(1L, 2L, "Valid review", "message", null);
        Questionnaire mockQuestionnaire = mock(Questionnaire.class);

        when(questionnaireDao.getElementById(1L)).thenReturn(mockQuestionnaire);
        when(mockQuestionnaire.isUnderReview()).thenReturn(true);
        when(messageSource.getMessage(eq("review.error.questionnaire.alreadyUnderReview"), any(), any(), any())).thenReturn("Already under review");

        Errors errors = new BindException(form, "createReviewForm");
        validator.validate(form, errors);

        assertTrue(errors.hasErrors());
        assertEquals("Already under review", Objects.requireNonNull(errors.getFieldError("questionnaireId")).getDefaultMessage());
    }

    @Test
    public void testValidate_DescriptionRequiredForDuplicate() {
        CreateReviewForm form = new CreateReviewForm(1L, 2L, "", "message", null);
        Questionnaire mockQuestionnaire = mock(Questionnaire.class);

        when(questionnaireDao.getElementById(1L)).thenReturn(mockQuestionnaire);
        when(mockQuestionnaire.isUnderReview()).thenReturn(false);
        when(mockQuestionnaire.getVersion()).thenReturn(2);
        when(messageSource.getMessage(eq("review.error.description.empty.for.duplicate"), any(), any(), any())).thenReturn("Description required");

        Errors errors = new BindException(form, "createReviewForm");
        validator.validate(form, errors);

        assertTrue(errors.hasErrors());
        assertEquals("Description required", Objects.requireNonNull(errors.getFieldError("description")).getDefaultMessage());
    }
}