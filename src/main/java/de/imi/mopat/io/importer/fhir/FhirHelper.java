package de.imi.mopat.io.importer.fhir;

import ca.uhn.fhir.validation.SingleValidationMessage;
import de.imi.mopat.io.importer.ImportQuestionnaireValidation;
import de.imi.mopat.model.enumeration.FhirVersion;

public abstract class FhirHelper {

    public static void addDefaultError(ImportQuestionnaireValidation result,
        SingleValidationMessage message) {
        result.reject(
            "import.fhir.validation.error.detailed",
            new Object[]{
                message.getSeverity().getCode(),
                message.getLocationLine(),
                message.getLocationString(),
                message.getMessage()
            },
            String.format(
                "An error occurred during validation: Severity: %s, Line: %s, Location: %s, Message: %s",
                message.getSeverity().getCode(),
                message.getLocationLine(),
                message.getLocationString(),
                message.getMessage()
            )

        );
    }

    class Builder {

        private FhirVersion version;

        public Builder version(FhirVersion version) {
            this.version = version;
            return this;
        }


        public FhirHelper build() {
            switch (version) {
                case DSTU3 -> {
                    return new FhirDstu3Helper();
                }
                case R4B -> {
                    return new FhirR4bHelper();
                }
                case R5 -> {
                    //TODO
                }
            }
            return null;
        }


    }

}
