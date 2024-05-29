package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
public class ConditionListDTOTest {

    private static final Random random = new Random();
    private int size;
    private ConditionListDTO conditionListDTO;

    public ConditionListDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        conditionListDTO = new ConditionListDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ConditionListDTO#getAvailableBundleDTOs()} and
     * {@link ConditionListDTO#setAvailableBundleDTOs(java.util.List)}. Valid input: List of
     * {@link BundleDTO} objects.
     */
    @Test
    public void testGetAndSetAvailableBundleDTOs() {
        size = random.nextInt(10) + 3;

        List<BundleDTO> bundleDTOs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            BundleDTO bundleDTO = new BundleDTO();
            bundleDTO.setId(random.nextLong());
            bundleDTO.setName(Helper.getRandomAlphabeticString(random.nextInt(10) + 3));
            bundleDTOs.add(bundleDTO);
        }

        conditionListDTO.setAvailableBundleDTOs(bundleDTOs);

        List<BundleDTO> bundleDTOsTest = conditionListDTO.getAvailableBundleDTOs();
        for (BundleDTO bundleDTO : bundleDTOsTest) {
            BundleDTO bundleDTOToCompare = bundleDTOsTest.get(bundleDTOsTest.indexOf(bundleDTO));
            if (!bundleDTO.getId().equals(bundleDTOToCompare.getId())) {
                fail(
                    "Evaluating get and set availableBundleDTOs failed. Returned list contains wrong element or element at wrong position.");
            }
        }

        assertEquals(
            "Evaluating get and set availableBundleDTOs failed. Returned list doesn't match the expected size",
            bundleDTOs.size(), bundleDTOsTest.size());
    }

    /**
     * Test of {@link ConditionListDTO#getAvailableBundleQuestionnaireDTOs()} and
     * {@link ConditionListDTO#setAvailableBundleQuestionnaireDTOs(java.util.List)}. Valid input:
     * List of {@link BundleQuestionnaireDTO} objects.
     */
    @Test
    public void testGetAndSetAvailableBundleQuestionnaireDTOs() {
        size = random.nextInt(10) + 3;

        List<BundleQuestionnaireDTO> bundleQuestionnaireDTOs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            QuestionnaireDTO questionnaireDTO = new QuestionnaireDTO();
            questionnaireDTO.setId(random.nextLong());
            BundleQuestionnaireDTO bundleQuestionnaireDTO = new BundleQuestionnaireDTO();
            bundleQuestionnaireDTO.setBundleId(random.nextLong());
            bundleQuestionnaireDTO.setQuestionnaireDTO(questionnaireDTO);
            bundleQuestionnaireDTOs.add(bundleQuestionnaireDTO);
        }

        conditionListDTO.setAvailableBundleQuestionnaireDTOs(bundleQuestionnaireDTOs);

        List<BundleQuestionnaireDTO> bundleQuestionnaireDTOsTest = conditionListDTO.getAvailableBundleQuestionnaireDTOs();
        for (BundleQuestionnaireDTO bundleQuestionnaireDTO : bundleQuestionnaireDTOsTest) {
            BundleQuestionnaireDTO bundleQuestionnaireDTOToCompare = bundleQuestionnaireDTOsTest.get(
                bundleQuestionnaireDTOsTest.indexOf(bundleQuestionnaireDTO));
            if (!(bundleQuestionnaireDTO.getBundleId()
                .equals(bundleQuestionnaireDTOToCompare.getBundleId())
                && bundleQuestionnaireDTO.getQuestionnaireDTO().getId()
                .equals(bundleQuestionnaireDTOToCompare.getQuestionnaireDTO().getId()))) {
                fail(
                    "Evaluating testGetAndSetAvailableBundleQuestionnaireDTOs failed. Returned list contains wrong element or element at wrong position.");
            }
        }

        assertEquals(
            "Evaluating testGetAndSetAvailableBundleQuestionnaireDTOs failed. Returned list doesn't match the expected size",
            bundleQuestionnaireDTOs.size(), bundleQuestionnaireDTOsTest.size());
    }

    /**
     * Tes of {@link ConditionListDTO#getConditionDTOs()} and
     * {@link ConditionListDTO#setConditionDTOs(java.util.List)}. Valid input: List of
     * {@link ConditionDTO} objects.
     */
    @Test
    public void testGetAndSetConditionDTOs() {
        size = random.nextInt(10) + 3;

        List<ConditionDTO> conditionDTOs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ConditionDTO conditionDTO = new ConditionDTO();
            conditionDTO.setId(random.nextLong());
            conditionDTOs.add(conditionDTO);
        }

        conditionListDTO.setConditionDTOs(conditionDTOs);

        List<ConditionDTO> conditionDTOsTest = conditionListDTO.getConditionDTOs();
        for (ConditionDTO conditionDTO : conditionDTOsTest) {
            ConditionDTO conditionDTOToCompare = conditionDTOsTest.get(
                conditionDTOsTest.indexOf(conditionDTO));
            if (!(conditionDTO.getId().equals(conditionDTOToCompare.getId()))) {
                fail(
                    "Evaluating testGetAndSetConditionDTOs failed. Returned list contains wrong element or element at wrong position.");
            }
        }

        assertEquals(
            "Evaluating testGetAndSetConditionDTOs failed. Returned list doesn't match the expected size",
            conditionDTOs.size(), conditionDTOsTest.size());
    }
}
