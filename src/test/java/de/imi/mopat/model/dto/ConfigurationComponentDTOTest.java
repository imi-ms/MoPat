package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class ConfigurationComponentDTOTest {

    private static final Random random = new Random();
    private ConfigurationComponentDTO testConfigurationComponentDTO;

    public ConfigurationComponentDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        Map<String, List<ConfigurationGroupDTO>> testConfigurationGroupDTOs = new HashMap<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            List<ConfigurationGroupDTO> testList = new ArrayList<>();
            int countList = random.nextInt(200);
            for (int j = 0; j < countList; j++) {
                ConfigurationGroupDTO testConfigurationGroupDTO = new ConfigurationGroupDTO();
                testConfigurationGroupDTO.setId(Math.abs(random.nextLong()));
                testList.add(testConfigurationGroupDTO);
            }
            testConfigurationGroupDTOs.put(
                Helper.getRandomAlphanumericString(random.nextInt(50) + 1), testList);
        }
        testConfigurationComponentDTO = new ConfigurationComponentDTO(testConfigurationGroupDTOs);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ConfigurationComponentDTO#ConfigurationComponentDTO(java.util.Map)}.<br> Valid
     * input: random Map of Strings and {@link ConfigurationGroupDTO ConfigurationGroupDTOs}
     */
    @Test
    public void testConstructor() {
        Map<String, List<ConfigurationGroupDTO>> testConfigurationGroupDTOs = new HashMap<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            List<ConfigurationGroupDTO> testList = new ArrayList<>();
            int countList = random.nextInt(200);
            for (int j = 0; j < countList; j++) {
                ConfigurationGroupDTO testConfigurationGroupDTO = new ConfigurationGroupDTO();
                testConfigurationGroupDTO.setId(Math.abs(random.nextLong()));
                testList.add(testConfigurationGroupDTO);
            }
            testConfigurationGroupDTOs.put(
                Helper.getRandomAlphanumericString(random.nextInt(50) + 1), testList);
        }
        testConfigurationComponentDTO = new ConfigurationComponentDTO(testConfigurationGroupDTOs);
        assertEquals(
            "The getting map of ConfigurationGroupDTOs was not the expected one after creating the object",
            testConfigurationGroupDTOs, testConfigurationComponentDTO.getConfigurationGroupDTOs());
    }

    /**
     * Test of {@link ConfigurationComponentDTO#getConfigurationsToDelete} and
     * {@link ConfigurationComponentDTO#setConfigurationsToDelete}.<br> Valid input: list of random
     * Longs
     */
    @Test
    public void testGetAndSetConfigurationsToDelete() {
        List<Long> testConfigurationsToDelete = new ArrayList<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testConfigurationsToDelete.add(Math.abs(random.nextLong()));
        }
        testConfigurationComponentDTO.setConfigurationsToDelete(testConfigurationsToDelete);
        assertEquals("The getting list of ConfigurationsToDelete was not the expected one",
            testConfigurationsToDelete, testConfigurationComponentDTO.getConfigurationsToDelete());
    }

    /**
     * Test of {@link ConfigurationComponentDTO#getImageDeleteMap} and
     * {@link ConfigurationComponentDTO#setImageDeleteMap}.<br> Valid input: random Map of Strings
     * and Booleans
     */
    @Test
    public void testGetAndSetImageDeleteMap() {
        Map<Long, Boolean> testImageDeleteMap = new HashMap<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            testImageDeleteMap.put(Math.abs(random.nextLong()), random.nextBoolean());
        }
        testConfigurationComponentDTO.setImageDeleteMap(testImageDeleteMap);
        assertEquals("The getting ImageDeleteMap was not the expected one", testImageDeleteMap,
            testConfigurationComponentDTO.getImageDeleteMap());
    }

    /**
     * Test of {@link ConfigurationComponentDTO#getConfigurationGroupDTOs} and
     * {@link ConfigurationComponentDTO#setConfigurationGroupDTOs}.<br> Valid input: random Map of
     * Strings and {@link ConfigurationGroupDTO ConfigurationGroupDTOs}
     */
    @Test
    public void testGetAndSetConfigurationGroupDTOs() {
        Map<String, List<ConfigurationGroupDTO>> testConfigurationGroupDTOs = new HashMap<>();
        int count = random.nextInt(200);
        for (int i = 0; i < count; i++) {
            List<ConfigurationGroupDTO> testList = new ArrayList<>();
            int countList = random.nextInt(200);
            for (int j = 0; j < countList; j++) {
                ConfigurationGroupDTO testConfigurationGroupDTO = new ConfigurationGroupDTO();
                testConfigurationGroupDTO.setId(Math.abs(random.nextLong()));
                testList.add(testConfigurationGroupDTO);
            }
            testConfigurationGroupDTOs.put(
                Helper.getRandomAlphanumericString(random.nextInt(50) + 1), testList);
        }
        testConfigurationComponentDTO.setConfigurationGroupDTOs(testConfigurationGroupDTOs);
        assertEquals("The getting map of ConfigurationGroupDTOs was not the expected one",
            testConfigurationGroupDTOs, testConfigurationComponentDTO.getConfigurationGroupDTOs());
    }
}
