package de.imi.mopat.io.importer.odm;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.helper.controller.QuestionnaireVersionGroupService;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.io.importer.ImportQuestionnaireResult;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.OdmValidationResult;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireVersionGroup;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.model.user.User;
import de.unimuenster.imi.org.cdisc.odm.v132.ODM;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionMetaDataVersion;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudy;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class OdmQuestionnaireImporter {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OdmQuestionnaireImporter.class);

    @Autowired
    private ODMProcessingBean odmReader;

    @Autowired
    private ExportTemplateDao exportTemplateDao;

    @Autowired
    private ConfigurationGroupDao configurationGroupDao;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Autowired
    private QuestionnaireVersionGroupService questionnaireVersionGroupService;

    @Autowired
    private StringUtilities stringUtilityHelper;

    public ImportQuestionnaireResult importOdmQuestionnaire(MultipartFile file, List<String> errorMessages) {
        Questionnaire questionnaire;

        if (file == null || file.getSize() == 0) {
            LOGGER.debug("File is null or size is 0");
            errorMessages.add(getMessage("import.odm.v132.content.nullOrSizeZero"));
            return null;
        }

        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!fileExtension.equalsIgnoreCase("xml")) {
            LOGGER.debug("Invalid file extension: {}", fileExtension);
            errorMessages.add(getMessage("import.odm.v132.content.noXML"));
            return null;
        }

        try {
            ODM importedODM = odmReader.unmarshal(file.getInputStream());

            // Validate Study List
            OdmValidationResult studyValidation = validateStudyList(importedODM);
            if (!studyValidation.isValid()) {
                errorMessages.add(studyValidation.getErrorMessage());
                return null;
            }

            // Validate MetaDataVersion List
            ODMcomplexTypeDefinitionStudy study = importedODM.getStudy().get(0);
            OdmValidationResult metaDataValidation = validateMetaDataVersionList(study);
            if (!metaDataValidation.isValid()) {
                errorMessages.add(metaDataValidation.getErrorMessage());
                return null;
            }

            // Validate FormDef List
            ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion = study.getMetaDataVersion().get(0);
            OdmValidationResult formDefValidation = validateFormDefList(metaDataVersion);
            if (!formDefValidation.isValid()) {
                errorMessages.add(formDefValidation.getErrorMessage());
                return null;
            }

            // Assuming validation has passed, process the file.
            ODMcomplexTypeDefinitionFormDef formDef = metaDataVersion.getFormDef().get(0);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User principal = (User) authentication.getPrincipal();
            Long changedBy = principal.getId();

            List<ExportTemplate> exportTemplates = ExportTemplate.createExportTemplates(
                "Automatically Generated Exporttemplate", ExportTemplateType.ODM, file, configurationGroupDao,
                exportTemplateDao);

            ImportQuestionnaireResult result = ODMv132ToMoPatConverter.convertToQuestionnaire(file, formDef, changedBy,
                metaDataVersion, exportTemplates, messageSource);

            questionnaire = result.getQuestionnaire();
            saveQuestionnaireWithVersionGroup(questionnaire, exportTemplates);
            return result;

        } catch (Exception e) {
            LOGGER.error("Error during ODM import: {}", e.getMessage(), e);
            errorMessages.add(getMessage("questionnaire.import.failure"));
            return null;
        }
    }

    private OdmValidationResult validateStudyList(ODM importedODM) {
        List<ODMcomplexTypeDefinitionStudy> studyList = importedODM.getStudy();
        if (studyList == null || studyList.isEmpty()) {
            LOGGER.debug("No Study element in the imported ODM.");
            return OdmValidationResult.failure(getMessage("import.odm.v132.content.noStudy"));
        }
        return OdmValidationResult.success();
    }

    private OdmValidationResult validateMetaDataVersionList(ODMcomplexTypeDefinitionStudy study) {
        List<ODMcomplexTypeDefinitionMetaDataVersion> metaDataVersionList = study.getMetaDataVersion();
        if (metaDataVersionList == null || metaDataVersionList.isEmpty()) {
            LOGGER.debug("No MetaDataVersion in the first Study element.");
            return OdmValidationResult.failure(getMessage("import.odm.v132.content.noMetaDataVersion"));
        }
        return OdmValidationResult.success();
    }

    private OdmValidationResult validateFormDefList(ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion) {
        List<ODMcomplexTypeDefinitionFormDef> formDefList = metaDataVersion.getFormDef();
        if (formDefList == null || formDefList.isEmpty()) {
            LOGGER.debug("No FormDef element in the first MetaDataVersion.");
            return OdmValidationResult.failure(getMessage("import.odm.v132.content.noFormDef"));
        }
        return OdmValidationResult.success();
    }

    private void saveQuestionnaireWithVersionGroup(Questionnaire questionnaire, List<ExportTemplate> exportTemplates) {
        if (!questionnaireDao.isQuestionnaireNameUnique(questionnaire.getName(), 0L)) {
            String timestamp = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG,
                LocaleContextHolder.getLocale()).format(Calendar.getInstance().getTime());
            questionnaire.setName(questionnaire.getName() + " " + timestamp);
        }
        questionnaireDao.merge(questionnaire);

        QuestionnaireVersionGroup versionGroup = questionnaireVersionGroupService.createQuestionnaireGroup(
            questionnaire.getName());
        questionnaire.setQuestionnaireVersionGroup(versionGroup);
        versionGroup.addQuestionnaire(questionnaire);
        questionnaireVersionGroupService.add(versionGroup);

        for (ExportTemplate exportTemplate : exportTemplates) {
            exportTemplate.setQuestionnaire(questionnaire);
            exportTemplateDao.merge(exportTemplate);
        }
    }

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

}
