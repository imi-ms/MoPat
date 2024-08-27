package de.imi.mopat.model.dto;

public class SliderIconDetailDTO {

    private Long id;
    private Integer iconPosition;
    private Long sliderIconConfigId;
    private String predefinedSliderIcon;
    private String userIcon;

    public SliderIconDetailDTO() {
    }

    // Full constructor
    public SliderIconDetailDTO(Long id, Integer iconPosition, Long sliderIconConfigId, String predefinedSliderIcon) {
        this.id = id;
        this.iconPosition = iconPosition;
        this.sliderIconConfigId = sliderIconConfigId;
        this.predefinedSliderIcon=predefinedSliderIcon;
    }



    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getIconPosition() {
        return iconPosition;
    }

    public void setIconPosition(Integer iconPosition) {
        this.iconPosition = iconPosition;
    }

    public Long getSliderIconConfigId() {
        return sliderIconConfigId;
    }

    public void setSliderIconConfigId(Long sliderIconConfigId) {
        this.sliderIconConfigId = sliderIconConfigId;
    }

    public String getPredefinedSliderIcon() {
        return predefinedSliderIcon;
    }

    public void setPredefinedSliderIcon(String predefinedSliderIcon) {
        this.predefinedSliderIcon = predefinedSliderIcon;
    }

    public String getUserSliderIcon() {
        return userIcon;
    }

    public void setUserSliderIcon(String userSliderIcon) {
        this.userIcon = userSliderIcon;
    }
}