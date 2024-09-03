package de.imi.mopat.helper.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.PredefinedSliderIconDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.SliderIconConfigDao;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.SliderIconConfig;
import de.imi.mopat.model.SliderIconDetail;
import de.imi.mopat.model.UserSliderIcon;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.model.dto.SliderIconConfigDTO;
import de.imi.mopat.model.dto.SliderIconDetailDTO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SliderIconDetailService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        SliderIconConfig.class);

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private SliderIconConfigDao sliderIconConfigDao;

    @Autowired
    private PredefinedSliderIconDao predefinedSliderIconDao;

    @Autowired
    private QuestionDao questionDao;


    /**
     * Converts this {@link SliderIconDetail} object to an {@link SliderIconDetailDTO} object.
     *
     * @return An {@link SliderIconDetailDTO} object based on this {@link SliderIconDetail} object.
     */
    @JsonIgnore
    public SliderIconDetailDTO toSliderIconDetailDTO(SliderIconDetail sliderIconDetail) {
        SliderIconDetailDTO sliderIconDetailDTO;
        if (sliderIconDetail.getPredefinedSliderIcon() != null) {
            sliderIconDetailDTO = new SliderIconDetailDTO(sliderIconDetail.getId(),
                sliderIconDetail.getIconPosition(), sliderIconDetail.getSliderIconConfig().getId(),
                sliderIconDetail.getPredefinedSliderIcon().getIconName());
        } else {
            sliderIconDetailDTO = new SliderIconDetailDTO(sliderIconDetail.getId(),
                sliderIconDetail.getIconPosition(), sliderIconDetail.getSliderIconConfig().getId());
            if (sliderIconDetail.getUserSliderIcon() != null) {
                String realPath = configurationDao.getImageUploadPath() + "/sliderIconConfig/"
                    + sliderIconDetail.getUserSliderIcon().getIconPath();
                try {
                    String base64 = StringUtilities.convertImageToBase64String(realPath,
                        sliderIconDetail.getUserSliderIcon().getIconPath().split("/")[1]);
                    sliderIconDetailDTO.setUserIconBase64(base64);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return sliderIconDetailDTO;
    }

    /**
     * Converts this {@link SliderIconDetailDTO} object to an {@link SliderIconDetail} object.
     *
     * @return An {@link SliderIconDetail} object based on this {@link SliderIconDetailDTO} object.
     */
    @JsonIgnore
    public SliderIconDetail toSliderIconDetail(SliderIconDetailDTO sliderIconDetailDTO,
        SliderIconConfig sliderIconConfig) {
        SliderIconDetail sliderIconDetail = new SliderIconDetail(
            sliderIconDetailDTO.getIconPosition());
        sliderIconDetail.setPredefinedSliderIcon(
            predefinedSliderIconDao.getIconByName(sliderIconDetailDTO.getPredefinedSliderIcon()));
        sliderIconDetail.setSliderIconConfig(sliderIconConfig);
        return sliderIconDetail;
    }

    /**
     * Converts this {@link SliderIconDetailDTO} object to an {@link SliderIconDetail} object.
     * It also persists image icon to the image storage path.
     * @return An {@link SliderIconDetail} object based on this {@link SliderIconDetailDTO} object.
     */
    @JsonIgnore
    public SliderIconDetail toSliderIconDetailPersistImage(SliderIconDetailDTO sliderIconDetailDTO,
        SliderIconConfig sliderIconConfig, QuestionDTO questionDTO, Question question) {
        SliderIconDetail sliderIconDetail = new SliderIconDetail(
            sliderIconDetailDTO.getIconPosition());

        String storagePath;
        if (!sliderIconDetailDTO.getUserIcon().isEmpty()) {
            String imageExtension = FilenameUtils.getExtension(
                sliderIconDetailDTO.getUserIcon().getOriginalFilename());
            String imagePath = (configurationDao.getImageUploadPath() + "/sliderIconConfig/"
                + sliderIconConfig.getId());

            // Check if the upload dir exists. If not, create it
            File uploadDir = new File(imagePath);
            if (!uploadDir.isDirectory()) {
                uploadDir.mkdirs();
            }
            // Set the upload filename to question and its ID
            File uploadFile = new File(imagePath,
                "config" + sliderIconConfig.getId() + "_" + sliderIconDetailDTO.getIconPosition() + "."
                    + imageExtension);
            try {
                // Write the image to disk
                BufferedImage uploadImage = ImageIO.read(
                    sliderIconDetailDTO.getUserIcon().getInputStream());
                ImageIO.write(uploadImage, imageExtension, uploadFile);
            } catch (IOException ex) {
                if (questionDTO.getId() == null) {
                    questionDao.remove(question);
                }
            }
            // Store the full storage path with name and extension
            storagePath = sliderIconConfig.getId() + "/config" + sliderIconConfig.getId() + "_"
                + sliderIconDetailDTO.getIconPosition() + "." + imageExtension;
        } else {
            // If the image has not changed use the old image path
            storagePath = null;
        }

        sliderIconDetail.setUserSliderIcon(new UserSliderIcon(storagePath));
        sliderIconDetail.setSliderIconConfig(sliderIconConfig);
        return sliderIconDetail;
    }

}
