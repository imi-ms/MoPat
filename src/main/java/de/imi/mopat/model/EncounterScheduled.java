package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.model.enumeration.EncounterScheduledMailStatus;
import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;

/**
 * The database table model for table <i>encounter_scheduled</i>. The EncounterScheduled represents
 * a set of scheduled encounter, which will be done in the future.
 */
@Entity
@Table(name = "encounter_scheduled")
public class EncounterScheduled implements Serializable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ApplicationMailer.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @JsonIgnore
    @Column(name = "case_number")
    private String caseNumber;
    @Pattern(regexp = "[A-Za-z0-9.!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9"
        + ".!#$%&'*+-/=?^_`{|}~]+\\.[A-Za-z]{2,}+", message = "{global.datatype.email.notValid}")
    @Column(name = "email")
    private String email;
    @Column(name = "reply_mail")
    private String replyMail;
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "bundle_id", referencedColumnName = "id")
    private Bundle bundle;
    @Temporal(TemporalType.DATE)
    @NotNull(message = "{encounter.startTime.notNull}")
    @Column(name = "start_date", nullable = false)
    private Date startDate;
    @Temporal(TemporalType.DATE)
    @Column(name = "end_date", nullable = true)
    private Date endDate;
    @Column(name = "repeat_period", nullable = true)
    private Integer repeatPeriod;
    @Enumerated(EnumType.STRING)
    @Column(name = "encounterscheduled_serial_type")
    private EncounterScheduledSerialType encounterScheduledSerialType;
    @Column(name = "locale")
    private String locale;
    @Column(name = "personal_text", columnDefinition = "TEXT")
    private String personalText;
    @Enumerated(EnumType.STRING)
    @Column(name = "mail_status")
    private EncounterScheduledMailStatus mailStatus = EncounterScheduledMailStatus.ACTIVE;
    @OneToMany(mappedBy = "EncounterScheduled", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Encounter> encounters = new HashSet<>();
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "clinic_id", referencedColumnName = "id")
    private Clinic clinic;

    public EncounterScheduled() {
    }

    /**
     * Uses the setters to initialize the object. See setters for constraints.
     *
     * @param caseNumber                   A case number for this scheduled encounter.
     * @param bundle                       The associated bundle for this scheduled encounter.
     * @param email                        An email-adress for communication with the patient.
     * @param startDate                    The start date of the scheduled encounter series.
     * @param encounterScheduledSerialType The interval type of this scheduled encounter
     * @param endDate                      The end date of the scheduled encounter series.
     * @param repeatPeriod                 The repeat period (in days) of the scheduled
     * @param locale                       the locale of the current EncounterScheduled object.
     * @param personalText                 personalText for the scheduled encounter.
     * @param replyMail                    The replay mail of the scheduled encounter. series.
     */
    public EncounterScheduled(final String caseNumber, final Bundle bundle, final Clinic clinic, final Date startDate,
        final EncounterScheduledSerialType encounterScheduledSerialType, final Date endDate,
        final Integer repeatPeriod, final String email, final String locale,
        final String personalText, final String replyMail) {
        setCaseNumber(caseNumber);
        setBundle(bundle);
        setStartDate(startDate);
        setEndDate(endDate);
        setRepeatPeriod(repeatPeriod);
        setEncounterScheduledSerialType(encounterScheduledSerialType);
        setEmail(email);
        setLocale(locale);
        setPersonalText(personalText);
        setReplyMail(replyMail);
        setClinic(clinic);
    }

    /**
     * Returns the id of the current EncounterScheduled object.
     *
     * @return The current id of this EncounterScheduled object. Might be
     * <code>null</code> for newly created objects.
     */
    public Long getId() {
        return id;
    }

    public String getUUID() {
        return this.uuid;
    }

    /**
     * Returns the locale of the current EncounterScheduled object as a String.
     *
     * @return The current locale of this EncounterScheduled object.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the locale of the current EncounterScheduled object as a String.
     *
     * @param locale The new locale to set for this EncounterScheduled object.
     */
    public void setLocale(final String locale) {
        this.locale = locale;
    }

    /**
     * Returns the personalText of the current EncounterScheduled object.
     *
     * @return The personalText of the this EncounterScheduled object.
     */
    public String getPersonalText() {
        return personalText;
    }

    /**
     * Sets the personaleText of the current EncounterScheduled object.
     *
     * @param personalText The new personalText to set for this EncounterScheduled object.
     */
    public void setPersonalText(final String personalText) {
        this.personalText = personalText;
    }

    /**
     * The case number or pseudonym is an identifier number for a HIS or study, to identify a
     * concrete encounter for this EncounterScheduled object.
     *
     * @return The case number or pseudonym of this EncounterScheduled object. Is never
     * <code>null</code>. Is never empty.
     */
    public String getCaseNumber() {
        return caseNumber;
    }

    /**
     * See {@link EncounterScheduled#getCaseNumber()} for a description. Sets the case number or
     * pseudonym of this EncounterScheduled object. Trims it before setting.
     *
     * @param caseNumber The case number or pseudonym of this EncounterScheduled object. Must not be
     *                   <code>null</code>. Must not be empty (after trimming).
     */
    public void setCaseNumber(final String caseNumber) {
        assert caseNumber != null : "The given case number is null";
        assert !caseNumber.trim().isEmpty() :
            "The given case number is empty (after" + " trimming)";
        this.caseNumber = caseNumber.trim();
    }

    /**
     * The email address is used to send an email for every encounter of this EncounterScheduled
     * object to the patient.
     *
     * @return The email address of this EncounterScheduled object. Is never
     * <code>null</code>. Is never empty.
     */
    public String getEmail() {
        return email;
    }

    /**
     * See {@link EncounterScheduled#getEmail()} for a description. Sets the email address of this
     * EncounterScheduled object.
     *
     * @param email The new email address of this EncounterScheduled object. Must not be
     *              <code>null</code>. Must not be empty.
     */
    public void setEmail(final String email) {
        assert email != null : "The given email was null";
        this.email = email;
    }

    /**
     * @return The {@link Bundle} this EncounterScheduled object is associated with. Must not be
     * <code>null</code>. Must not be empty.
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Sets a new bundle this encounter should be associated with.
     *
     * @param bundle The {@link Bundle} this encounter should be associated with. Must not be
     *               <code>null</code>.
     */
    public void setBundle(final Bundle bundle) {
        assert bundle != null : "The given Bundle was null";
        this.bundle = bundle;
    }

    /**
     * Returns the start date of the current EncounterScheduled object.
     *
     * @return The start date of the current EncounterScheduled object. Is never
     * <code>null</code>.
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the startDate {@link EncounterScheduled#getStartDate()} of this EncounterScheduled
     * object.
     *
     * @param startDate The new start date of this EncounterScheduled object.
     */
    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end date of the current EncounterScheduled object.
     *
     * @return The end date of the current EncounterScheduled object. Is never
     * <code>null</code>.
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the endDate {@link EncounterScheduled#getEndDate()} of this EncounterScheduled object.
     *
     * @param endDate The new end date of this EncounterScheduled object.
     */
    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the repeat period (days between two encounters associated with this
     * EncounterScheduled) of this EncounterScheduled object.
     *
     * @return The repeat period (days between two encounters associated with this
     * EncounterScheduled) of this EncounterScheduled object. The repeat period may be
     * <code>null</code> depending on the {@link EncounterScheduledSerialType}.
     */
    public Integer getRepeatPeriod() {
        return repeatPeriod;
    }

    /**
     * Sets the new repeat period (days between two encounters associated with this
     * EncounterScheduled) of this EncounterScheduled object.
     *
     * @param repeatPeriod The new repeat period (days between two encounters associated with this
     *                     EncounterScheduled) of this EncounterScheduled object.
     */
    public void setRepeatPeriod(final Integer repeatPeriod) {
        assert repeatPeriod > 0 : "The given repeatPriod was not greater than 0";
        this.repeatPeriod = repeatPeriod;
    }

    /**
     * Gets the {@link EncounterScheduledSerialType} of this EncounterScheduled object.
     *
     * @return The {@link EncounterScheduledSerialType} of this EncounterScheduled object. Must not
     * be <code>null</code>.
     */
    public EncounterScheduledSerialType getEncounterScheduledSerialType() {
        return encounterScheduledSerialType;
    }

    /**
     * Sets the new {@link EncounterScheduledSerialType} of this EncounterScheduled object.
     *
     * @param encounterScheduledSerialType The new
     *                                     <p>
     *                                     <p>
     *                                     {@link EncounterScheduledSerialType} of this
     *                                     EncounterScheduled object.
     */
    public void setEncounterScheduledSerialType(
        final EncounterScheduledSerialType encounterScheduledSerialType) {
        this.encounterScheduledSerialType = encounterScheduledSerialType;
    }

    /**
     * Returns all associated {@link Encounter encounters} of this EncounterScheduled object.
     *
     * @return All associated {@link Encounter encounters} of this EncounterScheduled object.
     */
    public Set<Encounter> getEncounters() {
        return encounters;
    }

    /**
     * Add an {@link Encounter} to this EncounterScheduled object.
     *
     * @param encounter Must not be <code>null</code>.
     */
    public void addEncounter(final Encounter encounter) {
        assert encounter != null : "The given Response was null";
        if (!this.encounters.contains(encounter)) {
            this.encounters.add(encounter);
        }

        if (encounter.getEncounterScheduled() == null || !encounter.getEncounterScheduled()
            .equals(this)) {
            encounter.setEncounterScheduled(this);
        }
    }

    /**
     * Sets a list of {@link Encounter encounters} to this EncounterScheduled object.
     *
     * @param encounters the new set of {@link Encounter encounters} for this EncounterScheduled
     *                   object.
     */
    public void setEncounters(final Set<Encounter> encounters) {
        this.encounters = encounters;
    }

    /**
     * Get the mail status of this {@link EncounterScheduled} object.
     *
     * @return The current mail status of this {@link EncounterScheduled} object.
     */
    public EncounterScheduledMailStatus getMailStatus() {
        return mailStatus;
    }

    /**
     * Set the mail status of this {@link EncounterScheduled} object.
     *
     * @param mailStatus The new mail status of this {@link EncounterScheduled} object.
     */
    public void setMailStatus(final EncounterScheduledMailStatus mailStatus) {
        this.mailStatus = mailStatus;
    }

    /**
     * Get the mail address that is used as reply to address for every mail sent for this scheduled
     * encounter to the patient.
     *
     * @return The reply mail address of the currenct scheduled encounter.
     */
    public String getReplyMail() {
        return replyMail;
    }

    /**
     * Set the mail address that is used as reply to address for every mail sent for this scheduled
     * encounter to the patient.
     *
     * @param replyMail The new reply mail address of the currenct scheduled encounter.
     */
    public void setReplyMail(final String replyMail) {
        this.replyMail = replyMail;
    }

    /**
     * Returns the {@link Clinic} for this {@link EncounterScheduled} object.
     *
     * @return The {@link Clinic} for this {@link EncounterScheduled} object.
     */
    public Clinic getClinic() {
        return clinic;
    }

    /**
     * Sets the {@link Clinic} for this {@link EncounterScheduled} object.
     *
     * @param clinic The new {@link Clinic} for this {@link EncounterScheduled}.
     */
    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
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
        if (!(obj instanceof EncounterScheduled)) {
            return false;
        }
        EncounterScheduled other = (EncounterScheduled) obj;
        return getUUID().equals(other.getUUID());
    }


    /**
     * Sends a mail which askes the patient to reactivate the reminder mails for thisfor
     * {@link EncounterScheduled}.
     *
     * @param applicationMailer {@link ApplicationMailer} to send mail with created content.
     * @param messageSource     {@link MessageSource} to get the appropriate messages.
     * @param baseUrl           String to get the root URL of the server.
     * @return True if the mail has been sent, false otherwise.
     */
    public Boolean sendReactivationMail(final ApplicationMailer applicationMailer,
        final MessageSource messageSource, final String baseUrl) {
        if ((this.endDate == null || this.endDate.after(new Date())) && this.mailStatus.equals(
            EncounterScheduledMailStatus.DEACTIVATED_PATIENT)) {
            Locale locale = LocaleHelper.getLocaleFromString(this.locale);

            String footerEmail = applicationMailer.getMailFooterEMail();
            String footerPhone = applicationMailer.getMailFooterPhone();

            // Create links for the mail content
            String reactivationLink =
                baseUrl + "/encounter/activateMailStatusByPatient?hash=" + this.uuid;

            // Create mail content
            String content = messageSource.getMessage("mail.encounterScheduled" + ".reactivation",
                new Object[]{reactivationLink, reactivationLink}, locale);
            String footer = messageSource.getMessage("mail.encounter.footer",
                new Object[]{footerEmail, footerPhone}, locale);
            String subject = messageSource.getMessage("mail.encounter.subject", new Object[]{},
                locale);

            try {
                applicationMailer.sendMail(this.email, null, subject, content + footer,
                    this.replyMail);
            } catch (Exception e) {
                LOGGER.debug("Die E-Mail konnte nicht gesendet werden.");
                return false;
            }

            return true;
        }
        return false;
    }
}
