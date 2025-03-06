package de.imi.mopat.helper.controller;

public enum ValidationResult {

    SUCCESS("review.success", "Action completed successfully."),
    NOT_AUTHENTICATED("review.error.user.notAuthenticated", "User not authenticated."),
    REVIEW_NOT_FOUND("review.error.notFound", "Review not found."),
    UNAUTHORIZED("review.error.unauthorized", "You are not authorized to perform this action."),
    INVALID_REVIEW_ID("review.error.invalidId", "Invalid reviewId."),
    REVIEW_ALREADY_EXISTS("review.error.questionnaire.alreadyUnderReview", "A review already exists for this questionnaire."),
    USER_NOT_FOUND("review.error.user.notFound", "User not found.");

    private final String code;
    private final String defaultMessage;
    private String localizedMessage;

    ValidationResult(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getLocalizedMessage() {
        return localizedMessage;
    }

    public void setLocalizedMessage(String localizedMessage) {
        this.localizedMessage = localizedMessage;
    }

    public boolean hasErrors() {
        return this != SUCCESS;
    }

    public boolean hasNoErrors() {
        return this == SUCCESS;
    }
}