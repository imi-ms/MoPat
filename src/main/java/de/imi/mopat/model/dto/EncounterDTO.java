package de.imi.mopat.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.model.enumeration.Gender;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * This data transfer object is used to submit an encounter in the form of a JSON String.
 */
public class EncounterDTO {

    private Long id;
    private String uuid;

    private List<ResponseDTO> responses = new ArrayList<>();
    private Long lastSeenQuestionId;

    private boolean isCompleted;
    private boolean isTest;
    private boolean isAtHome;

    private BundleDTO bundleDTO;
    private String bundleLanguage;
    // Ids of activated questionnaires for export
    private List<Long> activeQuestionnaireIds;
    private Timestamp startTime;
    private Timestamp endTime;
    private Timestamp lastReminderDate;
    private String successfullExports;
    private EncounterScheduledDTO encounterScheduledDTO;
    //BodyPart images used in survey as base64 string
    private String frontImage;
    private String backImage;

    @JsonIgnore
    private Long patientID; // patient number, exclusively for export
    @JsonIgnore
    @NotNull(message = "{encounter.caseNumber.notNull}")
    @NotEmpty(message = "{encounter.caseNumber.notEmpty}")
    private String caseNumber;
    @JsonIgnore
    private String firstname;
    @JsonIgnore
    private String lastname;
    @JsonIgnore
    private Date birthdate;
    @JsonIgnore
    private Gender gender;

    public EncounterDTO() {
    }

    public EncounterDTO(final Boolean isTest, final String caseNumber) {
        this.isTest = isTest;
        this.caseNumber = caseNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public List<ResponseDTO> getResponses() {
        return responses;
    }

    public void setResponses(final List<ResponseDTO> responses) {
        this.responses = responses;
    }

    public Long getLastSeenQuestionId() {
        return lastSeenQuestionId;
    }

    public void setLastSeenQuestionId(final Long lastSeenQuestionId) {
        this.lastSeenQuestionId = lastSeenQuestionId;
    }

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(final boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public boolean getIsTest() {
        return isTest;
    }

    public void setIsTest(boolean isTest) {
        this.isTest = isTest;
    }

    public BundleDTO getBundleDTO() {
        return bundleDTO;
    }

    public void setBundleDTO(final BundleDTO bundleDTO) {
        this.bundleDTO = bundleDTO;
    }

    public String getBundleLanguage() {
        return bundleLanguage;
    }

    public void setBundleLanguage(final String bundleLanguage) {
        this.bundleLanguage = bundleLanguage;
    }

    public List<Long> getActiveQuestionnaireIds() {
        return activeQuestionnaireIds;
    }

    public void setActiveQuestionnaireIds(final List<Long> activeQuestionnaireIds) {
        this.activeQuestionnaireIds = activeQuestionnaireIds;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(final Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(final Timestamp endTime) {
        this.endTime = endTime;
    }

    public Long getPatientID() {
        return patientID;
    }

    public void setPatientID(final Long patientID) {
        this.patientID = patientID;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(final String caseNumber) {
        this.caseNumber = caseNumber;
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

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(final Date birthdate) {
        this.birthdate = birthdate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getSuccessfullExports() {
        return successfullExports;
    }

    public void setSuccessfullExports(final String successfullExports) {
        this.successfullExports = successfullExports;
    }

    public EncounterScheduledDTO getEncounterScheduledDTO() {
        return encounterScheduledDTO;
    }

    public void setEncounterScheduledDTO(final EncounterScheduledDTO encounterScheduledDTO) {
        if (encounterScheduledDTO != null) {
            this.encounterScheduledDTO = encounterScheduledDTO;
            this.isAtHome = true;
        } else {
            this.isAtHome = false;
        }

    }

    public Timestamp getLastReminderDate() {
        return lastReminderDate;
    }

    public void setLastReminderDate(final Timestamp lastRemindDate) {
        this.lastReminderDate = lastRemindDate;
    }

    public String getFrontImage() {
        return frontImage;
    }

    public void setFrontImage(final String frontImage) {
        this.frontImage = frontImage;
    }

    public String getBackImage() {
        return backImage;
    }

    public void setBackImage(final String backImage) {
        this.backImage = backImage;
    }

    public boolean isIsAtHome() {
        return isAtHome;
    }

    public void setIsAtHome(final boolean isAtHome) {
        this.isAtHome = isAtHome;
    }

    @JsonIgnore
    public void removeDemographics() {
        this.birthdate = null;
        this.caseNumber = null;
        this.firstname = null;
        this.gender = null;
        this.lastname = null;
        this.patientID = null;
    }

    /**
     * Checks the presence of audit relevant attributes and lists them for logging purposes.
     *
     * @return is not <code>null</code>.
     */
    @JsonIgnore
    public String getLoggingAttributes() {
        // [bt] create String to list all the patient attributes that have
        // been set
        String result = "";
        if (getBirthdate() != null) {
            result = result.concat("Date of birth");
        }
        if (getFirstname() != null) {
            result = result.isEmpty() ? result.concat("First name") : result.concat(", First name");
        }
        if (getGender() != null) {
            result = result.isEmpty() ? result.concat("Gender") : result.concat(", gender");
        }
        if (getLastname() != null) {
            result = result.isEmpty() ? result.concat("Last name") : result.concat(", last name");
        }
        if (getPatientID() != null) {
            result = result.isEmpty() ? result.concat("Patient ID") : result.concat(", patient ID");
        }
        return result;
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
}
