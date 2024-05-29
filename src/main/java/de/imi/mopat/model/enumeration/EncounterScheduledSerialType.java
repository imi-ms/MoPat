package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of encounter scheduled serial types supported within MoPat 2.0.
 */
public enum EncounterScheduledSerialType {

    UNIQUELY("UNIQUELY"), REPEATEDLY("REPEATEDLY"), WEEKLY("WEEKLY"), MONTHLY("MONTHLY");

    private final String textValue;
    private static final Map<String, EncounterScheduledSerialType> stringToEnum = new HashMap<String, EncounterScheduledSerialType>();

    static // Initialize map from constant name to enum constant
    {
        for (EncounterScheduledSerialType cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    EncounterScheduledSerialType(final String textValue) {
        this.textValue = textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public static EncounterScheduledSerialType fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }
}
