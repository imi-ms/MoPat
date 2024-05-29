package de.imi.mopat.model.dto;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ConfigurationGroupDTO {

    private Long id;
    private Long referringId;
    private Integer position;
    private String labelMessageCode;
    private String name;
    private boolean repeating;
    private boolean deletable;
    private List<ConfigurationDTO> configurationDTOs = new ArrayList<>();

    public ConfigurationGroupDTO() {
    }

    public ConfigurationGroupDTO(final Long id, final String labelMessageCode,
        final boolean repeating, final List<ConfigurationDTO> configurationDTOs) {
        this.configurationDTOs = configurationDTOs;
        this.id = id;
        this.labelMessageCode = labelMessageCode;
        this.repeating = repeating;
        this.configurationDTOs = configurationDTOs;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getReferringId() {
        return referringId;
    }

    public void setReferringId(final Long referringId) {
        this.referringId = referringId;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    public String getLabelMessageCode() {
        return labelMessageCode;
    }

    public void setLabelMessageCode(final String labelMessageCode) {
        this.labelMessageCode = labelMessageCode;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(final boolean repeating) {
        this.repeating = repeating;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(final boolean deletable) {
        this.deletable = deletable;
    }

    public List<ConfigurationDTO> getConfigurationDTOs() {
        return configurationDTOs;
    }

    public void setConfigurationDTOs(final List<ConfigurationDTO> configurationDTOs) {
        this.configurationDTOs = configurationDTOs;
    }
}
