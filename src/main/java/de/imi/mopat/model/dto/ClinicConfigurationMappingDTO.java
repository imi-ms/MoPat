package de.imi.mopat.model.dto;

import de.imi.mopat.model.enumeration.ConfigurationType;

import java.util.List;

/**
 *
 */
public class ClinicConfigurationMappingDTO {

    private Long id;
    private ClinicConfigurationDTO clinicConfigurationDTO;
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

    public ClinicConfigurationDTO getClinicConfigurationDTO() {
        return clinicConfigurationDTO;
    }

    public void setClinicConfigurationDTO(final ClinicConfigurationDTO clinicConfigurationDTO) {
        this.clinicConfigurationDTO = clinicConfigurationDTO;
    }
}