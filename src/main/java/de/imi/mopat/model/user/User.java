package de.imi.mopat.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;

import java.io.Serializable;
import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlTransient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The database table model for table acl_sid.
 */
@Entity
@Table(name = "acl_sid")
public class User implements Serializable, UserDetails {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private Collection<Invitation> invitationCollection;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "uuid")
    private final String uuid = UUIDGenerator.createUUID();
    @Column(name = "sid")
    @NotNull(message = "{user.username.notNull}")
    @NotEmpty(message = "{user.username.notEmpty}")
    private String username;
    @NotNull(message = "{user.firstname.notNull}")
    @Size(min = 3, max = 255, message = "{user.firstname.size}")
    @Column(name = "firstname")
    private String firstname;
    @NotNull(message = "{user.lastname.notNull}")
    @Size(min = 3, max = 255, message = "{user.lastname.size}")
    @Column(name = "lastname")
    private String lastname;
    @NotNull(message = "{user.email.notNull}")
    @Size(min = 3, max = 255, message = "{user.email.size}")
    @Pattern(regexp = "[A-Za-z0-9.!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9"
        + ".!#$%&'*+-/=?^_`{|}~]+\\.[A-Za-z]{2,}+", message = "{global.datatype.email.notValid}")
    @Column(name = "email")
    private String email;
    //    @NotNull(message = "{user.password.notNull}")
//    @NotEmpty(message = "{user.password.notEmpty}")
//    @Size(min = 8, max = 255, message = "{user.password.size}")
    @Column(name = "password")
    private String password;
    private transient String newPassword;
    private transient String oldPassword;
    private transient String passwordCheck;
    @Column(name = "salt")
    private String salt;
    @NotNull(message = "{user.principal.notNull}")
    @Column(name = "principal")
    private boolean principal;
    @Valid
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Authority> authority = new HashSet<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private final Set<AclEntry> rights = new HashSet<>();
    @Column(name = "is_enabled")
    private Boolean isEnabled = Boolean.TRUE;
    @Column(name="use_pin")
    private Boolean usePin = Boolean.FALSE;
    @Column(name="pin")
    private String pin;
    @Column(name = "last_selected_clinic_id")
    private Long lastSelectedClinicId;

    public User() {
        //default constructor (in protected state), should not be accessible
        // to anything else but the JPA implementation (here: Hibernate) and
        // the JUnit tests
    }

    /**
     * Constructor for new user object with given username and password.Uses the setters to set the attributes. See
     * setters for constraints.
     *
     * @param username The username of the new user object.
     * @param password The password of the new user object.
     */
    public User(final String username, final String password) {
        // FIXME don't you want to set the salt and passwordCheck, too?
        // FIXME Missing parameters?
        setUsername(username);
        setPassword(password);
    }

    /**
     * Returns the id of the current user object.
     *
     * @return The current id of this user object. Might be <code>null</code> for newly created objects. Is not <code>
     * &lt;=0 </code>.
     */
    public Long getId() {
        return id;
    }

    private String getUUID() {
        return this.uuid;
    }

    /**
     * from http://static.springsource.org/spring-security/site/docs/3.0.x/apidocs
     * /org/springframework/security/core/userdetails/UserDetails# getAuthorities%28%29: Returns the authorities granted
     * to the user. Cannot return <code>null</code>.
     *
     * @return the authorities, sorted by natural key (never <code>null</code>). Is unmodifiable.
     */
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        // although the Spring JavaDoc says the elements have to be sorted by
        // natural key, none of the classes implementing GrantedAuthority
        // implement Comparable. Thus, no Collection of GrantedAuthority can be
        // sorted.
        List<GrantedAuthority> auth = new ArrayList<>();
        for (Authority userrole : this.authority) {
            auth.add(new SimpleGrantedAuthority(userrole.getAuthority()));
        }
        return Collections.unmodifiableCollection(auth);
    }

    /**
     * Updates the user's role by removing all existing roles of type {@link UserRole}
     * from the authority set and adding the specified new role.
     *
     * <p>This method will remove any {@link Authority} in the authority set that can be
     * converted to a {@link UserRole}. It then adds a new {@link Authority} with the
     * specified {@link UserRole}.
     *
     * @param newRole the new role to assign to the user, replacing any existing roles of type {@link UserRole}.
     */
    public void replaceRolesWith(UserRole newRole) {
        // Remove all authorities that are of type UserRole
        authority.removeIf(auth -> {
            try {
                // Attempt to convert the authority string to a UserRole
                UserRole userRole = UserRole.fromString(auth.getAuthority());
                // If the conversion is successful, this Authority object will be removed
                return userRole != null;
            } catch (IllegalArgumentException e) {
                // If the conversion fails, it means this Authority is not a UserRole,
                // so this Authority object will not be removed.
                return false;
            }
        });
        // Add the new role to the authority set
        authority.add(new Authority(this, newRole));
    }

    /**
     * Returns all authorities of the current user object.
     *
     * @return The current authorities of this user object. Is never
     * <code>null</code>. Might be empty. Is unmodifiable.
     */
    public Set<Authority> getAuthority() {
        return Collections.unmodifiableSet(authority);
    }

    /**
     * Sets a new set of authority for this user object.
     *
     * @param authority The new set of authority of this user object. Must not be
     *                  <code>null</code>.
     */
    public void setAuthority(final Set<Authority> authority) {
        assert authority != null : "The given set was null";
        this.authority = authority;
    }

    /**
     * Adds a new authority to the corresponding set of authorities.Takes care that the {@link Authority} object refers
     * to this one, too.
     *
     * @param userRole The authority, which will be added to this user. Must not be
     *                 <code>null</code>.
     */
    public void addAuthority(final Authority userRole) {
        assert userRole != null : "The given userRole was null";

        // Add the specified user role to this set of authorities
        this.authority.add(userRole);
        if (userRole.getUser() == null || !userRole.getUser().equals(this)) {
            // Add this user to specified user role
            userRole.setUser(this);
        }
    }

    /**
     * Returns the password of the current user object.
     *
     * @return The current password of this user object. Is never
     * <code>null</code>.
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets a new password for this user object.
     *
     * @param password The new password of this user object. Must not be
     *                 <code>null</code>.
     */
    public void setPassword(final String password) {
        assert password != null : "The given passwort was null";
        // FIXME any other constraints?
        this.password = password;
    }

    /**
     * Returns the transient password check of the current user object.
     *
     * @return The current transient password check of this user object. Is never <code>null</code>.
     */
    public String getPasswordCheck() {
        return passwordCheck;
    }

    /**
     * Sets a new transient password check for this user object.
     *
     * @param passwordCheck The new transient password check of this user object. Must not be
     *                      <code>null</code>.
     */
    public void setPasswordCheck(final String passwordCheck) {
        // FIXME any other constraints about this?
        assert passwordCheck != null : "The given passwortCheck was null";
        this.passwordCheck = passwordCheck;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(final String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(final String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * Returns the username of the current user object.
     *
     * @return The current username of this user object. Is never
     * <code>null</code>.
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets a new username for this user object.
     *
     * @param username The new username of this user object. Must not be
     *                 <code>null</code>.
     */
    public void setUsername(final String username) {
        // FIXME any other constraints about this?
        // FIXME do we want a user to change her username?
        assert username != null : "The given username was null";
        this.username = username.toLowerCase();
    }

    /**
     * Returns the firstname of this user object.
     *
     * @return The firstname of this user object. Is never <code>null</code>.
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * See {@link User#getFirstname()} for a decsription.
     *
     * @param firstname The firstname for a user object. Must not be
     *                  <code>null</code> and will be trimmed.
     */
    public void setFirstname(final String firstname) {
        assert firstname != null : "The given firstname was null";
        this.firstname = firstname.trim();
    }

    /**
     * Returns the lastname of this user object.
     *
     * @return The lastname of this user object. Is never <code>null</code>.
     */
    public String getLastname() {
        return lastname;
    }

    /**
     * See {@link User#getLastname()} for a decsription.
     *
     * @param lastname The lastname for a user object. Must not be
     *                 <code>null</code> and will be trimmed.
     */
    public void setLastname(final String lastname) {
        assert lastname != null : "The given lastname was null";
        this.lastname = lastname.trim();
    }

    /**
     * Returns the e-Mail address for this User object.
     *
     * @return The e-Mail address for this User object. Is never
     * <code>null</code>.
     */
    public String getEmail() {
        return email;
    }

    /**
     * See {@link User#getEmail()} for a description.
     *
     * @param email The user's e-Mail address. Must not be <code>null</code> and will be trimmed.
     */
    public void setEmail(final String email) {
        assert email != null : "The given eMail String was null";
        assert email.matches(
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\"
                + ".[A-Za-z]{2,})$") :
            "The given eMail " + "String didn't machted the eMail pattern";
        this.email = email.trim();
    }

    /**
     * Returns the salt of the current user object.
     *
     * @return The current salt of this user object.
     */
    public String getSalt() {
        // FIXME any constraints about this?
        return salt;
    }

    /**
     * Sets a new salt for this user object.
     *
     * @param salt The new salt of this user object.
     */
    public void setSalt(final String salt) {
        // FIXME any constraints about this?
        this.salt = salt;
    }

    /**
     * Adds <code>this</code> user to the {@link AclEntry} if it is not set yet.
     *
     * @param aclEntry must not be <code>null</code>.
     */
    public void addRight(final AclEntry aclEntry) {
        assert aclEntry != null : "The given right (aclEntry) was null";

        // Add the specified right to this set of rights
        this.rights.add(aclEntry);
        if (aclEntry.getUser() == null || !aclEntry.getUser().equals(this)) {
            // Add this user to specified right
            aclEntry.setUser(this);
        }
    }

    /**
     * Checks if the account is expired.
     *
     * @return Returns true if the account is not expired.
     * @see UserDetails
     */
    @Override
    public boolean isAccountNonExpired() {
        // TODO implement me
        return true;
    }

    /**
     * Checks if the account is locked.
     *
     * @return Returns true if the account is not locked.
     * @see UserDetails
     */
    @Override
    public boolean isAccountNonLocked() {
        // TODO implement me
        return true;
    }

    /**
     * Checks if the Credentials are expired.
     *
     * @return Returns true if the credentials are not expired.
     * @see UserDetails
     */
    @Override
    public boolean isCredentialsNonExpired() {
        // TODO implement me
        return true;
    }

    /**
     * Checks if the account is enabled.
     *
     * @return Returns true if the account.
     * @see UserDetails
     */
    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setIsEnabled(final Boolean isEnabled) {
        assert isEnabled != null : "The parameter isEnabled must not be null";
        this.isEnabled = isEnabled;
    }

    public Boolean getIsEnabled() {
        return this.isEnabled;
    }

    /**
     * Checks if the account is a Ldap account
     *
     * @return Returns true if the account is of type Ldap.
     */
    public boolean isLdap() {
        return getPassword() == null || getPassword().isEmpty();
    }

    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof User other)) {
            return false;
        }
        return getUUID().equals(other.getUUID());
    }

    /**
     * Returns a representation of this user object as a string value.
     *
     * @return The string, which represents this user object.
     * @see Object
     */
    @Override
    public String toString() {
        // TODO maybe we should add some separators
        return this.getId() + this.getUsername() + this.getPassword();
    }

    public boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(final boolean principal) {
        this.principal = principal;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<Invitation> getInvitationCollection() {
        return invitationCollection;
    }

    public void setInvitationCollection(final Collection<Invitation> invitationCollection) {
        this.invitationCollection = invitationCollection;
    }

    /**
     * @return the user's rights ({@link AclEntry AclEntries}) in an
     * {@link Collections#unmodifiableSet(Set) unmodifiable Set}. Is never
     * <code>null</code>. Might be empty.
     */
    public Set<AclEntry> getRights() {
        return Collections.unmodifiableSet(this.rights);
    }

    public Boolean getUsePin() {
        return usePin;
    }

    public void setUsePin(Boolean usePin) {
        this.usePin = usePin;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }


    public Long getLastSelectedClinicId() {
        return lastSelectedClinicId;
    }

    public void setLastSelectedClinicId(Long lastSelectedClinicId) {
        this.lastSelectedClinicId = lastSelectedClinicId;
    }

}