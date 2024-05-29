package de.imi.mopat.helper.controller;

/**
 * TODO [bt] comment ValidationMessage.
 */
public class ValidationMessage {

    private String messageCode;
    private String[] parameters;

    public ValidationMessage(final String messageCode, final String[] parameters) {
        setMessageCode(messageCode);
        setParameters(parameters);
    }

    /**
     * @return the message
     */
    public String getMessageCode() {
        return messageCode;
    }

    /**
     * @param messageCode the message to set
     */
    private void setMessageCode(final String messageCode) {
        this.messageCode = messageCode;
    }

    /**
     * @return the parameters
     */
    public String[] getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    private void setParameters(final String[] parameters) {
        this.parameters = parameters;
    }
}
