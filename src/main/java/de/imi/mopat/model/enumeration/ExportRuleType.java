package de.imi.mopat.model.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;

public enum ExportRuleType {

    ANSWER("ExportRuleAnswer"),
    ENCOUNTER("ExportRuleEncounter"),
    QUESTION("ExportRuleQuestion"),
    SCORE("ExportRuleScore");

    private static final Map<String, ExportRuleType> map = new HashMap<>(4);

    static {
        for (ExportRuleType cValue : values()) {
            map.put(cValue.type, cValue);
        }
    }

    private final String type;

    ExportRuleType(final String type) {
        this.type = type;
    }

    /**
     * Jackson factory method used to deserialize an {@link ExportRuleType} from a string
     * representation.
     * <p>
     * The provided {@code value} is interpreted as the serialized form of an enum constant (the
     * same string returned by {@link #toValue()}). The lookup is performed via the internal
     * {@code map} after normalizing the input using {@link StringUtils#lowerCase(String)}.
     * </p>
     *
     * @param value serialized value (typically the encounter getter/method name); may be
     *              {@code null}
     * @return the matching {@link ExportRuleType}, or {@code null} if {@code value} is
     * {@code null} or no matching entry exists
     */
    @JsonCreator
    public static ExportRuleType forValue(String value) {
        return map.get(value);
    }

    /**
     * Returns the type of the different encounter fields to be able to access every type in his own
     * manner.
     *
     * @return The type of the different encounter fields to be able to access every type in his own
     * manner.
     */
    public String getType() {
        return type;
    }

    /**
     * Jackson serialization method for this enum.
     * <p>
     * Returns the string representation used in JSON (the key under which this enum constant is
     * stored in the internal lookup map). This value is intended to round-trip with
     * {@link #forValue(String)} during (de)serialization.
     * </p>
     *
     * @return the serialized string value for this enum constant, or {@code null} if no mapping
     * exists
     */
    @JsonValue
    public String toValue() {
        for (Entry<String, ExportRuleType> entry : map.entrySet()) {
            if (entry.getValue() == this) {
                return entry.getKey();
            }
        }

        return null;
    }

}
