package de.imi.mopat.dao;

import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface ExportTemplateDao extends MoPatDao<ExportTemplate> {

    /**
     * Counts all exports done yesterday of a given {@link ExportTemplateType}. The method must be
     * carried out after 0:00 pm because the amount is calculated for the previous day
     *
     * @param type {@link ExportTemplateType} of the counted exports.
     * @return Count of all exports done yesterday of the given {@link ExportTemplateType}.
     */
    long getExportCount(ExportTemplateType type);

    /**
     * Deletes the associated {@link de.imi.mopat.model.EncounterExportTemplate}
     * instances for the {@link ExportTemplate}.
     *
     * @param exportTemplate The export template to process
     */
    void removeEncounterExportTemplatesForExportTemplate(ExportTemplate exportTemplate);
}
