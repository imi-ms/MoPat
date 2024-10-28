package de.imi.mopat.model.dto;

import de.imi.mopat.model.enumeration.ConfigurationType;

import java.util.List;

/**
 *
 */
public class ClinicConfigurationMappingDTO {

    private Long id;
    private Long clinicConfigurationId;
    private String entityClass;
    private String attribute;
    private ConfigurationType configurationType;
    private String labelMessageCode;
    private String descriptionMessageCode;
    private String testMethod;
    private String updateMethod;
    private String pattern;
    private List<String> options;
    private ClinicConfigurationMappingDTO parent;
    private Integer position;
    private List<ClinicConfigurationMappingDTO> children;
    private String value;

    public ClinicConfigurationMappingDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(final String entityClass) {
        this.entityClass = entityClass;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(final String attribute) {
        this.attribute = attribute;
    }

    public ConfigurationType getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(final ConfigurationType configurationType) {
        this.configurationType = configurationType;
    }

    public String getLabelMessageCode() {
        return labelMessageCode;
    }

    public void setLabelMessageCode(final String labelMessageCode) {
        this.labelMessageCode = labelMessageCode;
    }

    public String getDescriptionMessageCode() {
        return descriptionMessageCode;
    }

    public void setDescriptionMessageCode(final String descriptionMessageCode) {
        this.descriptionMessageCode = descriptionMessageCode;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(final String testMethod) {
        this.testMethod = testMethod;
    }

    public String getUpdateMethod() {
        return updateMethod;
    }

    public void setUpdateMethod(final String updateMethod) {
        this.updateMethod = updateMethod;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(final List<String> options) {
        this.options = options;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    public ClinicConfigurationMappingDTO getParent() {
        return parent;
    }

    public void setParent(final ClinicConfigurationMappingDTO parent) {
        this.parent = parent;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    public List<ClinicConfigurationMappingDTO> getChildren() {
        return children;
    }

    public void setChildren(List<ClinicConfigurationMappingDTO> children) {
        this.children = children;
    }

    public Long getClinicConfigurationId() {
        return clinicConfigurationId;
    }

    public void setClinicConfigurationId(Long clinicConfigurationId) {
        this.clinicConfigurationId = clinicConfigurationId;
    }


}