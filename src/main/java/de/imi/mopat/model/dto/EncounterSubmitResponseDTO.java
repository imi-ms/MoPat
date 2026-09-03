package de.imi.mopat.model.dto;

public class EncounterSubmitResponseDTO {

    private boolean success;
    private String status;
    private String message;

    public EncounterSubmitResponseDTO() {
    }

    public EncounterSubmitResponseDTO(boolean success, String status, String message) {
        this.success = success;
        this.status = status;
        this.message = message;
    }

    public static EncounterSubmitResponseDTO stored() {
        return new EncounterSubmitResponseDTO(true, "STORED", null);
    }

    public static EncounterSubmitResponseDTO ignored(String message) {
        return new EncounterSubmitResponseDTO(false, "IGNORED", message);
    }

    public static EncounterSubmitResponseDTO failed(String message) {
        return new EncounterSubmitResponseDTO(false, "FAILED", message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}