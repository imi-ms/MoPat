package de.imi.mopat.model;

public class OdmValidationResult {
    private final boolean valid;
    private final String errorMessage;

    public OdmValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static OdmValidationResult success() {
        return new OdmValidationResult(true, null);
    }

    public static OdmValidationResult failure(String errorMessage) {
        return new OdmValidationResult(false, errorMessage);
    }
}

