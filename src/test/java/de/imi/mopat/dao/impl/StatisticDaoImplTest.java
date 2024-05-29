package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.StatisticDao;
import de.imi.mopat.model.Statistic;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class StatisticDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    StatisticDao testStatisticDao;

    /**
     * Test of {@link StatisticDaoImpl#getEarliestDate}.<br> Valid input: random number of
     * {@link Statistics} with different dates
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetEarliestDate() {
        clearTable();
        assertNull(
            "The getting earliest date was not null although there was no entry in the table",
            testStatisticDao.getEarliestDate());
        int countStatistics = random.nextInt(25) + 1;
        long testDateInMillis = System.currentTimeMillis() - random.nextInt(250) * 86400000L;
        for (int i = 0; i < countStatistics; i++) {
            Statistic testStatistic = new Statistic();
            testStatistic.setDate(new Date(testDateInMillis + (i + 1) * 86400000L));
            testStatisticDao.merge(testStatistic);
        }
        Calendar testCalendar = Calendar.getInstance();
        testCalendar.setTimeInMillis(testDateInMillis);
        testCalendar.set(Calendar.MILLISECOND, 0);
        testCalendar.set(Calendar.SECOND, 0);
        testCalendar.set(Calendar.MINUTE, 0);
        testCalendar.set(Calendar.HOUR_OF_DAY, 0);
        Date testDate = testCalendar.getTime();

        Statistic testStatistic = new Statistic();
        testStatistic.setDate(testDate);
        testStatisticDao.merge(testStatistic);
        assertEquals("The getting Date was not the expected one", testDate,
            testStatisticDao.getEarliestDate());
    }

    /**
     * Test of {@link StatisticDaoImpl#getLatestDate}.<br> Valid input: random number of
     * {@link Statistics} with different dates
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetLatestDate() {
        clearTable();
        assertNull("The getting latest date was not null although there was no entry in the table",
            testStatisticDao.getLatestDate());
        int countStatistics = random.nextInt(25) + 1;
        long testDateInMillis = System.currentTimeMillis() + random.nextInt(250) * 86400000L;
        for (int i = 0; i < countStatistics; i++) {
            Statistic testStatistic = new Statistic();
            testStatistic.setDate(new Date(testDateInMillis - (i + 1) * 86400000L));
            testStatisticDao.merge(testStatistic);
        }
        Calendar testCalendar = Calendar.getInstance();
        testCalendar.setTimeInMillis(testDateInMillis);
        testCalendar.set(Calendar.MILLISECOND, 0);
        testCalendar.set(Calendar.SECOND, 0);
        testCalendar.set(Calendar.MINUTE, 0);
        testCalendar.set(Calendar.HOUR_OF_DAY, 0);
        Date testDate = testCalendar.getTime();

        Statistic testStatistic = new Statistic();
        testStatistic.setDate(testDate);
        testStatisticDao.merge(testStatistic);
        assertEquals("The getting Date was not the expected one", testDate,
            testStatisticDao.getLatestDate());
    }

    /**
     * Test of {@link StatisticDaoImpl#getStatisticsByDates}.<br> Valid input: random list of
     * different dates
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetStatisticsByDates() {
        clearTable();
        int countStatistics = random.nextInt(50) + 1;
        long testDateInMillis = System.currentTimeMillis() - random.nextInt(250) * 86400000L;
        List<Statistic> testStatistics = new ArrayList<>();
        List<Date> testDates = new ArrayList<>();
        for (int i = 0; i < countStatistics; i++) {
            Calendar testCalendar = Calendar.getInstance();
            testCalendar.setTimeInMillis(testDateInMillis + (i + 1) * 86400000L);
            testCalendar.set(Calendar.MILLISECOND, 0);
            testCalendar.set(Calendar.SECOND, 0);
            testCalendar.set(Calendar.MINUTE, 0);
            testCalendar.set(Calendar.HOUR_OF_DAY, 0);
            Date testDate = testCalendar.getTime();
            Statistic testStatistic = new Statistic();
            testStatistic.setDate(testDate);
            testStatisticDao.merge(testStatistic);
            if (random.nextBoolean()) {
                testDates.add(testDate);
                testStatistics.add(testStatistic);
            }
        }
        for (int i = 0; i < testDates.size(); i++) {
            assertEquals("The getting list of Statistics was not the expected one",
                testStatistics.get(i).getId(),
                testStatisticDao.getStatisticsByDates(testDates).get(i).getId());
        }
    }

    /**
     * Deletes all {@link Statistic Statistics} from the database.
     */
    private void clearTable() {
        List<Statistic> allStatistics = testStatisticDao.getAllElements();
        for (Statistic statistic : allStatistics) {
            testStatisticDao.remove(statistic);
        }
    }
}
