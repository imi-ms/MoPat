package de.imi.mopat.model.dto;

/**
 *
 */
public class InvitationUserDTO {

    private String firstName = null;
    private String lastName = null;
    private String email = null;

    public InvitationUserDTO() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
