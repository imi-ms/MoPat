package de.imi.mopat.validator;

import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.model.dto.ClinicDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The validator for {@link ClinicDTO ClinicDTO} objects.
 */
@Component
public class ClinicDTOValidator implements Validator {

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ClinicDao clinicDao;

    @Override
    public boolean supports(final Class<?> type) {
        return ClinicDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        ClinicDTO clinicDTO = (ClinicDTO) o;

        if (!clinicDTO.getName().isEmpty() && clinicDTO.getName().trim().isEmpty()) {
            clinicDTO.setName("");
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("clinic.error.nameIsEmpty", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        if (!clinicDTO.getName().matches("^[\\p{L}0-9\\s\\-_.:()\\[\\]!+?]+$")) {
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("clinic.error.nameContainsSpecialCharacters",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }

        if (clinicDTO.getName().matches("^[\\p{L}0-9\\s\\-_.:()\\[\\]!+?]+$")
            && !clinicDao.isClinicNameUnused(clinicDTO.getName(), clinicDTO.getId())) {
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("clinic.error.nameInUse", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }
    }
}