package de.imi.mopat.helper.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.imi.mopat.model.Question;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupDef;

/**
 * TODO [bt] comment ImportQuestionResult.
 */
public class ImportQuestionListResult {

    private final List<ImportQuestionResult> importQuestionResults = new ArrayList<ImportQuestionResult>();
    private final List<ValidationMessage> validationMessages = new ArrayList<ValidationMessage>();
    //In ODM this identfier stands for itemGroupDefOID, in FHIR it stands for
    // the item's linkId which represents a question group
    private String identifier;

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
     * @param messageCode must not be <code>null</code>.
     */
    public void addValidationMessage(final String messageCode) {
        validationMessages.add(new ValidationMessage(messageCode, null));
    }

    /**
     * TODO [bt] comment the method getValidationMessages in
     * ImportQuestionResult !
     *
     * @return Returns the validation message
     */
    public List<ValidationMessage> getValidationMessages() {
        return validationMessages;
    }

    /**
     * TODO [bt] comment the method addImportQuestionResult in
     * ODMQuestionGroupResult !
     *
     * @param questionResult the {@link ImportQuestionResult} object, which will be added to this
     *                       {@link ImportQuestionListResult}
     */
    public void addImportQuestionResult(final ImportQuestionResult questionResult) {
        importQuestionResults.add(questionResult);
    }

    /**
     * TODO [bt] comment the method getImportQuestionResults in
     * ODMQuestionGroupResult !
     *
     * @return all {@link ImportQuestionResult} objects of the current
     * {@link ImportQuestionListResult} object.
     */
    public List<ImportQuestionResult> getImportQuestionResults() {
        return importQuestionResults;
    }

    /**
     * Sets the ItemGroupDef OID for ODM or the item's linkId for FHIR with the incoming string
     *
     * @param itemGroupDefOID The ItemGroupDef OID for ODM or the item's linkId for FHIR with the
     *                        incoming string to set.
     */
    public void setIdentifier(final String itemGroupDefOID) {
        this.identifier = itemGroupDefOID;
    }

    /**
     * returns the identifier.
     *
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return an unmodifiable {@link List} (see {@link Collections#unmodifiableList(List)}) of all
     * {@link Question Questions} converted in the given Question Group (i.e.
     * {@link ODMcomplexTypeDefinitionItemGroupDef ItemGroupDef}). Is never
     * <code>null</code>, might be empty.
     */
    public List<Question> getQuestionList() {
        List<Question> result = new ArrayList<Question>();

        for (ImportQuestionResult importQuestionResult : importQuestionResults) {
            Question question = importQuestionResult.getQuestion();
            if (question != null) {
                result.add(question);
            }
        }

        return Collections.unmodifiableList(result);
    }

    public ImportQuestionResult getQuestionResultByIdentifier(final String identifier) {
        for (ImportQuestionResult importQuestionResult : this.importQuestionResults) {
            if (importQuestionResult.getIdentifier() != null && importQuestionResult.getIdentifier()
                .equals(identifier)) {
                return importQuestionResult;
            }
        }
        return null;
    }
}
