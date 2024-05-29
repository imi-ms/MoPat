package de.imi.mopat.model.dto;

import java.util.Date;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

public class OneTimeStatisticDTO {

    private Long bundleId;
    @NotNull(message = "{statistic.startdate.notNull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date bundleStartDate;
    @NotNull(message = "{statistic.enddate.notNull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date bundleEndDate;

    private String patientId;
    @NotNull(message = "{statistic.startdate.notNull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date patientStartDate;
    @NotNull(message = "{statistic.enddate.notNull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date patientEndDate;

    private String bundlePatientPatientId;
    private Long bundlePatientBundleId;
    @NotNull(message = "{statistic.startdate.notNull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date bundlePatientStartDate;
    @NotNull(message = "{statistic.enddate.notNull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date bundlePatientEndDate;

    private Long encounterCountByBundleInInterval;
    private Long encounterCountByCaseNumberInInterval;
    private Long encounterCountByCaseNumberByBundleInInterval;


    public OneTimeStatisticDTO() {

    }

    public OneTimeStatisticDTO(final Date startdate, final Date enddate) {
        this.bundleStartDate = startdate;
        this.bundleEndDate = enddate;
        this.patientStartDate = startdate;
        this.patientEndDate = enddate;
        this.bundlePatientStartDate = startdate;
        this.bundlePatientEndDate = enddate;
    }

    public Long getBundleId() {
        return bundleId;
    }

    public void setBundleId(final Long bundleId) {
        this.bundleId = bundleId;
    }

    public Date getBundleStartDate() {
        return bundleStartDate;
    }

    public void setBundleStartDate(final Date bundleStartDate) {
        this.bundleStartDate = bundleStartDate;
    }

    public Date getBundleEndDate() {
        return bundleEndDate;
    }

    public void setBundleEndDate(final Date bundleEndDate) {
        this.bundleEndDate = bundleEndDate;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(final String patientId) {
        this.patientId = patientId;
    }

    public Date getPatientStartDate() {
        return patientStartDate;
    }

    public void setPatientStartDate(final Date patientStartDate) {
        this.patientStartDate = patientStartDate;
    }

    public Date getPatientEndDate() {
        return patientEndDate;
    }

    public void setPatientEndDate(final Date patientEndDate) {
        this.patientEndDate = patientEndDate;
    }

    public String getBundlePatientPatientId() {
        return bundlePatientPatientId;
    }

    public void setBundlePatientPatientId(final String bundlePatientPatientId) {
        this.bundlePatientPatientId = bundlePatientPatientId;
    }

    public Long getBundlePatientBundleId() {
        return bundlePatientBundleId;
    }

    public void setBundlePatientBundleId(final Long bundlePatientBundleId) {
        this.bundlePatientBundleId = bundlePatientBundleId;
    }

    public Date getBundlePatientStartDate() {
        return bundlePatientStartDate;
    }

    public void setBundlePatientStartDate(final Date bundlePatientStartDate) {
        this.bundlePatientStartDate = bundlePatientStartDate;
    }

    public Date getBundlePatientEndDate() {
        return bundlePatientEndDate;
    }

    public void setBundlePatientEndDate(final Date bundlePatientEndDate) {
        this.bundlePatientEndDate = bundlePatientEndDate;
    }

    public Long getEncounterCountByBundleInInterval() {
        return encounterCountByBundleInInterval;
    }

    public void setEncounterCountByBundleInInterval(final Long encounterCountByBundleInInterval) {
        this.encounterCountByBundleInInterval = encounterCountByBundleInInterval;
    }

    public Long getEncounterCountByCaseNumberInInterval() {
        return encounterCountByCaseNumberInInterval;
    }

    public void setEncounterCountByCaseNumberInInterval(
        final Long encounterCountByCaseNumberInInterval) {
        this.encounterCountByCaseNumberInInterval = encounterCountByCaseNumberInInterval;
    }

    public Long getEncounterCountByCaseNumberByBundleInInterval() {
        return encounterCountByCaseNumberByBundleInInterval;
    }

    public void setEncounterCountByCaseNumberByBundleInInterval(
        final Long encounterCountByCaseNumberByBundleInInterval) {
        this.encounterCountByCaseNumberByBundleInInterval = encounterCountByCaseNumberByBundleInInterval;
    }
}
