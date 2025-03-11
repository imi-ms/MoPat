package de.imi.mopat.helper.controller;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.controller.forms.CreateReviewForm;
import de.imi.mopat.controller.forms.ReviewDecisionForm;
import de.imi.mopat.dao.ReviewDao;
import de.imi.mopat.dao.ReviewMessageDao;
import de.imi.mopat.helper.model.ReviewDTOMapper;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.Review;
import de.imi.mopat.model.dto.ReviewDTO;
import de.imi.mopat.model.dto.UserDTO;
import de.imi.mopat.model.enumeration.ReviewStatus;
import de.imi.mopat.model.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
        MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class ReviewServiceTest {

    @Mock
    private ReviewDao reviewDao;

    @Mock
    private ReviewMessageDao reviewMessageDao;

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @Mock
    private QuestionnaireService questionnaireService;

    @Mock
    private QuestionnaireVersionGroupService questionnaireVersionGroupService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ReviewDTOMapper reviewDTOMapper;

    @Mock
    private ApplicationMailer applicationMailer;

    @Mock
    private Clock clock;

    @Autowired
    @InjectMocks
    private ReviewService reviewService;


    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        Instant fixedInstant = Instant.now();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        SecurityContextHolder.setContext(new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken("testUser", "password",
                        List.of(new SimpleGrantedAuthority("ROLE_MODERATOR"))))
        );
    }

    @Test
    public void testAddReview_Success() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/review/details"));

        Long editorId = 1L;
        Long reviewerId = 2L;
        Long questionnaireId = 3L;

        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        when(questionnaireService.getQuestionnaireById(questionnaireId)).thenReturn(questionnaire);
        when(authService.getAuthenticatedUserId()).thenReturn(editorId);
        when(userService.getUserDTOById(reviewerId)).thenReturn(new UserDTO());

        CreateReviewForm form = new CreateReviewForm(questionnaireId, reviewerId, "Some description", "Personal message", "de");

        // Act
        ValidationResult result = reviewService.addReview(form, Locale.GERMAN, request);

        // Assert
        assertEquals(ValidationResult.SUCCESS, result);
        verify(reviewDao, times(2)).merge(any(Review.class));
        verify(applicationMailer, times(1)).sendMail(any(), any(), any(), any(), any());
    }

    @Test
    public void testApproveReview_Success() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        Long reviewerId = 1L;

        Long editorId = 2L;
        UserDTO editor = new UserDTO();
        editor.setId(editorId);

        Long reviewId = 1L;
        Review review = new Review();
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(reviewId);
        reviewDTO.setEditorId(editorId);

        ReviewDecisionForm form = new ReviewDecisionForm(reviewId, "approved", "Great work", "Personal message", true, reviewerId,"de");

        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/review/details"));
        when(userService.getUserDTOById(editorId)).thenReturn(editor);
        when(reviewDao.getElementById(any())).thenReturn(review);
        when(reviewDTOMapper.apply(any(Review.class))).thenReturn(reviewDTO);
        when(questionnaireService.getQuestionnaireById(any())).thenReturn(questionnaire);

        // Act
        ValidationResult result = reviewService.approveReview(form, Locale.GERMAN, request);

        // Assert
        assertEquals(ValidationResult.SUCCESS, result);
        assertEquals(ReviewStatus.APPROVED, reviewDao.getElementById(reviewId).getStatus());
        verify(reviewDao, times(1)).merge(review);
        verify(applicationMailer, times(1)).sendMail(any(), any(), any(), any(), any());
    }

    @Test
    public void testRejectReview_Success() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        Long reviewerId = 1L;

        Long editorId = 2L;
        UserDTO editor = new UserDTO();
        editor.setId(editorId);

        Long reviewId = 1L;
        Review review = new Review();
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(reviewId);
        reviewDTO.setEditorId(editorId);

        ReviewDecisionForm form = new ReviewDecisionForm(reviewId, "approved", "Great work", "Personal message", true, reviewerId,"de");

        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/review/details"));
        when(userService.getUserDTOById(editorId)).thenReturn(editor);
        when(reviewDao.getElementById(any())).thenReturn(review);
        when(reviewDTOMapper.apply(any(Review.class))).thenReturn(reviewDTO);
        when(questionnaireService.getQuestionnaireById(any())).thenReturn(questionnaire);

        // Act
        ValidationResult result = reviewService.rejectReview(form, Locale.GERMAN, request);

        // Assert
        assertEquals(ValidationResult.SUCCESS, result);
        assertEquals(ReviewStatus.REJECTED, reviewDao.getElementById(reviewId).getStatus());
        verify(reviewDao, times(1)).merge(review);
        verify(applicationMailer, times(1)).sendMail(any(), any(), any(), any(), any());
    }

    @Test
    public void testResubmitReview_Success() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();

        Long reviewerId = 1L;
        UserDTO reviewer = new UserDTO();
        reviewer.setId(reviewerId);

        Long reviewId = 1L;
        Review review = new Review();
        review.setQuestionnaire(questionnaire);
        review.setReviewerId(reviewerId);
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(reviewId);
        reviewDTO.setReviewerId(reviewerId);

        ReviewDecisionForm form = new ReviewDecisionForm(reviewId, "approved", "Great work", "Personal message", true, reviewerId,"de");

        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/review/details"));
        when(userService.getUserDTOById(reviewerId)).thenReturn(reviewer);
        when(reviewDao.getElementById(any())).thenReturn(review);
        when(reviewDTOMapper.apply(any(Review.class))).thenReturn(reviewDTO);
        when(questionnaireService.getQuestionnaireById(any())).thenReturn(questionnaire);

        // Act
        ValidationResult result = reviewService.resubmitReview(form, Locale.GERMAN, request);

        // Assert
        assertEquals(ValidationResult.SUCCESS, result);
        assertEquals(ReviewStatus.PENDING, reviewDao.getElementById(reviewId).getStatus());
        verify(reviewDao, times(1)).merge(review);
        verify(applicationMailer, times(1)).sendMail(any(), any(), any(), any(), any());
    }

    @Test
    public void testDeleteReviewById_Success() {
        // Arrange
        Locale locale = Locale.GERMAN;
        Long reviewId = 1L;

        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Review reviewToDelete = new Review();
        reviewToDelete.setQuestionnaire(questionnaire);

        when(reviewDao.getElementById(reviewId)).thenReturn(reviewToDelete);

        // Act
        ValidationResult result = reviewService.deleteReviewById(reviewId, locale);

        // Assert
        assertEquals(ValidationResult.SUCCESS, result);
        verify(reviewDao, times(1)).remove(reviewToDelete);
    }

    @Test
    public void testCanModifyReview_AdminCanModify() {
        // Arrange
        Long adminUserId = 1L;
        Long reviewId = 1L;

        when(authService.getAuthenticatedUserId()).thenReturn(adminUserId);
        when(authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)).thenReturn(true);

        // Act
        boolean canModify = reviewService.canModifyReview(reviewId);

        // Assert
        assertTrue(canModify);
    }

    @Test
    public void testCanModifyReview_EditorCanModify() {
        // Arrange
        Long editorId = 1L;
        Long reviewId = 1L;

        Review review = new Review();
        review.setEditorId(editorId);

        when(authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)).thenReturn(false);
        when(reviewDao.getElementById(reviewId)).thenReturn(review);

        // Simulating that the authenticated user is the same as the editor of the review
        when(authService.getAuthenticatedUserId()).thenReturn(editorId);

        // Act
        boolean canModify = reviewService.canModifyReview(reviewId);

        // Assert
        assertTrue(canModify);
    }

    @Test
    public void testCanModifyReview_OtherUserCannotModify() {
        // Arrange
        Long editorId = 1L;
        Long otherUserId = 2L;
        Long reviewId = 1L;

        Review review = new Review();
        review.setEditorId(editorId);

        when(authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)).thenReturn(false);
        when(reviewDao.getElementById(reviewId)).thenReturn(review);

        // Simulating that the authenticated user is NOT the editor of the review
        when(authService.getAuthenticatedUserId()).thenReturn(otherUserId);

        // Act
        boolean canModify = reviewService.canModifyReview(reviewId);

        // Assert
        assertFalse(canModify);
    }

    @Test
    public void testValidateReview_ValidId() {
        // Arrange
        Long validReviewId = 1L;
        Review validReview = new Review();

        Locale locale = Locale.GERMAN;

        when(reviewDao.getElementById(validReviewId)).thenReturn(validReview);

        // Act
        ValidationResult result = reviewService.validateReview(validReviewId, locale);

        // Assert
        assertEquals(ValidationResult.SUCCESS, result);
    }

    @Test
    public void testValidateReview_InvalidId() {
        // Arrange
        Locale locale = Locale.GERMAN;

        // Act
        ValidationResult result = reviewService.validateReview(null, locale);

        // Assert
        assertEquals(ValidationResult.INVALID_REVIEW_ID, result);
    }

    @Test
    public void testCompleteReviewIfPresent() {
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        Review review = new Review();
        review.setQuestionnaire(questionnaire);

        when(reviewDao.getAllElements()).thenReturn(List.of(review));

        reviewService.completeReviewIfPresent(questionnaire);

        assertEquals(ReviewStatus.APPROVED, review.getStatus());
        verify(reviewDao, times(1)).merge(review);
    }

    @Test
    public void testApproveReview_InvalidReviewId() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        ReviewDecisionForm form = new ReviewDecisionForm(null, "approved", "Great work", "Personal message", true, 1L, "de");

        // Act
        ValidationResult result = reviewService.approveReview(form, Locale.GERMAN, request);

        // Assert
        assertEquals(ValidationResult.INVALID_REVIEW_ID, result);
    }

    @Test
    public void testApproveReview_UserNotAuthorized() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        ReviewDecisionForm form = new ReviewDecisionForm(1L, "approved", "Great work", "Personal message", true, 1L, "de");

        when(authService.getAuthenticatedUserId()).thenReturn(null);

        // Act
        ValidationResult result = reviewService.approveReview(form, Locale.GERMAN, request);

        // Assert
        assertEquals(ValidationResult.NOT_AUTHENTICATED, result);
    }

    @Test
    public void testValidateReview_ReviewNotFound() {
        // Arrange
        Long invalidReviewId = 999L;
        Locale locale = Locale.GERMAN;

        when(reviewDao.getElementById(invalidReviewId)).thenReturn(null);

        // Act
        ValidationResult result = reviewService.validateReview(invalidReviewId, locale);

        // Assert
        assertEquals(ValidationResult.REVIEW_NOT_FOUND, result);
    }
}