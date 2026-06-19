package de.imi.mopat.validator;

import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.dto.EncounterScheduledApiRequestDTO;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import java.util.Calendar;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The validator for {@link de.imi.mopat.model.dto.EncounterScheduledApiRequestDTO} objects.
 */
@Component
public class EncounterScheduledApiRequestDTOValidator implements Validator {

    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(final Class<?> type) {
        return EncounterScheduledApiRequestDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object target, Errors errors) {

        EncounterScheduledApiRequestDTO dto = (EncounterScheduledApiRequestDTO) target;

        if (dto.getCaseNumber() == null) {
            errors.rejectValue("caseNumber", "encounterScheduledApi.caseNumber.required",
                messageSource.getMessage("encounterScheduledApi.validate.caseNumber.required",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }
        if (dto.getBundleId() == null) {
            errors.rejectValue("bundleId", "encounterScheduledApi.bundleId.required",
                messageSource.getMessage("encounterScheduledApi.validate.bundleId.required",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }
        if (dto.getClinicId() == null) {
            errors.rejectValue("clinicId", "encounterScheduledApi.clinicId.required",
                messageSource.getMessage("encounterScheduledApi.validate.clinicId.required",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }
        if (dto.getEmail() != null && !dto.getEmail().matches(
            "[A-Za-z0-9.!#$%&'*+\\-/=?^_`{|}~]+@[A-Za-z0-9.!#$%&'*+\\-/=?^_`{|}~]+\\.[A-Za-z]{2,}")) {
            errors.rejectValue("email", "encounterScheduledApi.invalidEmail",
                messageSource.getMessage("encounterScheduledApi.validate.invalidEmail",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }
        if (dto.getStartDate() == null) {
            errors.rejectValue("startDate", "encounterScheduledApi.startDate.required",
                messageSource.getMessage("encounterScheduledApi.validate.startDate.required",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }
        if (dto.getEncounterScheduledSerialType() == null) {
            errors.rejectValue("encounterScheduledSerialType", "encounterScheduledApi.serialType.required",
                messageSource.getMessage("encounterScheduledApi.validate.serialType.required",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }
        Date now = new Date();
        // Generate date today at midnight
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        Date today = calendar.getTime();

        if (dto.getStartDate() != null
            && dto.getStartDate().getTime() < today.getTime()) {
            errors.rejectValue("startDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage(
                    "encounterScheduledApi.validate.startEndDate.CanNotBeInThePast", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        if (dto.getEndDate() != null
            && dto.getEndDate().getTime() < today.getTime()) {
            errors.rejectValue("endDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage(
                    "encounterScheduledApi.validate.startEndDate.CanNotBeInThePast", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        if (dto.getEndDate() != null
            && dto.getEndDate().getTime() < dto.getStartDate().getTime()) {
            errors.rejectValue("endDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage(
                    "encounterScheduledApi.validate.startEndDate.enddateBiggerThanStartdate", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }
    }


}
