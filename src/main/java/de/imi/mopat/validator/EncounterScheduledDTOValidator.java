package de.imi.mopat.validator;

import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The validator for {@link EncounterScheduled} objects.
 */
@Component
public class EncounterScheduledDTOValidator implements Validator {

    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(final Class<?> type) {
        return EncounterScheduledDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {

        Date now = new Date();
        // Generate date today at midnight
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        Date today = calendar.getTime();

        // [bt] first, let the standard validator validate the target object with respect to it's JSR-303 constraints (jakarta.validation.constraints annotations)
        // [bt] now it's my time to validate the more complex stuff
        EncounterScheduledDTO encounterScheduledDTO = (EncounterScheduledDTO) target;

        if (encounterScheduledDTO.getStartDate() != null
            && encounterScheduledDTO.getStartDate().getTime() < today.getTime()) {
            errors.rejectValue("startDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage(
                    "encounterScheduled.validator" + ".startdateCanNotBeInThePast", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        if (encounterScheduledDTO.getEncounterScheduledSerialType()
            .equals(EncounterScheduledSerialType.REPEATEDLY)
            || encounterScheduledDTO.getEncounterScheduledSerialType()
            .equals(EncounterScheduledSerialType.WEEKLY)
            || encounterScheduledDTO.getEncounterScheduledSerialType()
            .equals(EncounterScheduledSerialType.MONTHLY)) {
            if (encounterScheduledDTO.getStartDate() == null) {
                errors.rejectValue("startDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage(
                        "encounterScheduled" + ".validator" + ".startDateEmpty", new Object[]{},
                        LocaleContextHolder.getLocale()));
            } else if (encounterScheduledDTO.getEndDate() == null) {
                errors.rejectValue("endDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("encounterScheduled" + ".validator" + ".enddateEmpty",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            } else if (encounterScheduledDTO.getEndDate().before(today)) {
                errors.rejectValue("endDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage(
                        "encounterScheduled" + ".validator" + ".enddateCanNotBeInThePast",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            } else if (
                !encounterScheduledDTO.getEndDate().after(encounterScheduledDTO.getStartDate())
                    || encounterScheduledDTO.getEndDate().equals(today)) {
                errors.rejectValue("endDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage(
                        "encounterScheduled" + ".validator" + ".enddateMustBeAfterStartdate",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
            if (encounterScheduledDTO.getEncounterScheduledSerialType()
                .equals(EncounterScheduledSerialType.REPEATEDLY) && (
                encounterScheduledDTO.getRepeatPeriod() == null
                    || encounterScheduledDTO.getRepeatPeriod() <= 0)) {
                errors.rejectValue("repeatPeriod", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage(
                        "encounterScheduled" + ".validator" + ".repeatPeriodGreaterThanZero",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }

        if (!encounterScheduledDTO.getReplyMail().isEmpty()) {
            if (!encounterScheduledDTO.getReplyMails()
                .get(encounterScheduledDTO.getBundleDTO().getId())
                .contains(encounterScheduledDTO.getReplyMail())) {
                errors.rejectValue("replyMail", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage(
                        "encounterScheduled" + ".validator" + ".invalidReplyMail", new Object[]{},
                        LocaleContextHolder.getLocale()));
            }
        }
    }
}
