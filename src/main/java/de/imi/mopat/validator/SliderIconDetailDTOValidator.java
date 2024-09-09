package de.imi.mopat.validator;

import de.imi.mopat.dao.SliderIconConfigDao;
import de.imi.mopat.model.dto.SliderIconConfigDTO;
import de.imi.mopat.model.dto.SliderIconDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;


@Component
public class SliderIconDetailDTOValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(final Class<?> type) {
        return SliderIconDetailDTOValidator.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {


    }

    public void validateWithType(final Object target, final Errors errors, String iconType) {
        validator.validate(target, errors);

        SliderIconDetailDTO sliderIconDetailDTO = (SliderIconDetailDTO) target;

        if (Objects.equals(iconType, "icon")) {
            try {
                String icon = sliderIconDetailDTO.getPredefinedSliderIcon();
                Integer position = sliderIconDetailDTO.getIconPosition();

                if (icon == null) {
                    rejectValue(errors, "sliderIcon.validator.missingIconValue", "predefinedSliderIcon");
                }

                if (position == null) {
                    rejectValue(errors, "sliderIcon.validator.missingPosition", "iconPosition");
                }
            } catch (Exception ex) {

            }
        } else if (Objects.equals(iconType, "image")) {
            try {
                MultipartFile icon = sliderIconDetailDTO.getUserIcon();
                Integer position = sliderIconDetailDTO.getIconPosition();

                if (Objects.equals(icon.getOriginalFilename(), "")) {
                    rejectValue(errors, "sliderIcon.validator.missingIconValue", "userIcon");
                }

                if (position == null) {
                    rejectValue(errors, "sliderIcon.validator.missingPosition", "iconPosition");
                }
            } catch (Exception ex) {

            }
        }

    }

    private void rejectValue(final Errors errors, final String messageCode, final String field) {
        String message = messageSource.getMessage(messageCode, new Object[]{},
                LocaleContextHolder.getLocale());

        errors.rejectValue(field, MoPatValidator.ERRORCODE_ERRORMESSAGE, message);
    }
}
