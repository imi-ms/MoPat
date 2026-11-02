package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.StatisticDao;
import de.imi.mopat.model.Statistic;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.ZoneId;
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
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
@Transactional("myTxManagerMoPat")
public class StatisticDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    StatisticDao testStatisticDao;
    @PersistenceContext(unitName = "MoPat")
    private EntityManager entityManager;

    /**
     * Test of {@link StatisticDaoImpl#getEarliestDate}.<br> Valid input: random number of
     * {@link Statistics} with different dates
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetEarliestDate() {
        clearTable();

        LocalDate base = LocalDate.of(2025, 11, 28);
        java.util.Date expectedDate = java.sql.Timestamp.valueOf(base.atTime(12, 0));

        Statistic earliest = new Statistic();
        earliest.setDate(expectedDate);
        testStatisticDao.merge(earliest);

        for (int i = 1; i <= 5; i++) {
            Statistic s = new Statistic();
            s.setDate(java.sql.Timestamp.valueOf(base.plusDays(i).atTime(12, 0)));
            testStatisticDao.merge(s);
        }

        java.util.Date actualFromDb = testStatisticDao.getEarliestDate();
        String actualStr = new java.sql.Date(actualFromDb.getTime()).toString();
        String expectedStr = "2025-11-28";

        assertEquals("The getting Date was not the expected one", expectedStr, actualStr);
    }

    /**
     * Test of {@link StatisticDaoImpl#getLatestDate}.<br> Valid input: random number of
     * {@link Statistics} with different dates
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetLatestDate() {
        clearTable();

        LocalDate base = LocalDate.of(2025, 11, 28);
        java.util.Date expectedDate = java.sql.Timestamp.valueOf(base.atTime(12, 0));

        Statistic earliest = new Statistic();
        earliest.setDate(expectedDate);
        testStatisticDao.merge(earliest);

        for (int i = 1; i <= 5; i++) {
            Statistic s = new Statistic();
            s.setDate(java.sql.Timestamp.valueOf(base.minusDays(i).atTime(12, 0)));
            testStatisticDao.merge(s);
        }

        java.util.Date actualFromDb = testStatisticDao.getLatestDate();
        String actualStr = new java.sql.Date(actualFromDb.getTime()).toString();
        String expectedStr = "2025-11-28";

        assertEquals("The getting Date was not the expected one", expectedStr, actualStr);
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
    @Transactional
    protected void clearTable() {
        List<Statistic> allStatistics = testStatisticDao.getAllElements();
        for (Statistic statistic : allStatistics) {
            testStatisticDao.remove(statistic);
        }
        entityManager.flush();
    }
}
