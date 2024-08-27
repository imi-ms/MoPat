package de.imi.mopat.helper.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.model.SliderIconConfig;
import de.imi.mopat.model.SliderIconDetail;
import de.imi.mopat.model.dto.SliderIconDetailDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import de.imi.mopat.model.dto.SliderIconConfigDTO;

@Service
public class SliderIconConfigService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        SliderIconConfig.class);


    /**
     * Converts this {@link SliderIconConfig} object to an {@link SliderIconConfigDTO} object.
     *
     * @return An {@link SliderIconConfigDTO} object based on this {@link SliderIconConfig} object.
     */
    @JsonIgnore
    public SliderIconConfigDTO toSliderIconConfigDTO(SliderIconConfig sliderIconConfig) {
        SliderIconConfigDTO sliderIconConfigDTO = new SliderIconConfigDTO();
        sliderIconConfigDTO.setId(sliderIconConfig.getId());
        sliderIconConfigDTO.setConfigName(sliderIconConfig.getConfigName());
        sliderIconConfigDTO.setNumberOfIcons(sliderIconConfig.getNumberOfIcons());
        List<SliderIconDetailDTO> sliderIconDetailDTOS = new ArrayList<>();
        for (SliderIconDetail sliderIconDetail : sliderIconConfig.getIcons()) {
            sliderIconDetailDTOS.add(new SliderIconDetailDTO(sliderIconDetail.getId(),
                sliderIconDetail.getIconPosition(), sliderIconConfig.getId(),
                sliderIconDetail.getPredefinedSliderIcon().getIconName()));
        }
        sliderIconConfigDTO.setSliderIconDetailDTOS(sliderIconDetailDTOS);

        return sliderIconConfigDTO;
    }
}
