package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.utils.Helper;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class QuestionDTOTest {

    private static final Random random = new Random();
    private QuestionDTO testQuestionDTO;

    public QuestionDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testQuestionDTO = new QuestionDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link QuestionDTO#getId} and {@link QuestionDTO#setId}.<br> Valid input: random
     * Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testQuestionDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID, testQuestionDTO.getId());
    }

    /**
     * Test of {@link QuestionDTO#getPosition} and {@link QuestionDTO#svvetPosition}.<br> Valid
     * input: random Integer
     */
    @Test
    public void testGetAndSetPosition() {
        Integer testPosition = Math.abs(random.nextInt());
        testQuestionDTO.setPosition(testPosition);
        assertEquals("The getting Position was not the expected one", testPosition,
            testQuestionDTO.getPosition());
    }

    /**
     * Test of {@link QuestionDTO#getLocalizedQuestionText} and
     * {@link QuestionDTO#setLocalizedQuestionText}.<br> Valid input: random Map of locales And
     * texts as Strings
     */
    @Test
    public void testGetAndSetLocalizedQuestionText() {
        SortedMap<String, String> testLocalizedQuestionText = new TreeMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testLocalizedQuestionText.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(200) + 1));
        }
        testQuestionDTO.setLocalizedQuestionText(testLocalizedQuestionText);
        assertEquals("The getting LocalizedQuestionText was not the expected one",
            testLocalizedQuestionText, testQuestionDTO.getLocalizedQuestionText());
    }

    /**
     * Test of {@link QuestionDTO#getIsRequired} and {@link QuestionDTO#setIsRequired}.<br> Valid
     * input: random Boolean
     */
    @Test
    public void testGetAndSetIsRequired() {
        Boolean testIsRequired = random.nextBoolean();
        testQuestionDTO.setIsRequired(testIsRequired);
        assertEquals("The getting isRequired was not the expected one", testIsRequired,
            testQuestionDTO.getIsRequired());
    }

    /**
     * Test of {@link QuestionDTO#getIsEnabled} and {@link QuestionDTO#setIsEnabled}.<br> Valid
     * input: random Boolean
     */
    @Test
    public void testGetAndSetIsEnabled() {
        Boolean testIsEnabled = random.nextBoolean();
        testQuestionDTO.setIsEnabled(testIsEnabled);
        assertEquals("The getting isEnabled was not the expected one", testIsEnabled,
            testQuestionDTO.getIsEnabled());
    }

    /**
     * Test of {@link QuestionDTO#getQuestionType} and {@link QuestionDTO#setQuestionType}.<br>
     * Valid input: random {@link QuestionType}
     */
    @Test
    public void testGetAndSetQuestionType() {
        QuestionType testQuestionType = Helper.getRandomEnum(QuestionType.class);
        testQuestionDTO.setQuestionType(testQuestionType);
        assertEquals("The getting QuestionType was not the expected one", testQuestionType,
            testQuestionDTO.getQuestionType());
    }

    /**
     * Test of {@link QuestionDTO#getMinNumberAnswers} and
     * {@link QuestionDTO#setMinNumberAnswers}.<br> Valid input: random Integer
     */
    @Test
    public void testGetAndSetMinNumberAnswers() {
        Integer testMinNumberAnswers = Math.abs(random.nextInt());
        testQuestionDTO.setMinNumberAnswers(testMinNumberAnswers);
        assertEquals("The getting MinNumberAnswers was not the expected one", testMinNumberAnswers,
            testQuestionDTO.getMinNumberAnswers());
    }

    /**
     * Test of {@link QuestionDTO#getMaxNumberAnswers} and
     * {@link QuestionDTO#setMaxNumberAnswers}.<br> Valid input: random Integer
     */
    @Test
    public void testGetAndSetMaxNumberAnswers() {
        Integer testMaxNumberAnswers = Math.abs(random.nextInt());
        testQuestionDTO.setMaxNumberAnswers(testMaxNumberAnswers);
        assertEquals("The getting MaxNumberAnswers was not the expected one", testMaxNumberAnswers,
            testQuestionDTO.getMaxNumberAnswers());
    }

    /**
     * Test of {@link QuestionDTO#getAnswers} and {@link QuestionDTO#setAnswers}.<br> Valid input:
     * random Map of Longs and {@link AnswerDTO AnswerDTOs}
     */
    @Test
    public void testGetAndSetAnswers() {
        SortedMap<Long, AnswerDTO> testAnswers = new TreeMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testAnswers.put(Math.abs(random.nextLong()), new AnswerDTO());
        }
        testQuestionDTO.setAnswers(testAnswers);
        assertEquals("The getting map of AnswerDTOs was not the expected one", testAnswers,
            testQuestionDTO.getAnswers());
    }

    /**
     * Test of {@link QuestionDTO#getQuestionnaireId} and
     * {@link QuestionDTO#setQuestionnaireId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetQuestionnaireId() {
        Long testQuestionnaireID = Math.abs(random.nextLong());
        testQuestionDTO.setQuestionnaireId(testQuestionnaireID);
        assertEquals("The getting QuestionnaireID was not the expected one", testQuestionnaireID,
            testQuestionDTO.getQuestionnaireId());
    }

    /**
     * Test of {@link QuestionDTO#isHasScores} and {@link QuestionDTO#setHasScores}.<br> Valid
     * input: random boolean
     */
    @Test
    public void testIsAndSetHasScores() {
        boolean testHasScores = random.nextBoolean();
        testQuestionDTO.setHasScores(testHasScores);
        assertEquals("The getting hasScores value was not the expected one", testHasScores,
            testQuestionDTO.isHasScores());
    }

    /**
     * Test of {@link QuestionDTO#isModifiable}.<br> Valid input: {@link AnswerDTO} =
     * <code>null</code>, {@link AnswerDTO} without {@link Response Responses} and {@ink AnswerDTO}
     * with {@link Response Responses}
     */
    @Test
    public void testIsModifiable() {
        SortedMap<Long, AnswerDTO> testAnswers = null;
        testQuestionDTO.setAnswers(testAnswers);
        assertTrue("The QuestionDTO was not modifiable altough there were no AnswerDTOs",
            testQuestionDTO.isModifiable());

        testAnswers = new TreeMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testAnswers.put(Math.abs(random.nextLong()), new AnswerDTO());
        }
        testQuestionDTO.setAnswers(testAnswers);
        assertTrue("The QuestionDTO was not modifiable altough there were no Responses",
            testQuestionDTO.isModifiable());

        testAnswers = new TreeMap<>();
        count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            AnswerDTO testAnswerDTO = new AnswerDTO();
            testAnswerDTO.setHasResponse(true);
            testAnswers.put(Math.abs(random.nextLong()), testAnswerDTO);
        }
        testQuestionDTO.setAnswers(testAnswers);
        assertFalse("The QuestionDTO was modifiable altough there were Responses",
            testQuestionDTO.isModifiable());
    }

    /**
     * Test of {@link QuestionDTO#hasExportRules}.<br> Valid input: {@link AnswerDTO AnswerDTOs}
     * without {@link ExportRule ExportRules} and {@link AnswerDTO AnswerDTOs} with
     * {@link ExportRule ExportRules}
     */
    @Test
    public void testHasExportRules() {
        SortedMap<Long, AnswerDTO> testAnswers = new TreeMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testAnswers.put(Math.abs(random.nextLong()), new AnswerDTO());
        }
        testQuestionDTO.setAnswers(testAnswers);
        assertFalse("The QuestionDTO had ExportRules although the AnswerDTOs had no ExportRules",
            testQuestionDTO.hasExportRules());

        testAnswers = new TreeMap<>();
        count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            AnswerDTO testAnswerDTO = new AnswerDTO();
            testAnswerDTO.setHasExportRule(true);
            testAnswers.put(Math.abs(random.nextLong()), testAnswerDTO);
        }
        testQuestionDTO.setAnswers(testAnswers);
        assertTrue("The QuestionDTO had no ExportRules although the AnswerDTOs had ExportRules",
            testQuestionDTO.hasExportRules());
    }

    /**
     * Test of {@link QuestionDTO#hasConditionsAsTrigger}.<br> Valid input: {@link AnswerDTO}
     * without {@link Condition Conditions} as {@link ConditionTrigger Trigger} and
     * {@link AnswerDTO} with {@link Condition Conditions} as {@link ConditionTrigger Trigger}
     */
    @Test
    public void testHasConditionsAsTrigger() {
        SortedMap<Long, AnswerDTO> testAnswers = new TreeMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testAnswers.put(Math.abs(random.nextLong()), new AnswerDTO());
        }
        testQuestionDTO.setAnswers(testAnswers);
        assertFalse(
            "The QuestionDTO had Conditions as Trigger although the AnswerDTOs had no Conditions as Trigger",
            testQuestionDTO.hasConditionsAsTrigger());

        testAnswers = new TreeMap<>();
        count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            AnswerDTO testAnswerDTO = new AnswerDTO();
            testAnswerDTO.setHasConditionsAsTrigger(true);
            testAnswers.put(Math.abs(random.nextLong()), testAnswerDTO);
        }
        testQuestionDTO.setAnswers(testAnswers);
        assertTrue(
            "The QuestionDTO had no Conditions as Trigger although the AnswerDTOs had Conditions as Trigger",
            testQuestionDTO.hasConditionsAsTrigger());
    }
}
