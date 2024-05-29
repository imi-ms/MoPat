package de.imi.mopat.model.enumeration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Definition of export date format types. Determines how a date should be formatted. Used in
 * {@link de.imi.mopat.model.ExportRuleFormat} objects.
 */
public enum ExportDateFormatType {

    YYYY_MM_DD("yyyy-MM-dd"), DD_MM_YY("dd.MM.yy");

    private final String format;
    private final Calendar calendar = new GregorianCalendar(2014, 9, 22);

    ExportDateFormatType(final String format) {
        this.format = format;
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
}
