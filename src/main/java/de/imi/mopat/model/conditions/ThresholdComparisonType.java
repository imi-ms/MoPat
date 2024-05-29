package de.imi.mopat.model.conditions;

import java.util.HashMap;
import java.util.Map;

import de.imi.mopat.model.SliderAnswer;

/**
 * Definitions of comparison operators that are utilized to compare an actual value of a
 * {@link ConditionTrigger}, e.g. a {@link SliderAnswer}, against a
 * {@link SliderAnswerThresholdCondition#getThreshold() SliderAnswerThresholdCondition's
 * threshold}.
 *
 * @since v1.2
 */
public enum ThresholdComparisonType {

    SMALLER_THAN("<"), SMALLER_THAN_EQUALS("<="), EQUALS("="), BIGGER_THAN_EQUALS(
        ">="), BIGGER_THAN(">"), NOT_EQUALS("!=");
    private String textValue;
    private static final Map<String, ThresholdComparisonType> stringToEnum = new HashMap<String, ThresholdComparisonType>();

    static // Initialize map from constant name to enum constant
    {
        for (ThresholdComparisonType cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    ThresholdComparisonType(final String textValue) {
        this.textValue = textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public static ThresholdComparisonType fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }
}
