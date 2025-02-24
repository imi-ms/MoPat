package de.imi.mopat.helper.controller;

import de.imi.mopat.controller.forms.CreateReviewForm;
import de.imi.mopat.controller.forms.ReviewDecisionForm;
import de.imi.mopat.dao.ReviewDao;
import de.imi.mopat.dao.ReviewMessageDao;
import de.imi.mopat.helper.model.ReviewDTOMapper;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.Review;
import de.imi.mopat.model.ReviewMessage;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.ReviewDTO;
import de.imi.mopat.model.dto.ReviewMessageDTO;
import de.imi.mopat.model.dto.UserDTO;
import de.imi.mopat.model.enumeration.ReviewStatus;
import de.imi.mopat.model.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private QuestionnaireVersionGroupService questionnaireVersionGroupService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ReviewDTOMapper reviewDTOMapper;

    @Autowired
    private ApplicationMailer applicationMailer;

    @Autowired
    private Clock clock;

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

    public ValidationResult deleteReviewById(Long reviewId, Locale locale) {
        ValidationResult validationResult = validateReview(reviewId, locale);
        if (validationResult.hasErrors()) {
            return validationResult;
        }

        validationResult = validateReviewAccess(reviewId, locale);
        if(validationResult.hasErrors()){
            return validationResult;
        }

        Review review = reviewDao.getElementById(reviewId);
        String questionnaireName = review.getQuestionnaire().getName();

        reviewDao.remove(review);
        return successWithMessage("review.success.deleted", locale, questionnaireName);
    }

    /*
     * TODO [LJ] Should there be a reviewer role?
     */
    public boolean isUserReviewer() {
        return authService.hasExactRole(UserRole.ROLE_ADMIN);
    }

    public ValidationResult approveReview(ReviewDecisionForm form, Locale locale, HttpServletRequest request) {
        ValidationResult validationResult = processReview(
                form.reviewId(),
                form.description(),
                ReviewStatus.APPROVED,
                form.isMainVersion(),
                locale
        );
        if (validationResult.hasErrors()) {
            return validationResult;
        }

        ReviewDTO reviewDTO = getReviewById(form.reviewId());
        String reviewLink = generateReviewLink(request, form.reviewId());
        Questionnaire questionnaire = questionnaireService.getQuestionnaireById(reviewDTO.getQuestionnaireId());

        sendReviewActionMail(reviewDTO.getEditorId(), questionnaire.getName(), locale, "approve", form.description(), form.personalMessage(), reviewLink);

        return successWithMessage("review.success.approved", locale);
    }

    public ValidationResult rejectReview(ReviewDecisionForm form, Locale locale, HttpServletRequest request) {
        ValidationResult validationResult = processReview(
                form.reviewId(),
                form.description(),
                ReviewStatus.REJECTED,
                false,
                locale
        );
        if (validationResult.hasErrors()) {
            return validationResult;
        }

        ReviewDTO reviewDTO = getReviewById(form.reviewId());
        String reviewLink = generateReviewLink(request, form.reviewId());
        Questionnaire questionnaire = questionnaireService.getQuestionnaireById(reviewDTO.getQuestionnaireId());

        sendReviewActionMail(reviewDTO.getEditorId(), questionnaire.getName(), locale, "reject", form.description(), form.personalMessage(), reviewLink);

        return successWithMessage("review.success.rejected", locale);
    }

    public ValidationResult resubmitReview(ReviewDecisionForm form, Locale locale, HttpServletRequest request) {
        ValidationResult validationResult = validateReview(form.reviewId(), locale);
        if (validationResult.hasErrors()) {
            return validationResult;
        }

        Review review = reviewDao.getElementById(form.reviewId());

        if (!form.description().isBlank()) {
            addReviewMessage(review, review.getEditorId(), form.description());
        }

        review.setStatus(ReviewStatus.PENDING);
        reviewDao.merge(review);

        String questionnaireName = review.getQuestionnaire().getName();
        String reviewLink = generateReviewLink(request, review.getId());
        sendReviewActionMail(review.getReviewerId(), questionnaireName, locale, "resubmit", form.description(), form.personalMessage(), reviewLink);

        return successWithMessage("review.success.resubmitted", locale, questionnaireName);
    }

    public ValidationResult assignReviewer(ReviewDecisionForm form, Locale locale, HttpServletRequest request) {
        ValidationResult validationResult = validateAuthenticatedUser(locale);
        if (validationResult.hasErrors()) {
            return validationResult;
        }

        validationResult = validateReview(form.reviewId(), locale);
        if (validationResult.hasErrors()) {
            return validationResult;
        }

        Long userId = authService.getAuthenticatedUserId();
        Review review = reviewDao.getElementById(form.reviewId());

        if (!form.description().isBlank()) {
            addReviewMessage(review, userId, form.description());
        }

        review.setReviewerId(form.reviewerId());
        reviewDao.merge(review);

        String reviewLink = generateReviewLink(request, review.getId());
        sendReviewActionMail(form.reviewerId(), review.getQuestionnaire().getName(), locale, "assignReviewer", form.description(), form.personalMessage(), reviewLink);

        return successWithMessage("review.success.newReviewerAssigned", locale);
    }

    public ValidationResult processReview(Long reviewId, String description, ReviewStatus status, boolean isMainVersion, Locale locale) {

        ValidationResult validationResult = validateAuthenticatedUser(locale);
        if (validationResult.hasErrors()) {
            return validationResult;
        }

        validationResult = validateReview(reviewId, locale);
        if (validationResult.hasErrors()) {
            return validationResult;
        }

        Long userId = authService.getAuthenticatedUserId();
        Review review = reviewDao.getElementById(reviewId);

        if (!description.isBlank()) {
            addReviewMessage(review, userId, description);
        }

        updateReviewStatus(review, status, isMainVersion);
        return ValidationResult.SUCCESS;
    }

    private void addReviewMessage(Review review, Long userId, String description) {
        ReviewMessage reviewMessage = new ReviewMessage(review, userId, review.getEditorId(), description);
        reviewMessageDao.merge(reviewMessage);

        List<ReviewMessage> conversation = review.getConversation();
        conversation.add(reviewMessage);
        review.setConversation(conversation);
    }

    private void updateReviewStatus(Review review, ReviewStatus status, boolean isMainVersion) {
        review.setStatus(status);

        review.setUpdatedAt(Timestamp.from(Instant.now(clock)));

        if (status == ReviewStatus.APPROVED && isMainVersion) {
            questionnaireVersionGroupService.setMainVersionForGroup(review.getQuestionnaire());
            questionnaireService.approveQuestionnaire(review.getQuestionnaire());
        }
        reviewDao.merge(review);
    }

    public ReviewDTO getReviewById(Long reviewId) {
        Review review = reviewDao.getElementById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review with ID " + reviewId + " not found.");
        }

        ReviewDTO reviewDTO = reviewDTOMapper.apply(review);
        addUserDetailsInConversations(reviewDTO.getConversation());
        return reviewDTO;
    }

    private void addUserDetailsInConversations(List<ReviewMessageDTO> conversation) {
        Map<Long, UserDTO> userMap = userService.getAllUser().stream()
                .collect(Collectors.toMap(
                        UserDTO::getId,
                        user -> user,
                        (existing, replacement) -> existing
                ));

        for (ReviewMessageDTO reviewMessage : conversation) {
            UserDTO sender = userMap.get(reviewMessage.getSenderId());
            UserDTO receiver = userMap.get(reviewMessage.getReceiverId());

            if (sender != null) {
                reviewMessage.setSenderName(sender.getFirstname() + " " + sender.getLastname());
                reviewMessage.setSenderInitials(getInitials(sender.getFirstname(), sender.getLastname()));
            }

            if (receiver != null) {
                reviewMessage.setReceiverName(receiver.getFirstname() + " " + receiver.getLastname());
                reviewMessage.setReceiverInitials(getInitials(receiver.getFirstname(), receiver.getLastname()));
            }
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

    public ValidationResult validateReview(Long reviewId, Locale locale) {
        if (reviewId == null || reviewId <= 0) {
            return failureWithMessage(ValidationResult.INVALID_REVIEW_ID, locale);
        }

        Review review = reviewDao.getElementById(reviewId);
        if (review == null) {
            return failureWithMessage(ValidationResult.REVIEW_NOT_FOUND, locale);
        }

        return ValidationResult.SUCCESS;
    }

    private ValidationResult validateReviewAccess(Long reviewId, Locale locale) {
        ValidationResult authValidation = validateAuthenticatedUser(locale);
        if (authValidation.hasErrors()) return authValidation;


        Review review = reviewDao.getElementById(reviewId);
        if (review == null) {
            return failureWithMessage(ValidationResult.REVIEW_NOT_FOUND, locale);
        }

        if (authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)) {
            return ValidationResult.SUCCESS;
        }

        Long userId = authService.getAuthenticatedUserId();
        if (!Objects.equals(review.getEditorId(), userId) && !Objects.equals(review.getReviewerId(), userId)) {
            return failureWithMessage(ValidationResult.UNAUTHORIZED, locale);
        }

        return ValidationResult.SUCCESS;
    }

    /**
     * Checks whether the currently authenticated user has permission to modify a review.
     * <p>
     * A user can modify (delete) a review if:
     * - They are authenticated.
     * - They have the role of an admin or higher.
     * - They are the editor of the review.
     * <p>
     * This method is used as a security expression in `@PreAuthorize` annotations within controllers.
     *
     * @param reviewId The ID of the review to check permissions for.
     * @return `true` if the user has modification rights, otherwise `false`.
     */
    public boolean canModifyReview(Long reviewId) {
        Long currentUserId = authService.getAuthenticatedUserId();

        if (currentUserId == null) {
            LOGGER.warn("No authenticated user found");
            return false;
        }

        if (authService.hasRoleOrAbove(UserRole.ROLE_ADMIN)) {
            return true;
        }

        Review review = reviewDao.getElementById(reviewId);
        if (review == null) {
            LOGGER.warn("Review not found for ID: {}", reviewId);
            return false;
        }

        // Check if the user is the editor of the review
        return currentUserId.equals(review.getEditorId());
    }
}