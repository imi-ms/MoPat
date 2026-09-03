package de.imi.mopat.error;


import org.springframework.http.HttpStatus;

public class EncounterSubmitException extends RuntimeException {

    private final HttpStatus status;

    public EncounterSubmitException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
