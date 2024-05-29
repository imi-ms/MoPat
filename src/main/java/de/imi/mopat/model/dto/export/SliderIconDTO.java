package de.imi.mopat.model.dto.export;

public class SliderIconDTO {

    private Long id;

    private Integer position = null;

    private String icon = null;

    private Long answerId;

    public Long getId() {
        return id;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(final Long answerId) {
        this.answerId = answerId;
    }
}
