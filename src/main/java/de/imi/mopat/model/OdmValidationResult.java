package de.imi.mopat.model;

public record OdmValidationResult(boolean valid, String errorMessage) {

    public static OdmValidationResult success() {
        return new OdmValidationResult(true, null);
    }

    public static OdmValidationResult failure(String errorMessage) {
        return new OdmValidationResult(false, errorMessage);
    }
}
