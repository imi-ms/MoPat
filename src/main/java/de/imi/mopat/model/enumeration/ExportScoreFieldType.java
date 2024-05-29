package de.imi.mopat.model.enumeration;

/**
 * Contains all score fields which are used in export template mapping. Every entry represents a
 * method in the {@link de.imi.mopat.model.score.Score} model and makes them assignable as a value
 * in the export process.
 */
public enum ExportScoreFieldType {

    VALUE("evaluate", "java.lang.Object"), FORMULA("getFormula", "java.lang.String");

    private final String methodName;
    private final String type;

    ExportScoreFieldType(final String methodName, final String type) {
        this.methodName = methodName;
        this.type = type;
    }

    /**
     * Returns the type of the different score fields to be able to access every type in his own
     * manner.
     *
     * @return The type of the different score fields to be able to access every type in his own
     * manner.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the method name from the score field to be able to access the value of an
     * {@link de.imi.mopat.model.score.Score} object.
     *
     * @return The method name to the score field to be able to access the value of an
     * {@link de.imi.mopat.model.score.Score} object.
     */
    public String getMethodName() {
        return methodName;
    }
}
