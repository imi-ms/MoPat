package de.imi.mopat.validator;

import de.imi.mopat.model.dto.OneTimeStatisticDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The validator for {@link OneTimeStatisticDTO oneTimeStatisticDTO} objects.
 */
@Component
public class OneTimeStatisticDTOValidator implements Validator {

    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(Class<?> type) {
        return OneTimeStatisticDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object o, final Errors errors) {

        OneTimeStatisticDTO oneTimeStatisticDTO = (OneTimeStatisticDTO) o;
        if (oneTimeStatisticDTO.getBundleEndDate() != null
            && oneTimeStatisticDTO.getBundleStartDate() != null
            && oneTimeStatisticDTO.getBundleEndDate()
            .before(oneTimeStatisticDTO.getBundleStartDate())) {
            errors.rejectValue("bundleStartDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("statistic.error" + ".enddateBeforeStartdate",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }
        if (oneTimeStatisticDTO.getPatientEndDate() != null
            && oneTimeStatisticDTO.getPatientStartDate() != null
            && oneTimeStatisticDTO.getPatientEndDate()
            .before(oneTimeStatisticDTO.getPatientStartDate())) {
            errors.rejectValue("patientStartDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("statistic.error" + ".enddateBeforeStartdate",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }
        if (oneTimeStatisticDTO.getBundlePatientEndDate() != null
            && oneTimeStatisticDTO.getBundlePatientStartDate() != null
            && oneTimeStatisticDTO.getBundlePatientEndDate()
            .before(oneTimeStatisticDTO.getBundlePatientStartDate())) {
            errors.rejectValue("bundlePatientStartDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("statistic.error" + ".enddateBeforeStartdate",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }
    }

}
