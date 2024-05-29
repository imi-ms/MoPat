package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.model.dto.ConfigurationDTO;
import de.imi.mopat.model.enumeration.ConfigurationType;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class ConfigurationTest {

    private static final Random random = new Random();
    private Configuration configuration;
    private String entityClass, attribute, labelMessageCode, descriptionMessageCode, testMethod, updateMethod;
    private ConfigurationType configurationType;
    private Integer position;
    private ConfigurationGroup configurationGroup;

    public ConfigurationTest() {
    }

    /**
     * Returns a valid new {@link Configuration}
     *
     * @return Returns a valid new {@link Configuration}
     */
    public static Configuration getNewValidConfiguration() {
        String entityClass = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String attribute = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        ConfigurationType configurationType = Helper.getRandomEnum(ConfigurationType.class);
        String labelMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String descriptionMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String testMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String updateMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        Integer position = Math.abs(random.nextInt()) + 1;
        ConfigurationGroup configurationGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();
        Configuration configuration = new Configuration(entityClass, attribute, configurationType,
            labelMessageCode, descriptionMessageCode, testMethod, updateMethod, position,
            configurationGroup);
        return configuration;
    }

    /**
     * Returns a valid new {@link Configuration}
     *
     * @param configurationGroup {@link ConfigurationGroup} of this {@link Configuration}
     * @return Returns a valid new {@link Configuration}
     */
    public static Configuration getNewValidConfiguration(ConfigurationGroup configurationGroup) {
        String entityClass = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String attribute = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        ConfigurationType configurationType = Helper.getRandomEnum(ConfigurationType.class);
        String labelMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String descriptionMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String testMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String updateMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        Integer position = Math.abs(random.nextInt()) + 1;
        Configuration configuration = new Configuration(entityClass, attribute, configurationType,
            labelMessageCode, descriptionMessageCode, testMethod, updateMethod, position,
            configurationGroup);
        return configuration;
    }

    @Before
    public void setUp() {
        entityClass = Helper.getRandomAlphabeticString(random.nextInt(25));
        attribute = Helper.getRandomAlphabeticString(random.nextInt(25));
        configurationType = Helper.getRandomEnum(ConfigurationType.class);
        labelMessageCode = Helper.getRandomAlphabeticString(random.nextInt(25));
        descriptionMessageCode = Helper.getRandomAlphabeticString(random.nextInt(25));
        testMethod = Helper.getRandomAlphabeticString(random.nextInt(25));
        updateMethod = Helper.getRandomAlphabeticString(random.nextInt(25));
        position = Math.abs(random.nextInt());
        configurationGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();
        configuration = new Configuration(entityClass, attribute, configurationType,
            labelMessageCode, descriptionMessageCode, testMethod, updateMethod, position,
            configurationGroup);
    }

    /**
     * Test of {@link Configuration#getPosition()} and
     * {@link Configuration#setPosition(java.lang.Integer)}<br> Valid input: random Integer<br>
     * Invalid input: <code>null</code> or Integer equal or less than zero.
     */
    @Test
    public void testSetGetPosition() {
        Integer position = Math.abs(random.nextInt(25)) + 1;
        configuration.setPosition(position);
        Integer testPosition = configuration.getPosition();
        assertNotNull(
            "Setting configuration failed. The returned value was null although not-null value was expected.",
            testPosition);
        assertEquals(
            "Setting configuration failed. The returned value didn't match the expected value.",
            testPosition, position);

        Throwable e = null;
        try {
            configuration.setPosition(null);
        } catch (AssertionError ae) {
            e = ae;
        }
        assertTrue("Setting position to null worked.", e instanceof AssertionError);

        position = -1 * position;

        try {
            configuration.setPosition(position);
        } catch (AssertionError ae) {
            e = ae;
        }
        assertTrue("Setting position to equal or less than zero value worked.",
            e instanceof AssertionError);
    }

    /**
     * Test of {@link Configuration#getChildren()} and
     * {@link Configuration#setChildren(java.util.List)}<br> Valid input: Instance of {@link List}
     * with {@link Configuration} as
     * <code>E</code>
     */
    @Test
    public void testSetGetChildren() {
        Integer size = Math.abs(random.nextInt(25)) + 1;
        List<Configuration> children = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            children.add(ConfigurationTest.getNewValidConfiguration());
        }

        configuration.setChildren(children);
        List<Configuration> testChildren = configuration.getChildren();
        assertNotNull(
            "Setting children failed. The returned value was null although not-null value was expected.",
            testChildren);
        assertEquals("Setting children failed. The returned list didn't match the expected list.",
            children, testChildren);
    }

    /**
     * Test of {@link Configuration#getParent()} and
     * {@link Configuration#setParent(de.imi.mopat.model.Configuration)}<br> Valid input: Instance
     * of {@link Configuration}
     */
    @Test
    public void testSetGetParent() {
        Configuration parent = ConfigurationTest.getNewValidConfiguration();
        configuration.setParent(parent);
        Configuration testConfiguration = configuration.getParent();
        assertNotNull(
            "Setting parent configuration failed. The returned value was null although not-null value was expected.",
            testConfiguration);
        assertEquals(
            "Setting parent configuration failed. The returned value didn't match the expected value.",
            parent, testConfiguration);
    }

    /**
     * Test of {@link Configuration#getConfigurationGroup()} and
     * {@link Configuration#setConfigurationGroup(de.imi.mopat.model.ConfigurationGroup)}<br> Valid
     * input: Instance of {@link ConfigurationGroup}.<br> Invalid input: <code>null</code>
     */
    @Test
    public void testSetGetConfigurationGroup() {
        ConfigurationGroup configurationGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();
        configuration.setConfigurationGroup(configurationGroup);
        ConfigurationGroup testConfigurationGroup = configuration.getConfigurationGroup();
        assertNotNull(
            "Setting configuration group failed. The returned value was null although not-null value was expected.",
            testConfigurationGroup);
        assertEquals(
            "Setting configuration group failed. The returned value didn't match the expected value.",
            configurationGroup, testConfigurationGroup);

        Throwable e = null;

        try {
            configuration.setConfigurationGroup(null);
        } catch (AssertionError ae) {
            e = ae;
        }

        assertTrue("Setting configuration group to null worked.", e instanceof AssertionError);
    }

    /**
     * Test of {@link Configuration#getConfigurationType()}
     */
    @Test
    public void testGetConfigurationType() {
        ConfigurationType configurationTypeTest = configuration.getConfigurationType();
        assertNotNull(
            "Getting the configurationType failed. The returned value was null although not-null value was expected.",
            configurationTypeTest);
        assertEquals(
            "Getting the configuraitonType failed. The returned value didn't match the expected value.",
            configurationType, configurationTypeTest);
    }

    /**
     * Test of {@link Configuration#getLabelMessageCode()}
     */
    @Test
    public void testGetLabelMessageCode() {
        String labelMessageCodeTest = configuration.getLabelMessageCode();
        assertNotNull(
            "Getting the labelMessageCode failed. The returned value was null although not-null value was expected.",
            labelMessageCodeTest);
        assertEquals(
            "Getting the labelMessageCode failed. The returned value didn't match the expected value.",
            labelMessageCode, labelMessageCodeTest);
    }

    /**
     * Test of {@link Configuration#getDescriptionMessageCode()}
     */
    @Test
    public void testGetDescriptionMessageCode() {
        String descriptionMessageCodeTest = configuration.getDescriptionMessageCode();
        assertNotNull(
            "Getting the descriptionMessageCode failed. The returned value was null although not-null value was expected.",
            descriptionMessageCodeTest);
        assertEquals(
            "Getting the descriptionMessageCode failed. The returned value didn't match the expected value.",
            descriptionMessageCode, descriptionMessageCodeTest);
    }

    /**
     * Test of {@link Configuration#getUpdateMethod()}
     */
    @Test
    public void testGetUpdateMethod() {
        String testUpdateMethod = configuration.getUpdateMethod();
        assertNotNull(
            "Getting the UpdateMethod failed. The returned value was null although not-null value was expected.",
            testUpdateMethod);
        assertEquals(
            "Getting the UpdateMethod failed. The returned value didn't match the expected value.",
            updateMethod, testUpdateMethod);
    }

    /**
     * Test of {@link Configuration#getTestMethod()}
     */
    @Test
    public void testGetTestMethod() {
        String testTestMethod = configuration.getTestMethod();
        assertNotNull(
            "Getting the TestMethod failed. The returned value was null although not-null value was expected.",
            testTestMethod);
        assertEquals(
            "Getting the TestMethod failed. The returned value didn't match the expected value.",
            testMethod, testTestMethod);
    }

    /**
     * Test of {@link Configuration#getValue()} and
     * {@link Configuration#setValue(java.lang.String)}<br> Valid input: random String or
     * <code>null</code>
     */
    @Test
    public void testSetGetValue() {
        String value = null;
        configuration.setValue(value);
        assertNull("The value was not null after setting it so.", configuration.getValue());
        value = Helper.getRandomAlphanumericString(random.nextInt(50));
        configuration.setValue(value);
        String testValue = configuration.getValue();
        assertNotNull(
            "Setting the value failed. The returned value was null although not-null value was expected.",
            testValue);
        assertEquals(
            "Setting the value failed. The returned value didn't match the expected value.", value,
            testValue);
    }

    /**
     * Test of {@link Configuration#getAttribute()}
     */
    @Test
    public void testGetAttribute() {
        String testAttribute = configuration.getAttribute();
        assertNotNull(
            "Getting the attribute failed. The returned value was null although not-null value was expected.",
            testAttribute);
        assertEquals(
            "Getting the attribute failed. The returned value didn't match the expected value.",
            attribute, testAttribute);
    }

    /**
     * Test of {@link Configuration#getEntityClass()}
     */
    @Test
    public void testGetEntityClass() {
        String testEntityClass = configuration.getEntityClass();
        assertNotNull(
            "Getting the attribute failed. The returned value was null although not-null value was expected.",
            testEntityClass);
        assertEquals(
            "Getting the attribute failed. The returned value didn't match the expected value.",
            entityClass, testEntityClass);
    }

    /**
     * Test of {@link Configuration#toConfigurationDTO()}
     */
    @Test
    public void testToConfigurationDTO() {
        Configuration spyConfiguration = spy(configuration);
        Mockito.when(spyConfiguration.getId()).thenReturn(Math.abs(random.nextLong()));
        Configuration parent = spy(ConfigurationTest.getNewValidConfiguration());
        Mockito.when(parent.getId()).thenReturn(Math.abs(random.nextLong()));
        spyConfiguration.setParent(parent);
        spyConfiguration.setChildren(new ArrayList<>());

        ConfigurationDTO configurationDTO = spyConfiguration.toConfigurationDTO();
        assertNotNull(
            "ToConfigurationDTO method failed. The returned value was null although not-null was expected.",
            configurationDTO);
        assertEquals(
            "ToConfigurationDTO method failed. The returned id didn't match the expected value.",
            spyConfiguration.getId(), configurationDTO.getId());
        assertEquals(
            "ToConfigurationDTO method failed. The returned attribute didn't match the expected value.",
            spyConfiguration.getAttribute(), configurationDTO.getAttribute());
        assertEquals(
            "ToConfigurationDTO method failed. The returned labelMessageCode didn't match the expected value.",
            spyConfiguration.getLabelMessageCode(), configurationDTO.getLabelMessageCode());
        assertEquals(
            "ToConfigurationDTO method failed. The returned descriptionMessageCode didn't match the expected value.",
            spyConfiguration.getDescriptionMessageCode(),
            configurationDTO.getDescriptionMessageCode());
        assertEquals(
            "ToConfigurationDTO method failed. The returned entityClass didn't match the expected value.",
            spyConfiguration.getEntityClass(), configurationDTO.getEntityClass());
        assertEquals(
            "ToConfigurationDTO method failed. The returned value didn't match the expected value.",
            spyConfiguration.getValue(), configurationDTO.getValue());
        assertEquals(
            "ToConfigurationDTO method failed. The returned configurationType didn't match the expected value.",
            spyConfiguration.getConfigurationType(), configurationDTO.getConfigurationType());
        assertEquals(
            "ToConfigurationDTO method failed. The returned testMethod didn't match the expected value.",
            spyConfiguration.getTestMethod(), configurationDTO.getTestMethod());
        assertEquals(
            "ToConfigurationDTO method failed. The returned updateMethod didn't match the expected value.",
            spyConfiguration.getUpdateMethod(), configurationDTO.getUpdateMethod());
        assertEquals(
            "ToConfigurationDTO method failed. The returned id of parent configuration didn't match the expected value.",
            spyConfiguration.getParent().getId(), configurationDTO.getParent().getId());
        assertEquals(
            "ToConfigurationDTO method failed. The returned position didn't match the expected value.",
            spyConfiguration.getPosition(), configurationDTO.getPosition());

        List<Configuration> children = new ArrayList<>();
        Integer size = Math.abs(random.nextInt(25)) + 1;
        for (int i = 0; i < size; i++) {
            Configuration child = spy(ConfigurationTest.getNewValidConfiguration());
            Mockito.when(child.getId()).thenReturn(Math.abs(random.nextLong()));
            children.add(child);
        }
        spyConfiguration.setChildren(children);
        configurationDTO = spyConfiguration.toConfigurationDTO();
        assertEquals(
            "ToConfigurationDTO method failed. The returned list of children didn't match the expected value.",
            children.size(), configurationDTO.getChildren().size());

    }
}
