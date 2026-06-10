package de.imi.mopat.model.dto;

import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Date;
import java.util.Locale;
import org.springframework.format.annotation.DateTimeFormat;

public class EncounterScheduledApiRequestDTO {

    @NotNull
    private String caseNumber;

    @NotNull
    private Long bundleId;

    @NotNull
    private Long clinicId;

    @Pattern(regexp = "[A-Za-z0-9.!#$%&'*+\\-/=?^_`{|}~]+@[A-Za-z0-9.!#$%&'*+\\-/=?^_`{|}~]+\\.[A-Za-z]{2,}")
    private String email;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @NotNull
    private EncounterScheduledSerialType encounterScheduledSerialType = EncounterScheduledSerialType.UNIQUELY;

    private String replyMail;

    private String personalText;

    //TODO: Yannick fragen, ob das so ok ist
    private Locale locale;

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public Long getBundleId() {
        return bundleId;
    }

    public void setBundleId(Long bundleId) {
        this.bundleId = bundleId;
    }

    public Long getClinicId() {
        return clinicId;
    }

    public void setClinicId(Long clinicId) {
        this.clinicId = clinicId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public EncounterScheduledSerialType getEncounterScheduledSerialType() {
        return encounterScheduledSerialType;
    }

    public void setEncounterScheduledSerialType(EncounterScheduledSerialType type) {
        this.encounterScheduledSerialType = type;
    }

    public String getReplyMail() {
        return replyMail;
    }

    public void setReplyMail(String replyMail) {
        this.replyMail = replyMail;
    }

    public String getPersonalText() {
        return personalText;
    }

    public void setPersonalText(String personalText) {
        this.personalText = personalText;
    }
    public Locale getLocale() {
        return locale;
    }
    public void setLocale (Locale locale){
        this.locale = locale;
    }
}