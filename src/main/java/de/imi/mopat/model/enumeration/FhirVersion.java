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
}
