package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.model.Statistic;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class StatisticDTOTest {

    private static final Random random = new Random();
    private StatisticDTO testStatisticDTO;

    public StatisticDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testStatisticDTO = new StatisticDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link StatisticDTO#StatisticDTO(java.util.Date, java.util.Date)}.<br> Valid input:
     * random random start- and enddate
     */
    @Test
    public void testConstructor() {
        Date testStartDate = new Date(random.nextLong());
        Date testEndDate = new Date(random.nextLong());
        testStatisticDTO = new StatisticDTO(testStartDate, testEndDate);
        assertEquals("The getting startDate was not the expected one", testStartDate,
            testStatisticDTO.getStartDate());
        assertEquals("The getting endDate was not the expected one", testEndDate,
            testStatisticDTO.getEndDate());
        assertEquals("The getting maxDate was not the expected one", testEndDate,
            testStatisticDTO.getMaxDate());
        assertEquals("The getting minDate was not the expected one", testStartDate,
            testStatisticDTO.getMinDate());
        assertEquals("The getting count was not the expected one", 2, testStatisticDTO.getCount());
    }

    /**
     * Test of {@link StatisticDTO#getMinDate} and {@link StatisticDTO#setMinDate}.<br> Valid input:
     * random Date
     */
    @Test
    public void testGetAndSetMinDate() {
        Date testMinDate = new Date(random.nextLong());
        testStatisticDTO.setMinDate(testMinDate);
        assertEquals("The getting minDate was not the expected one", testMinDate,
            testStatisticDTO.getMinDate());
    }

    /**
     * Test of {@link StatisticDTO#getMaxDate} and {@link StatisticDTO#setMaxDate}.<br> Valid input:
     * random Date
     */
    @Test
    public void testGetAndSetMaxDate() {
        Date testMaxDate = new Date(random.nextLong());
        testStatisticDTO.setMaxDate(testMaxDate);
        assertEquals("The getting maxDate was not the expected one", testMaxDate,
            testStatisticDTO.getMaxDate());
    }

    /**
     * Test of {@link StatisticDTO#getStartDate} and {@link StatisticDTO#setStartDate}.<br> Valid
     * input: random Date
     */
    @Test
    public void testGetAndSetStartDate() {
        Date testStartDate = new Date(random.nextLong());
        testStatisticDTO.setStartDate(testStartDate);
        assertEquals("The getting startDate was not the expected one", testStartDate,
            testStatisticDTO.getStartDate());
    }

    /**
     * Test of {@link StatisticDTO#getEndDate} and {@link StatisticDTO#setEndDate}.<br> Valid input:
     * random Date
     */
    @Test
    public void testGetAndSetEndDate() {
        Date testEndDate = new Date(random.nextLong());
        testStatisticDTO.setEndDate(testEndDate);
        assertEquals("The getting endDate was not the expected one", testEndDate,
            testStatisticDTO.getEndDate());
    }

    /**
     * Test of {@link StatisticDTO#getCount} and {@link StatisticDTO#setCount}.<br> Valid input:
     * random Integet
     */
    @Test
    public void testGetAndSetCount() {
        int testCount = Math.abs(random.nextInt());
        testStatisticDTO.setCount(testCount);
        assertEquals("The getting count was not the expected one", testCount,
            testStatisticDTO.getCount());
    }

    /**
     * Test of {@link StatisticDTO#} and {@link StatisticDTO#}.<br> Valid input: random list of
     * {@link Statistic Statistics}
     */
    @Test
    public void testGetAndSetStatistics() {
        List<Statistic> testStatistics = new ArrayList<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testStatistics.add(new Statistic());
        }
        testStatisticDTO.setStatistics(testStatistics);
        assertEquals("The getting list of Statistics was not the expected one", testStatistics,
            testStatisticDTO.getStatistics());
    }
}
