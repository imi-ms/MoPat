package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.dto.ResponseDTO;
import de.imi.mopat.model.enumeration.EncounterScheduledMailStatus;
import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;
import de.imi.mopat.model.enumeration.ExportStatus;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;

/**
 * The database table model for table <i>encounter</i>. The encounter model represents a patient
 * conduction a questionnaire. This model holds information for a HIS, like the name of the patient,
 * gender, birthdate etc. Furthermore it contains information about which clinic conducts the
 * questionnaire and which bundle is used for this encounter.
 */
@Entity
@Table(name = "encounter")
public class Encounter implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @JsonIgnore
    @Column(name = "uuid", unique = true)
    private String uuid = UUIDGenerator.createUUID();
    @JsonIgnore
    @Column(name = "patient_id")
    private Long patientID; //ORBIS patient number, exclusively for export

    @Column(name = "bundle_language")
    private String bundleLanguage;
    // [bt] @NotNull missing, because of jakarta.validation.valid annotation
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "bundle_id", referencedColumnName = "id")
    private Bundle bundle;
    @NotNull(message = "{encounter.startTime.notNull}")
    @Column(name = "start_time", nullable = false)
    private Timestamp startTime = new Timestamp(System.currentTimeMillis());
    @Column(name = "end_time")
    private Timestamp endTime;
    @JsonIgnore
    @NotNull(message = "{encounter.caseNumber.notNull}")
    @NotEmpty(message = "{encounter.caseNumber.notEmpty}")
    @Column(name = "case_number", nullable = false)
    private String caseNumber; // ORBIS case number or study number
    @OneToMany(mappedBy = "encounter", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Response> responses = new HashSet<>();
    @Column(name = "last_seen_question_id")
    private Long lastSeenQuestionId;
    @JsonIgnore
    @OneToMany(mappedBy = "encounter", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EncounterExportTemplate> encounterExportTemplates = new HashSet<>();
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "encounter_scheduled_id", referencedColumnName = "id")
    private EncounterScheduled encounterScheduled;
    @JsonIgnore
    @ElementCollection
    @Column(name = "active_questionnaires", nullable = false)
    private List<Long> activeQuestionnaires = new ArrayList<>();
    @Column(name = "last_reminder_date")
    private Timestamp lastReminderDate;
    @ManyToOne
    @JoinColumn(name = "clinic_id", referencedColumnName = "id")
    private Clinic clinic;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ApplicationMailer.class);

    public Encounter() { //default constructor (in protected state), should
        // not be accessible to anything else but the JPA implementation
        // (here: Hibernate) and the JUnit tests
    }

    /**
     * An encounter is an instance of the MoPat process.It contains the information (if given) about
     * the patient, who will answer a given
     * {@link Questionnaire questionnaire or a bundle of questionnaires}. Furthermore this model
     * holds information about the start and end time, and the given
     * {@link Answer#getResponses() responses}.<br> Uses the setters to set attributes. See setters
     * for constraints.
     *
     * @param bundle     The {@link Bundle} of the new encounter
     * @param caseNumber The caseNumber of the new encounter
     */
    public Encounter(final Bundle bundle, final String caseNumber) {
        setBundle(bundle);
        setCaseNumber(caseNumber);
    }

    /**
     * @return Might be <code>null</code> for newly created objects. Is never
     * <code> &lt;= 0
     * </code>.
     */
    public Long getId() {
        return id;
    }

    public String getUUID() {
        return this.uuid;
    }

    /**
     * See {@link Answer#getResponses()} for a description
     * <p>
     * Returns all {@link Response Response} objects of the current encounter object.
     *
     * @return The current {@link Response Response} objects of this encounter object. Is never
     * <code>null</code>. Might be empty. Is unmodifiable.
     */
    public Set<Response> getResponses() {
        return Collections.unmodifiableSet(responses);
    }

    /**
     * Sets the {@link Response responses} for this encounter.
     *
     * @param responses Set of {@link Response responses}
     */
    public void setResponses(final Set<Response> responses) {
        assert responses != null : "The given responses were null";
        this.responses = responses;
    }

    /**
     * Adds responses to this encounter object.Takes care that the {@link Response} objects refer to
     * this one, too. See {@link Answer#getResponses()} for a description.
     *
     * @param responses A list of {@link Response Response} objects. Must not be
     *                  <code>null</code>.
     */
    public void addResponses(final Set<Response> responses) {
        assert responses != null : "The given set was null";
        for (Response response : responses) {
            addResponse(response);
        }
    }

    /**
     * See {@link Answer#getResponses()} for a description
     * <p>
     * Takes care that the {@link Response} refers to this answer,too.
     *
     * @param response Must not be <code>null</code>.
     */
    public void addResponse(final Response response) {
        assert response != null : "The given Response was null";
        if (!responses.contains(response)) {
            responses.add(response);
        }
        if (response.getEncounter() == null || !response.getEncounter().equals(this)) {
            response.setEncounter(this);
        }
    }

    /**
     * A HIS patient ID is a further ID, received from a HIS when a
     * {@link Questionnaire Questionnaire} is wired to the HIS. Therefore it must be used when
     * exporting to the HIS.
     *
     * @return The HIS patient ID. Is never <code> &lt;= 0 </code>.
     */
    public Long getPatientID() {
        return patientID;
    }

    /**
     * See {@link Encounter#getPatientID()} for a description Sets a new HIS patient ID for this
     * encounter.Must not be <code> &lt;= 0 </code>.
     *
     * @param patientID The new HIS patient ID
     */
    public void setPatientID(final Long patientID) {
        assert patientID > 0 : "The given Patient ID was <= 0";
        this.patientID = patientID;
    }

    /**
     * See {@link Encounter#Encounter()} for a description for the whole object.
     *
     * @return The bundleLanguage for this encounter. Must not be null be
     * <code>null</code>.
     */
    public String getBundleLanguage() {
        return bundleLanguage;
    }

    /**
     * Sets the bundle language of this encounter.See {@link Encounter#Encounter()} for a
     * description for the whole object.
     *
     * @param bundleLanguage The bundle language. Must not be <code>null</code>. Must not be empty
     *                       (after trimming). Will be trimmed while setting.
     */
    public void setBundleLanguage(final String bundleLanguage) {
        assert bundleLanguage != null : "The given bundle language was null";
        assert
            bundleLanguage.trim().isEmpty() == false :
            "The given bundle language was empty (after " + "trimming)";
        this.bundleLanguage = bundleLanguage;
    }

    /**
     * @return The {@link Bundle Bundle} the encounter is associated with. Might be
     * <code>null</code> (quite unusual).
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Sets a new bundle this encounter should be associated with.Takes care that the {@link Bundle}
     * object refers to this one, too.
     *
     * @param bundle The {@link Bundle Bundle} this encounter should be associated with. Must not be
     *               <code>null</code>.
     */
    public void setBundle(final Bundle bundle) {
        assert bundle != null : "The given Bundle was null";
        this.bundle = bundle;
        //take care that the objects know each other
        if (!bundle.getEncounters().contains(this)) {
            bundle.addEncounter(this);
        }
    }

    /**
     * To verify if an encounter finished a questionnaire, a start time is needed.
     *
     * @return The start time of the encounter. Can be <code>null</code> if it is unknown. Is not
     * after the encounter's end time.
     */
    public Timestamp getStartTime() {
        return startTime;
    }

    /**
     * See {@link Encounter#getStartTime()} for a description Sets the start time of the encounter
     *
     * @param startTime The start time of the encounter. Can be
     *                  <code>null</code> if it is unknown. Must not be after
     *                  the encounter's end time.
     */
    public void setStartTime(final Timestamp startTime) {
        if (startTime != null && endTime != null) {
            assert startTime.before(endTime) :
                "The given startTime was after" + " the encounter's end time";
        }
        this.startTime = startTime;
    }

    /**
     * To verify if an encounter finished a questionnaire, an end time is needed.
     *
     * @return The end time of the encounter. Might be <code>null</code> if it is unknown. Is not
     * before the encounter's start time.
     */
    public Timestamp getEndTime() {
        return endTime;
    }

    /**
     * See {@link Encounter#getEndTime()} for a description Sets the end time of the encounter
     *
     * @param endTime The end time of the encounter. Can be <code>null</code> if it is unknown. Must
     *                not be before the encounter's start time.
     */
    public void setEndTime(final Timestamp endTime) {
        if (endTime != null && startTime != null) {
            assert endTime.after(startTime) :
                "The given end time was before " + "the encounter's start time";
        }
        this.endTime = endTime;
    }

    /**
     * The case number is an identifier number for a HIS or study, to identify a concrete encounter
     * and the responses of it.
     *
     * @return The HIS case number of the encounter. Is never <code>null</code>. Is never empty.
     */
    public String getCaseNumber() {
        return caseNumber;
    }

    /**
     * See {@link Encounter#getCaseNumber()} for a description Sets the HIS case number of the
     * encounter.Trims it before setting.
     *
     * @param caseNumber The HIS case number of the encounter. Must not be
     *                   <code>null</code>. Must not be empty (after trimming).
     */
    public void setCaseNumber(final String caseNumber) {
        assert caseNumber != null : "The given case number is null";
        assert !caseNumber.trim().isEmpty() :
            "The given case number is empty (after" + " trimming)";
        this.caseNumber = caseNumber.trim();
    }

    /**
     * Returns the id of the question which is last seen in this encounter. See
     * {@link Encounter#Encounter()} for a description for the whole object.
     *
     * @return The id of the last seen question. Might be <code>null</code>,
     */
    public Long getLastSeenQuestionId() {
        return lastSeenQuestionId;
    }

    /**
     * Sets questionId for the last seen question in this encounter. See
     * {@link Encounter#Encounter()} for a description for the whole object.
     *
     * @param lastSeenQuestionId The lastSeenQuestionId.
     */
    public void setLastSeenQuestionId(final Long lastSeenQuestionId) {
        this.lastSeenQuestionId = lastSeenQuestionId;
    }

    /**
     * Returns the lastReminderDate for this {@link Encounter} object.
     *
     * @return The lastReminderDate as {@link Timestamp} for this object.
     */
    public Timestamp getLastReminderDate() {
        return lastReminderDate;
    }

    /**
     * Sets the lastReminderDate for this {@link Encounter} object.
     *
     * @param lastReminderDate The new {@link Timestamp} to set for this object. Can be
     *                         <code>null</code> if the encounter ist not attached to an
     *                         {@link EncounterScheduled}.
     */
    public void setLastReminderDate(final Timestamp lastReminderDate) {
        this.lastReminderDate = lastReminderDate;
    }

    /**
     * Sets the active {@link Questionnaire questionnaires} list of this encounter.
     *
     * @param activeQuestionnaires New active {@link Questionnaire questionnaires} list. Must not be
     *                             <code>null</code>.
     */
    public void setActiveQuestionnaires(final List<Long> activeQuestionnaires) {
        assert activeQuestionnaires != null : "The given list is null";
        this.activeQuestionnaires = activeQuestionnaires;
    }

    /**
     * Returns the list of all active {@link Questionnaire questionnaires} of this encounter.
     *
     * @return The list of all active {@link Questionnaire questionnaires} of this encounter. Is
     * never <code>null</code>.
     */
    public List<Long> getActiveQuestionnaires() {
        return this.activeQuestionnaires;
    }

    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Encounter)) {
            return false;
        }
        Encounter other = (Encounter) obj;
        return getUUID().equals(other.getUUID());
    }

    /**
     * Takes care that the {@link Bundle} object does not refer to this one, either.
     */
    public void removeBundle() {
        if (bundle != null) {
            Bundle bundleTemp = bundle;
            bundle = null;
            if (bundleTemp.getEncounters().contains(this)) {
                bundleTemp.removeEncounter(this);
            }
        }
    }

    /**
     * Takes care that the responses don't refer to this anymore, either.
     */
    public void removeResponses() {
        Collection<Response> tempResponses = new ArrayList<>(responses);
        for (Response response : tempResponses) {
            removeResponse(response);
        }
        this.responses.clear();
    }

    /**
     * Takes care that the {@link Response} doesn't refer to this object anymore.But removes the
     * reference from {@link Response} to this object only if the given {@link Response} was part of
     * this encounter and the response referred to this encounter.
     *
     * @param response Must not be <code>null</code>.
     */
    protected void removeResponse(final Response response) {
        assert response != null : "The given response was null";
        responses.remove(response);
        if (response.getEncounter() != null && response.getEncounter().equals(this)) {
            response.removeEncounter();
        }
    }

    /**
     * Add all {@link EncounterExportTemplate EncounterExportTemplate} objects with data from this
     * encounter.
     * <p>
     * An {@link EncounterExportTemplate EncounterExportTemplate} represents an export attempt
     * whether successful or failed.
     *
     * @param encounterExportTemplates Set of
     *                                 {@link EncounterExportTemplate EncounterExportTemplate}
     *                                 objects.
     */
    public void addEncounterExportTemplates(
        final Set<EncounterExportTemplate> encounterExportTemplates) {
        assert encounterExportTemplates != null : "The given set was null";
        for (EncounterExportTemplate encounterExportTemplate : encounterExportTemplates) {
            addEncounterExportTemplate(encounterExportTemplate);
        }
    }

    /**
     * Add an {@link EncounterExportTemplate EncounterExportTemplate} object to this encounter.
     * <p>
     * An {@link EncounterExportTemplate EncounterExportTemplate} represents an export attempt
     * whether successful or failed.
     *
     * @param encounterExportTemplate {@link EncounterExportTemplate EncounterExportTemplate}
     *                                object
     */
    public void addEncounterExportTemplate(final EncounterExportTemplate encounterExportTemplate) {
        if (!encounterExportTemplates.contains(encounterExportTemplate)) {
            this.encounterExportTemplates.add(encounterExportTemplate);
        }
        // take care the objects know each other
        if (encounterExportTemplate.getEncounter() == null
            || !encounterExportTemplate.getEncounter().equals(this)) {
            encounterExportTemplate.setEncounter(this);
        }
    }

    /**
     * Removes an {@link EncounterExportTemplate} object from this {@link Encounter}.
     *
     * @param encounterExportTemplate The {@link EncounterExportTemplate} that schould deleted from
     *                                this {@link Encounter}.
     */
    public void removeEncounterExportTemplate(
        final EncounterExportTemplate encounterExportTemplate) {
        encounterExportTemplates.remove(encounterExportTemplate);
    }

    /**
     * Returns all {@link EncounterExportTemplate EncounterExportTemplate} objects which where
     * exported with the data of this {@link Encounter Encounter} object.
     * <p>
     * An {@link EncounterExportTemplate EncounterExportTemplate} object hold information about an
     * executed export.
     *
     * @return The current {@link EncounterExportTemplate EncounterExportTemplate} objects of this
     * encounter object. Is never <code>null</code>. Might be empty. Is unmodifiable.
     */
    @JsonIgnore
    public Set<EncounterExportTemplate> getEncounterExportTemplates() {
        return Collections.unmodifiableSet(encounterExportTemplates);
    }

    /**
     * Returns the {@link EncounterScheduled} for this {@link Encounter} object.
     *
     * @return The {@link EncounterScheduled} for this {@link Encounter} object. Might be
     * <code>null</code>,
     */
    public EncounterScheduled getEncounterScheduled() {
        return encounterScheduled;
    }

    /**
     * Sets the {@link EncounterScheduled} for this {@link Encounter} object.
     *
     * @param encounterScheduled The new {@link EncounterScheduled} for this {@link Encounter}.
     */
    public void setEncounterScheduled(final EncounterScheduled encounterScheduled) {
        if (!encounterScheduled.getEncounters().contains(this)) {
            encounterScheduled.addEncounter(this);
        }
        this.encounterScheduled = encounterScheduled;
    }

    /**
     * Returns the {@link Clinic} for this {@link Encounter} object.
     *
     * @return The {@link Clinic} for this {@link Encounter} object.
     */
    public Clinic getClinic() {
        return clinic;
    }

    /**
     * Sets the {@link Clinic} for this {@link Encounter} object.
     *
     * @param clinic The new {@link Clinic} for this {@link Encounter}.
     */
    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }


    /**
     * Return the number of currently assigned and successfully exported export templates. If there
     * are more than one export entry but a single one was successful it counts.
     *
     * @return The number of assigned and successfully exported export templates. Is greather than
     * or equal 0.
     */
    @JsonIgnore
    public int getNumberOfAssignedAndSuccessfullyExportedExportTemplates() {
        Set<ExportTemplate> assignedExportTemplates = this.getBundle()
            .getAllAssignedExportTemplates();
        int exportTemplates = 0;
        // iterate over all existing encounter export template
        for (EncounterExportTemplate encounterExportTemplate : encounterExportTemplates) {
            // bundle questionnaire and export template are assigned and
            // export template was successfully exported
            if (assignedExportTemplates.contains(encounterExportTemplate.getExportTemplate())
                && encounterExportTemplate.getExportStatus() == ExportStatus.SUCCESS) {
                // add export template to the list of successfully exported
                // templates
                exportTemplates++;
            }
        }
        // return the number of assigned and successfully exported export
        // templates
        return exportTemplates;
    }

    /**
     * Returns all no longer assigned export entries as a list of
     * {@link EncounterExportTemplate EncounterExportTempate} objects.
     *
     * @return All no longer assigned export entries as a list of
     * {@link EncounterExportTemplate EncounterExportTempate} objects. Can not be <code>null</code>.
     * Might be empty.
     */
    @JsonIgnore
    public List<EncounterExportTemplate> getNoLongerAssignedEncounterExportTemplates() {
        Set<ExportTemplate> assignedExportTemplates = this.getBundle()
            .getAllAssignedExportTemplates();
        List<EncounterExportTemplate> assignedEncounterExportTemplates = new ArrayList<>();
        // iterate over all existing encounter export template
        for (EncounterExportTemplate encounterExportTemplate : this.encounterExportTemplates) {
            // bundle questionnaire and export template are assigned
            if (assignedExportTemplates.contains(encounterExportTemplate.getExportTemplate())) {
                assignedEncounterExportTemplates.add(encounterExportTemplate);
            }
        }
        // remove all assigned encounter export template from the complete list
        // so only the no longer assigned are left
        List<EncounterExportTemplate> noLongerAssigned = new ArrayList<>(
            this.getEncounterExportTemplates());
        noLongerAssigned.removeAll(assignedEncounterExportTemplates);
        // sort by date asc
        Collections.sort(noLongerAssigned);
        return noLongerAssigned;
    }

    /**
     * Returns all export entries depending on this encounter and provided export template.
     *
     * @param exportTemplate Get only the export entries with this export template.
     * @return List of all {@link EncounterExportTemplate EncounterExportTemplate} objects with the
     * provided export template. Can not be <code>null</code>. Might be empty.
     */
    public List<EncounterExportTemplate> getEncounterExportTemplatesByExportTemplate(
        ExportTemplate exportTemplate) {
        List<EncounterExportTemplate> encounterExportTemplateList = new ArrayList<>();
        for (EncounterExportTemplate encounterExportTemplate : this.encounterExportTemplates) {
            // return only encounter-exportTemplate associations with the
            // given exportTemplate
            if (encounterExportTemplate.getExportTemplate().equals(exportTemplate)) {
                encounterExportTemplateList.add(encounterExportTemplate);
            }
        }
        // sort by date asc
        Collections.sort(encounterExportTemplateList);
        return encounterExportTemplateList;
    }

    @Override
    public String toString() {
        return "ID:" + this.getId() + " Case Number:" + this.getCaseNumber() + " Starttime:"
            + this.getStartTime() + " Endtime: " + this.getEndTime() + ". PatientId: "
            + this.getPatientID();
    }


    @JsonIgnore
    public String getJSON() {
        String value;
        try {
            value = new ObjectMapper().writeValueAsString(this);
        } catch (IOException e) {
            value = null;
        }
        return value;
    }

    /**
     * Sends a reminder mail for this object with links to start the survey and to cancel sending
     * mails for the corresponding {@link EncounterScheduled}.
     *
     * @param applicationMailer {@link ApplicationMailer} to send mail with created content.
     * @param messageSource     {@link MessageSource} to get the appropriate messages.
     * @param baseUrl           String to get the root URL of the server.
     * @return True if the mail has been sent, false otherwise.
     */
    public Boolean sendMail(final ApplicationMailer applicationMailer,
        final MessageSource messageSource, final String baseUrl) {
        // If this encounter is part of a schedueld encounter and not fully
        // answered
        if (this.endTime == null && this.encounterScheduled != null
            && this.encounterScheduled.getMailStatus() == EncounterScheduledMailStatus.ACTIVE) {
            Locale locale = LocaleHelper.getLocaleFromString(
                this.getEncounterScheduled().getLocale());

            String footerEmail = applicationMailer.getMailFooterEMail();
            String footerPhone = applicationMailer.getMailFooterPhone();

            // Create links for the mail content
            String surveyLink = baseUrl + "/mobile/survey/encounter?hash=" + this.uuid + "&lang="
                + locale.toString();
            String cancelLink = baseUrl + "/encounter/deactivateMailStatusByPatient?hash="
                + this.encounterScheduled.getUUID();
            String dates = "";

            // Create a string which contains the dates of following encounters
            String encounterDates = "";
            if (!this.encounterScheduled.getEncounterScheduledSerialType()
                .equals(EncounterScheduledSerialType.UNIQUELY)) {
                // Set the date of the probable second encounter
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_MONTH, this.encounterScheduled.getRepeatPeriod());
                // Create a string of the dates of all following encounters
                while (calendar.getTime().before(this.encounterScheduled.getEndDate())) {
                    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
                    encounterDates = encounterDates + "\n" + dateFormat.format(calendar.getTime());
                    calendar.add(Calendar.DAY_OF_MONTH, this.encounterScheduled.getRepeatPeriod());
                }
                dates = messageSource.getMessage("mail.encounter.dates",
                    new Object[]{encounterDates}, locale);
            }

            // Create mail content
            String personalText = "";
            if (this.encounterScheduled.getPersonalText() != null
                && !this.encounterScheduled.getPersonalText().isEmpty()) {
                personalText = messageSource.getMessage("mail.encounter.personalText",
                    new Object[]{this.encounterScheduled.getPersonalText()}, locale);
            }
            String content = messageSource.getMessage("mail.encounter.content",
                new Object[]{personalText, surveyLink, dates, cancelLink}, locale);
            String footer = messageSource.getMessage("mail.encounter.footer",
                new Object[]{footerEmail, footerPhone}, locale);
            String subject = messageSource.getMessage("mail.encounter.subject", new Object[]{},
                locale);

            try {
                applicationMailer.sendMail(this.encounterScheduled.getEmail(), null, subject,
                    content + footer, this.encounterScheduled.getReplyMail());
            } catch (MailException e) {
                this.encounterScheduled.setMailStatus(
                    EncounterScheduledMailStatus.ADDRESS_REJECTED);
                LOGGER.debug("It wasn't possible to send email: " + e.getMessage());
                return false;
            } catch (Exception ex) {
                LOGGER.debug("It wasn't possible to send email: " + ex.getMessage());
                return false;
            }

            this.setLastReminderDate(new Timestamp(System.currentTimeMillis()));

            return true;
        }

        return false;
    }
}
