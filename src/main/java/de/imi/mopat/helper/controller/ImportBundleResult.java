package de.imi.mopat.helper.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.imi.mopat.model.Bundle;

/**
 * TODO [bt] comment ODMQuestionResult.
 */
public class ImportBundleResult {

    private Bundle bundle;
    private final List<ValidationMessage> validationMessages = new ArrayList<>();
    private final List<ImportQuestionnaireResult> importQuestionnaireResults = new ArrayList<>();

    /**
     * Returns the {@link Bundle} for this {@link ImportBundleResult}.
     *
     * @return The {@link Bundle} for this {@link ImportBundleResult}.
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * @param bundle Sets the {@link Bundle} for this {@link ImportBundleResult}.
     */
    public void setBundle(final Bundle bundle) {
        this.bundle = bundle;
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
     * @param messageCode must not be <code>null</code>.
     */
    public void addValidationMessage(final String messageCode) {
        validationMessages.add(new ValidationMessage(messageCode, null));
    }

    /**
     * TODO [bt] comment the method addImportQuestionnaireResult in
     * ImportBundleResult
     * !
     *
     * @param questionnaireResult the {ImportQuestionnaireResult} to add.
     */
    public void addImportQuestionnaireResult(final ImportQuestionnaireResult questionnaireResult) {
        importQuestionnaireResults.add(questionnaireResult);
    }

    /**
     * @return an unmodifiable List (see {@link Collections#unmodifiableList(List)}) of all
     * {@link ImportQuestionnaireResult ODMQuestionnaireResults} of this
     * {@link ImportBundleResult}.
     */
    public List<ImportQuestionnaireResult> getQuestionnaireResults() {

        return Collections.unmodifiableList(importQuestionnaireResults);
    }
}
