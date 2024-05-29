package de.imi.mopat.validator;

import de.imi.mopat.dao.StatisticDao;
import de.imi.mopat.model.dto.StatisticDTO;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The validator for {@link StatisticDTO statisticDTO} objects.
 */
@Component
public class StatisticDTOValidator implements Validator {

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private StatisticDao statisticDao;

    @Override
    public boolean supports(final Class<?> type) {
        return StatisticDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        Date earliestDate = statisticDao.getEarliestDate();
        Date latestDate = statisticDao.getLatestDate();

        StatisticDTO statisticDTO = (StatisticDTO) o;
        Date startDate = statisticDTO.getStartDate();
        Date endDate = statisticDTO.getEndDate();

        if (startDate != null && (startDate.before(earliestDate) || startDate.after(latestDate))) {
            errors.rejectValue("startDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("statistic.error" + ".startdateOutOfRange", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        if (endDate != null && (endDate.before(earliestDate) || endDate.after(latestDate))) {
            errors.rejectValue("endDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("statistic.error" + ".enddateOutOfRange", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        if (startDate != null && endDate != null && startDate.after(endDate)) {
            errors.rejectValue("startDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("statistic.error" + ".enddateBeforeStartdate",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }

        if (statisticDTO.getCount() < 1) {
            errors.rejectValue("count", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("statistic.error" + ".countMustBeGreaterThanZero",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }

        if (startDate != null && endDate != null) {
            int days = (int) TimeUnit.DAYS.convert(endDate.getTime() - startDate.getTime(),
                TimeUnit.MILLISECONDS) + 1;
            if (statisticDTO.getCount() > days) {
                errors.rejectValue("count", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("statistic.error" + ".countGreaterThanDays",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }
    }

}
