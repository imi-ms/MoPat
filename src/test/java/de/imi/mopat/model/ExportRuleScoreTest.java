package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import de.imi.mopat.model.enumeration.ExportScoreFieldType;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.ScoreTest;
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
public class ExportRuleScoreTest {

    private static final Random random = new Random();
    private ExportRuleScore testExportRuleScore;

    public ExportRuleScoreTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new ExportRuleScore
     *
     * @return Returns a valid new ExportRuleScore
     */
    public static ExportRuleScore getNewValidExportRuleScore() {
        ExportTemplate exportTemplate = ExportTemplateTest.getNewValidExportTemplate();
        String exportField = Helper.getRandomString(random.nextInt(50) + 1);
        Score score = ScoreTest.getNewValidScore();
        ExportScoreFieldType scoreField = Helper.getRandomEnum(ExportScoreFieldType.class);

        ExportRuleScore exportRuleScore = new ExportRuleScore(exportTemplate, exportField, score,
            scoreField);

        return exportRuleScore;
    }

    @Before
    public void setUp() {
        testExportRuleScore = getNewValidExportRuleScore();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ExportRuleScore#getScoreField} and {@link ExportRuleScore#setScoreField}.<br>
     * Valid input: random {@link ExportScoreFieldType}
     */
    @Test
    public void testGetAndSetScoreField() {
        ExportScoreFieldType testScoreField = Helper.getRandomEnum(ExportScoreFieldType.class);
        testExportRuleScore.setScoreField(testScoreField);
        assertEquals("The getting ExportScoreFieldType was not the expected one", testScoreField,
            testExportRuleScore.getScoreField());
    }

    /**
     * Test of {@link ExportRuleScore#getScore} and {@link ExportRuleScore#setScore}.<br> Valid
     * input: random {@link Score}
     */
    @Test
    public void testGetAndSetScore() {
        Score testScore = ScoreTest.getNewValidScore();
        testExportRuleScore.setScore(testScore);
        assertEquals("The getting Score was not the expected one", testScore,
            testExportRuleScore.getScore());
        // Set same Score again and check set size
        int countExportRules = testScore.getExportRules().size();
        testExportRuleScore.setScore(testScore);
        assertEquals("After adding the same Score twice the set was altered", countExportRules,
            testScore.getExportRules().size());
    }

    /**
     * Test of {@link ExportRuleScore#removeScore}.<br> Valid input: random {@link Score}
     */
    @Test
    public void testRemoveScore() {
        Score testScore = ScoreTest.getNewValidScore();
        testExportRuleScore.setScore(testScore);
        assertNotNull("The Score was null after setting it", testExportRuleScore.getScore());
        testExportRuleScore.removeScore();
        assertNull("The Score was not null after removing it", testExportRuleScore.getScore());
        testExportRuleScore.removeScore();
        assertNull("The Score was not null after removing it again",
            testExportRuleScore.getScore());
    }
}
