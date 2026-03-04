package de.imi.mopat.model.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;

/**
 * Definition of export date format types. Determines how a date should be formatted. Used in
 * {@link de.imi.mopat.model.ExportRuleFormat} objects.
 */
public enum ExportDateFormatType {

    YYYY_MM_DD("yyyy-MM-dd"), DD_MM_YY("dd.MM.yy");

    private static final Map<String, ExportDateFormatType> map = new HashMap<>(2);

    static {
        for (ExportDateFormatType cValue : values()) {
            map.put(cValue.format, cValue);
        }
    }

    private final String format;
    private final Calendar calendar = new GregorianCalendar(2014, 9, 22);

    ExportDateFormatType(final String format) {
        this.format = format;
    }

    /**
     * Jackson factory method used to deserialize an {@link ExportDateFormatType} from a string
     * representation.
     * <p>
     * The provided {@code value} is interpreted as the serialized form of an enum constant (the
     * same string returned by {@link #toValue()}). The lookup is performed via the internal
     * {@code map} after normalizing the input using {@link StringUtils#lowerCase(String)}.
     * </p>
     *
     * @param value serialized value (typically the encounter getter/method name); may be
     *              {@code null}
     * @return the matching {@link ExportDateFormatType}, or {@code null} if {@code value} is
     * {@code null} or no matching entry exists
     */
    @JsonCreator
    public static ExportDateFormatType forValue(String value) {
        return map.get(value);
    }

    /**
     * Returns the format, which is compatibel with the SimpleDateFormat.
     *
     * @return Returns the format, which is compatibel with the SimpleDateFormat.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Return a preview of the format with a default time.
     *
     * @return Return a preview of the format with a default time.
     */
    public String getFormatPreview() {
        return new SimpleDateFormat(format).format(calendar.getTime());
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
        for (Entry<String, ExportDateFormatType> entry : map.entrySet()) {
            if (entry.getValue() == this) {
                return entry.getKey();
            }
        }

        return null;
    }
}
