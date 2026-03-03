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
    private String configurationGroupLabelCode;

    private SortedMap<Long, JsonExportRuleDTO> exportRuleDTOs = new TreeMap<>();

    private byte[] fileByteArrayEncoded;

    /**
     * @return the identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the identifier.
     *
     * @param id identifier to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the UUID
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the UUID.
     *
     * @param uuid UUID to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the export template type
     */
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

    /**
     * Sets the filename.
     *
     * @param filename filename to set (must not be {@code null})
     * @throws AssertionError if assertions are enabled and {@code filename} is {@code null}
     */
    public void setFilename(final String filename) {
        assert filename != null : "the filename was null";
        this.filename = filename;
    }

    /**
     * @return the original filename (as provided on upload/import)
     */
    public String getOriginalFilename() {
        return originalFilename;
    }

    /**
     * Sets the original filename (as provided on upload/import).
     *
     * @param originalFilename original filename to set (must not be {@code null})
     * @throws AssertionError if assertions are enabled and {@code originalFilename} is
     *                        {@code null}
     */
    public void setOriginalFilename(final String originalFilename) {
        assert originalFilename != null : "The originalFilename was null";
        this.originalFilename = originalFilename;
    }

    /**
     * Adds an export rule DTO to this template DTO under the given ID key.
     *
     * @param id                key to store the rule under
     * @param jsonExportRuleDTO rule DTO to add
     */
    public void addExportRuleDTOs(long id, JsonExportRuleDTO jsonExportRuleDTO) {
        this.exportRuleDTOs.put(id, jsonExportRuleDTO);
    }

    /**
     * @return the export rule DTOs mapped by their key (sorted)
     */
    public SortedMap<Long, JsonExportRuleDTO> getExportRuleDTOs() {
        return exportRuleDTOs;
    }

    /**
     * Sets the export rule DTO map.
     *
     * @param exportRuleDTOs export rules mapped by key (sorted)
     */
    public void setExportRuleDTOs(SortedMap<Long, JsonExportRuleDTO> exportRuleDTOs) {
        this.exportRuleDTOs = exportRuleDTOs;
    }

    /**
     * @return the configuration group label message code
     */
    public String getConfigurationGroupLabelCode() {
        return configurationGroupLabelCode;
    }

    /**
     * Sets the configuration group label message code.
     *
     * @param configurationGroupLabelCode message code to set
     */
    public void setConfigurationGroupLabelCode(String configurationGroupLabelCode) {
        this.configurationGroupLabelCode = configurationGroupLabelCode;
    }

    /**
     * @return the Base64-encoded file content as a byte array
     */
    public byte[] getFileByteArrayEncoded() {
        return fileByteArrayEncoded;
    }

    /**
     * Sets the Base64-encoded file content.
     *
     * @param fileByteArrayEncoded Base64-encoded file content as a byte array
     */
    public void setFileByteArrayEncoded(byte[] fileByteArrayEncoded) {
        this.fileByteArrayEncoded = fileByteArrayEncoded;
    }
}
