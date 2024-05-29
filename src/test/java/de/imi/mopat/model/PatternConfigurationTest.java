package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.model.dto.ConfigurationDTO;
import de.imi.mopat.model.enumeration.ConfigurationType;
import de.imi.mopat.utils.Helper;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class PatternConfigurationTest {

    private static final Random random = new Random();
    private PatternConfiguration testPatternConfiguration;

    public PatternConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new PatternConfiguration
     *
     * @return Returns a valid new PatternConfiguration
     */
    public static PatternConfiguration getNewValidPatternConfiguration() {
        String pattern = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        Long id = random.nextLong();
        String entityClass = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String attribute = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String value = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        ConfigurationType configurationType = Helper.getRandomEnum(ConfigurationType.class);
        String labelMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String descriptionMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String testMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String updateMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String name = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        Integer position = Math.abs(random.nextInt()) + 1;
        ConfigurationGroup configurationGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();

        PatternConfiguration patternConfiguration = new PatternConfiguration(pattern, id,
            entityClass, attribute, value, configurationType, labelMessageCode,
            descriptionMessageCode, testMethod, updateMethod, name, position, configurationGroup);

        return patternConfiguration;
    }

    @Before
    public void setUp() {
        testPatternConfiguration = getNewValidPatternConfiguration();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link PatternConfiguration#toConfigurationDTO}.<br> Valid input: random
     * {@link PatternConfiguration}
     */
    @Test
    public void testToConfigurationDTO() {
        ConfigurationDTO testConfigurationDTO = testPatternConfiguration.toConfigurationDTO();
        assertEquals("The getting pattern was not the expected one",
            testPatternConfiguration.getPattern(), testConfigurationDTO.getPattern());
    }

    /**
     * Test of {@link PatternConfiguration#getPattern} and
     * {@link PatternConfiguration#setPattern}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetPattern() {
        String testPattern = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testPatternConfiguration.setPattern(testPattern);
        assertEquals("The getting pattern was not the expected one", testPattern,
            testPatternConfiguration.getPattern());
    }
}
