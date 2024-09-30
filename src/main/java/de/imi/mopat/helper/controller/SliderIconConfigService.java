package de.imi.mopat.helper.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.PredefinedSliderIconDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.SliderIconConfigDao;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.SliderIconConfig;
import de.imi.mopat.model.SliderIconDetail;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.model.dto.SliderIconDetailDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.imi.mopat.model.dto.SliderIconConfigDTO;

@Service
public class SliderIconConfigService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        SliderIconConfig.class);

    @Autowired
    private SliderIconDetailService sliderIconDetailService;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private SliderIconConfigDao sliderIconConfigDao;

    @Autowired
    private PredefinedSliderIconDao predefinedSliderIconDao;

    @Autowired
    private QuestionDao questionDao;


    /**
     * Converts this {@link SliderIconConfig} object to a {@link SliderIconConfigDTO} object.
     *
     * @return A {@link SliderIconConfigDTO} object based on this {@link SliderIconConfig} object.
     */
    @JsonIgnore
    public SliderIconConfigDTO toSliderIconConfigDTO(SliderIconConfig sliderIconConfig) {
        SliderIconConfigDTO sliderIconConfigDTO = new SliderIconConfigDTO();
        sliderIconConfigDTO.setId(sliderIconConfig.getId());
        sliderIconConfigDTO.setConfigName(sliderIconConfig.getConfigName());
        sliderIconConfigDTO.setNumberOfIcons(sliderIconConfig.getNumberOfIcons());
        List<SliderIconDetailDTO> sliderIconDetailDTOS = new ArrayList<>();
        for (SliderIconDetail sliderIconDetail : sliderIconConfig.getIcons()) {
            SliderIconDetailDTO sliderIconDetailDTO = sliderIconDetailService.toSliderIconDetailDTO(
                sliderIconDetail);
            sliderIconDetailDTOS.add(sliderIconDetailDTO);
        }
        sliderIconConfigDTO.setIconType(
            sliderIconConfig.getIcons().get(0).getPredefinedSliderIcon() != null ? "icon"
                : "image");
        sliderIconConfigDTO.setSliderIconDetailDTOS(sliderIconDetailDTOS);

        return sliderIconConfigDTO;
    }

    /**
     * Converts this {@link SliderIconConfigDTO} object to a {@link SliderIconConfig} object.
     *
     * @return A {@link SliderIconConfig} object based on this {@link SliderIconConfigDTO} object.
     */
    @JsonIgnore
    public SliderIconConfig newSliderIconConfig(SliderIconConfigDTO sliderIconConfigDTO,
        QuestionDTO questionDTO, Question question) {
        SliderIconConfig sliderIconConfig = new SliderIconConfig(
            sliderIconConfigDTO.getNumberOfIcons(), sliderIconConfigDTO.getConfigName());
        sliderIconConfigDao.merge(sliderIconConfig);
        if (Objects.equals(sliderIconConfigDTO.getIconType(), "icon")) {
            List<SliderIconDetail> sliderIconDetails = new ArrayList<>();
            for (SliderIconDetailDTO sliderIconDetailDTO : sliderIconConfigDTO.getSliderIconDetailDTOS()) {
                sliderIconDetails.add(
                    sliderIconDetailService.toSliderIconDetail(sliderIconDetailDTO,
                        sliderIconConfig));
            }
            sliderIconConfig.setIcons(sliderIconDetails);
        } else {
            List<SliderIconDetail> sliderIconDetails = new ArrayList<>();
            if (questionDTO.getId() == null) {
                questionDao.merge(question);
            }
            for (SliderIconDetailDTO sliderIconDetailDTO : sliderIconConfigDTO.getSliderIconDetailDTOS()) {

                sliderIconDetails.add(
                    sliderIconDetailService.toSliderIconDetailPersistImage(sliderIconDetailDTO,
                        sliderIconConfig));
            }
            sliderIconConfig.setIcons(sliderIconDetails);
        }
        sliderIconConfigDao.merge(sliderIconConfig);
        return sliderIconConfig;
    }

}
