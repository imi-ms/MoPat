package de.imi.mopat.io.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.io.MetadataExporter;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.FreetextAnswer;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionActionType;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.conditions.ThresholdComparisonType;
import de.imi.mopat.model.enumeration.QuestionType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.model.CodeType;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.DateType;
import org.hl7.fhir.r5.model.DecimalType;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.IntegerType;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireAnswerConstraint;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemAnswerOptionComponent;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemEnableWhenComponent;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemOperator;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemType;
import org.hl7.fhir.r5.model.StringType;
import org.springframework.context.MessageSource;

/**
 * An exporter for FHIR metadata reprasentation of a {@link Questionnaire}.
 */
public class MetadataExporterFhirR5 implements MetadataExporter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        MetadataExporterFhirR5.class);
    private final String ANSWER_OID = "MoPat/Answer/";

    private String currentDefaultLanguage = "en_GB";

    /**
     * This method converts a {@link Questionnaire} object to a fhir questionnaire resource and
     * encodes this resource to a string returned as byte array.
     *
     * @param questionnaire    Metadata to convert.
     * @param messageSource    Containing message resources.
     * @param configurationDao Data access object to get the system's configuration state.
     * @return Byte array containing the bytes of the metadata xml string.
     */
    @Override
    public byte[] export(final Questionnaire questionnaire, final MessageSource messageSource,
        final ConfigurationDao configurationDao, final ConfigurationGroupDao configurationGroupDao,
        final ExportTemplateDao exportTemplateDao, final QuestionnaireDao questionnaireDao,
        final QuestionDao questionDao, final ScoreDao scoreDao) {

        this.currentDefaultLanguage = determineDefaultLanguageForQuestionnaire(questionnaire);

        org.hl7.fhir.r5.model.Questionnaire fhirQuestionnaire = new org.hl7.fhir.r5.model.Questionnaire();

        fhirQuestionnaire.setName(questionnaire.getName());
        fhirQuestionnaire.setTitleElement(
            convertLocalizedTextToStringType(questionnaire.getLocalizedDisplayName()));
        fhirQuestionnaire.setDescription(questionnaire.getDescription());

        if (questionnaire.getUpdatedAt() != null) {
            fhirQuestionnaire.setDate(new Date(questionnaire.getUpdatedAt().getTime()));
        }

        fhirQuestionnaire.setStatus(
            questionnaire.isPublished() ? PublicationStatus.ACTIVE : PublicationStatus.DRAFT);

        Map<Question, QuestionnaireItemComponent> triggeringQuestions = new HashMap<>();
        Map<Question, QuestionnaireItemComponent> targetQuestions = new HashMap<>();

        for (Question question : questionnaire.getQuestions()) {
            QuestionnaireItemComponent item = convertQuestionToFhirItem(question, configurationDao);
            fhirQuestionnaire.addItem(item);

            QuestionnaireItemComponent freeTextItem = createFreeTextItemIfNeeded(question);
            if (freeTextItem != null) {
                fhirQuestionnaire.addItem(freeTextItem);
            }

            if (question.hasConditionsAsTrigger()) {
                triggeringQuestions.put(question, item);
            }
            if (question.hasConditionsAsTarget()) {
                targetQuestions.put(question, item);
            }
        }

        convertConditionsToEnableWhen(triggeringQuestions, targetQuestions);

        return serializeFhirQuestionnaire(fhirQuestionnaire);
    }

    /**
     * Converts a map of localized texts into a FHIR {@link StringType} with language translation
     * extensions.
     * <p>
     * The first entry in the map sets the primary value of the {@code StringType}. All entries,
     * including the first, are added as language extensions following the FHIR translation
     * structure.
     * </p>
     *
     * @param localizedText a map where the keys are language codes (e.g., "en_US") and the values
     *                      are the localized strings
     * @return a {@link StringType} instance containing the value and language translation
     * extensions
     */
    private StringType convertLocalizedTextToStringType(Map<String, String> localizedText) {
        Map.Entry<String, String> defaultEntry = localizedText.entrySet().stream()
            .filter(e -> e.getKey().equals(currentDefaultLanguage)).findFirst().orElse(null);

        StringType text = new StringType();

        if (defaultEntry != null) {
            text.setValue(defaultEntry.getValue());
        }

        addRemainingLanguageExtensions(text, localizedText);

        return text;
    }

    /**
     * Adds a FHIR translation language extension to the given {@link StringType}.
     * <p>
     * The extension uses the standard FHIR URL for translation and includes:
     * <ul>
     *   <li>a "lang" sub-extension with the language code (underscores replaced with hyphens),</li>
     *   <li>a "content" sub-extension with the localized text value.</li>
     * </ul>
     * </p>
     *
     * @param text  the {@link StringType} to which the language extension will be added
     * @param entry a map entry where the key is the language code (e.g., "en_US") and the value is
     *              the localized text
     */
    private void addLanguageExtension(StringType text, Map.Entry<String, String> entry) {
        Extension languageExtension = new Extension();
        languageExtension.setUrl("http://hl7.org/fhir/StructureDefinition/translation");
        languageExtension.addExtension("lang", new CodeType(entry.getKey().replace("_", "-")));
        languageExtension.addExtension("content", new StringType(entry.getValue()));
        text.addExtension(languageExtension);
    }

    /**
     * Adds language translation extensions for all remaining entries in the iterator to the given
     * {@link StringType}.
     * <p>
     * This method iterates through the provided map entries and adds each as a language extension
     * using {@link #addLanguageExtension(StringType, Map.Entry)}.
     * </p>
     *
     * @param text          the {@link StringType} to which the language extensions will be added
     * @param localizedText a map where the keys are language codes (e.g., "en_US") with the
     *                      corresponding values being the localized strings.
     */
    private void addRemainingLanguageExtensions(StringType text,
        Map<String, String> localizedText) {
        localizedText.entrySet().forEach(entry -> {
            addLanguageExtension(text, entry);
        });
    }

    /**
     * Converts a domain {@link Question} object into a FHIR {@link QuestionnaireItemComponent}.
     * <p>
     * The method sets common properties such as link ID, required flag, and localized question
     * text. It then converts the question according to its specific {@link QuestionType} into the
     * corresponding FHIR questionnaire item type and structure.
     * </p>
     * <p>
     * Supported question types include choice-based, slider, number input, date, free text,
     * barcode, and info text. For choice questions, it adds min/max occurrence extensions.
     * </p>
     *
     * @param question         the domain {@link Question} to convert
     * @param configurationDao the DAO to retrieve configuration values, such as FHIR system URI
     *                         (used in choice conversions)
     * @return a FHIR {@link QuestionnaireItemComponent} representing the question in the FHIR
     * questionnaire format
     */
    private QuestionnaireItemComponent convertQuestionToFhirItem(Question question,
        ConfigurationDao configurationDao) {
        QuestionnaireItemComponent item = new QuestionnaireItemComponent();

        item.setLinkId(Question.class.getSimpleName() + "/" + question.getId());

        //Repeat and Required not allowed for display items
        if (question.getQuestionType() != QuestionType.INFO_TEXT) {
            item.setRepeats(false);
            item.setRequired(question.getIsRequired());
        }

        item.setTextElement(convertLocalizedTextToStringType(question.getLocalizedQuestionText()));
        switch (question.getQuestionType()) {
            case DROP_DOWN:
            case MULTIPLE_CHOICE:
            case BODY_PART:
                convertChoiceQuestion(item, question, configurationDao);
                break;

            case INFO_TEXT:
                item.setType(QuestionnaireItemType.DISPLAY);
                break;

            case NUMBER_CHECKBOX_TEXT:
                // fallthrough to NUMBER_CHECKBOX to handle Slider and freetext creation
            case NUMBER_CHECKBOX:
            case SLIDER:
                convertSliderQuestion(item, question);
                break;

            case NUMBER_INPUT:
                convertNumberInputQuestion(item, question);
                break;

            case IMAGE:
                // TODO: handle images here if needed
                break;

            case DATE:
                convertDateQuestion(item, question);
                break;

            case FREE_TEXT:
            case BARCODE:
                item.setType(QuestionnaireItemType.TEXT);
                break;

            default:
                // default fallback or throw?
                break;
        }

        // Add min/max extensions for choice questions
        if (question.getQuestionType() == QuestionType.DROP_DOWN
            || question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            item.addExtension("http://hl7.org/fhir/StructureDefinition/questionnaire-minOccurs",
                new IntegerType(question.getMinNumberAnswers()));
            item.setRequired(true);

            if (question.getMaxNumberAnswers() > 1) {
                item.setRepeats(true);
                item.addExtension("http://hl7.org/fhir/StructureDefinition/questionnaire-maxOccurs",
                    new IntegerType(question.getMaxNumberAnswers()));
            } else if (question.getMaxNumberAnswers() == 1) {
                item.setRepeats(false);
            }
        }

        return item;
    }

    /**
     * Converts the answers of a choice-based {@link Question} (DROP_DOWN or MULTIPLE_CHOICE) into
     * FHIR {@link QuestionnaireItemAnswerOptionComponent}s and adds them to the provided
     * {@link QuestionnaireItemComponent}.
     * <p>
     * For each {@link SelectAnswer} that is not marked as "isOther", this method creates an option
     * with:
     * <ul>
     *   <li>a localized display text with language extensions,</li>
     *   <li>a coding with a system URI, code, and optional ordinal value extension.</li>
     * </ul>
     * If an answer is marked as "isOther", the item type is set to OPENCHOICE.
     * </p>
     *
     * @param item             the FHIR questionnaire item to add options to
     * @param question         the domain question containing the answers to convert
     * @param configurationDao DAO to retrieve configuration values such as the FHIR system URI
     */
    private void convertChoiceQuestion(QuestionnaireItemComponent item, Question question,
        ConfigurationDao configurationDao) {
        item.setType(QuestionnaireItemType.CODING);
        for (Answer answer : question.getAnswers()) {
            if (answer instanceof SelectAnswer selectAnswer && !selectAnswer.getIsOther()) {
                QuestionnaireItemAnswerOptionComponent option = new QuestionnaireItemAnswerOptionComponent();
                Coding coding = new Coding();
                StringType display = convertLocalizedTextToStringType(
                    selectAnswer.getLocalizedLabel());

                coding.setDisplayElement(display);
                coding.setCode(ANSWER_OID + answer.getId());
                coding.setSystem(configurationDao.getFHIRsystemURI());

                if (selectAnswer.getValue() != null) {
                    coding.addExtension(
                        "http://hl7.org/fhir/StructureDefinition/questionnaire-ordinalValue",
                        new DecimalType(selectAnswer.getValue()));
                }
                option.setValue(coding);
                item.addAnswerOption(option);
            }
            else {
                item.setAnswerConstraint(QuestionnaireAnswerConstraint.OPTIONSORSTRING);
            }
        }
    }

    /**
     * Converts a slider-based {@link Question} (NUMBER_CHECKBOX, NUMBER_CHECKBOX_TEXT, SLIDER) into
     * a FHIR {@link QuestionnaireItemComponent} with appropriate type and value range extensions.
     * <p>
     * Determines whether the slider answer should be represented as a DECIMAL or INTEGER type based
     * on the presence of decimal stepsize or minimum value. Adds the minValue and maxValue
     * extensions to the item.
     * </p>
     * <p>
     * Note: For NUMBER_CHECKBOX_TEXT questions, the associated free text item creation is handled
     * separately.
     * </p>
     *
     * @param item     the FHIR questionnaire item to configure
     * @param question the domain question containing slider answers
     */
    private void convertSliderQuestion(QuestionnaireItemComponent item, Question question) {
        // Extract slider related answers
        SliderAnswer sliderAnswer = (SliderAnswer) question.getAnswers().get(0);

        if (question.getQuestionType() == QuestionType.NUMBER_CHECKBOX_TEXT) {
            // Create freeTextItem for freetext label
            // This will be handled separately outside this method
        }

        // Determine if type should be decimal or integer
        boolean isDecimal = sliderAnswer.getStepsize() - sliderAnswer.getStepsize().intValue() > 0
            || sliderAnswer.getMinValue() - sliderAnswer.getMinValue().intValue() > 0;

        item.setType(isDecimal ? QuestionnaireItemType.DECIMAL : QuestionnaireItemType.INTEGER);
        item.addExtension("http://hl7.org/fhir/StructureDefinition/minValue",
            new DecimalType(sliderAnswer.getMinValue()));
        item.addExtension("http://hl7.org/fhir/StructureDefinition/maxValue",
            new DecimalType(sliderAnswer.getMaxValue()));
    }

    /**
     * Converts a NUMBER_INPUT type {@link Question} into a FHIR
     * {@link QuestionnaireItemComponent}.
     * <p>
     * Determines whether the answer should be represented as an INTEGER or DECIMAL type based on
     * the stepsize. Adds optional minValue and maxValue extensions if they are specified.
     * </p>
     *
     * @param item     the FHIR questionnaire item to configure
     * @param question the domain question containing the number input answer
     */
    private void convertNumberInputQuestion(QuestionnaireItemComponent item, Question question) {
        NumberInputAnswer numberInputAnswer = (NumberInputAnswer) question.getAnswers().get(0);
        boolean isInteger = numberInputAnswer.getStepsize() != null
            && (numberInputAnswer.getStepsize() - numberInputAnswer.getStepsize().intValue()) == 0;

        item.setType(isInteger ? QuestionnaireItemType.INTEGER : QuestionnaireItemType.DECIMAL);

        if (numberInputAnswer.getMinValue() != null) {
            item.addExtension("http://hl7.org/fhir/StructureDefinition/minValue",
                new DecimalType(numberInputAnswer.getMinValue()));
        }
        if (numberInputAnswer.getMaxValue() != null) {
            item.addExtension("http://hl7.org/fhir/StructureDefinition/maxValue",
                new DecimalType(numberInputAnswer.getMaxValue()));
        }
    }

    /**
     * Converts a DATE type {@link Question} into a FHIR {@link QuestionnaireItemComponent}.
     * <p>
     * Sets the item type to DATE and adds optional minValue and maxValue extensions based on the
     * start and end dates of the {@link DateAnswer}, formatted as "yyyy-MM-dd".
     * </p>
     *
     * @param item     the FHIR questionnaire item to configure
     * @param question the domain question containing the date answer
     */
    private void convertDateQuestion(QuestionnaireItemComponent item, Question question) {
        item.setType(QuestionnaireItemType.DATE);

        DateAnswer dateAnswer = (DateAnswer) question.getAnswers().get(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (dateAnswer.getStartDate() != null) {
            item.addExtension("http://hl7.org/fhir/StructureDefinition/minValue",
                new DateType(dateFormat.format(dateAnswer.getStartDate())));
        }
        if (dateAnswer.getEndDate() != null) {
            item.addExtension("http://hl7.org/fhir/StructureDefinition/maxValue",
                new DateType(dateFormat.format(dateAnswer.getEndDate())));
        }
    }

    /**
     * Creates a FHIR free text {@link QuestionnaireItemComponent} for questions of type
     * NUMBER_CHECKBOX_TEXT.
     * <p>
     * If the question is of type NUMBER_CHECKBOX_TEXT, this method extracts the localized free text
     * label from the associated {@link SliderFreetextAnswer} and converts it to a FHIR
     * {@link StringType} for the item's text element.
     * </p>
     *
     * @param question the domain question to convert
     * @return a configured free text questionnaire item if the question type is
     * NUMBER_CHECKBOX_TEXT, otherwise null
     */
    private QuestionnaireItemComponent createFreeTextItemIfNeeded(Question question) {
        if (question.getQuestionType() == QuestionType.NUMBER_CHECKBOX_TEXT) {
            SliderFreetextAnswer sliderFreetextAnswer = (SliderFreetextAnswer) question.getAnswers()
                .get(0);
            QuestionnaireItemComponent freeTextItem = new QuestionnaireItemComponent();
            freeTextItem.setLinkId(Question.class.getSimpleName() + "/" + question.getId() + "-ft");
            freeTextItem.setType(QuestionnaireItemType.TEXT);
            freeTextItem.setTextElement(
                convertLocalizedTextToStringType(sliderFreetextAnswer.getLocalizedFreetextLabel()));
            return freeTextItem;
        }
        return null;
    }

    /**
     * Converts domain-specific conditional logic on questions' answers into FHIR
     * {@link QuestionnaireItemEnableWhenComponent} rules to control the visibility or enablement of
     * target questionnaire items.
     * <p>
     * For each triggering question and its answers, this method inspects associated conditions.
     * Depending on the question type and condition action, it adds appropriate enableWhen
     * components to the target questionnaire items, thereby specifying when they should be enabled
     * or shown.
     * </p>
     *
     * <p><b>Supported question types and conditions:</b></p>
     * <ul>
     *   <li><b>MULTIPLE_CHOICE, DROP_DOWN:</b> Handles {@link SelectAnswerCondition} to enable target
     *       items when a specific answer option is selected.</li>
     *   <li><b>NUMBER_CHECKBOX, NUMBER_INPUT, SLIDER:</b> Handles {@link SliderAnswerThresholdCondition}
     *       to enable or disable target items based on numeric threshold comparisons.</li>
     * </ul>
     *
     * @param triggeringQuestions a map of questions that trigger conditions mapped to their
     *                            corresponding FHIR questionnaire items
     * @param targetQuestions     a map of target questions (to be enabled or disabled) mapped to
     *                            their FHIR questionnaire items
     */
    private void convertConditionsToEnableWhen(
        Map<Question, QuestionnaireItemComponent> triggeringQuestions,
        Map<Question, QuestionnaireItemComponent> targetQuestions) {
        for (Map.Entry<Question, QuestionnaireItemComponent> currentEntry : triggeringQuestions.entrySet()) {
            Question question = currentEntry.getKey();
            QuestionnaireItemComponent item = currentEntry.getValue();

            for (Answer answer : question.getAnswers()) {
                for (Condition condition : answer.getConditions()) {
                    if (!(condition.getTarget() instanceof Question)) {
                        continue;
                    }

                    switch (question.getQuestionType()) {
                        case MULTIPLE_CHOICE:
                        case DROP_DOWN:
                            SelectAnswerCondition selectAnswerCondition = (SelectAnswerCondition) condition;
                            if (selectAnswerCondition.getAction() == ConditionActionType.ENABLE) {
                                for (QuestionnaireItemAnswerOptionComponent option : item.getAnswerOption()) {
                                    try {
                                        if (option.getValueCoding().getCode()
                                            .equals(ANSWER_OID + answer.getId())) {
                                            QuestionnaireItemEnableWhenComponent enableWhen = new QuestionnaireItemEnableWhenComponent();
                                            enableWhen.setAnswer(option.getValueCoding());
                                            enableWhen.setOperator(QuestionnaireItemOperator.EQUAL);
                                            enableWhen.setQuestion(item.getLinkId());
                                            targetQuestions.get(selectAnswerCondition.getTarget())
                                                .addEnableWhen(enableWhen);
                                        }
                                    } catch (FHIRException ex) {
                                        // Log or handle exception if needed
                                    }
                                }
                            }
                            break;

                        case NUMBER_CHECKBOX:
                        case NUMBER_INPUT:
                        case SLIDER:
                            SliderAnswerThresholdCondition sliderAnswerCondition = (SliderAnswerThresholdCondition) condition;
                            boolean shouldAddEnableWhen =
                                (sliderAnswerCondition.getAction() == ConditionActionType.ENABLE
                                    && sliderAnswerCondition.getThresholdComparisonType()
                                    == ThresholdComparisonType.EQUALS) || (
                                    sliderAnswerCondition.getAction() == ConditionActionType.DISABLE
                                        && sliderAnswerCondition.getThresholdComparisonType()
                                        == ThresholdComparisonType.NOT_EQUALS);

                            if (shouldAddEnableWhen) {
                                QuestionnaireItemEnableWhenComponent enableWhen = new QuestionnaireItemEnableWhenComponent();
                                enableWhen.setAnswer(
                                    new DecimalType(sliderAnswerCondition.getThreshold()));
                                enableWhen.setOperator(QuestionnaireItemOperator.EQUAL);
                                enableWhen.setQuestion(item.getLinkId());
                                targetQuestions.get(sliderAnswerCondition.getTarget())
                                    .addEnableWhen(enableWhen);
                            }
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }

    /**
     * Serializes a FHIR r4b {@link org.hl7.fhir.r5.model.Questionnaire} resource into a
     * pretty-printed XML byte array.
     * <p>
     * Uses the HAPI FHIR library's XML parser to convert the questionnaire object into an XML
     * string, then encodes it into bytes for further use or storage.
     * </p>
     *
     * @param fhirQuestionnaire the FHIR Questionnaire resource to serialize
     * @return a byte array containing the serialized XML representation of the questionnaire, or an
     * empty byte array if serialization fails
     */
    private byte[] serializeFhirQuestionnaire(
        org.hl7.fhir.r5.model.Questionnaire fhirQuestionnaire) {
        FhirContext ctx = FhirContext.forR5();
        IParser parser = ctx.newXmlParser().setPrettyPrint(true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            String questionnaireAsFhirXmlString = parser.encodeResourceToString(fhirQuestionnaire);
            bos.write(questionnaireAsFhirXmlString.getBytes());
            return bos.toByteArray();
        } catch (IOException | NullPointerException ex) {
            LOGGER.error("Error while exporting questionnaire as FHIR metadata file: {}", ex);
        }
        return new byte[0];
    }

    private String determineDefaultLanguageForQuestionnaire(Questionnaire questionnaire) {
        List<String> localeList = getLocaleListFromQuestionnaire(questionnaire);

        if (localeList.isEmpty() || localeList.contains("en_GB")) {
            return "en_GB";
        } else if (localeList.contains("en_US")) {
            return "en_US";
        } else if (localeList.contains("de_DE")) {
            return "de_DE";
        } else if (localeList.contains("fr_FR")) {
            return "fr_FR";
        } else if (localeList.contains("it_IT")) {
            return "it_IT";
        } else if (localeList.contains("es_ES")) {
            return "es_ES";
        } else {
            return localeList.get(0);
        }

    }

    private List<String> getLocaleListFromQuestionnaire(Questionnaire questionnaire) {
        return questionnaire.getLocalizedDisplayName().keySet().stream().toList();
    }

}
