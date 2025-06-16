package de.imi.mopat.io.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.imi.mopat.dao.*;
import de.imi.mopat.io.MetadataExporter;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.*;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.*;
import org.hl7.fhir.r4b.model.*;
import org.hl7.fhir.r4b.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemEnableWhenComponent;
import org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemType;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An exporter for FHIR metadata reprasentation of a {@link Questionnaire}.
 */
public class MetadataExporterFhirR4b implements MetadataExporter {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(MetadataExporterPDF.class);
    private final String ANSWER_OID = "MoPat/Answer/";

    @Autowired
    private ConditionDao conditionDao;

    /**
     * This method converts a {@link Questionnaire} object to a fhir
     * questionnaire resource and encodes this resource to a string returned as
     * byte array.
     *
     * @param questionnaire    Metadata to convert.
     * @param messageSource    Containing message resources.
     * @param configurationDao Data access object to get the system's
     *                         configuration state.
     * @return Byte array containing the bytes of the metadata xml string.
     */
    @Override
    public byte[] export(
            final Questionnaire questionnaire,
            final MessageSource messageSource,
            final ConfigurationDao configurationDao,
            final ConfigurationGroupDao configurationGroupDao,
            final ExportTemplateDao exportTemplateDao,
            final QuestionnaireDao questionnaireDao,
            final QuestionDao questionDao,
            final ScoreDao scoreDao) {

        org.hl7.fhir.r4b.model.Questionnaire fhirQuestionnaire =
                new org.hl7.fhir.r4b.model.Questionnaire();

        fhirQuestionnaire.setName(questionnaire.getName());

        // Convert localizedDisplayName to fhir translation extensions
        Iterator<Map.Entry<String, String>> iterator =
                questionnaire.getLocalizedDisplayName()
                             .entrySet()
                             .iterator();
        Map.Entry<String, String> entry = iterator.next();
        StringType text = new StringType();
        text.setValue(entry.getValue());
        text.addExtension(
                "http://hl7.org/fhir/StructureDefinition/language",
                new CodeType(entry.getKey()
                                  .replace(
                                          "_",
                                          "-")));
        Extension languageExtension;
        while (iterator.hasNext()) {
            entry = iterator.next();
            languageExtension = new Extension();
            languageExtension.setUrl("http://hl7.org/fhir/StructureDefinition"
                                             + "/translation");
            languageExtension.addExtension(
                    "http://hl7.org/fhir/StructureDefinition/language",
                    new CodeType(entry.getKey()
                                      .replace(
                                              "_",
                                              "-")));
            languageExtension.addExtension(
                    "content",
                    new StringType(entry.getValue()));
            text.addExtension(languageExtension);
        }

        fhirQuestionnaire.setTitleElement(text);

        fhirQuestionnaire.setDescription(questionnaire.getDescription());
        if (questionnaire.getUpdatedAt()
                != null) {
            fhirQuestionnaire.setDate(new Date(questionnaire.getUpdatedAt()
                                                            .getTime()));
        }

        if (questionnaire.isPublished()) {
            fhirQuestionnaire.setStatus(PublicationStatus.ACTIVE);
        } else {
            fhirQuestionnaire.setStatus(PublicationStatus.DRAFT);
        }

        Map<Question, QuestionnaireItemComponent> triggeringQuestions =
                new HashMap<>();
        Map<Question, QuestionnaireItemComponent> targetQuestions =
                new HashMap<>();
        // Convert the questionnaire's questions to fhir items
        for (Question question : questionnaire.getQuestions()) {
            QuestionnaireItemComponent item = new QuestionnaireItemComponent();

            item.setLinkId(Question.class.getSimpleName()
                                   + "/"
                                   + question.getId());
            item.setRepeats(false);
            item.setRequired(question.getIsRequired());

            // Convert the localizedQuestionText to fhir translation extensions
            StringType questionText = new StringType();
            iterator = question.getLocalizedQuestionText()
                               .entrySet()
                               .iterator();
            entry = iterator.next();
            questionText.addExtension(
                    "http://hl7.org/fhir/StructureDefinition/language",
                    new CodeType(entry.getKey()
                                      .replace(
                                              "_",
                                              "-")));
            questionText.setValue(entry.getValue());
            while (iterator.hasNext()) {
                entry = iterator.next();
                languageExtension = new Extension();
                languageExtension.setUrl("http://hl7.org/fhir/StructureDefinition/translation");
                languageExtension.addExtension(
                        "http://hl7.org/fhir/StructureDefinition/language",
                        new CodeType(entry.getKey()
                                          .replace(
                                                  "_",
                                                  "-")));
                languageExtension.addExtension(
                        "content",
                        new StringType(entry.getValue()));
                questionText.addExtension(languageExtension);
            }
            item.setTextElement(questionText);

            // Mapping the question type from mopat to fhir questionnaire
            // item type
            QuestionnaireItemComponent freeTextItem = null;
            switch (question.getQuestionType()) {
                case DROP_DOWN:
                case MULTIPLE_CHOICE:
                    item.setType(QuestionnaireItemType.CHOICE);
                    for (Answer answer : question.getAnswers()) {
                        if (answer instanceof SelectAnswer) {
                            SelectAnswer selectAnswer = (SelectAnswer) answer;
                            //If selectAnswer isOther is false export the answer
                            if (!selectAnswer.getIsOther()) {

                                org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemAnswerOptionComponent option =
                                        new org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemAnswerOptionComponent();
                                Coding coding = new Coding();
                                StringType display = new StringType();

                                //Convert localizedAnswerText to fhir
                                // translation extensions
                                iterator = selectAnswer.getLocalizedLabel()
                                                       .entrySet()
                                                       .iterator();
                                entry = iterator.next();
                                display.setValue(entry.getValue());
                                display.addExtension(
                                        "http://hl7.org/fhir/StructureDefinition/language",
                                        new CodeType(entry.getKey()
                                                          .replace(
                                                                  "_",
                                                                  "-")));
                                while (iterator.hasNext()) {
                                    entry = iterator.next();
                                    languageExtension = new Extension();
                                    languageExtension.setUrl("http://hl7.org/fhir/StructureDefinition/translation");
                                    languageExtension.addExtension(
                                            "http://hl7.org/fhir/StructureDefinition/language",
                                            new CodeType(entry.getKey()
                                                              .replace(
                                                                      "_",
                                                                      "-")));
                                    languageExtension.addExtension(
                                            "content",
                                            new StringType(entry.getValue()));
                                    display.addExtension(languageExtension);
                                }

                                coding.setDisplayElement(display);
                                coding.setCode(ANSWER_OID
                                                       + answer.getId());
                                coding.setSystem(configurationDao.getFHIRsystemURI());
                                if (selectAnswer.getValue()
                                        != null) {
                                    coding.addExtension(
                                            "http://hl7.org/fhir/StructureDefinition/questionnaire-ordinalValue",
                                            new DecimalType(selectAnswer.getValue()));
                                }
                                option.setValue(coding);
                                item.addAnswerOption(option);
                            } else {
                                //otherwise ignore the whole answer, because
                                // an isOther answer is create automatically
                                // during import
                                //and the item type is set to openchoice
                                item.setType(QuestionnaireItemType.OPENCHOICE);
                            }
                        }
                    }

                    item.addExtension(
                            "http://hl7.org/fhir/StructureDefinition"
                                    + "/questionnaire-minOccurs",
                            new IntegerType(question.getMinNumberAnswers()));
                    item.addExtension(
                            "http://hl7.org/fhir/StructureDefinition"
                                    + "/questionnaire-maxOccurs",
                            new IntegerType(question.getMaxNumberAnswers()));

                    break;
                case INFO_TEXT:
                    item.setType(QuestionnaireItemType.DISPLAY);
                    break;
                case NUMBER_CHECKBOX_TEXT:
                    SliderFreetextAnswer sliderFreetextAnswer =
                            (SliderFreetextAnswer) question.getAnswers()
                                                           .get(0);
                    freeTextItem = new QuestionnaireItemComponent();
                    freeTextItem.setType(QuestionnaireItemType.TEXT);
                    StringType freeText = new StringType();
                    iterator = sliderFreetextAnswer.getLocalizedFreetextLabel()
                                                   .entrySet()
                                                   .iterator();
                    entry = iterator.next();
                    freeText.setValue(entry.getValue());
                    freeText.addExtension(
                            "http://hl7.org/fhir/StructureDefinition/language",
                            new CodeType(entry.getKey()
                                              .replace(
                                                      "_",
                                                      "-")));
                    freeText.setValue(entry.getValue());
                    while (iterator.hasNext()) {
                        entry = iterator.next();
                        languageExtension = new Extension();
                        languageExtension.setUrl("http://hl7.org/fhir/StructureDefinition/translation");
                        languageExtension.addExtension(
                                "http://hl7.org/fhir/StructureDefinition/language",
                                new CodeType(entry.getKey()
                                                  .replace(
                                                          "_",
                                                          "-")));
                        languageExtension.addExtension(
                                "content",
                                new StringType(entry.getValue()));
                        freeText.addExtension(languageExtension);
                    }
                case NUMBER_CHECKBOX:
                case SLIDER:
                    SliderAnswer sliderAnswer =
                            (SliderAnswer) question.getAnswers()
                                                   .get(0);

                    // If minimum value or stepsize is a decimal number the
                    // corresponding item type is set to decimal
                    if (sliderAnswer.getStepsize()
                            - sliderAnswer.getStepsize()
                                          .intValue()
                            > 0
                            || sliderAnswer.getMinValue()
                            - sliderAnswer.getMinValue()
                                          .intValue()
                            > 0) {
                        item.setType(QuestionnaireItemType.DECIMAL);
                    } else {
                        item.setType(QuestionnaireItemType.INTEGER);
                    }

                    item.addExtension(
                            "http://hl7.org/fhir/StructureDefinition/minValue",
                            new DecimalType(sliderAnswer.getMinValue()));
                    item.addExtension(
                            "http://hl7.org/fhir/StructureDefinition/maxValue",
                            new DecimalType(sliderAnswer.getMaxValue()));
                    break;
                case NUMBER_INPUT:
                    NumberInputAnswer numberInputAnswer =
                            (NumberInputAnswer) question.getAnswers()
                                                        .get(0);
                    item.setType(QuestionnaireItemType.DECIMAL);
                    if (numberInputAnswer.getStepsize()
                            != null
                            && (numberInputAnswer.getStepsize()
                            - numberInputAnswer.getStepsize()
                                               .intValue())
                            == 0) {
                        item.setType(QuestionnaireItemType.INTEGER);
                    }
                    if (numberInputAnswer.getMinValue()
                            != null) {
                        item.addExtension(
                                "http://hl7.org/fhir/StructureDefinition"
                                        + "/minValue",
                                new DecimalType(numberInputAnswer.getMinValue()));
                    }
                    if (numberInputAnswer.getMaxValue()
                            != null) {
                        item.addExtension(
                                "http://hl7.org/fhir/StructureDefinition"
                                        + "/maxValue",
                                new DecimalType(numberInputAnswer.getMaxValue()));
                    }

                    break;
                case IMAGE:
                    //TODO: transfer image as base64String to item
                    break;
                case DATE:
                    item.setType(QuestionnaireItemType.DATE);

                    DateAnswer dateAnswer = (DateAnswer) question.getAnswers()
                                                                 .get(0);
                    SimpleDateFormat dateFormat =
                            new SimpleDateFormat("yyyy-MM-dd");
                    if (dateAnswer.getStartDate()
                            != null) {
                        item.addExtension(
                                "http://hl7.org/fhir/StructureDefinition"
                                        + "/minValue",
                                new DateType(dateFormat.format(dateAnswer.getStartDate())));
                    }
                    if (dateAnswer.getEndDate()
                            != null) {
                        item.addExtension(
                                "http://hl7.org/fhir/StructureDefinition"
                                        + "/maxValue",
                                new DateType(dateFormat.format(dateAnswer.getEndDate())));
                    }
                    break;
                case FREE_TEXT:
                case BARCODE:
                    item.setType(QuestionnaireItemType.TEXT);
                    break;
                default:
                    break;
            }

            fhirQuestionnaire.addItem(item);
            if (freeTextItem
                    != null) {
                fhirQuestionnaire.addItem(freeTextItem);
            }

            if (question.hasConditionsAsTrigger()) {
                triggeringQuestions.put(
                        question,
                        item);
            }

            if (question.hasConditionsAsTarget()) {
                targetQuestions.put(
                        question,
                        item);
            }
        }

        // Get all conditions and convert them to fhir enablewhen components
        for (Map.Entry<Question, QuestionnaireItemComponent> currentEntry
                : triggeringQuestions.entrySet()) {
            Question question = currentEntry.getKey();
            QuestionnaireItemComponent item = currentEntry.getValue();

            // Get all answers and get the conditions
            for (Answer answer : question.getAnswers()) {
                for (Condition condition : answer.getConditions()) {
                    // Only include conditions targeting at questions
                    if (condition.getTarget() instanceof Question) {
                        switch (question.getQuestionType()) {
                            case MULTIPLE_CHOICE:
                            case DROP_DOWN:
                                SelectAnswerCondition selectAnswerCondition =
                                        (SelectAnswerCondition) condition;
                                if (selectAnswerCondition.getAction()
                                        == ConditionActionType.ENABLE) {
                                    for (org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemAnswerOptionComponent option : item.getAnswerOption()) {
                                        try {
                                            if (option.getValueCoding()
                                                      .getCode()
                                                      .equals(ANSWER_OID
                                                                      + answer.getId())) {
                                                QuestionnaireItemEnableWhenComponent
                                                        enableWhen =
                                                        new QuestionnaireItemEnableWhenComponent();
                                                enableWhen.setAnswer(option.getValueCoding());
                                                enableWhen.setQuestion(item.getLinkId());
                                                targetQuestions.get(selectAnswerCondition.getTarget())
                                                               .addEnableWhen(enableWhen);
                                            }
                                        } catch (FHIRException ex) {
                                        }
                                    }
                                }
                                break;
                            case NUMBER_CHECKBOX:
                            case NUMBER_INPUT:
                            case SLIDER:
                                SliderAnswerThresholdCondition
                                        sliderAnswerCondition =
                                        (SliderAnswerThresholdCondition) condition;
                                if ((sliderAnswerCondition.getAction()
                                        == ConditionActionType.ENABLE
                                        && sliderAnswerCondition.getThresholdComparisonType()
                                        == ThresholdComparisonType.EQUALS)
                                        || (sliderAnswerCondition.getAction()
                                        == ConditionActionType.DISABLE
                                        && sliderAnswerCondition.getThresholdComparisonType()
                                        == ThresholdComparisonType.NOT_EQUALS)) {
                                    QuestionnaireItemEnableWhenComponent
                                            enableWhen =
                                            new QuestionnaireItemEnableWhenComponent();
                                    enableWhen.setAnswer(new DecimalType(sliderAnswerCondition.getThreshold()));
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

        FhirContext ctx = FhirContext.forR4B();
        IParser parser = ctx.newXmlParser()
                            .setPrettyPrint(true);
        
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            String questionnaireAsFhirXmlString =
                    parser.encodeResourceToString(fhirQuestionnaire);
            bos.write(questionnaireAsFhirXmlString.getBytes());
            return bos.toByteArray();
        } catch (IOException | NullPointerException ex) {
            LOGGER.error(
                    "Error while exporting questionnaire as FHIR metadata "
                            + "file: {}",
                    ex);
        }

        return new byte[0];
    }
}
