package de.imi.mopat.model.dto;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 *
 */
public class InvitationDTO {

    private Long id = null;
    private String uuid = null;

    private List<InvitationUserDTO> invitationUsers = new ArrayList<>();

    private String role = null;
    private String personalText = null;
    @NotNull(message = "{invitation.locale.notNull}")
    @Size(min = 1, message = "{invitation.locale.notNull}")
    private String locale = null;
    private List<ClinicDTO> assignedClinics = new ArrayList<>();

    public InvitationDTO() {

    }

    public List<ClinicDTO> getAssignedClinics() {
        return assignedClinics;
    }

    public void setAssignedClinics(final List<ClinicDTO> assignedClinics) {
        this.assignedClinics = assignedClinics;
    }

    public String getUuid() {
        return uuid;
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getPersonalText() {
        return personalText;
    }

    public String getLocale() {
        return locale;
    }

    public List<InvitationUserDTO> getInvitationUsers() {
        return invitationUsers;
    }

    public void setInvitationUsers(final List<InvitationUserDTO> invitationUsers) {
        this.invitationUsers = invitationUsers;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public void setPersonalText(final String personalText) {
        this.personalText = personalText;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }
}