package de.imi.mopat.io.importer.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParserErrorHandler;
import de.imi.mopat.helper.controller.ValidationMessage;
import de.imi.mopat.io.importer.ImportQuestionnaireResult;
import de.imi.mopat.io.importer.ImportQuestionnaireValidation;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface to call FhirHelper functions without the need to specify the version everytime. Also holds questionnaires
 * of the specific FHIR version.
 */
public abstract class FhirImporterVersionAdapter {
    
    /**
     * Returns the path of the schema files based
     * on the Fhir Version present
     * @return String
     */
    abstract String getValidationPath();
    
    /**
     * Returns the ExportTemplateType for the
     * corresponding FHIR Version of the adapter
     * @return ExportTemplateType
     */
    abstract ExportTemplateType getExportTemplateType();
    
    /**
     * Creates a new instance of a FHIR Questionnaire
     */
    abstract void createFhirQuestionnaire();
    
    /**
     * Sets the URL for the FHIR Questionnaire
     *
     * @param url URL to set
     */
    abstract void setFhirQuestionnaireUrl(String url);
    
    /**
     * Checks if the title for the questionnaire is empty
     *
     * @return true if empty, false otherwise
     */
    abstract boolean isFhirQuestionnaireTitleEmpty();
    
    /**
     * Returns the title of the FHIR questionnaire
     * @return String of the title
     */
    abstract String getFhirQuestionnaireTitle();
    
    /**
     * Sets the title of the FHIR Questionnaire
     * @param title to set
     */
    abstract void setFhirQuestionnaireTitle(String title);
    
    /**
     * Returns the questionnaire instance and asserts that the type is correct
     *
     * @return Object(Questionnaire)
     */
    abstract Object getFhirQuestionnaire();
    
    /**
     * Sets the questionnaire. Asserts that object is of type Questionnaire for the specific FHIR Version
     *
     * @param questionnaire to set
     */
    abstract void setFhirQuestionnaire(Object questionnaire);
    
    /**
     * Returns the class type of the questionnaire.
     * Will be FHIR Version specific
     * @return Class type of Questionnaire
     */
    abstract Class<?> getFhirQuestionnaireClass();
    
    /**
     * This method validates a file against a XML-Schema-Definition file.
     *
     * @param fileToValidate                {@link File} instance that has to be validated.
     * @param result                        Stores the errors occuring during the validation process.
     * @param messageSource                 Spring messageSource instance containing validation messages.
     * @return True, if the validation was successfull, otherwise there's a {@link DataFormatException} thrown.
     */
    @Deprecated
    abstract boolean validateFileAgainstSchema(final MultipartFile fileToValidate, final String validationPath,
        final ImportQuestionnaireValidation result, final MessageSource messageSource);
    
    
    /**
     * Validates a given resource string with
     * the HAPI resource instance validators.
     * @param fhirResourceString to validate
     * @param errors validation object to store error messages in
     * @param frontendLocale locale to use for error messages
     * @return true, if valid; false otherwise
     */
    abstract boolean validateFileWithFhirInstanceValidator(final String fhirResourceString,
        final ImportQuestionnaireValidation errors, String frontendLocale);
    
    
    /**
     * Parses the given inputStream to a {@link IBaseResource IBaseResource} object.
     *
     * @param inputStream InputStream which contains the file to parse.
     * @return Resource the file is representing.
     */
    abstract IBaseResource parseResourceFromFile(final InputStream inputStream);
    
    /**
     * Returns a singleton instance of class {@link FhirContext}.
     *
     * @return FhirContext instance.
     */
    abstract FhirContext getContext();
    
    /**
     * Encodes the given resource to XML format and writes it to the given file.
     *
     * @param file     File where the encoded resource should be written to.
     */
    abstract void writeQuestionnaireToFile(final File file);
    
    /**
     * Set the error handler to the fhir parser.
     *
     * @param errorHandler ErrorHandler that handles the errors thrown while parsing a file
     */
    abstract void setParserValidator(final IParserErrorHandler errorHandler);
    
    /**
     * Maps FHIR questionnaire to MoPat questionnaire instance.
     *
     * @param exportTemplates   List of {@link ExportTemplate ExportTemplates } the question's
     *                          answers are mapped to.
     * @param messageSource     Object to hold messages connected with message codes.
     * @return {@link ImportQuestionnaireResult} object containing converted questionnaire and the
     * {@link ValidationMessage validationMessages}.
     */
    abstract ImportQuestionnaireResult convertFHIRQuestionnaireToMoPatQuestionnaire(
        List<ExportTemplate> exportTemplates, MessageSource messageSource);
    
}
