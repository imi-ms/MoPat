package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import de.imi.mopat.model.dto.ConfigurationGroupDTO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class ConfigurationGroupTest {

    private ConfigurationGroup testConfigurationGroup;

    public ConfigurationGroupTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new {@link ConfigurationGroup}
     *
     * @return Returns a valid new {@link ConfigurationGroup}
     */
    public static ConfigurationGroup getNewValidConfigurationGroup() {
        ConfigurationGroup configurationGroup = new ConfigurationGroup();
        return configurationGroup;
    }

    @Before
    public void setUp() {
        testConfigurationGroup = getNewValidConfigurationGroup();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ConfigurationGrop#getPosition} and {@link ConfigurationGroup#setPosition}.<br>
     * Valid input: random Integer
     */
    @Test
    public void testGetAndSetPosition() {
        Random random = new Random();
        Integer testPosition = random.nextInt();
        testConfigurationGroup.setPosition(testPosition);
        assertEquals("The getting position was not the expected one", testPosition,
            testConfigurationGroup.getPosition());
    }

    /**
     * Test of {@link ConfigurationGrop#getName} and {@link ConfigurationGroup#setName}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetName() {
        String testName = UUID.randomUUID().toString();
        testConfigurationGroup.setName(testName);
        assertEquals("The getting name was not the expected one", testName,
            testConfigurationGroup.getName());
    }

    /**
     * Test of {@link ConfigurationGrop#getLabelMessageCode} and
     * {@link ConfigurationGroup#setLabelMessageCode}.<br> Valid input: random String
     */
    @Test
    public void testGetAndSetLabelMessageCode() {
        String testLabelMessageCode = UUID.randomUUID().toString();
        testConfigurationGroup.setLabelMessageCode(testLabelMessageCode);
        assertEquals("The getting LabelMessageCode was not the expected one", testLabelMessageCode,
            testConfigurationGroup.getLabelMessageCode());
    }

    /**
     * Test of {@link ConfigurationGrop#isRepeating} and
     * {@link ConfigurationGroup#setRepeating}.<br> Valid input: random Boolean
     */
    @Test
    public void testIsAndSetRepeating() {
        Random random = new Random();
        Boolean testRepeating = random.nextBoolean();
        testConfigurationGroup.setRepeating(testRepeating);
        assertEquals("The getting isRepeating was not the expected one", testRepeating,
            testConfigurationGroup.isRepeating());
    }

    /**
     * Test of {@link ConfigurationGrop#getConfigurations} and
     * {@link ConfigurationGroup#setConfigurations}.<br> Valid input: random list of
     * {@link Configuration Configurations}
     */
    @Test
    public void testGetAndSetConfigurations() {
        Random random = new Random();
        int countConfiguratons = random.nextInt(50) + 1;
        List<Configuration> testList = new ArrayList<>();
        for (int i = 0; i < countConfiguratons; i++) {
            testList.add(ConfigurationTest.getNewValidConfiguration(testConfigurationGroup));
        }
        testConfigurationGroup.setConfigurations(testList);
        assertEquals("The getting list of configuratons was not the expected one", testList,
            testConfigurationGroup.getConfigurations());
    }

    /**
     * Test of {@link ConfigurationGrop#equals}.<br> Invalid input: The same
     * {@link ConfigurationGroup} twice in a HashSet
     */
    @Test
    public void testEquals() {
        HashSet<ConfigurationGroup> testSet = new HashSet<>();
        testSet.add(testConfigurationGroup);
        testSet.add(testConfigurationGroup);
        assertEquals("It was possible to add the same ConfigurationGroup twice in one set", 1,
            testSet.size());

        assertEquals("The ConfigurationGroup was not equal to itself", testConfigurationGroup,
            testConfigurationGroup);
        assertNotEquals("The ConfigurationGroup was equal to null", null, testConfigurationGroup);
        ConfigurationGroup otherConfigurationGroup = getNewValidConfigurationGroup();
        assertNotEquals("The ConfigurationGroup was equal to a different ConfigurationGroup",
            testConfigurationGroup, otherConfigurationGroup);
        Object otherObject = new Object();
        assertNotEquals("The ConfigurationGroup was equal to a different Object",
            testConfigurationGroup, otherObject);
    }

    /**
     * Test of {@link ConfigurationGrop#toConfigurationGroupDTO}.<br> Valid input: random position,
     * labelMessageCode, name, isRepeating
     */
    @Test
    public void testToConfigurationGroupDTO() {
        Random random = new Random();

        testConfigurationGroup.setPosition(random.nextInt());
        testConfigurationGroup.setLabelMessageCode(UUID.randomUUID().toString());
        testConfigurationGroup.setName(UUID.randomUUID().toString());
        testConfigurationGroup.setRepeating(random.nextBoolean());

        ConfigurationGroupDTO testConfigurationGroupDTO = testConfigurationGroup.toConfigurationGroupDTO();

        assertEquals("The getting position was not the expected one",
            testConfigurationGroup.getPosition(), testConfigurationGroupDTO.getPosition());
        assertEquals("The getting LabelMessageCode was not the expected one",
            testConfigurationGroup.getLabelMessageCode(),
            testConfigurationGroupDTO.getLabelMessageCode());
        assertEquals("The getting name was not the expected one", testConfigurationGroup.getName(),
            testConfigurationGroupDTO.getName());
        assertEquals("The getting isRepeating was not the expected one",
            testConfigurationGroup.isRepeating(), testConfigurationGroupDTO.isRepeating());
    }
}
