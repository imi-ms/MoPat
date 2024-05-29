package de.imi.mopat.model.dto;

import de.imi.mopat.model.enumeration.ConfigurationType;

import java.util.List;

/**
 *
 */
public class ConfigurationDTO {

    private Long id;
    private String entityClass;
    private String attribute;
    private String value;
    private ConfigurationType configurationType;
    private String labelMessageCode;
    private String descriptionMessageCode;
    private String testMethod;
    private String updateMethod;
    private String pattern;
    private List<String> options;
    private ConfigurationDTO parent;
    private List<ConfigurationDTO> children;
    private ConfigurationGroupDTO configurationGroupDTO;
    private Integer position;

    public ConfigurationDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
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

    public List<ConfigurationDTO> getChildren() {
        return children;
    }

    public void setChildren(final List<ConfigurationDTO> children) {
        this.children = children;
    }

    public ConfigurationGroupDTO getConfigurationGroupDTO() {
        return configurationGroupDTO;
    }

    public void setConfigurationGroupDTO(final ConfigurationGroupDTO configurationGroupDTO) {
        this.configurationGroupDTO = configurationGroupDTO;
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

    public ConfigurationDTO getParent() {
        return parent;
    }

    public void setParent(final ConfigurationDTO parent) {
        this.parent = parent;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }
}