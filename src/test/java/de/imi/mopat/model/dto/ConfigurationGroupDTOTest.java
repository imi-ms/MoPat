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
public class ConfigurationGroupDTOTest {

    private static final Random random = new Random();
    private ConfigurationGroupDTO testConfigurationGroupDTO;

    public ConfigurationGroupDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new ConfigurationGroupDTO
     *
     * @return Returns a valid new ConfigurationGroupDTO
     */
    public static ConfigurationGroupDTO getNewValidConfigurationGroupDTO() {
        Long id = Math.abs(random.nextLong());
        String labelMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        boolean repeating = random.nextBoolean();
        List<ConfigurationDTO> configurationDTOs = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            ConfigurationDTO testConfigurationDTO = new ConfigurationDTO();
            testConfigurationDTO.setId(Math.abs(random.nextLong()));
            configurationDTOs.add(testConfigurationDTO);
        }

        ConfigurationGroupDTO configurationGroupDTO = new ConfigurationGroupDTO(id,
            labelMessageCode, repeating, configurationDTOs);

        return configurationGroupDTO;
    }

    @Before
    public void setUp() {
        testConfigurationGroupDTO = getNewValidConfigurationGroupDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ConfigurationGroupDTO#getId} and {@link ConfigurationGroupDTO#setId}.<br>
     * Valid input: random Long
     */
    @Test
    public void testGetAndSetId() {
        Long testID = Math.abs(random.nextLong());
        testConfigurationGroupDTO.setId(testID);
        assertEquals("The getting ID was not the expected one", testID,
            testConfigurationGroupDTO.getId());
    }

    /**
     * Test of {@link ConfigurationGroupDTO#getReferringId} and
     * {@link ConfigurationGroupDTO#setReferringId}.<br> Valid input: random Long
     */
    @Test
    public void testGetAndSetReferringId() {
        Long testReferringID = Math.abs(random.nextLong());
        testConfigurationGroupDTO.setReferringId(testReferringID);
        assertEquals("The getting referringID was not the expected one", testReferringID,
            testConfigurationGroupDTO.getReferringId());
    }

    /**
     * Test of {@link ConfigurationGroupDTO#getPosition} and
     * {@link ConfigurationGroupDTO#setPosition}.<br> Valid input: random Integer
     */
    @Test
    public void testGetAndSetPosition() {
        Integer testPosition = Math.abs(random.nextInt());
        testConfigurationGroupDTO.setPosition(testPosition);
        assertEquals("The getting position was not the expected one", testPosition,
            testConfigurationGroupDTO.getPosition());
    }

    /**
     * Test of {@link ConfigurationGroupDTO#getLabelMessageCode} and
     * {@link ConfigurationGroupDTO#setLabelMessageCode}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetLabelMessageCode() {
        String testLabelMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testConfigurationGroupDTO.setLabelMessageCode(testLabelMessageCode);
        assertEquals("The getting LabelMessageCode was not the expected one", testLabelMessageCode,
            testConfigurationGroupDTO.getLabelMessageCode());
    }

    /**
     * Test of {@link ConfigurationGroupDTO#getName} and {@link ConfigurationGroupDTO#setName}.<br>
     * Valid input: random String
     */
    @Test
    public void testGetAndSetName() {
        String testName = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testConfigurationGroupDTO.setName(testName);
        assertEquals("The getting name was not the expected one", testName,
            testConfigurationGroupDTO.getName());
    }

    /**
     * Test of {@link ConfigurationGroupDTO#isRepeating} and
     * {@link ConfigurationGroupDTO#setRepeating}.<br> Valid input: random boolean
     */
    @Test
    public void testIsAndSetRepeating() {
        boolean testRepeating = random.nextBoolean();
        testConfigurationGroupDTO.setRepeating(testRepeating);
        assertEquals("The getting isRepeating was not the expected one", testRepeating,
            testConfigurationGroupDTO.isRepeating());
    }

    /**
     * Test of {@link ConfigurationGroupDTO#getConfigurationDTOs} and
     * {@link ConfigurationGroupDTO#setConfigurationDTOs}.<br> Valid input: random list of
     * {@link ConfigurationDTO ConfigurationDTOs}
     */
    @Test
    public void testGetAndSetConfigurationDTOs() {
        List<ConfigurationDTO> testConfigurationDTOs = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            ConfigurationDTO testConfigurationDTO = new ConfigurationDTO();
            testConfigurationDTO.setId(Math.abs(random.nextLong()));
            testConfigurationDTOs.add(testConfigurationDTO);
        }
        testConfigurationGroupDTO.setConfigurationDTOs(testConfigurationDTOs);
        assertEquals("The getting list of ConfigurationDTOs was not the expected one",
            testConfigurationDTOs, testConfigurationGroupDTO.getConfigurationDTOs());
    }
}
