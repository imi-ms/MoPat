package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class ExportRulesDTOTest {

    private static final Random random = new Random();
    private ExportRulesDTO testExportRulesDTO;

    public ExportRulesDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testExportRulesDTO = new ExportRulesDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ExportRulesDTO#getExportRuleScoreFormats} and
     * {@link ExportRulesDTO#setExportRuleScoreFormats}.<br> Valid input: random Map
     */
    @Test
    public void testGetAndSetExportRuleScoreFormats() {
        Map<Long, ExportRuleFormatDTO> testExportRuleScoreFormats = new HashMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            ExportRuleFormatDTO testExportRuleFormatDTO = new ExportRuleFormatDTO();
            testExportRuleFormatDTO.setId(Math.abs(random.nextLong()));
            testExportRuleScoreFormats.put(Math.abs(random.nextLong()), testExportRuleFormatDTO);
        }
        testExportRulesDTO.setExportRuleScoreFormats(testExportRuleScoreFormats);
        assertEquals("The getting map of ExportRuleScoreFormats was not the expected one",
            testExportRuleScoreFormats, testExportRulesDTO.getExportRuleScoreFormats());
    }

    /**
     * Test of {@link ExportRulesDTO#getExportRules} and {@link ExportRulesDTO#setExportRules}.<br>
     * Valid input: random list of {@link ExportRuleDTO ExportRuleDTOs}
     */
    @Test
    public void testGetAndSetExportRules() {
        List<ExportRuleDTO> testExportRuleDTOs = new ArrayList<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            ExportRuleDTO testExportRuleDTO = new ExportRuleDTO();
            testExportRuleDTO.setAnswerId(Math.abs(random.nextLong()));
            testExportRuleDTOs.add(testExportRuleDTO);
        }
        testExportRulesDTO.setExportRules(testExportRuleDTOs);
        assertEquals("The getting list of ExportRuleDTOs was not the expected one",
            testExportRuleDTOs, testExportRulesDTO.getExportRules());
    }

    /**
     * Test of {@link ExportRulesDTO#getExportTemplateId} and
     * {@link ExportRulesDTO#setExportTemplateId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetExportTemplateId() {
        Long testExportTemplateID = Math.abs(random.nextLong());
        testExportRulesDTO.setExportTemplateId(testExportTemplateID);
        assertEquals("The getting exportTemplateID was not the expected one", testExportTemplateID,
            testExportRulesDTO.getExportTemplateId());
    }

    /**
     * Test of {@link ExportRulesDTO#getExportRuleFormats} and
     * {@link ExportRulesDTO#setExportRuleFormats}.<br> Valid input: random Map
     */
    @Test
    public void testGetAndSetExportRuleFormats() {
        Map<Long, ExportRuleFormatDTO> testExportRuleFormats = new HashMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            ExportRuleFormatDTO testExportRuleFormatDTO = new ExportRuleFormatDTO();
            testExportRuleFormatDTO.setId(Math.abs(random.nextLong()));
            testExportRuleFormats.put(Math.abs(random.nextLong()), testExportRuleFormatDTO);
        }
        testExportRulesDTO.setExportRuleFormats(testExportRuleFormats);
        assertEquals("The getting map of ExportRuleFormats was not the expected one",
            testExportRuleFormats, testExportRulesDTO.getExportRuleFormats());
    }
}
