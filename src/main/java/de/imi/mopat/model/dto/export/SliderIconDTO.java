package de.imi.mopat.model.dto.export;

public class SliderIconDTO {

    private Long id;

    private Integer iconPosition = null;

    private String predefinedSliderIcon = null;

    private Long answerId;

    public Long getId() {
        return id;
    }

    public Integer getIconPosition() {
        return iconPosition;
    }

    public void setIconPosition(final Integer position) {
        this.iconPosition = position;
    }

    public String getPredefinedSliderIcon() {
        return predefinedSliderIcon;
    }

    public void setPredefinedSliderIcon(final String icon) {
        this.predefinedSliderIcon = icon;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(final Long answerId) {
        this.answerId = answerId;
    }
}
