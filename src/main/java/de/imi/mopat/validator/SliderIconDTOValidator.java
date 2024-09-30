package de.imi.mopat.validator;

import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.export.SliderIconDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.util.ArrayList;
import java.util.List;


@Component
public class SliderIconDTOValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AnswerDao answerDao;

    @Override
    public boolean supports(final Class<?> type) {
        return SliderIconDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        validator.validate(target, errors);

        SliderIconDTO sliderIconDTO = (SliderIconDTO) target;

        try {
            String icon = sliderIconDTO.getPredefinedSliderIcon();
            Integer position = sliderIconDTO.getIconPosition();

            if (icon == null) {
                rejectValue(errors, "sliderIcon.validator.missingIconValue", "predefinedSliderIcon");
            }

            if (position == null) {
                rejectValue(errors, "sliderIcon.validator.missingPosition", "iconPosition");
            }
        } catch (Exception ex) {

        }
    }

    public void validateWithAnswer(final Object target, final Errors errors,
        final AnswerDTO answerDTO) {
        validator.validate(target, errors);

        SliderIconDTO sliderIconDTO = (SliderIconDTO) target;

        try {
            String icon = sliderIconDTO.getPredefinedSliderIcon();
            Integer position = sliderIconDTO.getIconPosition();

            if (icon == null || icon.isEmpty()) {
                rejectValue(errors, "sliderIcon.validator.missingIconValue", "icon");
            }

            if (position == null) {
                rejectValue(errors, "sliderIcon.validator.missingPosition", "position");
            }

            List<SliderIconDTO> remainingSliderIconDTOs = new ArrayList<>(answerDTO.getIcons());
            remainingSliderIconDTOs.remove(sliderIconDTO);

            for (SliderIconDTO sliderIconDTOInList : remainingSliderIconDTOs) {
                if (sliderIconDTOInList.getIconPosition() == position) {
                    rejectValue(errors, "sliderIcon.validator.positionAlreadyInUse", "position");
                }
            }
        } catch (Exception ex) {

        }
    }


    private void rejectValue(final Errors errors, final String messageCode, final String field) {
        String message = messageSource.getMessage(messageCode, new Object[]{},
            LocaleContextHolder.getLocale());

        errors.rejectValue(field, MoPatValidator.ERRORCODE_ERRORMESSAGE, message);
    }
}
