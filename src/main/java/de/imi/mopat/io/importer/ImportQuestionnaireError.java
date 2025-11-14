package de.imi.mopat.io.importer;

public class ImportQuestionnaireError {
    
    private final String errorCode;
    
    private final Object[] errorArguments;
    
    private final String defaultErrorMessage;
    
    public ImportQuestionnaireError(String errorCode, Object[] errorArguments, String defaultErrorMessage) {
        this.errorCode = errorCode;
        this.errorArguments = errorArguments;
        this.defaultErrorMessage = defaultErrorMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object[] getErrorArguments() {
        return errorArguments;
    }
    
    public String getDefaultErrorMessage() {
        return defaultErrorMessage;
    }
}
