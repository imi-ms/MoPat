package de.imi.mopat.model.dto;

import jakarta.validation.constraints.NotNull;

/**
 *
 */
public class BundleClinicDTO {

    @NotNull(message = "{bundleClinic.position.notNull}")
    private Integer position;
    private ClinicDTO clinicDTO;
    private BundleDTO bundleDTO;

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public ClinicDTO getClinicDTO() {
        return clinicDTO;
    }

    public void setClinicDTO(ClinicDTO clinicDTO) {
        this.clinicDTO = clinicDTO;
    }

    public BundleDTO getBundleDTO() {
        return bundleDTO;
    }

    public void setBundleDTO(BundleDTO bundleDTO) {
        this.bundleDTO = bundleDTO;
    }
}