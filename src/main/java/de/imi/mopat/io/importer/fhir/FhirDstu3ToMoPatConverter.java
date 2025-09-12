package de.imi.mopat.io.importer.fhir;

import static org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemType.DISPLAY;
import static org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemType.INTEGER;

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
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.DecimalType;
import org.hl7.fhir.dstu3.model.IntegerType;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemEnableWhenComponent;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.TimeType;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseBackboneElement;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This class provides methods to map FHIR resources (questionnaire) to MoPat models. <br> Further
 * it stores data from the latest mapping in maps to assign FHIR elements to MoPat models.
 */
public class FhirDstu3ToMoPatConverter {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        FhirDstu3ToMoPatConverter.class);
    private static Map<String, String> localizedDisplayNames = new HashMap<>();

    /**
     * Maps FHIR questionnaire to MoPat questionnaire instance.
     *
     * @param fhirQuestionnaire FHIR questionnaire that has to be mapped to MoPat questionnaire.
     * @param exportTemplates   List of {@link ExportTemplate ExportTemplates} the question's
     *                          answers are mapped to.
     * @param messageSource     Object to hold messages connected with message codes.
     * @return {@link ImportQuestionnaireResult} object containing converted questionnaire and the
     * {@link ValidationMessage validationMessages}.
     */
    public static ImportQuestionnaireResult convertFHIRQuestionnaireToMoPatQuestionnaire(
        Questionnaire fhirQuestionnaire, List<ExportTemplate> exportTemplates,
        MessageSource messageSource) {

        LOGGER.info("Enter convertFHIRQuestionnaireToMoPatQuestionnaire");

        ImportQuestionnaireResult importQuestionnaire = new ImportQuestionnaireResult();
        de.imi.mopat.model.Questionnaire mopatQuestionnaire = initializeQuestionnaire();

        String locale = resolveLocale(fhirQuestionnaire);
        String displayName = setQuestionnaireTitleAndName(mopatQuestionnaire, fhirQuestionnaire,
            locale);

        setDescriptionText(mopatQuestionnaire, importQuestionnaire, fhirQuestionnaire);

        List<Questionnaire.QuestionnaireItemComponent> items = collectAllItems(fhirQuestionnaire);

        processItemsToQuestions(fhirQuestionnaire, mopatQuestionnaire, items, exportTemplates,
            messageSource, locale, importQuestionnaire);

        convertEnableWhenComponentsToConditions(items, importQuestionnaire);

        finalizeLocalizedDisplayNames(locale, displayName);

        mopatQuestionnaire.setLocalizedDisplayName(localizedDisplayNames);
        importQuestionnaire.setQuestionnaire(mopatQuestionnaire);

        LOGGER.info("Leave convertFHIRQuestionnaireToMoPatQuestionnaire");
        return importQuestionnaire;
    }

    /**
     * Initializes a new instance of the Questionnaire object with default values. Sets the
     * questionnaire as unpublished, assigns the current user's ID as the modifier, and updates the
     * timestamp to the current system time.
     *
     * @return A new Questionnaire instance with default initial properties.
     */
    private static de.imi.mopat.model.Questionnaire initializeQuestionnaire() {
        de.imi.mopat.model.Questionnaire questionnaire = new de.imi.mopat.model.Questionnaire();
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication()
            .getPrincipal();
        questionnaire.setPublished(false);
        questionnaire.setChangedBy(currentUser.getId());
        questionnaire.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return questionnaire;
    }

    /**
     * Resolves the locale for a given FHIR questionnaire. It first checks if the questionnaire has
     * a language defined. If so, it converts the language code to the appropriate locale format by
     * replacing dashes with underscores. If the language is not defined, it defaults to the current
     * locale's language as determined by the locale context holder.
     *
     * @param fhirQuestionnaire the FHIR questionnaire object from which to derive the locale
     * @return a string representing the resolved locale in the format with underscores
     */
    private static String resolveLocale(Questionnaire fhirQuestionnaire) {
        String locale;
        if (fhirQuestionnaire.getLanguage() != null) {
            locale = fhirQuestionnaire.getLanguage().replace("-", "_");
        } else {
            locale = LocaleContextHolder.getLocale().toLanguageTag().replace("-", "_");
        }
        return locale;
    }

    /**
     * Sets the title and name of the given mopatQuestionnaire based on the title or name of the
     * provided fhirQuestionnaire. If the fhirQuestionnaire's title is present, it is used as the
     * primary source; otherwise, the name is used. Updates the localized display name with the
     * provided locale.
     *
     * @param mopatQuestionnaire the target questionnaire object where the name is set
     * @param fhirQuestionnaire  the source FHIR Questionnaire object that provides the title or
     *                           name
     * @param locale             the language or locale code used for updating the display name map
     * @return the display name determined based on the title or name of the fhirQuestionnaire
     */
    private static String setQuestionnaireTitleAndName(
        de.imi.mopat.model.Questionnaire mopatQuestionnaire, Questionnaire fhirQuestionnaire,
        String locale) {

        String displayName = "default";

        if (fhirQuestionnaire.getTitle() != null) {
            mopatQuestionnaire.setName(fhirQuestionnaire.getTitle());
            localizedDisplayNames = FhirDstu3Helper.getLanugageMapFromLanguageExtension(
                fhirQuestionnaire.getTitleElement());
            localizedDisplayNames.put(locale, fhirQuestionnaire.getTitle());
            displayName = fhirQuestionnaire.getTitle();
        } else if (fhirQuestionnaire.getName() != null && !fhirQuestionnaire.getName().trim()
            .isEmpty()) {
            mopatQuestionnaire.setName(fhirQuestionnaire.getName());
            localizedDisplayNames.put(locale, fhirQuestionnaire.getName());
            displayName = fhirQuestionnaire.getName();
        }
        return displayName;
    }

    /**
     * Sets the description text for a given {@code de.imi.mopat.model.Questionnaire} object based
     * on the data provided in a {@code Questionnaire} object and adds validation messages to the
     * {@code ImportQuestionnaireResult} if necessary. The method determines the appropriate
     * description text and assigns it to the {@code mopatQuestionnaire}.
     *
     * @param mopatQuestionnaire  The {@code de.imi.mopat.model.Questionnaire} object for which the
     *                            description is being set.
     * @param importQuestionnaire The result of the questionnaire import process where validation
     *                            messages can be stored.
     * @param fhirQuestionnaire   The FHIR {@code Questionnaire} object that provides the
     *                            description or text used for setting the description.
     */
    private static void setDescriptionText(de.imi.mopat.model.Questionnaire mopatQuestionnaire,
        ImportQuestionnaireResult importQuestionnaire, Questionnaire fhirQuestionnaire) {

        StringBuilder descriptionText = new StringBuilder();

        if (fhirQuestionnaire.getDescription() != null && !fhirQuestionnaire.getDescription().trim()
            .isEmpty()) {

            descriptionText.append(fhirQuestionnaire.getDescription());
            appendOptionalDescription(fhirQuestionnaire, descriptionText);

        } else if (fhirQuestionnaire.getText() != null && !fhirQuestionnaire.getText()
            .getDivAsString().trim().isEmpty()) {

            descriptionText.append(fhirQuestionnaire.getText().getDivAsString());
            importQuestionnaire.addValidationMessage(
                "import.fhir.questionnaire.descriptionSetToText",
                new String[]{fhirQuestionnaire.getText().getDivAsString()});
        } else {
            setFallbackDescription(fhirQuestionnaire, descriptionText, importQuestionnaire);
        }

        mopatQuestionnaire.setDescription(descriptionText.toString());
    }

    /**
     * Appends optional description information from the given FHIR Questionnaire to the provided
     * StringBuilder. The description includes the purpose and use context of the questionnaire, if
     * available.
     *
     * @param fhirQuestionnaire The FHIR Questionnaire object containing the purpose and use context
     *                          details.
     * @param descriptionText   The StringBuilder to which the optional description will be
     *                          appended.
     */
    private static void appendOptionalDescription(Questionnaire fhirQuestionnaire,
        StringBuilder descriptionText) {
        if (fhirQuestionnaire.getPurpose() != null) {
            descriptionText.append("\n\n");
            descriptionText.append(fhirQuestionnaire.getPurpose());
        }

        if (fhirQuestionnaire.getUseContext() != null && !fhirQuestionnaire.getUseContext()
            .isEmpty() && fhirQuestionnaire.getUseContextFirstRep()
            .getValue() instanceof CodeableConcept) {
            descriptionText.append("\n\n");
            try {
                descriptionText.append(
                    fhirQuestionnaire.getUseContextFirstRep().getValueCodeableConcept().getText());
            } catch (FHIRException e) {
                // Logging or handling can be added if necessary
            }
        }
    }

    /**
     * Sets a fallback description for the FHIR Questionnaire. The fallback description is
     * determined by checking the title and name of the questionnaire. If the title is not null or
     * empty, it is set as the description. Otherwise, if the name is not null or empty, it is set
     * as the description. Validation messages are added for these cases.
     *
     * @param fhirQuestionnaire   the FHIR Questionnaire object from which the title or name is used
     *                            as a fallback description
     * @param descriptionText     the StringBuilder object where the fallback description is
     *                            appended
     * @param importQuestionnaire the result object where validation messages are recorded
     */
    private static void setFallbackDescription(Questionnaire fhirQuestionnaire,
        StringBuilder descriptionText, ImportQuestionnaireResult importQuestionnaire) {

        if (fhirQuestionnaire.getTitle() != null && !fhirQuestionnaire.getTitle().trim()
            .isEmpty()) {
            descriptionText.append(fhirQuestionnaire.getTitle());
            importQuestionnaire.addValidationMessage(
                "import.fhir.questionnaire.descriptionSetToTitle",
                new String[]{fhirQuestionnaire.getTitle()});
        } else if (fhirQuestionnaire.getName() != null && !fhirQuestionnaire.getName().trim()
            .isEmpty()) {
            descriptionText.append(fhirQuestionnaire.getName());
            importQuestionnaire.addValidationMessage(
                "import.fhir.questionnaire.descriptionSetToName",
                new String[]{fhirQuestionnaire.getName()});
        }
    }

    /**
     * Collects all items from the given FHIR questionnaire, including nested items.
     *
     * @param fhirQuestionnaire The FHIR Questionnaire from which to collect items.
     * @return A list of all QuestionnaireItemComponent instances, including nested components.
     */
    private static List<Questionnaire.QuestionnaireItemComponent> collectAllItems(
        Questionnaire fhirQuestionnaire) {
        List<Questionnaire.QuestionnaireItemComponent> items = new ArrayList<>();
        for (Questionnaire.QuestionnaireItemComponent item : fhirQuestionnaire.getItem()) {
            items.add(item);
            items.addAll(FhirDstu3Helper.getAllItems(item));
        }
        return items;
    }

    /**
     * Processes the items from the FHIR Questionnaire and converts them to questions to be added to
     * the MoPat Questionnaire. Updates the MoPat Questionnaire with the newly created questions and
     * tracks the results of the import operation.
     *
     * @param fhirQuestionnaire   the FHIR Questionnaire being processed
     * @param mopatQuestionnaire  the MoPat Questionnaire to which the processed items are added
     * @param items               the list of QuestionnaireItemComponents to process and convert
     *                            into questions
     * @param exportTemplates     the list of export templates used during the conversion
     * @param messageSource       the message source used for localization
     * @param locale              the locale used for translating messages
     * @param importQuestionnaire the object where the results of the import operation are stored
     */
    private static void processItemsToQuestions(Questionnaire fhirQuestionnaire,
        de.imi.mopat.model.Questionnaire mopatQuestionnaire,
        List<Questionnaire.QuestionnaireItemComponent> items, List<ExportTemplate> exportTemplates,
        MessageSource messageSource, String locale, ImportQuestionnaireResult importQuestionnaire) {

        ImportQuestionListResult importQuestionListResult = new ImportQuestionListResult();
        Integer position = 0;

        for (Questionnaire.QuestionnaireItemComponent item : items) {
            ImportQuestionResult importQuestionResult = convertItemToQuestion(fhirQuestionnaire,
                item, locale, exportTemplates, messageSource);

            importQuestionListResult.addImportQuestionResult(importQuestionResult);
            if (importQuestionResult.getQuestion() != null) {
                importQuestionResult.getQuestion().setPosition(++position);
                importQuestionResult.getQuestion().setQuestionnaire(mopatQuestionnaire);
                mopatQuestionnaire.addQuestion(importQuestionResult.getQuestion());
            }
        }

        importQuestionnaire.addImportQuestionListResult(importQuestionListResult);
    }

    /**
     * Converts the "enableWhen" components of questionnaire items into conditions and processes
     * them accordingly.
     *
     * @param items               the list of questionnaire item components to process
     * @param importQuestionnaire the object that holds information related to the imported
     *                            questionnaire
     */
    private static void convertEnableWhenComponentsToConditions(
        List<Questionnaire.QuestionnaireItemComponent> items,
        ImportQuestionnaireResult importQuestionnaire) {

        for (QuestionnaireItemComponent item : items) {
            for (QuestionnaireItemEnableWhenComponent enableWhen : item.getEnableWhen()) {
                convertEnableWhenToCondition(enableWhen, item, importQuestionnaire, items);
            }
        }
    }

    /**
     * Updates entries in the localizedDisplayNames map where the current value is null or empty
     * with the provided displayName.
     *
     * @param locale      the locale string associated with the display name
     * @param displayName the display name to be used for updating null or empty map entries
     */
    private static void finalizeLocalizedDisplayNames(String locale, String displayName) {
        for (Map.Entry<String, String> entry : localizedDisplayNames.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                entry.setValue(displayName);
            }
        }
    }


    /**
     * Converts a FHIR item as instance of {@link QuestionnaireItemComponent} to a MoPat
     * {@link Question question} instance.
     *
     * @param questionnaire   The FHIR {@link Questionnaire} Resource which question's are
     *                        converted.
     * @param item            {@link QuestionnaireItemComponent} that contains all information for a
     *                        question.
     * @param locale          {@link Locale} containing the language of the questionnaire the item
     *                        belongs.
     * @param exportTemplates List of {@link ExportTemplate ExportTemplates} the question's answers
     *                        are mapped to.
     * @param messageSource   Object to hold messages connected with message codes.
     * @return The {@link Question} object which has been mapped from FHIR item. If the FHIR item
     * type doesn't fit to MoPat {@link QuestionType} return
     * <code>null</code>.
     */
    public static ImportQuestionResult convertItemToQuestion(final Questionnaire questionnaire,
        final Questionnaire.QuestionnaireItemComponent item, final String locale,
        final List<ExportTemplate> exportTemplates, final MessageSource messageSource) {
        LOGGER.debug(
            "convertItemToQuestion(Questionnaire" + ".QuestionnaireItemComponent item, String "
                + "locale, List<ExportTemplate> exportTemplates,"
                + " MessageSource messageSource)");
        ImportQuestionResult importQuestionResult = new ImportQuestionResult();
        importQuestionResult.setIdentifier(item.getLinkId());

        // If the item has got options and has got a text or is of type
        // display convert it to mopat question
        if (hasOptionsOrDisplayOrTextOrCode(item)) {
            LOGGER.debug("Mapping item {}", item.getLinkId());
            Question question = new Question();
            question.setIsRequired(item.getRequired());
            question.setIsEnabled(Boolean.TRUE);

            setLocalizedTextForQuestion(question, item, messageSource, locale);

            doHandleAnswer(question, questionnaire, messageSource, importQuestionResult, item,
                exportTemplates, locale);
        } else {
            importQuestionResult.addValidationMessage("import.fhir.item.containsNoAnswersOrText",
                new String[]{item.getLinkId(), item.getType().getDisplay()});
        }
        LOGGER.debug("Leave convertItemToQuestion(Questionnaire"
            + ".QuestionnaireItemComponent item, String "
            + "locale, List<ExportTemplate> exportTemplates," + " MessageSource messageSource)");
        return importQuestionResult;
    }

    /**
     * Determines whether the given QuestionnaireItemComponent has options, a display type, text, or
     * code. This method checks if the item contains any of these attributes and returns true if at
     * least one of them is present.
     *
     * @param item the QuestionnaireItemComponent to evaluate. This component is typically part of a
     *             questionnaire resource and may include options, display elements, text, or
     *             codes.
     * @return true if the item contains options (hasOption or hasOptions), a display type,
     * non-empty text, or non-empty code; false otherwise.
     */
    private static boolean hasOptionsOrDisplayOrTextOrCode(QuestionnaireItemComponent item) {
        return item.hasOption() || item.hasOptions() || (item.getOptionsTarget() != null
            && !item.getOptionsTarget().isEmpty()) || item.getType().equals(DISPLAY) || (
            item.getText() != null && !item.getText().isEmpty()) || (item.getCode() != null
            && !item.getCode().isEmpty());
    }

    /**
     * Sets the localized text for a given question based on the provided questionnaire item
     * component, message source, and locale. This method retrieves localized translations for the
     * question text and associates them with the question. If no text is defined, it attempts to
     * use coding elements for the question text and applies localization as needed.
     *
     * @param question      The Question object to update with localized text.
     * @param item          The QuestionnaireItemComponent from which to extract the text or coding
     *                      information for localization.
     * @param messageSource The MessageSource object used to fetch fallback localized messages when
     *                      necessary.
     * @param locale        The target language/locale code to retrieve the localized text.
     */
    private static void setLocalizedTextForQuestion(Question question,
        QuestionnaireItemComponent item, MessageSource messageSource, String locale) {
        Map<String, String> localizedQuestionText = new HashMap<>();

        // Set the questions text and get the translations
        if (item.getText() != null) {
            localizedQuestionText = FhirDstu3Helper.getLanugageMapFromLanguageExtension(
                item.getTextElement());

            // If no text is found but the item has got a coding element,
            // use it as question text
        } else if (item.getCode() != null && !item.getCode().isEmpty()) {
            Coding code = item.getCodeFirstRep();
            if (code.getDisplay() != null && !code.getDisplay().isEmpty()) {
                localizedQuestionText = FhirDstu3Helper.getLanugageMapFromLanguageExtension(
                    code.getDisplayElement());

            } else {
                localizedQuestionText.put(locale,
                    messageSource.getMessage("import.fhir.element" + ".codeNoDisplay",
                        new Object[]{code.getCode(), code.getSystem()},
                        LocaleHelper.getLocaleFromString(locale)));
            }
        }
        question.setLocalizedQuestionText(localizedQuestionText);

        addMissingTextForAvailableLocales(localizedQuestionText);
    }

    /**
     * Adds missing text entries for available locales that are not already present in the localized
     * display names map. If a locale in the given map of localized question text does not exist in
     * the localized display names, an entry is added to represent it with a null value.
     *
     * @param localizedQuestionText a map where the keys are locale identifiers and the values are
     *                              the localized question text. This map is checked against the
     *                              existing localized display names.
     */
    private static void addMissingTextForAvailableLocales(
        Map<String, String> localizedQuestionText) {
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
    }


    /**
     * Handles different types of answers within a questionnaire item and performs the necessary
     * operations corresponding to the question type. Adds validation or conversion messages when
     * appropriate.
     *
     * @param question             the question being processed
     * @param questionnaire        the questionnaire containing the question
     * @param messageSource        the message source used for validation and localization
     * @param importQuestionResult the result object for capturing conversion and validation
     *                             messages
     * @param item                 the questionnaire item component to process
     * @param exportTemplates      the list of export templates to be used in handling the answer
     * @param locale               the locale in which the questionnaire is processed
     */
    private static void doHandleAnswer(Question question, Questionnaire questionnaire,
        MessageSource messageSource, ImportQuestionResult importQuestionResult,
        QuestionnaireItemComponent item, List<ExportTemplate> exportTemplates, String locale) {
        switch (item.getType()) {
            case BOOLEAN:
                doHandleBooleanAnswer(question, messageSource, importQuestionResult, item,
                    exportTemplates);
                addConversionMessage(question, importQuestionResult, item);
                break;
            case CHOICE:
            case OPENCHOICE:
                doHandleChoiceAnswer(question, questionnaire, messageSource, importQuestionResult,
                    item, exportTemplates, locale);
                addConversionMessage(question, importQuestionResult, item);
                break;
            case DATE:
                doHandleDateAnswer(question, importQuestionResult, item, exportTemplates);
                addConversionMessage(question, importQuestionResult, item);
                break;
            case INTEGER:
            case DECIMAL:
                doHandleNumberAnswer(question, importQuestionResult, item, exportTemplates);
                addConversionMessage(question, importQuestionResult, item);
                break;

            case GROUP:
                // Ignore group items if there are no other questions
                // attachted to it
                if (item.getItem() == null || item.getItem().isEmpty()) {
                    importQuestionResult.addValidationMessage(
                        "import.fhir.item.questionGroupNoItems",
                        new String[]{item.getLinkId(), item.getType().toString()});
                }
                break;
            case DISPLAY:
                doHandleDisplayAnswer(question);
                addConversionMessage(question, importQuestionResult, item);
                break;
            case STRING:
            case TEXT:
                doHandleTextAnswer(question, item, exportTemplates);
                addConversionMessage(question, importQuestionResult, item);
                break;
            default:
                importQuestionResult.addValidationMessage(
                    "import.fhir.item.questionTypeNotSupported",
                    new String[]{item.getType().toString()});
                break;
        }
    }


    /**
     * Handles the conversion of a boolean FHIR question item into a MoPat question with specific
     * localized answer texts and mappings.
     *
     * @param question             The {@link Question} object to which the boolean answers will be
     *                             added and configured.
     * @param messageSource        The source of messages and localized strings for providing answer
     *                             labels based on locales.
     * @param importQuestionResult The {@link ImportQuestionResult} object for collecting validation
     *                             messages during the conversion process.
     * @param item                 The {@link QuestionnaireItemComponent} representing the boolean
     *                             question item to process.
     * @param exportTemplates      A list of {@link ExportTemplate} objects where the question's
     *                             answers and export rules will be configured.
     */
    private static void doHandleBooleanAnswer(Question question, MessageSource messageSource,
        ImportQuestionResult importQuestionResult, QuestionnaireItemComponent item,
        List<ExportTemplate> exportTemplates) {
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
        SelectAnswer selectAnswer = new SelectAnswer(question, question.getIsEnabled(),
            localizedAnswerText, Boolean.FALSE);
        selectAnswer.setValue(FhirDstu3Helper.getScoreFromExtension(item));
        importQuestionResult.addValidationMessage("import.fhir.option.result", new String[]{
            messageSource.getMessage("survey.question.answer.yes", new Object[]{},
                LocaleContextHolder.getLocale())});
        if (selectAnswer.getValue() != null) {
            importQuestionResult.addValidationMessage("import.fhir.option.resultScore",
                new String[]{selectAnswer.getValue().toString()});
        }

        // Create exportRule for the boolean answer yes
        for (ExportTemplate exportTemplate : exportTemplates) {
            ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate,
                trueExportField, selectAnswer);
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
        selectAnswer.setValue(FhirDstu3Helper.getScoreFromExtension(item));
        importQuestionResult.addValidationMessage("import.fhir.option.result", new String[]{
            messageSource.getMessage("survey.question.answer.no", new Object[]{},
                LocaleContextHolder.getLocale())});
        if (selectAnswer.getValue() != null) {
            importQuestionResult.addValidationMessage("import.fhir.option.resultScore",
                new String[]{selectAnswer.getValue().toString()});
        }

        // Create exportRule for the boolean answer no
        for (ExportTemplate exportTemplate : exportTemplates) {
            ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate,
                falseExportField, selectAnswer);
            ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
            exportRuleAnswer.setExportRuleFormat(exportRuleFormat);
            selectAnswer.addExportRule(exportRuleAnswer);
            exportTemplate.addExportRule(exportRuleAnswer);
        }
    }

    /**
     * Handles the processing of choice-based questions (multiple-choice or open-choice) when
     * importing questions into the system. It sets up the question type, validates constraints,
     * converts options or value sets to selectable answers, and creates default answers if no
     * options are explicitly defined.
     *
     * @param question             The {@link Question} instance to be updated with the processed
     *                             data.
     * @param questionnaire        The {@link Questionnaire} that contains the question and its
     *                             metadata.
     * @param messageSource        The {@link MessageSource} used for retrieving localized
     *                             validation messages.
     * @param importQuestionResult The {@link ImportQuestionResult} object for storing validation
     *                             messages and tracking import results.
     * @param item                 The {@link QuestionnaireItemComponent} representing the question
     *                             item in the FHIR {@link Questionnaire}.
     * @param exportTemplates      A list of {@link ExportTemplate} objects used for defining export
     *                             rules for answers.
     * @param locale               The locale string for processing localized content.
     */
    private static void doHandleChoiceAnswer(Question question, Questionnaire questionnaire,
        MessageSource messageSource, ImportQuestionResult importQuestionResult,
        QuestionnaireItemComponent item, List<ExportTemplate> exportTemplates, String locale) {
        // Get if exists the min and max number of necessary answers
        Entry<Double, Double> maxAndMinEntry = FhirDstu3Helper.getMinAndMaxFromExtension(item,
            true);
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
        for (Questionnaire.QuestionnaireItemOptionComponent option : item.getOption()) {
            convertOptionToAnswer(option, question, importQuestionResult, exportTemplates,
                messageSource, locale, null);
        }
        if (item.getOptions() != null && questionnaire.getContained() != null
            && !questionnaire.getContained().isEmpty()) {
            // If the item contains a reference to a value set
            Reference reference = item.getOptions();
            // Search for the referenced value set in the
            // contained resources
            for (Resource resource : questionnaire.getContained()) {
                if (resource.getId().equals(reference.getReference())) {
                    // Set the item's options target to the
                    // referenced value set
                    item.setOptionsTarget((ValueSet) resource);
                }
            }
        }
        if (item.getOptionsTarget() != null) {
            // If the item contains a value set of options
            ValueSet valueSet = item.getOptionsTarget();
            if (valueSet != null) {
                for (ValueSet.ConceptSetComponent conceptComponent : valueSet.getCompose()
                    .getInclude()) {
                    for (ValueSet.ConceptReferenceComponent conceptReference : conceptComponent.getConcept()) {
                        // Convert the value sets elements to
                        // mopat answers
                        convertOptionToAnswer(conceptReference, question, importQuestionResult,
                            exportTemplates, messageSource, locale, conceptComponent.getSystem());
                    }
                }
            }
        }

        // If the item doesn't contain any options create a
        // default answer
        Map<String, String> localizedAnswerText;

        if ((item.getOption() == null || item.getOption().isEmpty()) && (item.getOptions() == null
            || item.getOptions().isEmpty()) && (item.getOptionsTarget() == null
            || item.getOptionsTarget().getCompose() == null
            || item.getOptionsTarget().getCompose().getInclude() == null || item.getOptionsTarget()
            .getCompose().getInclude().isEmpty())) {

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
                ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate,
                    exportField, freetextAnswer);
                exportRuleAnswer.setExportRuleFormat(new ExportRuleFormat());
                freetextAnswer.addExportRule(exportRuleAnswer);
            }
            importQuestionResult.addValidationMessage("import.fhir.item.openchoice.otherFreetext");
        }
    }

    /**
     * Handles the processing of date-based FHIR question items by setting the question type,
     * validating date constraints, configuring start and end dates, and creating export rules.
     *
     * @param question             The {@link Question} instance to be updated with the processed
     *                             date-based information.
     * @param importQuestionResult The {@link ImportQuestionResult} object for storing validation
     *                             messages and tracking import results.
     * @param item                 The {@link QuestionnaireItemComponent} representing the
     *                             date-based FHIR question item to process.
     * @param exportTemplates      A list of {@link ExportTemplate} objects used for defining export
     *                             rules for answers.
     */
    private static void doHandleDateAnswer(Question question,
        ImportQuestionResult importQuestionResult, QuestionnaireItemComponent item,
        List<ExportTemplate> exportTemplates) {
        question.setQuestionType(QuestionType.DATE);
        // Get the min and max value of the date item and set it
        // to the questions start and end date
        Entry<Date, Date> maxAndMinDateEntry = FhirDstu3Helper.getMinAndMaxDateFromExtension(item);
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

        Answer answer = new DateAnswer(question, question.getIsEnabled(), startDate, endDate);

        // Create the export rule
        for (ExportTemplate exportTemplate : exportTemplates) {
            ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate,
                item.getLinkId().replace(".", "u002E").replace("_", "u005F"), answer);
            ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
            exportRuleFormat.setDateFormat(ExportDateFormatType.YYYY_MM_DD);
            exportRuleAnswer.setExportRuleFormat(exportRuleFormat);
            answer.addExportRule(exportRuleAnswer);
        }
    }

    /**
     * Handles the processing of numerical answers for the given question. It defines number-related
     * constraints such as minimum, maximum, and step size as well as adjusts the question type
     * based on these constraints. Also, it manages the export rules for numerical questions.
     *
     * @param question             The question object to be configured and updated with numerical
     *                             constraints.
     * @param importQuestionResult The object used to collect validation messages during
     *                             processing.
     * @param item                 The FHIR-compliant questionnaire item containing type and
     *                             constraints for input data.
     * @param exportTemplates      A list of export templates for configuring how the numerical
     *                             answers should be exported.
     */
    private static void doHandleNumberAnswer(Question question,
        ImportQuestionResult importQuestionResult, QuestionnaireItemComponent item,
        List<ExportTemplate> exportTemplates) {
        Entry<Double, Double> maxAndMinEntry;
        Double stepSize = null;

        if (item.getType() == INTEGER) {
            stepSize = 1.0d;
        }
        if (stepSize == null) {
            stepSize = 0.01d;
        }

        // Get the min and max value accepted as input
        maxAndMinEntry = FhirDstu3Helper.getMinAndMaxFromExtension(item, false);
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

        Answer answer;
        question.setQuestionType(QuestionType.NUMBER_INPUT);
        if (minValue != null && maxValue != null) {
            // If the distance between min and max is less or
            // equal to 10 map the questions of type integer to
            // number checkbox questions
            if (difference > 0.0D && difference <= 10.0D
                && item.getType() == QuestionnaireItemType.INTEGER) {
                SliderAnswer sliderAnswer = new SliderAnswer(question, true, minValue, maxValue,
                    stepSize, false);
                sliderAnswer.setLocalizedMaximumText(new HashMap<>());
                sliderAnswer.setLocalizedMinimumText(new HashMap<>());
                question.setQuestionType(QuestionType.NUMBER_CHECKBOX);
                answer = sliderAnswer;
            } else if (difference > 0.0D && difference <= 2.0D
                && item.getType() == QuestionnaireItemType.DECIMAL) {
                SliderAnswer sliderAnswer = new SliderAnswer(question, true, minValue, maxValue,
                    stepSize, false);
                sliderAnswer.setLocalizedMaximumText(new HashMap<>());
                sliderAnswer.setLocalizedMinimumText(new HashMap<>());
                // else to slider answer if distance is less or
                // equal to 2
                question.setQuestionType(QuestionType.SLIDER);
                answer = sliderAnswer;
            } else {
                answer = new NumberInputAnswer(question, question.getIsEnabled(), minValue,
                    maxValue, stepSize);
            }
        } else {
            answer = new NumberInputAnswer(question, question.getIsEnabled(), minValue, maxValue,
                stepSize);
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
    }

    /**
     * Configures the provided question to be a display-type question with optional response. Sets
     * the question type to {@link QuestionType#INFO_TEXT} and marks it as not required.
     *
     * @param question The {@link Question} to be configured as a display-only type.
     */
    private static void doHandleDisplayAnswer(Question question) {
        question.setQuestionType(QuestionType.INFO_TEXT);
        question.setIsRequired(false);
    }

    /**
     * Handles processing of a text-based answer for a given question and questionnaire item
     * component. This method sets the question type to free text, creates a free text answer
     * object, and generates export rules based on the provided export templates.
     *
     * @param question        The question object to be processed and updated with the free text
     *                        answer.
     * @param item            The questionnaire item component associated with the question.
     * @param exportTemplates A list of export templates used to create export rules for the
     *                        answer.
     */
    private static void doHandleTextAnswer(Question question, QuestionnaireItemComponent item,
        List<ExportTemplate> exportTemplates) {
        question.setQuestionType(QuestionType.FREE_TEXT);
        Answer answer = new FreetextAnswer(question, true);
        // Create export rules
        for (ExportTemplate exportTemplate : exportTemplates) {
            ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate,
                item.getLinkId().replace(".", "u002E").replace("_", "u005F"), answer);
            ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
            exportRuleAnswer.setExportRuleFormat(exportRuleFormat);
            answer.addExportRule(exportRuleAnswer);
        }
    }

    /**
     * Adds a conversion message to the import question result, based on the provided question and
     * questionnaire item component.
     *
     * @param question             The question object representing the converted question.
     * @param importQuestionResult The result object that contains validation messages and the
     *                             question.
     * @param item                 The questionnaire item component used to generate the conversion
     *                             message.
     */
    private static void addConversionMessage(Question question,
        ImportQuestionResult importQuestionResult, QuestionnaireItemComponent item) {
        // Add the validation message as first message
        importQuestionResult.getValidationMessages().add(0,
            new ValidationMessage("import.fhir.item" + ".questionConverted",
                new String[]{item.getLinkId(), item.getType().getDisplay(),
                    question.getQuestionType().getTextValue()}));
        importQuestionResult.setQuestion(question);
    }

    /**
     * Maps the FHIR option to a MoPat answer instance.
     *
     * @param option               Object that has to be mapped to MoPat answer.
     * @param question             MoPat question the answer belongs to.
     * @param importQuestionResult {@link ImportQuestionResult} object containing the converted
     *                             question and validationMessages.
     * @param exportTemplates      List of {@link ExportTemplate ExportTemplates} the question's
     *                             answers are mapped to.
     * @param system               The system the {ValueCoding} element may belong to.
     * @param locale               {@link Locale} to localize the questionnaire's content.
     * @param messageSource        Object to hold messages connected with message codes.
     */
    public static void convertOptionToAnswer(final IBaseBackboneElement option,
        final Question question, final ImportQuestionResult importQuestionResult,
        final List<ExportTemplate> exportTemplates, final MessageSource messageSource,
        final String locale, final String system) {
        LOGGER.debug(
            "Enter convertOptionToAnswer(IBaseBackboneElement " + "option, Question question, "
                + "ImportQuestionResult importQuestionResult, "
                + "List<ExportTemplate> exportTemplates, "
                + "MessageSource messageSource, String locale)");
        Map<String, String> localizedAnswerText = new HashMap<>();
        Double scoreValue = null;
        // Option is specified by the containing item's collection of options
        if (option instanceof Questionnaire.QuestionnaireItemOptionComponent item) {
            try {
                String value = null;
                // Differ between the value types the option may contain and
                // get the translated answer label texts
                if (item.getValue() instanceof Coding) {
                    // The option is of type coding and contains display text
                    if (item.getValueCoding().getDisplay() != null && !item.getValueCoding()
                        .getDisplay().isEmpty()) {
                        // Get the translations and add it to the localized
                        // labels of the mopat answer
                        localizedAnswerText = FhirDstu3Helper.getLanugageMapFromLanguageExtension(
                            item.getValueCoding().getDisplayElement());

                    } else {
                        // Otherwise the option's coding element doesn't
                        // contain any display text, so set it by default for
                        // all languages given by the question text's
                        // translations
                        for (String currentLocale : question.getLocalizedQuestionText().keySet()) {
                            String answerText = null;
                            if (item.getValueCoding().getSystem() == null || item.getValueCoding()
                                .getSystem().isEmpty()) {
                                answerText = messageSource.getMessage(
                                    "import.fhir.element" + ".codeNoDisplayNoSystem",
                                    new Object[]{item.getValueCoding().getCode()},
                                    LocaleHelper.getLocaleFromString(currentLocale));
                            } else {
                                answerText = messageSource.getMessage(
                                    "import.fhir.element.codeNoDisplay",
                                    new Object[]{item.getValueCoding().getCode(),
                                        item.getValueCoding().getSystem()},
                                    LocaleHelper.getLocaleFromString(currentLocale));
                            }
                            localizedAnswerText.put(currentLocale, answerText);
                        }
                    }
                    // Set the exportField value
                    value = importQuestionResult.getIdentifier().replace(".", "u002E")
                        .replace("_", "u005F") + "_" + item.getValueCoding().getCode()
                        .replace(".", "u002E").replace("_", "u005F");
                } else if (item.getValue() instanceof StringType) {
                    // Set the translated answer labels
                    localizedAnswerText = FhirDstu3Helper.getLanugageMapFromLanguageExtension(
                        item.getValueStringType());

                    // Set the exportField value
                    value = importQuestionResult.getIdentifier().replace(".", "u002E")
                        .replace("_", "u005F") + "_" + item.getValueStringType().asStringValue()
                        .replace(".", "u002E").replace("_", "u005F");
                } else if (item.getValue() instanceof DateType) {
                    // The option may contain date as choosable answer, just
                    // parse it to string
                    value = importQuestionResult.getIdentifier().replace(".", "u002E")
                        .replace("_", "u005F") + "_" + item.getValueDateType().asStringValue()
                        .replace(".", "u002E").replace("_", "u005F");
                    for (String currentLocale : question.getLocalizedQuestionText().keySet()) {
                        localizedAnswerText.put(currentLocale,
                            item.getValueDateType().asStringValue());
                    }
                } else if (item.getValue() instanceof IntegerType) {
                    // The option may contain integer as choosable answer,
                    // just parse it to string
                    value = importQuestionResult.getIdentifier().replace(".", "u002E")
                        .replace("_", "u005F") + "_" + item.getValueIntegerType().asStringValue()
                        .replace(".", "u002E").replace("_", "u005F");
                    for (String currentLocale : question.getLocalizedQuestionText().keySet()) {
                        localizedAnswerText.put(currentLocale,
                            item.getValueIntegerType().asStringValue());
                    }
                } else if (item.getValue() instanceof TimeType) {
                    // The option may contain time type as choosable answer,
                    // just parse it to string
                    value = importQuestionResult.getIdentifier().replace(".", "u002E")
                        .replace("_", "u005F") + "_" + item.getValueTimeType().asStringValue()
                        .replace(".", "u002E").replace("_", "u005F");
                    for (String currentLocale : question.getLocalizedQuestionText().keySet()) {
                        localizedAnswerText.put(currentLocale,
                            item.getValueTimeType().asStringValue());
                    }
                }
                // Create the answer
                SelectAnswer answer = new SelectAnswer(question, question.getIsEnabled(),
                    localizedAnswerText, false);
                if (item.getValue() instanceof Coding) {
                    answer.setValue(FhirDstu3Helper.getScoreFromExtension(item.getValueCoding()));
                    scoreValue = answer.getValue();
                }
                if (value != null) {
                    // Create the exportRules
                    for (ExportTemplate exportTemplate : exportTemplates) {
                        ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate,
                            value, answer);
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
            localizedAnswerText = FhirDstu3Helper.getLanugageMapFromLanguageExtension(
                item.getDisplayElement());
            String displayText;
            if (item.getDisplay() != null) {
                displayText = item.getDisplay();
            } else {
                if (system == null || system.isEmpty()) {
                    displayText = messageSource.getMessage(
                        "import.fhir.element" + ".codeNoDisplayNoSystem",
                        new Object[]{item.getCode()}, LocaleHelper.getLocaleFromString(locale));
                } else {
                    displayText = messageSource.getMessage("import.fhir.element" + ".codeNoDisplay",
                        new Object[]{item.getCode(), system},
                        LocaleHelper.getLocaleFromString(locale));
                }
            }

            // Create the answer
            SelectAnswer answer = new SelectAnswer(question, question.getIsEnabled(),
                localizedAnswerText, false);
            answer.setValue(FhirDstu3Helper.getScoreFromExtension(item.getDisplayElement()));
            scoreValue = answer.getValue();
            // Create the exportRules
            for (ExportTemplate exportTemplate : exportTemplates) {
                ExportRuleAnswer exportRuleAnswer = new ExportRuleAnswer(exportTemplate,
                    importQuestionResult.getIdentifier().replace(".", "u002E").replace("_", "u005F")
                        + "_" + item.getCode().replace(".", "u002E").replace("_", "u005F"), answer);
                exportRuleAnswer.setExportRuleFormat(new ExportRuleFormat());
                answer.addExportRule(exportRuleAnswer);
            }
        }

        // Add the validation messages for the mapped answer texts and score
        // values
        if (localizedAnswerText != null && !localizedAnswerText.isEmpty()) {
            for (String currentLocale : localizedAnswerText.keySet()) {
                String text = localizedAnswerText.get(currentLocale);
                importQuestionResult.addValidationMessage("import.fhir.option.result",
                    new String[]{text});
            }
            if (scoreValue != null) {
                importQuestionResult.addValidationMessage("import.fhir.option.resultScore",
                    new String[]{scoreValue.toString()});
            }
        }
    }

    /**
     * Maps FHIR {@link QuestionnaireItemEnableWhenComponent enableWhen} element to MoPat
     * {@link Condition condition}.
     *
     * @param enableWhen                {@link QuestionnaireItemEnableWhenComponent EnableWhen}
     *                                  element to convert to {@link Condition} object.
     * @param items                     List of {@link QuestionnaireItemComponent items} containing
     *                                  the element which acitvates the
     *                                  {@link QuestionnaireItemComponent targetItem}.
     * @param targetItem                {@link QuestionnaireItemComponent} the enableWhen element
     *                                  belongs to.
     * @param importQuestionnaireResult {@link ImportQuestionnaireResult} object containing the
     *                                  converted questionnaire and validationMessages.
     */
    public static void convertEnableWhenToCondition(
        final QuestionnaireItemEnableWhenComponent enableWhen,
        final QuestionnaireItemComponent targetItem,
        final ImportQuestionnaireResult importQuestionnaireResult,
        final List<QuestionnaireItemComponent> items) {
        LOGGER.debug("Enter convertEnableWhenToCondition" + "(QuestionnaireItemEnableWhenComponent "
            + "enableWhen, QuestionnaireItemComponent " + "targetItem, ImportQuestionnaireResult "
            + "importQuestionnaireResult, " + "List<QuestionnaireItemComponent> items)");
        ImportQuestionListResult importQuestionListResult = importQuestionnaireResult.getQuestionListResults()
            .get(0);
        ImportQuestionResult targetQuestionResult = importQuestionListResult.getQuestionResultByIdentifier(
            targetItem.getLinkId());
        ImportQuestionResult triggerQuestionResult = importQuestionListResult.getQuestionResultByIdentifier(
            enableWhen.getQuestion());
        QuestionnaireItemComponent triggerItem = FhirDstu3Helper.getItemByLinkId(
            enableWhen.getQuestion(), items);

        // The common case is that the enableWhen element activate the
        // targetQuestion, so it is initial disabled.
        targetQuestionResult.getQuestion().setIsEnabled(Boolean.FALSE);
        ConditionActionType action = ConditionActionType.ENABLE;
        switch (triggerItem.getType()) {
            case CHOICE:
                if (enableWhen.hasHasAnswer()) {
                    // If enableWhen's hasAnswer element is set, check its'
                    // value and create for each adhering answer of the
                    // question a condition
                    if (!enableWhen.getHasAnswer()) {
                        targetQuestionResult.getQuestion().setIsEnabled(Boolean.TRUE);
                        action = ConditionActionType.DISABLE;
                    }

                    for (Answer answer : triggerQuestionResult.getQuestion().getAnswers()) {
                        answer.addCondition(
                            new SelectAnswerCondition(answer, targetQuestionResult.getQuestion(),
                                action, null));
                        // Gets the localized answer text by the current
                        // locale, if the locale doesn't exist in the map, it
                        // returns the first value, same for the question at
                        // the next line
                        // These params have to be set for the
                        // validationMessage. This way it's more clearly
                        // arranged.
                        Map<String, String> localizedLabel = ((SelectAnswer) answer).getLocalizedLabel();
                        triggerQuestionResult.addValidationMessage(
                            "import.fhir.condition.selectAnswerCondition", new String[]{
                                localizedLabel.getOrDefault(
                                    LocaleContextHolder.getLocale().toString(),
                                    localizedLabel.values().toArray()[0].toString()),
                                targetQuestionResult.getQuestion()
                                    .getLocalizedQuestionText().getOrDefault(
                                    LocaleContextHolder.getLocale().toString(),
                                    targetQuestionResult.getQuestion().getLocalizedQuestionText()
                                        .values().toArray()[0].toString()), action.name()});
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
                                answer.addCondition(new SelectAnswerCondition(answer,
                                    targetQuestionResult.getQuestion(), action, null));
                                // Gets the localized answer text by the
                                // current locale, if the locale doesn't
                                // exist in the map, it returns the first
                                // value, same for the question at the next line
                                // These params have to be set for the
                                // validationMessage. This way it's more
                                // clearly arranged.
                                Map<String, String> localizedLabel = ((SelectAnswer) answer).getLocalizedLabel();
                                triggerQuestionResult.addValidationMessage(
                                    "import.fhir.condition" + ".selectAnswerCondition",
                                    new String[]{localizedLabel.getOrDefault(
                                        LocaleContextHolder.getLocale().toString(),
                                        localizedLabel.values().toArray()[0].toString()),
                                        targetQuestionResult.getQuestion()
                                            .getLocalizedQuestionText().getOrDefault(
                                            LocaleContextHolder.getLocale().toString(),
                                            targetQuestionResult.getQuestion()
                                                .getLocalizedQuestionText().values()
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
                if (enableWhen.hasHasAnswer()) {
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
                        if (numberInputAnswer.getMaxValue() != null
                            && numberInputAnswer.getMinValue() != null && (
                            numberInputAnswer.getMaxValue() < value
                                || numberInputAnswer.getMinValue() > value)) {
                            return;
                        }
                    }

                    if (answer instanceof SliderAnswer sliderAnswer) {
                        if (sliderAnswer.getMaxValue() < value
                            || sliderAnswer.getMinValue() > value) {
                            return;
                        }
                    }
                    // Create the condition, the thresholdComparisonType is
                    // always "==" because there's no type specified by the
                    // fhir specification
                    triggerQuestionResult.getQuestion().getAnswers().get(0).addCondition(
                        new SliderAnswerThresholdCondition(
                            triggerQuestionResult.getQuestion().getAnswers().get(0),
                            targetQuestionResult.getQuestion(), action, null,
                            ThresholdComparisonType.EQUALS, value));
                    triggerQuestionResult.addValidationMessage(
                        "import.fhir.condition" + ".sliderAnswerThresholdCondition", new String[]{
                            targetQuestionResult.getQuestion()
                                .getLocalizedQuestionText().getOrDefault(
                                LocaleContextHolder.getLocale().toString(),
                                targetQuestionResult.getQuestion().getLocalizedQuestionText()
                                    .values().toArray()[0].toString()),
                            ThresholdComparisonType.EQUALS.getTextValue(), value.toString(),
                            action.name()});
                }
            default:
                break;
        }

        for (QuestionnaireItemComponent childItem : targetItem.getItem()) {
            convertEnableWhenToCondition(enableWhen, childItem, importQuestionnaireResult, items);
        }
    }
}
