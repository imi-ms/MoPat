package de.imi.mopat.io.importer.fhir;

import static org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemType.DISPLAY;

import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.controller.ValidationMessage;
import de.imi.mopat.io.importer.ImportQuestionListResult;
import de.imi.mopat.io.importer.ImportQuestionResult;
import de.imi.mopat.io.importer.ImportQuestionnaireResult;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.ExportRuleAnswer;
import de.imi.mopat.model.ExportRuleFormat;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.FreetextAnswer;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionActionType;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.conditions.ThresholdComparisonType;
import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.model.enumeration.ExportDecimalDelimiterType;
import de.imi.mopat.model.enumeration.ExportNumberType;
import de.imi.mopat.model.enumeration.ExportRoundingStrategyType;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.model.user.User;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseBackboneElement;
import org.hl7.fhir.r4b.model.CanonicalType;
import org.hl7.fhir.r4b.model.CodeableConcept;
import org.hl7.fhir.r4b.model.Coding;
import org.hl7.fhir.r4b.model.DateType;
import org.hl7.fhir.r4b.model.DecimalType;
import org.hl7.fhir.r4b.model.Extension;
import org.hl7.fhir.r4b.model.IntegerType;
import org.hl7.fhir.r4b.model.Questionnaire;
import org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemEnableWhenComponent;
import org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemOperator;
import org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemType;
import org.hl7.fhir.r4b.model.Resource;
import org.hl7.fhir.r4b.model.StringType;
import org.hl7.fhir.r4b.model.TimeType;
import org.hl7.fhir.r4b.model.ValueSet;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This class provides methods to map FHIR resources (questionnaire) to MoPat models. <br> Further it stores data from
 * the latest mapping in maps to assign FHIR elements to MoPat models.
 */
public class FhirR4bToMoPatConverter {
    
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FhirR4bToMoPatConverter.class);
    private static Map<String, String> localizedDisplayNames = new HashMap<>();
    
    /**
     * Maps FHIR questionnaire to MoPat questionnaire instance.
     *
     * @param fhirQuestionnaire FHIR questionnaire that has to be mapped to MoPat questionnaire.
     * @param exportTemplates   List of {@link ExportTemplate ExportTemplates } the question's answers are mapped to.
     * @param messageSource     Object to hold messages connected with message codes.
     * @return {@link ImportQuestionnaireResult} object containing converted questionnaire and the
     * {@link ValidationMessage validationMessages}.
     */
    public static ImportQuestionnaireResult convertFHIRQuestionnaireToMoPatQuestionnaire(
        Questionnaire fhirQuestionnaire, List<ExportTemplate> exportTemplates, MessageSource messageSource) {
        LOGGER.info("Enter convertFHIRQuestionnaireToMoPatQuestionnaire" + "(Questionnaire fhirQuestionnaire, "
            + "List<ExportTemplate> exportTemplates, " + "MessageSource messageSource)");
        ImportQuestionnaireResult importQuestionnaire = new ImportQuestionnaireResult();
        de.imi.mopat.model.Questionnaire mopatQuestionnaire = new de.imi.mopat.model.Questionnaire();
        
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mopatQuestionnaire.setPublished(false);
        mopatQuestionnaire.setChangedBy(currentUser.getId());
        mopatQuestionnaire.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        String locale = null;
        if (fhirQuestionnaire.getLanguage() != null) {
            // FHIR uses language code pattern xx-XX, convert this to pattern
            // xx_XX used by mopat
            locale = fhirQuestionnaire.getLanguage().replace("-", "_");
        } else {
            locale = LocaleContextHolder.getLocale().toLanguageTag().replace("-", "_");
        }
        
        String displayName = "default";
        if (fhirQuestionnaire.getTitle() != null) {
            // All language extensions defined in FHIR
            mopatQuestionnaire.setName(fhirQuestionnaire.getTitle());
            localizedDisplayNames = FhirR4bHelper.getLanugageMapFromLanguageExtension(
                fhirQuestionnaire.getTitleElement());
            localizedDisplayNames.put(locale, fhirQuestionnaire.getTitle());
            displayName = fhirQuestionnaire.getTitle();
        } else if (fhirQuestionnaire.getName() != null && !fhirQuestionnaire.getName().trim().isEmpty()) {
            mopatQuestionnaire.setName(fhirQuestionnaire.getName());
            localizedDisplayNames.put(locale, fhirQuestionnaire.getName());
            displayName = fhirQuestionnaire.getName();
        }
        
        StringBuilder descriptionText = new StringBuilder();
        if (fhirQuestionnaire.getDescription() != null && !fhirQuestionnaire.getDescription().trim().isEmpty()) {
            descriptionText.append(fhirQuestionnaire.getDescription());
            if (fhirQuestionnaire.getPurpose() != null) {
                descriptionText.append("\n\n");
                descriptionText.append(fhirQuestionnaire.getPurpose());
            }
            if (fhirQuestionnaire.getUseContext() != null && !fhirQuestionnaire.getUseContext().isEmpty()
                && fhirQuestionnaire.getUseContextFirstRep().getValue() instanceof CodeableConcept) {
                descriptionText.append("\n\n");
                try {
                    descriptionText.append(
                        fhirQuestionnaire.getUseContextFirstRep().getValueCodeableConcept().getText());
                } catch (FHIRException e) {
                }
            }
        } else if (fhirQuestionnaire.getText() != null && !fhirQuestionnaire.getText().getDivAsString().trim()
            .isEmpty()) {
            descriptionText.append(fhirQuestionnaire.getText().getDivAsString());
            importQuestionnaire.addValidationMessage("import.fhir.questionnaire.descriptionSetToText",
                new String[]{fhirQuestionnaire.getText().getDivAsString()});
        } else if (fhirQuestionnaire.getTitle() != null && !fhirQuestionnaire.getTitle().trim().isEmpty()) {
            descriptionText.append(fhirQuestionnaire.getTitle());
            importQuestionnaire.addValidationMessage("import.fhir.questionnaire.descriptionSetToTitle",
                new String[]{fhirQuestionnaire.getTitle()});
        } else if (fhirQuestionnaire.getName() != null && !fhirQuestionnaire.getName().trim().isEmpty()) {
            descriptionText.append(fhirQuestionnaire.getName());
            importQuestionnaire.addValidationMessage("import.fhir.questionnaire.descriptionSetToName",
                new String[]{fhirQuestionnaire.getName()});
        }
        
        mopatQuestionnaire.setDescription(descriptionText.toString());
        
        // As soon as properties are set, collect the actual items from the
        // hapi/fhir questionnaire in a plain list
        List<QuestionnaireItemComponent> items = new ArrayList<>();
        for (QuestionnaireItemComponent item : fhirQuestionnaire.getItem()) {
            items.add(item);
            items.addAll(FhirR4bHelper.getAllItems(item));
        }
        
        // Create and map all items to questions
        ImportQuestionListResult importQuestionListResult = new ImportQuestionListResult();
        Integer position = 0;
        for (QuestionnaireItemComponent item : items) {
            ImportQuestionResult importQuestionResult = convertItemToQuestion(fhirQuestionnaire, item, locale,
                exportTemplates, messageSource);
            importQuestionListResult.addImportQuestionResult(importQuestionResult);
            if (importQuestionResult.getQuestion() != null) {
                importQuestionResult.getQuestion().setPosition(++position);
                importQuestionResult.getQuestion().setQuestionnaire(mopatQuestionnaire);
                mopatQuestionnaire.addQuestion(importQuestionResult.getQuestion());
            }
        }
        
        importQuestionnaire.addImportQuestionListResult(importQuestionListResult);
        
        // Convert enableWhenComponents to condition
        for (QuestionnaireItemComponent item : items) {
            for (QuestionnaireItemEnableWhenComponent enableWhen : item.getEnableWhen()) {
                convertEnableWhenToCondition(enableWhen, item, importQuestionnaire, items);
            }
        }
        
        for (Entry<String, String> entry : localizedDisplayNames.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                entry.setValue(displayName);
            }
        }
        mopatQuestionnaire.setLocalizedDisplayName(localizedDisplayNames);
        
        importQuestionnaire.setQuestionnaire(mopatQuestionnaire);
        LOGGER.info("Leave convertFHIRQuestionnaireToMoPatQuestionnaire" + "(Questionnaire fhirQuestionnaire, "
            + "List<ExportTemplate> exportTemplates, " + "MessageSource messageSource)");
        return importQuestionnaire;
    }
    
    /**
     * Converts a FHIR item as instance of {@link QuestionnaireItemComponent} to a MoPat {@link Question question}
     * instance.
     *
     * @param questionnaire   The FHIR {@link Questionnaire} Resource which question's are converted.
     * @param item            {@link QuestionnaireItemComponent} that contains all information for a question.
     * @param locale          {@link Locale} containing the language of the questionnaire the item belongs.
     * @param exportTemplates List of {@link ExportTemplate ExportTemplates} the question's answers are mapped to.
     * @param messageSource   Object to hold messages connected with message codes.
     * @return The {@link Question} object which has been mapped from FHIR item. If the FHIR item type doesn't fit to
     * MoPat {@link QuestionType} return
     * <code>null</code>.
     */
    public static ImportQuestionResult convertItemToQuestion(final Questionnaire questionnaire,
        final QuestionnaireItemComponent item, final String locale, final List<ExportTemplate> exportTemplates,
        final MessageSource messageSource) {
        LOGGER.debug("convertItemToQuestion(Questionnaire" + ".QuestionnaireItemComponent item, String "
            + "locale, List<ExportTemplate> exportTemplates," + " MessageSource messageSource)");
        ImportQuestionResult importQuestionResult = new ImportQuestionResult();
        importQuestionResult.setIdentifier(item.getLinkId());
        
        // If the item has got options and has got a text or is of type
        // display convert it to mopat question
        if (item.hasAnswerOption() || item.hasAnswerValueSet() || item.getType().equals(DISPLAY) || (
            item.getText() != null && !item.getText().isEmpty()) || (item.getCode() != null && !item.getCode()
            .isEmpty())) {
            LOGGER.debug("Mapping item {}", item.getLinkId());
            Question question = new Question();
            question.setIsRequired(item.getRequired());
            question.setIsEnabled(Boolean.TRUE);
            Map<String, String> localizedQuestionText = new HashMap<>();
            
            // Set the questions text and get the translations
            if (item.getText() != null) {
                localizedQuestionText = FhirR4bHelper.getLanugageMapFromLanguageExtension(item.getTextElement());
                List<Extension> lang = item.getTextElement().getExtensionsByUrl("lang");
                if (lang.size() > 0 && lang.get(0) != null) {
                    localizedQuestionText.put(lang.get(0).getValueAsPrimitive().getValueAsString().replace("-", "_"),
                        item.getText());
                } else {
                    localizedQuestionText.put(locale, item.getText());
                }
                // If no text is found but the item has got a coding element,
                // use it as question text
            } else if (item.getCode() != null && !item.getCode().isEmpty()) {
                Coding code = item.getCodeFirstRep();
                if (code.getDisplay() != null && !code.getDisplay().isEmpty()) {
                    localizedQuestionText = FhirR4bHelper.getLanugageMapFromLanguageExtension(code.getDisplayElement());
                    List<Extension> lang = code.getDisplayElement().getExtensionsByUrl("lang");
                    if (lang.size() > 0 && lang.get(0) != null) {
                        localizedQuestionText.put(
                            lang.get(0).getValueAsPrimitive().getValueAsString().replace("-", "_"), code.getDisplay());
                    } else {
                        localizedQuestionText.put(locale, code.getDisplay());
                    }
                } else {
                    localizedQuestionText.put(locale, messageSource.getMessage("import.fhir.element" + ".codeNoDisplay",
                        new Object[]{code.getCode(), code.getSystem()}, LocaleHelper.getLocaleFromString(locale)));
                }
            }
            question.setLocalizedQuestionText(localizedQuestionText);
            
            // Get all languages, that are used in the whole questionnaire,
            // to add a display name for each language
            for (String currentLocale : localizedQuestionText.keySet()) {
                Boolean containsLocale = false;
                for (String localizedDisplayName : localizedDisplayNames.keySet()) {
                    if (currentLocale.equals(localizedDisplayName)) {
                        containsLocale = true;
                        break;
                    }
                }
                if (!containsLocale) {
                    localizedDisplayNames.put(currentLocale, null);
                }
            }
            
            // Set the question's type and create answers accordingly
            Answer answer = null;
            Double stepSize = null;
            Entry<Double, Double> maxAndMinEntry;
            // Mapping from FHIR to MoPat of item to question
            switch (item.getType()) {
                case BOOLEAN:
                    // Set the min and max number answer by default to 0 and 1
                    question.setMinMaxNumberAnswers(0, 1);
                    question.setQuestionType(QuestionType.MULTIPLE_CHOICE);
                    
                    Map<String, String> localizedAnswerText = new HashMap<>();
                    // Set the export field for answer true
                    String trueExportField =
                        item.getLinkId().replace(".", "u002E").replace("_", "u005F") + "_" + Boolean.TRUE;
                    
                    // Get the localized label for the answer yes
                    for (String currentLocale : question.getLocalizedQuestionText().keySet()) {
                        localizedAnswerText.put(currentLocale,
                            messageSource.getMessage("survey.question" + ".answer" + ".yes", new Object[]{},
                                LocaleHelper.getLocaleFromString(currentLocale)));
                    }
                    SelectAnswer selectAnswer = new SelectAnswer(question, question.getIsEnabled(), localizedAnswerText,
                        Boolean.FALSE);
                    selectAnswer.setValue(FhirR4bHelper.getScoreFromExtension(item));
                    importQuestionResult.addValidationMessage("import.fhir.option.result", new String[]{
                        messageSource.getMessage("survey.question.answer.yes", new Object[]{},
                            LocaleContextHolder.getLocale())});
                    if (selectAnswer.getValue() != null) {
                        importQuestionResult.addValidationMessage("import.fhir.option.resultScore",
                            new String[]{selectAnswer.getValue().toString()});
                    }
                    
                    // Create exportRule for the boolean answer yes
                    for (ExportTemplate exportTemplate : exportTemplates) {
                        ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate, trueExportField,
                            selectAnswer);
                        ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
                        exportRuleAnswer.setExportRuleFormat(exportRuleFormat);
                        selectAnswer.addExportRule(exportRuleAnswer);
                        exportTemplate.addExportRule(exportRuleAnswer);
                    }
                    
                    // Set the export field for the answer false
                    String falseExportField =
                        item.getLinkId().replace(".", "u002E").replace("_", "u005F") + "_" + Boolean.FALSE;
                    
                    localizedAnswerText = new HashMap<>();
                    // Get the localized label for the answer no
                    for (String currentLocale : question.getLocalizedQuestionText().keySet()) {
                        localizedAnswerText.put(currentLocale,
                            messageSource.getMessage("survey.question" + ".answer.no", new Object[]{},
                                LocaleHelper.getLocaleFromString(currentLocale)));
                    }
                    selectAnswer = new SelectAnswer(question, question.getIsEnabled(), localizedAnswerText,
                        Boolean.FALSE);
                    selectAnswer.setValue(FhirR4bHelper.getScoreFromExtension(item));
                    importQuestionResult.addValidationMessage("import.fhir.option.result", new String[]{
                        messageSource.getMessage("survey.question.answer.no", new Object[]{},
                            LocaleContextHolder.getLocale())});
                    if (selectAnswer.getValue() != null) {
                        importQuestionResult.addValidationMessage("import.fhir.option.resultScore",
                            new String[]{selectAnswer.getValue().toString()});
                    }
                    
                    // Create exportRule for the boolean answer no
                    for (ExportTemplate exportTemplate : exportTemplates) {
                        ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate, falseExportField,
                            selectAnswer);
                        ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
                        exportRuleAnswer.setExportRuleFormat(exportRuleFormat);
                        selectAnswer.addExportRule(exportRuleAnswer);
                        exportTemplate.addExportRule(exportRuleAnswer);
                    }
                    break;
                case CHOICE:
                case OPENCHOICE:
                    // Get if exists the min and max number of necessary answers
                    maxAndMinEntry = FhirR4bHelper.getMinAndMaxFromExtension(item, true);
                    question.setQuestionType(QuestionType.MULTIPLE_CHOICE);
                    if (maxAndMinEntry.getValue() != null) {
                        // Set the min number of answers to the given value
                        // in the extension
                        question.setMinNumberAnswers(maxAndMinEntry.getValue().intValue());
                    } else {
                        // Set it by default to 0 if there's no min number of
                        // answers
                        question.setMinNumberAnswers(0);
                    }
                    // Set the validation message for the min value
                    importQuestionResult.addValidationMessage("import.fhir.item.minNumberAnswers",
                        new String[]{question.getMinNumberAnswers().toString()});
                    if (maxAndMinEntry.getKey() != null) {
                        // Set the max number of answers to the given value
                        // in the extension
                        question.setMaxNumberAnswers(maxAndMinEntry.getKey().intValue());
                    } else {
                        // Set it by default to 1 if there's no max number of
                        // answers
                        question.setMaxNumberAnswers(1);
                    }
                    // Set the validation message for the max value
                    importQuestionResult.addValidationMessage("import.fhir.item.maxNumberAnswers",
                        new String[]{question.getMaxNumberAnswers().toString()});
                    
                    // Convert the options of the multiple choice question
                    for (Questionnaire.QuestionnaireItemAnswerOptionComponent option : item.getAnswerOption()) {
                        convertOptionToAnswer(option, question, importQuestionResult, exportTemplates, messageSource,
                            locale, null);
                    }
                    
                    ValueSet containedValueSet = null;
                    
                    if (item.getAnswerValueSet() != null && questionnaire.getContained() != null
                        && !questionnaire.getContained().isEmpty()) {
                        // If the item contains a reference to a value set
                        CanonicalType reference = item.getAnswerValueSetElement();
                        // Search for the referenced value set in the
                        // contained resources
                        for (Resource resource : questionnaire.getContained()) {
                            if (resource.getId().equals(reference.getIdElement().toString())) {
                                // Set the item's options target to the
                                // referenced value set
                                containedValueSet = (ValueSet) resource;
                            }
                        }
                    }
                    if (containedValueSet != null) {
                        // If the item contains a value set of options
                        for (ValueSet.ConceptSetComponent conceptComponent : containedValueSet.getCompose()
                            .getInclude()) {
                            for (ValueSet.ConceptReferenceComponent conceptReference : conceptComponent.getConcept()) {
                                // Convert the value sets elements to
                                // mopat answers
                                convertOptionToAnswer(conceptReference, question, importQuestionResult, exportTemplates,
                                    messageSource, locale, conceptComponent.getSystem());
                            }
                        }
                    }
                    
                    // If the item doesn't contain any options create a
                    // default answer
                    if ((item.getAnswerOption() == null || item.getAnswerOption().isEmpty()) && (
                        item.getAnswerValueSet() == null || item.getAnswerValueSet().isEmpty()) && (
                        containedValueSet == null || containedValueSet.getCompose() == null
                            || containedValueSet.getCompose().getInclude() == null || containedValueSet.getCompose()
                            .getInclude().isEmpty())) {
                        localizedAnswerText = new HashMap<>();
                        for (Entry<String, String> entry : question.getLocalizedQuestionText().entrySet()) {
                            localizedAnswerText.put(entry.getKey(),
                                messageSource.getMessage("import.fhir.item.noOptions", new Object[]{},
                                    LocaleHelper.getLocaleFromString(entry.getKey())));
                        }
                        new SelectAnswer(question, question.getIsEnabled(), localizedAnswerText, Boolean.FALSE);
                    }
                    
                    if (item.getType() == QuestionnaireItemType.OPENCHOICE) {
                        localizedAnswerText = new HashMap<>();
                        for (Entry<String, String> entry : question.getLocalizedQuestionText().entrySet()) {
                            localizedAnswerText.put(entry.getKey(),
                                messageSource.getMessage("import.fhir.item.other", new Object[]{},
                                    LocaleHelper.getLocaleFromString(entry.getKey())));
                        }
                        //create isOther and freetext answer
                        new SelectAnswer(question, true, localizedAnswerText, true);
                        FreetextAnswer freetextAnswer = new FreetextAnswer(question, true);
                        String exportField =
                            item.getLinkId().replace(".", "u002E").replace("_", "u005F") + "/other_freetext";
                        for (ExportTemplate exportTemplate : exportTemplates) {
                            ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate, exportField,
                                freetextAnswer);
                            exportRuleAnswer.setExportRuleFormat(new ExportRuleFormat());
                            freetextAnswer.addExportRule(exportRuleAnswer);
                        }
                        importQuestionResult.addValidationMessage("import.fhir.item.openchoice.otherFreetext");
                    }
                    
                    break;
                case DATE:
                    question.setQuestionType(QuestionType.DATE);
                    // Get the min and max value of the date item and set it
                    // to the questions start and end date
                    Entry<Date, Date> maxAndMinDateEntry = FhirR4bHelper.getMinAndMaxDateFromExtension(item);
                    Date startDate = maxAndMinDateEntry.getValue();
                    Date endDate = maxAndMinDateEntry.getKey();
                    
                    // Catch the invalid case of start date after enddate
                    if (startDate != null && endDate != null && startDate.after(endDate)) {
                        startDate = null;
                        endDate = null;
                        importQuestionResult.addValidationMessage("import.fhir.item.startDateAfterEndDate");
                    }
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    if (startDate != null && !startDate.after(endDate)) {
                        importQuestionResult.addValidationMessage("import.fhir.item.startDateAdded",
                            new String[]{dateFormat.format(startDate)});
                    }
                    
                    if (endDate != null && !startDate.after(endDate)) {
                        importQuestionResult.addValidationMessage("import.fhir.item.endDateAdded",
                            new String[]{dateFormat.format(endDate)});
                    }
                    
                    answer = new DateAnswer(question, question.getIsEnabled(), startDate, endDate);
                    
                    // Create the export rule
                    for (ExportTemplate exportTemplate : exportTemplates) {
                        ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate,
                            item.getLinkId().replace(".", "u002E").replace("_", "u005F"), answer);
                        ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
                        exportRuleFormat.setDateFormat(ExportDateFormatType.YYYY_MM_DD);
                        exportRuleAnswer.setExportRuleFormat(exportRuleFormat);
                        answer.addExportRule(exportRuleAnswer);
                    }
                    break;
                case INTEGER:
                    // If the answer is of type integer set the stepsize to
                    // 1, the rest of the mapping is done like any other
                    // mapping of question accepting number input
                    stepSize = 1.0d;
                case DECIMAL:
                    // Stepsize is null if the question is not if type integer
                    if (stepSize == null) {
                        stepSize = 0.01d;
                    }
                    // Get the min and max value accepted as input
                    maxAndMinEntry = FhirR4bHelper.getMinAndMaxFromExtension(item, false);
                    Double minValue = maxAndMinEntry.getValue();
                    Double maxValue = maxAndMinEntry.getKey();
                    Double difference = 0.0D;
                    if (minValue != null && maxValue != null) {
                        difference = maxValue - minValue;
                        if (minValue >= maxValue) {
                            // Min and max value is invalid if min is greater
                            // than max value
                            importQuestionResult.addValidationMessage("import.fhir.item.minGreaterThanMax",
                                new String[]{minValue.toString(), maxValue.toString()});
                            minValue = null;
                            maxValue = null;
                            stepSize = null;
                        }
                    }
                    
                    question.setQuestionType(QuestionType.NUMBER_INPUT);
                    if (minValue != null && maxValue != null) {
                        // If the distance between min and max is less or
                        // equal to 10 map the questions of type integer to
                        // number checkbox questions
                        if (difference > 0.0D && difference <= 10.0D
                            && item.getType() == QuestionnaireItemType.INTEGER) {
                            SliderAnswer sliderAnswer = new SliderAnswer(question, true, minValue, maxValue, stepSize,
                                false);
                            sliderAnswer.setLocalizedMaximumText(new HashMap<>());
                            sliderAnswer.setLocalizedMinimumText(new HashMap<>());
                            question.setQuestionType(QuestionType.NUMBER_CHECKBOX);
                            answer = sliderAnswer;
                        } else if (difference > 0.0D && difference <= 2.0D
                            && item.getType() == QuestionnaireItemType.DECIMAL) {
                            SliderAnswer sliderAnswer = new SliderAnswer(question, true, minValue, maxValue, stepSize,
                                false);
                            sliderAnswer.setLocalizedMaximumText(new HashMap<>());
                            sliderAnswer.setLocalizedMinimumText(new HashMap<>());
                            // else to slider answer if distance is less or
                            // equal to 2
                            question.setQuestionType(QuestionType.SLIDER);
                            answer = sliderAnswer;
                        } else {
                            answer = new NumberInputAnswer(question, question.getIsEnabled(), minValue, maxValue,
                                stepSize);
                        }
                    } else {
                        answer = new NumberInputAnswer(question, question.getIsEnabled(), minValue, maxValue, stepSize);
                    }
                    
                    // Add the validation message
                    if (minValue != null && maxValue != null) {
                        importQuestionResult.addValidationMessage("import.fhir.item.minMaxAdded",
                            new String[]{minValue.toString(), maxValue.toString()});
                    }
                    
                    // Create the export rule
                    for (ExportTemplate exportTemplate : exportTemplates) {
                        ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate,
                            item.getLinkId().replace(".", "u002E").replace("_", "u005F"), answer);
                        ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
                        
                        if (item.getType().equals(QuestionnaireItemType.DECIMAL)) {
                            exportRuleFormat.setNumberType(ExportNumberType.FLOAT);
                        } else if (item.getType().equals(QuestionnaireItemType.INTEGER)) {
                            exportRuleFormat.setNumberType(ExportNumberType.INTEGER);
                        }
                        
                        exportRuleFormat.setRoundingStrategy(ExportRoundingStrategyType.STANDARD);
                        exportRuleFormat.setDecimalDelimiter(ExportDecimalDelimiterType.DOT);
                        exportRuleFormat.setDecimalPlaces(2);
                        exportRuleAnswer.setExportRuleFormat(exportRuleFormat);
                        answer.addExportRule(exportRuleAnswer);
                    }
                    break;
                
                case GROUP:
                    // Ignore group items if there are no other questions
                    // attachted to it
                    if (item.getItem() == null || item.getItem().isEmpty()) {
                        importQuestionResult.addValidationMessage("import.fhir.item.questionGroupNoItems",
                            new String[]{item.getLinkId(), item.getType().toString()});
                        return importQuestionResult;
                    }
                case DISPLAY:
                    question.setQuestionType(QuestionType.INFO_TEXT);
                    question.setIsRequired(false);
                    break;
                case STRING:
                case TEXT:
                    question.setQuestionType(QuestionType.FREE_TEXT);
                    answer = new FreetextAnswer(question, true);
                    // Create export rules
                    for (ExportTemplate exportTemplate : exportTemplates) {
                        ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate,
                            item.getLinkId().replace(".", "u002E").replace("_", "u005F"), answer);
                        ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
                        exportRuleAnswer.setExportRuleFormat(exportRuleFormat);
                        answer.addExportRule(exportRuleAnswer);
                    }
                    break;
                default:
                    // All other QuestionnaireItemTypes are not supported by
                    // mopat
                    importQuestionResult.addValidationMessage("import.fhir.item.questionTypeNotSupported",
                        new String[]{item.getType().toString()});
                    return importQuestionResult;
            }
            // Add the validation message as first message
            importQuestionResult.getValidationMessages().add(0,
                new ValidationMessage("import.fhir.item" + ".questionConverted",
                    new String[]{item.getLinkId(), item.getType().getDisplay(),
                        question.getQuestionType().getTextValue()}));
            importQuestionResult.setQuestion(question);
        } else {
            importQuestionResult.addValidationMessage("import.fhir.item.containsNoAnswersOrText",
                new String[]{item.getLinkId(), item.getType().getDisplay()});
        }
        LOGGER.debug("Leave convertItemToQuestion(Questionnaire" + ".QuestionnaireItemComponent item, String "
            + "locale, List<ExportTemplate> exportTemplates," + " MessageSource messageSource)");
        return importQuestionResult;
    }
    
    /**
     * Maps the FHIR option to a MoPat answer instance.
     *
     * @param option               Object that has to be mapped to MoPat answer.
     * @param question             MoPat question the answer belongs to.
     * @param importQuestionResult {@link ImportQuestionResult} object containing the converted question and
     *                             validationMessages.
     * @param exportTemplates      List of {@link ExportTemplate ExportTemplates} the question's answers are mapped to.
     * @param system               The system the {ValueCoding} element may belong to.
     * @param locale               {@link Locale} to localize the questionnaire's content.
     * @param messageSource        Object to hold messages connected with message codes.
     */
    public static void convertOptionToAnswer(final IBaseBackboneElement option, final Question question,
        final ImportQuestionResult importQuestionResult, final List<ExportTemplate> exportTemplates,
        final MessageSource messageSource, final String locale, final String system) {
        LOGGER.debug("Enter convertOptionToAnswer(IBaseBackboneElement " + "option, Question question, "
            + "ImportQuestionResult importQuestionResult, " + "List<ExportTemplate> exportTemplates, "
            + "MessageSource messageSource, String locale)");
        Map<String, String> localizedAnswerText = new HashMap<>();
        Double scoreValue = null;
        // Option is specified by the containing item's collection of options
        if (option instanceof Questionnaire.QuestionnaireItemAnswerOptionComponent item) {
            try {
                String value = null;
                // Differ between the value types the option may contain and
                // get the translated answer label texts
                if (item.getValue() instanceof Coding) {
                    // The option is of type coding and contains display text
                    if (item.getValueCoding().getDisplay() != null && !item.getValueCoding().getDisplay().isEmpty()) {
                        // Get the translations and add it to the localized
                        // labels of the mopat answer
                        localizedAnswerText = FhirR4bHelper.getLanugageMapFromLanguageExtension(
                            item.getValueCoding().getDisplayElement());
                        List<Extension> lang = item.getValueCoding().getDisplayElement().getExtensionsByUrl("lang");
                        if (lang != null && lang.size() > 0 && lang.get(0) != null) {
                            localizedAnswerText.put(
                                lang.get(0).getValueAsPrimitive().getValueAsString().replace("-", "_"),
                                item.getValueCoding().getDisplay());
                        } else {
                            localizedAnswerText.put(locale, item.getValueCoding().getDisplay());
                        }
                    } else {
                        // Otherwise the option's coding element doesn't
                        // contain any display text, so set it by default for
                        // all languages given by the question text's
                        // translations
                        for (String currentLocale : question.getLocalizedQuestionText().keySet()) {
                            String answerText = null;
                            if (item.getValueCoding().getSystem() == null || item.getValueCoding().getSystem()
                                .isEmpty()) {
                                answerText = messageSource.getMessage("import.fhir.element" + ".codeNoDisplayNoSystem",
                                    new Object[]{item.getValueCoding().getCode()},
                                    LocaleHelper.getLocaleFromString(currentLocale));
                            } else {
                                answerText = messageSource.getMessage("import.fhir.element.codeNoDisplay",
                                    new Object[]{item.getValueCoding().getCode(), item.getValueCoding().getSystem()},
                                    LocaleHelper.getLocaleFromString(currentLocale));
                            }
                            localizedAnswerText.put(currentLocale, answerText);
                        }
                    }
                    // Set the exportField value
                    value = importQuestionResult.getIdentifier().replace(".", "u002E").replace("_", "u005F") + "_"
                        + item.getValueCoding().getCode().replace(".", "u002E").replace("_", "u005F");
                } else if (item.getValue() instanceof StringType) {
                    // Set the translated answer labels
                    localizedAnswerText = FhirR4bHelper.getLanugageMapFromLanguageExtension(item.getValueStringType());
                    List<Extension> lang = item.getValueStringType().getExtensionsByUrl("lang");
                    if (lang != null && lang.size() > 0 && lang.get(0) != null) {
                        localizedAnswerText.put(lang.get(0).getValueAsPrimitive().getValueAsString().replace("-", "_"),
                            item.getValueStringType().asStringValue());
                    } else {
                        localizedAnswerText.put(locale, item.getValueStringType().asStringValue());
                    }
                    // Set the exportField value
                    value = importQuestionResult.getIdentifier().replace(".", "u002E").replace("_", "u005F") + "_"
                        + item.getValueStringType().asStringValue().replace(".", "u002E").replace("_", "u005F");
                } else if (item.getValue() instanceof DateType) {
                    // The option may contain date as choosable answer, just
                    // parse it to string
                    value = importQuestionResult.getIdentifier().replace(".", "u002E").replace("_", "u005F") + "_"
                        + item.getValueDateType().asStringValue().replace(".", "u002E").replace("_", "u005F");
                    for (String currentLocale : question.getLocalizedQuestionText().keySet()) {
                        localizedAnswerText.put(currentLocale, item.getValueDateType().asStringValue());
                    }
                } else if (item.getValue() instanceof IntegerType) {
                    // The option may contain integer as choosable answer,
                    // just parse it to string
                    value = importQuestionResult.getIdentifier().replace(".", "u002E").replace("_", "u005F") + "_"
                        + item.getValueIntegerType().asStringValue().replace(".", "u002E").replace("_", "u005F");
                    for (String currentLocale : question.getLocalizedQuestionText().keySet()) {
                        localizedAnswerText.put(currentLocale, item.getValueIntegerType().asStringValue());
                    }
                } else if (item.getValue() instanceof TimeType) {
                    // The option may contain time type as choosable answer,
                    // just parse it to string
                    value = importQuestionResult.getIdentifier().replace(".", "u002E").replace("_", "u005F") + "_"
                        + item.getValueTimeType().asStringValue().replace(".", "u002E").replace("_", "u005F");
                    for (String currentLocale : question.getLocalizedQuestionText().keySet()) {
                        localizedAnswerText.put(currentLocale, item.getValueTimeType().asStringValue());
                    }
                }
                // Create the answer
                SelectAnswer answer = new SelectAnswer(question, question.getIsEnabled(), localizedAnswerText, false);
                if (item.getValue() instanceof Coding) {
                    answer.setValue(FhirR4bHelper.getScoreFromExtension(item.getValueCoding()));
                    scoreValue = answer.getValue();
                }
                if (value != null) {
                    // Create the exportRules
                    for (ExportTemplate exportTemplate : exportTemplates) {
                        ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate, value, answer);
                        exportRuleAnswer.setExportRuleFormat(new ExportRuleFormat());
                        answer.addExportRule(exportRuleAnswer);
                    }
                }
            } catch (FHIRException e) {
                LOGGER.debug("Mapping answer {} to exportRule failed. ValueCoding " + "incorrect.",
                    item.getValue().toString());
            }
            // The option is specified by a value set
        } else if (option instanceof ValueSet.ConceptReferenceComponent item) {
            // Get the translated answer texts
            localizedAnswerText = FhirR4bHelper.getLanugageMapFromLanguageExtension(item.getDisplayElement());
            String displayText;
            if (item.getDisplay() != null) {
                displayText = item.getDisplay();
            } else {
                if (system == null || system.isEmpty()) {
                    displayText = messageSource.getMessage("import.fhir.element" + ".codeNoDisplayNoSystem",
                        new Object[]{item.getCode()}, LocaleHelper.getLocaleFromString(locale));
                } else {
                    displayText = messageSource.getMessage("import.fhir.element" + ".codeNoDisplay",
                        new Object[]{item.getCode(), system}, LocaleHelper.getLocaleFromString(locale));
                }
            }
            // Get the default language for the answer
            List<Extension> lang = item.getDisplayElement().getExtensionsByUrl("lang");
            if (lang != null && lang.size() > 0 && lang.get(0) != null) {
                localizedAnswerText.put(lang.get(0).getValueAsPrimitive().getValueAsString().replace("-", "_"),
                    displayText);
            } else {
                localizedAnswerText.put(locale.replace("-", "_"), displayText);
            }
            // Create the answer
            SelectAnswer answer = new SelectAnswer(question, question.getIsEnabled(), localizedAnswerText, false);
            answer.setValue(FhirR4bHelper.getScoreFromExtension(item.getDisplayElement()));
            scoreValue = answer.getValue();
            // Create the exportRules
            for (ExportTemplate exportTemplate : exportTemplates) {
                ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate,
                    importQuestionResult.getIdentifier().replace(".", "u002E").replace("_", "u005F") + "_"
                        + item.getCode().replace(".", "u002E").replace("_", "u005F"), answer);
                exportRuleAnswer.setExportRuleFormat(new ExportRuleFormat());
                answer.addExportRule(exportRuleAnswer);
            }
        }
        
        // Add the validation messages for the mapped answer texts and score
        // values
        if (localizedAnswerText != null && !localizedAnswerText.isEmpty()) {
            for (String currentLocale : localizedAnswerText.keySet()) {
                String text = localizedAnswerText.get(currentLocale);
                importQuestionResult.addValidationMessage("import.fhir.option.result", new String[]{text});
            }
            if (scoreValue != null) {
                importQuestionResult.addValidationMessage("import.fhir.option.resultScore",
                    new String[]{scoreValue.toString()});
            }
        }
    }
    
    /**
     * Maps FHIR {@link QuestionnaireItemEnableWhenComponent enableWhen} element to MoPat {@link Condition condition}.
     *
     * @param enableWhen                {@link QuestionnaireItemEnableWhenComponent EnableWhen} element to convert to
     *                                  {@link Condition} object.
     * @param items                     List of {@link QuestionnaireItemComponent items} containing the element which
     *                                  acitvates the {@link QuestionnaireItemComponent targetItem}.
     * @param targetItem                {@link QuestionnaireItemComponent} the enableWhen element belongs to.
     * @param importQuestionnaireResult {@link ImportQuestionnaireResult} object containing the converted questionnaire
     *                                  and validationMessages.
     */
    public static void convertEnableWhenToCondition(final QuestionnaireItemEnableWhenComponent enableWhen,
        final QuestionnaireItemComponent targetItem, final ImportQuestionnaireResult importQuestionnaireResult,
        final List<QuestionnaireItemComponent> items) {
        LOGGER.debug("Enter convertEnableWhenToCondition" + "(QuestionnaireItemEnableWhenComponent "
            + "enableWhen, QuestionnaireItemComponent " + "targetItem, ImportQuestionnaireResult "
            + "importQuestionnaireResult, " + "List<QuestionnaireItemComponent> items)");
        ImportQuestionListResult importQuestionListResult = importQuestionnaireResult.getQuestionListResults().get(0);
        ImportQuestionResult targetQuestionResult = importQuestionListResult.getQuestionResultByIdentifier(
            targetItem.getLinkId());
        ImportQuestionResult triggerQuestionResult = importQuestionListResult.getQuestionResultByIdentifier(
            enableWhen.getQuestion());
        QuestionnaireItemComponent triggerItem = FhirR4bHelper.getItemByLinkId(enableWhen.getQuestion(), items);
        
        // The common case is that the enableWhen element activate the
        // targetQuestion, so it is initial disabled.
        targetQuestionResult.getQuestion().setIsEnabled(Boolean.FALSE);
        ConditionActionType action = ConditionActionType.ENABLE;
        switch (triggerItem.getType()) {
            case CHOICE:
                if (enableWhen.hasOperator() && enableWhen.getOperator().equals(QuestionnaireItemOperator.EXISTS)) {
                    boolean expectedExistence = enableWhen.getAnswerBooleanType().booleanValue();
                    
                    // Logic for when answer must exist to enable the target item
                    if (!expectedExistence) {
                        targetQuestionResult.getQuestion().setIsEnabled(Boolean.TRUE);
                        action = ConditionActionType.DISABLE;
                    }
                    
                    for (Answer answer : triggerQuestionResult.getQuestion().getAnswers()) {
                        answer.addCondition(
                            new SelectAnswerCondition(answer, targetQuestionResult.getQuestion(), action, null));
                        // Gets the localized answer text by the current
                        // locale, if the locale doesn't exist in the map, it
                        // returns the first value, same for the question at
                        // the next line
                        // These params have to be set for the
                        // validationMessage. This way it's more clearly
                        // arranged.
                        Map<String, String> localizedLabel = ((SelectAnswer) answer).getLocalizedLabel();
                        triggerQuestionResult.addValidationMessage("import.fhir.condition.selectAnswerCondition",
                            new String[]{localizedLabel.getOrDefault(LocaleContextHolder.getLocale().toString(),
                                localizedLabel.values().toArray()[0].toString()),
                                targetQuestionResult.getQuestion().getLocalizedQuestionText().getOrDefault(
                                    LocaleContextHolder.getLocale().toString(),
                                    targetQuestionResult.getQuestion().getLocalizedQuestionText().values()
                                        .toArray()[0].toString()), action.name()});
                    }
                } else if (enableWhen.hasAnswer()) {
                    String answerText = null;
                    if (enableWhen.getAnswer() instanceof StringType) {
                        try {
                            answerText = enableWhen.getAnswerStringType().asStringValue();
                        } catch (FHIRException e) {
                            return;
                        }
                    } else if (enableWhen.getAnswer() instanceof Coding) {
                        try {
                            answerText = enableWhen.getAnswerCoding().getDisplay();
                        } catch (FHIRException e) {
                            return;
                        }
                    }
                    // Walk through all answers adhering to the
                    // triggerQuestion and check if there's any answer text
                    // that fits to the enableWhen's answer text
                    for (Answer answer : triggerQuestionResult.getQuestion().getAnswers()) {
                        SelectAnswer selectAnswer = (SelectAnswer) answer;
                        for (String text : selectAnswer.getLocalizedLabel().values()) {
                            // If the answer's text fits, create condition
                            // where answer is the trigger and the
                            // targetQuestionResult the target
                            if (text.equals(answerText)) {
                                answer.addCondition(
                                    new SelectAnswerCondition(answer, targetQuestionResult.getQuestion(), action,
                                        null));
                                // Gets the localized answer text by the
                                // current locale, if the locale doesn't
                                // exist in the map, it returns the first
                                // value, same for the question at the next line
                                // These params have to be set for the
                                // validationMessage. This way it's more
                                // clearly arranged.
                                Map<String, String> localizedLabel = ((SelectAnswer) answer).getLocalizedLabel();
                                triggerQuestionResult.addValidationMessage(
                                    "import.fhir.condition" + ".selectAnswerCondition", new String[]{
                                        localizedLabel.getOrDefault(LocaleContextHolder.getLocale().toString(),
                                            localizedLabel.values().toArray()[0].toString()),
                                        targetQuestionResult.getQuestion().getLocalizedQuestionText().getOrDefault(
                                            LocaleContextHolder.getLocale().toString(),
                                            targetQuestionResult.getQuestion().getLocalizedQuestionText().values()
                                                .toArray()[0].toString()), action.name()});
                                break;
                            }
                        }
                    }
                    
                } else {
                    return;
                }
                break;
            case DECIMAL:
            case INTEGER:
                // Handle the mapping for enableWhen triggered by number inputs
                if (enableWhen.hasOperator() && enableWhen.getOperator().equals(QuestionnaireItemOperator.EXISTS)) {
                    return;
                } else if (enableWhen.hasAnswer()) {
                    targetQuestionResult.getQuestion().setIsEnabled(Boolean.FALSE);
                    Double value = 0.0d;
                    if (enableWhen.getAnswer() instanceof DecimalType) {
                        try {
                            value = enableWhen.getAnswerDecimalType().getValue().doubleValue();
                        } catch (FHIRException e) {
                            return;
                        }
                    } else if (enableWhen.getAnswer() instanceof IntegerType) {
                        try {
                            value = enableWhen.getAnswerIntegerType().getValue().doubleValue();
                        } catch (FHIRException e) {
                            return;
                        }
                    }
                    Answer answer = triggerQuestionResult.getQuestion().getAnswers().get(0);
                    
                    // If the value of the triggering answer is not inbetween
                    // the intervall of min, max skip this condition
                    if (answer instanceof NumberInputAnswer numberInputAnswer) {
                        if (numberInputAnswer.getMaxValue() != null && numberInputAnswer.getMinValue() != null && (
                            numberInputAnswer.getMaxValue() < value || numberInputAnswer.getMinValue() > value)) {
                            return;
                        }
                    }
                    
                    if (answer instanceof SliderAnswer sliderAnswer) {
                        if (sliderAnswer.getMaxValue() < value || sliderAnswer.getMinValue() > value) {
                            return;
                        }
                    }
                    // Create the condition, the thresholdComparisonType is
                    // always "==" because there's no type specified by the
                    // fhir specification
                    triggerQuestionResult.getQuestion().getAnswers().get(0).addCondition(
                        new SliderAnswerThresholdCondition(triggerQuestionResult.getQuestion().getAnswers().get(0),
                            targetQuestionResult.getQuestion(), action, null, ThresholdComparisonType.EQUALS, value));
                    triggerQuestionResult.addValidationMessage(
                        "import.fhir.condition" + ".sliderAnswerThresholdCondition", new String[]{
                            targetQuestionResult.getQuestion().getLocalizedQuestionText().getOrDefault(
                                LocaleContextHolder.getLocale().toString(),
                                targetQuestionResult.getQuestion().getLocalizedQuestionText().values()
                                    .toArray()[0].toString()), ThresholdComparisonType.EQUALS.getTextValue(),
                            value.toString(), action.name()});
                }
            default:
                break;
        }
        
        for (QuestionnaireItemComponent childItem : targetItem.getItem()) {
            convertEnableWhenToCondition(enableWhen, childItem, importQuestionnaireResult, items);
        }
    }
}
