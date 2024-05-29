package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.model.enumeration.ConfigurationType;
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
public class ConfigurationDTOTest {

    private static final Random random = new Random();
    private ConfigurationDTO testConfigurationDTO;

    public ConfigurationDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testConfigurationDTO = new ConfigurationDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ConfigurationDTO#getId} and {@link ConfigurationDTO#setId}.<br> Valid input:
     * random Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testConfigurationDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID,
            testConfigurationDTO.getId());
    }

    /**
     * Test of {@link ConfigurationDTO#getEntityClass} and
     * {@link ConfigurationDTO#setEntityClass}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetEntityClass() {
        String testEntityClass = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testConfigurationDTO.setEntityClass(testEntityClass);
        assertEquals("The getting EntityClass was not the expected one", testEntityClass,
            testConfigurationDTO.getEntityClass());
    }

    /**
     * Test of {@link ConfigurationDTO#getAttribute} and {@link ConfigurationDTO#setAttribute}.<br>
     * Valid input: random String
     */
    @Test
    public void testGetAndSetAttribute() {
        String testAttribute = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testConfigurationDTO.setAttribute(testAttribute);
        assertEquals("The getting Attribute was not the expected one", testAttribute,
            testConfigurationDTO.getAttribute());
    }

    /**
     * Test of {@link ConfigurationDTO#getValue} and {@link ConfigurationDTO#setValue}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetValue() {
        String testValue = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testConfigurationDTO.setValue(testValue);
        assertEquals("The getting Value was not the expected one", testValue,
            testConfigurationDTO.getValue());
    }

    /**
     * Test of {@link ConfigurationDTO#getConfigurationType} and
     * {@link ConfigurationDTO#setConfigurationType}.<br> Valid input: random
     * {@link ConfigurationType}
     */
    @Test
    public void testGetAndSetConfigurationType() {
        ConfigurationType testConfigurationType = Helper.getRandomEnum(ConfigurationType.class);
        testConfigurationDTO.setConfigurationType(testConfigurationType);
        assertEquals("The getting ConfigurationType was not the expected one",
            testConfigurationType, testConfigurationDTO.getConfigurationType());
    }

    /**
     * Test of {@link ConfigurationDTO#getLabelMessageCode} and
     * {@link ConfigurationDTO#setLabelMessageCode}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetLabelMessageCode() {
        String testLabelMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testConfigurationDTO.setLabelMessageCode(testLabelMessageCode);
        assertEquals("The getting LabelMessageCode was not the expected one", testLabelMessageCode,
            testConfigurationDTO.getLabelMessageCode());
    }

    /**
     * Test of {@link ConfigurationDTO#getDescriptionMessageCode} and
     * {@link ConfigurationDTO#setDescriptionMessageCode}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetDescriptionMessageCode() {
        String testDescriptionMessageCode = Helper.getRandomAlphanumericString(
            random.nextInt(50) + 1);
        testConfigurationDTO.setDescriptionMessageCode(testDescriptionMessageCode);
        assertEquals("The getting DescriptionMessageCode was not the expected one",
            testDescriptionMessageCode, testConfigurationDTO.getDescriptionMessageCode());
    }

    /**
     * Test of {@link ConfigurationDTO#getTestMethod} and
     * {@link ConfigurationDTO#setTestMethod}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetTestMethod() {
        String testMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testConfigurationDTO.setTestMethod(testMethod);
        assertEquals("The getting TestMethod was not the expected one", testMethod,
            testConfigurationDTO.getTestMethod());
    }

    /**
     * Test of {@link ConfigurationDTO#getUpdateMethod} and
     * {@link ConfigurationDTO#setUpdateMethod}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetUpdateMethod() {
        String testUpdateMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testConfigurationDTO.setUpdateMethod(testUpdateMethod);
        assertEquals("The getting UpdateMethod was not the expected one", testUpdateMethod,
            testConfigurationDTO.getUpdateMethod());
    }

    /**
     * Test of {@link ConfigurationDTO#getChildren} and {@link ConfigurationDTO#setChildren}.<br>
     * Valid input: random list of {@link ConfigurationDTO ConfigurationDTOs}
     */
    @Test
    public void testGetAndSetChildren() {
        List<ConfigurationDTO> testChildren = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            ConfigurationDTO tempConfigurationDTO = new ConfigurationDTO();
            tempConfigurationDTO.setId(Math.abs(random.nextLong()));
            testChildren.add(tempConfigurationDTO);
        }
        testConfigurationDTO.setChildren(testChildren);
        assertEquals("The getting list of children was not the expected one", testChildren,
            testConfigurationDTO.getChildren());
    }

    /**
     * Test of {@link ConfigurationDTO#getConfigurationGroupDTO} and
     * {@link ConfigurationDTO#setConfigurationGroupDTO}.<br> Valid input: random
     * {@link ConfigurationGroupDTO}
     */
    @Test
    public void testGetAndSetConfigurationGroupDTO() {
        ConfigurationGroupDTO testConfigurationGroupDTO = new ConfigurationGroupDTO();
        testConfigurationGroupDTO.setId(Math.abs(random.nextLong()));
        testConfigurationDTO.setConfigurationGroupDTO(testConfigurationGroupDTO);
        assertEquals("The getting ConfigurationGroupDTO was not the expected one",
            testConfigurationGroupDTO, testConfigurationDTO.getConfigurationGroupDTO());
    }

    /**
     * Test of {@link ConfigurationDTO#getOptions} and {@link ConfigurationDTO#setOptions}.<br>
     * Valid input: random list of Strings
     */
    @Test
    public void testGetAndSetOptions() {
        List<String> testOptions = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testOptions.add(Helper.getRandomAlphanumericString(random.nextInt(50) + 1));
        }
        testConfigurationDTO.setOptions(testOptions);
        assertEquals("The getting list of options was not the expected one", testOptions,
            testConfigurationDTO.getOptions());
    }

    /**
     * Test of {@link ConfigurationDTO#getPattern} and {@link ConfigurationDTO#setPattern}.<br>
     * Valid input: random String
     */
    @Test
    public void testGetAndSetPattern() {
        String testPattern = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testConfigurationDTO.setPattern(testPattern);
        assertEquals("The getting Pattern was not the expected one", testPattern,
            testConfigurationDTO.getPattern());
    }

    /**
     * Test of {@link ConfigurationDTO#getParent} and {@link ConfigurationDTO#setParent}.<br> Valid
     * input: random {@link ConfigurationDTO}
     */
    @Test
    public void testGetAndSetParent() {
        ConfigurationDTO testParent = new ConfigurationDTO();
        testParent.setId(Math.abs(random.nextLong()));
        testConfigurationDTO.setParent(testParent);
        assertEquals("The getting parent was not the expected one", testParent,
            testConfigurationDTO.getParent());
    }

    /**
     * Test of {@link ConfigurationDTO#getPosition} and {@link ConfigurationDTO#setPosition}.<br>
     * Valid input: random Integer
     */
    @Test
    public void testGetAndSetPosition() {
        Integer testPosition = Math.abs(random.nextInt());
        testConfigurationDTO.setPosition(testPosition);
        assertEquals("The getting position was not the expected one", testPosition,
            testConfigurationDTO.getPosition());
    }
}
