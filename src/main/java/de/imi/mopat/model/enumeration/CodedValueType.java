package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of coded value types supported within MoPat 2.0
 */
public enum CodedValueType {
    STRING("STRING"), INTEGER("INTEGER"), FLOAT("FLOAT");
    private final String textValue;
    private static final Map<String, CodedValueType> stringToEnum = new HashMap<>();

    static // Initialize map from constant name to enum constant
    {
        for (CodedValueType cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    CodedValueType(final String textValue) {
        this.textValue = textValue;
    }

    public String getTextValue() {
        return textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }

    public static CodedValueType fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }
}
