package de.imi.mopat.io.importer;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple class to hold the results of the validation process
 * so that Spring's separation of concerns is kept
 */
public class ImportQuestionnaireValidation {
    private final List<ImportQuestionnaireError> validationErrors;
    private ImportQuestionnaireResult importResult;
    
    public ImportQuestionnaireValidation() {
        this.validationErrors = new ArrayList<>();
    }
    
    /**
     * Returns the list of validationsErrors
     * @return List<ImportQuestionnaireError> validationErrors
     */
    public List<ImportQuestionnaireError> getValidationErrors() {
        return validationErrors;
    }
    
    /**
     * Checks if the list contains any errors
     * @return true if list has entries, false otherwise
     */
    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }
    
    /**
     * Add a new error to the list with the given
     * error code and its arguments
     * @param errorCode to define the error with
     * @param errorArguments For the error code
     * @param defaultErrorMessage To show for the error
     */
    public void reject(
        String errorCode,
        Object[] errorArguments,
        String defaultErrorMessage
    ) {
        this.validationErrors.add(new ImportQuestionnaireError(
            errorCode,
            errorArguments,
            defaultErrorMessage
        ));
    }
    
    /**
     * Overloaded method to add a new error
     * by only giving the error code
     * @param errorCode
     */
    public void reject(
        String errorCode
    ) {
        this.validationErrors.add(new ImportQuestionnaireError(
            errorCode,
            null,
            null
        ));
    }
    
    /**
     * Getter for importResult
     * @return importResult
     */
    public ImportQuestionnaireResult getImportResult() {
        return importResult;
    }
    
    /**
     * Setter for importResult
     * @param importResult ImportQuestionnaireResult
     */
    public void setImportResult(ImportQuestionnaireResult importResult) {
        this.importResult = importResult;
    }
}
