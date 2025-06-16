package de.imi.mopat.io.importer.fhir;

import ca.uhn.fhir.parser.IParserErrorHandler;
import de.imi.mopat.model.enumeration.FhirVersion;

public interface FhirHelper {
    
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
