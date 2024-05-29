/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of mail status of a encounterScheduled.
 */
public enum EncounterScheduledMailStatus {
    ACTIVE("ACTIVE"), ADDRESS_REJECTED("ADDRESS_REJECTED"), CONSENT_PENDING(
        "CONSENT_PENDING"), DEACTIVATED_PATIENT(
        "DEACTIVATED_PATIENT"), DEACTIVATED_ENCOUNTER_MANAGER(
        "DEACTIVATED_ENCOUNTER_MANAGER"), INTERRUPTED("INTERRUPTED");

    private final String textValue;
    private static final Map<String, EncounterScheduledMailStatus> stringToEnum = new HashMap<String, EncounterScheduledMailStatus>();

    EncounterScheduledMailStatus(final String textValue) {
        this.textValue = textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public static EncounterScheduledMailStatus fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }
}
