package de.imi.mopat.helper.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.imi.mopat.model.Questionnaire;

/**
 * TODO [bt] comment ODMQuestionResult
 */
public class ImportQuestionnaireResult {

    private Questionnaire questionnaire;
    private final List<ValidationMessage> validationMessages = new ArrayList<ValidationMessage>();
    private final List<ImportQuestionListResult> importQuestionListResults = new ArrayList<ImportQuestionListResult>();

    /**
     * TODO.
     *
     * @return the Questionnaire
     */
    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    /**
     * @param questionnaire The questionnaire to set
     */
    public void setQuestionnaire(final Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
    }

    /**
     * Adds a validation message, together with it's arguments (to fill the placeholders) to this
     * conversion result.
     *
     * @param messageCode must not be <code>null</code>.
     * @param parameters  can be <code>null</code>.
     */
    public void addValidationMessage(final String messageCode, final String[] parameters) {
        validationMessages.add(new ValidationMessage(messageCode, parameters));
    }

    /**
     * TODO [bt] comment the method addValidationMessage in ODMQuestionResult !
     *
     * @param messageCode The validation message to add. Must not be
     *                    <code>null</code>.
     */
    public void addValidationMessage(final String messageCode) {
        validationMessages.add(new ValidationMessage(messageCode, null));
    }

    /**
     * TODO [bt] comment the method addImportQuestionListResult in
     * ODMQuestionGroupResult !
     *
     * @param questionListResult The {ImportQuestionListResult} to add.
     */
    public void addImportQuestionListResult(final ImportQuestionListResult questionListResult) {
        importQuestionListResults.add(questionListResult);
    }

    /**
     * TODO [bt] comment the method getValidationMessages in
     * ODMQuestionGroupResult !
     *
     * @return a List of the validation messages
     */
    public List<ValidationMessage> getValidationMessages() {
        return validationMessages;
    }

    /**
     * @return an unmodifiable List (see {@link Collections#unmodifiableList(List)}) of all
     * {@link ImportQuestionListResult ODMQuestionListResults} of this
     * {@link ImportQuestionnaireResult}.
     */
    public List<ImportQuestionListResult> getQuestionListResults() {

        return Collections.unmodifiableList(importQuestionListResults);
    }

    public int getNumberOfQuestions() {
        int result = 0;

        for (ImportQuestionListResult importQuestionListResult : importQuestionListResults) {
            result = result + importQuestionListResult.getQuestionList().size();
        }
        return result;
    }
}
