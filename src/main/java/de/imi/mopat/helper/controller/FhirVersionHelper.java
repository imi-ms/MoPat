package de.imi.mopat.helper.controller;

import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.model.enumeration.FhirVersion;
import org.springframework.stereotype.Component;

/**
 * Utility class for handling mappings and checks between FHIR-related enums
 * such as ExportTemplateType and FhirVersion. Provides helper methods
 * to facilitate conversions and logical checks within the FHIR export domain.
 */
@Component
public class FhirVersionHelper {

    /**
     * Helper function to map the export templatr type to the internal
     * FhirVersion enum.
     * @param exportTemplateType to map
     * @return FhirVersion enum
     */
    public FhirVersion mapExportTemplateTypeToFhirVersion(ExportTemplateType exportTemplateType) {
        switch (exportTemplateType) {
            case FHIR_DSTU3 -> {
                return FhirVersion.DSTU3;
            }
            case FHIR_R4B -> {
                return FhirVersion.R4B;
            }
            case FHIR_R5 -> {
                return FhirVersion.R5;
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Helper function to map the internal FhirVersion enum to the ExportTemplateType.
     * @param fhirVersion to map
     * @return ExportTemplateType or null if no match is found
     */
    public ExportTemplateType mapFhirVersionToExportTemplateType(FhirVersion fhirVersion) {
        switch (fhirVersion) {
            case DSTU3 -> {
                return ExportTemplateType.FHIR_DSTU3;
            }
            case R4B -> {
                return ExportTemplateType.FHIR_R4B;
            }
            case R5 -> {
                return ExportTemplateType.FHIR_R5;
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Maps the String representation of the FHIR version to the internal enum.
     * @param fhirVersion to map
     * @return FhirVersion enum or null if no match is found
     */
    public FhirVersion mapFrontendFhirStringToVersion(String fhirVersion) {
        return switch (fhirVersion) {
            case "FHIRDSTU3" -> FhirVersion.DSTU3;
            case "FHIRR4B" -> FhirVersion.R4B;
            case "FHIRR5" -> FhirVersion.R5;
            default -> null;
        };
    }


    /**
     * Checks if the given export template type is a FHIR-related export type.
     *
     * @param exportTemplateType the export template type to check; must not be null
     * @return true if the export template type is one of FHIR_... types, false otherwise
     */
    public boolean isFhirExportType(ExportTemplateType exportTemplateType) {
        if (exportTemplateType == ExportTemplateType.FHIR_DSTU3 || exportTemplateType == ExportTemplateType.FHIR_R4B) {
            return true;
        } else {
            return false;
        }
    }


}
