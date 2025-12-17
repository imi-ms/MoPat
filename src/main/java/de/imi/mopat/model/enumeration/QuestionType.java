package de.imi.mopat.model.enumeration;

import de.imi.mopat.controller.strategy.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of question types supported within MoPat 2.0
 */
public enum QuestionType {

    MULTIPLE_CHOICE("MULTIPLE_CHOICE", new MultipleChoiceOrDropdownStrategy(), "/images/form-svgs/multipleChoice.svg"),
    SLIDER("SLIDER", new SliderOrNumCheckBoxStrategy(), "/images/form-svgs/slider.svg"),
    NUMBER_CHECKBOX("NUMBER_CHECKBOX", new SliderOrNumCheckBoxStrategy(), "/images/form-svgs/numbered.svg"),
    NUMBER_CHECKBOX_TEXT("NUMBER_CHECKBOX_TEXT", new NumberCheckBoxTextStrategy(), "/images/form-svgs/numbered.svg"),
    DROP_DOWN("DROP_DOWN", new MultipleChoiceOrDropdownStrategy(), "/images/form-svgs/dropdown.svg"),
    FREE_TEXT("FREE_TEXT", new FreeTextOrBarcodeStrat(), "/images/form-svgs/text.svg"),
    INFO_TEXT("INFO_TEXT", new DoNothing(), "/images/form-svgs/info.svg"),
    NUMBER_INPUT("NUMBER_INPUT", new NumberInputStrat(), "/images/form-svgs/numbers.svg"),
    DATE("DATE", new DateStrat(), "/images/form-svgs/date.svg"),
    IMAGE("IMAGE", new ImageStrat(), "/images/form-svgs/image.svg"),
    BODY_PART("BODY_PART", new BodyPartStrategy(), "/images/form-svgs/body.svg"),
    BARCODE("BARCODE", new FreeTextOrBarcodeStrat(), "/images/form-svgs/barcode.svg");
    private final String textValue;
    private static final Map<String, QuestionType> stringToEnum = new HashMap<String, QuestionType>();

    static // Initialize map from constant name to enum constant
    {
        for (QuestionType cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    private final CreateOrUpdateAnswerStrategy strategy;
    private final String iconPath;

    QuestionType(final String textValue, final CreateOrUpdateAnswerStrategy strategy, String iconPath) {
        this.textValue = textValue;
        this.strategy = strategy;
        this.iconPath = iconPath;
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

    public String getIconPath() {
        return iconPath;
    }
}