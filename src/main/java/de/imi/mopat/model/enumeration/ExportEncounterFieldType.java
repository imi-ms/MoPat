package de.imi.mopat.model.enumeration;

/**
 * Contains all encounter fields which are used in export template mapping. Every entry represents
 * an attribute in the {@link de.imi.mopat.model.Encounter} model and makes them assignable as a
 * value in the export process.
 */
public enum ExportEncounterFieldType {

    PATIENT_ID("getPatientID", "java.lang.Long"), CASE_NUMBER("getCaseNumber",
        "java.lang.String"), START_TIME("getStartTime", "java.sql.Timestamp"), LANGUAGE(
        "getBundleLanguage", "java.lang.String");

    private final String methodName;
    private final String type;

    ExportEncounterFieldType(final String methodName, final String type) {
        this.methodName = methodName;
        this.type = type;
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
}
