package de.imi.mopat.dao.impl;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.ClinicConfigurationDao;
import de.imi.mopat.dao.ClinicConfigurationMappingDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.model.*;
import de.imi.mopat.model.enumeration.ConfigurationType;
import de.imi.mopat.utils.Helper;
import org.checkerframework.checker.units.qual.C;
import org.junit.After;
import org.junit.Before;
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

    @Autowired
    ClinicConfigurationDao clinicConfigurationDao;

    private Clinic clinic;

    @Before
    public void setUp() {
        ClinicConfiguration clinicConfiguration = clinicConfigurationDao.getElementById(1L);
        clinic = ClinicTest.getNewValidClinic();
        ClinicConfigurationMapping clinicConfigurationMapping = new ClinicConfigurationMapping(clinic, clinicConfiguration, "true");
        List<ClinicConfigurationMapping> clinicConfigurationMappings = new ArrayList<>();
        clinicConfigurationMappings.add(clinicConfigurationMapping);
        clinicConfiguration = clinicConfigurationDao.getElementById(2L);
        clinicConfigurationMapping = new ClinicConfigurationMapping(clinic, clinicConfiguration, "true");
        clinicConfigurationMappings.add(clinicConfigurationMapping);
        clinicConfiguration = clinicConfigurationDao.getElementById(3L);
        clinicConfigurationMapping = new ClinicConfigurationMapping(clinic, clinicConfiguration, "true");
        clinicConfigurationMappings.add(clinicConfigurationMapping);
        clinic.setClinicConfigurationMappings(clinicConfigurationMappings);
        clinicDao.merge(clinic);
    }

    @After
    public void tearDown(){
        clinicDao.remove(clinic);
    }
}
