package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

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
public class ScoreDTOTest {

    private static final Random random = new Random();
    private ScoreDTO testScoreDTO;

    public ScoreDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testScoreDTO = new ScoreDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ScoreDTO#getId} and {@link ScoreDTO#setId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testScoreDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID, testScoreDTO.getId());
    }

    /**
     * Test of {@link ScoreDTO#getName} and {@link ScoreDTO#setName}.<br> Valid input: random
     * String
     */
    @Test
    public void testGetAndSetName() {
        String testName = Helper.getRandomAlphabeticString(random.nextInt(50) + 1);
        testScoreDTO.setName(testName);
        assertEquals("The getting Name was not the expected one", testName, testScoreDTO.getName());
    }

    /**
     * Test of {@link ScoreDTO#getQuestionnaireId} and {@link ScoreDTO#setQuestionnaireId}.<br>
     * Valid input: random Long
     */
    @Test
    public void testGetAndSetQuestionnaireId() {
        Long testQuestionnaireID = Math.abs(random.nextLong());
        testScoreDTO.setQuestionnaireId(testQuestionnaireID);
        assertEquals("The getting QuestionnaireID was not the expected one", testQuestionnaireID,
            testScoreDTO.getQuestionnaireId());
    }

    /**
     * Test of {@link ScoreDTO#getExpression} and {@link ScoreDTO#setExpression}.<br> Valid input:
     * random {@link ExpressionDTO}
     */
    @Test
    public void testGetAndSetExpression() {
        ExpressionDTO testExppressionDTO = new ExpressionDTO();
        testScoreDTO.setExpression(testExppressionDTO);
        assertEquals("The getting ExpressionDTO was not the expected one", testExppressionDTO,
            testScoreDTO.getExpression());
    }

    /**
     * Test of {@link ScoreDTO#getDependingScoreNames} and
     * {@link ScoreDTO#setDependingScoreNames}.<br> Valid input: list with random number of random
     * Strings
     */
    @Test
    public void testGetAndSetDependingScoreNames() {
        int countDependingScoreNames = random.nextInt(50) + 1;
        List<String> testDependingScoreNames = new ArrayList<>();
        for (int i = 0; i < countDependingScoreNames; i++) {
            testDependingScoreNames.add(Helper.getRandomAlphanumericString(random.nextInt(50) + 5));
        }
        testScoreDTO.setDependingScoreNames(testDependingScoreNames);
        assertEquals("The getting list of depnedingScoreNames was not the expected one",
            testDependingScoreNames, testScoreDTO.getDependingScoreNames());
    }

    /**
     * Test of {@link ScoreDTO#hasExportRules} and {@link ScoreDTO#setHasExportRules}.<br> Valid
     * input: random Boolean
     */
    @Test
    public void testSetAndHasExportRules() {
        boolean testHasExportRules = random.nextBoolean();
        testScoreDTO.setHasExportRules(testHasExportRules);
        assertEquals("The getting hasExportRules was not the expected one", testHasExportRules,
            testScoreDTO.hasExportRules());
    }
}
