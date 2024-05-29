package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleTest;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterTest;
import de.imi.mopat.utils.Helper;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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
public class EncounterDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    EncounterDao testEncounterDao;
    @Autowired
    BundleDao bundleDao;

    /**
     * Test of {@link EncounterDaoImpl#getIncompleteEncounters}.<br> Invalid input:
     * <code>null</code><br> Valid input: caseNumber of complete and incomplete
     * {@link Encounter Encounters}
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetIncompleteEncounters() {
        String testCaseNumber = null;
        Throwable e = null;
        try {
            testEncounterDao.getIncompleteEncounters(testCaseNumber);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to get incomplete Encounter with case number null",
            e instanceof AssertionError);

        clearTable();
        testCaseNumber = Helper.getRandomAlphanumericString(random.nextInt(25) + 1);
        assertTrue(
            "The getting list of incomplete Encounters was not empty although there was no Encounter with the given caseNumber",
            testEncounterDao.getIncompleteEncounters(testCaseNumber).isEmpty());

        int countEncounters = random.nextInt(25) + 2;
        List<Encounter> incompleteEncounters = new ArrayList<>();
        for (int i = 0; i < countEncounters; i++) {
            boolean check;
            switch (i) {
                case 0:
                    check = true;
                    break;
                case 1:
                    check = false;
                    break;
                default:
                    check = random.nextBoolean();
                    break;
            }
            Encounter testEncounter;
            Bundle testBundle = BundleTest.getNewValidBundle();
            bundleDao.merge(testBundle);
            if (check) {
                testEncounter = new Encounter(testBundle, testCaseNumber);
                testEncounter.setStartTime(new Timestamp(System.currentTimeMillis() - i * 10000L));
                incompleteEncounters.add(testEncounter);
            } else {
                testEncounter = new Encounter(testBundle, testCaseNumber);
                testEncounter.setStartTime(new Timestamp(System.currentTimeMillis()));
                testEncounter.setEndTime(new Timestamp(System.currentTimeMillis() + 100));
            }
            testEncounterDao.merge(testEncounter);
        }
        assertEquals("The getting list of incomplete Encounters was not the expected one",
            incompleteEncounters, testEncounterDao.getIncompleteEncounters(testCaseNumber));
    }

    /**
     * Test of {@link EncounterDaoImpl#getFinishedEncounterOlderThan}.<br> Invalid input:
     * <code>null</code><br> Valid input: Timestamp with older and newer finished
     * {@link Encounter Encounters}
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetFinishedEncounterOlderThan() {
        Timestamp testTimestamp = null;
        Throwable e = null;
        try {
            testEncounterDao.getFinishedEncounterOlderThan(testTimestamp);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to get finished Encounter older than null",
            e instanceof AssertionError);

        clearTable();
        testTimestamp = new Timestamp(System.currentTimeMillis());
        assertTrue(
            "The getting list of finished Encounters was not empty although there was no Encounter",
            testEncounterDao.getFinishedEncounterOlderThan(testTimestamp).isEmpty());

        int countEncounters = random.nextInt(25) + 2;
        List<Encounter> finishedEncounters = new ArrayList<>();
        for (int i = 0; i < countEncounters; i++) {
            boolean check;
            switch (i) {
                case 0:
                    check = true;
                    break;
                case 1:
                    check = false;
                    break;
                default:
                    check = random.nextBoolean();
                    break;
            }
            Encounter testEncounter;
            Bundle testBundle = BundleTest.getNewValidBundle();
            bundleDao.merge(testBundle);
            if (check) {
                testEncounter = EncounterTest.getNewValidEncounter(testBundle);
                testEncounter.setStartTime(null);
                testEncounter.setEndTime(
                    new Timestamp(testTimestamp.getTime() - (random.nextInt(1000) + 1) * 10000));
                testEncounter.setStartTime(new Timestamp(
                    testEncounter.getEndTime().getTime() - (random.nextInt(1000) + 1) * 10000));
                finishedEncounters.add(testEncounter);
            } else {
                testEncounter = EncounterTest.getNewValidEncounter(testBundle);
                testEncounter.setStartTime(null);
                testEncounter.setEndTime(
                    new Timestamp(testTimestamp.getTime() + (random.nextInt(1000) + 1) * 10000));
                testEncounter.setStartTime(new Timestamp(
                    testEncounter.getEndTime().getTime() - (random.nextInt(1000) + 1) * 10000));
            }
            testEncounterDao.merge(testEncounter);
        }
        assertEquals(
            "The getting list of finished Encounters older than a given timestamp was not the expected one",
            finishedEncounters, testEncounterDao.getFinishedEncounterOlderThan(testTimestamp));
    }

    /**
     * Test of {@link EncounterDaoImpl#getIncompleteEncountersOlderThan}.<br> Invalid input:
     * <code>null</code><br> Valid input: Timestamp with older and newer incomplete
     * {@link Encounter Encounters}
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetIncompleteEncountersOlderThan() {
        Timestamp testTimestamp = null;
        Throwable e = null;
        try {
            testEncounterDao.getIncompleteEncountersOlderThan(testTimestamp);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to get finished Encounter older than null",
            e instanceof AssertionError);

        clearTable();
        testTimestamp = new Timestamp(System.currentTimeMillis());
        assertTrue(
            "The getting list of finished Encounters was not empty although there was no Encounter",
            testEncounterDao.getIncompleteEncountersOlderThan(testTimestamp).isEmpty());

        int countEncounters = random.nextInt(25) + 1;
        List<Encounter> incompleteEncounters = new ArrayList<>();
        for (int i = 0; i < countEncounters; i++) {
            Encounter testEncounter;
            Bundle testBundle = BundleTest.getNewValidBundle();
            bundleDao.merge(testBundle);
            if (random.nextBoolean()) {
                testEncounter = EncounterTest.getNewValidEncounter(testBundle);
                testEncounter.setStartTime(
                    new Timestamp(testTimestamp.getTime() - (random.nextInt(1000) + 1) * 10000));
                incompleteEncounters.add(testEncounter);
            } else {
                testEncounter = EncounterTest.getNewValidEncounter(testBundle);
                testEncounter.setStartTime(
                    new Timestamp(testTimestamp.getTime() + (random.nextInt(1000) + 1) * 10000));
            }
            testEncounterDao.merge(testEncounter);
        }
        assertEquals(
            "The getting list of incomplete Encounters older than a given timestamp was not the expected one",
            incompleteEncounters, testEncounterDao.getIncompleteEncountersOlderThan(testTimestamp));
    }

    /**
     * Test of {@link EncounterDaoImpl#getCountIncompleteEncounter}.<br> Valid input: random number
     * of finished and incomplete {@link Encounter Encounters}
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetCountIncompleteEncounter() {
        clearTable();
        assertEquals(
            "The getting count of incomplete Encounters was not 0 although there was no Encounter",
            0, (long) testEncounterDao.getCountIncompleteEncounter());

        int countEncounters = random.nextInt(25) + 1;
        int countIncompleteEncounters = 0;
        for (int i = 0; i < countEncounters; i++) {
            Encounter testEncounter;
            Bundle testBundle = BundleTest.getNewValidBundle();
            bundleDao.merge(testBundle);
            if (random.nextBoolean()) {
                testEncounter = EncounterTest.getNewValidEncounter(testBundle);
                testEncounter.setStartTime(new Timestamp(System.currentTimeMillis() - i * 10000L));
                countIncompleteEncounters++;
            } else {
                testEncounter = EncounterTest.getNewValidEncounter(testBundle);
                testEncounter.setStartTime(new Timestamp(System.currentTimeMillis()));
                testEncounter.setEndTime(new Timestamp(System.currentTimeMillis() + 100));
            }
            testEncounterDao.merge(testEncounter);
        }
        assertEquals("The getting count of incomplete Encounters was not the expected one",
            countIncompleteEncounters, (long) testEncounterDao.getCountIncompleteEncounter());
    }

    /**
     * Test of {@link EncounterDaoImpl#getCountCompleteEncountersOlderThan}.<br> Valid input:
     * Timestamp with older and newer incomplete {@link Encounter Encounters}
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetCountCompleteEncountersOlderThan() {
        clearTable();
        Timestamp testTimestamp = new Timestamp(System.currentTimeMillis());
        assertEquals(
            "The getting count of finished Encounters was not 1 although there was no Encounter", 0,
            (long) testEncounterDao.getCountCompleteEncountersOlderThan(testTimestamp));

        int countEncounters = random.nextInt(25) + 1;
        int countFinishedEncounters = 0;
        for (int i = 0; i < countEncounters; i++) {
            Encounter testEncounter;
            Bundle testBundle = BundleTest.getNewValidBundle();
            bundleDao.merge(testBundle);
            if (random.nextBoolean()) {
                testEncounter = EncounterTest.getNewValidEncounter(testBundle);
                testEncounter.setStartTime(null);
                testEncounter.setEndTime(
                    new Timestamp(testTimestamp.getTime() - (random.nextInt(1000) + 1) * 10000));
                testEncounter.setStartTime(new Timestamp(
                    testEncounter.getEndTime().getTime() - (random.nextInt(1000) + 1) * 10000));
                countFinishedEncounters++;
            } else {
                testEncounter = EncounterTest.getNewValidEncounter(testBundle);
                testEncounter.setStartTime(null);
                testEncounter.setEndTime(
                    new Timestamp(testTimestamp.getTime() + (random.nextInt(1000) + 1) * 10000));
                testEncounter.setStartTime(new Timestamp(
                    testEncounter.getEndTime().getTime() - (random.nextInt(1000) + 1) * 10000));
            }
            testEncounterDao.merge(testEncounter);
        }
        assertEquals(
            "The getting count of finished Encounters older than a given timestamp was not the expected one",
            countFinishedEncounters,
            (long) testEncounterDao.getCountCompleteEncountersOlderThan(testTimestamp));
    }

    /**
     * Test of {@link EncounterDaoImpl#getCountIncompleteEncountersOlderThan}.<br> Valid input:
     * Timestamp with older and newer incomplete {@link Encounter Encounters}
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetCountIncompleteEncountersOlderThan() {
        clearTable();
        Timestamp testTimestamp = new Timestamp(System.currentTimeMillis());
        assertEquals(
            "The getting count of finished Encounters was not 0 although there was no Encounter", 0,
            (long) testEncounterDao.getCountIncompleteEncountersOlderThan(testTimestamp));

        int countEncounters = random.nextInt(25) + 1;
        int countIncompleteEncounters = 0;
        for (int i = 0; i < countEncounters; i++) {
            Encounter testEncounter;
            Bundle testBundle = BundleTest.getNewValidBundle();
            bundleDao.merge(testBundle);
            if (random.nextBoolean()) {
                testEncounter = EncounterTest.getNewValidEncounter(testBundle);
                testEncounter.setStartTime(
                    new Timestamp(testTimestamp.getTime() - (random.nextInt(1000) + 1) * 10000));
                countIncompleteEncounters++;
            } else {
                testEncounter = EncounterTest.getNewValidEncounter(testBundle);
                testEncounter.setStartTime(
                    new Timestamp(testTimestamp.getTime() + (random.nextInt(1000) + 1) * 10000));
            }
            testEncounterDao.merge(testEncounter);
        }
        assertEquals(
            "The getting count of incomplete Encounters older than a given timestamp was not the expected one",
            countIncompleteEncounters,
            (long) testEncounterDao.getCountIncompleteEncountersOlderThan(testTimestamp));

    }

    /**
     * Test of {@link EncounterDaoImpl#getAllCaseNumbers}.<br> Valid input: radom {@link Encounter}
     * with random case number as String
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetAllCaseNumbers() {
        clearTable();
        int countEncounters = random.nextInt(25) + 1;
        List<String> caseNumbers = new ArrayList<>();
        for (int i = 0; i < countEncounters; i++) {
            String testCaseNumber = Helper.getRandomAlphanumericString(random.nextInt(25) + 1);
            if (!caseNumbers.contains(testCaseNumber)) {
                Bundle testBundle = BundleTest.getNewValidBundle();
                bundleDao.merge(testBundle);
                Encounter testEncounter = new Encounter(testBundle, testCaseNumber);
                testEncounterDao.merge(testEncounter);
                caseNumbers.add(testCaseNumber);
            }
        }
        Collections.sort(caseNumbers);
        List<String> daoCaseNumbers = testEncounterDao.getAllCaseNumbers();
        Collections.sort(daoCaseNumbers);
        assertEquals("The getting list of case numbers was not the expected one", caseNumbers,
            daoCaseNumbers);
    }

    /**
     * Test of {@link EncounterDaoImpl#getEncounterCountByBundleInInterval}.<br> Valid input: valid
     * BundleId and range with random {@link Encounter Encounters} with this BundleId in and not in
     * this range
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetEncounterCountByBundleInInterval() {
        clearTable();
        Bundle testBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundle);
        Long testStart = System.currentTimeMillis() - (random.nextInt(250) + 1) * 86400000L;
        Long testEnd = System.currentTimeMillis() + (random.nextInt(250) + 1) * 86400000L;
        assertEquals(
            "The getting count of Encounters by Bundle in intervall was not 0 although there was no Encounter",
            0, (long) testEncounterDao.getEncounterCountByBundleInInterval(testBundle.getId(),
                new Date(testStart), new Date(testEnd)));

        int countEncounters = random.nextInt(25) + 1;
        int countByBundleInInterval = 0;
        for (int i = 0; i < countEncounters; i++) {
            Encounter testEncounter = EncounterTest.getNewValidEncounter(testBundle);
            if (random.nextBoolean()) {
                if (random.nextBoolean()) {
                    testEncounter.setStartTime(null);
                    testEncounter.setEndTime(new Timestamp(testStart + random.nextInt(86400000)));
                    testEncounter.setStartTime(new Timestamp(testEncounter.getEndTime().getTime()
                        - (random.nextInt(250) + 1) * 86400000L));
                } else {
                    testEncounter.setStartTime(new Timestamp(testStart + random.nextInt(86400000)));
                }
                countByBundleInInterval++;
            } else if (random.nextBoolean()) {
                testEncounter.setEndTime(
                    new Timestamp(testEnd + (random.nextInt(50) + 5) * 86400000L));
            } else {
                testEncounter.setStartTime(
                    new Timestamp(testEnd + (random.nextInt(50) + 5) * 86400000L));
            }
            testEncounterDao.merge(testEncounter);
        }
        assertEquals(
            "The getting count of Encounters by Bundle in intervall was not the expected one",
            countByBundleInInterval,
            (long) testEncounterDao.getEncounterCountByBundleInInterval(testBundle.getId(),
                new Date(testStart), new Date(testEnd)));
    }

    /**
     * Test of {@link EncounterDaoImpl#getEncounterCountByCaseNumberInInterval}.<br> Valid input:
     * valid case number and range with random {@link Encounter Encounters} with this case number in
     * and not in this range
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetEncounterCountByCaseNumberInInterval() {
        clearTable();
        String testCaseNumber = Helper.getRandomAlphanumericString(random.nextInt(25) + 1);
        Long testStart = System.currentTimeMillis() - (random.nextInt(250) + 1) * 86400000L;
        Long testEnd = System.currentTimeMillis() + (random.nextInt(250) + 1) * 86400000L;
        assertEquals(
            "The getting count of Encounters by case number in intervall was not 0 although there was no Encounter",
            0, (long) testEncounterDao.getEncounterCountByCaseNumberInInterval(testCaseNumber,
                new Date(testStart), new Date(testEnd)));

        int countEncounters = random.nextInt(25) + 1;
        int countByCaseNumberInInterval = 0;
        for (int i = 0; i < countEncounters; i++) {
            Bundle testBundle = BundleTest.getNewValidBundle();
            bundleDao.merge(testBundle);
            Encounter testEncounter = new Encounter(testBundle, testCaseNumber);
            if (random.nextBoolean()) {
                if (random.nextBoolean()) {
                    testEncounter.setStartTime(null);
                    testEncounter.setEndTime(new Timestamp(testStart + random.nextInt(86400000)));
                    testEncounter.setStartTime(new Timestamp(testEncounter.getEndTime().getTime()
                        - (random.nextInt(250) + 1) * 86400000L));
                } else {
                    testEncounter.setStartTime(new Timestamp(testStart + random.nextInt(86400000)));
                }
                countByCaseNumberInInterval++;
            } else if (random.nextBoolean()) {
                testEncounter.setEndTime(
                    new Timestamp(testEnd + (random.nextInt(50) + 5) * 86400000L));
            } else {
                testEncounter.setStartTime(
                    new Timestamp(testEnd + (random.nextInt(50) + 5) * 86400000L));
            }
            testEncounterDao.merge(testEncounter);
        }
        assertEquals(
            "The getting count of Encounters by case number in intervall was not the expected one",
            countByCaseNumberInInterval,
            (long) testEncounterDao.getEncounterCountByCaseNumberInInterval(testCaseNumber,
                new Date(testStart), new Date(testEnd)));

    }

    /**
     * Test of {@link EncounterDaoImpl#getEncounterCountByCaseNumberByBundleInInterval}.<br> Valid
     * input: valid BundleId, case number and range with random {@link Encounter Encounters} with
     * this BundleId and case number in and not in this range
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetEncounterCountByCaseNumberByBundleInInterval() {
        clearTable();
        String testCaseNumber = Helper.getRandomAlphanumericString(random.nextInt(25) + 1);
        Bundle testBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundle);
        Long testStart = System.currentTimeMillis() - (random.nextInt(250) + 1) * 86400000L;
        Long testEnd = System.currentTimeMillis() + (random.nextInt(250) + 1) * 86400000L;
        assertEquals(
            "The getting count of Encounters by Bundle and by case number in intervall was not 0 although there was no Encounter",
            0, (long) testEncounterDao.getEncounterCountByCaseNumberByBundleInInterval(
                testBundle.getId(), testCaseNumber, new Date(testStart), new Date(testEnd)));

        int countEncounters = random.nextInt(25) + 1;
        int countByBundleInInterval = 0;
        for (int i = 0; i < countEncounters; i++) {
            Encounter testEncounter = new Encounter(testBundle, testCaseNumber);
            if (random.nextBoolean()) {
                if (random.nextBoolean()) {
                    testEncounter.setStartTime(null);
                    testEncounter.setEndTime(new Timestamp(testStart + random.nextInt(86400000)));
                    testEncounter.setStartTime(new Timestamp(testEncounter.getEndTime().getTime()
                        - (random.nextInt(250) + 1) * 86400000L));
                } else {
                    testEncounter.setStartTime(new Timestamp(testStart + random.nextInt(86400000)));
                }
                countByBundleInInterval++;
            } else if (random.nextBoolean()) {
                testEncounter.setEndTime(
                    new Timestamp(testEnd + (random.nextInt(50) + 5) * 86400000L));
            } else {
                testEncounter.setStartTime(
                    new Timestamp(testEnd + (random.nextInt(50) + 5) * 86400000L));
            }
            testEncounterDao.merge(testEncounter);
        }
        assertEquals(
            "The getting count of Encounters by Bundle and by case number in intervall was not the expected one",
            countByBundleInInterval,
            (long) testEncounterDao.getEncounterCountByCaseNumberByBundleInInterval(
                testBundle.getId(), testCaseNumber, new Date(testStart), new Date(testEnd)));

    }

    /**
     * Deletes all {@link Encounter Encounters} from the database.
     */
    private void clearTable() {
        List<Encounter> allEncounter = testEncounterDao.getAllElements();
        for (Encounter encounter : allEncounter) {
            testEncounterDao.remove(encounter);
        }
    }
}
