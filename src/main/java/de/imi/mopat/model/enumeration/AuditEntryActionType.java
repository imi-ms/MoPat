package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of action types that are logged for auditing purposes.
 */
public enum AuditEntryActionType {
    READ("READ"),
    WRITE("WRITE"),
    CHANGE("CHANGE"),
    RECEIVED("RECEIVED"),
    SENT("SENT"),
    DELETE("DELETE");
    private static final Map<String, AuditEntryActionType> stringToEnum = new HashMap<String, AuditEntryActionType>();

    static // Initialize map from constant name to enum constant
    {
        for (AuditEntryActionType cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    private final String textValue;

    AuditEntryActionType(final String textValue) {
        this.textValue = textValue;
    }

    public static AuditEntryActionType fromString(String textValue) {
        return stringToEnum.get(textValue);
    }

    @Override
    public String toString() {
        return textValue;
    }

    public String getTextValue() {
        return textValue;
    }
}
