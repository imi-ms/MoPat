package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.model.enumeration.ExportDecimalDelimiterType;
import de.imi.mopat.model.enumeration.ExportNumberType;
import de.imi.mopat.model.enumeration.ExportRoundingStrategyType;
import de.imi.mopat.utils.Helper;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class ExportRuleFormatDTOTest {

    private static final Random random = new Random();
    private ExportRuleFormatDTO testExportRuleFormatDTO;

    public ExportRuleFormatDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testExportRuleFormatDTO = new ExportRuleFormatDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ExportRuleFormatDTO#getNumberType} and
     * {@link ExportRuleFormatDTO#setNumberType}.<br> Valid input: random {@link ExportNumberType}
     */
    @Test
    public void testGetAndSetNumberType() {
        ExportNumberType testNumberType = Helper.getRandomEnum(ExportNumberType.class);
        testExportRuleFormatDTO.setNumberType(testNumberType);
        assertEquals("The getting NumberType was not the expected one", testNumberType,
            testExportRuleFormatDTO.getNumberType());
    }

    /**
     * Test of {@link ExportRuleFormatDTO#getRoundingStrategy} and
     * {@link ExportRuleFormatDTO#setRoundingStrategy}.<br> Valid input: random
     * {@link ExportRoundingStrategyType}
     */
    @Test
    public void testGetAndSetRoundingStrategy() {
        ExportRoundingStrategyType testRoundingStrategy = Helper.getRandomEnum(
            ExportRoundingStrategyType.class);
        testExportRuleFormatDTO.setRoundingStrategy(testRoundingStrategy);
        assertEquals("The getting RoundingStrategy was not the expected one", testRoundingStrategy,
            testExportRuleFormatDTO.getRoundingStrategy());
    }

    /**
     * Test of {@link ExportRuleFormatDTO#getDecimalPlaces} and
     * {@link ExportRuleFormatDTO#setDecimalPlaces}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetDecimalPlaces() {
        String testDecimalPlaces = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testExportRuleFormatDTO.setDecimalPlaces(testDecimalPlaces);
        assertEquals("The getting DecimalPlaces was not the expected one", testDecimalPlaces,
            testExportRuleFormatDTO.getDecimalPlaces());
    }

    /**
     * Test of {@link ExportRuleFormatDTO#getDecimalDelimiter} and
     * {@link ExportRuleFormatDTO#setDecimalDelimiter}.<br> Valid input: random
     * {@link ExportDecimalDelimiterType}
     */
    @Test
    public void testGetAndSetDecimalDelimiter() {
        ExportDecimalDelimiterType testDecimalDelimiter = Helper.getRandomEnum(
            ExportDecimalDelimiterType.class);
        testExportRuleFormatDTO.setDecimalDelimiter(testDecimalDelimiter);
        assertEquals("The getting DecimalDelimiter was not the expected one", testDecimalDelimiter,
            testExportRuleFormatDTO.getDecimalDelimiter());
    }

    /**
     * Test of {@link ExportRuleFormatDTO#getDateFormat} and
     * {@link ExportRuleFormatDTO#setDateFormat}.<br> Valid input: random
     * {@link ExportDateFormatType}
     */
    @Test
    public void testGetAndSetDateFormat() {
        ExportDateFormatType testDateFormat = Helper.getRandomEnum(ExportDateFormatType.class);
        testExportRuleFormatDTO.setDateFormat(testDateFormat);
        assertEquals("The getting DateFormat was not the expected one", testDateFormat,
            testExportRuleFormatDTO.getDateFormat());
    }

    /**
     * Test of {@link ExportRuleFormatDTO#} and {@link ExportRuleFormatDTO#}.<br> Valid input:
     * random Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testExportRuleFormatDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID,
            testExportRuleFormatDTO.getId());
    }
}
