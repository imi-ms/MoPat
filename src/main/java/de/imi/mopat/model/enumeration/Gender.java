package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of gender supported within MoPat 2.0.
 */
public enum Gender {
    MALE("MALE"),
    FEMALE("FEMALE");
    private static final Map<String, Gender> stringToEnum = new HashMap<String, Gender>();

    static // Initialize map from constant name to enum constant
    {
        for (Gender cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    private final String textValue;

    Gender(final String textValue) {
        this.textValue = textValue;
    }

    public static Gender fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }

    public String getTextValue() {
        return textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }
}
