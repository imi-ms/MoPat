package de.imi.mopat.model.enumeration;

import de.imi.mopat.controller.strategy.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of question types supported within MoPat 2.0
 */
public enum QuestionType {

    MULTIPLE_CHOICE("MULTIPLE_CHOICE", new MultipleChoiceOrDropdownStrategy()),
    SLIDER("SLIDER", new SliderOrNumCheckBoxStrategy()),
    NUMBER_CHECKBOX("NUMBER_CHECKBOX", new SliderOrNumCheckBoxStrategy()),
    NUMBER_CHECKBOX_TEXT("NUMBER_CHECKBOX_TEXT", new NumberCheckBoxTextStrategy()),
    DROP_DOWN("DROP_DOWN", new MultipleChoiceOrDropdownStrategy()),
    FREE_TEXT("FREE_TEXT", new FreeTextOrBarcodeStrat()),
    INFO_TEXT("INFO_TEXT", new DoNothing()),
    NUMBER_INPUT("NUMBER_INPUT", new NumberInputStrat()),
    DATE("DATE", new DateStrat()),
    IMAGE("IMAGE", new ImageStrat()),
    BODY_PART("BODY_PART", new BodyPartStrategy()),
    BARCODE("BARCODE", new FreeTextOrBarcodeStrat());
    private final String textValue;
    private static final Map<String, QuestionType> stringToEnum = new HashMap<String, QuestionType>();

    static // Initialize map from constant name to enum constant
    {
        for (QuestionType cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    private final CreateOrUpdateAnswerStrategy strategy;

    QuestionType(final String textValue, final CreateOrUpdateAnswerStrategy strategy) {
        this.textValue = textValue;
        this.strategy = strategy;
    }

    public CreateOrUpdateAnswerStrategy getStrategy() {
        return this.strategy;
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