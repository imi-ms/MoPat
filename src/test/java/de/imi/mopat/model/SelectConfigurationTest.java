package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.model.dto.ConfigurationDTO;
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
public class SelectConfigurationTest {

    private static final Random random = new Random();
    private SelectConfiguration testSelectConfiguration;

    public SelectConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new SelectConfiguration
     *
     * @return Returns a valid new SelectConfiguration
     */
    public static SelectConfiguration getNewValidSelectConfiguration() {
        List<String> options = new ArrayList<>();
        int countOptions = random.nextInt(50);
        for (int i = 0; i < countOptions; i++) {
            options.add(Helper.getRandomString(random.nextInt(50) + 1));
        }
        Long id = random.nextLong();
        String entityClass = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String attribute = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String value = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        ConfigurationType configurationType = Helper.getRandomEnum(ConfigurationType.class);
        String labelMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String descriptionMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String testMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String updateMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        Integer position = Math.abs(random.nextInt()) + 1;
        ConfigurationGroup configurationGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();

        SelectConfiguration selectConfiguration = new SelectConfiguration(options, id, entityClass,
            attribute, value, configurationType, labelMessageCode, descriptionMessageCode,
            testMethod, updateMethod, position, configurationGroup);

        return selectConfiguration;
    }

    @Before
    public void setUp() {
        testSelectConfiguration = getNewValidSelectConfiguration();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link SelectConfiguraton#toConfigurationDTO}.<br> Valid input: random
     * {@link SelectConfiguration}
     */
    @Test
    public void testToConfigurationDTO() {
        ConfigurationDTO testConfigurationDTO = testSelectConfiguration.toConfigurationDTO();
        assertEquals("The getting list of options was not the expected one",
            testSelectConfiguration.getOptions(), testConfigurationDTO.getOptions());
    }

    /**
     * Test of {@link SelectConfiguraton#getOptions} and {@link SelectConfiguraton#setOptions}.<br>
     * Valid input: random List of Strings
     */
    @Test
    public void testGetAndSetOptions() {
        List<String> testOptions = new ArrayList<>();
        int countOptions = random.nextInt(50);
        for (int i = 0; i < countOptions; i++) {
            testOptions.add(Helper.getRandomString(random.nextInt(50) + 1));
        }
        testSelectConfiguration.setOptions(testOptions);
        assertEquals("The getting list of options was not the expected one", testOptions,
            testSelectConfiguration.getOptions());
    }
}
