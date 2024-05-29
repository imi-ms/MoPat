package de.imi.mopat.model;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.model.enumeration.ExportStatus;
import de.imi.mopat.utils.Helper;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class EncounterExportTemplateTest {

    private static final Random random = new Random();
    private EncounterExportTemplate testEncounterExportTemplate;

    public EncounterExportTemplateTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new {@link EncounterExportTemplate}
     *
     * @return Returns a valid new {@link EncounterExportTemplate}
     */
    public static EncounterExportTemplate getNewValidEncounterExportTemplate() {
        Encounter encounter = EncounterTest.getNewValidEncounter();
        ExportTemplate exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        ExportStatus exportStatus = Helper.getRandomEnum(ExportStatus.class);
        EncounterExportTemplate encounterExportTemplate = new EncounterExportTemplate(encounter,
            exportTemplate, exportStatus);
        return encounterExportTemplate;
    }

    /**
     * Returns a valid new {@link EncounterExportTemplate}
     *
     * @param exportTemplate {@link ExportTemplate} of this {@link EncounterExportTemplate}
     * @param exportStatus   {@link ExportStatus} of this {@link EncounterExportTemplate}
     * @return Returns a valid new {@link EncounterExportTemplate}
     */
    public static EncounterExportTemplate getNewValidEncounterExportTemplate(
        ExportTemplate exportTemplate, ExportStatus exportStatus) {
        Encounter encounter = EncounterTest.getNewValidEncounter();
        EncounterExportTemplate encounterExportTemplate = new EncounterExportTemplate(encounter,
            exportTemplate, exportStatus);
        return encounterExportTemplate;
    }

    @Before
    public void setUp() {
        testEncounterExportTemplate = getNewValidEncounterExportTemplate();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link EncounterExportTemplate#getEncounter} and
     * {@link EncounterExportTemplate#setEncounter}.<br> Invalid input: <code>null</code><br> Valid
     * input: random {@link Encounter}
     */
    @Test
    public void testGetAndSetEncounter() {
        Encounter testEncounter = null;
        Throwable e = null;
        try {
            testEncounterExportTemplate.setEncounter(testEncounter);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the Encounter as null", e instanceof AssertionError);

        testEncounter = EncounterTest.getNewValidEncounter();
        testEncounterExportTemplate.setEncounter(testEncounter);
        assertEquals("The getting Encounter was not the expected one", testEncounter,
            testEncounterExportTemplate.getEncounter());
    }

    /**
     * Test of {@link EncounterExportTemplate#getExportTemplate} and
     * {@link EncounterExportTemplate#setExportTemplate}.<br> Invalid input: <code>null</code><br>
     * Valid input: random {@link ExportTemplate}
     */
    @Test
    public void testGetAndSetExportTemplate() {
        ExportTemplate testExportTemplate = null;
        Throwable e = null;
        try {
            testEncounterExportTemplate.setExportTemplate(testExportTemplate);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the ExportTemplate as null",
            e instanceof AssertionError);

        testExportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        testEncounterExportTemplate.setExportTemplate(testExportTemplate);
        assertEquals("The getting ExportTemplate was not the expected one", testExportTemplate,
            testEncounterExportTemplate.getExportTemplate());
    }

    /**
     * Test of {@link EncounterExportTemplate#getExportTime}.<br>
     */
    @Test
    public void testGetExportTime() {
        assertTrue("The getting ExportTime was not the expected one",
            testEncounterExportTemplate.getExportTime()
                .after(new Timestamp(System.currentTimeMillis() - 20))
                && testEncounterExportTemplate.getExportTime()
                .before(new Timestamp(System.currentTimeMillis() + 20)));
    }

    /**
     * Test of {@link EncounterExportTemplate#equals}.<br> Invalid input: the same
     * {@link EncounterExportTemplate} twice in a set
     */
    @Test
    public void testEquals() {
        Set<EncounterExportTemplate> testSet = new HashSet<>();
        testSet.add(testEncounterExportTemplate);
        testSet.add(testEncounterExportTemplate);
        assertEquals("It was possible to one EncounterExportTemplate twice in a set",
            testSet.size(), 1);

        assertEquals("The EncounterExportTemplate was not equal to itself",
            testEncounterExportTemplate, testEncounterExportTemplate);
        assertNotEquals("The EncounterExportTemplate was equal to null", null,
            testEncounterExportTemplate);
        EncounterExportTemplate otherEncounterExportTemplate = getNewValidEncounterExportTemplate();
        assertNotEquals(
            "The EncounterExportTemplate was equal to a different EncounterExportTemplate",
            testEncounterExportTemplate, otherEncounterExportTemplate);
        Object otherObject = new Object();
        assertNotEquals("The EncounterExportTemplate was equal to a different Object",
            testEncounterExportTemplate, otherObject);
    }

    /**
     * Test of {@link EncounterExportTemplate#getExportStatus} and
     * {@link EncounterExportTemplate#setExportStatus}.<br> Invalid input: <code>null</code><br>
     * Valid input: random {@link ExportStatus}
     */
    @Test
    public void testGetAndSetExportStatus() {
        ExportStatus testExportStatus = null;
        Throwable e = null;
        try {
            testEncounterExportTemplate.setExportStatus(testExportStatus);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the ExportStatus as null", e instanceof AssertionError);

        testExportStatus = Helper.getRandomEnum(ExportStatus.class);
        testEncounterExportTemplate.setExportStatus(testExportStatus);
        assertEquals("The getting ExportStatus was not the expected one", testExportStatus,
            testEncounterExportTemplate.getExportStatus());
    }

    /**
     * Test of {@link EncounterExportTemplate#getIsManuallyExported} and
     * {@link EncounterExportTemplate#getIsManuallyExported}.<br> Invalid input:
     * <code>null</code><br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetIsManuallyExported() {
        Boolean testIsManuallyExported = null;
        Throwable e = null;
        try {
            testEncounterExportTemplate.setIsManuallyExported(testIsManuallyExported);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set the isManuallyExported as null",
            e instanceof AssertionError);

        testIsManuallyExported = random.nextBoolean();
        testEncounterExportTemplate.setIsManuallyExported(testIsManuallyExported);
        assertEquals("The getting isManuallyExported was not the expected one",
            testIsManuallyExported, testEncounterExportTemplate.getIsManuallyExported());
    }

    /**
     * Test of {@link EncounterExportTemplate#compareTo}.<br>
     */
    @Test
    public void testCompareTo() {
        try {
            // Sleep one millisecond so that the TimeStamps of the EncounterExportTemplates are not equal
            sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(EncounterExportTemplateTest.class.getName())
                .log(Level.SEVERE, null, ex);
        }
        EncounterExportTemplate compareEncounterExportTemplate = getNewValidEncounterExportTemplate();
        assertEquals("Failure in compareTo() method", -1,
            testEncounterExportTemplate.compareTo(compareEncounterExportTemplate));
        assertEquals("Failure in compareTo() method", 0,
            testEncounterExportTemplate.compareTo(testEncounterExportTemplate));
        assertEquals("Failure in compareTo() method", 1,
            compareEncounterExportTemplate.compareTo(testEncounterExportTemplate));
    }
}
