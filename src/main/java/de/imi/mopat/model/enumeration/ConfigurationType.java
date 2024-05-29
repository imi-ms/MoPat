package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of configuration types that are available for a configuration entry.
 */
public enum ConfigurationType {

    // Locale path on the server. Application must have read and write rights.
    LOCAL_PATH("LOCAL_PATH"), // Path on the web. Must be reachable.
    WEB_PATH("WEB_PATH"), // Integer. Value will be casted into integer.
    INTEGER("INTEGER"), // Long. Value will be casted into long.
    LONG("LONG"), // Double. Value will be casted into double.
    DOUBLE("DOUBLE"), // String. Value can contain every character available
    // in UTF8.
    STRING("STRING"), // The value will be validated against the given pattern.
    PATTERN("PATTERN"), // Path to the image. Application must have read and
    // write rights.
    IMAGE("IMAGE"), // Boolean. Value will be casted into boolean.
    BOOLEAN("BOOLEAN"), // Select. Value is one of the given select options.
    SELECT("SELECT"), // String. Value can contain every character available
    // in UTF8.
    PASSWORD("PASSWORD"), // Path to the File. Application must have read and
    // write rights
    FILE("FILE");

    private final String textValue;

    ConfigurationType(final String textValue) {
        this.textValue = textValue;
    }

    private static final Map<String, ConfigurationType> stringToEnum = new HashMap<String, ConfigurationType>();

    static // Initialize map from constant name to enum constant
    {
        for (ConfigurationType cValue : values()) {
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

    public static ConfigurationType fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }
}