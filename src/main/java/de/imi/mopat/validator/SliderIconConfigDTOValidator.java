package de.imi.mopat.validator;

import de.imi.mopat.dao.SliderIconConfigDao;
import de.imi.mopat.model.dto.SliderIconConfigDTO;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;


@Component
public class SliderIconConfigDTOValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SliderIconConfigDao sliderIconConfigDao;

    @Override
    public boolean supports(final Class<?> type) {
        return SliderIconConfigDTOValidator.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        validator.validate(target, errors);

        SliderIconConfigDTO sliderIconConfigDTO = (SliderIconConfigDTO) target;

        try {
            if(Objects.equals(sliderIconConfigDTO.getConfigName(), "")){
                errors.rejectValue("configName", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("sliderIconConfig.validator" + ".configNameNotNull",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }

            if(sliderIconConfigDao.getElementByName(sliderIconConfigDTO.getConfigName())!= null){
                errors.rejectValue("configName", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("sliderIconConfig.validator" + ".configNameNotUnique",
                        new Object[]{}, LocaleContextHolder.getLocale()));
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
