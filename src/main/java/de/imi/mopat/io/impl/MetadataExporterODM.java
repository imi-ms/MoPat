package de.imi.mopat.io.impl;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.io.MetadataExporter;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.BodyPartAnswer;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.FreetextAnswer;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.enumeration.QuestionType;
import de.unimuenster.imi.org.cdisc.odm.v132.CLDataType;
import de.unimuenster.imi.org.cdisc.odm.v132.Comparator;
import de.unimuenster.imi.org.cdisc.odm.v132.DataType;
import de.unimuenster.imi.org.cdisc.odm.v132.EventType;
import de.unimuenster.imi.org.cdisc.odm.v132.FileType;
import de.unimuenster.imi.org.cdisc.odm.v132.ODM;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionAlias;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCheckValue;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeList;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeListItem;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeListRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionDecode;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionDescription;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionGlobalVariables;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionMetaDataVersion;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionProtocol;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionProtocolName;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionQuestion;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionRangeCheck;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudy;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudyDescription;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudyEventDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudyEventRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudyName;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionTranslatedText;
import de.unimuenster.imi.org.cdisc.odm.v132.SoftOrHard;
import de.unimuenster.imi.org.cdisc.odm.v132.YesOrNo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.context.MessageSource;

/**
 * An exporter for an ODM representation of the metadata of a {@link Questionnaire}.
 * <p>
 * OIDs consist of: - FileOID: [BaseOID].[questionnaireID].[UNIX Timestamp] - StudyOID:
 * [BaseOID].[questionnaireID].[UNIX Timestamp].1 - MetadataVersionOID:
 * [BaseOID].[questionnaireID].[UNIX Timestamp].2 - StudyEventDefOID:
 * [BaseOID].[questionnaireID].[UNIX Timestamp].1.1 - FormDefOID: [BaseOID].[questionnaireID] -
 * ItemGroupDefOID: [BaseOID].[questionnaireID].1 - ItemDefOID: - number checkbox freetext:
 * [BaseOID].[questionID].1 and [BaseOID] .[questionID].2 - other questiontypes:
 * [BaseOID].[questionID] - CodeListOID: [BaseOID] .[questionID].1
 */
public class MetadataExporterODM implements MetadataExporter {

    private BigInteger orderNumber = BigInteger.ONE;
    private String configurationOID;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        MetadataExporterODM.class);

    @Override
    public byte[] export(final Questionnaire questionnaire, final MessageSource messageSource,
        final ConfigurationDao configurationDao, final ConfigurationGroupDao configurationGroupDao,
        final ExportTemplateDao exportTemplateDao, final QuestionnaireDao questionnaireDao,
        final QuestionDao questionDao, final ScoreDao scoreDao) {

        // Get current timestamp as XMLGregorianCalendar
        GregorianCalendar gregorianCalender = new GregorianCalendar();
        XMLGregorianCalendar nowXMLTimestamp = null;
        try {
            nowXMLTimestamp = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(gregorianCalender);
        } catch (DatatypeConfigurationException e) {
            LOGGER.error(
                "DatatypeConfigurationException while creating an " + "XMLGregorianCalendar.");
        }

        // Get the OID from the configuration
        configurationOID = configurationDao.getMetadataExporterODMOID();
        String fileOID =
            configurationOID + "." + questionnaire.getId() + "." + System.currentTimeMillis();

        // Create the ODM file
        ODM odmExportFile = new ODM();
        odmExportFile.setFileOID(fileOID);
        odmExportFile.setODMVersion("1.3.2");
        odmExportFile.setFileType(FileType.SNAPSHOT);
        odmExportFile.setCreationDateTime(nowXMLTimestamp);

        // Create standard study
        ODMcomplexTypeDefinitionStudy studyElement = new ODMcomplexTypeDefinitionStudy();
        studyElement.setOID(fileOID + ".1");

        // Create GlobalVariables and add to study
        ODMcomplexTypeDefinitionGlobalVariables globalVariables = new ODMcomplexTypeDefinitionGlobalVariables();
        ODMcomplexTypeDefinitionStudyName studyName = new ODMcomplexTypeDefinitionStudyName();
        studyName.setValue(questionnaire.getName());
        globalVariables.setStudyName(studyName);
        ODMcomplexTypeDefinitionStudyDescription studyDescription = new ODMcomplexTypeDefinitionStudyDescription();
        studyDescription.setValue(questionnaire.getDescription());
        globalVariables.setStudyDescription(studyDescription);
        ODMcomplexTypeDefinitionProtocolName protocolName = new ODMcomplexTypeDefinitionProtocolName();
        protocolName.setValue("Protocol");
        globalVariables.setProtocolName(protocolName);
        studyElement.setGlobalVariables(globalVariables);

        // Create standard MetaDataVersion
        ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion = new ODMcomplexTypeDefinitionMetaDataVersion();
        metaDataVersion.setOID(fileOID + ".2");
        metaDataVersion.setName("MD.1");

        // Create standard Protocol and reference the created StudyEventDef
        ODMcomplexTypeDefinitionProtocol protocol = new ODMcomplexTypeDefinitionProtocol();
        ODMcomplexTypeDefinitionStudyEventRef studyEventRef = new ODMcomplexTypeDefinitionStudyEventRef();
        studyEventRef.setStudyEventOID(fileOID + ".1.1");
        studyEventRef.setOrderNumber(BigInteger.ONE);
        studyEventRef.setMandatory(YesOrNo.NO);
        protocol.getStudyEventRef().add(studyEventRef);
        metaDataVersion.setProtocol(protocol);

        // Create standard StudyEventDef and reference the FormDef
        ODMcomplexTypeDefinitionStudyEventDef studyEventDef = new ODMcomplexTypeDefinitionStudyEventDef();
        studyEventDef.setOID(fileOID + ".1.1");
        studyEventDef.setName("SE.1");
        studyEventDef.setType(EventType.COMMON);
        studyEventDef.setRepeating(YesOrNo.NO);
        ODMcomplexTypeDefinitionFormRef formRef = new ODMcomplexTypeDefinitionFormRef();
        formRef.setFormOID(configurationOID + "." + questionnaire.getId());
        formRef.setOrderNumber(BigInteger.ONE);
        formRef.setMandatory(YesOrNo.NO);
        studyEventDef.getFormRef().add(formRef);
        metaDataVersion.getStudyEventDef().add(studyEventDef);

        // Convert the questionnaire to an ODM FormDef and reference the
        // created ItemGroup
        ODMcomplexTypeDefinitionFormDef formDef = new ODMcomplexTypeDefinitionFormDef();
        formDef.setOID(configurationOID + "." + questionnaire.getId());
        formDef.setName(questionnaire.getName());
        formDef.setRepeating(YesOrNo.NO);
        ODMcomplexTypeDefinitionTranslatedText formDefDescriptionTranslatedText = new ODMcomplexTypeDefinitionTranslatedText();
        formDefDescriptionTranslatedText.setLang("de-DE");
        formDefDescriptionTranslatedText.setValue(questionnaire.getDescription());
        ODMcomplexTypeDefinitionDescription formDefDescription = new ODMcomplexTypeDefinitionDescription();
        formDefDescription.getTranslatedText().add(formDefDescriptionTranslatedText);
        formDef.setDescription(formDefDescription);
        ODMcomplexTypeDefinitionItemGroupRef itemGroupRef = new ODMcomplexTypeDefinitionItemGroupRef();
        itemGroupRef.setItemGroupOID(configurationOID + "." + questionnaire.getId() + ".1");
        itemGroupRef.setOrderNumber(BigInteger.ONE);
        itemGroupRef.setMandatory(YesOrNo.NO);
        formDef.getItemGroupRef().add(itemGroupRef);
        metaDataVersion.getFormDef().add(formDef);

        // Create one ItemGroupDef, since MoPat does not support question groups
        ODMcomplexTypeDefinitionItemGroupDef itemGroupDef = new ODMcomplexTypeDefinitionItemGroupDef();
        itemGroupDef.setOID(configurationOID + "." + questionnaire.getId() + ".1");
        itemGroupDef.setName("IG.1");
        itemGroupDef.setRepeating(YesOrNo.NO);
        metaDataVersion.getItemGroupDef().add(itemGroupDef);

        // Convert all questions to ItemDefs and add them to the MetaDataVersion
        for (Question question : questionnaire.getQuestions()) {
            if (question.getQuestionType() != QuestionType.INFO_TEXT) {
                this.convertToItemDef(question, metaDataVersion, itemGroupDef, messageSource);
            }
        }

        // Add the metadataversion to the study and the study to the odm file
        studyElement.getMetaDataVersion().add(metaDataVersion);
        odmExportFile.getStudy().add(studyElement);

        // Get the byte array from the XML file
        ByteArrayOutputStream bos = null;
        try {
            JAXBContext contextObj = JAXBContext.newInstance(ODM.class);
            Marshaller marshallerObj = contextObj.createMarshaller();
            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            bos = new ByteArrayOutputStream();
            marshallerObj.marshal(odmExportFile, bos);
            bos.flush();
            bos.close();
            return bos.toByteArray();
        } catch (JAXBException e) {
            LOGGER.error("Error while creating an JAXBContext or a Marshaller" + ".");
        } catch (IOException e) {
            LOGGER.error("Error while creating an ByteArrayOutputStream.");
        }

        return new byte[0];
    }

    /**
     * Converts a {@link Question} to an {@link ODMcomplexTypeDefinitionItemDef ItemDef}.
     *
     * @param question        The {@link Question} to convert
     * @param metaDataVersion The {link ODMcomplexTypeDefinitionMetaDataVersion MetaDataVersion}, to
     *                        which the new {@link ODMcomplexTypeDefinitionItemDef ItemDef} should
     *                        be added.
     * @param itemGroup       The {@link ODMcomplexTypeDefinitionItemGroupDef ItemGroup}, to which
     *                        the new {@link ODMcomplexTypeDefinitionItemDef ItemDef} should be
     *                        added.
     * @param messageSource   {@link MessageSource} to get description and help texts.
     */
    private void convertToItemDef(final Question question,
        final ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion,
        final ODMcomplexTypeDefinitionItemGroupDef itemGroup, final MessageSource messageSource) {
        ODMcomplexTypeDefinitionItemDef itemDef = new ODMcomplexTypeDefinitionItemDef();

        // Set the OID of the question to the internal database ID
        itemDef.setOID(configurationOID + "." + question.getId());
        itemDef.setName("I." + question.getId());

        // Set all translated question texts
        ODMcomplexTypeDefinitionQuestion questionElement = new ODMcomplexTypeDefinitionQuestion();
        for (Map.Entry entry : question.getLocalizedQuestionText().entrySet()) {
            ODMcomplexTypeDefinitionTranslatedText translatedText = new ODMcomplexTypeDefinitionTranslatedText();
            translatedText.setLang(entry.getKey().toString().replaceAll("_", "-"));
            translatedText.setValue(entry.getValue().toString());
            questionElement.getTranslatedText().add(translatedText);
        }
        itemDef.setQuestion(questionElement);

        ODMcomplexTypeDefinitionDescription description;
        ODMcomplexTypeDefinitionTranslatedText descriptionText;
        ODMcomplexTypeDefinitionRangeCheck minRangeCheck;
        ODMcomplexTypeDefinitionRangeCheck maxRangeCheck;
        ODMcomplexTypeDefinitionCheckValue minCheckValue;
        ODMcomplexTypeDefinitionCheckValue maxCheckValue;

        // Check the questiontype of the given question
        switch (question.getQuestionType()) {
            case BODY_PART:
            case MULTIPLE_CHOICE:
            case DROP_DOWN:

                // Get and set specific data type
                itemDef.setDataType(this.getDataType(question));

                // Set the description including minimum and maximum answers
                // to give
                description = new ODMcomplexTypeDefinitionDescription();
                descriptionText = new ODMcomplexTypeDefinitionTranslatedText();
                descriptionText.setLang("en-GB");
                if (Objects.equals(question.getMinNumberAnswers(),
                    question.getMaxNumberAnswers())) {
                    descriptionText.setValue(
                        messageSource.getMessage("survey.questionnaire.ExactAnswer", new Object[0],
                                LocaleHelper.getLocaleFromString("en_GB"))
                            .replace("{min}", question.getMinNumberAnswers().toString()));
                } else if (question.getMinNumberAnswers() == 0) {
                    descriptionText.setValue(
                        messageSource.getMessage("survey.questionnaire.MaxAnswer", new Object[0],
                                LocaleHelper.getLocaleFromString("en_GB"))
                            .replace("{max}", question.getMaxNumberAnswers().toString()));
                } else {
                    descriptionText.setValue(
                        messageSource.getMessage("survey.questionnaire.MinMaxAnswer", new Object[0],
                                LocaleHelper.getLocaleFromString("en_GB"))
                            .replace("{min}", question.getMinNumberAnswers().toString())
                            .replace("{max}", question.getMaxNumberAnswers().toString()));
                }
                description.getTranslatedText().add(descriptionText);
                itemDef.setDescription(description);

                // Add the code list reference to the question
                ODMcomplexTypeDefinitionCodeListRef codeListRef = new ODMcomplexTypeDefinitionCodeListRef();
                String codeListOID = configurationOID + "." + question.getId() + ".1";
                codeListRef.setCodeListOID(codeListOID);
                itemDef.setCodeListRef(codeListRef);
                metaDataVersion.getItemDef().add(itemDef);

                // Create code list and add it to the MetaDataVersion
                this.convertToCodeList(question, codeListOID, metaDataVersion, messageSource);

                // Get the answer id of the is other answer, is null if there
                // is not such an answer
                Long isOtherAnswerId = question.getIsOtherAnswerId();

                if (isOtherAnswerId != null) {
                    ODMcomplexTypeDefinitionItemDef itemDefFreetext = new ODMcomplexTypeDefinitionItemDef();

                    // Search for freetext answer
                    Answer freetextAnswer = null;
                    for (Answer currentAnswer : question.getAnswers()) {
                        if (currentAnswer instanceof FreetextAnswer) {
                            freetextAnswer = currentAnswer;
                            break;
                        }
                    }

                    itemDefFreetext.setOID(
                        configurationOID + "." + question.getId() + "." + freetextAnswer.getId());
                    itemDefFreetext.setName("I." + question.getId() + "." + freetextAnswer.getId());
                    itemDefFreetext.setDescription(itemDef.getDescription());

                    // Set all translated question texts with freetext answer
                    // texts
                    ODMcomplexTypeDefinitionQuestion questionFreetextElement = new ODMcomplexTypeDefinitionQuestion();
                    for (Map.Entry entry : question.getLocalizedQuestionText().entrySet()) {
                        ODMcomplexTypeDefinitionTranslatedText translatedText = new ODMcomplexTypeDefinitionTranslatedText();
                        translatedText.setLang(entry.getKey().toString().replaceAll("_", "-"));
                        // Find the select answer that triggers the freetext
                        // answer
                        SelectAnswer selectAnswer = null;
                        for (Answer currentAnswer : question.getAnswers()) {
                            if (Objects.equals(currentAnswer.getId(), isOtherAnswerId)) {
                                selectAnswer = (SelectAnswer) currentAnswer;
                            }
                        }
                        translatedText.setValue(
                            entry.getValue().toString() + " " + selectAnswer.getLocalizedLabel()
                                .get(entry.getKey().toString()));
                        questionFreetextElement.getTranslatedText().add(translatedText);
                    }
                    itemDefFreetext.setQuestion(questionFreetextElement);

                    itemDefFreetext.setDataType(DataType.STRING);
                    metaDataVersion.getItemDef().add(itemDefFreetext);
                }
                break;
            case DATE:
                itemDef.setDataType(DataType.DATE);

                DateAnswer dateAnswer = (DateAnswer) question.getAnswers().get(0);

                // Set the description including minimum and maximum dates if
                // available
                if (dateAnswer.getStartDate() != null || dateAnswer.getEndDate() != null) {
                    description = new ODMcomplexTypeDefinitionDescription();
                    descriptionText = new ODMcomplexTypeDefinitionTranslatedText();
                    descriptionText.setLang("en-GB");
                    if (dateAnswer.getStartDate() != null && dateAnswer.getEndDate() != null) {
                        descriptionText.setValue(
                            messageSource.getMessage("survey.questionnaire.label.date.startEndDate",
                                    new Object[0], LocaleHelper.getLocaleFromString("en_GB"))
                                .replace("{startDate}",
                                    Constants.DATE_FORMAT.format(dateAnswer.getStartDate()))
                                .replace("{endDate}",
                                    Constants.DATE_FORMAT.format(dateAnswer.getEndDate())));
                    } else if (dateAnswer.getStartDate() != null) {
                        descriptionText.setValue(
                            messageSource.getMessage("survey.questionnaire.label.date.startDate",
                                    new Object[0], LocaleHelper.getLocaleFromString("en_GB"))
                                .replace("{startDate}",
                                    Constants.DATE_FORMAT.format(dateAnswer.getStartDate())));
                    } else {
                        descriptionText.setValue(
                            messageSource.getMessage("survey.questionnaire.label.date.endDate",
                                    new Object[0], LocaleHelper.getLocaleFromString("en_GB"))
                                .replace("{endDate}",
                                    Constants.DATE_FORMAT.format(dateAnswer.getEndDate())));
                    }
                    description.getTranslatedText().add(descriptionText);
                    itemDef.setDescription(description);
                }

                // Check if there is a minimum or a maximum date and set the
                // ranges
                if (dateAnswer.getStartDate() != null) {
                    ODMcomplexTypeDefinitionRangeCheck startRangeCheck = new ODMcomplexTypeDefinitionRangeCheck();
                    startRangeCheck.setComparator(Comparator.GE);
                    startRangeCheck.setSoftHard(SoftOrHard.SOFT);

                    ODMcomplexTypeDefinitionCheckValue startCheckValue = new ODMcomplexTypeDefinitionCheckValue();
                    startCheckValue.setValue(
                        Constants.DATE_FORMAT.format(dateAnswer.getStartDate()));

                    startRangeCheck.getCheckValue().add(startCheckValue);
                    itemDef.getRangeCheck().add(startRangeCheck);
                }

                if (dateAnswer.getEndDate() != null) {
                    ODMcomplexTypeDefinitionRangeCheck endRangeCheck = new ODMcomplexTypeDefinitionRangeCheck();
                    endRangeCheck.setComparator(Comparator.LE);
                    endRangeCheck.setSoftHard(SoftOrHard.SOFT);

                    ODMcomplexTypeDefinitionCheckValue endCheckValue = new ODMcomplexTypeDefinitionCheckValue();
                    endCheckValue.setValue(Constants.DATE_FORMAT.format(dateAnswer.getEndDate()));

                    endRangeCheck.getCheckValue().add(endCheckValue);
                    itemDef.getRangeCheck().add(endRangeCheck);
                }
                metaDataVersion.getItemDef().add(itemDef);
                break;
            case FREE_TEXT:
            case BARCODE:
                itemDef.setDataType(DataType.STRING);
                metaDataVersion.getItemDef().add(itemDef);
                break;
            case NUMBER_CHECKBOX:
            case SLIDER:
                SliderAnswer sliderAnswer = (SliderAnswer) question.getAnswers().get(0);

                // Check if only integers are possible as answers
                if (sliderAnswer.getStepsize() == Math.floor(sliderAnswer.getStepsize())
                    && sliderAnswer.getMinValue() == Math.floor(sliderAnswer.getMinValue())) {
                    itemDef.setDataType(DataType.INTEGER);
                } else {
                    itemDef.setDataType(DataType.DOUBLE);
                }

                // Translate the ranges of the slider answer to ODM range checks
                minRangeCheck = new ODMcomplexTypeDefinitionRangeCheck();
                minRangeCheck.setComparator(Comparator.GE);
                minRangeCheck.setSoftHard(SoftOrHard.SOFT);
                minCheckValue = new ODMcomplexTypeDefinitionCheckValue();
                minCheckValue.setValue(sliderAnswer.getMinValue().toString());
                minRangeCheck.getCheckValue().add(minCheckValue);
                itemDef.getRangeCheck().add(minRangeCheck);

                maxRangeCheck = new ODMcomplexTypeDefinitionRangeCheck();
                maxRangeCheck.setComparator(Comparator.LE);
                maxRangeCheck.setSoftHard(SoftOrHard.SOFT);
                maxCheckValue = new ODMcomplexTypeDefinitionCheckValue();
                maxCheckValue.setValue(sliderAnswer.getMaxValue().toString());
                maxRangeCheck.getCheckValue().add(maxCheckValue);
                itemDef.getRangeCheck().add(maxRangeCheck);

                metaDataVersion.getItemDef().add(itemDef);
                break;
            case NUMBER_INPUT:
                NumberInputAnswer numberInputAnswer = (NumberInputAnswer) question.getAnswers()
                    .get(0);

                // Check if only integers are possible
                if (numberInputAnswer.getStepsize() != null
                    && numberInputAnswer.getStepsize() == Math.floor(
                    numberInputAnswer.getStepsize()) && (numberInputAnswer.getMinValue() == null
                    || numberInputAnswer.getMinValue() == Math.floor(
                    numberInputAnswer.getMinValue()))) {
                    itemDef.setDataType(DataType.INTEGER);
                } else {
                    itemDef.setDataType(DataType.DOUBLE);
                }

                // Set the description including minimum and maximum values
                // if available
                if (numberInputAnswer.getMinValue() != null
                    || numberInputAnswer.getMaxValue() != null) {
                    description = new ODMcomplexTypeDefinitionDescription();
                    descriptionText = new ODMcomplexTypeDefinitionTranslatedText();
                    descriptionText.setLang("en-GB");
                    if (numberInputAnswer.getMinValue() != null
                        && numberInputAnswer.getMaxValue() != null) {
                        descriptionText.setValue(messageSource.getMessage(
                                "survey.questionnaire.label.numberInput.minMax", new Object[0],
                                LocaleHelper.getLocaleFromString("en_GB"))
                            .replace("{min}", numberInputAnswer.getMinValue().toString())
                            .replace("{max}", numberInputAnswer.getMaxValue().toString()));
                    } else if (numberInputAnswer.getMinValue() != null) {
                        descriptionText.setValue(
                            messageSource.getMessage("survey.questionnaire.label.numberInput.min",
                                    new Object[0], LocaleHelper.getLocaleFromString("en_GB"))
                                .replace("{min}", numberInputAnswer.getMinValue().toString()));
                    } else {
                        descriptionText.setValue(
                            messageSource.getMessage("survey.questionnaire.label.numberInput.max",
                                    new Object[0], LocaleHelper.getLocaleFromString("en_GB"))
                                .replace("{max}", numberInputAnswer.getMaxValue().toString()));
                    }
                    description.getTranslatedText().add(descriptionText);
                    itemDef.setDescription(description);
                }

                // Check if there is a minimum or a maximum value and set the
                // ranges
                if (numberInputAnswer.getMinValue() != null) {
                    minRangeCheck = new ODMcomplexTypeDefinitionRangeCheck();
                    minRangeCheck.setComparator(Comparator.GE);
                    minRangeCheck.setSoftHard(SoftOrHard.SOFT);
                    minCheckValue = new ODMcomplexTypeDefinitionCheckValue();
                    minCheckValue.setValue(numberInputAnswer.getMinValue().toString());
                    minRangeCheck.getCheckValue().add(minCheckValue);
                    itemDef.getRangeCheck().add(minRangeCheck);
                }

                if (numberInputAnswer.getMaxValue() != null) {
                    maxRangeCheck = new ODMcomplexTypeDefinitionRangeCheck();
                    maxRangeCheck.setComparator(Comparator.LE);
                    maxRangeCheck.setSoftHard(SoftOrHard.SOFT);
                    maxCheckValue = new ODMcomplexTypeDefinitionCheckValue();
                    maxCheckValue.setValue(numberInputAnswer.getMaxValue().toString());
                    maxRangeCheck.getCheckValue().add(maxCheckValue);
                    itemDef.getRangeCheck().add(maxRangeCheck);
                }

                metaDataVersion.getItemDef().add(itemDef);
                break;
            case NUMBER_CHECKBOX_TEXT:
                SliderFreetextAnswer sliderFreetextAnswer = (SliderFreetextAnswer) question.getAnswers()
                    .get(0);
                itemDef.setOID(configurationOID + "." + question.getId() + ".1");

                // Check if only integers are possible
                if (sliderFreetextAnswer.getStepsize() == Math.floor(
                    sliderFreetextAnswer.getStepsize())
                    && sliderFreetextAnswer.getMinValue() == Math.floor(
                    sliderFreetextAnswer.getMinValue())) {
                    itemDef.setDataType(DataType.INTEGER);
                } else {
                    itemDef.setDataType(DataType.DOUBLE);
                }

                // Translate the ranges of the slider answer to ODM range checks
                minRangeCheck = new ODMcomplexTypeDefinitionRangeCheck();
                minRangeCheck.setComparator(Comparator.GE);
                minRangeCheck.setSoftHard(SoftOrHard.SOFT);
                minCheckValue = new ODMcomplexTypeDefinitionCheckValue();
                minCheckValue.setValue(sliderFreetextAnswer.getMinValue().toString());
                minRangeCheck.getCheckValue().add(minCheckValue);
                itemDef.getRangeCheck().add(minRangeCheck);

                maxRangeCheck = new ODMcomplexTypeDefinitionRangeCheck();
                maxRangeCheck.setComparator(Comparator.LE);
                maxRangeCheck.setSoftHard(SoftOrHard.SOFT);
                maxCheckValue = new ODMcomplexTypeDefinitionCheckValue();
                maxCheckValue.setValue(sliderFreetextAnswer.getMaxValue().toString());
                maxRangeCheck.getCheckValue().add(maxCheckValue);
                itemDef.getRangeCheck().add(maxRangeCheck);

                metaDataVersion.getItemDef().add(itemDef);

                // Add an additional itemDef for the freetext
                ODMcomplexTypeDefinitionItemDef itemDefFreetext = new ODMcomplexTypeDefinitionItemDef();
                itemDefFreetext.setOID(configurationOID + "." + question.getId() + ".2");
                itemDefFreetext.setName("I." + question.getId() + "_freetext");
                itemDefFreetext.setDescription(itemDef.getDescription());
                itemDefFreetext.setQuestion(itemDef.getQuestion());

                itemDefFreetext.setDataType(DataType.STRING);
                metaDataVersion.getItemDef().add(itemDefFreetext);
                break;
            default:
                break;
        }
        this.convertToItemRef(question, itemGroup);
    }

    /**
     * Converts a given {@link Question} to an {@link ODMcomplexTypeDefinitionItemRef ItemRef}.
     *
     * @param question  The {@link Question} to convert
     * @param itemGroup The {@link ODMcomplexTypeDefinitionItemGroupDef ItemGroup }, to which the
     *                  new {@link ODMcomplexTypeDefinitionItemRef ItemRef} should be added.
     */
    private void convertToItemRef(final Question question,
        final ODMcomplexTypeDefinitionItemGroupDef itemGroup) {
        // Create a new itemRef and set its data
        ODMcomplexTypeDefinitionItemRef itemRef = new ODMcomplexTypeDefinitionItemRef();
        itemRef.setOrderNumber(orderNumber);
        if (question.getIsRequired()) {
            itemRef.setMandatory(YesOrNo.YES);
        } else {
            itemRef.setMandatory(YesOrNo.NO);
        }
        itemRef.setItemOID(configurationOID + "." + question.getId());

        // Increment the orderNumber for the next itemRef
        orderNumber = orderNumber.add(BigInteger.ONE);

        // Check if the question is a multiple choice question and one answer
        // is marked as other that a second itemRef for the freetext is needed
        boolean isOtherExists = false;
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            for (Answer answer : question.getAnswers()) {
                if (answer instanceof SelectAnswer && ((SelectAnswer) answer).getIsOther()) {
                    isOtherExists = true;
                    break;
                }
            }
        }

        // Create a second itemRef for the freetext of a number checkbox
        if (question.getQuestionType() == QuestionType.NUMBER_CHECKBOX_TEXT) {
            // Adjust the OID of the numbered checkbox question
            itemRef.setItemOID(configurationOID + "." + question.getId() + ".1");
            ODMcomplexTypeDefinitionItemRef itemRefFreetext = new ODMcomplexTypeDefinitionItemRef();
            itemRefFreetext.setOrderNumber(orderNumber);
            if (question.getIsRequired()) {
                itemRefFreetext.setMandatory(YesOrNo.YES);
            } else {
                itemRefFreetext.setMandatory(YesOrNo.NO);
            }
            itemRefFreetext.setItemOID(configurationOID + "." + question.getId() + ".2");
            itemGroup.getItemRef().add(itemRefFreetext);

            orderNumber = orderNumber.add(BigInteger.ONE);
        } else if (isOtherExists) {
            ODMcomplexTypeDefinitionItemRef itemRefFreetext = new ODMcomplexTypeDefinitionItemRef();
            itemRefFreetext.setOrderNumber(orderNumber);
            if (question.getIsRequired()) {
                itemRefFreetext.setMandatory(YesOrNo.YES);
            } else {
                itemRefFreetext.setMandatory(YesOrNo.NO);
            }
            // Search for freetext answer
            Answer freetextAnswer = null;
            for (Answer currentAnswer : question.getAnswers()) {
                if (currentAnswer instanceof FreetextAnswer) {
                    freetextAnswer = currentAnswer;
                    break;
                }
            }
            itemRefFreetext.setItemOID(
                configurationOID + "." + question.getId() + "." + freetextAnswer.getId());
            itemGroup.getItemRef().add(itemRefFreetext);

            orderNumber = orderNumber.add(BigInteger.ONE);
        }
        // Add the item ref to the item group
        itemGroup.getItemRef().add(itemRef);
    }

    /**
     * Converts the {@link Answer Answers} of the given {@link Question} to a {link
     * ODMcomplexTypeDefinitionCodeList CodeList}.
     *
     * @param question        {@link Question} whose {@link Answer Answers} are to be converted
     * @param codeListOID     OID of the created {@link ODMcomplexTypeDefinitionCodeList CodeList}
     * @param metaDataVersion {@link ODMcomplexTypeDefinitionMetaDataVersion MetaDataVersion} of the
     *                        created {@link ODMcomplexTypeDefinitionCodeList CodeList}
     * @return the Id of the freetext answer, -1 if there is none
     */
    private void convertToCodeList(final Question question, final String codeListOID,
        final ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion,
        final MessageSource messageSource) {
        // Create a codeList and set its data
        ODMcomplexTypeDefinitionCodeList codeList = new ODMcomplexTypeDefinitionCodeList();
        codeList.setOID(codeListOID);
        codeList.setName(codeListOID);
        CLDataType cLDataType = this.getCLDataType(question);
        codeList.setDataType(cLDataType);

        SelectAnswer selectAnswer;
        BodyPartAnswer bodyPartAnswer;
        Integer codedValueCounter = 1;
        List<String> existingCodedValues = new ArrayList<>();
        // Iterate over each answer of the given question
        for (Answer answer : question.getAnswers()) {
            ODMcomplexTypeDefinitionCodeListItem codeListItem = new ODMcomplexTypeDefinitionCodeListItem();
            if (answer instanceof SelectAnswer) {
                selectAnswer = (SelectAnswer) answer;
                // Check the value of the answer and set it
                // Make sure the coded value is unique in this question, if
                // it is already used, take the next free integer
                if (selectAnswer.getCodedValue() == null || selectAnswer.getCodedValue().isEmpty()
                    || existingCodedValues.contains(selectAnswer.getCodedValue())) {
                    while (existingCodedValues.contains(codedValueCounter.toString())) {
                        codedValueCounter++;
                    }
                    codeListItem.setCodedValue(codedValueCounter.toString());
                    existingCodedValues.add(codedValueCounter.toString());
                    codedValueCounter++;
                } else {
                    codeListItem.setCodedValue(selectAnswer.getCodedValue());
                    existingCodedValues.add(selectAnswer.getCodedValue());
                }

                codeListItem.setDecode(new ODMcomplexTypeDefinitionDecode());
                // Add the answer in every available language
                for (Map.Entry entry : selectAnswer.getLocalizedLabel().entrySet()) {
                    ODMcomplexTypeDefinitionTranslatedText translatedText = new ODMcomplexTypeDefinitionTranslatedText();
                    translatedText.setLang(entry.getKey().toString().replaceAll("_", "-"));
                    translatedText.setValue(entry.getValue().toString());
                    codeListItem.getDecode().getTranslatedText().add(translatedText);
                }
            } else if (answer instanceof BodyPartAnswer) {
                bodyPartAnswer = (BodyPartAnswer) answer;
                codeListItem.setCodedValue(bodyPartAnswer.getBodyPart().getMessageCode());
                codeListItem.setDecode(new ODMcomplexTypeDefinitionDecode());
                //Add the body part answer in every available language
                for (String locale : question.getLocalizedQuestionText().keySet()) {
                    ODMcomplexTypeDefinitionTranslatedText translatedText = new ODMcomplexTypeDefinitionTranslatedText();
                    translatedText.setLang(locale.replaceAll("_", "-"));
                    translatedText.setValue(
                        messageSource.getMessage(bodyPartAnswer.getBodyPart().getMessageCode(),
                            new String[]{}, LocaleHelper.getLocaleFromString(locale)));
                    codeListItem.getDecode().getTranslatedText().add(translatedText);
                }
            }
            if (answer instanceof SelectAnswer || answer instanceof BodyPartAnswer) {
                // Add Alias with MoPat answer ID to memorize original answer
                ODMcomplexTypeDefinitionAlias answerAlias = new ODMcomplexTypeDefinitionAlias();
                answerAlias.setContext("MoPatAnswerID");
                answerAlias.setName(answer.getId().toString());
                codeListItem.getAlias().add(answerAlias);
                codeList.getCodeListItem().add(codeListItem);
            }
        }

        metaDataVersion.getCodeList().add(codeList);
    }

    /**
     * Returns the {@link DataType} of the given {@link Question}.
     *
     * @param question The {@link Question} whose {@link DataType} should be returned.
     * @return The {@link DataType} of the given {@link Question}.
     */
    private DataType getDataType(final Question question) {
        // Iterate over each answer and check whether its value is null or a
        // floating number
        for (Answer answer : question.getAnswers()) {
            if (answer instanceof SelectAnswer selectAnswer) {
                if (selectAnswer.getValue() == null) {
                    return DataType.STRING;
                } else if (selectAnswer.getValue() != Math.floor(selectAnswer.getValue())
                    || Double.isInfinite(selectAnswer.getValue())) {
                    return DataType.FLOAT;
                }
            } else if (answer instanceof BodyPartAnswer) {
                return DataType.STRING;
            }
        }
        return DataType.INTEGER;
    }

    /**
     * Returns the {@link CLDataType CodeListDataType} of the given {@link Question}.
     *
     * @param question The {@link Question}, whose {@link CLDataType} should be returned.
     * @return The {@link CLDataType} of the given {@link Question}.
     */
    private CLDataType getCLDataType(final Question question) {
        if (question.getCodedValueType() == null) {
            for (Answer answer : question.getAnswers()) {
                if (answer instanceof BodyPartAnswer) {
                    return CLDataType.STRING;
                }
            }
            return CLDataType.INTEGER;
        }

        return switch (question.getCodedValueType()) {
            case STRING -> CLDataType.STRING;
            case INTEGER -> CLDataType.INTEGER;
            case FLOAT -> CLDataType.FLOAT;
            default -> CLDataType.STRING;
        };
    }
}
