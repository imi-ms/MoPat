package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.helper.model.ConditionDTOMapper;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTest;
import de.imi.mopat.utils.Helper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
public class AnswerDTOTest {

    private static final Random random = new Random();
    private AnswerDTO testAnswerDTO;
    private ConditionDTOMapper conditionDTOMapper = new ConditionDTOMapper();

    public AnswerDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testAnswerDTO = new AnswerDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link AnswerDTO#getId} and {@link AnswerDTO#setId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testAnswerDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID, testAnswerDTO.getId());
    }

    /**
     * Test of {@link AnswerDTO#getStartDate} and {@link AnswerDTO#setStartDate}.<br> Valid input:
     * random String
     */
    @Test
    public void testGetAndSetStartDate() {
        String testStartDate = Helper.getRandomString(random.nextInt(50));
        testAnswerDTO.setStartDate(testStartDate);
        assertEquals("The getting startDate was not the expected one", testStartDate,
            testAnswerDTO.getStartDate());
    }

    /**
     * Test of {@link AnswerDTO#getEndDate} and {@link AnswerDTO#setEndDate}.<br> Valid input:
     * random String
     */
    @Test
    public void testGetAndSetEndDate() {
        String testEndDate = Helper.getRandomString(random.nextInt(50));
        testAnswerDTO.setEndDate(testEndDate);
        assertEquals("The getting endDate was not the expected one", testEndDate,
            testAnswerDTO.getEndDate());
    }

    /**
     * Test of {@link AnswerDTO#getValue} and {@link AnswerDTO#setValue}.<br> Valid input: random
     * Double
     */
    @Test
    public void testGetAndSetValue() {
        Double testValue = random.nextDouble();
        testAnswerDTO.setValue(testValue);
        assertEquals("The getting value was not the expected one", testValue,
            testAnswerDTO.getValue());
    }

    /**
     * Test of {@link AnswerDTO#getMinValue} and {@link AnswerDTO#setMinValue}.<br> Valid input:
     * random Double
     */
    @Test
    public void testGetAndSetMinValue() {
        Double testMinValue = random.nextDouble();
        testAnswerDTO.setMinValue(testMinValue);
        assertEquals("The getting minValue was not the expected one", testMinValue,
            testAnswerDTO.getMinValue());
    }

    /**
     * Test of {@link AnswerDTO#getMaxValue} and {@link AnswerDTO#setMaxValue}.<br> Valid input:
     * random Double
     */
    @Test
    public void testGetAndSetMaxValue() {
        Double testMaxValue = random.nextDouble();
        testAnswerDTO.setMaxValue(testMaxValue);
        assertEquals("The getting maxValue was not the expected one", testMaxValue,
            testAnswerDTO.getMaxValue());
    }

    /**
     * Test of {@link AnswerDTO#getStepsize} and {@link AnswerDTO#setStepsize}.<br> Valid input:
     * random String
     */
    @Test
    public void testGetAndSetStepsize() {
        String testStepsize = Helper.getRandomString(random.nextInt(50));
        testAnswerDTO.setStepsize(testStepsize);
        assertEquals("The getting stepsize was not the expected one", testStepsize,
            testAnswerDTO.getStepsize());
    }

    /**
     * Test of {@link AnswerDTO#getLocalizedLabel} and {@link AnswerDTO#setLocalizedLabel}.<br>
     * Valid input: random SortedMap
     */
    @Test
    public void testGetAndSetLocalizedLabel() {
        SortedMap<String, String> testMap = new TreeMap<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testMap.put(Helper.getRandomLocale(), Helper.getRandomString(random.nextInt(50)));
        }
        testAnswerDTO.setLocalizedLabel(testMap);
        assertEquals("The getting localizedLabel was not the expected one", testMap,
            testAnswerDTO.getLocalizedLabel());
    }

    /**
     * Test of {@link AnswerDTO#getLocalizedMaximumText} and
     * {@link AnswerDTO#setLocalizedMaximumText}.<br> Valid input: random SortedMap
     */
    @Test
    public void testGetAndSetLocalizedMaximumText() {
        SortedMap<String, String> testMap = new TreeMap<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testMap.put(Helper.getRandomLocale(), Helper.getRandomString(random.nextInt(50)));
        }
        testAnswerDTO.setLocalizedMaximumText(testMap);
        assertEquals("The getting localizedMaximumText was not the expected one", testMap,
            testAnswerDTO.getLocalizedMaximumText());
    }

    /**
     * Test of {@link AnswerDTO#getLocalizedMinimumText} and
     * {@link AnswerDTO#setLocalizedMinimumText}.<br> Valid input: random SortedMap
     */
    @Test
    public void testGetAndSetLocalizedMinimumText() {
        SortedMap<String, String> testMap = new TreeMap<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testMap.put(Helper.getRandomLocale(), Helper.getRandomString(random.nextInt(50)));
        }
        testAnswerDTO.setLocalizedMinimumText(testMap);
        assertEquals("The getting localizedMinimumText was not the expected one", testMap,
            testAnswerDTO.getLocalizedMinimumText());
    }

    /**
     * Test of {@link AnswerDTO#getLocalizedFreetextLabel} and
     * {@link AnswerDTO#setLocalizedFreetextLabel}.<br> Valid input: random SortedMap
     */
    @Test
    public void testGetAndSetLocalizedFreetextLabel() {
        SortedMap<String, String> testMap = new TreeMap<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testMap.put(Helper.getRandomLocale(), Helper.getRandomString(random.nextInt(50)));
        }
        testAnswerDTO.setLocalizedFreetextLabel(testMap);
        assertEquals("The getting localizedFreetextLabel was not the expected one", testMap,
            testAnswerDTO.getLocalizedFreetextLabel());
    }

    /**
     * Test of {@link AnswerDTO#getVertical} and {@link AnswerDTO#setVertical}.<br> Valid input:
     * random Boolean
     */
    @Test
    public void testGetAndSetVertical() {
        Boolean testVertical = random.nextBoolean();
        testAnswerDTO.setVertical(testVertical);
        assertEquals("The getting vertical was not the expected one", testVertical,
            testAnswerDTO.getVertical());
    }

    /**
     * Test of {@link AnswerDTO#getShowValueOnButton} and
     * {@link AnswerDTO#setShowValueOnButton}.<br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetShowValueOnButton() {
        Boolean testShowValueOnButton = random.nextBoolean();
        testAnswerDTO.setShowValueOnButton(testShowValueOnButton);
        assertEquals("The getting showValueOnButton was not the expected one",
            testShowValueOnButton, testAnswerDTO.getShowValueOnButton());
    }

    /**
     * Test of {@link AnswerDTO#getIsEnabled} and {@link AnswerDTO#setIsEnabled}.<br> Valid input:
     * random Boolean
     */
    @Test
    public void testGetAndSetIsEnabled() {
        Boolean testIsEnabled = random.nextBoolean();
        testAnswerDTO.setIsEnabled(testIsEnabled);
        assertEquals("The getting isEnabled was not the expected one", testIsEnabled,
            testAnswerDTO.getIsEnabled());
    }

    /**
     * Test of {@link AnswerDTO#getImagePath} and {@link AnswerDTO#setImagePath}.<br> Valid input:
     * random String
     */
    @Test
    public void testGetAndSetImagePath() {
        String testImagePath = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testAnswerDTO.setImagePath(testImagePath);
        assertEquals("The getting imagePath was not the expected one", testImagePath,
            testAnswerDTO.getImagePath());
    }

    /**
     * Test of {@link AnswerDTO#getImageBase64} and {@link AnswerDTO#setImageBase64}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetImageBase64() {
        String testImageBas64 = Helper.getRandomAlphanumericString(random.nextInt(100) + 20);
        testAnswerDTO.setImageBase64(testImageBas64);
        assertEquals("The getting imageBase64 was not the expected one", testImageBas64,
            testAnswerDTO.getImageBase64());
    }

    /**
     * Test of {@link AnswerDTO#getImageFile} and {@link AnswerDTO#setImageFile}.<br> Valid input:
     * MultiPartFile with MoPat-Logo from images path
     */
    @Test
    public void testGetAndSetImageFile() {
        Path path = Paths.get("src/main/webapp/images/logo.png");
        String name = "logo.png";
        String originalFileName = "logo.png";
        String contentType = "image/png";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
            throw new AssertionError("Could not read the given file!");
        }
        MultipartFile testImageFile = new MockMultipartFile(name, originalFileName, contentType,
            content);
        testAnswerDTO.setImageFile(testImageFile);
        assertEquals("The getting imageFile was not the expected one", testImageFile,
            testAnswerDTO.getImageFile());
    }

    /**
     * Test of {@link AnswerDTO#getHasResponse} and {@link AnswerDTO#setHasResponse}.<br> Valid
     * input: random Boolean
     */
    @Test
    public void testGetAndSetHasResponse() {
        Boolean testHasResponse = random.nextBoolean();
        testAnswerDTO.setHasResponse(testHasResponse);
        assertEquals("The getting hasResponse was not the expected one", testHasResponse,
            testAnswerDTO.getHasResponse());
    }

    /**
     * Test of {@link AnswerDTO#getHasExportRule} and {@link AnswerDTO#setHasExportRule}.<br> Valid
     * input: random Boolean
     */
    @Test
    public void testGetAndSetHasExportRule() {
        Boolean testHasExportRule = random.nextBoolean();
        testAnswerDTO.setHasExportRule(testHasExportRule);
        assertEquals("The getting hasExportRule was not the expected one", testHasExportRule,
            testAnswerDTO.getHasExportRule());
    }

    /**
     * Test of {@link AnswerDTO#getHasConditionsAsTrigger} and
     * {@link AnswerDTO#setHasConditionsAsTrigger}.<br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetHasConditionsAsTrigger() {
        Boolean testHasConditionsAsTrigger = random.nextBoolean();
        testAnswerDTO.setHasConditionsAsTrigger(testHasConditionsAsTrigger);
        assertEquals("The getting hasConditionsAsTrigger was not the expected one",
            testHasConditionsAsTrigger, testAnswerDTO.getHasConditionsAsTrigger());
    }

    /**
     * Test of {@link AnswerDTO#getHasConditionsAsTarget} and
     * {@link AnswerDTO#setHasConditionsAsTarget}.<br> Valid input: random Boolean
     */
    @Test
    public void testGetAndSetHasConditionsAsTarget() {
        Boolean testHasConditionsAsTarget = random.nextBoolean();
        testAnswerDTO.setHasConditionsAsTarget(testHasConditionsAsTarget);
        assertEquals("The getting hasConditionsAsTarget was not the expected one",
            testHasConditionsAsTarget, testAnswerDTO.getHasConditionsAsTarget());
    }

    /**
     * Test of {@link AnswerDTO#getConditions} and {@link AnswerDTO#setConditions}.<br> Valid input:
     * random list of {@link Condition Conditions}
     */
    @Test
    public void testGetAndSetConditions() {
        List<ConditionDTO> conditions = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            conditions.add(conditionDTOMapper.apply(ConditionTest.getNewValidCondition()));
        }
        testAnswerDTO.setConditions(conditions);
        assertEquals("The getting list of conditions was not the expected one", conditions,
            testAnswerDTO.getConditions());
    }
}
