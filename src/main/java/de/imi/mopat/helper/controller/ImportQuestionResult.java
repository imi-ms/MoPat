package de.imi.mopat.helper.controller;

import java.util.ArrayList;
import java.util.List;

import de.imi.mopat.model.Question;

/**
 * TODO [bt] comment ImportQuestionResult
 */
public class ImportQuestionResult {

    //In ODM this identifier stands for oID, in FHIR it stands for the item's
    // linkId representing the question
    private String identifier;
    private Question question;
    private final List<ValidationMessage> validationMessages = new ArrayList<>();
    private final ArrayList<ImportConditionResult> conditions = new ArrayList<>();

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
     * TODO [bt] comment the method addValidationMessage in
     * ImportQuestionResult !
     *
     * @param messageCode the message to be added to the validation messages
     */
    public void addValidationMessage(final String messageCode) {
        validationMessages.add(new ValidationMessage(messageCode, null));
    }

    /**
     * TODO [bt] comment the method addValidationMessage in
     * ImportQuestionResult !
     *
     * @param validationMessage The validation message to be added to this conversion result.
     */
    public void addValidationMessage(final ValidationMessage validationMessage) {
        validationMessages.add(validationMessage);
    }

    /**
     * TODO [bt] comment the method getValidationMessages in
     * ImportQuestionResult !
     *
     * @return a list of validation messages
     */
    public List<ValidationMessage> getValidationMessages() {
        return validationMessages;
    }

    /**
     * Sets the atribute question with the incoming Question.
     *
     * @param question The incoming question
     */
    public void setQuestion(final Question question) {
        this.question = question;
    }

    /**
     * Returns the MoPat question.
     *
     * @return MoPat question
     */
    public Question getQuestion() {
        return question;
    }

    /**
     * Sets the atribute oID with the incoming String.
     *
     * @param identifier The incoming String
     */
    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the oID for this (ODM) question.
     *
     * @return oID
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Adds a new {@link ImportConditionResult} object to this {@link ImportQuestionResult}.
     *
     * @param condition The {@link ImportConditionResult} object, which will be added to this
     *                  {@link ImportQuestionResult}. Must not be
     *                  <code>null</code>.
     */
    public void addCondition(final ImportConditionResult condition) {
        this.conditions.add(condition);
    }

    /**
     * Adds all given {@link ImportConditionResult} objects that are not already associated with
     * this {@link ImportQuestionResult}.
     *
     * @param conditions The list of {@link ImportConditionResult} objects, which will be added to
     *                   this {@link ImportQuestionResult}. Must not be
     *                   <code>null</code>.
     */
    public void addConditions(final List<ImportConditionResult> conditions) {
        this.conditions.addAll(conditions);
    }

    /**
     * Returns all {@link ImportConditionResult} objects of the current {@link ImportQuestionResult}
     * object.
     *
     * @return conditions The current {@link ImportConditionResult} objects of this
     * {@link ImportQuestionResult} object. Is never <code>null</code>. Might be empty.
     */
    public ArrayList<ImportConditionResult> getConditions() {
        return conditions;
    }

}
