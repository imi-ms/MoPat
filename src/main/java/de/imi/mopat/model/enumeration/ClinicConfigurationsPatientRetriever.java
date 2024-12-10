package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of ClinicConfigurationsPatientRetriever types that are available for a clinic.
 */
public enum ClinicConfigurationsPatientRetriever {

    usePatientDataLookup("usePatientDataLookup"),
    registerPatientData("registerPatientData"),
    usePseudonymizationService("usePseudonymizationService");


    private final String textValue;

    ClinicConfigurationsPatientRetriever(final String textValue) {
        this.textValue = textValue;
    }

    private static final Map<String, ClinicConfigurationsPatientRetriever> stringToEnum = new HashMap<String, ClinicConfigurationsPatientRetriever>();

    static // Initialize map from constant name to enum constant
    {
        for (ClinicConfigurationsPatientRetriever cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    @Override
    public String toString() {
        return textValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public static ClinicConfigurationsPatientRetriever fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }
}