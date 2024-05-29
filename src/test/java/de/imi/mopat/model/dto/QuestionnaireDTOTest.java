package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.ExportTemplateTest;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
public class QuestionnaireDTOTest {

    private static final Random random = new Random();
    private QuestionnaireDTO testQuestionnaireDTO;

    public QuestionnaireDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testQuestionnaireDTO = new QuestionnaireDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link QuestionnaireDTO#getQuestionDTOs} and
     * {@link QuestionnaireDTO#setQuestionDTOs}.<br> Valid input: random list of
     * {@link QuestionDTO QuestionDTOs}
     */
    @Test
    public void testGetAndSetQuestionDTOs() {
        List<QuestionDTO> testQuestionDTOs = new ArrayList<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testQuestionDTOs.add(new QuestionDTO());
        }
        testQuestionnaireDTO.setQuestionDTOs(testQuestionDTOs);
        assertEquals("The getting list of QuestionDTOs was not the expected one", testQuestionDTOs,
            testQuestionnaireDTO.getQuestionDTOs());
    }

    /**
     * Test of {@link QuestionnaireDTO#getExportTemplates} and
     * {@link QuestionnaireDTO#setExportTemplates}.<br> Valid input: random set of
     * {@link ExportTemplate ExportTemplates}
     */
    @Test
    public void testGetAndSetExportTemplates() {
        Set<ExportTemplate> testExportTemplates = new HashSet<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testExportTemplates.add(ExportTemplateTest.getNewValidExportTemplate());
        }
        testQuestionnaireDTO.setExportTemplates(testExportTemplates);
        assertEquals("The getting set of ExportTemplates was not the expected one",
            testExportTemplates, testQuestionnaireDTO.getExportTemplates());
    }

    /**
     * Test of {@link QuestionnaireDTO#getId} and {@link QuestionnaireDTO#setId}.<br> Valid input:
     * random Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testQuestionnaireDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID,
            testQuestionnaireDTO.getId());
    }

    /**
     * Test of {@link QuestionnaireDTO#isDeleteLogo} and {@link QuestionnaireDTO#setDeleteLogo}.<br>
     * Valid input: random Boolean
     */
    @Test
    public void testIsAndSetDeleteLogo() {
        Boolean testDeleteLogo = random.nextBoolean();
        testQuestionnaireDTO.setDeleteLogo(testDeleteLogo);
        assertEquals("The getting DeleteLogo was not the expected one", testDeleteLogo,
            testQuestionnaireDTO.isDeleteLogo());
    }

    /**
     * Test of {@link QuestionnaireDTO#getLogo} and {@link QuestionnaireDTO#setLogo}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetLogo() {
        String testLogo = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testQuestionnaireDTO.setLogo(testLogo);
        assertEquals("The getting Logo was not the expected one", testLogo,
            testQuestionnaireDTO.getLogo());
    }

    /**
     * Test of {@link QuestionnaireDTO#getName} and {@link QuestionnaireDTO#setName}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetName() {
        String testName = Helper.getRandomAlphabeticString(random.nextInt(50) + 1);
        testQuestionnaireDTO.setName(testName);
        assertEquals("The getting Name was not the expected one", testName,
            testQuestionnaireDTO.getName());
    }

    /**
     * Test of {@link QuestionnaireDTO#getDescription} and
     * {@link QuestionnaireDTO#setDescription}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetDescription() {
        String testDescription = Helper.getRandomString(random.nextInt(50) + 1);
        testQuestionnaireDTO.setDescription(testDescription);
        assertEquals("The getting Description was not the expected one", testDescription,
            testQuestionnaireDTO.getDescription());
    }

    /**
     * Test of {@link QuestionnaireDTO#getLocalizedWelcomeText} and
     * {@link QuestionnaireDTO#setLocalizedWelcomeText}.<br> Valid input: random Map of locales and
     * texts as String
     */
    @Test
    public void testGetAndSetLocalizedWelcomeText() {
        SortedMap<String, String> testLocalizedWelcomeText = new TreeMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testLocalizedWelcomeText.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(200) + 1));
        }
        testQuestionnaireDTO.setLocalizedWelcomeText(testLocalizedWelcomeText);
        assertEquals("The getting Map of LocalizedWelcomeText was not the expected one",
            testLocalizedWelcomeText, testQuestionnaireDTO.getLocalizedWelcomeText());
    }

    /**
     * Test of {@link QuestionnaireDTO#getLocalizedFinalText} and
     * {@link QuestionnaireDTO#setLocalizedFinalText}.<br> Valid input: random Map of locales and
     * texts as String
     */
    @Test
    public void testGetAndSetLocalizedFinalText() {
        SortedMap<String, String> testLocalizedFinalText = new TreeMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testLocalizedFinalText.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(200) + 1));
        }
        testQuestionnaireDTO.setLocalizedFinalText(testLocalizedFinalText);
        assertEquals("The getting Map of LocalizedFinalText was not the expected one",
            testLocalizedFinalText, testQuestionnaireDTO.getLocalizedFinalText());
    }

    /**
     * Test of {@link QuestionnaireDTO#getLocalizedDisplayName} and
     * {@link QuestionnaireDTO#setLocalizedDisplayName}.<br> Valid input: random Map of locales and
     * texts as String
     */
    @Test
    public void testGetAndSetLocalizedDisplayName() {
        SortedMap<String, String> testLocalizedDisplayName = new TreeMap<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testLocalizedDisplayName.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(200) + 1));
        }
        testQuestionnaireDTO.setLocalizedDisplayName(testLocalizedDisplayName);
        assertEquals("The getting Map of LocalizedDisplayName was not the expected one",
            testLocalizedDisplayName, testQuestionnaireDTO.getLocalizedDisplayName());
    }

    /**
     * Test of {@link QuestionnaireDTO#getHasConditionsAsTarget} and
     * {@link QuestionnaireDTO#setHasConditionsAsTarget}.<br> Valid input: random Boolean
     */
    @Test
    public void testGetandSetHasConditionsAsTarget() {
        Boolean testHasConditionsAsTarget = random.nextBoolean();
        testQuestionnaireDTO.setHasConditionsAsTarget(testHasConditionsAsTarget);
        assertEquals("The getting hasConditionsAsTarget was not the expected one",
            testHasConditionsAsTarget, testQuestionnaireDTO.getHasConditionsAsTarget());
    }

    /**
     * Test of {@link QuestionnaireDTO#getHasScores} and {@link QuestionnaireDTO#setHasScores}.<br>
     * Valid input: random Boolean
     */
    @Test
    public void testGetandSetHasScores() {
        Boolean testHasScores = random.nextBoolean();
        testQuestionnaireDTO.setHasScores(testHasScores);
        assertEquals("The getting hasScores was not the expected one", testHasScores,
            testQuestionnaireDTO.getHasScores());
    }

    /**
     * Test of {@link QuestionnaireDTO#equals}.<br> Valid input: the same and another
     * {@link QuestionnaireDTO}
     */
    @Test
    public void testEquals() {
        testQuestionnaireDTO.setId(Math.abs(random.nextLong()));
        Set<QuestionnaireDTO> testSet = new HashSet<>();
        testSet.add(testQuestionnaireDTO);
        testSet.add(testQuestionnaireDTO);
        assertEquals("It was possible to add the same QuestionnaireDTO twice to one set", 1,
            testSet.size());

        QuestionnaireDTO otherQuestionnaireDTO = new QuestionnaireDTO();
        otherQuestionnaireDTO.setId(Math.abs(random.nextLong()));
        assertEquals("The QuestionnaireDTO was not equalto itself", testQuestionnaireDTO,
            testQuestionnaireDTO);
        assertNotEquals("The QuestionnaireDTO was equal to another one", testQuestionnaireDTO,
            otherQuestionnaireDTO);
    }
}
