package de.imi.mopat.model.dto;

import java.awt.Image;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;

public class SliderIconDetailDTO {

    private Long id;

    private Integer iconPosition;

    private Long sliderIconConfigId;

    private String predefinedSliderIcon;

    @JsonIgnore
    private MultipartFile userIcon;

    private String userIconBase64;

    public SliderIconDetailDTO() {
    }

    // Full constructor
    public SliderIconDetailDTO(Long id, Integer iconPosition, Long sliderIconConfigId, String predefinedSliderIcon) {
        this.id = id;
        this.iconPosition = iconPosition;
        this.sliderIconConfigId = sliderIconConfigId;
        this.predefinedSliderIcon=predefinedSliderIcon;
    }

    public SliderIconDetailDTO(Long id, Integer iconPosition, Long sliderIconConfigId) {
        this.id = id;
        this.iconPosition = iconPosition;
        this.sliderIconConfigId = sliderIconConfigId;
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

    public MultipartFile getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(MultipartFile userIcon) {
        this.userIcon = userIcon;
    }


    public String getUserIconBase64() {
        return userIconBase64;
    }

    public void setUserIconBase64(String userIconBase64) {
        this.userIconBase64 = userIconBase64;
    }
}