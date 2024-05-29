package de.imi.mopat.model.dto;

import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 *
 */
public class ClinicDTO {

    private Long id = null;
    @NotNull(message = "{clinic.name.notNull}")
    @Size(min = 3, max = 255, message = "{clinic.name.size}")
    private String name;
    @NotNull(message = "{clinic.description.notNull}")
    @Size(min = 1, message = "{clinic.description.notNull}")
    private String description;
    @Pattern(regexp = "^$|[A-Za-z0-9.!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9"
        + ".!#$%&'*+-/=?^_`{|}~]+\\.[A-Za-z]{2,}+", message = "{global.datatype.email.notValid}")
    private String email;
    private List<BundleClinicDTO> bundleClinicDTOs;
    private List<UserDTO> assignedUserDTOs;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public List<UserDTO> getAssignedUserDTOs() {
        return assignedUserDTOs;
    }

    public void setAssignedUserDTOs(final List<UserDTO> assignedUsers) {
        this.assignedUserDTOs = assignedUsers;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<BundleClinicDTO> getBundleClinicDTOs() {
        return bundleClinicDTOs;
    }

    public void setBundleClinicDTOs(final List<BundleClinicDTO> bundleClinicDTOs) {
        this.bundleClinicDTOs = bundleClinicDTOs;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }
        return this.getId().hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        ClinicDTO compareClinicDTO = null;
        if (object instanceof ClinicDTO) {
            compareClinicDTO = (ClinicDTO) object;
        }
        if (compareClinicDTO != null && compareClinicDTO.getId() != null && this.getId() != null) {
            return this.getId().equals(compareClinicDTO.getId());
        } else {
            return false;
        }
    }
}