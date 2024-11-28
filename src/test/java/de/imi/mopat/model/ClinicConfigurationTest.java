package de.imi.mopat.model;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.helper.model.ClinicConfigurationDTOMapper;
import de.imi.mopat.model.dto.ClinicConfigurationDTO;
import de.imi.mopat.model.dto.ConfigurationDTO;
import de.imi.mopat.model.enumeration.ConfigurationType;
import de.imi.mopat.utils.Helper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

/**
 *
 */
public class ClinicConfigurationTest {

    private static final Random random = new Random();
    private ClinicConfiguration clinicConfiguration;
    private String entityClass, attribute, labelMessageCode, descriptionMessageCode, testMethod, updateMethod;
    private ConfigurationType configurationType;
    private Integer position;

    public ClinicConfigurationTest() {
    }

    /**
     * Returns a valid new {@link ClinicConfiguration}
     *
     * @return Returns a valid new {@link ClinicConfiguration}
     */
    public static ClinicConfiguration getNewValidConfiguration() {
        String entityClass = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String attribute = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        ConfigurationType configurationType = Helper.getRandomEnum(ConfigurationType.class);
        String labelMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String descriptionMessageCode = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String testMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        String updateMethod = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        Integer position = Math.abs(random.nextInt()) + 1;
        return new ClinicConfiguration(entityClass, attribute, configurationType,
            labelMessageCode, descriptionMessageCode, testMethod, updateMethod, position);
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
        clinicConfiguration = new ClinicConfiguration(entityClass, attribute, configurationType,
            labelMessageCode, descriptionMessageCode, testMethod, updateMethod, position);
    }

    /**
     * Test of {@link ClinicConfiguration#getPosition()} and
     * {@link ClinicConfiguration#setPosition(Integer)}<br> Valid input: random Integer<br>
     * Invalid input: <code>null</code> or Integer equal or less than zero.
     */
    @Test
    public void testSetGetPosition() {
        Integer position = Math.abs(random.nextInt(25)) + 1;
        clinicConfiguration.setPosition(position);
        Integer testPosition = clinicConfiguration.getPosition();
        assertNotNull(
            "Setting clinicConfiguration failed. The returned value was null although not-null value was expected.",
            testPosition);
        assertEquals(
            "Setting clinicConfiguration failed. The returned value didn't match the expected value.",
            testPosition, position);

        Throwable e = null;
        try {
            clinicConfiguration.setPosition(null);
        } catch (AssertionError ae) {
            e = ae;
        }
        assertTrue("Setting position to null worked.", e instanceof AssertionError);

        position = -1 * position;

        try {
            clinicConfiguration.setPosition(position);
        } catch (AssertionError ae) {
            e = ae;
        }
        assertTrue("Setting position to equal or less than zero value worked.",
            e instanceof AssertionError);
    }

    /**
     * Test of {@link ClinicConfiguration#getChildren()} and
     * {@link ClinicConfiguration#setChildren(List)}<br> Valid input: Instance of {@link List}
     * with {@link ClinicConfiguration} as
     * <code>E</code>
     */
    @Test
    public void testSetGetChildren() {
        Integer size = Math.abs(random.nextInt(25)) + 1;
        List<ClinicConfiguration> children = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            children.add(ClinicConfigurationTest.getNewValidConfiguration());
        }

        clinicConfiguration.setChildren(children);
        List<ClinicConfiguration> testChildren = clinicConfiguration.getChildren();
        assertNotNull(
            "Setting children failed. The returned value was null although not-null value was expected.",
            testChildren);
        assertEquals("Setting children failed. The returned list didn't match the expected list.",
            children, testChildren);
    }

    /**
     * Test of {@link ClinicConfiguration#getParent()} and
     * {@link ClinicConfiguration#setParent(ClinicConfiguration)}<br> Valid input: Instance
     * of {@link ClinicConfiguration}
     */
    @Test
    public void testSetGetParent() {
        ClinicConfiguration parent = ClinicConfigurationTest.getNewValidConfiguration();
        clinicConfiguration.setParent(parent);
        ClinicConfiguration testConfiguration = clinicConfiguration.getParent();
        assertNotNull(
            "Setting parent clinicConfiguration failed. The returned value was null although not-null value was expected.",
            testConfiguration);
        assertEquals(
            "Setting parent clinicConfiguration failed. The returned value didn't match the expected value.",
            parent, testConfiguration);
    }

    /**
     * Test of {@link ClinicConfiguration#getConfigurationType()}
     */
    @Test
    public void testGetConfigurationType() {
        ConfigurationType configurationTypeTest = clinicConfiguration.getConfigurationType();
        assertNotNull(
            "Getting the configurationType failed. The returned value was null although not-null value was expected.",
            configurationTypeTest);
        assertEquals(
            "Getting the configuraitonType failed. The returned value didn't match the expected value.",
            configurationType, configurationTypeTest);
    }

    /**
     * Test of {@link ClinicConfiguration#getLabelMessageCode()}
     */
    @Test
    public void testGetLabelMessageCode() {
        String labelMessageCodeTest = clinicConfiguration.getLabelMessageCode();
        assertNotNull(
            "Getting the labelMessageCode failed. The returned value was null although not-null value was expected.",
            labelMessageCodeTest);
        assertEquals(
            "Getting the labelMessageCode failed. The returned value didn't match the expected value.",
            labelMessageCode, labelMessageCodeTest);
    }

    /**
     * Test of {@link ClinicConfiguration#getDescriptionMessageCode()}
     */
    @Test
    public void testGetDescriptionMessageCode() {
        String descriptionMessageCodeTest = clinicConfiguration.getDescriptionMessageCode();
        assertNotNull(
            "Getting the descriptionMessageCode failed. The returned value was null although not-null value was expected.",
            descriptionMessageCodeTest);
        assertEquals(
            "Getting the descriptionMessageCode failed. The returned value didn't match the expected value.",
            descriptionMessageCode, descriptionMessageCodeTest);
    }

    /**
     * Test of {@link ClinicConfiguration#getUpdateMethod()}
     */
    @Test
    public void testGetUpdateMethod() {
        String testUpdateMethod = clinicConfiguration.getUpdateMethod();
        assertNotNull(
            "Getting the UpdateMethod failed. The returned value was null although not-null value was expected.",
            testUpdateMethod);
        assertEquals(
            "Getting the UpdateMethod failed. The returned value didn't match the expected value.",
            updateMethod, testUpdateMethod);
    }

    /**
     * Test of {@link ClinicConfiguration#getTestMethod()}
     */
    @Test
    public void testGetTestMethod() {
        String testTestMethod = clinicConfiguration.getTestMethod();
        assertNotNull(
            "Getting the TestMethod failed. The returned value was null although not-null value was expected.",
            testTestMethod);
        assertEquals(
            "Getting the TestMethod failed. The returned value didn't match the expected value.",
            testMethod, testTestMethod);
    }

    /**
     * Test of {@link ClinicConfiguration#getValue()} and
     * {@link ClinicConfiguration#setValue(String)}<br> Valid input: random String or
     * <code>null</code>
     */
    @Test
    public void testSetGetValue() {
        String value = null;
        clinicConfiguration.setValue(value);
        assertNull("The value was not null after setting it so.", clinicConfiguration.getValue());
        value = Helper.getRandomAlphanumericString(random.nextInt(50));
        clinicConfiguration.setValue(value);
        String testValue = clinicConfiguration.getValue();
        assertNotNull(
            "Setting the value failed. The returned value was null although not-null value was expected.",
            testValue);
        assertEquals(
            "Setting the value failed. The returned value didn't match the expected value.", value,
            testValue);
    }

    /**
     * Test of {@link ClinicConfiguration#getAttribute()}
     */
    @Test
    public void testGetAttribute() {
        String testAttribute = clinicConfiguration.getAttribute();
        assertNotNull(
            "Getting the attribute failed. The returned value was null although not-null value was expected.",
            testAttribute);
        assertEquals(
            "Getting the attribute failed. The returned value didn't match the expected value.",
            attribute, testAttribute);
    }

    /**
     * Test of {@link ClinicConfiguration#getEntityClass()}
     */
    @Test
    public void testGetEntityClass() {
        String testEntityClass = clinicConfiguration.getEntityClass();
        assertNotNull(
            "Getting the attribute failed. The returned value was null although not-null value was expected.",
            testEntityClass);
        assertEquals(
            "Getting the attribute failed. The returned value didn't match the expected value.",
            entityClass, testEntityClass);
    }

    /**
     * Test of {@link ClinicConfiguration#toClinicConfigurationDTO()} ()}
     */
    @Test
    public void testToConfigurationDTO() {
        ClinicConfiguration spyConfiguration = spy(clinicConfiguration);
        Mockito.when(spyConfiguration.getId()).thenReturn(Math.abs(random.nextLong()));
        ClinicConfiguration parent = spy(ClinicConfigurationTest.getNewValidConfiguration());
        Mockito.when(parent.getId()).thenReturn(Math.abs(random.nextLong()));
        spyConfiguration.setParent(parent);
        spyConfiguration.setChildren(new ArrayList<>());
        ClinicConfigurationDTOMapper clinicConfigurationDTOMapper = new ClinicConfigurationDTOMapper();
        ClinicConfigurationDTO configurationDTO = clinicConfigurationDTOMapper.apply(spyConfiguration);
        assertNotNull(
            "toClinicConfigurationDTO method failed. The returned value was null although not-null was expected.",
            configurationDTO);
        assertEquals(
            "toClinicConfigurationDTO method failed. The returned id didn't match the expected value.",
            spyConfiguration.getId(), configurationDTO.getId());
        assertEquals(
            "toClinicConfigurationDTO method failed. The returned attribute didn't match the expected value.",
            spyConfiguration.getAttribute(), configurationDTO.getAttribute());
        assertEquals(
            "toClinicConfigurationDTO method failed. The returned labelMessageCode didn't match the expected value.",
            spyConfiguration.getLabelMessageCode(), configurationDTO.getLabelMessageCode());
        assertEquals(
            "toClinicConfigurationDTO method failed. The returned descriptionMessageCode didn't match the expected value.",
            spyConfiguration.getDescriptionMessageCode(),
            configurationDTO.getDescriptionMessageCode());
        assertEquals(
            "toClinicConfigurationDTO method failed. The returned entityClass didn't match the expected value.",
            spyConfiguration.getEntityClass(), configurationDTO.getEntityClass());
        assertEquals(
            "toClinicConfigurationDTO method failed. The returned value didn't match the expected value.",
            spyConfiguration.getValue(), configurationDTO.getValue());
        assertEquals(
            "toClinicConfigurationDTO method failed. The returned configurationType didn't match the expected value.",
            spyConfiguration.getConfigurationType(), configurationDTO.getConfigurationType());
        assertEquals(
            "toClinicConfigurationDTO method failed. The returned testMethod didn't match the expected value.",
            spyConfiguration.getTestMethod(), configurationDTO.getTestMethod());
        assertEquals(
            "toClinicConfigurationDTO method failed. The returned updateMethod didn't match the expected value.",
            spyConfiguration.getUpdateMethod(), configurationDTO.getUpdateMethod());
        assertEquals(
            "toClinicConfigurationDTO method failed. The returned id of parent configuration didn't match the expected value.",
            spyConfiguration.getParent().getId(), configurationDTO.getParent().getId());
        assertEquals(
            "toClinicConfigurationDTO method failed. The returned position didn't match the expected value.",
            spyConfiguration.getPosition(), configurationDTO.getPosition());

        List<ClinicConfiguration> children = new ArrayList<>();
        Integer size = Math.abs(random.nextInt(25)) + 1;
        for (int i = 0; i < size; i++) {
            ClinicConfiguration child = spy(ClinicConfigurationTest.getNewValidConfiguration());
            Mockito.when(child.getId()).thenReturn(Math.abs(random.nextLong()));
            children.add(child);
        }
        spyConfiguration.setChildren(children);
        configurationDTO = clinicConfigurationDTOMapper.apply(spyConfiguration);

        assertEquals(
            "ToConfigurationDTO method failed. The returned list of children didn't match the expected value.",
            children.size(), configurationDTO.getChildren().size());

    }
}
