package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of an export status for ODM files
 */
public enum ExportStatus {
    SUCCESS("SUCCESS"), CONFLICT("CONFLICT"), FAILURE("FAILURE");
    private final String textValue;
    private static final Map<String, ExportStatus> stringToEnum = new HashMap<String, ExportStatus>();

    static // Initialize map from constant name to enum constant
    {
        for (ExportStatus cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    ExportStatus(final String textValue) {
        this.textValue = textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public static ExportStatus fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }
}
