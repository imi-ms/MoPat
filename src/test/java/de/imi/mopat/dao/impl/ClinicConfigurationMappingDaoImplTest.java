package de.imi.mopat.dao.impl;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.ClinicConfigurationMappingDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.model.*;
import de.imi.mopat.model.enumeration.ConfigurationType;
import org.checkerframework.checker.units.qual.C;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
        MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class ClinicConfigurationMappingDaoImplTest {

    @Autowired
    ClinicConfigurationMappingDao testClinicconfigurationMappingDao;

    @Autowired
    ClinicDao clinicDao;

    /**
     * Test of {@link ConfigurationDaoImpl#isRegistryOfPatientActivated}.<br>
     */
    @Test
    public void testIsRegistryOfPatientActivated() {
        ClinicConfiguration clinicConfiguration = new ClinicConfiguration("GLOBAL","registerPatientData", ConfigurationType.BOOLEAN, "","","","",1);
        Clinic clinic = ClinicTest.getNewValidClinic();
        ClinicConfigurationMapping clinicConfigurationMapping = new ClinicConfigurationMapping(clinic, clinicConfiguration, "true");
        List<ClinicConfigurationMapping> clinicConfigurationMappings = new ArrayList<>();
        clinicConfigurationMappings.add(clinicConfigurationMapping);
        clinic.setClinicConfigurationMappings(clinicConfigurationMappings);
        clinicDao.merge(clinic);
        assertTrue("The getting is registry of patient activated was not the expected one",
                testClinicconfigurationMappingDao.isRegistryOfPatientActivated(clinicConfigurationMapping.getClinic().getId()));
    }

    /**
     * Test of {@link ConfigurationDaoImpl#isUsePatientDataLookupActivated}.<br>
     */
    @Test
    public void testIsUsePatientDataLookupActivated() {
        ClinicConfiguration clinicConfiguration = new ClinicConfiguration("GLOBAL","usePatientDataLookup", ConfigurationType.BOOLEAN, "","","","",1);
        Clinic clinic = ClinicTest.getNewValidClinic();
        ClinicConfigurationMapping clinicConfigurationMapping = new ClinicConfigurationMapping(clinic, clinicConfiguration, "true");
        List<ClinicConfigurationMapping> clinicConfigurationMappings = new ArrayList<>();
        clinicConfigurationMappings.add(clinicConfigurationMapping);
        clinic.setClinicConfigurationMappings(clinicConfigurationMappings);
        clinicDao.merge(clinic);
        assertTrue("The getting is registry of patient activated was not the expected one",
                testClinicconfigurationMappingDao.isUsePatientDataLookupActivated(clinicConfigurationMapping.getClinic().getId()));
    }

    /**
     * Test of {@link ConfigurationDaoImpl#isPseudonymizationServiceActivated}.<br>
     */
    @Test
    public void testIsPseudonymizationServiceActivated() {
        ClinicConfiguration clinicConfiguration = new ClinicConfiguration("GLOBAL","usePseudonymizationService", ConfigurationType.BOOLEAN, "","","","",1);
        Clinic clinic = ClinicTest.getNewValidClinic();
        ClinicConfigurationMapping clinicConfigurationMapping = new ClinicConfigurationMapping(clinic, clinicConfiguration, "true");
        List<ClinicConfigurationMapping> clinicConfigurationMappings = new ArrayList<>();
        clinicConfigurationMappings.add(clinicConfigurationMapping);
        clinic.setClinicConfigurationMappings(clinicConfigurationMappings);
        clinicDao.merge(clinic);
        assertTrue(
            "The getting is pseudonymization service activated activated was not the expected one",
                testClinicconfigurationMappingDao.isPseudonymizationServiceActivated(clinicConfigurationMapping.getClinic().getId()));
    }
}
