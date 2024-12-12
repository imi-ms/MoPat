package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.EncounterScheduledTest;
import java.util.ArrayList;
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
public class EncounterScheduledDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    EncounterScheduledDao testEncounterScheduledDao;
    @Autowired
    BundleDao bundleDao;
    @Autowired
    ClinicDao clinicDao;

    /**
     * Test of {@link EncounterScheduledDaoImpl#getAllElements}.<br> Valid input: random number of
     * valid {@link EncounterScheduled}
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetAllElements() {
        clearTable();
        int countEncounterScheduled = random.nextInt(25) + 1;
        List<EncounterScheduled> testList = new ArrayList<>();
        for (int i = 0; i < countEncounterScheduled; i++) {
            EncounterScheduled testEncounterScheduled = EncounterScheduledTest.getNewValidEncounterScheduled();
            Bundle testBundle = testEncounterScheduled.getBundle();
            Clinic testClinic = testEncounterScheduled.getClinic();
            clinicDao.merge(testClinic);
            bundleDao.merge(testBundle);
            testEncounterScheduledDao.merge(testEncounterScheduled);
            testList.add(testEncounterScheduled);
        }
        assertEquals("The getting list of EncounterScheduleds was not the expected one", testList,
            testEncounterScheduledDao.getAllElements());
    }

    /**
     * Test of {@link EncounterScheduledDaoImpl#getEncounterScheduledByDate}.<br> Valid input:
     * random valid {@link EncounterScheduled} with the given date in range or not in range
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetEncounterScheduledByDate() {
        clearTable();
        long dateInMillis;
        if (random.nextBoolean()) {
            dateInMillis =
                System.currentTimeMillis() + (long) Math.abs(random.nextInt(10000)) * 86400000L;
        } else {
            dateInMillis =
                System.currentTimeMillis() - (long) Math.abs(random.nextInt(10000)) * 86400000L;
        }
        int countEncounterScheduled = random.nextInt(25) + 1;
        List<EncounterScheduled> inRangeEncounterScheduled = new ArrayList<>();
        List<EncounterScheduled> outRangeEncounterScheduled = new ArrayList<>();
        for (int i = 0; i < countEncounterScheduled; i++) {
            if (random.nextBoolean()) {
                inRangeEncounterScheduled.add(
                    EncounterScheduledTest.getNewValidEncounterScheduledWithDate(
                        new Date(dateInMillis)));
            } else {
                Date date;
                if (random.nextBoolean()) {
                    date = new Date(
                        dateInMillis + (long) (Math.abs(random.nextInt(500)) + 12) * 86400000L);
                } else {
                    date = new Date(
                        dateInMillis - (long) (Math.abs(random.nextInt(500)) + 12) * 86400000L);
                }
                outRangeEncounterScheduled.add(
                    EncounterScheduledTest.getNewValidEncounterScheduledWithDate(date));
            }
        }

        for (EncounterScheduled testEncounterScheduled : inRangeEncounterScheduled) {
            Bundle testBundle = testEncounterScheduled.getBundle();
            Clinic testClinic = testEncounterScheduled.getClinic();
            clinicDao.merge(testClinic);
            bundleDao.merge(testBundle);

            testEncounterScheduledDao.merge(testEncounterScheduled);
        }
        for (EncounterScheduled testEncounterScheduled : outRangeEncounterScheduled) {
            Bundle testBundle = testEncounterScheduled.getBundle();
            Clinic testClinic = testEncounterScheduled.getClinic();
            clinicDao.merge(testClinic);
            bundleDao.merge(testBundle);
            testEncounterScheduledDao.merge(testEncounterScheduled);
        }

        assertEquals("The getting list of pastEncounterScheduleds was not the expected one",
            inRangeEncounterScheduled,
            testEncounterScheduledDao.getEncounterScheduledByDate(new Date(dateInMillis)));
    }

    /**
     * Test of {@link EncounterScheduledDaoImpl#getPastEncounterScheduled}.<br> Valid input: random
     * valid {@link EncounterScheduled} before and after today
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetPastEncounterScheduled() {
        clearTable();
        int countEncounterScheduled = random.nextInt(25) + 1;
        List<EncounterScheduled> pastEncounterScheduled = new ArrayList<>();
        List<EncounterScheduled> futureEncounterScheduled = new ArrayList<>();
        for (int i = 0; i < countEncounterScheduled; i++) {
            if (random.nextBoolean()) {
                Date endDate = new Date(System.currentTimeMillis() - Math.abs(random.nextInt()));
                pastEncounterScheduled.add(
                    EncounterScheduledTest.getNewValidEncounterScheduledWithEndDate(endDate));
            } else {
                Date endDate = new Date(System.currentTimeMillis()
                    + (long) (Math.abs(random.nextInt(500)) + 1) * 86400000L);
                futureEncounterScheduled.add(
                    EncounterScheduledTest.getNewValidEncounterScheduledWithEndDate(endDate));
            }
        }

        for (EncounterScheduled testEncounterScheduled : pastEncounterScheduled) {
            Bundle testBundle = testEncounterScheduled.getBundle();
            Clinic testClinic = testEncounterScheduled.getClinic();
            clinicDao.merge(testClinic);
            bundleDao.merge(testBundle);
            testEncounterScheduledDao.merge(testEncounterScheduled);
        }
        for (EncounterScheduled testEncounterScheduled : futureEncounterScheduled) {
            Bundle testBundle = testEncounterScheduled.getBundle();
            Clinic testClinic = testEncounterScheduled.getClinic();
            clinicDao.merge(testClinic);
            bundleDao.merge(testBundle);
            testEncounterScheduledDao.merge(testEncounterScheduled);
        }

        assertEquals("The getting list of pastEncounterScheduleds was not the expected one",
            pastEncounterScheduled, testEncounterScheduledDao.getPastEncounterScheduled());
    }

    /**
     * Deletes all {@link EncounterScheduled} from the database.
     */
    private void clearTable() {
        List<EncounterScheduled> allEncounterScheduled = testEncounterScheduledDao.getAllElements();
        for (EncounterScheduled encounterScheduled : allEncounterScheduled) {
            testEncounterScheduledDao.remove(encounterScheduled);
        }
    }
}
