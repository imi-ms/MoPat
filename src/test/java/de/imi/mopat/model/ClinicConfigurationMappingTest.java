package de.imi.mopat.model;

import de.imi.mopat.model.dto.ClinicConfigurationDTO;
import de.imi.mopat.model.enumeration.ConfigurationType;
import de.imi.mopat.utils.Helper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

/**
 *
 */
public class ClinicConfigurationMappingTest {

    private static final Random random = new Random();
    private ClinicConfigurationMapping clinicConfigurationMapping;
    String value;

    public ClinicConfigurationMappingTest() {
    }

    /**
     * Returns a valid new {@link ClinicConfigurationMapping}
     *
     * @return Returns a valid new {@link ClinicConfigurationMapping}
     */
    public static ClinicConfigurationMapping getNewValidConfiguration() {
        Clinic clinic = ClinicTest.getNewValidClinic();
        ClinicConfiguration clinicConfiguration = ClinicConfigurationTest.getNewValidConfiguration();
        String value = Helper.getRandomAlphabeticString(random.nextInt(25));
        return new ClinicConfigurationMapping(clinic, clinicConfiguration, value);
    }

    @Before
    public void setUp() {
        Clinic clinic = ClinicTest.getNewValidClinic();
        ClinicConfiguration clinicConfiguration = ClinicConfigurationTest.getNewValidConfiguration();
        value = Helper.getRandomAlphabeticString(random.nextInt(25));
        clinicConfigurationMapping = new ClinicConfigurationMapping(clinic, clinicConfiguration, value);
    }



    /**
     * Test of {@link ClinicConfigurationMapping#getValue()} and
     * {@link ClinicConfigurationMapping#setValue(String)}<br> Valid input: random String or
     * <code>null</code>
     */
    @Test
    public void testSetGetValue() {
        String value = null;
        clinicConfigurationMapping.setValue(value);
        assertNull("The value was not null after setting it so.", clinicConfigurationMapping.getValue());
        value = Helper.getRandomAlphanumericString(random.nextInt(50));
        clinicConfigurationMapping.setValue(value);
        String testValue = clinicConfigurationMapping.getValue();
        assertNotNull(
            "Setting the value failed. The returned value was null although not-null value was expected.",
            testValue);
        assertEquals(
            "Setting the value failed. The returned value didn't match the expected value.", value,
            testValue);
    }
}
