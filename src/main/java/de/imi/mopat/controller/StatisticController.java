package de.imi.mopat.controller;

import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.StatisticDao;
import de.imi.mopat.model.dto.OneTimeStatisticDTO;
import de.imi.mopat.model.dto.StatisticDTO;
import de.imi.mopat.validator.OneTimeStatisticDTOValidator;
import de.imi.mopat.validator.StatisticDTOValidator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 */
@Controller
public class StatisticController {

    @Autowired
    private StatisticDao statisticDao;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private StatisticDTOValidator statisticDTOValidator;
    @Autowired
    private OneTimeStatisticDTOValidator oneTimeStatisticDTOValidator;
    @Autowired
    private QuestionnaireDao questionnaireDao;
    @Autowired
    private BundleDao bundleDao;
    @Autowired
    private EncounterDao encounterDao;

    /**
     * Controls the HTTP GET requests for the URL
     * <i>/statistic/onetimestatistic</i>. Sets to generate the model together
     * for one-time statistics
     *
     * @param model The model, which holds the information for the view.
     * @return The <i>/statistic/onetimestatistic</i> website.
     */
    @RequestMapping(value = "/statistic/onetimestatistic", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getOneTimeStatistic(final Model model) {

        OneTimeStatisticDTO oneTimeStatisticDTO = new OneTimeStatisticDTO(new Date(), new Date());
        model.addAttribute("oneTimeStatisticDTO", oneTimeStatisticDTO);
        model.addAttribute("bundles", bundleDao.getAllElements());
        model.addAttribute("questionnaires", questionnaireDao.getAllElements());
        model.addAttribute("patients", encounterDao.getAllCaseNumbers());
        return "statistic/onetimestatistic";
    }

    /**
     * Controls the HTTP POST requests for the URL
     * <i>/statistic/onetimestatistic"</i>. Provides the ability to request
     * current statistics.
     *
     * @param oneTimeStatisticDTO The forwarded {@link OneTimeStatisticDTO} object from the form.
     * @param result              The result for validation of the bundle object.
     * @param model               The model, which holds the information for the view.
     * @return Redirect to the <i>statistic/index</i> website.
     */
    @RequestMapping(value = "/statistic/onetimestatistic", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getOneTimeStatistics(
        @ModelAttribute("oneTimeStatisticDTO") @Valid final OneTimeStatisticDTO oneTimeStatisticDTO,
        final BindingResult result, final Model model) {
        oneTimeStatisticDTOValidator.validate(oneTimeStatisticDTO, result);
        model.addAttribute("bundles", bundleDao.getAllElements());
        model.addAttribute("questionnaires", questionnaireDao.getAllElements());
        model.addAttribute("patients", encounterDao.getAllCaseNumbers());
        if (result.hasErrors()) {
            return "statistic/onetimestatistic";
        }
        oneTimeStatisticDTO.setEncounterCountByBundleInInterval(
            encounterDao.getEncounterCountByBundleInInterval(oneTimeStatisticDTO.getBundleId(),
                oneTimeStatisticDTO.getBundleStartDate(), oneTimeStatisticDTO.getBundleEndDate()));
        oneTimeStatisticDTO.setEncounterCountByCaseNumberInInterval(
            encounterDao.getEncounterCountByCaseNumberInInterval(oneTimeStatisticDTO.getPatientId(),
                oneTimeStatisticDTO.getPatientStartDate(),
                oneTimeStatisticDTO.getPatientEndDate()));
        oneTimeStatisticDTO.setEncounterCountByCaseNumberByBundleInInterval(
            encounterDao.getEncounterCountByCaseNumberByBundleInInterval(
                oneTimeStatisticDTO.getBundlePatientBundleId(),
                oneTimeStatisticDTO.getBundlePatientPatientId(),
                oneTimeStatisticDTO.getBundlePatientStartDate(),
                oneTimeStatisticDTO.getBundlePatientEndDate()));

        return "statistic/onetimestatistic";
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/statistic/index</i>.
     *
     * @param model The model, which holds the information for the view.
     * @return The <i>statistic/index</i> website.
     */
    @RequestMapping(value = "/statistic/index", method = RequestMethod.GET)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getStatistic(final Model model) {

        Date minDate = statisticDao.getEarliestDate();
        Date maxDate = statisticDao.getLatestDate();
        StatisticDTO statisticDTO = new StatisticDTO(minDate, maxDate);
        // Add start- and end-date to the list of displayed dates
        if (minDate != null && maxDate != null) {
            List<Date> displayedDates = new ArrayList<>();
            displayedDates.add(statisticDTO.getStartDate());
            displayedDates.add(statisticDTO.getEndDate());
            statisticDTO.setStatistics(statisticDao.getStatisticsByDates(displayedDates));
            Collections.sort(statisticDTO.getStatistics());
        }
        model.addAttribute("statisticDTO", statisticDTO);
        return "statistic/index";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/statistic/index"</i>. Provides the ability to
     * request statistics
     *
     * @param statisticDTO The forwarded {@link StatisticDTO} object from the form.
     * @param result       The result for validation of the bundle object.
     * @param model        The model, which holds the information for the view.
     * @return Redirect to the <i>statistic/index</i> website.
     */
    @RequestMapping(value = "/statistic/index", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getStatistics(
        @ModelAttribute("statisticDTO") @Valid final StatisticDTO statisticDTO,
        final BindingResult result, final Model model) {
        statisticDTOValidator.validate(statisticDTO, result);
        statisticDTO.setMinDate(statisticDao.getEarliestDate());
        statisticDTO.setMaxDate(statisticDao.getLatestDate());

        if (result.hasErrors()) {
            return "statistic/index";
        }

        List<Date> displayedDates = new ArrayList<>();
        // Add start- and enddate to the list of displayed dates
        Date startdate = statisticDTO.getStartDate();
        Date enddate = statisticDTO.getEndDate();
        displayedDates.add(startdate);
        int count = statisticDTO.getCount();
        if (count != 1) {
            displayedDates.add(enddate);

            // Evaluate the amount of days between start- and enddate
            int days = (int) TimeUnit.DAYS.convert(enddate.getTime() - startdate.getTime(),
                TimeUnit.MILLISECONDS) + 1;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startdate);
            boolean countEven = (count % 2 == 0);
            // Substract the start- and enddate from the amount of remaining
            // dates to put on the list of displayed dates
            int datesToFill = count - 2;
            // If the count of dates is odd
            if (!countEven) {
                // Add the date in the middle of start- and enddate to the
                // list of displayed dates
                calendar.add(Calendar.DAY_OF_YEAR, days / 2);
                displayedDates.add(calendar.getTime());
                datesToFill--;
            }
            if (datesToFill > 0) {
                // Evaluate the average difference between two displayed dates
                double averageDifference = (double) (days) / (double) (count - 1);
                for (int i = 1; i <= datesToFill / 2; i++) {
                    // Fill the list of displayed dates from both sides
                    calendar.setTime(startdate);
                    calendar.add(Calendar.DAY_OF_YEAR, (int) (i * averageDifference));
                    displayedDates.add(calendar.getTime());
                    calendar.setTime(enddate);
                    calendar.add(Calendar.DAY_OF_YEAR, (int) -(i * averageDifference));
                    displayedDates.add(calendar.getTime());
                }
            }
        }
        statisticDTO.setStatistics(statisticDao.getStatisticsByDates(displayedDates));
        Collections.sort(statisticDTO.getStatistics());
        model.addAttribute("statisticDTO", statisticDTO);

        return "statistic/index";
    }
}
