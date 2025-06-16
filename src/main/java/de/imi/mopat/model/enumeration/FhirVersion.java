package de.imi.mopat.model.enumeration;

/**
 * A helper enum to determine the FHIR Version to use
 */
public enum FhirVersion {

    DSTU3("DSTU3"),
    R4B("R4B"),
    R5("R5");


    private final String versionString;

    FhirVersion(String versionString) {
        this.versionString = versionString;
    }
    
    /**
     * Helper Function to transform a ExportTemplateType
     * to a FhirVersion Enum
     * @param exportTemplateType to get the FhirVersion for
     * @return FhirVersion, null if none can be found
     */
    public static FhirVersion getVersionForExportTemplateType(ExportTemplateType exportTemplateType) {
        switch (exportTemplateType) {
            case FHIR_DSTU3 -> {
                return FhirVersion.DSTU3;
            }
            case FHIR_R4B -> {
                return FhirVersion.R4B;
            }
            
            default -> {
                return null;
            }
        }
    }
}
