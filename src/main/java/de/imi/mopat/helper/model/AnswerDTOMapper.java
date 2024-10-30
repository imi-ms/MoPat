package de.imi.mopat.helper.model;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.*;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.export.SliderIconDTO;
import de.imi.mopat.model.enumeration.BodyPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AnswerDTOMapper implements Function<Answer, AnswerDTO> {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(AnswerDTOMapper.class);
    
    @Autowired
    private ConfigurationDao configurationDao;
    
    @Autowired
    private ConditionDTOMapper conditionDTOMapper;


    @Override
    public AnswerDTO apply(Answer answer) {
        AnswerDTO answerDTO = new AnswerDTO();

        if (answer instanceof SelectAnswer) {
            mapSelectAnswer((SelectAnswer) answer, answerDTO);
        } else if (answer instanceof SliderFreetextAnswer) {
            mapSliderFreetextAnswer((SliderFreetextAnswer) answer, answerDTO);
        } else if (answer instanceof SliderAnswer) {
            mapSliderAnswer((SliderAnswer) answer, answerDTO);
        } else if (answer instanceof DateAnswer) {
            mapDateAnswer((DateAnswer) answer, answerDTO);
        } else if (answer instanceof NumberInputAnswer) {
            mapNumberInputAnswer((NumberInputAnswer) answer, answerDTO);
        } else if (answer instanceof ImageAnswer) {
            mapImageAnswer((ImageAnswer) answer, answerDTO);
        } else if (answer instanceof BodyPartAnswer) {
            mapBodyPartAnswer((BodyPartAnswer) answer, answerDTO);
        }

        answerDTO.setId(answer.getId());
        answerDTO.setIsEnabled(answer.getIsEnabled());
        answerDTO.setHasResponse(!answer.getResponses().isEmpty());
        answerDTO.setHasConditionsAsTrigger(!answer.getConditions().isEmpty());
        answerDTO.setConditions(answer.getConditions().stream()
                .map(conditionDTOMapper::apply)
                .collect(Collectors.toList()));
        answerDTO.setHasExportRule(!answer.getExportRules().isEmpty());

        return answerDTO;
    }

    private void mapSelectAnswer(SelectAnswer answer, AnswerDTO answerDTO) {
        answerDTO.setLocalizedLabel(new TreeMap<>(answer.getLocalizedLabel()));
        if (answer.getValue() != null) {
            answerDTO.setValue(answer.getValue());
        }
        answerDTO.setIsOther(answer.getIsOther());
        answerDTO.setCodedValue(answer.getCodedValue());
    }

    private void mapSliderAnswer(SliderAnswer answer, AnswerDTO answerDTO) {
        answerDTO.setMinValue(answer.getMinValue());
        answerDTO.setMaxValue(answer.getMaxValue());
        answerDTO.setVertical(answer.getVertical());
        answerDTO.setStepsize(formatStepsize(answer.getStepsize()));
        answerDTO.setLocalizedMinimumText(new TreeMap<>(answer.getLocalizedMinimumText()));
        answerDTO.setLocalizedMaximumText(new TreeMap<>(answer.getLocalizedMaximumText()));
        answerDTO.setShowValueOnButton(answer.getShowValueOnButton());
        answerDTO.setShowIcons(answer.getShowIcons());
        answerDTO.setIcons(answer.getIcons().stream()
                .map(icon -> new SliderIconDTO(icon.getIcon(), icon.getPosition(), answer.getId()))
                .collect(Collectors.toList()));
    }

    private void mapSliderFreetextAnswer(SliderFreetextAnswer answer, AnswerDTO answerDTO) {
        answerDTO.setMinValue(answer.getMinValue());
        answerDTO.setMaxValue(answer.getMaxValue());
        answerDTO.setVertical(answer.getVertical());
        answerDTO.setStepsize(formatStepsize(answer.getStepsize()));
        answerDTO.setLocalizedMinimumText(new TreeMap<>(answer.getLocalizedMinimumText()));
        answerDTO.setLocalizedMaximumText(new TreeMap<>(answer.getLocalizedMaximumText()));
        answerDTO.setLocalizedFreetextLabel(new TreeMap<>(answer.getLocalizedFreetextLabel()));
    }

    private void mapDateAnswer(DateAnswer answer, AnswerDTO answerDTO) {
        SimpleDateFormat dateFormat = Constants.DATE_FORMAT;
        Optional.ofNullable(answer.getStartDate())
                .ifPresent(startDate -> answerDTO.setStartDate(dateFormat.format(startDate)));
        Optional.ofNullable(answer.getEndDate())
                .ifPresent(endDate -> answerDTO.setEndDate(dateFormat.format(endDate)));
    }

    private void mapNumberInputAnswer(NumberInputAnswer answer, AnswerDTO answerDTO) {
        answerDTO.setMinValue(answer.getMinValue());
        answerDTO.setMaxValue(answer.getMaxValue());
        Optional.ofNullable(answer.getStepsize())
                .ifPresent(stepsize -> answerDTO.setStepsize(stepsize.toString()));
    }

    private void mapImageAnswer(ImageAnswer answer, AnswerDTO answerDTO) {
        String imagePath = configurationDao.getImageUploadPath() + "/question/" + answer.getImagePath();
        answerDTO.setImagePath(imagePath);
        try {
            //Navigate out of classpath root and WEB-INF
            String fileName = imagePath.substring(imagePath.lastIndexOf("/"));
            answerDTO.setImageBase64(StringUtilities.convertImageToBase64String(imagePath, fileName));
        } catch (IOException e) {
            LOGGER.error("Image of answer with id {} and path {} was not readable!", answer.getId(), answer.getImagePath());
        }
    }

    private void mapBodyPartAnswer(BodyPartAnswer answer, AnswerDTO answerDTO) {
        BodyPart bodyPart = answer.getBodyPart();
        answerDTO.setBodyPartPath(bodyPart.getPath());
        answerDTO.setBodyPartMessageCode(bodyPart.getMessageCode());
        answerDTO.setBodyPartImage(bodyPart.getImagePath());
    }

    private String formatStepsize(Double stepsize) {
        DecimalFormat decimalFormat = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        decimalFormat.setMaximumFractionDigits(340); //340 = DecimalFormat.DOUBLE_FRACTION_DIGITS
        return decimalFormat.format(stepsize);
    }
}