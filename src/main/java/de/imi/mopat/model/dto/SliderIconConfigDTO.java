package de.imi.mopat.model.dto;

import java.util.List;
import java.util.Set;

/**
 *
 */
public class SliderIconConfigDTO {
    private Long id;
    private Integer numberOfIcons;
    private String configName; // Assumed to be String based on likely usage
    private List<SliderIconDetailDTO> sliderIconDetailDTOS; // Simplified to IDs for ease and data size concerns
    private String configType;

    public SliderIconConfigDTO() {
    }

    public SliderIconConfigDTO(Integer numberOfIcons, String configName) {
        this.numberOfIcons = numberOfIcons;
        this.configName = configName;
    }
    public SliderIconConfigDTO(Long id, Integer numberOfIcons, String configName) {
        this.id=id;
        this.numberOfIcons = numberOfIcons;
        this.configName = configName;
    }
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumberOfIcons() {
        return numberOfIcons;
    }

    public void setNumberOfIcons(Integer numberOfIcons) {
        this.numberOfIcons = numberOfIcons;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public List<SliderIconDetailDTO> getSliderIconDetailDTOS() {
        return sliderIconDetailDTOS;
    }

    public void setSliderIconDetailDTOS(List<SliderIconDetailDTO> sliderIconDetailDTOS) {
        this.sliderIconDetailDTOS = sliderIconDetailDTOS;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }
}
