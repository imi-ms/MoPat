package de.imi.mopat.model.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;

/**
 * Contains all encounter fields which are used in export template mapping. Every entry represents
 * an attribute in the {@link de.imi.mopat.model.Encounter} model and makes them assignable as a
 * value in the export process.
 */
public enum ExportEncounterFieldType {

    PATIENT_ID("getPatientID", "java.lang.Long"), CASE_NUMBER("getCaseNumber",
        "java.lang.String"), START_TIME("getStartTime", "java.sql.Timestamp"), LANGUAGE(
        "getBundleLanguage", "java.lang.String");

    private static final Map<String, ExportEncounterFieldType> map = new HashMap<>(4);

    static {
        for (ExportEncounterFieldType cValue : values()) {
            map.put(cValue.methodName, cValue);
        }
    }

    private final String methodName;
    private final String type;

    ExportEncounterFieldType(final String methodName, final String type) {
        this.methodName = methodName;
        this.type = type;
    }

    /**
     * Jackson factory method used to deserialize an {@link ExportEncounterFieldType} from a string
     * representation.
     * <p>
     * The provided {@code value} is interpreted as the serialized form of an enum constant (the
     * same string returned by {@link #toValue()}). The lookup is performed via the internal
     * {@code map} after normalizing the input using {@link StringUtils#lowerCase(String)}.
     * </p>
     *
     * @param value serialized value (typically the encounter getter/method name); may be
     *              {@code null}
     * @return the matching {@link ExportEncounterFieldType}, or {@code null} if {@code value} is
     * {@code null} or no matching entry exists
     */
    @JsonCreator
    public static ExportEncounterFieldType forValue(String value) {
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
     * Returns the method name from the encounter field to be able to access the value of an
     * {@link de.imi.mopat.model.Encounter} object.
     *
     * @return The method name to the encounter field to be able to access the value of an
     * {@link de.imi.mopat.model.Encounter} object.
     */
    public String getMethodName() {
        return methodName;
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
        for (Entry<String, ExportEncounterFieldType> entry : map.entrySet()) {
            if (entry.getValue() == this) {
                return entry.getKey();
            }
        }

        return null;
    }
}
