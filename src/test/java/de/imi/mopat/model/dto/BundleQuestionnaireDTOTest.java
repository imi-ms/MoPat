package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.utils.Helper;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class BundleQuestionnaireDTOTest {

    private static final Random random = new Random();
    private BundleQuestionnaireDTO testBundleQuestionnaireDTO;

    public BundleQuestionnaireDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testBundleQuestionnaireDTO = new BundleQuestionnaireDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link BundleQuestionnaireDTO#getBundleId} and
     * {@link BundleQuestionnaireDTO#setBundleId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetBundleId() {
        Long testBundleID = Math.abs(random.nextLong());
        testBundleQuestionnaireDTO.setBundleId(testBundleID);
        assertEquals("The getting BundleID was not the expected one", testBundleID,
            testBundleQuestionnaireDTO.getBundleId());
    }

    /**
     * Test of {@link BundleQuestionnaireDTO#getQuestionnaireDTO} and
     * {@link BundleQuestionnaireDTO#setQuestionnaireDTO}.<br> Valid input: random
     * {@link QuestionnaireDTO}
     */
    @Test
    public void testGetAndSetQuestionnaireDTO() {
        QuestionnaireDTO testQuestionnaireDTO = new QuestionnaireDTO();
        testQuestionnaireDTO.setId(Math.abs(random.nextLong()));
        testBundleQuestionnaireDTO.setQuestionnaireDTO(testQuestionnaireDTO);
        assertEquals("The getting QuestionnaireDTO was not the expected one", testQuestionnaireDTO,
            testBundleQuestionnaireDTO.getQuestionnaireDTO());
    }

    /**
     * Test of {@link BundleQuestionnaireDTO#getExportTemplateTypes} and
     * {@link BundleQuestionnaireDTO#setExportTemplateTypes}.<br> Valid input: Set of random
     * ExportTemplateTypes
     */
    @Test
    public void testGetAndSetExportTemplateTypes() {
        Set<ExportTemplateType> testExportTemplateTypes = new HashSet<>();
        int count = random.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            testExportTemplateTypes.add(Helper.getRandomEnum(ExportTemplateType.class));
        }
        testBundleQuestionnaireDTO.setExportTemplateTypes(testExportTemplateTypes);
        assertEquals("The getting set of ExportTemplateTypes was not the expected one",
            testExportTemplateTypes, testBundleQuestionnaireDTO.getExportTemplateTypes());
    }

    /**
     * Test of {@link BundleQuestionnaireDTO#getExportTemplates} and
     * {@link BundleQuestionnaireDTO#setExportTemplates}.<br> Valid input: Set of random Longs
     */
    @Test
    public void testGetAndSetExportTemplates() {
        Set<Long> testExportTemplates = new HashSet<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testExportTemplates.add(random.nextLong());
        }
        testBundleQuestionnaireDTO.setExportTemplates(testExportTemplates);
        assertEquals("The getting set of ExportTemplates was not the expected one",
            testExportTemplates, testBundleQuestionnaireDTO.getExportTemplates());
    }

    /**
     * Test of {@link BundleQuestionnaireDTO#getPosition} and
     * {@link BundleQuestionnaireDTO#setPosition}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetPosition() {
        Long testPosition = random.nextLong();
        testBundleQuestionnaireDTO.setPosition(testPosition);
        assertEquals("The getting position was not the expected one", testPosition,
            testBundleQuestionnaireDTO.getPosition());
    }

    /**
     * Test of {@link BundleQuestionnaireDTO#getIsEnabled} and
     * {@link BundleQuestionnaireDTO#setIsEnabled}.<br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetIsEnabled() {
        Boolean testIsEnabled = random.nextBoolean();
        testBundleQuestionnaireDTO.setIsEnabled(testIsEnabled);
        assertEquals("The getting isEnabeld was not the expected one", testIsEnabled,
            testBundleQuestionnaireDTO.getIsEnabled());
    }
}
