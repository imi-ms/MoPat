package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of patient attributes that have to be listed for auditing purposes.
 */
public enum AuditPatientAttribute {

    CASE_NUMBER("CASE_NUMBER"), PATIENT_ID("PATIENT_ID"), FIRST_NAME("FIRST_NAME"), LAST_NAME(
        "LAST_NAME"), DATE_OF_BIRTH("DATE_OF_BIRTH"), GENDER("GENDER"), TREATMENT_DATA(
        "TREATMENT_DATA"), EMAIL_ADDRESS("EMAIL_ADDRESS"), MAIL_STATUS("MAIL_STATUS");
    private final String textValue;
    private static final Map<String, AuditPatientAttribute> stringToEnum = new HashMap<String, AuditPatientAttribute>();

    static // Initialize map from constant name to enum constant
    {
        for (AuditPatientAttribute cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    AuditPatientAttribute(final String textValue) {
        this.textValue = textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public static AuditPatientAttribute fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }
}