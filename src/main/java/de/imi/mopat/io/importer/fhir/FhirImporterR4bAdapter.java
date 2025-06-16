package de.imi.mopat.io.importer.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParserErrorHandler;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.io.importer.ImportQuestionnaireResult;
import de.imi.mopat.io.importer.ImportQuestionnaireValidation;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4b.model.Questionnaire;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

public class FhirImporterR4bAdapter extends FhirImporterVersionAdapter {
    
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FhirImporterR4bAdapter.class);
    private Questionnaire questionnaire;
    
    @Override
    public String getValidationPath() {
        return Constants.FHIR_VALIDATION_SCHEMA_SUB_DIRECTORY_R4B;
    }
    
    @Override
    public ExportTemplateType getExportTemplateType() {
        return ExportTemplateType.FHIR_R4B;
    }
    
    @Override
    public void createFhirQuestionnaire() {
        this.questionnaire = new Questionnaire();
    }
    
    @Override
    public void setFhirQuestionnaireUrl(String url) {
        if (this.questionnaire != null) {
            this.questionnaire.setUrl(url);
        }
    }
    
    @Override
    public boolean isFhirQuestionnaireTitleEmpty() {
        return (this.questionnaire.getTitle() == null || this.questionnaire.getTitle().trim().isEmpty());
    }
    
    @Override
    public String getFhirQuestionnaireTitle() {
        if (this.questionnaire != null) {
            return this.questionnaire.getTitle();
        } else {
            return null;
        }
    }
    
    @Override
    public void setFhirQuestionnaireTitle(String title) {
        if (this.questionnaire != null) {
            this.questionnaire.setTitle(title);
        }
    }
    
    @Override
    public Object getFhirQuestionnaire() {
        assert this.questionnaire.getClass() == Questionnaire.class;
        return this.questionnaire;
    }
    
    @Override
    public void setFhirQuestionnaire(Object questionnaire) {
        assert questionnaire.getClass() == Questionnaire.class;
        this.questionnaire = (Questionnaire) questionnaire;
    }
    
    @Override
    public Class<?> getFhirQuestionnaireClass() {
        return Questionnaire.class;
    }
    
    @Override
    public boolean validateFileAgainstSchema(MultipartFile fileToValidate, String validationPath,
        ImportQuestionnaireValidation result, MessageSource messageSource) {
        return FhirR4bHelper.validateFileAgainstSchema(fileToValidate, validationPath,
            Constants.SCHEMA_QUESTIONNAIRE_FILE, result, messageSource);
    }
    
    @Override
    boolean validateFileWithFhirInstanceValidator(String fhirResourceString,
        ImportQuestionnaireValidation errors) {
        return FhirR4bHelper.validateFileWithFhirInstanceValidator(fhirResourceString, errors);
    }
    
    @Override
    public IBaseResource parseResourceFromFile(InputStream inputStream) {
        return FhirR4bHelper.parseResourceFromFile(inputStream);
    }
    
    @Override
    public FhirContext getContext() {
        return FhirR4bHelper.getContext();
    }
    
    @Override
    public void writeQuestionnaireToFile(File file) {
        if (this.questionnaire != null) {
            FhirR4bHelper.writeResourceToFile(this.questionnaire, file);
        }
    }
    
    @Override
    public void setParserValidator(IParserErrorHandler errorHandler) {
        FhirR4bHelper.setParserValidator(errorHandler);
    }
    
    @Override
    public ImportQuestionnaireResult convertFHIRQuestionnaireToMoPatQuestionnaire(List<ExportTemplate> exportTemplates,
        MessageSource messageSource) {
        if (this.questionnaire != null) {
            return FhirR4bToMoPatConverter.convertFHIRQuestionnaireToMoPatQuestionnaire(this.questionnaire,
                exportTemplates, messageSource);
        } else {
            return null;
        }
    }
}
