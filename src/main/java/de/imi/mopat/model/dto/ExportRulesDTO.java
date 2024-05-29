package de.imi.mopat.model.dto;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class ExportRulesDTO {

    private Long exportTemplateId = null;
    private List<ExportRuleDTO> exportRules;
    private Map<Long, ExportRuleFormatDTO> exportRuleFormats;
    private Map<Long, ExportRuleFormatDTO> exportRuleScoreFormats;

    public Map<Long, ExportRuleFormatDTO> getExportRuleScoreFormats() {
        return exportRuleScoreFormats;
    }

    public void setExportRuleScoreFormats(
        final Map<Long, ExportRuleFormatDTO> exportRuleScoreFormats) {
        this.exportRuleScoreFormats = exportRuleScoreFormats;
    }

    /**
     * Sets a list of {@link ExportRuleDTO ExportRuleDTO} objects.
     *
     * @param exportRules list of {@link ExportRuleDTO ExportRuleDTO} objects
     */
    public void setExportRules(final List<ExportRuleDTO> exportRules) {
        this.exportRules = exportRules;
    }

    /**
     * Returns a list of {@link ExportRuleDTO ExportRuleDTO} objects.
     *
     * @return a list of {@link ExportRuleDTO ExportRuleDTO} objects
     */
    public List<ExportRuleDTO> getExportRules() {
        return this.exportRules;
    }

    /**
     * @return the exportTemplateId
     */
    public Long getExportTemplateId() {
        return exportTemplateId;
    }

    /**
     * @param exportTemplateId the exportTemplateId to set
     */
    public void setExportTemplateId(final Long exportTemplateId) {
        this.exportTemplateId = exportTemplateId;
    }

    /**
     * @return the exportRuleFormats
     */
    public Map<Long, ExportRuleFormatDTO> getExportRuleFormats() {
        return exportRuleFormats;
    }

    /**
     * @param exportRuleFormats the exportRuleFormats to set
     */
    public void setExportRuleFormats(final Map<Long, ExportRuleFormatDTO> exportRuleFormats) {
        this.exportRuleFormats = exportRuleFormats;
    }
}