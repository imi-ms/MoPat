package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.model.enumeration.ExportDateFormatType;
import de.imi.mopat.model.enumeration.ExportEncounterFieldType;
import de.imi.mopat.model.enumeration.ExportScoreFieldType;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
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
public class ExportRuleDTOTest {

    private static final Random random = new Random();
    private ExportRuleDTO testExportRuleDTO;

    public ExportRuleDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testExportRuleDTO = new ExportRuleDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ExportRuleDTO#getAnswerId} and {@link ExportRuleDTO#setAnswerId}.<br> Valid
     * input: random Long
     */
    @Test
    public void testGetAndSetAnswerId() {
        Long testAnswerID = Math.abs(random.nextLong());
        testExportRuleDTO.setAnswerId(testAnswerID);
        assertEquals("The getting AnswerID was not the expected one", testAnswerID,
            testExportRuleDTO.getAnswerId());
    }

    /**
     * Test of {@link ExportRuleDTO#getExportField} and {@link ExportRuleDTO#setExportField}.<br>
     * Valid input: random list of Strings
     */
    @Test
    public void testGetAndSetExportField() {
        List<String> testExportFields = new ArrayList<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testExportFields.add(Helper.getRandomAlphanumericString(random.nextInt(50) + 1));
        }
        testExportRuleDTO.setExportField(testExportFields);
        assertEquals("The getting list of ExportFields was not the expected one", testExportFields,
            testExportRuleDTO.getExportField());
    }

    /**
     * Test of {@link ExportRuleDTO#getEncounterField} and
     * {@link ExportRuleDTO#setEncounterField}.<br> Valid input: random
     * {@link ExportEncounterFieldType}
     */
    @Test
    public void testGetAndSetEncounterField() {
        ExportEncounterFieldType testEncounterField = Helper.getRandomEnum(
            ExportEncounterFieldType.class);
        testExportRuleDTO.setEncounterField(testEncounterField);
        assertEquals("The getting EncounterField was not the expected one", testEncounterField,
            testExportRuleDTO.getEncounterField());
    }

    /**
     * Test of {@link ExportRuleDTO#getEncounterDateFormat} and
     * {@link ExportRuleDTO#setEncounterDateFormat}.<br> Valid input: random
     * {@link ExportDateFormatType}
     */
    @Test
    public void testGetAndSetEncounterDateFormat() {
        ExportDateFormatType testEncounterDateFormat = Helper.getRandomEnum(
            ExportDateFormatType.class);
        testExportRuleDTO.setEncounterDateFormat(testEncounterDateFormat);
        assertEquals("The getting EncounterDateFormat was not the expected one",
            testEncounterDateFormat, testExportRuleDTO.getEncounterDateFormat());
    }

    /**
     * Test of {@link ExportRuleDTO#getTempExportFormatId} and
     * {@link ExportRuleDTO#setTempExportFormatId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetTempExportFormatId() {
        Long testTempExportFormatID = Math.abs(random.nextLong());
        testExportRuleDTO.setTempExportFormatId(testTempExportFormatID);
        assertEquals("The getting tempExportFormatID was not the expected one",
            testTempExportFormatID, testExportRuleDTO.getTempExportFormatId());
    }

    /**
     * Test of {@link ExportRuleDTO#getUseFreetextValue} and
     * {@link ExportRuleDTO#setUseFreetextValue}.<br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetUseFreetextValue() {
        Boolean testUseFreetextValue = random.nextBoolean();
        testExportRuleDTO.setUseFreetextValue(testUseFreetextValue);
        assertEquals("The getting useFreetextValue was not the expected one", testUseFreetextValue,
            testExportRuleDTO.getUseFreetextValue());
    }

    /**
     * Test of {@link ExportRuleDTO#getScoreField} and {@link ExportRuleDTO#setScoreField}.<br>
     * Valid input: random {@link ExportScoreFieldType}
     */
    @Test
    public void testGetAndSetScoreField() {
        ExportScoreFieldType testScoreField = Helper.getRandomEnum(ExportScoreFieldType.class);
        testExportRuleDTO.setScoreField(testScoreField);
        assertEquals("The getting ScoreField was not the expected one", testScoreField,
            testExportRuleDTO.getScoreField());
    }

    /**
     * Test of {@link ExportRuleDTO#getScoreId} and {@link ExportRuleDTO#setScoreId}.<br> Valid
     * input: random Long
     */
    @Test
    public void testGetAndSetScoreId() {
        Long testScoreID = Math.abs(random.nextLong());
        testExportRuleDTO.setScoreId(testScoreID);
        assertEquals("The getting ScoreID was not the expected one", testScoreID,
            testExportRuleDTO.getScoreId());
    }
}
