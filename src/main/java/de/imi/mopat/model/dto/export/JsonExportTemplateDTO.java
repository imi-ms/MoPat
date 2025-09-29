package de.imi.mopat.model.dto.export;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class represents the data transfer object of model {@link ExportTemplate} to convert a model
 * to json for import and export.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("export")
public class JsonExportTemplateDTO {

    private Long id;
    private String uuid = UUIDGenerator.createUUID();
    private String name;
    private ExportTemplateType exportTemplateType;

    private String filename;
    private String originalFilename;
//    private ConfigurationGroup configurationGroup;
//    private Questionnaire questionnaire;

    private SortedMap<Long, JsonExportRuleAnswerDTO> exportRuleDTOs = new TreeMap<>();
//    private Set<JsonExportRuleDTO> exportRuleDTOs = new HashSet<>();
//
//    private Set<BundleQuestionnaire> bundleQuestionnaires = new HashSet<>();
//
//    private Set<EncounterExportTemplate> encounterExportTemplates = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }


    public void setName(final String name) {
        this.name = name;
    }

    public ExportTemplateType getExportTemplateType() {
        return this.exportTemplateType;
    }

    /**
     * Sets the {@link ExportTemplateType} object for this export template object.
     *
     * @param exportTemplateType The new {@link ExportTemplateType} object to be set.
     */
    public void setExportTemplateType(final ExportTemplateType exportTemplateType) {
        assert exportTemplateType != null : "The ExportTemplateType was null";
        this.exportTemplateType = exportTemplateType;
    }

    /**
     * Returns the filename on disk of this export template. Under this name the template file is
     * found in the object storage.
     *
     * @return The filename on disk. Can not be <code>null</code>.
     */
    public String getFilename() {
        return filename;
    }


    public void setFilename(final String filename) {
        assert filename != null : "the filename was null";
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(final String originalFilename) {
        assert originalFilename != null : "The originalFilename was null";
        this.originalFilename = originalFilename;
    }

    public void addExportRuleDTOs(long id, JsonExportRuleAnswerDTO jsonExportRuleDTO) {
        this.exportRuleDTOs.put(
            id,
            jsonExportRuleDTO);

    }

    public SortedMap<Long, JsonExportRuleAnswerDTO> getExportRuleDTOs() {
        return exportRuleDTOs;
    }

    public void setExportRuleDTOs(
        SortedMap<Long, JsonExportRuleAnswerDTO> exportRuleDTOs) {
        this.exportRuleDTOs = exportRuleDTOs;
    }
}
