package de.imi.mopat.helper.controller;

import de.imi.mopat.model.conditions.Condition;

/**
 *
 */
public class ImportConditionResult {

    private String targetIdentifier = null;
    private String triggerIdentifier = null;
    private String triggerValue = null;
    private Condition condition = null;
    //private List<ValidationMessage> validationMessages = new
    // ArrayList<ValidationMessage>();

    /**
     * Default constructor.
     */
    public ImportConditionResult() {
    }

    /**
     * Constructor for ODM with XPATHSentence.
     *
     * @param xPathSentence the sentence to be splitted and parsed
     */
    public ImportConditionResult(final String xPathSentence) {
        //check if the sentence contains the required parameters
        if (xPathSentence.contains("ItemGroupData") && xPathSentence.contains("ItemData")
            && xPathSentence.toLowerCase().contains("value")) {
            //divide the sentence into sub-string divided by "/"
            String[] sentenceSplitted = xPathSentence.split("/");
            for (String currentSentence : sentenceSplitted) {
                //check if the current division contains "eq" or "="
                if (currentSentence.contains("eq") || currentSentence.contains("=")) {
                    //check if the current sentence is an ItemGroupData
                    if (currentSentence.contains("ItemGroupData")) {
                        String[] sentenceMoreSplitted = currentSentence.split("\"");
                        //we have split the sentence with ", but it could be
                        // that ' was used instead
                        if (sentenceMoreSplitted.length < 2) {
                            sentenceMoreSplitted = currentSentence.split("'");
                        }
                        //if the split worked, the length should be higher
                        // than 1
                        if (sentenceMoreSplitted.length > 1) {
                            //look for the sub-sentence that contains OID
                            for (int i = 0; i < sentenceMoreSplitted.length - 1; i++) {
                                if (sentenceMoreSplitted[i].contains("OID")) {
                                    this.targetIdentifier = sentenceMoreSplitted[i + 1];
                                    break;
                                }
                            }
                        }
                    } //same process as before
                    else if (currentSentence.contains("ItemData")) {
                        String[] sentenceMoreSplitted = currentSentence.split("\"");
                        if (sentenceMoreSplitted.length < 2) {
                            sentenceMoreSplitted = currentSentence.split("'");
                        }
                        if (sentenceMoreSplitted.length > 1 && currentSentence.contains(
                            "ItemOID")) {
                            for (int i = 0; i < sentenceMoreSplitted.length - 1; i++) {
                                if (sentenceMoreSplitted[i].contains("ItemOID")) {
                                    this.triggerIdentifier = sentenceMoreSplitted[i + 1];
                                    break;
                                }
                            }
                            if (currentSentence.toLowerCase().contains("value")) {
                                for (int i = 0; i < sentenceMoreSplitted.length - 1; i++) {
                                    if (sentenceMoreSplitted[i].toLowerCase().contains("value")) {
                                        this.triggerValue = sentenceMoreSplitted[i + 1];
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (currentSentence.toLowerCase().contains("value")
                        && this.getTriggerIdentifier() != null) {
                        String[] sentenceMoreSplitted = currentSentence.split("\"");
                        if (sentenceMoreSplitted.length < 2) {
                            sentenceMoreSplitted = currentSentence.split("'");
                        }
                        if (sentenceMoreSplitted.length > 1) {
                            for (int i = 0; i < sentenceMoreSplitted.length - 1; i++) {
                                if (sentenceMoreSplitted[i].toLowerCase().contains("value")) {
                                    this.triggerValue = sentenceMoreSplitted[i + 1];
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public String getTargetIdentifier() {
        return targetIdentifier;
    }

    public void setTargetIdentifier(final String targetIdentifier) {
        this.targetIdentifier = targetIdentifier;
    }

    public String getTriggerIdentifier() {
        return triggerIdentifier;
    }

    public void setTriggerIdentifier(final String itemR) {
        this.triggerIdentifier = itemR;
    }

    public String getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(final String item) {
        this.triggerValue = item;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(final Condition condition) {
        this.condition = condition;
    }
}
