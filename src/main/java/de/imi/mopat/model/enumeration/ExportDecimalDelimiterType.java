package de.imi.mopat.model.enumeration;

/**
 * Definition of export decimal delimiter types. Determines the decimal delimiter of a floating
 * number. Used in {@link de.imi.mopat.model.ExportRuleFormat} objects.
 */
public enum ExportDecimalDelimiterType {

    DOT('.'), COMMA(',');

    private final char delimiter;

    ExportDecimalDelimiterType(final char delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Returns the current decimal delimiter which will be used for floating point numbers.
     *
     * @return The current decimal delimiter which will be used for floating point numbers.
     */
    public char getDelimiter() {
        return delimiter;
    }
}
