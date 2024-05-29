package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of question types supported within MoPat 2.0
 */
public enum QuestionType {

    MULTIPLE_CHOICE("MULTIPLE_CHOICE"), SLIDER("SLIDER"), NUMBER_CHECKBOX(
        "NUMBER_CHECKBOX"), NUMBER_CHECKBOX_TEXT("NUMBER_CHECKBOX_TEXT"), DROP_DOWN(
        "DROP_DOWN"), FREE_TEXT("FREE_TEXT"), INFO_TEXT("INFO_TEXT"), NUMBER_INPUT(
        "NUMBER_INPUT"), DATE("DATE"), IMAGE("IMAGE"), BODY_PART("BODY_PART"), BARCODE("BARCODE");
    private final String textValue;
    private static final Map<String, QuestionType> stringToEnum = new HashMap<String, QuestionType>();

    static // Initialize map from constant name to enum constant
    {
        for (QuestionType cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    QuestionType(final String textValue) {
        this.textValue = textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public static QuestionType fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }
}