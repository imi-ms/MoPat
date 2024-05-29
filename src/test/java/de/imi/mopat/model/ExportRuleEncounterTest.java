package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.model.enumeration.ExportEncounterFieldType;
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
public class ExportRuleEncounterTest {

    private static final Random random = new Random();
    private ExportRuleEncounter testExportRuleEncounter;

    public ExportRuleEncounterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new ExportRuleEncounter
     *
     * @return Returns a valid new ExportRuleEncounter
     */
    public static ExportRuleEncounter getNewValidExportRuleEncounter() {
        ExportTemplate exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        String exportField = Helper.getRandomString(random.nextInt(50) + 1);
        ExportEncounterFieldType encounterField = Helper.getRandomEnum(
            ExportEncounterFieldType.class);

        ExportRuleEncounter exportRuleEncounter = new ExportRuleEncounter(exportTemplate,
            exportField, encounterField);

        return exportRuleEncounter;
    }

    @Before
    public void setUp() {
        testExportRuleEncounter = getNewValidExportRuleEncounter();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link exportRuleEncounter#}.<br> Valid input: random
     * {@link ExportEncounterFieldType}
     */
    @Test
    public void testGetAndSetEncounterField() {
        ExportEncounterFieldType testEncounterField = Helper.getRandomEnum(
            ExportEncounterFieldType.class);
        testExportRuleEncounter.setEncounterField(testEncounterField);
        assertEquals("The getting EncounterField was not the expected one", testEncounterField,
            testExportRuleEncounter.getEncounterField());
    }
}