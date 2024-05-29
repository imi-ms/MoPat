package de.imi.mopat.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * This data transfer object is used to submit a response in the form of a JSON String.
 */
public class ResponseDTO {

    private Long answerId;
    private String customtext = null;
    private Double value = null;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date date = null;
    private boolean enabled = true;
    private List<PointOnImageDTO> pointsOnImage;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(final Long answerId) {
        this.answerId = answerId;
    }

    public String getCustomtext() {
        return customtext;
    }

    public void setCustomtext(final String customtext) {
        this.customtext = customtext;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(final Double value) {
        this.value = value;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public List<PointOnImageDTO> getPointsOnImage() {
        return pointsOnImage;
    }

    public void setPointsOnImage(final List<PointOnImageDTO> pointsOnImage) {
        this.pointsOnImage = pointsOnImage;
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
