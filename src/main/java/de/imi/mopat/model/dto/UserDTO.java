package de.imi.mopat.model.dto;

import jakarta.validation.constraints.Pattern;

/**
 *
 */
public class UserDTO {

    private Long id;
    private String username;
    private String firstname;
    private String lastname;
    @Pattern(regexp = "[A-Za-z0-9.!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9"
        + ".!#$%&'*+-/=?^_`{|}~]+\\.[A-Za-z]{2,}+", message = "{global.datatype.email.notValid}")
    private String email;

    public UserDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(final String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}