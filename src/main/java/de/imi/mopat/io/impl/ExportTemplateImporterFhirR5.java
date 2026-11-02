package de.imi.mopat.io.impl;

import ca.uhn.fhir.parser.DataFormatException;
import de.imi.mopat.io.ExportTemplateImporter;
import de.imi.mopat.io.importer.fhir.FhirR5Helper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Questionnaire;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireAnswerConstraint;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemAnswerOptionComponent;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r5.model.Questionnaire.QuestionnaireItemType;
import org.hl7.fhir.r5.model.StringType;
import org.xml.sax.SAXException;

/**
 *
 */
public class ExportTemplateImporterFhirR5 implements ExportTemplateImporter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ExportTemplateImporterFhirR5.class);

    private static final FhirR5Helper fhirR5Helper = new FhirR5Helper();

    @Override
    public List<String> importFile(final InputStream inputStream)
        throws IOException, SAXException, ParserConfigurationException {
        List<String> mappingExportFields = new ArrayList<>();

        try {
            Questionnaire questionnaire = (Questionnaire) FhirR5Helper.parseResourceFromFile(
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
                        case CODING:
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

                            if (item.getAnswerConstraint() == QuestionnaireAnswerConstraint.OPTIONSORSTRING) {
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
