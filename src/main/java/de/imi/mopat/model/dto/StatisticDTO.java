package de.imi.mopat.model.dto;

import de.imi.mopat.model.Statistic;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

public class StatisticDTO implements Serializable {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date minDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date maxDate;
    @NotNull(message = "{statistic.startdate.notNullt}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    @NotNull(message = "{statistic.enddate.notNull}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
    private int count;
    private List<Statistic> statistics = new ArrayList<>();

    public StatisticDTO(final Date startDate, final Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxDate = endDate;
        this.minDate = startDate;
        this.count = 2;
    }

    public StatisticDTO() {
    }

    public Date getMinDate() {
        return minDate;
    }

    public void setMinDate(final Date minDate) {
        this.minDate = minDate;
    }

    public Date getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(final Date maxDate) {
        this.maxDate = maxDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public List<Statistic> getStatistics() {
        return statistics;
    }

    public void setStatistics(final List<Statistic> statistics) {
        this.statistics = statistics;
    }
}
