package de.imi.mopat.io.importer.fhir;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.i18n.HapiLocalizer;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.IParserErrorHandler;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import de.imi.mopat.io.importer.ImportQuestionnaireValidation;
import de.imi.mopat.model.enumeration.FHIRExtensionType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.DecimalType;
import org.hl7.fhir.dstu3.model.Element;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IntegerType;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseItemComponent;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseStatus;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.StructureDefinition;
import org.hl7.fhir.dstu3.model.TimeType;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class contains methods, which aren't fixed to a specific class.
 */
public class FhirDstu3Helper extends FhirHelper {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FhirDstu3Helper.class);
    private static FhirContext context;
    private static final IParser PARSER = getContext().newXmlParser();

    /**
     * Set the error handler to the fhir parser.
     *
     * @param errorHandler ErrorHandler that handles the errors thrown while parsing a file
     */
    public static void setParserValidator(final IParserErrorHandler errorHandler) {
        PARSER.setParserErrorHandler(errorHandler);
    }

    /**
     * This method validates a file against a XML-Schema-Definition file.
     *
     * @param fileToValidate                {@link File} instance that has to be validated.
     * @param validationSchemaFileDirectory The directory where the schema file is saved.
     * @param validationSchemaFileName      The name of the schema file.
     * @param result                        Stores the errors occuring during the validation
     *                                      process.
     * @param messageSource                 Spring messageSource instance containing validation
     *                                      messages.
     * @return True, if the validation was successfull, otherwise there's a
     * {@link DataFormatException} thrown.
     */
    @Deprecated
    public static boolean validateFileAgainstSchema(final MultipartFile fileToValidate,
        final String validationSchemaFileDirectory, final String validationSchemaFileName,
        final ImportQuestionnaireValidation result, final MessageSource messageSource) {
        LOGGER.info("Validating questionnaire resource file against xml " + "schema definition...");

        if (validationSchemaFileDirectory == null || validationSchemaFileDirectory.trim()
            .isEmpty()) {
            result.reject("import.fhir.validate.schemaFileDirectoryNull");
            LOGGER.info("ERROR: Directory of xsd is empty or null.");
            return false;
        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = null;
        Document document = null;
        File validationSchemaFile = new File(validationSchemaFileDirectory,
            validationSchemaFileName);
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(fileToValidate.getInputStream());
            SchemaFactory schemaFactory = SchemaFactory.newInstance(
                "http://www.w3.org/2001/XMLSchema");
            Schema schema = schemaFactory.newSchema(validationSchemaFile);
            documentBuilderFactory.setSchema(schema);
            Validator schemaValidator = schema.newValidator();
            DOMSource source = new DOMSource(document);
            try {
                schemaValidator.validate(source);
            } catch (SAXException e) {
                result.reject("import.fhir.validate.invalidFile",
                    new Object[]{e.getLocalizedMessage()}, "File is invalid: {}");
                LOGGER.info("Validation failed. File is invalid. {}", e.getMessage());
                return false;
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            result.reject("import.fhir.validate.error", new Object[]{e.getLocalizedMessage()},
                "Error during validation: {}");
            LOGGER.info("ERROR: {}", e.getMessage());
            return false;
        }
        LOGGER.info("Validation succeeded. File is valid.");
        return true;
    }

    /**
     * Validates a given resource string with the HAPI resource instance validators.
     *
     * @param fhirResourceString to validate
     * @param errors             validation object to store error messages in
     * @param frontendLocale     locale of the frontend, used to get the correct translation
     * @return true, if valid; false otherwise
     */
    public static boolean validateFileWithFhirInstanceValidator(final String fhirResourceString,
        final ImportQuestionnaireValidation errors, String frontendLocale) {

        Locale originalLocale = Locale.getDefault();

        try {
            frontendLocale = frontendLocale.replace("_", "-");

            Locale selectedLocale = Locale.forLanguageTag(frontendLocale);
            Locale.setDefault(selectedLocale);

            reinitContext();

            ValidationSupportChain validationSupport = new ValidationSupportChain(
                new DefaultProfileValidationSupport(getContext()),
                new InMemoryTerminologyServerValidationSupport(getContext()));

            PrePopulatedValidationSupport prePopulated = new PrePopulatedValidationSupport(
                getContext());
            StructureDefinition translationExt = getContext().newJsonParser()
                .parseResource(StructureDefinition.class, FhirDstu3Helper.class.getResourceAsStream(
                    "/fhir/StructureDefinition-translation.json"));

            prePopulated.addStructureDefinition(translationExt);
            validationSupport.addValidationSupport(prePopulated);

            FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupport);

            FhirValidator validator = getContext().newValidator();
            validator.registerValidatorModule(instanceValidator);

            IValidatorModule module = new FhirInstanceValidator(FhirDstu3Helper.getContext());
            validator.registerValidatorModule(module);

            ValidationResult result = validator.validateWithResult(fhirResourceString);

            List<SingleValidationMessage> messages = result.getMessages().stream().filter(
                    singleValidationMessage ->
                        singleValidationMessage.getSeverity() == ResultSeverityEnum.ERROR
                            || singleValidationMessage.getSeverity() == ResultSeverityEnum.FATAL)
                .toList();

            for (SingleValidationMessage message : messages) {
                addDefaultError(errors, message);
            }

            return messages.isEmpty();
        } finally {
            Locale.setDefault(originalLocale);
            reinitContext();
        }
    }


    /**
     * Returns a singleton instance of class {@link FhirContext}.
     *
     * @return FhirContext instance.
     */
    public static FhirContext getContext() {
        if (context == null) {
            context = FhirContext.forDstu3();
        }
        return context;
    }

    /**
     * Reinitializes the FHIR context  with the DSTU 3 version of FHIR.
     */
    public static void reinitContext() {
        context = FhirContext.forDstu3();
    }

    /**
     * Encodes the given resource to XML format and writes it to the given file.
     *
     * @param resource Resource instance that should be encoded to xml data format.
     * @param file     File where the encoded resource should be written to.
     */
    public static void writeResourceToFile(final Resource resource, final File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            PARSER.setPrettyPrint(true);
            PARSER.encodeResourceToWriter(resource, writer);
        } catch (IOException | DataFormatException e) {
            LOGGER.error("Error while writing resource...", e);
        }
    }

    /**
     * Parses the given inputStream to a {@link IBaseResource IBaseResource} object.
     *
     * @param inputStream InputStream which contains the file to parse.
     * @return Resource the file is representing.
     */
    public static IBaseResource parseResourceFromFile(final InputStream inputStream)
        throws ConfigurationException, DataFormatException {
        return PARSER.parseResource(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    /**
     * Sets {@link QuestionnaireResponse} object which consists of {@link Questionnaire} object.
     *
     * @param questionnaire Object the questionnaireResponse is created of.
     * @return The {@link QuestionnaireResponse} object appropriate to the questionnaire.
     */
    public static QuestionnaireResponse getQuestionnaireResponse(
        final Questionnaire questionnaire) {
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        questionnaireResponse.setQuestionnaire(new Reference(questionnaire.getUrl()));
        questionnaireResponse.addContained(questionnaire);
        questionnaireResponse.setStatus(QuestionnaireResponseStatus.INPROGRESS);

        for (Questionnaire.QuestionnaireItemComponent currentItem : questionnaire.getItem()) {
            questionnaireResponse.addItem(transferItemToResponseItem(currentItem));
        }
        return questionnaireResponse;
    }

    /**
     * Collects all answer as instance of {QuestionnaireResponseItemAnswerComponent} of
     * {@link QuestionnaireResponse} in a plain list.
     *
     * @param questionnaireResponse {@link QuestionnaireResponse} object containing the answers.
     * @return List of all answers.
     */
    public static List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> getAllAnswersOfQuestionnaireResponse(
        final QuestionnaireResponse questionnaireResponse) {
        List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answers = new ArrayList<>();

        for (QuestionnaireResponse.QuestionnaireResponseItemComponent currentItem : questionnaireResponse.getItem()) {
            answers.addAll(getAllAnswersOfResponseItem(currentItem));
        }
        return answers;
    }

    /**
     * Collects all answers of given item and returns it as list.
     *
     * @param item Object that contains the answers to collect.
     * @return List of answers that should be collected.
     */
    private static List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> getAllAnswersOfResponseItem(
        final QuestionnaireResponse.QuestionnaireResponseItemComponent item) {
        List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answers = new ArrayList<>();

        for (QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer : item.getAnswer()) {
            answers.add(answer);
        }
        for (QuestionnaireResponse.QuestionnaireResponseItemComponent currentItem : item.getItem()) {
            answers.addAll(getAllAnswersOfResponseItem(currentItem));
        }
        return answers;
    }

    /**
     * Transfers data of FHIR fhirQuestionnaire item as HAPI {@link QuestionnaireItemComponent}
     * object to FHIR questionnaireResponse item as HAPI {@link QuestionnaireResponseItemComponent}
     * object.
     *
     * @param item QuestionnaireItemComponent object which contains data that should be
     *             transferred.
     * @return QuestionnaireResponseItemComponent object containing the transferred data.
     */
    private static QuestionnaireResponse.QuestionnaireResponseItemComponent transferItemToResponseItem(
        final Questionnaire.QuestionnaireItemComponent item) {
        //if the item type is not supported by mopat ignore it
        QuestionnaireResponse.QuestionnaireResponseItemComponent responseItem = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        responseItem.setLinkId(item.getLinkId());
        responseItem.setLinkIdElement(item.getLinkIdElement());
        responseItem.setText(item.getText());
        responseItem.setTextElement(item.getTextElement());

        if (item.getType() != Questionnaire.QuestionnaireItemType.CHOICE
            && item.getType() != Questionnaire.QuestionnaireItemType.OPENCHOICE) {
            if (item.getType() == Questionnaire.QuestionnaireItemType.GROUP) {
                //Item has other items, so loop through those ones and call
                // this method again
                for (Questionnaire.QuestionnaireItemComponent currentItem : item.getItem()) {
                    responseItem.addItem(transferItemToResponseItem(currentItem));
                }
            } else {
                //Set the single answer value type
                QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
                answer.setId(item.getLinkId());
                switch (item.getType()) {
                    case BOOLEAN:
                        answer.setValue(new BooleanType());
                        break;
                    case DATE:
                        answer.setValue(new DateType());
                        break;
                    case DECIMAL:
                        answer.setValue(new DecimalType());
                        break;
                    case INTEGER:
                        answer.setValue(new IntegerType());
                        break;
                    case DISPLAY:
                    case STRING:
                    case TEXT:
                        answer.setValue(new StringType());
                        break;
                    default:
                        break;
                }
                responseItem.addAnswer(answer);
                for (Questionnaire.QuestionnaireItemComponent currentItem : item.getItem()) {
                    responseItem.addItem(transferItemToResponseItem(currentItem));
                }
            }
        } else {
            //Options can be defined as a set of options adhering in the
            // resource file (first option) or as reference or contained
            // reference as valueSet (second option)
            if (item.getOption() != null && !item.getOption().isEmpty()) {
                for (Questionnaire.QuestionnaireItemOptionComponent option : item.getOption()) {
                    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
                    try {
                        if (option.getValue() instanceof Coding) {
                            answer.setId(option.getValueCoding().getCode());
                            answer.setValue(new BooleanType());
                        } else if (option.getValue() instanceof StringType) {
                            answer.setId(option.getValueStringType().asStringValue());
                            answer.setValue(new BooleanType());
                        } else if (option.getValue() instanceof DateType) {
                            answer.setId(option.getValueDateType().asStringValue());
                            answer.setValue(new BooleanType());
                        } else if (option.getValue() instanceof IntegerType) {
                            answer.setId(option.getValueIntegerType().asStringValue());
                            answer.setValue(new BooleanType());
                        } else if (option.getValue() instanceof TimeType) {
                            answer.setId(option.getValueTimeType().asStringValue());
                            answer.setValue(new BooleanType());
                        }
                        responseItem.addAnswer(answer);
                    } catch (FHIRException e) {
                        LOGGER.debug(
                            "Mapping questionnaire option to " + "questionnaire response answer "
                                + "failed" + ". {}", e);
                    }
                }
                for (Questionnaire.QuestionnaireItemComponent currentItem : item.getItem()) {
                    responseItem.addItem(transferItemToResponseItem(currentItem));
                }
            } else if (item.getOptions() != null && !item.getOptions().isEmpty()) {
                ValueSet valueSet = (ValueSet) item.getOptions().getResource();
                if (valueSet != null) {
                    for (ValueSet.ConceptSetComponent conceptComponent : valueSet.getCompose()
                        .getInclude()) {
                        for (ValueSet.ConceptReferenceComponent conceptReference : conceptComponent.getConcept()) {
                            QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
                            answer.setId(conceptReference.getCode());
                            answer.setValue(new BooleanType());
                            responseItem.addAnswer(answer);
                        }
                    }
                }
            }
            if (item.getType() == Questionnaire.QuestionnaireItemType.OPENCHOICE) {
                QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answer = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
                answer.setId(item.getLinkId() + "/other");
                answer.setValue(new StringType());
                responseItem.addAnswer(answer);
            }
        }
        return responseItem;
    }

    /**
     * Searchs for the {@link QuestionnaireItemComponent item} identified by the given linkId.
     *
     * @param linkId identifying the item element.
     * @param items  List searched for the item element.
     * @return {@link QuestionnaireItemComponent} object if the list contains the item, or
     * <code>null</code> if it's not the case.
     */
    public static QuestionnaireItemComponent getItemByLinkId(final String linkId,
        final List<QuestionnaireItemComponent> items) {
        QuestionnaireItemComponent item = null;
        for (QuestionnaireItemComponent currentItem : items) {
            if (item != null) {
                break;
            } else if (currentItem.getLinkId().equals(linkId)) {
                item = currentItem;
            } else if (currentItem.hasItem()) {
                item = getItemByLinkId(linkId, currentItem.getItem());
            }
        }
        return item;
    }

    /**
     * Collects all {@link QuestionnaireItemComponent items} a item contains and returns it as
     * list.
     *
     * @param item Object searched for items.
     * @return List of all items that are located at the searched item.
     */
    public static List<QuestionnaireItemComponent> getAllItems(
        final QuestionnaireItemComponent item) {
        List<QuestionnaireItemComponent> items = new ArrayList<>();
        for (QuestionnaireItemComponent child : item.getItem()) {
            items.add(child);
            items.addAll(getAllItems(child));
        }
        return items;
    }

    /**
     * Collects all locales from a translation extension.
     *
     * @param element Object that contains the extensions.
     * @return List of all locales.
     */
    public static List<String> getLocaleAsStringFromLanguageExtension(final Element element) {
        List<String> localesAsString = new ArrayList<>();

        loop:
        for (Extension extension : element.getExtensionsByUrl(
            FHIRExtensionType.TRANSLATION.getTextValue())) {
            List<Extension> languageCode = extension.getExtensionsByUrl("lang");
            if (languageCode.get(0) == null || languageCode.get(0).isEmpty()) {
                continue loop;
            }
            if (languageCode.size() != 1) {
                LOGGER.info("Ignoring redundant languages in language " + "extensions.");
            }
            CodeType locale = (CodeType) languageCode.get(0).getValue();
            localesAsString.add(locale.asStringValue().replace("-", "_"));
        }

        return localesAsString;
    }

    /**
     * This collects all translations mapped to it's locales as String for single element.
     *
     * @param element Element containing the extensions.
     * @return Locales as key mapped to the translation text as value.
     */
    public static Map<String, String> getLanugageMapFromLanguageExtension(final Element element) {
        Map<String, String> languageMap = new HashMap<>();

        for (Extension extension : element.getExtensionsByUrl(
            FHIRExtensionType.TRANSLATION.getTextValue())) {
            List<Extension> languageCode = extension.getExtensionsByUrl("lang");
            List<Extension> content = extension.getExtensionsByUrl("content");
            if (languageCode.size() == 1 && content.size() == 1) {
                CodeType locale = (CodeType) languageCode.get(0).getValue();
                StringType translation = (StringType) content.get(0).getValue();
                languageMap.put(locale.getValueAsString().replace("-", "_"),
                    translation.asStringValue());
            } else {
                LOGGER.error("Language extension can't be mapped. Extension "
                    + "will be ignored because it's invalid. "
                    + "It contains more or less than one " + "language codes or translations.");
            }
        }

        return languageMap;
    }

    /**
     * Gets the ordinalValue extension's value.
     *
     * @param element Element containing the extension.
     * @return {Score Score's} value as double.
     */
    public static Double getScoreFromExtension(final Element element) {
        List<Extension> extensions = element.getExtensionsByUrl(
            FHIRExtensionType.SCORE.getTextValue());
        if (extensions != null && !extensions.isEmpty()) {
            DecimalType value = null;
            try {
                value = (DecimalType) extensions.get(0).getValue();
            } catch (Exception e) {
                LOGGER.debug(
                    "Casting score (ordinalValue) extension to " + "DecimalType failed. {}", e);
            }
            return value.getValue().doubleValue();
        }
        return null;
    }

    /**
     * Gets the min and the max value of the element's min and max value extensions.
     *
     * @param element            Object containing the extensions.
     * @param getNumberOfAnswers Differs between the meaning of the extension, either it aims for
     *                           the min and max number of selectable answers, or it specifies the
     *                           min and max value of a answer that requires a number as input.
     * @return {@link Entry} containing the max value as key and the min value as value.
     */
    public static Entry<Double, Double> getMinAndMaxFromExtension(final Element element,
        final Boolean getNumberOfAnswers) {
        Extension min = null;
        Extension max = null;
        Double key = null;
        Double value = null;
        if (getNumberOfAnswers) {
            if (element.getExtensionsByUrl(FHIRExtensionType.MAX_NUMBER_ANSWER.getTextValue())
                != null && !element.getExtensionsByUrl(
                FHIRExtensionType.MAX_NUMBER_ANSWER.getTextValue()).isEmpty()) {
                max = element.getExtensionsByUrl(FHIRExtensionType.MAX_NUMBER_ANSWER.getTextValue())
                    .get(0);
            }
            if (element.getExtensionsByUrl(FHIRExtensionType.MIN_NUMBER_ANSWER.getTextValue())
                != null && !element.getExtensionsByUrl(
                FHIRExtensionType.MIN_NUMBER_ANSWER.getTextValue()).isEmpty()) {
                min = element.getExtensionsByUrl(FHIRExtensionType.MIN_NUMBER_ANSWER.getTextValue())
                    .get(0);
            }
        } else {
            if (element.getExtensionsByUrl(FHIRExtensionType.MAX_VALUE.getTextValue()) != null
                && !element.getExtensionsByUrl(FHIRExtensionType.MAX_VALUE.getTextValue())
                .isEmpty()) {
                max = element.getExtensionsByUrl(FHIRExtensionType.MAX_VALUE.getTextValue()).get(0);
            }
            if (element.getExtensionsByUrl(FHIRExtensionType.MIN_VALUE.getTextValue()) != null
                && !element.getExtensionsByUrl(FHIRExtensionType.MIN_VALUE.getTextValue())
                .isEmpty()) {
                min = element.getExtensionsByUrl(FHIRExtensionType.MIN_VALUE.getTextValue()).get(0);
            }
        }

        if (max != null) {
            if (max.getValue() instanceof IntegerType intValue) {
                key = intValue.getValue().doubleValue();
            } else if (max.getValue() instanceof DecimalType decimalValue) {
                key = decimalValue.getValue().doubleValue();
            }
        }
        if (min != null) {
            if (min.getValue() instanceof IntegerType intValue) {
                value = intValue.getValue().doubleValue();
            } else if (min.getValue() instanceof DecimalType decimalValue) {
                value = decimalValue.getValue().doubleValue();
            }
        }
        return new SimpleEntry<>(key, value);
    }

    /**
     * Gets the start date (max value) and the end date (min value) specified by the elements min
     * and max value extensions.
     *
     * @param element Object containing the extensions.
     * @return {@link Entry} containing the end date as key and start date as value.
     */
    public static Entry<Date, Date> getMinAndMaxDateFromExtension(final Element element) {
        Extension max = null;
        Extension min = null;
        if (element.getExtensionsByUrl(FHIRExtensionType.MAX_VALUE.toString()) != null
            && !element.getExtensionsByUrl(FHIRExtensionType.MAX_VALUE.toString()).isEmpty()) {
            max = element.getExtensionsByUrl(FHIRExtensionType.MAX_VALUE.toString()).get(0);
        }
        if (element.getExtensionsByUrl(FHIRExtensionType.MIN_VALUE.toString()) != null
            && !element.getExtensionsByUrl(FHIRExtensionType.MIN_VALUE.toString()).isEmpty()) {
            min = element.getExtensionsByUrl(FHIRExtensionType.MIN_VALUE.toString()).get(0);
        }
        Date key = null;
        Date value = null;

        if (max != null) {
            key = ((DateType) max.getValue()).getValue();
        }

        if (min != null) {
            value = ((DateType) max.getValue()).getValue();
        }
        return new SimpleEntry<>(key, value);
    }
}
