package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.StatisticDao;
import de.imi.mopat.model.Statistic;
import de.imi.mopat.model.dto.StatisticDTO;
import de.imi.mopat.utils.Helper;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class StatisticDTOValidatorTest {

    private static final Random random = new Random();
    @Autowired
    StatisticDTOValidator statisticDTOValidator;
    @Autowired
    StatisticDao statisticDao;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link StatisticDTOValidator#supports(java.lang.Class)} Valid input:
     * {@link StatisticDTO#class} Invalid input: Other classes than {@link StatisticDTO#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for SliderAnswer.class failed. SliderAnswerValidator didn't support SliderAnswer.class except it was expected to do.",
            statisticDTOValidator.supports(StatisticDTO.class));
        assertFalse(
            "Supports method for random class failed. SliderAnswerValidator supported that class except it wasn't expected to do.",
            statisticDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link StatisticDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)} Valid input: Instance of {@link BindingResult} and
     * instance of {@link StatisticDTO} where endDate follows startDate at times, both dates aren't
     * out of range of the earliest and latest date the statisticDao persists and the count needs to
     * be greater than zero and less than the count of days between startDate and endDate.<br>
     * Invalid input: Instance of {@link StatisticDTO} where endDate doesn't follow startDate at
     * times, both dates are out of range of earliest and latest date the statisticDao persists and
     * the count is greater than zero and less than the count of days between startDate and
     * endDate.<br>
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testValidate() {
        //clear table
        for (Statistic stat : statisticDao.getAllElements()) {
            statisticDao.remove(stat);
        }

        String message, testErrorMessage;
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));

        //Create statistic somewhere in the past 30 years
        Statistic statistic = new Statistic();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_YEAR, random.nextInt(365) + 1);
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - random.nextInt(30) - 1);
        statistic.setDate(calendar.getTime());
        statisticDao.merge(statistic);

        statistic = new Statistic();
        statistic.setDate(new Date());
        statisticDao.merge(statistic);

        Date latestDate = statisticDao.getLatestDate();
        Date earliestDate = statisticDao.getEarliestDate();

        StatisticDTO statisticDTO = new StatisticDTO();
        Long distance = latestDate.getTime() - earliestDate.getTime();

        do {
            statisticDTO.setStartDate(
                new Date(earliestDate.getTime() + random.nextInt(Math.abs(distance.intValue()))));
            statisticDTO.setEndDate(new Date(statisticDTO.getStartDate().getTime() + random.nextInt(
                Math.abs(distance.intValue()))));
        } while (statisticDTO.getEndDate().after(latestDate));

        Date startDate = statisticDTO.getStartDate();
        Date endDate = statisticDTO.getEndDate();

        int days = (int) TimeUnit.DAYS.convert(
            statisticDTO.getEndDate().getTime() - statisticDTO.getStartDate().getTime(),
            TimeUnit.MILLISECONDS) + 1;
        statisticDTO.setCount(random.nextInt(days) + 1);
        //valid instance
        statisticDTOValidator.validate(statisticDTO, result);
        assertFalse(
            "Validation of statisticDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //invalid instance
        //startDate out of range
        statisticDTO.setStartDate(new Date(earliestDate.getTime() - Math.abs(random.nextLong())));
        statisticDTOValidator.validate(statisticDTO, result);
        assertTrue(
            "Validation of statisticDTO failed for invalid instance with startDate out of range. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("statistic.error.startdateOutOfRange", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of statisticDTO failed for invalid instance with startDate out of range. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        statisticDTO.setStartDate(new Date(latestDate.getTime() + Math.abs(random.nextLong())));
        statisticDTOValidator.validate(statisticDTO, result);
        assertTrue(
            "Validation of statisticDTO failed for invalid instance with startDate out of range. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of statisticDTO failed for invalid instance with startDate out of range. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        statisticDTO.setStartDate(startDate);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        statisticDTO.setEndDate(new Date(earliestDate.getTime() - Math.abs(random.nextLong())));
        statisticDTOValidator.validate(statisticDTO, result);
        assertTrue(
            "Validation of statisticDTO failed for invalid instance with endDate out of range. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("statistic.error.enddateOutOfRange", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of statisticDTO failed for invalid instance with endDate out of range. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        statisticDTO.setStartDate(startDate);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        statisticDTO.setEndDate(new Date(latestDate.getTime() + Math.abs(random.nextLong())));
        statisticDTOValidator.validate(statisticDTO, result);
        assertTrue(
            "Validation of statisticDTO failed for invalid instance with endDate out of range. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of statisticDTO failed for invalid instance with endDate out of range. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        statisticDTO.setEndDate(endDate);
        statisticDTO.setCount(-1 * random.nextInt(Math.abs(random.nextInt())));
        statisticDTOValidator.validate(statisticDTO, result);
        assertTrue(
            "Validation of statisticDTO failed for invalid instance with negative count. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("statistic.error.enddateOutOfRange", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of statisticDTO failed for invalid instance with endDate out of range. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        statisticDTO.setStartDate(null);
        statisticDTO.setEndDate(endDate);
        statisticDTO.setCount(random.nextInt(days) + 1);
        statisticDTOValidator.validate(statisticDTO, result);
        assertFalse(
            "Validation of statisticDTO failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        statisticDTO.setStartDate(null);
        statisticDTO.setEndDate(null);
        statisticDTO.setCount(random.nextInt(days) + 1);
        statisticDTOValidator.validate(statisticDTO, result);
        assertFalse(
            "Validation of statisticDTO failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        statisticDTO.setStartDate(startDate);
        statisticDTO.setEndDate(null);
        statisticDTO.setCount(random.nextInt(days) + 1);
        statisticDTOValidator.validate(statisticDTO, result);
        assertFalse(
            "Validation of statisticDTO failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        statisticDTO.setEndDate(endDate);
        statisticDTO.setCount(days + 1);
        statisticDTOValidator.validate(statisticDTO, result);
        assertTrue(
            "Validation of statisticDTO failed for invalid instance with count greater than days. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("statistic.error.countGreaterThanDays", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of statisticDTO failed for invalid instance with count greater than days. The returned error message didn't match the expected one.",
            message, testErrorMessage);

    }
}
