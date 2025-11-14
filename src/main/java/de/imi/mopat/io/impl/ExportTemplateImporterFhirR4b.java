package de.imi.mopat.io.impl;

import ca.uhn.fhir.parser.DataFormatException;
import de.imi.mopat.io.ExportTemplateImporter;
import de.imi.mopat.io.importer.fhir.FhirR4bHelper;
import org.hl7.fhir.r4b.model.*;
import org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemAnswerOptionComponent;
import org.hl7.fhir.r4b.model.Questionnaire.QuestionnaireItemType;
import org.hl7.fhir.exceptions.FHIRException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ExportTemplateImporterFhirR4b implements ExportTemplateImporter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ExportTemplateImporterFhirR4b.class);

    @Override
    public List<String> importFile(final InputStream inputStream)
        throws IOException, SAXException, ParserConfigurationException {
        List<String> mappingExportFields = new ArrayList<>();

        try {
            Questionnaire questionnaire = (Questionnaire) FhirR4bHelper.parseResourceFromFile(
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
                            for (QuestionnaireItemAnswerOptionComponent option : item.getAnswerOption()) {
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
                            //TODO: Value Sets are not stored here anymore


                            /*
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
                            */

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
