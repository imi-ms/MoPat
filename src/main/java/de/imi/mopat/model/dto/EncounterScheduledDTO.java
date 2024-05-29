package de.imi.mopat.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.model.enumeration.EncounterScheduledMailStatus;
import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * This data transfer object is used to submit an EncounterScheduled in the form of a JSON String.
 */
public class EncounterScheduledDTO {

    private Long id;
    private String uuid;

    @JsonIgnore
    @NotNull(message = "{encounterScheduled.caseNumber.notNull}")
    @Size(min = 1, message = "{encounterScheduled.caseNumber.notNull}")
    private String caseNumber;

    @JsonIgnore
    @NotNull(message = "{encounterScheduled.bundle.notNull}")
    private BundleDTO bundleDTO;

    @JsonIgnore
    @Pattern(regexp = "[A-Za-z0-9.!#$%&'*+-/=?^_`{|}~]+@[A-Za-z0-9"
        + ".!#$%&'*+-/=?^_`{|}~]+\\.[A-Za-z]{2,}+", message = "{global.datatype.email.notValid}")
    private String email;

    // No email validation necessary, custom validator checks this property
    private String replyMail;

    @NotNull(message = "{encounterScheduled.startDate.notNull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @JsonIgnore
    @NotNull(message = "{encounterScheduled.encounterScheduledSerialType" + ".notNull}")
    private EncounterScheduledSerialType encounterScheduledSerialType = EncounterScheduledSerialType.UNIQUELY;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    private Locale locale;

    @JsonIgnore
    private String personalText;

    @JsonIgnore
    private Integer repeatPeriod;

    private EncounterScheduledMailStatus mailStatus;

    private List<EncounterDTO> encounterDTOs;
    //possible reply to email addresses
    private Map<Long, Set<String>> replyMails;

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

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(final String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public BundleDTO getBundleDTO() {
        return bundleDTO;
    }

    public void setBundleDTO(final BundleDTO bundleDTO) {
        this.bundleDTO = bundleDTO;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public EncounterScheduledSerialType getEncounterScheduledSerialType() {
        return encounterScheduledSerialType;
    }

    public void setEncounterScheduledSerialType(
        EncounterScheduledSerialType encounterScheduledSerialType) {
        this.encounterScheduledSerialType = encounterScheduledSerialType;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public Integer getRepeatPeriod() {
        return repeatPeriod;
    }

    public void setRepeatPeriod(final Integer repeatPeriod) {
        this.repeatPeriod = repeatPeriod;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public String getPersonalText() {
        return personalText;
    }

    public void setPersonalText(final String personalText) {
        this.personalText = personalText;
    }

    public List<EncounterDTO> getEncounterDTOs() {
        return encounterDTOs;
    }

    public void setEncounterDTOs(final List<EncounterDTO> encounterDTOs) {
        this.encounterDTOs = encounterDTOs;
    }

    public EncounterScheduledMailStatus getMailStatus() {
        return mailStatus;
    }

    public void setMailStatus(final EncounterScheduledMailStatus mailStatus) {
        this.mailStatus = mailStatus;
    }

    public String getReplyMail() {
        return replyMail;
    }

    public void setReplyMail(final String replyMail) {
        this.replyMail = replyMail;
    }

    public Map<Long, Set<String>> getReplyMails() {
        return replyMails;
    }

    public void setReplyMails(final Map<Long, Set<String>> replyMails) {
        this.replyMails = replyMails;
    }

    public Boolean isCompleted() {
        if (this.endDate != null) {
            return new Date().after(endDate);
        } else {
            return false;
        }
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
