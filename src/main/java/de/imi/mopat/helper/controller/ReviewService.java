package de.imi.mopat.helper.controller;

import de.imi.mopat.controller.forms.CreateReviewForm;
import de.imi.mopat.dao.ReviewDao;
import de.imi.mopat.dao.ReviewMessageDao;
import de.imi.mopat.helper.model.ReviewDTOMapper;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.Review;
import de.imi.mopat.model.ReviewMessage;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.ReviewDTO;
import de.imi.mopat.model.dto.UserDTO;
import de.imi.mopat.model.enumeration.ReviewStatus;
import de.imi.mopat.model.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

@Service
public class ReviewService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewDao reviewDao;

    @Autowired
    private ReviewMessageDao reviewMessageDao;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ReviewDTOMapper reviewDTOMapper;

    @Autowired
    private ApplicationMailer applicationMailer;

    private static final Comparator<ReviewDTO> REVIEW_COMPARATOR = Comparator
            .comparing((ReviewDTO review) -> review.getStatus() == ReviewStatus.APPROVED ? 1 : 0)
            .thenComparing(ReviewDTO::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()));


    public List<ReviewDTO> getAllReviews() {
        return reviewDao.getAllElements().stream()
                .map(reviewDTOMapper)
                .sorted(REVIEW_COMPARATOR)
                .peek(this::addUserDetailsToReviewDTO)
                .toList();
    }

    public List<ReviewDTO> getAssignedReviews() {
        Long userId = authService.getAuthenticatedUserId();

        Predicate<ReviewDTO> isAssignedToUser = reviewDTO ->
                Objects.equals(reviewDTO.getEditorId(), userId) ||
                        Objects.equals(reviewDTO.getReviewerId(), userId);

        return getAllReviews().stream()
                .filter(isAssignedToUser)
                .sorted(REVIEW_COMPARATOR)
                .toList();
    }

    public List<UserDTO> getAllReviewers() {
        /*
         * TODO [LJ]: Who should be a reviewer?
         */
        return userService.getUsersByRole(UserRole.ROLE_ADMIN);
    }

    public List<QuestionnaireDTO> getUnapprovedQuestionnaires() {
        /*
          TODO [LJ]: meaningful sorting? only return the questionnaires that were created by the user?
         */
        return questionnaireService.getAllQuestionnaireDTOs().stream()
                .filter(questionnaireDTO -> !questionnaireDTO.isApproved())
                .toList();
    }

    public  ValidationResult addReview(CreateReviewForm form, Locale locale, HttpServletRequest request) {

        ValidationResult validationResult = validateAuthenticatedUser(locale);
        if (validationResult.hasErrors()){
            return validationResult;
        }

        validationResult = validateUserId(form.reviewerId(), locale);
        if (validationResult.hasErrors()){
            return validationResult;
        }

        Long editorId = authService.getAuthenticatedUserId();
        Long reviewerId = form.reviewerId();

        Questionnaire questionnaire = questionnaireService.getQuestionnaireById(form.questionnaireId());

        Review review = createReview(questionnaire, editorId, reviewerId, form.description());
        String reviewLink = generateReviewLink(request, review.getId());
        sendReviewActionMail(reviewerId, questionnaire.getName(), locale, "invitation", form.description(), form.personalMessage(), reviewLink);

        return successWithMessage("review.success.created", locale, questionnaire.getName());
    }

    public Map<String, String> getLocalizedEmailPreviewTexts(String actionType, Locale locale, String questionnaireName, String description, String personalMessage) {
        String previewLink = "LINK";
        return getLocalizedActionEmailTexts(
                actionType,
                questionnaireName.isBlank()
                        ? messageSource.getMessage("review.mail.subject.exampleQuestionnaireName", null, locale)
                        : questionnaireName,
                locale,
                description,
                personalMessage,
                previewLink
        );
    }

    private void addUserDetailsToReviewDTO(ReviewDTO reviewDTO) {
        UserDTO editor = userService.getUserDTOById(reviewDTO.getEditorId());
        UserDTO reviewer = userService.getUserDTOById(reviewDTO.getReviewerId());

        if (editor != null) {
            reviewDTO.setEditorName(editor.getFirstname() + " " + editor.getLastname());
            reviewDTO.setEditorInitials(getInitials(editor.getFirstname(), editor.getLastname()));
        }

        if (reviewer != null) {
            reviewDTO.setReviewerName(reviewer.getFirstname() + " " + reviewer.getLastname());
            reviewDTO.setReviewerInitials(getInitials(reviewer.getFirstname(), reviewer.getLastname()));
        }
    }

    private String getInitials(String firstname, String lastname) {
        StringBuilder initials = new StringBuilder();

        if (firstname != null && !firstname.isEmpty()) {
            initials.append(firstname.charAt(0));
        }
        if (lastname != null && !lastname.isEmpty()) {
            initials.append(lastname.charAt(0));
        }

        return initials.toString().toUpperCase();
    }

    private ValidationResult validateAuthenticatedUser(Locale locale) {
        Long userId = authService.getAuthenticatedUserId();
        if (userId == null) {
            return failureWithMessage(ValidationResult.NOT_AUTHENTICATED, locale);
        }
        return ValidationResult.SUCCESS;
    }

    private ValidationResult failureWithMessage(ValidationResult validationResult, Locale locale, Object... args) {
        String localizedMessage = messageSource.getMessage(
                validationResult.getCode(),
                args,
                validationResult.getDefaultMessage(),
                locale
        );
        validationResult.setLocalizedMessage(
                localizedMessage
        );
        return validationResult;
    }

    private ValidationResult validateUserId(Long reviewerId, Locale locale) {
        if (userService.getUserDTOById(reviewerId) == null) {
            return failureWithMessage(ValidationResult.USER_NOT_FOUND, locale);
        }
        return ValidationResult.SUCCESS;
    }

    private Review createReview(Questionnaire questionnaire, Long editorId, Long reviewerId, String description) {
        Review review = new Review(questionnaire, ReviewStatus.PENDING, editorId, reviewerId);
        reviewDao.merge(review);

        if (StringUtils.isNotBlank(description)) {
            ReviewMessage reviewMessage = new ReviewMessage(review, editorId, reviewerId, description);
            reviewMessageDao.merge(reviewMessage);
            review.setConversation(List.of(reviewMessage));
        }

        reviewDao.merge(review);
        return review;
    }

    private ValidationResult successWithMessage(String messageKey, Locale locale, Object... args) {
        ValidationResult validationResult = ValidationResult.SUCCESS;
        String localizedMessage = messageSource.getMessage(
                messageKey,
                args,
                "Success",
                locale
        );
        validationResult.setLocalizedMessage(
                localizedMessage
        );
        return validationResult;
    }

    private String generateReviewLink(HttpServletRequest request, Long reviewId) {
        String baseUrl = getBaseUrl(request);
        Map<String, String> queryParams = Map.of("id", String.valueOf(reviewId));
        return buildUrlWithParams(baseUrl, "/review/details", queryParams);
    }

    private boolean sendReviewActionMail(
            Long userId,
            String questionnaireName,
            Locale locale,
            String actionType,
            String description,
            String personalMessage,
            String link
    ) {
        UserDTO user = userService.getUserDTOById(userId);
        if (user == null) return false;

        try {
            // Determine subject and content based on action type
            Map<String, String> emailTexts = getLocalizedActionEmailTexts(
                    actionType,
                    questionnaireName,
                    locale,
                    description,
                    personalMessage,
                    link
            );

            String subject = emailTexts.get("subject");
            String content = emailTexts.get("content");

            // Send email
            applicationMailer.sendMail(user.getEmail(), null, subject, content, null);
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to send {} email to {}", actionType, user.getEmail(), e);
            return false;
        }
    }

    private static String getBaseUrl(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String baseUrl;
        if (contextPath.equalsIgnoreCase("/")) {
            baseUrl = (String) request.getRequestURL()
                    .subSequence(0, request.getRequestURL().lastIndexOf("/review"));
        } else {
            baseUrl = request.getRequestURL()
                    .subSequence(0, request.getRequestURL().lastIndexOf(contextPath + "/review"))
                    + contextPath;
        }
        return baseUrl;
    }

    private static String buildUrlWithParams(String baseUrl, String endpoint, Map<String, String> queryParams) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        if (!endpoint.startsWith("/")) {
            urlBuilder.append("/");
        }
        urlBuilder.append(endpoint);

        if (queryParams != null && !queryParams.isEmpty()) {
            urlBuilder.append("?");
            queryParams.forEach((key, value) -> urlBuilder.append(key).append("=").append(value).append("&"));
            urlBuilder.setLength(urlBuilder.length() - 1); // Remove the trailing '&'
        }

        return urlBuilder.toString();
    }

    public Map<String, String> getLocalizedActionEmailTexts(
            String actionType,
            String questionnaireName,
            Locale locale,
            String description,
            String personalMessage,
            String link
    ) {
        Map<String, String> result = new HashMap<>();
        StringBuilder contentBuilder = new StringBuilder();
        String mailFooterEMail = applicationMailer.getMailFooterEMail();
        String mailFooterPhone = applicationMailer.getMailFooterPhone();

        // Define subject
        String subject = messageSource.getMessage("review.mail.subject." + actionType, new Object[]{questionnaireName}, locale);
        result.put("subject", subject);

        // Greeting
        String greeting = messageSource.getMessage("review.mail.greeting", null, locale);
        contentBuilder.append(greeting);

        // Description Intro
        String descriptionIntro = messageSource.getMessage("review.mail.descriptionIntro." + actionType, null, locale);
        contentBuilder.append(descriptionIntro);

        // Description Text
        if (description != null && !description.isBlank()) {
            String descriptionText = messageSource.getMessage("review.mail.descriptionText", null, locale);
            contentBuilder.append(' ').append(descriptionText).append(description).append("\n\n");
        }else{
            contentBuilder.append(".\n\n");
        }

        // Personal Message
        if (personalMessage != null && !personalMessage.isBlank()) {
            String personalMessageIntro = messageSource.getMessage("review.mail.personalMessageIntro", null, locale);
            contentBuilder.append(personalMessageIntro).append(personalMessage).append("\n\n");
        }

        // Link
        if (link != null && !link.isBlank()) {
            String linkText = messageSource.getMessage("review.mail.link", new Object[]{link}, locale);
            contentBuilder.append(linkText);
        }

        // Footer
        String footer = messageSource.getMessage("review.mail.footer", new Object[]{mailFooterEMail, mailFooterPhone}, locale);
        contentBuilder.append(footer);

        result.put("content", contentBuilder.toString());
        return result;
    }
}