package de.imi.mopat.model.user;

import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.dto.InvitationDTO;
import de.imi.mopat.model.dto.InvitationUserDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;

/**
 * The database table model for <i>invitation</i>. The invitation table holds the invitations sent
 * to people to join MoPat. This holds information about people which received an invitation to
 * become a {@link User} of MoPat and how long their invitation is valid.
 */
@Entity
@Table(name = "invitation")
public class Invitation implements Serializable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ApplicationMailer.class);
    @NotNull
    @Column(name = "uuid")
    private final String uuid = UUIDGenerator.createUUID();
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    // [bt] @NotNull annotation wanted, but not possible due to Spring
    // behaviour (creation of empty class and injecting values after @valid
    // test)
    @Size(min = 1, max = 255, message = "{invitation.firstName.size}")
    @Column(name = "firstname")
    private String firstName;
    // [bt] @NotNull annotation wanted, but not possible due to Spring
    // behaviour (creation of empty class and injecting values after @valid
    // test)
    @Size(min = 1, max = 255, message = "{invitation.lastName.size}")
    @Column(name = "lastname")
    private String lastName;
    // [bt] @NotNull annotation wanted, but not possible due to Spring
    // behaviour (creation of empty class and injecting values after @valid
    // test)
    @Size(min = 1, max = 255, message = "{invitation.email.size}")
    @Pattern(regexp = "[A-Za-z0-9.!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9"
        + ".!#$%&'*+-/=?^_`{|}~]+\\.[A-Za-z]{2,}+", message = "{global.datatype.email.notValid}")
    @Column(name = "email")
    private String email;
    // [bt] @NotNull annotation wanted, but not possible due to Spring
    // behaviour (creation of empty class and injecting values after @valid
    // test)
    @Size(min = 1, max = 255, message = "{invitation.role.size}")
    @Column(name = "role")
    private String role;
    @NotNull(message = "{invitation.expirationDate.notNull}")
    @Column(name = "expirationDate")
    @Temporal(TemporalType.DATE)
    private Date expirationDate;
    @Column(name = "personal_text", columnDefinition = "TEXT")
    private String personalText;
    @NotNull(message = "{invitation.locale.notNull}")
    @Column(name = "locale")
    private String locale;
    @JoinColumn(name = "owner", referencedColumnName = "id")
    @ManyToOne
    private User owner;
    @ManyToMany
    @JoinTable(name = "invitation_acl_object_identity", joinColumns = {
        @JoinColumn(name = "invitation_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "acl_object_identity_id", referencedColumnName = "id")})
    private Set<AclObjectIdentity> assignedClinics = new HashSet<>();

    public Invitation() {
        // [bt] each Invitation shall have an expiration date 7 days in the
        // future from its instantiation.
        this.refreshExpirationDate();
    }

    public Invitation(final String email, final User owner) {
        this();
        this.email = email;
        this.owner = owner;
    }

    /**
     * Returns the id of <code>this</code>.
     *
     * @return The current id of this invitation object. Might be
     * <code>null</code> for newly created objects. Is not <code> &lt;=0
     * </code>.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the uuid of <code>this</code>.
     *
     * @return The current uuid of this invitation object. Is never
     * <code>null</code>.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @return The first name of the MoPat user to invite. Is never
     * <code>null</code>. Has at least <code>1</code> and at most
     * <code>255</code> characters.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * See {@link Invitation#getFirstName()} for a description.
     *
     * @param firstName The first name of the MoPat user to invite. Must not be
     *                  <code>null</code>, must have at least <code>1</code>
     *                  and at max
     *                  <code>255</code> characters (after trimming). Will be
     *                  trimmed.
     */
    public void setFirstName(final String firstName) {
        assert firstName != null : "The given first name was null";
        assert
            firstName.trim().length() >= 1 :
            "The given first name had < 1 characters (after " + "trimming)";
        assert
            firstName.trim().length() <= 255 :
            "The given first name had > 255 characters (after " + "trimming)";
        this.firstName = firstName.trim();
    }

    /**
     * @return The last name of the MoPat user to invite. Is never
     * <code>null</code>. Has at least <code>1</code> and at most
     * <code>255</code> characters.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * See {@link Invitation#getLastName()} for a description.
     *
     * @param lastName The last name of the MoPat user to invite. Must not be
     *                 <code>null</code>, must have at least <code>1</code>
     *                 and at max
     *                 <code>255</code> characters (after trimming). Will be
     *                 trimmed.
     */
    public void setLastName(final String lastName) {
        assert lastName != null : "The given last name was null";
        assert
            lastName.trim().length() >= 1 :
            "The given last name had < 1 characters (after " + "trimming)";
        assert
            lastName.trim().length() <= 255 :
            "The given last name had >255 characters (after " + "trimming)";
        this.lastName = lastName.trim();
    }

    /**
     * @return The email address of the MoPat user to invite. Is never
     * <code>null</code>. Always represents a correct (but not necessarily
     * existent!) email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * See {@link Invitation#getEmail()} for a description.
     *
     * @param email The email address to sent the invitation to. Must not be
     *              <code>null</code>. Must follow the common pattern for
     *              email addresses:
     *              <ol> <li>any number &gt;= 1 of literals, common
     *              symbols/punctuation
     *              characters and digits (except special characters)</li>
     *              <li>exactly one
     *              '@'</li> <li>any number &gt;= 1 of literals, common
     *              symbols/punctuation
     *              characters and digits (except special characters)</li>
     *              <li>a '.'</li>
     *              <li>at least two characters as top level domain</li>
     *              </ol> * Example:
     *              <code>"foo<b>@</b>bar.com"</code>. Will be trimmed.
     */
    public void setEmail(final String email) {
        assert email != null : "The given eMail String was null";
        // [hd] Secret origin of the eMail regExp: http://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/

        assert email.trim().matches(
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\"
                + ".[A-Za-z]{2,})$") :
            "The given eMail " + "String didn't machted the eMail pattern";
        this.email = email.trim();
    }

    /**
     * @return The role the invited user will own. Is never code
     * <code>null</code>.
     */
    public String getRole() {
        return role;
    }

    /**
     * See {@link Invitation#getRole()} for a description.
     *
     * @param role The role to give to the invited user. Must not be
     *             <code>null</code>. Will be trimmed.
     */
    public void setRole(final String role) {
        assert role != null : "The given parameter was null";
        this.role = role.trim();
    }

    /**
     * Returns the expiration date for this invitation object. It states the date when this
     * invitation object will be deleted.
     *
     * @return the expiration date for this invitation object. Is never
     * <code>null</code>.
     */
    public Date getExpirationDate() {
        return expirationDate;
    }

    /**
     * See {@link Invitation#getExpirationDate()} for a description.
     *
     * @param expirationDate The new expiration date for this invitation.
     */
    public void setExpirationDate(final Date expirationDate) {
        assert expirationDate != null : "The given parameter was null";
        this.expirationDate = expirationDate;
    }

    /**
     * Refreshes the invitation's expiration date. Thus, it will be set on today plus 7 days.
     */
    public void refreshExpirationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 7);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        this.setExpirationDate(calendar.getTime());
    }

    /**
     * Returns the {@link User} object who created this invitation.
     *
     * @return The {@link User} object who created this invitation. Is never
     * <code>null</code>.
     */
    public User getOwner() {
        return owner;
    }

    /**
     * See {@link Invitation#getOwner()} for a description.
     *
     * @param owner The {@link User} who created this invitation object. Must not be
     *              <code>null</code>.
     */
    public void setOwner(final User owner) {
        assert owner != null : "The given parameter was null";
        this.owner = owner;
    }

    public String getPersonalText() {
        return personalText;
    }

    public void setPersonalText(final String personalText) {
        assert personalText != null : "The given parameter was null";
        this.personalText = personalText;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        assert locale != null : "The given parameter was null";
        this.locale = locale;
    }

    /**
     * Returns a unmodifiable set of all assigned {@link de.imi.mopat.model.Clinic} ids.
     *
     * @return Returns a unmodifiable set of all assigned {@link de.imi.mopat.model.Clinic} ids.
     */
    public Set<AclObjectIdentity> getAssignedClinics() {
        return Collections.unmodifiableSet(assignedClinics);
    }

    /**
     * Sets all assigned {@link de.imi.mopat.model.Clinic} ids for the current {@link Invitation}
     * object.
     *
     * @param assignedClinics Set of {@link de.imi.mopat.model.Clinic} ids.
     */
    public void setAssignedClinics(Set<AclObjectIdentity> assignedClinics) {
        assert assignedClinics != null : "The given parameter was null";
        this.assignedClinics = assignedClinics;
    }

    /**
     * Removes a clinic from the current {@link Invitation} object.
     *
     * @param aclClinic The {@link AclObjectIdentity} object of a {@link de.imi.mopat.model.Clinic}.
     *                  Must not be
     *                  <code>null</code>.
     */
    public void removeClinic(final AclObjectIdentity aclClinic) {
        assert aclClinic != null : "The given aclClinic was null";
        this.assignedClinics.remove(aclClinic);
    }

    /**
     * Checks if the current {@link Invitation} is active or not.
     *
     * @return True if the {@link Invitation} is active, otherwise false.
     */
    public boolean isActive() {
        return getExpirationDate().getTime() > new Date().getTime();
    }

    /**
     * @return A hashCode based on <code>this</code>' UUID.
     */
    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    /**
     * @param obj can be <code>null</code>.
     * @return <code>false</code> if the given parameter is either
     * <code>null</code> or not an <code>instanceof</code> {@link Invitation}.
     * <code>true</code> if the parameter's UUID is equal to <code>this</code>'
     * UUID.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Invitation other)) {
            return false;
        }
        return getUUID().equals(other.getUUID());
    }

    private String getUUID() {
        return this.uuid;
    }

    /**
     * Converts {@link Invitation} object to an {@link InvitationDTO} object.
     *
     * @return {@link InvitationDTO} object.
     */
    public InvitationDTO toInvitationDTO() {
        InvitationDTO invitationDTO = new InvitationDTO();

        invitationDTO.setId(this.id);
        InvitationUserDTO invitationUserDTO = new InvitationUserDTO();
        invitationUserDTO.setEmail(this.email);
        invitationUserDTO.setFirstName(this.firstName);
        invitationUserDTO.setLastName(this.lastName);
        invitationDTO.getInvitationUsers().add(invitationUserDTO);
        invitationDTO.setLocale(this.locale);
        invitationDTO.setPersonalText(this.personalText);
        invitationDTO.setRole(this.getRole());
        return invitationDTO;
    }

    /**
     * Sends an invitation mail with a registration link.
     *
     * @param applicationMailer {@link ApplicationMailer} to send mail with created content.
     * @param messageSource     {@link MessageSource} to get the appropriate messages.
     * @param baseUrl           String to get the root URL of the server.
     * @return True if the mail has been sent, false otherwise.
     */
    public Boolean sendMail(final ApplicationMailer applicationMailer,
        final MessageSource messageSource, final String baseUrl) {
        Locale locale = LocaleHelper.getLocaleFromString(this.getLocale());

        // Create mail content
        String subject = messageSource.getMessage("mail.invitation.subject", new Object[]{},
            locale);
        String footerEmail = applicationMailer.getMailFooterEMail();
        String footerPhone = applicationMailer.getMailFooterPhone();
        String footer = messageSource.getMessage("mail.invitation.footer",
            new Object[]{footerEmail, footerPhone}, locale);

        String personalMessage = ".";
        if (this.getPersonalText() != null && !this.getPersonalText().trim().isEmpty()) {
            personalMessage = " " + messageSource.getMessage("mail.invitation.personal",
                new Object[]{this.getPersonalText()}, locale);
        }
        String activationLink =
            baseUrl + "/mobile/user/register?hash=" + this.getUuid() + "&lang=" + locale;
        String content = messageSource.getMessage("mail.invitation.content",
            new Object[]{personalMessage, activationLink}, locale);

        try {
            applicationMailer.sendMail(this.getEmail(), null, subject, content + footer, null);
        } catch (Exception e) {
            LOGGER.debug("Die E-Mail konnte nicht gesendet werden.");
            return false;
        }
        return true;
    }
}
