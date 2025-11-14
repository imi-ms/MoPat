package de.imi.mopat.io.impl;

import ca.uhn.fhir.parser.DataFormatException;
import de.imi.mopat.io.importer.fhir.FhirDstu3Helper;
import de.imi.mopat.io.ExportTemplateImporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Questionnaire;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemOptionComponent;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.exceptions.FHIRException;
import org.xml.sax.SAXException;

/**
 *
 */
public class ExportTemplateImporterFhirDstu3 implements ExportTemplateImporter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ExportTemplateImporterFhirDstu3.class);

    @Override
    public List<String> importFile(final InputStream inputStream)
        throws IOException, SAXException, ParserConfigurationException {
        List<String> mappingExportFields = new ArrayList<>();

        try {
            Questionnaire questionnaire = (Questionnaire) FhirDstu3Helper.parseResourceFromFile(
                inputStream);
            List<QuestionnaireItemComponent> items = new ArrayList<>();
            for (QuestionnaireItemComponent item : questionnaire.getItem()) {
                items.add(item);
                items.addAll(item.getItem());
            }

            for (QuestionnaireItemComponent item : items) {
                if (item.getType() != null) {
                    switch (item.getType()) {
                        case BOOLEAN:
                            mappingExportFields.add(
                                item.getLinkId().replace(".", "u002E").replace("_", "u005F")
                                    + "_true");
                            mappingExportFields.add(
                                item.getLinkId().replace(".", "u002E").replace("_", "u005F")
                                    + "_false");
                            break;
                        case OPENCHOICE:
                        case CHOICE:
                            for (QuestionnaireItemOptionComponent option : item.getOption()) {
                                try {
                                    if (option.getValue() instanceof Coding) {
                                        mappingExportFields.add(
                                            item.getLinkId().replace(".", "u002E")
                                                .replace("_", "u005F") + "_"
                                                + option.getValueCoding().getCode()
                                                .replace(".", "u002E").replace("_", "u005F"));
                                    } else if (option.getValue() instanceof StringType) {
                                        mappingExportFields.add(
                                            item.getLinkId().replace(".", "u002E")
                                                .replace("_", "u005F") + "_"
                                                + option.getValueStringType().toString()
                                                .replace(".", "u002E").replace("_", "u005F"));
                                    }
                                } catch (FHIRException e) {
                                    LOGGER.info(
                                        "ExportField could not be set, " + "following error "
                                            + "occurred: ", e.getMessage());
                                }
                            }
                            Reference reference = item.getOptions();
                            for (Resource resource : questionnaire.getContained()) {
                                if (resource.getId().equals(reference.getReference())) {
                                    item.setOptionsTarget((ValueSet) resource);
                                }
                            }
                            ValueSet valueSet = item.getOptionsTarget();
                            if (valueSet != null && valueSet.getCompose() != null) {
                                for (ValueSet.ConceptSetComponent conceptComponent : valueSet.getCompose()
                                    .getInclude()) {
                                    for (ValueSet.ConceptReferenceComponent conceptReference : conceptComponent.getConcept()) {
                                        mappingExportFields.add(
                                            item.getLinkId().replace(".", "u002E")
                                                .replace("_", "u005F") + "_"
                                                + conceptReference.getCode().replace(".", "u002E")
                                                .replace("_", "u005F"));
                                    }
                                }
                            }

                            if (item.getType() == QuestionnaireItemType.OPENCHOICE) {
                                mappingExportFields.add(
                                    item.getLinkId().replace(".", "u002E").replace("_", "u005F")
                                        + "/other_freetext");
                            }
                            break;
                        case DATE:
                        case DECIMAL:
                        case INTEGER:
                        case STRING:
                        case TEXT:
                            mappingExportFields.add(
                                item.getLinkId().replace(".", "u002E").replace("_", "u005F"));
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (DataFormatException e) {
            LOGGER.error("Error occured while importing FHIR ExportTemplate: ", e);
        }

        return mappingExportFields;
    }
}
