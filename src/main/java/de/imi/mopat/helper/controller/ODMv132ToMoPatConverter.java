package de.imi.mopat.helper.controller;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.ExportRuleAnswer;
import de.imi.mopat.model.ExportRuleFormat;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.FreetextAnswer;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.conditions.ConditionActionType;
import de.imi.mopat.model.conditions.ConditionTarget;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.enumeration.CodedValueType;
import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.model.enumeration.ExportNumberType;
import de.imi.mopat.model.enumeration.ExportRoundingStrategyType;
import de.unimuenster.imi.org.cdisc.odm.v132.Comparator;
import de.unimuenster.imi.org.cdisc.odm.v132.DataType;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCheckValue;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeList;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeListItem;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeListRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionConditionDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionDescription;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormalExpression;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionMetaDataVersion;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionQuestion;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionRangeCheck;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudyEventDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionTranslatedText;
import de.unimuenster.imi.org.cdisc.odm.v132.YesOrNo;

import java.util.HashSet;
import java.util.Locale;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO [bt] comment ODMv132ToMoPatConverter
 */
public class ODMv132ToMoPatConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ODMv132ToMoPatConverter.class);

    public static final String TRANSLATED_TEXT_LANG_DE_DE = "de-DE";
    public static final String TRANSLATED_TEXT_LANG_DE = "de";

    public static final String MOPAT_LANG_DE_DE = "de_DE";

    /**
     * Default value for the minimum of {@link QuestionType#SLIDER},
     * {@link QuestionType#NUMBER_CHECKBOX} and {@link QuestionType#NUMBER_CHECKBOX_TEXT} if the ODM
     * to convert does not provide a {@link ODMcomplexTypeDefinitionRangeCheck} for 'GE'.
     */
    public static final Integer DEFAULT_MIN_VALUE = 0;

    /**
     * Default value for the maximum of {@link QuestionType#SLIDER},
     * {@link QuestionType#NUMBER_CHECKBOX} and {@link QuestionType#NUMBER_CHECKBOX_TEXT} if the ODM
     * to convert does not provide a {@link ODMcomplexTypeDefinitionRangeCheck} for 'LE'.
     */
    public static final Integer DEFAULT_MAX_VALUE = 10;

    public static final Boolean DEFAULT_VERTICAL = false;
    public static final Integer DEFAULT_INTEGER_MIN_MAX_DIFFERENCE = 10;
    public static final Double DEFAULT_INTEGER_STEPSIZE = 1d;
    /**
     * If you change the stepsize, you have to change it in the question/edit.jsp as well.
     */
    public static final Double DEFAULT_DOUBLE_STEPSIZE = 0.01d;
    public static final Integer DEFAULT_SIGNIFICANT_DIGITS = 2;
    /**
     * Default value for {@link Question#setMinNumberAnswers(Integer)}, since ODM does not provide
     * values for that
     */
    public static final Integer DEFAULT_MIN_NUMBER_ANSWERS = 1;
    /**
     * Default value for {@link Question#setMaxNumberAnswers(Integer)}, since ODM does not provide
     * values for that
     */
    public static final Integer DEFAULT_MAX_NUMBER_ANSWERS = 1;
    /**
     * Default value about how dates are formatted (as defined in the CDISC ODM v1.3.2
     * specification, 2.13 Data Formats)
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * Default value for the scenario that a question is of DataType 'boolean'. Will convert it into
     * a multiple choice question with the answer options 'Ja' and 'Nein'. This is the label for the
     * 'Ja' answer.
     */
    public static final String BOOLEAN_ANSWER_LABEL_YES = "Ja";
    /**
     * Default value for the scenario that a question is of DataType 'boolean'. Will convert it into
     * a multiple choice question with the answer options 'Ja' and 'Nein'. This is the label for the
     * 'Nein' answer.
     */
    public static final String BOOLEAN_ANSWER_LABEL_NO = "Nein";

    /**
     * TODO [bt] comment the method convertToBundle in ODMv132ToMoPatConverter !
     *
     * @param studyEventDef   TODO
     * @param changedBy       TODO
     * @param metaDataVersion TODO
     * @return never <code>null</code>.
     */
    public static final ImportBundleResult convertToBundle(
        ODMcomplexTypeDefinitionStudyEventDef studyEventDef, Long changedBy,
        ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion) {
        LOGGER.debug("Enter convertToBundle" + "(ODMcomplexTypeDefinitionStudyEventDef, Long, "
            + "ODMcomplexTypeDefinitionMetaDataVersion)");
        assert studyEventDef != null : "The given StudyEventDef was null";
        assert changedBy != null : "The given changedBy was null";
        assert metaDataVersion != null : "The given MetaDataVersion was null";

        ImportBundleResult result = new ImportBundleResult();

        // TODO go on implementing here
        LOGGER.debug("Leaving convertToBundle" + "(ODMcomplexTypeDefinitionStudyEventDef, Long, "
            + "ODMcomplexTypeDefinitionMetaDataVersion)");
        return result;
    }

    /**
     * TODO [bt] comment the method convertToQuestionnaire in
     * ODMv132ToMoPatConverter !
     *
     * @param importedODM     TODO
     * @param formDef         The given formDef must not be <code>null</code>.
     * @param changedBy       The given changedBy must not be <code>null</code>.
     * @param metaDataVersion The given metaDataVersion must not be
     *                        <code>null</code>.
     * @param exportTemplates TODO
     * @param messageSource   TODO
     * @return never <code>null</code>.
     * @throws jakarta.xml.xpath.XPathExpressionException TODO
     */
    public static final ImportQuestionnaireResult convertToQuestionnaire(MultipartFile importedODM,
        ODMcomplexTypeDefinitionFormDef formDef, Long changedBy,
        ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion,
        List<ExportTemplate> exportTemplates, MessageSource messageSource)
        throws XPathExpressionException {
        LOGGER.debug("Enter convertToQuestionnaire" + "(ODMcomplexTypeDefinitionFormDef, Long, "
            + "ODMcomplexTypeDefinitionMetaDataVersion)");
        assert formDef != null : "The given FormDef was null";
        assert changedBy != null : "The given changedBy was null";
        assert metaDataVersion != null : "The given MetaDataVersion was null";

        ImportQuestionnaireResult result = new ImportQuestionnaireResult();

        LOGGER.debug("Checking name for FormDef of OID {}", formDef.getOID());
        String name = formDef.getName().trim();
        int questionnaireNameMinLength = 3;
        int questionnaireNameMaxLength = 255;
        if (name.length() < questionnaireNameMinLength) {
            LOGGER.debug("The name ('{}') of FormDef of OID {} was shorter "
                    + "than {} characters (minimum questionnaire name length in "
                    + "MoPat), so I'll take its OID instead.", name, formDef.getOID(),
                questionnaireNameMinLength);
            result.addValidationMessage("import.odm.v132.formDef.nameTooShort",
                new String[]{formDef.getOID(), name});
            name = formDef.getOID().trim();
            if (name.length() < questionnaireNameMinLength) {
                LOGGER.debug("The OID ('{}') of the FormDef was too short as well,"
                    + " so I'll put some underscores in front of it.", name);
                result.addValidationMessage("import.odm.v132.formDef.oidTooShort",
                    new String[]{formDef.getOID()});
                do {
                    name = "_".concat(name);
                } while (name.length() < questionnaireNameMinLength);
            }
        }
        if (name.length() > questionnaireNameMaxLength) {
            LOGGER.debug("The name of FormDef of OID {} was larger than {} "
                    + "characters (maximum questionnaire name length in MoPat), so "
                    + "I'll cut it after {} characters.", formDef.getOID(), questionnaireNameMaxLength,
                questionnaireNameMaxLength);
            result.addValidationMessage("import.odm.v132.formDef.nameTooLong",
                new String[]{formDef.getOID(), name});
            name = name.substring(0, questionnaireNameMaxLength);
        }
        LOGGER.debug(
            "Name checking for FormDef of OID {} done. The " + "Questionnaire's name will be: {}",
            formDef.getOID(), name);
        LOGGER.debug("Checking for description in FormDef of OID {}", formDef.getOID());

        ODMcomplexTypeDefinitionDescription odmDescription = formDef.getDescription();
        String description;
        if (odmDescription == null) {
            LOGGER.debug("The FormDef of OID {} did not provide a Description. "
                + "Will take the name ('{}') instead.", formDef.getOID(), name);
            result.addValidationMessage("import.odm.v132.formDef.descriptionNull",
                new String[]{formDef.getOID(), name});
            description = name;
        } else {
            LOGGER.debug("The FormDef of OID {} has a Description. Will iterate "
                + "over its TranslatedTexts to find a proper one.", formDef.getOID());
            Map<String, String> translatedLocalizedText = getBestFittingTranslatedText(
                odmDescription.getTranslatedText(), questionnaireNameMinLength);
            if (translatedLocalizedText == null) {
                LOGGER.debug("The FormDef of OID {} did not have a "
                        + "Description->TranslatedText of minimum length ({}) and the"
                        + " matching lang/default attribute. Will take the name " + "('{}') instead.",
                    formDef.getOID(), questionnaireNameMinLength, name);
                result.addValidationMessage(
                    "import.odm.v132.formDef" + ".noMatchingDescriptionMinimumLength",
                    new String[]{formDef.getOID(), name});
                description = name;
            } else {
                LOGGER.debug("The FormDef of OID {} has a "
                        + "Description->TranslatedText of minimum length ({}) and the"
                        + " matching lang/default attribute. Questionnaire "
                        + "description will be set to: {}", formDef.getOID(),
                    questionnaireNameMinLength, translatedLocalizedText);
                description = translatedLocalizedText.entrySet().iterator().next().getValue();
            }
        }

        boolean isPublished = false;
        LOGGER.debug("All info for Questionnaire complete (name: '{}', "
            + "description: '{}', changedBy: '{}', isPublished: '{}'). "
            + "Will now create and set it.", name, description, changedBy, isPublished);
        Questionnaire questionnaire = new Questionnaire(name, description, changedBy, changedBy, isPublished);
        result.setQuestionnaire(questionnaire);

        List<ODMcomplexTypeDefinitionItemGroupRef> itemGroupRefList = formDef.getItemGroupRef();
        if (itemGroupRefList == null) {
            LOGGER.debug("The FormDef of OID {} did not contain any ItemGroupRefs."
                + " Won't create any question(group)s", formDef.getOID());
            result.addValidationMessage("import.odm.v132.formDef.itemGroupRefNull",
                new String[]{formDef.getOID()});
        } else {
            LOGGER.debug("The FormDef of OID {} contains at least one ItemGroupRef"
                    + ". Checking for ItemGroupDefs in MetaDataVersion of OID" + " {} now.",
                formDef.getOID(), metaDataVersion.getOID());
            List<ODMcomplexTypeDefinitionItemGroupDef> itemGroupDefList = metaDataVersion.getItemGroupDef();
            if (itemGroupDefList == null || itemGroupDefList.isEmpty()) {
                LOGGER.debug("The MetaDataVersion of OID {} did not contain any "
                        + "ItemGroupDefs. Cannot follow the referenced "
                        + "ItemGroupRefs. Will create an error message and " + "finish.",
                    metaDataVersion.getOID());
                result.addValidationMessage(
                    "import.odm.v132.metaDataVersion" + ".itemGroupDefListNullEmpty",
                    new String[]{metaDataVersion.getOID()});
            } else {
                LOGGER.debug("MetaDataVersion of OID {} contains at least one"
                        + " ItemGroupDef. Will iterate over them and the "
                        + "ItemGroupRefs in FormDef of OID {} to get referenced " + "question(group)s.",
                    metaDataVersion.getOID(), formDef.getOID());
                Map<ODMcomplexTypeDefinitionItemGroupRef, ODMcomplexTypeDefinitionItemGroupDef> matchingItemGroupRefDefs = new HashMap<>();
                List<ODMcomplexTypeDefinitionItemGroupRef> matchedItemGroupRefs = new ArrayList<>();
                List<ODMcomplexTypeDefinitionItemGroupDef> matchedItemGroupDefs = new ArrayList<>();
                for (ODMcomplexTypeDefinitionItemGroupRef itemGroupRef : itemGroupRefList) {
                    String refItemGroupOID = itemGroupRef.getItemGroupOID();
                    LOGGER.debug("Now checking the ItemGroupRef of OID {}.", refItemGroupOID);
                    boolean itemGroupRefFoundInItemGroupDef = false;
                    for (ODMcomplexTypeDefinitionItemGroupDef itemGroupDef : itemGroupDefList) {
                        LOGGER.debug("Now checking the ItemGroupDef with OID {} "
                                + "for equality with ItemGroupRef of ItemOID " + "{}",
                            itemGroupDef.getOID(), itemGroupRef.getItemGroupOID());
                        if (refItemGroupOID.equalsIgnoreCase(itemGroupDef.getOID())) {
                            LOGGER.debug("ItemGroupDef with OID {} matched the "
                                    + "reffered OID. Will put it into the "
                                    + "list of ItemGroupDefs to make a " + "QuestionGroup out of",
                                itemGroupDef.getOID());
                            itemGroupRefFoundInItemGroupDef = true;
                            matchingItemGroupRefDefs.put(itemGroupRef, itemGroupDef);
                            matchedItemGroupRefs.add(itemGroupRef);
                            matchedItemGroupDefs.add(itemGroupDef);
                            break;
                        }
                    }
                    if (!itemGroupRefFoundInItemGroupDef) {
                        LOGGER.debug("Iteration over ItemGroupDefs in "
                                + "MetaDataVersion of OID {} done. No ItemGroupDef "
                                + "for the ItemGroupRefOID {} could be found. The "
                                + "ItemGroupRef/-Def will not be converted/considered.",
                            metaDataVersion.getOID(), itemGroupRef.getItemGroupOID());
                        result.addValidationMessage(
                            "import.odm.v132.formDef" + ".noMatchingItemGroupDefForItemGroupRef",
                            new String[]{formDef.getOID(), itemGroupRef.getItemGroupOID()});
                    }
                }
                LOGGER.debug("Iteration over ItemGroupRefs in FormDef of OID "
                        + "{} and ItemGroupDefs in MetaDataVersion of OID {} done. "
                        + "Size of matched ItemGroupDefs: {}. Will now check for " + "OrderNumbers",
                    formDef.getOID(), metaDataVersion.getOID(), matchingItemGroupRefDefs.size());
                boolean orderNumberForAllItemGroupRefsPresent = true;
                Map<Integer, ODMcomplexTypeDefinitionItemGroupDef> orderedItemGroupDefs = new HashMap<Integer, ODMcomplexTypeDefinitionItemGroupDef>();
                for (ODMcomplexTypeDefinitionItemGroupRef itemGroupRef : matchedItemGroupRefs) {
                    LOGGER.debug("Now checking for an OrderNumber for ItemGroupRef" + " of OID {}",
                        itemGroupRef.getItemGroupOID());
                    BigInteger orderNumber = itemGroupRef.getOrderNumber();
                    if (orderNumber == null) {
                        LOGGER.debug("The ItemGroupRef with OID {} does not have "
                                + "an OrderNumber, so I'll write an error "
                                + "message, but continue in the order as " + "given in the odm file",
                            itemGroupRef.getItemGroupOID());
                        result.addValidationMessage(
                            "import.odm.v132.formDef.itemGroupRef" + ".noOrderNumber",
                            new String[]{formDef.getOID(), itemGroupRef.getItemGroupOID()});
                        orderNumberForAllItemGroupRefsPresent = false;
                    } else {
                        LOGGER.debug("The ItemGroupRef with OID {} has an "
                                + "OrderNumber, so I'll try to put the referenced "
                                + "ItemGroupDef onto its position ({}) in the List",
                            itemGroupRef.getItemGroupOID(), orderNumber.intValue());
                        if (orderedItemGroupDefs.get(orderNumber.intValue()) == null) {
                            LOGGER.debug("The List of ItemGroupDefs has a "
                                    + "free spot at position/OrderNumber {}, so I'll "
                                    + "just put the ItemGroupDef of OID {} there.",
                                orderNumber.intValue(), itemGroupRef.getItemGroupOID());
                            orderedItemGroupDefs.put(orderNumber.intValue(),
                                matchingItemGroupRefDefs.get(itemGroupRef));
                        } else {
                            LOGGER.debug("The List of ItemGroupDefs already has an"
                                    + " ItemGroupDef at position/OrderNumber "
                                    + "{}, which does not comply to the ODM "
                                    + "standard. Will not consider " + "orderNumbers in the end.",
                                orderNumber.intValue());
                            result.addValidationMessage(
                                "import.odm.v132.formDef.itemGroupRef" + ".multipleOrderNumber",
                                new String[]{formDef.getOID(), itemGroupRef.getItemGroupOID(),
                                    orderNumber.toString()});
                            orderNumberForAllItemGroupRefsPresent = false;
                        }
                    }
                }
                List<ODMcomplexTypeDefinitionItemGroupDef> itemGroupDefListToIterateOver;
                if (orderNumberForAllItemGroupRefsPresent) {
                    LOGGER.debug("All ItemGroupDefs of MetaDataVersion of OID"
                            + " {} that were referenced in FormDef of OID {} have a "
                            + "unique OrderNumber in their ItemGroupRef. Thus, I will"
                            + " consider the OrderNumbers when converting to Question" + "(group)s",
                        metaDataVersion.getOID(), formDef.getOID());
                    List<Integer> orderNumbers = new ArrayList<Integer>(
                        orderedItemGroupDefs.keySet());
                    Collections.sort(orderNumbers);
                    itemGroupDefListToIterateOver = new ArrayList<ODMcomplexTypeDefinitionItemGroupDef>();
                    for (Integer orderNumber : orderNumbers) {
                        itemGroupDefListToIterateOver.add(orderedItemGroupDefs.get(orderNumber));
                    }
                } else {
                    LOGGER.debug("Not all ItemGroupDefs of MetaDataVersion of"
                        + " OID {} that were referenced in FormDef of OID {} have"
                        + " a unique OrderNumber in their ItemGroupRef. Thus, I "
                        + "will not consider the OrderNumbers when converting to "
                        + "Question(group)s", metaDataVersion.getOID(), formDef.getOID());
                    itemGroupDefListToIterateOver = matchedItemGroupDefs;
                }

                LOGGER.debug("Collection of all necessary info for converting "
                    + "refered ItemGroupDefs of FormDef of OID {} done. "
                    + "Will now iterate over them and do the conversion.", formDef.getOID());
                for (ODMcomplexTypeDefinitionItemGroupDef itemGroupDef : itemGroupDefListToIterateOver) {
                    LOGGER.debug("Calling conversion method for ItemGroupDef of " + "OID {}.",
                        itemGroupDef.getOID());
                    int currentHighestQuestionPosition = result.getNumberOfQuestions();
                    result.addImportQuestionListResult(
                        convertToQuestionList(itemGroupDef, questionnaire,
                            currentHighestQuestionPosition, metaDataVersion, exportTemplates,
                            messageSource));
                }
            }
        }

        // Save the languages supported by the questions
        HashSet<String> questionLanguages = new HashSet<>();

        // Iterate through all the questions to see what conditions are correct
        for (ImportQuestionListResult questionList : result.getQuestionListResults()) {
            for (ImportQuestionResult odmQuestion : questionList.getImportQuestionResults()) {
                // Meanwhile collect all languages supported by the questions
                if (odmQuestion.getQuestion() != null) {
                    questionLanguages.addAll(
                        odmQuestion.getQuestion().getLocalizedQuestionText().keySet());
                }
                if (!odmQuestion.getConditions().isEmpty()) {
                    // Iterate through the conditions
                    for (ImportConditionResult condition : odmQuestion.getConditions()) {
                        if (condition.getTargetIdentifier() != null
                            && condition.getTriggerIdentifier() != null
                            && condition.getTriggerValue() != null) {
                            // Look for the ItemGroupData containing the
                            // trigger ItemGroupDef OID
                            for (ImportQuestionListResult questionListCondition : result.getQuestionListResults()) {
                                if (questionListCondition.getIdentifier()
                                    .equals(condition.getTargetIdentifier())) {
                                    // Look for the ItemData containing the
// trigger ItemDef OID
                                    boolean questionTriggerFound = false;
                                    for (ImportQuestionResult odmQuestionCondition : questionList.getImportQuestionResults()) {
                                        if (odmQuestionCondition.getIdentifier()
                                            .equals(condition.getTriggerIdentifier())) {
                                            questionTriggerFound = true;
                                            // Retrieve the trigger answer
                                            // Check if the question is
                                            // multiple choice and then look
                                            // for the answer that matches
                                            // the Code List value
                                            if (odmQuestionCondition.getQuestion().getQuestionType()
                                                .equals(QuestionType.MULTIPLE_CHOICE)) {
                                                for (int i = 0;
                                                    i < odmQuestionCondition.getQuestion()
                                                        .getAnswers().size(); i++) {
                                                    Double scoreValue = ((SelectAnswer) odmQuestionCondition.getQuestion()
                                                        .getAnswers().get(i)).getValue();
                                                    if (scoreValue != null && scoreValue.equals(
                                                        Double.parseDouble(
                                                            condition.getTriggerValue()))) {
                                                        // Add a new condition
                                                        new SelectAnswerCondition(
                                                            odmQuestionCondition.getQuestion()
                                                                .getAnswers().get(i),
                                                            odmQuestion.getQuestion(),
                                                            ConditionActionType.ENABLE, null);
                                                        // Set the question
                                                        // to "not enabled"
                                                        odmQuestion.getQuestion()
                                                            .setIsEnabled(false);
                                                        odmQuestion.addValidationMessage(
                                                            "import.odm" + ".v132"
                                                                + ".conditionDef.ConditionIncluded",
                                                            new String[]{scoreValue.toString(),
                                                                odmQuestion.getIdentifier(),
                                                                odmQuestionCondition.getIdentifier()});
                                                        break;

                                                    } else {
                                                        odmQuestion.addValidationMessage(
                                                            "import.odm" + ".v132"
                                                                + ".conditionDef.ScoreMissing",
                                                            new String[]{
                                                                odmQuestion.getIdentifier(),
                                                                odmQuestionCondition.getIdentifier()});
                                                    }
                                                }
                                            } //TODO import conditions for
                                            // more question types
                                            else {
                                                odmQuestion.addValidationMessage(
                                                    "import.odm.v132" + ".conditionDef"
                                                        + ".TriggerQuestionNotValid",
                                                    new String[]{odmQuestion.getIdentifier(),
                                                        odmQuestionCondition.getIdentifier()});
                                            }
                                        }
                                    }
                                    if (!questionTriggerFound) {
                                        odmQuestion.addValidationMessage(
                                            "import.odm.v132.conditionDef"
                                                + ".TriggerQuestionNotFound",
                                            new String[]{odmQuestion.getIdentifier()});
                                    }
                                }
                            }
                        } else {
                            if (condition.getTargetIdentifier() == null) {
                                odmQuestion.addValidationMessage("import.odm.v132.conditionDef"
                                        + ".ConditionMissingItemGroupData",
                                    new String[]{odmQuestion.getIdentifier()});
                            }
                            if (condition.getTriggerIdentifier() == null) {
                                odmQuestion.addValidationMessage(
                                    "import.odm.v132.conditionDef" + ".ConditionMissingItemData",
                                    new String[]{odmQuestion.getIdentifier()});
                            }
                            if (condition.getTriggerValue() == null) {
                                odmQuestion.addValidationMessage(
                                    "import.odm.v132.conditionDef" + ".ConditionMissingValue",
                                    new String[]{odmQuestion.getIdentifier()});
                            }
                        }
                    }

                }
            }
        }

        // Add all languages supported by the questions to the questionnaire
        Map<String, String> localizedDisplayNames = new HashMap<>();
        for (String questionLanguage : questionLanguages) {
            localizedDisplayNames.put(questionLanguage, name);
        }
        questionnaire.setLocalizedDisplayName(localizedDisplayNames);

        LOGGER.debug("Leaving convertToQuestionnaire" + "(ODMcomplexTypeDefinitionFormDef, Long, "
            + "ODMcomplexTypeDefinitionMetaDataVersion)");
        return result;
    }

    /**
     * TODO [bt] comment the method convertToQuestionGroup in
     * ODMv132ToMoPatConverter !
     *
     * @param itemGroupDef                   TODO
     * @param questionnaire                  TODO
     * @param currentHighestQuestionPosition TODO
     * @param metaDataVersion                TODO
     * @param exportTemplates                TODO
     * @param messageSource                  TODO
     * @return never <code>null</code>.
     */
    public static final ImportQuestionListResult convertToQuestionList(
        ODMcomplexTypeDefinitionItemGroupDef itemGroupDef, Questionnaire questionnaire,
        int currentHighestQuestionPosition, ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion,
        List<ExportTemplate> exportTemplates, MessageSource messageSource) {
        LOGGER.debug(
            "Enter convertToQuestionList" + "(ODMcomplexTypeDefinitionItemGroupDef, Questionnaire, "
                + "ODMcomplexTypeDefinitionMetaDataVersion)");
        assert itemGroupDef != null : "The given itemGroupDef was null";
        assert questionnaire != null : "The given questionnaire was null";
        assert metaDataVersion != null : "The given MetaDataVersion was null";

        ImportQuestionListResult result = new ImportQuestionListResult();

        LOGGER.debug("Checking the list of ItemRefs for ItemGroupDef of OID {}",
            itemGroupDef.getOID());
        List<ODMcomplexTypeDefinitionItemRef> itemRefList = itemGroupDef.getItemRef();
        if (itemRefList == null || itemRefList.isEmpty()) {
            LOGGER.debug("The ItemGroupDef of OID {} did not provide a List of "
                + "ItemRefs. Since MoPat needs them (they are the "
                + "questions), this ItemGroupDef will not be converted. "
                + "Creating an error message and leaving.", itemGroupDef.getOID());
            result.addValidationMessage("import.odm.v132.itemGroupDef.itemRefListNullEmpty",
                new String[]{itemGroupDef.getOID()});
        } else {
            LOGGER.debug("The ItemGroupDef of OID {} does provide a List of "
                    + "ItemRefs. Will now check for ItemDefs in " + "MetaDataVersion of OID {}",
                itemGroupDef.getOID(), metaDataVersion.getOID());
            List<ODMcomplexTypeDefinitionItemDef> itemDefList = metaDataVersion.getItemDef();
            if (itemDefList == null || itemDefList.isEmpty()) {
                LOGGER.debug("The MetaDataVersion of OID {} did not contain "
                    + "ItemDefs (getItemDef() == null || getItemDef().istEmpty())"
                    + ". Won't be able to create Questions out of the referred "
                    + "ItemDefs (in ItemGroupDef with OID {}). Will write an "
                    + "error message and finish.", metaDataVersion.getOID(), itemGroupDef.getOID());
                result.addValidationMessage("import.odm.v132.metaDataVersion.itemDefListNullEmpty",
                    new String[]{metaDataVersion.getOID()});
            } else {
                LOGGER.debug("The MetaDataVersion of OID {} contains at least"
                        + " one ItemDef. Will now iterate over them and the ItemRefs "
                        + "in ItemGroupDef of OID {} to collect the ItemDefs.",
                    metaDataVersion.getOID(), itemGroupDef.getOID());
                Map<ODMcomplexTypeDefinitionItemRef, ODMcomplexTypeDefinitionItemDef> matchingItemRefDefs = new HashMap<ODMcomplexTypeDefinitionItemRef, ODMcomplexTypeDefinitionItemDef>();
                List<ODMcomplexTypeDefinitionItemRef> matchedItemRefs = new ArrayList<ODMcomplexTypeDefinitionItemRef>();
                List<ODMcomplexTypeDefinitionItemDef> matchedItemDefs = new ArrayList<ODMcomplexTypeDefinitionItemDef>();
                Map<ODMcomplexTypeDefinitionItemDef, Boolean> itemDefIsRequiredMap = new HashMap<>();
                Map<ODMcomplexTypeDefinitionItemDef, ArrayList<ODMcomplexTypeDefinitionFormalExpression>> itemDefConditionFormalExpression = new HashMap<>();
                for (ODMcomplexTypeDefinitionItemRef itemRef : itemRefList) {
                    LOGGER.debug("Now checking for an ItemDef with OID {} (because"
                            + " it is reffered this way in the ItemGroupDef " + "of OID {}).",
                        itemRef.getItemOID(), itemGroupDef.getOID());
                    String refItemOID = itemRef.getItemOID();
                    boolean itemRefFoundInItemDef = false;
                    for (ODMcomplexTypeDefinitionItemDef itemDef : itemDefList) {
                        LOGGER.debug("Now checking the ItemDef with OID {} for "
                                + "equality with ItemRef of ItemOID {}", itemDef.getOID(),
                            itemRef.getItemOID());
                        if (refItemOID.equalsIgnoreCase(itemDef.getOID())) {
                            LOGGER.debug("ItemDef with OID {} matched the reffered"
                                + " OID. Will put it into the list of "
                                + "ItemDefs to make Question out of", itemDef.getOID());
                            itemRefFoundInItemDef = true;
                            matchingItemRefDefs.put(itemRef, itemDef);
                            matchedItemRefs.add(itemRef);
                            matchedItemDefs.add(itemDef);
                            boolean isRequired = itemRef.getMandatory().equals(YesOrNo.YES);
                            LOGGER.debug("While putting the ItemDef of OID {}"
                                    + " into the list, I remember the value '{}' for "
                                    + "this ItemDef as flag for being required or not.",
                                itemDef.getOID(), isRequired);
                            itemDefIsRequiredMap.put(itemDef, isRequired);

                            String collectionExceptionConditionOID = null;
                            ArrayList<ODMcomplexTypeDefinitionFormalExpression> formalExpressionsItemDef = new ArrayList<>();
                            if (itemRef.getCollectionExceptionConditionOID() != null) {
                                collectionExceptionConditionOID = itemRef.getCollectionExceptionConditionOID();
                            }
                            for (ODMcomplexTypeDefinitionConditionDef condition : metaDataVersion.getConditionDef()) {
                                if (condition.getOID().equals(collectionExceptionConditionOID)) {
                                    formalExpressionsItemDef = (ArrayList<ODMcomplexTypeDefinitionFormalExpression>) condition.getFormalExpression();
                                }
                            }
                            LOGGER.debug("While putting the ItemDef of OID {}"
                                    + " into the list, I remember the value '{}' for "
                                    + "this ItemDef as flag for havinf a " + "FormalExpression.",
                                itemDef.getOID(), collectionExceptionConditionOID);
                            itemDefConditionFormalExpression.put(itemDef, formalExpressionsItemDef);
                            break;
                        }
                    }
                    if (!itemRefFoundInItemDef) {
                        LOGGER.debug("Iteration over ItemDefs in "
                                + "MetaDataVersion of OID {} done. No ItemDef for the"
                                + " ItemRefOID {} could be found. The ItemRef/-Def "
                                + "will not be converted/considered.", metaDataVersion.getOID(),
                            itemRef.getItemOID());
                        result.addValidationMessage(
                            "import.odm.v132.itemGroupDef" + ".noMatchingItemDefForItemRef",
                            new String[]{itemGroupDef.getOID(), itemRef.getItemOID()});
                    }
                }
                LOGGER.debug("Iteration over ItemRefs in ItemGroupDef of OID "
                        + "{} and ItemDefs in MetaDataVersion of OID {} done. Size of"
                        + " matched ItemDefs: {}. Will now check for OrderNumbers",
                    itemGroupDef.getOID(), metaDataVersion.getOID(), matchingItemRefDefs.size());
                boolean orderNumberForAllItemRefsPresent = true;
                Map<Integer, ODMcomplexTypeDefinitionItemDef> orderedItemDefs = new HashMap<>();
                for (ODMcomplexTypeDefinitionItemRef itemRef : matchedItemRefs) {
                    LOGGER.debug("Now checking for an OrderNumber for ItemRef of " + "OID {}",
                        itemRef.getItemOID());
                    BigInteger orderNumber = itemRef.getOrderNumber();
                    if (orderNumber == null) {
                        LOGGER.debug("The ItemRef with OID {} does not have an "
                                + "OrderNumber, so I'll write an error "
                                + "message, but continue in the order as " + "given in the odm file",
                            itemRef.getItemOID());
                        result.addValidationMessage(
                            "import.odm.v132.itemGroupDef.itemRef" + ".noOrderNumber",
                            new String[]{itemGroupDef.getOID(), itemRef.getItemOID()});
                        orderNumberForAllItemRefsPresent = false;
                    } else {
                        LOGGER.debug("The ItemRef with OID {} has an "
                                + "OrderNumber, so I'll try to put the referenced "
                                + "ItemDef onto its position ({}) in the List", itemRef.getItemOID(),
                            orderNumber.intValue());
                        if (orderedItemDefs.get(orderNumber.intValue()) == null) {
                            LOGGER.debug("The List of ItemDefs has a free spot at "
                                    + "position/OrderNumber {}, so I'll just "
                                    + "put the ItemDef of OID {} there.", orderNumber.intValue(),
                                itemRef.getItemOID());
                            orderedItemDefs.put(orderNumber.intValue(),
                                matchingItemRefDefs.get(itemRef));
                        } else {
                            LOGGER.debug("The List of ItemDefs already has an "
                                    + "ItemDef at position/OrderNumber {}, "
                                    + "which does not comply to the ODM "
                                    + "standard. Will not consider " + "orderNumbers in the end.",
                                orderNumber.intValue());
                            result.addValidationMessage(
                                "import.odm.v132.itemGroupDef.itemRef" + ".multipleOrderNumber",
                                new String[]{itemGroupDef.getOID(), itemRef.getItemOID(),
                                    orderNumber.toString()});
                            orderNumberForAllItemRefsPresent = false;
                        }
                    }
                }
                List<ODMcomplexTypeDefinitionItemDef> itemDefListToIterateOver;
                if (orderNumberForAllItemRefsPresent) {
                    LOGGER.debug("All ItemDefs of MetaDataVersion of OID {} "
                            + "that were referenced in ItemGroupDef of OID {} have a "
                            + "unique OrderNumber in their ItemRef. Thus, I will "
                            + "consider the OrderNumbers when converting to Questions",
                        metaDataVersion.getOID(), itemGroupDef.getOID());
                    ArrayList<Integer> orderNumberList = new ArrayList<>(orderedItemDefs.keySet());
                    Collections.sort(orderNumberList);
                    itemDefListToIterateOver = new ArrayList<>();
                    for (Integer orderNumber : orderNumberList) {
                        itemDefListToIterateOver.add(orderedItemDefs.get(orderNumber));
                    }
                } else {
                    LOGGER.debug("Not all ItemDefs of MetaDataVersion of OID "
                            + "{} that were referenced in ItemGroupDef of OID {} have"
                            + " a unique OrderNumber in their ItemRef. Thus, I will "
                            + "not consider the OrderNumbers when converting to " + "Questions",
                        metaDataVersion.getOID(), itemGroupDef.getOID());
                    itemDefListToIterateOver = matchedItemDefs;
                }

                LOGGER.debug("Collection of all necessary info for converting "
                    + "refered ItemDefs of ItemGroupDef of OID {} done. "
                    + "Will now iterate over them and do the conversion.", itemGroupDef.getOID());
                for (ODMcomplexTypeDefinitionItemDef itemDef : itemDefListToIterateOver) {
                    LOGGER.debug("Calling conversion method for ItemDef of OID {}.",
                        itemDef.getOID());
                    // [bt] set the position of the question, because we
// start counting with 1 in MoPat
                    int position = (currentHighestQuestionPosition + result.getQuestionList().size()
                        + 1);
                    // retrieve the CollectionExceptionConditionOID
                    result.addImportQuestionResult(
                        convertToQuestion(itemDef, itemDefIsRequiredMap.get(itemDef),
                            itemDefConditionFormalExpression.get(itemDef), position, questionnaire,
                            metaDataVersion, exportTemplates, itemGroupDef, messageSource));
                }
            }
        }

        LOGGER.debug("Leaving convertToQuestionList"
            + "(ODMcomplexTypeDefinitionItemGroupDef, Questionnaire, "
            + "ODMcomplexTypeDefinitionMetaDataVersion)");
        result.setIdentifier(itemGroupDef.getOID());
        return result;
    }

    /**
     * TODO [bt] comment the method convertToQuestion in ODMv132ToMoPatConverter
     * !
     *
     * @param itemDef           must not be <code>null</code>.
     * @param isRequired        must not be <code>null</code>.
     * @param formalExpressions TODO
     * @param position          must not be <code>null</code>.
     * @param questionnaire     must not be <code>null</code>.
     * @param metaDataVersion   if the {@link ODMcomplexTypeDefinitionItemDef ItemDef} refers to a
     *                          CodeList (using
     *                          {@link ODMcomplexTypeDefinitionCodeListRef CodeListRefs}), the
     *                          metaDataVersion is utilized to identify the referred
     *                          {@link ODMcomplexTypeDefinitionCodeList CodeList}. Must not be
     *                          <code>null</code>.
     * @param exportTemplates   TODO
     * @param itemGroupDef      TODO
     * @param messageSource     TODO
     * @return never <code>null</code>.
     */
    public static final ImportQuestionResult convertToQuestion(
        ODMcomplexTypeDefinitionItemDef itemDef, Boolean isRequired,
        ArrayList<ODMcomplexTypeDefinitionFormalExpression> formalExpressions, Integer position,
        Questionnaire questionnaire, ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion,
        List<ExportTemplate> exportTemplates, ODMcomplexTypeDefinitionItemGroupDef itemGroupDef,
        MessageSource messageSource) {
        LOGGER.debug("Enter convertToQuestion"
            + "(ODMcomplexTypeDefinitionItemDef, Boolean, Integer, Questionnaire,"
            + " ODMcomplexTypeDefinitionMetaDataVersion )");
        assert itemDef != null : "The given ItemDef was null";
        assert isRequired != null : "The given boolean isRequired was null";
        assert position != null : "The given position was null";
        assert questionnaire != null : "The given questionnaire was null";
        assert metaDataVersion != null : "The given MetaDataVersion was null";

        ImportQuestionResult result = new ImportQuestionResult();
        Question question = null;

        LOGGER.debug("Checking the Question element");
        ODMcomplexTypeDefinitionQuestion odmQuestion = itemDef.getQuestion();
        if (odmQuestion == null) {// [bt] as defined in , an ItemDef without a
            // question element will not
            // be converted into a MoPat question
            LOGGER.debug("The Question Element of ItemDef with OID {} was null; "
                + "but we need one for the question text. Will write an "
                + "error message and finish.", itemDef.getOID());
            result.addValidationMessage("import.odm.v132.itemDef.questionNull",
                new String[]{itemDef.getOID()});
        } else {
            LOGGER.debug("The Question Element of ItemDef with OID {} was not "
                    + "null; now checking its TranslatedText children for the" + " question text.",
                itemDef.getOID());

            List<ODMcomplexTypeDefinitionTranslatedText> translatedTextList = odmQuestion.getTranslatedText();
            int minQuestionTextLength = 1;
            Map<String, String> localizedQuestionTexts = getTranslations(translatedTextList,
                minQuestionTextLength);

            // [bt] detection of question text is finished, now let's see
            // whether we have a question text
            if (localizedQuestionTexts == null || localizedQuestionTexts.isEmpty()) {
                LOGGER.debug("Could not find a proper question text. Will add"
                    + " an error message to the result and finish.");
                result.addValidationMessage("import.odm.v132.itemDef.noProperQuestion",
                    new String[]{itemDef.getOID(), Integer.toString(minQuestionTextLength)});
            } else {
                LOGGER.debug("Proper question text found ({}), now checking for "
                        + "CodedListRef --> Multiple Choice question", localizedQuestionTexts);
                ODMcomplexTypeDefinitionCodeListRef codeListRef = itemDef.getCodeListRef();
                if (codeListRef == null) {
                    LOGGER.debug("The ItemDef with OID {} did not refer to a "
                            + "CodeListRef (getCodeListRef() == null), so "
                            + "I'll check for the DataType and create proper " + "questions",
                        itemDef.getOID());
                    DataType dataType = itemDef.getDataType();
                    QuestionType questionType;
                    switch (dataType) {
                        case TEXT:
                        case STRING: {
                            LOGGER.debug("DataType {} in ItemDef of OID {} "
                                    + "detected. Will convert it into a free " + "text question.",
                                dataType, itemDef.getOID());
                            questionType = QuestionType.FREE_TEXT;
                            question = new Question(localizedQuestionTexts, isRequired, true,
                                questionType, position, questionnaire);
                            // Create the answer and include the export rules
                            FreetextAnswer freeTextAnswer = new FreetextAnswer(question, true);
                            for (ExportTemplate exportTemplate : exportTemplates) {
                                ExportRuleAnswer exportRuleAnswerText = new ExportRuleAnswer(
                                    exportTemplate, itemGroupDef.getOID().replace(".", "u002E")
                                    .replace("_", "u005F") + "_" + itemDef.getOID()
                                    .replace(".", "u002E").replace("_", "u005F"), freeTextAnswer);
                                exportRuleAnswerText.setExportRuleFormat(new ExportRuleFormat());
                                freeTextAnswer.addExportRule(exportRuleAnswerText);
                            }
                            break;
                        }
                        case INTEGER: {
                            LOGGER.debug("DataType {} in ItemDef of OID {} "
                                    + "detected. Will check for RangeChecks "
                                    + "to decide whether to convert it into a"
                                    + " number checkbox or slider question.", dataType,
                                itemDef.getOID());
                            Double[] minMaxFromRangeCheck = getMinMaxFromRangeCheck(itemDef,
                                itemDef.getRangeCheck(), result);
                            Double min = minMaxFromRangeCheck[0];
                            Double max = minMaxFromRangeCheck[1];
                            Boolean vertical = DEFAULT_VERTICAL;
                            LOGGER.debug(
                                "Finished with min/max value stuff. " + "Will set the stepsize.");
                            Double stepsize = DEFAULT_INTEGER_STEPSIZE;
                            result.addValidationMessage("import.odm.v132.itemDef.stepSize",
                                new String[]{itemDef.getOID(), Double.toString(stepsize)});

                            if (min == null || max == null) {
                                questionType = QuestionType.NUMBER_INPUT;
                                LOGGER.debug("Minimum or maximum is null. Set"
                                    + " the question type to NUMBER INPUT.");
                                result.addValidationMessage("import.odm.v132.itemDef.numberInput",
                                    new String[]{itemDef.getOID()});
                            } else if ((max - min) < DEFAULT_INTEGER_MIN_MAX_DIFFERENCE) {
                                questionType = QuestionType.NUMBER_CHECKBOX;
                                LOGGER.debug("The range between minimum and "
                                    + "maximum is lower than the maximum range "
                                    + "for a NUMBER CHECKBOX question. Set the "
                                    + "question type to NUMBER CHECKBOX.");
                                result.addValidationMessage(
                                    "import.odm.v132.itemDef" + ".numberCheckbox",
                                    new String[]{itemDef.getOID(), String.valueOf(max - min + 1),
                                        String.valueOf(DEFAULT_INTEGER_MIN_MAX_DIFFERENCE)});
                            } else {
                                questionType = QuestionType.SLIDER;
                                LOGGER.debug("The range between minimum and "
                                    + "maximum is greater than the maximum range "
                                    + "for a NUMBER CHECKBOX question. Set the "
                                    + "question type to HORIZONTAL SLIDER.");
                                result.addValidationMessage("import.odm.v132.itemDef.slider",
                                    new String[]{itemDef.getOID(), String.valueOf(max - min + 1),
                                        String.valueOf(DEFAULT_INTEGER_MIN_MAX_DIFFERENCE)});
                            }

                            question = new Question(localizedQuestionTexts, isRequired, true,
                                questionType, position, questionnaire);
                            if (questionType == QuestionType.NUMBER_INPUT) {

                                // Create the answer and include the export
                                // rules
                                NumberInputAnswer numberInputAnswer = new NumberInputAnswer(
                                    question, true, min, max, stepsize);
                                for (ExportTemplate exportTemplate : exportTemplates) {
                                    ExportRuleAnswer exportRuleAnswerNumber = new ExportRuleAnswer(
                                        exportTemplate, itemGroupDef.getOID().replace(".", "u002E")
                                        .replace("_", "u005F") + "_" + itemDef.getOID()
                                        .replace(".", "u002E").replace("_", "u005F"),
                                        numberInputAnswer);
                                    ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
                                    exportRuleFormat.setNumberType(ExportNumberType.INTEGER);
                                    exportRuleFormat.setRoundingStrategy(
                                        ExportRoundingStrategyType.STANDARD);
                                    exportRuleAnswerNumber.setExportRuleFormat(exportRuleFormat);
                                    numberInputAnswer.addExportRule(exportRuleAnswerNumber);
                                }
                            } else {
                                // Create the answer and include the export
                                // rules
                                SliderAnswer sliderAnswer = new SliderAnswer(question, true, min,
                                    max, stepsize, vertical);
                                for (ExportTemplate exportTemplate : exportTemplates) {
                                    ExportRuleAnswer exportRuleAnswerSlider = new ExportRuleAnswer(
                                        exportTemplate, itemGroupDef.getOID().replace(".", "u002E")
                                        .replace("_", "u005F") + "_" + itemDef.getOID()
                                        .replace(".", "u002E").replace("_", "u005F"), sliderAnswer);
                                    ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
                                    exportRuleFormat.setNumberType(ExportNumberType.INTEGER);
                                    exportRuleFormat.setRoundingStrategy(
                                        ExportRoundingStrategyType.STANDARD);
                                    exportRuleAnswerSlider.setExportRuleFormat(exportRuleFormat);
                                    sliderAnswer.addExportRule(exportRuleAnswerSlider);
                                }
                            }
                            break;
                        }
                        case FLOAT:
                        case DOUBLE: {
                            LOGGER.debug("DataType {} in ItemDef of OID {} "
                                + "detected. Will check for RangeChecks "
                                + "for min and max values.", dataType, itemDef.getOID());
                            Double[] minMaxFromRangeCheck = getMinMaxFromRangeCheck(itemDef,
                                itemDef.getRangeCheck(), result);
                            Double min = minMaxFromRangeCheck[0];
                            Double max = minMaxFromRangeCheck[1];

                            LOGGER.debug(
                                "Finished with min/max value stuff. " + "Will set the stepsize.");
                            Boolean vertical = DEFAULT_VERTICAL;
                            Double stepsize;

                            // If the significat digits is set, the stepsize
                            // 1^-SignificantDigits
                            if (itemDef.getSignificantDigits() != null
                                && itemDef.getSignificantDigits().intValue() >= 0) {
                                stepsize =
                                    1 / Math.pow(10, itemDef.getSignificantDigits().intValue());
                                LOGGER.debug(
                                    "Get the stepsize out of the " + "significant digit attribute");

                                if (stepsize < DEFAULT_DOUBLE_STEPSIZE) {
                                    LOGGER.debug("If the significant digit "
                                        + "value is lower than the minimum "
                                        + "supported stepsize, use the default " + "stepsize");
                                    result.addValidationMessage(
                                        "import.odm.v132.itemDef" + ".significantDigitTooSmall",
                                        new String[]{itemDef.getOID(), String.valueOf(stepsize),
                                            String.valueOf(DEFAULT_DOUBLE_STEPSIZE)});
                                    stepsize = DEFAULT_DOUBLE_STEPSIZE;
                                } else {
                                    LOGGER.debug("If the significant digit "
                                        + "value is a supported stepsize, use it.");
                                    result.addValidationMessage(
                                        "import.odm.v132.itemDef" + ".significantDigit",
                                        new String[]{itemDef.getOID(), String.valueOf(stepsize)});
                                }

                            } else {
                                stepsize = DEFAULT_DOUBLE_STEPSIZE;
                                LOGGER.debug("If the significant digit is not"
                                    + " set, use the default double stepsize.");
                                result.addValidationMessage(
                                    "import.odm.v132.itemDef" + ".significantDigitTooSmall",
                                    new String[]{itemDef.getOID(),
                                        String.valueOf(itemDef.getSignificantDigits()),
                                        String.valueOf(DEFAULT_DOUBLE_STEPSIZE)});
                            }

                            if (min == null || max == null) {
                                questionType = QuestionType.NUMBER_INPUT;
                                LOGGER.debug("Minimum or maximum is null. Set"
                                    + " the question type to NUMBER INPUT.");
                                result.addValidationMessage("import.odm.v132.itemDef.numberInput",
                                    new String[]{itemDef.getOID()});
                            } else {
                                questionType = QuestionType.SLIDER;
                                LOGGER.debug("Minimum and maximum are not "
                                    + "null. Set the question type to HORIZONTAL " + "SLIDER.");
                                result.addValidationMessage("import.odm.v132.itemDef.slider",
                                    new String[]{itemDef.getOID(),
                                        String.valueOf((max - min) / stepsize),
                                        String.valueOf(DEFAULT_INTEGER_MIN_MAX_DIFFERENCE)});
                            }

                            question = new Question(localizedQuestionTexts, isRequired, true,
                                questionType, position, questionnaire);

                            if (questionType == QuestionType.NUMBER_INPUT) {
                                // Create the answer and include the export
                                // rules
                                NumberInputAnswer numberInputAnswer = new NumberInputAnswer(
                                    question, true, min, max, stepsize);
                                for (ExportTemplate exportTemplate : exportTemplates) {
                                    ExportRuleAnswer exportRuleAnswerNumber = new ExportRuleAnswer(
                                        exportTemplate, itemGroupDef.getOID().replace(".", "u002E")
                                        .replace("_", "u005F") + "_" + itemDef.getOID()
                                        .replace(".", "u002E").replace("_", "u005F"),
                                        numberInputAnswer);
                                    ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
                                    exportRuleFormat.setNumberType(ExportNumberType.FLOAT);
                                    exportRuleFormat.setRoundingStrategy(
                                        ExportRoundingStrategyType.STANDARD);
                                    exportRuleFormat.setDecimalPlaces(
                                        itemDef.getSignificantDigits() != null
                                            && itemDef.getSignificantDigits().intValue() >= 0
                                            ? itemDef.getSignificantDigits().intValue()
                                            : DEFAULT_SIGNIFICANT_DIGITS);
                                    exportRuleAnswerNumber.setExportRuleFormat(exportRuleFormat);
                                    numberInputAnswer.addExportRule(exportRuleAnswerNumber);
                                }
                            } else {
                                // Create the answer and include the export
                                // rules
                                SliderAnswer sliderAnswer = new SliderAnswer(question, true, min,
                                    max, stepsize, vertical);
                                for (ExportTemplate exportTemplate : exportTemplates) {
                                    ExportRuleAnswer exportRuleAnswerSlider = new ExportRuleAnswer(
                                        exportTemplate, itemGroupDef.getOID().replace(".", "u002E")
                                        .replace("_", "u005F") + "_" + itemDef.getOID()
                                        .replace(".", "u002E").replace("_", "u005F"), sliderAnswer);
                                    ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
                                    exportRuleFormat.setNumberType(ExportNumberType.FLOAT);
                                    exportRuleFormat.setRoundingStrategy(
                                        ExportRoundingStrategyType.STANDARD);
                                    exportRuleFormat.setDecimalPlaces(
                                        itemDef.getSignificantDigits() != null
                                            && itemDef.getSignificantDigits().intValue() >= 0
                                            ? itemDef.getSignificantDigits().intValue()
                                            : DEFAULT_SIGNIFICANT_DIGITS);
                                    exportRuleAnswerSlider.setExportRuleFormat(exportRuleFormat);
                                    sliderAnswer.addExportRule(exportRuleAnswerSlider);
                                }
                            }
                            break;
                        }
                        case DATE: {
                            LOGGER.debug("DataType {} in ItemDef of OID {} "
                                + "detected. Will check for RangeChecks "
                                + "for min and max values.", dataType, itemDef.getOID());
                            Date startDate = null;
                            Date endDate = null;
                            List<ODMcomplexTypeDefinitionRangeCheck> rangeCheckList = itemDef.getRangeCheck();
                            if (rangeCheckList == null || rangeCheckList.isEmpty()) {
                                LOGGER.debug("ItemDef of OID {} did not contain "
                                        + "any RangeChecks. Won't set any " + "start or end dates.",
                                    itemDef.getOID());
                                result.addValidationMessage(
                                    "import.odm.v132.itemDef" + ".noRangeCheckGEForDate",
                                    new String[]{itemDef.getOID()});
                                result.addValidationMessage(
                                    "import.odm.v132.itemDef" + ".noRangeCheckLEForDate",
                                    new String[]{itemDef.getOID()});
                            } else {
                                LOGGER.debug("ItemDef of OID {} contains "
                                        + "RangeChecks. Will iterate over "
                                        + "them to check for RangeChecks " + "about 'GE' and 'LE'.",
                                    itemDef.getOID());
                                for (ODMcomplexTypeDefinitionRangeCheck rangeCheck : rangeCheckList) {
                                    LOGGER.debug("Now checking RangeCheck at " + "position {}",
                                        rangeCheckList.indexOf(rangeCheck));
                                    Comparator comparator = rangeCheck.getComparator();
                                    if (comparator == null) {
                                        LOGGER.debug(
                                            "RangeCheck at position {} " + "did not contain a "
                                                + "Comparator. Since this is "
                                                + "necessary, the RangeCheck " + "will be skipped.",
                                            rangeCheckList.indexOf(rangeCheck));
                                    } else {
                                        LOGGER.debug(
                                            "RangeCheck at position {} " + "contains a Comparator. "
                                                + "Switching over it.",
                                            rangeCheckList.indexOf(rangeCheck));
                                        switch (comparator) {
                                            case GE: {
                                                if (startDate == null) {
                                                    LOGGER.debug("RangeCheck " + "comparator {} "
                                                        + "detected. " + "Checking for "
                                                        + "'CheckValue'", comparator);
                                                    List<ODMcomplexTypeDefinitionCheckValue> checkValueList = rangeCheck.getCheckValue();
                                                    if (checkValueList == null
                                                        || checkValueList.isEmpty()) {
                                                        LOGGER.debug(
                                                            "RangeCheck " + "at " + "position "
                                                                + "{} does " + "not " + "contain "
                                                                + "any " + "CheckValue"
                                                                + " elements." + " Will skip"
                                                                + " it.",
                                                            rangeCheckList.indexOf(rangeCheck));
                                                        result.addValidationMessage(
                                                            "import.odm" + ".v132" + ".itemDef"
                                                                + ".rangeCheck"
                                                                + ".checkValueNullEmpty",
                                                            new String[]{itemDef.getOID(),
                                                                Integer.toString(
                                                                    rangeCheckList.indexOf(
                                                                        rangeCheck))});
                                                    } else {
                                                        LOGGER.debug(
                                                            "RangeCheck " + "at " + "position "
                                                                + "{} " + "contains " + "at least "
                                                                + "one " + "CheckValue"
                                                                + ". Iterating over it.",
                                                            rangeCheckList.indexOf(rangeCheck));
                                                        for (ODMcomplexTypeDefinitionCheckValue checkValue : checkValueList) {
                                                            LOGGER.debug(
                                                                "Now checking the CheckValue element at position {}.",
                                                                checkValueList.indexOf(checkValue));
                                                            try {
                                                                LOGGER.debug(
                                                                    "Trying to parse the xml value ('{}') into a date",
                                                                    checkValue.getValue());
                                                                startDate = new SimpleDateFormat(
                                                                    DATE_FORMAT).parse(
                                                                    checkValue.getValue());
                                                                LOGGER.debug(
                                                                    "Parsing successful. Start date is: {}",
                                                                    startDate);
                                                                break;
                                                            } catch (ParseException pe) {
                                                                LOGGER.debug(
                                                                    "ParseException when trying to parse the CheckValue at position {} of RangeCheck at Position: {}",
                                                                    checkValueList.indexOf(
                                                                        checkValue),
                                                                    rangeCheckList.indexOf(
                                                                        rangeCheck), pe);
                                                                result.addValidationMessage(
                                                                    "import.odm.v132.itemDef.rangeCheck.checkValue.noDate",
                                                                    new String[]{itemDef.getOID(),
                                                                        Integer.toString(
                                                                            rangeCheckList.indexOf(
                                                                                rangeCheck)),
                                                                        Integer.toString(
                                                                            checkValueList.indexOf(
                                                                                checkValue)),
                                                                        DATE_FORMAT});
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    LOGGER.debug("RangeCheck at " + "position {} "
                                                            + "has the " + "comparator {},"
                                                            + " but start " + "date is "
                                                            + "already set. " + "Won't consider"
                                                            + " it.",
                                                        rangeCheckList.indexOf(rangeCheck),
                                                        comparator);
                                                    result.addValidationMessage(
                                                        "import.odm.v132" + ".itemDef"
                                                            + ".rangeCheckComparatorRedundand",
                                                        new String[]{itemDef.getOID(),
                                                            Integer.toString(
                                                                rangeCheckList.indexOf(rangeCheck)),
                                                            comparator.toString()});
                                                }
                                                break;
                                            }
                                            case LE: {
                                                if (endDate == null) {
                                                    LOGGER.debug("RangeCheck " + "comparator {} "
                                                        + "detected. " + "Checking for "
                                                        + "'CheckValue'", comparator);
                                                    List<ODMcomplexTypeDefinitionCheckValue> checkValueList = rangeCheck.getCheckValue();
                                                    if (checkValueList == null
                                                        || checkValueList.isEmpty()) {
                                                        LOGGER.debug(
                                                            "RangeCheck " + "at " + "position "
                                                                + "{} does " + "not " + "contain "
                                                                + "any " + "CheckValue"
                                                                + " elements." + " Will skip"
                                                                + " it.",
                                                            rangeCheckList.indexOf(rangeCheck));
                                                        result.addValidationMessage(
                                                            "import.odm" + ".v132" + ".itemDef"
                                                                + ".rangeCheck"
                                                                + ".checkValueNullEmpty",
                                                            new String[]{itemDef.getOID(),
                                                                Integer.toString(
                                                                    rangeCheckList.indexOf(
                                                                        rangeCheck))});
                                                    } else {
                                                        LOGGER.debug(
                                                            "RangeCheck " + "at " + "position "
                                                                + "{} " + "contains " + "at least "
                                                                + "one " + "CheckValue"
                                                                + ". Iterating over it.",
                                                            rangeCheckList.indexOf(rangeCheck));
                                                        for (ODMcomplexTypeDefinitionCheckValue checkValue : checkValueList) {
                                                            LOGGER.debug(
                                                                "Now checking the CheckValue element at position {}.",
                                                                checkValueList.indexOf(checkValue));
                                                            try {
                                                                LOGGER.debug(
                                                                    "Trying to parse the xml value ('{}') into a date",
                                                                    checkValue.getValue());
                                                                endDate = new SimpleDateFormat(
                                                                    DATE_FORMAT).parse(
                                                                    checkValue.getValue());
                                                                LOGGER.debug(
                                                                    "Parsing successful. End date is: {}",
                                                                    endDate);
                                                                break;
                                                            } catch (ParseException pe) {
                                                                LOGGER.debug(
                                                                    "ParseException when trying to parse the CheckValue at position {} of RangeCheck at Position: {}",
                                                                    checkValueList.indexOf(
                                                                        checkValue),
                                                                    rangeCheckList.indexOf(
                                                                        rangeCheck), pe);
                                                                result.addValidationMessage(
                                                                    "import.odm.v132.itemDef.rangeCheck.checkValue.noDate",
                                                                    new String[]{itemDef.getOID(),
                                                                        Integer.toString(
                                                                            rangeCheckList.indexOf(
                                                                                rangeCheck)),
                                                                        Integer.toString(
                                                                            checkValueList.indexOf(
                                                                                checkValue)),
                                                                        DATE_FORMAT});
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    LOGGER.debug("RangeCheck at " + "position {} "
                                                            + "has the " + "comparator {},"
                                                            + " but end date " + "is already set"
                                                            + ". Won't " + "consider it.",
                                                        rangeCheckList.indexOf(rangeCheck),
                                                        comparator);
                                                    result.addValidationMessage(
                                                        "import.odm.v132" + ".itemDef"
                                                            + ".rangeCheckComparatorRedundand",
                                                        new String[]{itemDef.getOID(),
                                                            Integer.toString(
                                                                rangeCheckList.indexOf(rangeCheck)),
                                                            comparator.toString()});
                                                }
                                                break;
                                            }
                                            default: {
                                                LOGGER.debug("RangeCheck at " + "position {} "
                                                        + "contains a " + "Comparator I don't"
                                                        + " support ({}).",
                                                    rangeCheckList.indexOf(rangeCheck), comparator);
                                                result.addValidationMessage(
                                                    "import.odm.v132" + ".itemDef"
                                                        + ".rangeCheckComparatorUnsupported",
                                                    new String[]{itemDef.getOID(), Integer.toString(
                                                        rangeCheckList.indexOf(rangeCheck)),
                                                        comparator.toString()});
                                                break;
                                            }
                                        }
                                    }
                                }// [bt] finish with searching for min and
                                // max values

                                if (startDate != null && endDate != null && endDate.before(
                                    startDate)) {
                                    LOGGER.debug("Start and end date were found, "
                                            + "but end date ({}) was not "
                                            + "after start date ({}). Won't " + "set them.", endDate,
                                        startDate);
                                    result.addValidationMessage("import.odm.v132.itemDef"
                                            + ".rangeCheckEndDateBeforeStartDate",
                                        new String[]{itemDef.getOID(),
                                            new SimpleDateFormat(DATE_FORMAT).format(endDate),
                                            new SimpleDateFormat(DATE_FORMAT).format(startDate)});
                                    startDate = null;
                                    endDate = null;
                                }
                            }

                            questionType = QuestionType.DATE;
                            question = new Question(localizedQuestionTexts, isRequired, true,
                                questionType, position, questionnaire);
                            // Create the answer and include the export rules
                            DateAnswer dateAnswer = new DateAnswer(question, true, startDate,
                                endDate);
                            for (ExportTemplate exportTemplate : exportTemplates) {
                                ExportRuleAnswer exportRuleAnswerDate = new ExportRuleAnswer(
                                    exportTemplate, itemGroupDef.getOID().replace(".", "u002E")
                                    .replace("_", "u005F") + "_" + itemDef.getOID()
                                    .replace(".", "u002E").replace("_", "u005F"), dateAnswer);
                                ExportRuleFormat exportRuleFormat = new ExportRuleFormat();
                                exportRuleFormat.setDateFormat(ExportDateFormatType.DD_MM_YY);
                                exportRuleAnswerDate.setExportRuleFormat(exportRuleFormat);
                                dateAnswer.addExportRule(exportRuleAnswerDate);
                            }
                            break;
                        }
                        case BOOLEAN: {
                            LOGGER.debug("The DataType of ItemDef with OID {} was "
                                    + "{}. Will convert it into a simple "
                                    + "multiple choice question with the " + "options 'yes' and 'no'.",
                                itemDef.getOID(), dataType);
                            questionType = QuestionType.MULTIPLE_CHOICE;
                            question = new Question(localizedQuestionTexts, isRequired, true,
                                questionType, position, questionnaire);
                            question.setMinMaxNumberAnswers(DEFAULT_MIN_NUMBER_ANSWERS,
                                DEFAULT_MAX_NUMBER_ANSWERS);
                            String answerLabelYes = BOOLEAN_ANSWER_LABEL_YES;
                            String answerLabelNo = BOOLEAN_ANSWER_LABEL_NO;
                            result.addValidationMessage(
                                "import.odm.v132.itemDef" + ".dataTypeBooleanDefaultBevahiour",
                                new String[]{itemDef.getOID(), dataType.toString(), answerLabelYes,
                                    answerLabelNo});

                            Map<String, String> localizedLabelMap = new HashMap<>();
                            for (String localeCode : localizedQuestionTexts.keySet()) {
                                Locale currentLocale;
                                String[] localeSplit = localeCode.split("_");
                                currentLocale = new Locale(localeSplit[0]);
                                if (localeSplit.length == 2) {
                                    currentLocale = new Locale(localeSplit[0], localeSplit[1]);
                                }
                                localizedLabelMap.put(localeCode,
                                    messageSource.getMessage("survey" + ".question.answer.yes",
                                        new Object[]{question.getId()}, currentLocale));
                                if (localizedLabelMap.get(localeCode).equals("")) {
                                    LOGGER.debug("The language code " + localeCode + " has no "
                                        + "translation for " + "boolean questions" + ".");
                                }
                            }

                            // Create the answer and include the export rules
                            SelectAnswer answerTrue = new SelectAnswer(question, true,
                                localizedLabelMap, Boolean.FALSE);
                            for (ExportTemplate exportTemplate : exportTemplates) {
                                ExportRuleAnswer exportRuleAnswerTrue = new ExportRuleAnswer(
                                    exportTemplate, itemGroupDef.getOID().replace(".", "u002E")
                                    .replace("_", "u005F") + "_" + itemDef.getOID()
                                    .replace(".", "u002E").replace("_", "u005F") + "_true",
                                    answerTrue);
                                exportRuleAnswerTrue.setExportRuleFormat(new ExportRuleFormat());

                                answerTrue.addExportRule(exportRuleAnswerTrue);
                            }

                            localizedLabelMap = new HashMap<>();
                            for (String localeCode : localizedQuestionTexts.keySet()) {
                                Locale currentLocale;
                                String[] localeSplit = localeCode.split("_");
                                currentLocale = new Locale(localeSplit[0]);
                                if (localeSplit.length == 2) {
                                    currentLocale = new Locale(localeSplit[0], localeSplit[1]);
                                }
                                localizedLabelMap.put(localeCode,
                                    messageSource.getMessage("survey" + ".question.answer.no", null,
                                        currentLocale));
                                if (localizedLabelMap.get(localeCode).equals("")) {
                                    LOGGER.debug("The language code " + localeCode + " has no "
                                        + "translation for " + "boolean questions" + ".");
                                }
                            }
                            // Create the answer and include the export rules
                            SelectAnswer answerFalse = new SelectAnswer(question, true,
                                localizedLabelMap, Boolean.FALSE);
                            for (ExportTemplate exportTemplate : exportTemplates) {
                                ExportRuleAnswer exportRuleAnswerFalse = new ExportRuleAnswer(
                                    exportTemplate, itemGroupDef.getOID().replace(".", "u002E")
                                    .replace("_", "u005F") + "_" + itemDef.getOID()
                                    .replace(".", "u002E").replace("_", "u005F") + "_false",
                                    answerTrue);
                                exportRuleAnswerFalse.setExportRuleFormat(new ExportRuleFormat());
                                answerFalse.addExportRule(exportRuleAnswerFalse);
                            }
                            break;
                        }
                        default: {
                            LOGGER.debug("The DataType of ItemDef with OID {} was "
                                + "{}. I currently can't "
                                + "convert/interprete this DataType into "
                                + "a proper MoPat QuestionType. Will "
                                + "write an error message and go on.", itemDef.getOID(), dataType);
                            result.addValidationMessage(
                                "import.odm.v132.itemDef" + ".dataTypeNotSupported",
                                new String[]{itemDef.getOID(), dataType.toString()});
                            break;
                        }
                    }
                } // [bt] end of DataType-driven handling of question type
                else {// [bt] beginning of Multiple Choice Question/CodeList
                    // stuff
                    LOGGER.debug("The ItemDef with OID {} refers to a CodeListRef "
                            + "(getCodeListRef() != null), so I'll make a "
                            + "freetext question out of this. If the codeList"
                            + " will be found, i'll convert it into a " + "multiple choice question.",
                        itemDef.getOID());
                    LOGGER.debug("All information necessary for creation of a "
                            + "Question of Type {} is available, so I'll " + "create the question now",
                        QuestionType.FREE_TEXT);

                    question = new Question(localizedQuestionTexts, isRequired, true,
                        QuestionType.FREE_TEXT, position, questionnaire);

                    String codeListOID = codeListRef.getCodeListOID();
                    List<ODMcomplexTypeDefinitionCodeList> codeListList = metaDataVersion.getCodeList();
                    if (codeListList == null) {
                        LOGGER.debug("The ItemDef with OID {} refers to a "
                                + "CodeList (getCodeListRef() != null) with OID {}, "
                                + "but the given MetaDataVersion with OID {} does not"
                                + " contain any CodeList (getCodeList() == null). "
                                + "Will add an error message and finish", itemDef.getOID(), codeListOID,
                            metaDataVersion.getOID());
                        result.addValidationMessage(
                            "import.odm.v132.itemDef" + ".codeListReferredButNoneProvided",
                            new String[]{itemDef.getOID(), codeListOID, metaDataVersion.getOID()});
                    } else {
                        LOGGER.debug("The ItemDef with OID {} refers to a "
                                + "CodeList (getCodeListRef() != null) with OID {} "
                                + "and the MetaDataVersion with OID {} contains a "
                                + "list of CodeLists. Will now iterate over them to "
                                + "find the referred one.", itemDef.getOID(), codeListOID,
                            metaDataVersion.getOID());
                        ODMcomplexTypeDefinitionCodeList odmAnswerList = null;
                        for (ODMcomplexTypeDefinitionCodeList codeList : codeListList) {
                            LOGGER.debug("checking CodeList with OID {}", codeList.getOID());
                            if (codeList.getOID().equalsIgnoreCase(codeListOID)) {
                                LOGGER.debug("CodeList with OID {} matched "
                                    + "(ignoring cases) the referenced CodeList "
                                    + "({}). Will take this one for the answers "
                                    + "of the multiple choice question");

                                question.setQuestionType(QuestionType.MULTIPLE_CHOICE);
                                question.setMinMaxNumberAnswers(DEFAULT_MIN_NUMBER_ANSWERS,
                                    DEFAULT_MAX_NUMBER_ANSWERS);

                                switch (codeList.getDataType()) {
                                    case STRING:
                                        question.setCodedValueType(CodedValueType.STRING);
                                        break;
                                    case INTEGER:
                                        question.setCodedValueType(CodedValueType.INTEGER);
                                        break;
                                    case FLOAT:
                                        question.setCodedValueType(CodedValueType.FLOAT);
                                        break;
                                }

                                result.addValidationMessage(
                                    "import.odm.v132.itemDef" + ".minMaxNumberAnswersDefaultSet",
                                    new String[]{itemDef.getOID(),
                                        Integer.toString(question.getMinNumberAnswers()),
                                        Integer.toString(question.getMaxNumberAnswers())});
                                odmAnswerList = codeList;
                                break;
                            }
                        }

                        if (odmAnswerList == null) {
                            LOGGER.debug("No matching CodeList could be found"
                                    + " for the referenced CodeList with OID {} in "
                                    + "MetaDataVersion with OID {} to create a "
                                    + "multiple choice question for ItemDef with OID "
                                    + "{}. Will create an error message and finish", codeListOID,
                                metaDataVersion.getOID(), itemDef.getOID());
                            result.addValidationMessage(
                                "import.odm.v132.itemDef" + ".codeListReferredButNoneMatchingFound",
                                new String[]{itemDef.getOID(), codeListOID,
                                    metaDataVersion.getOID()});
                            // Create FreeTextAnswer because no CodeList was
                            // found
                            FreetextAnswer freeTextAnswer = new FreetextAnswer(question, true);
                            // Include the export rule
                            for (ExportTemplate exportTemplate : exportTemplates) {
                                ExportRuleAnswer exportRuleAnswerText = new ExportRuleAnswer(
                                    exportTemplate, itemGroupDef.getOID().replace(".", "u002E")
                                    .replace("_", "u005F") + "_" + itemDef.getOID()
                                    .replace(".", "u002E").replace("_", "u005F"), freeTextAnswer);
                                exportRuleAnswerText.setExportRuleFormat(new ExportRuleFormat());
                                freeTextAnswer.addExportRule(exportRuleAnswerText);
                            }
                        } else {
                            LOGGER.debug("A matching CodeList could be found "
                                    + "for the referenced CodeList with OID {} in "
                                    + "MetaDataVersion with OID {} to create a "
                                    + "multiple choice question for ItemDef with OID "
                                    + "{}. Now checking for CodeListItems.", codeListOID,
                                metaDataVersion.getOID(), itemDef.getOID());
                            List<ODMcomplexTypeDefinitionCodeListItem> codeListItemList = odmAnswerList.getCodeListItem();
                            if (codeListItemList == null || codeListItemList.isEmpty()) {
                                LOGGER.debug("No CodeListItems are inside the "
                                    + "CodeList with OID {}. MoPat only "
                                    + "supports CodeListItems. Will write"
                                    + " an error message and finish", odmAnswerList.getOID());
                                result.addValidationMessage(
                                    "import.odm.v132.codeList" + ".codeListItemListNullEmpty",
                                    new String[]{odmAnswerList.getOID(), metaDataVersion.getOID(),
                                        itemDef.getOID()});
                            } else {
                                LOGGER.debug("The CodeList with OID {} "
                                        + "(analysed for creation of question for "
                                        + "ItemDef with OID {}) contains at least one"
                                        + " CodeListItem. Will iterate over them now.",
                                    odmAnswerList.getOID(), itemDef.getOID());
                                boolean orderNumberForAllCodeListItemsPresent = true;
                                Map<Integer, ODMcomplexTypeDefinitionCodeListItem> codeListItemListOrdered = new HashMap<Integer, ODMcomplexTypeDefinitionCodeListItem>();
                                for (ODMcomplexTypeDefinitionCodeListItem codeListItem : codeListItemList) {
                                    LOGGER.debug("Checking the CodeListItem with "
                                            + "CodedValue {} whether it has " + "an OrderNumber or not",
                                        codeListItem.getCodedValue());
                                    if (codeListItem.getOrderNumber() != null) {
                                        LOGGER.debug("The CodeListItem with "
                                                + "CodedValue {} has an OrderNumber, "
                                                + "so I'll put it into the internal "
                                                + "list at position {}", codeListItem.getCodedValue(),
                                            codeListItem.getOrderNumber().intValue());
                                        codeListItemListOrdered.put(
                                            codeListItem.getOrderNumber().intValue(), codeListItem);
                                    } else {
                                        LOGGER.debug(
                                            "The CodeListItem with " + "CodedValue {} does not "
                                                + "have an OrderNumber, so "
                                                + "I'll write an error "
                                                + "message, but continue in "
                                                + "the order as given in the " + "odm file",
                                            codeListItem.getCodedValue());
                                        result.addValidationMessage("import.odm.v132.codeList"
                                                + ".codeListItemNoOrderNumber",
                                            new String[]{codeListItem.getCodedValue(),
                                                metaDataVersion.getOID(), itemDef.getOID()});
                                        orderNumberForAllCodeListItemsPresent = false;
                                    }
                                }
                                LOGGER.debug("Check for OrderNumbers in all "
                                    + "CodeListItems finished (result: {}), now "
                                    + "iterating over the CodeListItems to create" + " Answers");
                                Iterator<ODMcomplexTypeDefinitionCodeListItem> codeListItemIterator;
                                if (orderNumberForAllCodeListItemsPresent) {
                                    // [bt] iterate over my own list, because
                                    // the ordering is correct
                                    List<Integer> orderNumbers = new ArrayList<Integer>(
                                        codeListItemListOrdered.keySet());
                                    Collections.sort(orderNumbers);
                                    List<ODMcomplexTypeDefinitionCodeListItem> codeListItemsOrdered = new ArrayList<ODMcomplexTypeDefinitionCodeListItem>();
                                    for (Integer orderNumber : orderNumbers) {
                                        codeListItemsOrdered.add(
                                            codeListItemListOrdered.get(orderNumber));
                                    }
                                    codeListItemIterator = codeListItemsOrdered.iterator();
                                } else {
                                    // [bt] iterate over codeList (again),
                                    // because at least one CodeListItem does
                                    // not have an OrderNumber
                                    codeListItemIterator = codeListItemList.iterator();
                                }

                                List<Map<String, String>> localizedAnswerTextList = new ArrayList<>();
                                ArrayList<String> codeListItemStrings = new ArrayList<String>();
                                while (codeListItemIterator.hasNext()) {
                                    ODMcomplexTypeDefinitionCodeListItem codeListItem = codeListItemIterator.next();
                                    int minAnswerTextLength = 1;
                                    Map<String, String> localizedAnswerTexts = getTranslations(
                                        codeListItem.getDecode().getTranslatedText(),
                                        minAnswerTextLength);
                                    if (localizedAnswerTexts == null) {
                                        LOGGER.debug("No proper TranslatedText for"
                                                + " the CodeListItem with " + "Coded Value {} found. "
                                                + "Thus, no SelectAnswer will" + " created for this "
                                                + "CodeListItem. Will write "
                                                + "an error message and move "
                                                + "on to the next CodeListItem",
                                            codeListItem.getCodedValue());
                                        result.addValidationMessage("import.odm.v132.codeList"
                                                + ".codeListItemNoTranslatedText",
                                            new String[]{codeListItem.getCodedValue(),
                                                metaDataVersion.getOID(), itemDef.getOID(),
                                                Integer.toString(minAnswerTextLength)});
                                    } else {
                                        localizedAnswerTextList.add(localizedAnswerTexts);
                                        codeListItemStrings.add(codeListItem.getCodedValue());
                                    }
                                }
                                Iterator codeListItemStringIterator = codeListItemStrings.iterator();
                                for (Map<String, String> localizedAnswerText : localizedAnswerTextList) {
                                    Map<String, String> localizedLabelMap = new HashMap<>();
                                    localizedLabelMap.putAll(localizedAnswerText);
                                    if (codeListItemStringIterator.hasNext()) {
                                        String codeListItemString = (String) codeListItemStringIterator.next();
                                        // Create the answer and include the
                                        // export rule
                                        SelectAnswer selectAnswer = new SelectAnswer(question, true,
                                            localizedLabelMap, Boolean.FALSE);
                                        // If the last character is a space
                                        if (codeListItemString.lastIndexOf(" ")
                                            == codeListItemString.length() - 1) {
                                            LOGGER.debug(
                                                "The given CodedValues " + "last character is a "
                                                    + "space and therefore " + "cannot be manually "
                                                    + "mapped.", codeListItemString);
                                            result.addValidationMessage("import.odm.v132.codeList"
                                                    + ".codedValueLastCharacterSpace",
                                                new String[]{codeListItemString});
                                        }
                                        // Set the coded value
                                        selectAnswer.setCodedValue(codeListItemString);
                                        // Try to add the score value to the
                                        // question
                                        try {
                                            selectAnswer.setValue(
                                                Double.parseDouble(codeListItemString));
                                        } catch (NumberFormatException exception) {
                                            // If the value is not of type
                                            // double ignore it
                                            LOGGER.debug("The given CodedValue {} "
                                                    + "is not of type Double "
                                                    + "and thus cannot be set" + " as a score value.",
                                                codeListItemString);
                                            result.addValidationMessage(
                                                "import.odm.v132.codeList" + ".codedValueNotDouble",
                                                new String[]{codeListItemString});
                                        }

                                        for (ExportTemplate exportTemplate : exportTemplates) {
                                            ExportRuleAnswer exportRuleAnswerSelect = new ExportRuleAnswer(
                                                exportTemplate,
                                                itemGroupDef.getOID().replace(".", "u002E")
                                                    .replace("_", "u005F") + "_" + itemDef.getOID()
                                                    .replace(".", "u002E").replace("_", "u005F")
                                                    + "_" + codeListItemString.replace(".", "u002E")
                                                    .replace("_", "u005F"), selectAnswer);
                                            exportRuleAnswerSelect.setExportRuleFormat(
                                                new ExportRuleFormat());
                                            selectAnswer.addExportRule(exportRuleAnswerSelect);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } // [bt] end of Multiple Choice/CodeList stuff
            }
        }
        result.setIdentifier(itemDef.getOID());
        // Loop through all given formal expressions
        if (!formalExpressions.isEmpty()) {
            // Analyse if the formalExpression is MoPat readable
            for (ODMcomplexTypeDefinitionFormalExpression formalExpression : formalExpressions) {
                // Extract condition data from the formal expression
                if (formalExpression.getContext().equals("XPATH")) {
                    ImportConditionResult condition = new ImportConditionResult(
                        formalExpression.getValue());
                    result.addCondition(condition);
                } else {
                    LOGGER.debug("MoPat cannot interpret formal expressions " + "type");
                }
            }
        }

        result.setQuestion(question);
        LOGGER.debug("Leaving convertToQuestion"
            + "(ODMcomplexTypeDefinitionItemDef, Boolean, Integer, Questionnaire,"
            + " ODMcomplexTypeDefinitionMetaDataVersion )");
        return result;
    }

    /**
     * Returns all translations from given TranslatedText elements according to their language
     * code.
     *
     * @param translatedTextList must not be <code>null</code>. Can be empty.
     * @param minLength          TODO
     * @return All Translations from given TranslatedText elements.
     */
    private static Map<String, String> getTranslations(
        List<ODMcomplexTypeDefinitionTranslatedText> translatedTextList, int minLength) {
        Map<String, String> result = new HashMap<>();
        for (ODMcomplexTypeDefinitionTranslatedText translatedText : translatedTextList) {
            String lang = translatedText.getLang();
            if (lang != null) {
                String value = translatedText.getValue().replaceAll("(\\r|\\n)", "");
                if (value != null && value.trim().length() >= minLength) {
                    // Replace '-' with '_' to fullfill the locale standard
// which is
                    // used in MoPat
                    lang = lang.replace('-', '_');
                    // Check if the language code is without country code
                    if (lang.equals("de")) {
                        lang = "de_DE";
                    } else if (lang.equals("fa")) {
                        lang = "fa_IR";
                    }
                    // Check if the locale is avalaible in MoPat
                    if (LocaleHelper.getAvailableLocales().contains(lang)) {
                        result.put(lang, value.trim());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Searches in 3 iterations and stops if an iteration is successful:
     * <ol>
     * <li>for a TranslatedText with lang.equalsIgnoreCase("de-DE") and
     * non-empty content (after trimming)</li>
     * <li>for a TranslatedText with lang.equalsIgnoreCase("de") and non-empty
     * content (after trimming)</li>
     * <li>for a TranslatedText with no lang attribute and non-empty content
     * (after trimming)</li>
     * </ol>
     * Change this code for future versions of MoPat where questions can be
     * modeled with multiple languages.
     *
     * @param translatedTextList must not be <code>null</code>. Can be empty.
     * @param minLength          TODO
     * @return the content of the TranslatedText that was found in the first or, if not before,
     * second, or, if not before, third iteration.
     * <code>null</code> otherwise.
     */
    private static Map<String, String> getBestFittingTranslatedText(
        List<ODMcomplexTypeDefinitionTranslatedText> translatedTextList, int minLength) {
        LOGGER.debug("Enter getBestFittingTranslatedText"
            + "(List<ODMcomplexTypeDefinitionTranslatedText>)");
        Map<String, String> result = null;

        boolean textInDeDePresent = false;
        for (ODMcomplexTypeDefinitionTranslatedText translatedText : translatedTextList) {
            LOGGER.debug("Checking translatedText at position {},",
                translatedTextList.indexOf(translatedText));
            String lang = translatedText.getLang();
            if (lang != null) {
                LOGGER.debug("lang attribute of TranslatedText at position {} is " + "not null",
                    translatedTextList.indexOf(translatedText));
                if (lang.equalsIgnoreCase(TRANSLATED_TEXT_LANG_DE_DE)) {
                    LOGGER.debug("lang attribute of TranslatedText at "
                            + "position {} matches (ignoring case) the wanted lang of" + " {}",
                        translatedTextList.indexOf(translatedText), TRANSLATED_TEXT_LANG_DE_DE);
                    String value = translatedText.getValue().replaceAll("(\\r|\\n)", "");
                    if (value != null && value.trim().length() >= minLength) {
                        LOGGER.debug("Content (i.e. question text) for "
                                + "TranslatedText at position {} has the minimum "
                                + "length ({}) (after trimming). Will set it ({})",
                            translatedTextList.indexOf(translatedText), minLength, value);
                        textInDeDePresent = true;
                        result = new HashMap<>();
                        result.put(MOPAT_LANG_DE_DE, value.trim());
                        break;
                    } else {
                        LOGGER.debug("Although the TranslatedText at position"
                                + " {} provided a matching lang attribute, the "
                                + "content was not long enough ({} chars) (after "
                                + "trimming). Won't take it",
                            translatedTextList.indexOf(translatedText), minLength);
                    }
                } else {
                    LOGGER.debug("TranslatedText at position {} did not have the "
                            + "matching lang attribute of {}",
                        translatedTextList.indexOf(translatedText), TRANSLATED_TEXT_LANG_DE_DE);
                }
            } else {
                LOGGER.debug("TranslatedText at position {} did not have a "
                        + "lang attribute (currently needed, because we're checking " + "for {})",
                    translatedTextList.indexOf(translatedText), TRANSLATED_TEXT_LANG_DE_DE);
            }
        }
        if (!textInDeDePresent) {
            LOGGER.debug("No TranslatedText with lang {} and non-empty content (i"
                    + ".e. question text) could be found. Thus, checking for " + "{} now",
                TRANSLATED_TEXT_LANG_DE_DE, TRANSLATED_TEXT_LANG_DE);
            boolean textInDePresent = false;
            for (ODMcomplexTypeDefinitionTranslatedText translatedText : translatedTextList) {
                LOGGER.debug("Checking translatedText at position {},",
                    translatedTextList.indexOf(translatedText));
                String lang = translatedText.getLang();
                if (lang != null) {
                    LOGGER.debug("lang attribute of TranslatedText at position {} " + "is not null",
                        translatedTextList.indexOf(translatedText));
                    if (lang.equalsIgnoreCase(TRANSLATED_TEXT_LANG_DE)) {
                        LOGGER.debug("lang attribute of TranslatedText at "
                                + "position {} matches (ignoring case) the wanted " + "lang of {}",
                            translatedTextList.indexOf(translatedText), TRANSLATED_TEXT_LANG_DE);
                        String value = translatedText.getValue().replaceAll("(\\r|\\n)", "");
                        if (value != null && value.trim().length() >= minLength) {
                            LOGGER.debug("Content (i.e. question text) for "
                                    + "TranslatedText at position {} has the minimum "
                                    + "length ({}) (after trimming). Will set it ({})",
                                translatedTextList.indexOf(translatedText), minLength, value);
                            textInDePresent = true;
                            result = new HashMap<>();
                            result.put("de", value.trim());
                            break;
                        } else {
                            LOGGER.debug("Although the TranslatedText at "
                                    + "position {} provided a matching lang "
                                    + "attribute, the content was not long enough ({}"
                                    + " chars) (after trimming). Won't take it",
                                translatedTextList.indexOf(translatedText), minLength);
                        }
                    } else {
                        LOGGER.debug("TranslatedText at position {} did not have "
                                + "the matching lang attribute of {}",
                            translatedTextList.indexOf(translatedText), TRANSLATED_TEXT_LANG_DE);
                    }
                } else {
                    LOGGER.debug("TranslatedText at position {} did not have "
                            + "a lang attribute (currently needed, because we're " + "checking for {})",
                        translatedTextList.indexOf(translatedText), TRANSLATED_TEXT_LANG_DE);
                }
            }
            if (!textInDePresent) {
                LOGGER.debug("No TranslatedText with lang {} and non-empty content"
                        + " be found. Thus, checking for default (i.e. no) " + "lang now",
                    TRANSLATED_TEXT_LANG_DE);
                for (ODMcomplexTypeDefinitionTranslatedText translatedText : translatedTextList) {
                    LOGGER.debug("Checking translatedText at position {},",
                        translatedTextList.indexOf(translatedText));
                    String lang = translatedText.getLang();
                    if (lang == null) {// [bt] now the lang attribute is not
                        // of interest to us anymore
                        LOGGER.debug("lang attribute of TranslatedText at position"
                            + " {} is null (good, because we are looking "
                            + "for the default lang)", translatedTextList.indexOf(translatedText));
                        String value = translatedText.getValue().replaceAll("(\\r|\\n)", "");

                        if (value != null && value.trim().length() >= minLength) {
                            LOGGER.debug("Content of TranslatedText at "
                                    + "position {} has the minimum length ({}) (after"
                                    + " trimming). Will set it ({})",
                                translatedTextList.indexOf(translatedText), minLength, value);
                            result = new HashMap<>();
                            result.put(TRANSLATED_TEXT_LANG_DE, value.trim());
                            break;
                        } else {
                            LOGGER.debug("Although the TranslatedText at "
                                    + "position {} was the default one (no lang "
                                    + "attribute), the content was not long enough "
                                    + "({} chars) (after trimming). Won't take it.",
                                translatedTextList.indexOf(translatedText), minLength);
                        }
                    } else {
                        LOGGER.debug("TranslatedText at position {} did have a "
                                + "lang attribute (currently not of interest,"
                                + " because we're checking for the default)",
                            translatedTextList.indexOf(translatedText));
                    }
                }
            }
        }
        LOGGER.debug("Leaving getBestFittingTranslatedText"
            + "(List<ODMcomplexTypeDefinitionTranslatedText>)");
        return result;
    }

    /**
     * @param itemDef        TODO
     * @param rangeCheckList TODO
     * @param result         TODO
     * @return TODO
     */
    private static Double[] getMinMaxFromRangeCheck(ODMcomplexTypeDefinitionItemDef itemDef,
        List<ODMcomplexTypeDefinitionRangeCheck> rangeCheckList, ImportQuestionResult result) {
        Double[] minAndMax = new Double[2];
        Double min = null;
        Double max = null;
        if (rangeCheckList != null && !rangeCheckList.isEmpty()) {
            LOGGER.debug("ItemDef of OID {} contains RangeChecks. Will iterate "
                + "over them to check for RangeChecks about 'GE' and 'LE'.", itemDef.getOID());
            for (ODMcomplexTypeDefinitionRangeCheck rangeCheck : rangeCheckList) {
                LOGGER.debug("Now checking RangeCheck at position {}",
                    rangeCheckList.indexOf(rangeCheck));
                Comparator comparator = rangeCheck.getComparator();
                if (comparator == null) {
                    LOGGER.debug("RangeCheck at position {} did not contain a "
                        + "Comparator. Since this is necessary, the "
                        + "RangeCheck will be skipped.", rangeCheckList.indexOf(rangeCheck));
                } else {
                    LOGGER.debug(
                        "RangeCheck at position {} contains a Comparator." + " Switching over it.",
                        rangeCheckList.indexOf(rangeCheck));
                    switch (comparator) {
                        case GE: {
                            if (min == null) {
                                LOGGER.debug("RangeCheck comparator {} detected. "
                                    + "Checking for 'CheckValue'", comparator);
                                List<ODMcomplexTypeDefinitionCheckValue> checkValueList = rangeCheck.getCheckValue();
                                if (checkValueList == null || checkValueList.isEmpty()) {
                                    LOGGER.debug("RangeCheck at position {} does "
                                            + "not contain any CheckValue " + "elements. Will skip it.",
                                        rangeCheckList.indexOf(rangeCheck));
                                    result.addValidationMessage("import.odm.v132.itemDef"
                                            + ".rangeCheck.checkValueNullEmpty",
                                        new String[]{itemDef.getOID(),
                                            Integer.toString(rangeCheckList.indexOf(rangeCheck))});
                                } else {
                                    LOGGER.debug(
                                        "RangeCheck at position {} " + "contains at least one "
                                            + "CheckValue. Iterating over it.",
                                        rangeCheckList.indexOf(rangeCheck));
                                    for (ODMcomplexTypeDefinitionCheckValue checkValue : checkValueList) {
                                        LOGGER.debug("Now checking the CheckValue "
                                                + "element at position {}.",
                                            checkValueList.indexOf(checkValue));
                                        try {
                                            LOGGER.debug(
                                                "Trying to parse the xml " + "value ('{}') into an "
                                                    + "int", checkValue.getValue());
                                            min = Double.parseDouble(checkValue.getValue());
                                            LOGGER.debug(
                                                "Parsing successful. Min " + "value is: {}", min);
                                            break;
                                        } catch (NumberFormatException nfe) {
                                            LOGGER.debug(
                                                "NumberFormatException " + "when trying to parse "
                                                    + "the CheckValue at " + "position {} of "
                                                    + "RangeCheck at " + "Position: {}",
                                                checkValueList.indexOf(checkValue),
                                                rangeCheckList.indexOf(rangeCheck), nfe);
                                            result.addValidationMessage(
                                                "import.odm.v132.itemDef" + ".rangeCheck.checkValue"
                                                    + ".noDouble", new String[]{itemDef.getOID(),
                                                    Integer.toString(
                                                        rangeCheckList.indexOf(rangeCheck)),
                                                    Integer.toString(
                                                        checkValueList.indexOf(checkValue))});
                                        }
                                    }
                                }
                            } else {
                                LOGGER.debug("RangeCheck at position {} has the "
                                        + "comparator {}, but min value is "
                                        + "already set. Won't consider it.",
                                    rangeCheckList.indexOf(rangeCheck), comparator);
                                result.addValidationMessage(
                                    "import.odm.v132.itemDef" + ".rangeCheckComparatorRedundand",
                                    new String[]{itemDef.getOID(),
                                        Integer.toString(rangeCheckList.indexOf(rangeCheck)),
                                        comparator.toString()});
                            }
                            break;
                        }
                        case LE: {
                            if (max == null) {
                                LOGGER.debug("RangeCheck comparator {} detected. "
                                    + "Checking for 'CheckValue'", comparator);
                                List<ODMcomplexTypeDefinitionCheckValue> checkValueList = rangeCheck.getCheckValue();
                                if (checkValueList == null || checkValueList.isEmpty()) {
                                    LOGGER.debug("RangeCheck at position {} does "
                                            + "not contain any CheckValue " + "elements. Will skip it.",
                                        rangeCheckList.indexOf(rangeCheck));
                                    result.addValidationMessage("import.odm.v132.itemDef"
                                            + ".rangeCheck.checkValueNullEmpty",
                                        new String[]{itemDef.getOID(),
                                            Integer.toString(rangeCheckList.indexOf(rangeCheck))});
                                } else {
                                    LOGGER.debug(
                                        "RangeCheck at position {} " + "contains at least one "
                                            + "CheckValue. Iterating over it.",
                                        rangeCheckList.indexOf(rangeCheck));
                                    for (ODMcomplexTypeDefinitionCheckValue checkValue : checkValueList) {
                                        LOGGER.debug("Now checking the CheckValue "
                                                + "element at position {}.",
                                            checkValueList.indexOf(checkValue));
                                        try {
                                            LOGGER.debug(
                                                "Trying to parse the xml " + "value ('{}') into an "
                                                    + "int", checkValue.getValue());
                                            max = Double.parseDouble(checkValue.getValue());
                                            LOGGER.debug(
                                                "Parsing successful. Max " + "value is: {}", max);
                                            break;
                                        } catch (NumberFormatException nfe) {
                                            LOGGER.debug(
                                                "NumberFormatException " + "when trying to parse "
                                                    + "the CheckValue at " + "position {} of "
                                                    + "RangeCheck at " + "Position: {}",
                                                checkValueList.indexOf(checkValue),
                                                rangeCheckList.indexOf(rangeCheck), nfe);
                                            result.addValidationMessage(
                                                "import.odm.v132.itemDef" + ".rangeCheck.checkValue"
                                                    + ".noDouble", new String[]{itemDef.getOID(),
                                                    Integer.toString(
                                                        rangeCheckList.indexOf(rangeCheck)),
                                                    Integer.toString(
                                                        checkValueList.indexOf(checkValue))});
                                        }
                                    }
                                }
                            } else {
                                LOGGER.debug("RangeCheck at position {} has the "
                                        + "comparator {}, but max value is "
                                        + "already set. Won't consider it.",
                                    rangeCheckList.indexOf(rangeCheck), comparator);
                                result.addValidationMessage(
                                    "import.odm.v132.itemDef" + ".rangeCheckComparatorRedundand",
                                    new String[]{itemDef.getOID(),
                                        Integer.toString(rangeCheckList.indexOf(rangeCheck)),
                                        comparator.toString()});
                            }
                            break;
                        }
                        default: {
                            LOGGER.debug("RangeCheck at position {} contains a "
                                    + "Comparator I don't support ({}).",
                                rangeCheckList.indexOf(rangeCheck), comparator);
                            result.addValidationMessage(
                                "import.odm.v132.itemDef" + ".rangeCheckComparatorUnsupported",
                                new String[]{itemDef.getOID(),
                                    Integer.toString(rangeCheckList.indexOf(rangeCheck)),
                                    comparator.toString()});
                            break;
                        }
                    }
                }
            }// [bt] finish with searching for min and max values

            if (min != null && max != null && max <= min) {
                LOGGER.debug("Min and Max were found, but max ({}) was not > " + "than min ({}).");
                result.addValidationMessage("import.odm.v132.itemDef.rangeCheckMaxSmallerMin",
                    new String[]{itemDef.getOID(), Double.toString(max), Double.toString(min)});
                max = null;
                min = null;
            }
        }
        minAndMax[0] = min;
        minAndMax[1] = max;
        return minAndMax;
    }
}
