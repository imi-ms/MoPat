package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class BundleDTOTest {

    private static final Random random = new Random();
    private BundleDTO testBundleDTO;

    public BundleDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testBundleDTO = new BundleDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link BundleDTO#getId} and {@link BundleDTO#setId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testBundleDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID, testBundleDTO.getId());
    }

    /**
     * Test of {@link BundleDTO#getName} and {@link BundleDTO#setName}.<br> Valid input: random
     * String
     */
    @Test
    public void testGetAndSetName() {
        String testName = Helper.getRandomString(random.nextInt(100));
        testBundleDTO.setName(testName);
        assertEquals("The getting name was not the expected one", testName,
            testBundleDTO.getName());
    }

    /**
     * Test of {@link BundleDTO#getDescription} and {@link BundleDTO#setDescription}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetDescription() {
        String testDescription = Helper.getRandomString(random.nextInt(100));
        testBundleDTO.setDescription(testDescription);
        assertEquals("The getting description was not the expected one", testDescription,
            testBundleDTO.getDescription());
    }

    /**
     * Test of {@link BundleDTO#getChangedBy} and {@link BundleDTO#setChangedBy}.<br> Valid input:
     * random Long
     */
    @Test
    public void testGetAndSetChangedBy() {
        Long testChangedBy = Math.abs(random.nextLong());
        testBundleDTO.setChangedBy(testChangedBy);
        assertEquals("The getting changedBy was not the expected one", testChangedBy,
            testBundleDTO.getChangedBy());
    }

    /**
     * Test of {@link BundleDTO#getIsPublished} and {@link BundleDTO#setIsPublished}.<br> Valid
     * input: random Boolean
     */
    @Test
    public void testGetAndSetIsPublished() {
        Boolean testIsPublished = random.nextBoolean();
        testBundleDTO.setIsPublished(testIsPublished);
        assertEquals("The getting isPublished was not the expected one", testIsPublished,
            testBundleDTO.getIsPublished());
    }

    /**
     * Test of {@link BundleDTO#getdeactivateProgressAndNameDuringSurvey} and
     * {@link BundleDTO#setdeactivateProgressAndNameDuringSurvey}.<br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetdeactivateProgressAndNameDuringSurvey() {
        Boolean testDeactivateProgressAndNameDuringSurvey = random.nextBoolean();
        testBundleDTO.setdeactivateProgressAndNameDuringSurvey(
            testDeactivateProgressAndNameDuringSurvey);
        assertEquals("The getting isPublished was not the expected one",
            testDeactivateProgressAndNameDuringSurvey,
            testBundleDTO.getdeactivateProgressAndNameDuringSurvey());
    }

    /**
     * Test of {@link BundleDTO#getAvailableLanguages} and
     * {@link BundleDTO#setAvailableLanguages}.<br> Valid input: random list of Strings
     */
    @Test
    public void testGetAndSetAvailableLanguages() {
        List<String> testAvailableLanguages = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testAvailableLanguages.add(Helper.getRandomLocale());
        }
        testBundleDTO.setAvailableLanguages(testAvailableLanguages);
        assertEquals("The getting availableLanguages were not the expected one",
            testAvailableLanguages, testBundleDTO.getAvailableLanguages());
    }

    /**
     * Test of {@link BundleDTO#getBundleQuestionnaireDTOs} and
     * {@link BundleDTO#setBundleQuestionnaireDTOs}.<br> Valid input: random
     * {@link BundleQuestionnaireDTO}
     */
    @Test
    public void testGetAndSetBundleQuestionnaireDTOs() {
        List<BundleQuestionnaireDTO> testBundleQuestionnaireDTOs = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            BundleQuestionnaireDTO testBundleQuestionnaireDTO = new BundleQuestionnaireDTO();
            testBundleQuestionnaireDTO.setBundleId(Math.abs(random.nextLong()));
            testBundleQuestionnaireDTOs.add(testBundleQuestionnaireDTO);
        }
        testBundleDTO.setBundleQuestionnaireDTOs(testBundleQuestionnaireDTOs);
        assertEquals("The getting list of BundleQuestionnaireDTOs was not the expected one",
            testBundleQuestionnaireDTOs, testBundleDTO.getBundleQuestionnaireDTOs());
    }

    /**
     * Test of {@link BundleDTO#getLocalizedWelcomeText} and
     * {@link BundleDTO#setLocalizedWelcomeText}.<br> Valid input: random SortedMap
     */
    @Test
    public void testGetAndSetLocalizedWelcomeText() {
        SortedMap<String, String> testMap = new TreeMap<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testMap.put(Helper.getRandomLocale(), Helper.getRandomString(random.nextInt(50)));
        }
        testBundleDTO.setLocalizedWelcomeText(testMap);
        assertEquals("The getting localizedWelcomeText was not the expected one", testMap,
            testBundleDTO.getLocalizedWelcomeText());
    }

    /**
     * Test of {@link BundleDTO#getLocalizedFinalText} and
     * {@link BundleDTO#setLocalizedFinalText}.<br> Valid input: random SortedMap
     */
    @Test
    public void testGetAndSetLocalizedFinalText() {
        SortedMap<String, String> testMap = new TreeMap<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testMap.put(Helper.getRandomLocale(), Helper.getRandomString(random.nextInt(50)));
        }
        testBundleDTO.setLocalizedFinalText(testMap);
        assertEquals("The getting localizedFinalText was not the expected one", testMap,
            testBundleDTO.getLocalizedFinalText());
    }

    /**
     * Test of {@link BundleDTO#getShowProgressPerBundle} and
     * {@link BundleDTO#setShowProgressPerBundle}.<br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetShowProgressPerBundle() {
        Boolean testShowProgressPerBundle = random.nextBoolean();
        testBundleDTO.setShowProgressPerBundle(testShowProgressPerBundle);
        assertEquals("The getting showProgressPerBundle was not the expected one",
            testShowProgressPerBundle, testBundleDTO.getShowProgressPerBundle());
    }

    /**
     * Test of {@link BundleDTO#getIsModifiable} and {@link BundleDTO#setIsModifiable}.<br> Valid
     * input: random Boolean
     */
    @Test
    public void testGetAndSetIsModifiable() {
        Boolean testIsModifiable = random.nextBoolean();
        testBundleDTO.setIsModifiable(testIsModifiable);
        assertEquals("The getting isModifiable was not the expected one", testIsModifiable,
            testBundleDTO.getIsModifiable());
    }

    /**
     * Test of {@link BundleDTO#hashCode}.<br> Valid input: Id = <code>null</code>, random long
     */
    @Test
    public void testHashCode() {
        testBundleDTO.setId(null);
        assertEquals("The getting hashCode was not the expected one", -1, testBundleDTO.hashCode());
        Long testID = Math.abs(random.nextLong());
        testBundleDTO.setId(testID);
        assertEquals("The getting hashCode was not the expected one", testID.hashCode(),
            testBundleDTO.hashCode());
    }

    /**
     * Test of {@link BundleDTO#equals}.<br> Valid input: same {@link BundleDTO} twice in one
     * HashSet, another {@link BundleDTO} and another {@link Object}
     */
    @Test
    public void testEquals() {
        testBundleDTO.setId(Math.abs(random.nextLong()));
        Set<BundleDTO> testSet = new HashSet<>();
        testSet.add(testBundleDTO);
        testSet.add(testBundleDTO);
        assertEquals("It was possible to add one BundleDTO twice to a HashSet", 1, testSet.size());
        assertEquals("A BundleDTO was not equal to itself", testBundleDTO, testBundleDTO);
        Object otherObject = new Object();
        assertNotEquals("A BundleDTO was equal to another Object", testBundleDTO, otherObject);
        BundleDTO otherBundleDTO = null;
        assertNotEquals("A BundleDTO was equal to null", testBundleDTO, otherBundleDTO);
        otherBundleDTO = new BundleDTO();
        otherBundleDTO.setId(null);
        assertNotEquals("A BundleDTO was equal to different BundleDTO with ID null", testBundleDTO,
            otherBundleDTO);
        otherBundleDTO.setId(Math.abs(random.nextLong()));
        testBundleDTO.setId(null);
        assertNotEquals("A BundleDTO was equal to different BundleDTO altough its ID was null",
            testBundleDTO, otherBundleDTO);
        testBundleDTO.setId(Math.abs(random.nextLong()));
        assertNotEquals("A BundleDTO was equal to different BundleDTO", testBundleDTO,
            otherBundleDTO);
    }

    /**
     * Test of {@link BundleDTO#getJSON}.<br> Valid input: valid {@link BundleDTO} with random Id,
     * name, localized welcome/final texts, showProgressperBundle,
     * deactivateProgressAndNameDuringSurvey and
     * {@link BundleQuestionnaireDTO BundleQuestionnaireDTOs}
     */
    @Test
    public void testGetJSON() {
        testBundleDTO.setId(Math.abs(random.nextLong()));
        testBundleDTO.setName(Helper.getRandomAlphanumericString(random.nextInt(50) + 1));
        SortedMap<String, String> testLocalizedWelcomeTexts = new TreeMap<>();
        int countLocalizedWelcomeTexts = random.nextInt(200) + 1;
        for (int i = 0; i < countLocalizedWelcomeTexts; i++) {
            testLocalizedWelcomeTexts.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(50)));
        }
        testBundleDTO.setLocalizedWelcomeText(testLocalizedWelcomeTexts);
        SortedMap<String, String> testLocalizedFinalTexts = new TreeMap<>();
        int countLocalizedFinalTexts = random.nextInt(200) + 1;
        for (int i = 0; i < countLocalizedFinalTexts; i++) {
            testLocalizedFinalTexts.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(50)));
        }
        testBundleDTO.setLocalizedFinalText(testLocalizedFinalTexts);
        testBundleDTO.setShowProgressPerBundle(random.nextBoolean());
        testBundleDTO.setdeactivateProgressAndNameDuringSurvey(random.nextBoolean());
        List<BundleQuestionnaireDTO> testBundleQuestionnaireDTOs = new ArrayList<>();
        int countBundleQuestionnaireDTOs = random.nextInt(200) + 1;
        for (int i = 0; i < countBundleQuestionnaireDTOs; i++) {
            BundleQuestionnaireDTO testBundleQuestionnaireDTO = new BundleQuestionnaireDTO();
            testBundleQuestionnaireDTO.setBundleId(Math.abs(random.nextLong()));
            testBundleQuestionnaireDTOs.add(testBundleQuestionnaireDTO);
        }
        testBundleDTO.setBundleQuestionnaireDTOs(testBundleQuestionnaireDTOs);

        StringBuilder testJSON = new StringBuilder();
        testJSON.append("{\"id\":").append(testBundleDTO.getId());
        testJSON.append(",\"name\":\"").append(testBundleDTO.getName()).append("\"");
        testJSON.append(",\"localizedWelcomeText\":{");
        for (Map.Entry<String, String> entry : testBundleDTO.getLocalizedWelcomeText().entrySet()) {
            testJSON.append("\"");
            testJSON.append(entry.getKey());
            testJSON.append("\":\"");
            testJSON.append(entry.getValue());
            testJSON.append("\",");
        }
        testJSON.deleteCharAt(testJSON.length() - 1);
        testJSON.append("},\"localizedFinalText\":{");
        for (Map.Entry<String, String> entry : testBundleDTO.getLocalizedFinalText().entrySet()) {
            testJSON.append("\"");
            testJSON.append(entry.getKey());
            testJSON.append("\":\"");
            testJSON.append(entry.getValue());
            testJSON.append("\",");
        }
        testJSON.deleteCharAt(testJSON.length() - 1);
        testJSON.append("},\"showProgressPerBundle\":")
            .append(testBundleDTO.getShowProgressPerBundle());
        testJSON.append(",\"deactivateProgressAndNameDuringSurvey\":")
            .append(testBundleDTO.getdeactivateProgressAndNameDuringSurvey());
        testJSON.append(",\"bundleQuestionnaireDTOs\":[");
        for (BundleQuestionnaireDTO bundleQuestionaireDTO : testBundleDTO.getBundleQuestionnaireDTOs()) {
            testJSON.append("{\"questionnaireDTO\":")
                .append(bundleQuestionaireDTO.getQuestionnaireDTO());
            testJSON.append(",\"position\":").append(bundleQuestionaireDTO.getPosition());
            testJSON.append(",\"isEnabled\":").append(bundleQuestionaireDTO.getIsEnabled());
            testJSON.append(",\"showScores\":").append(bundleQuestionaireDTO.getShowScores());
            testJSON.append(",\"bundleId\":").append(bundleQuestionaireDTO.getBundleId());
            testJSON.append("},");
        }
        testJSON.deleteCharAt(testJSON.length() - 1);
        testJSON.append("]}");

        assertEquals("The getting JSON was not the expected one", testJSON.toString(),
            testBundleDTO.getJSON());
    }
}
