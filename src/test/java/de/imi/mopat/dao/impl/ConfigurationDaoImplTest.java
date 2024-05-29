package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.ConfigurationDao;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class ConfigurationDaoImplTest {

    @Autowired
    ConfigurationDao testConfigurationDao;

    /**
     * Test of {@link ConfigurationDaoImpl#getConfigurationByAttributeAndClass}.<br>
     */
    @Test
    public void testWrongInput() {
        assertNull("The getting base URL was not the expected one",
            testConfigurationDao.getConfigurationByAttributeAndClass("test", "test"));
    }

    /**
     * Test of {@link ConfigurationDaoImpl#getBaseURL}.<br>
     */
    @Test
    public void testGetBaseURL() {
        assertEquals("The getting base URL was not the expected one", "http://localhost:8080",
            testConfigurationDao.getBaseURL());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#getDefaultLanguage}.<br>
     */
    @Test
    public void testGetDefaultLanguage() {
        assertEquals("The getting default language was not the expected one", "de_DE",
            testConfigurationDao.getDefaultLanguage());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#getDefaultLocale}.<br>
     */
    @Test
    public void testGetDefaultLocale() {
        assertEquals("The getting default locale was not the expected one", new Locale("de", "DE"),
            testConfigurationDao.getDefaultLocale());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#getObjectStoragePath}.<br>
     */
    @Test
    public void testGetObjectStoragePath() {
        assertEquals("The getting object storage path was not the expected one",
            "/var/lib/tomcat10/upload/", testConfigurationDao.getObjectStoragePath());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#getLogo}.<br>
     */
    @Test
    public void testGetLogo() {
        assertNull("The getting logo was not the expected one", testConfigurationDao.getLogo());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#getSupportEMail}.<br>
     */
    @Test
    public void testGetSupportEMail() {
        assertEquals("The getting support mail was not the expected one", "",
            testConfigurationDao.getSupportEMail());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#getSupportPhone}.<br>
     */
    @Test
    public void testGetSupportPhone() {
        assertEquals("The getting support phone was not the expected one", "",
            testConfigurationDao.getSupportPhone());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#getFinishedEncounterTimeWindow}.<br>
     */
    @Test
    public void testGetFinishedEncounterTimeWindow() {
        assertEquals("The getting finished encounter time window was not the expected one",
            2592000000L, (long) testConfigurationDao.getFinishedEncounterTimeWindow());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#getIncompleteEncounterTimeWindow}.<br>
     */
    @Test
    public void testGetIncompleteEncounterTimeWindow() {
        assertEquals("The getting incomplete encounter time window was not the expected one",
            15552000000L, (long) testConfigurationDao.getIncompleteEncounterTimeWindow());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#getFinishedEncounterMailaddressTimeWindow}.<br>
     */
    @Test
    public void testGetFinishedEncounterMailaddressTimeWindow() {
        assertEquals(
            "The getting finished encounter mailaddress time window was not the expected one",
            2592000000L, (long) testConfigurationDao.getFinishedEncounterMailaddressTimeWindow());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#isRegistryOfPatientActivated}.<br>
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testIsRegistryOfPatientActivated() {
        assertTrue("The getting is registry of patient activated was not the expected one",
            testConfigurationDao.isRegistryOfPatientActivated());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#isUsePatientDataLookupActivated}.<br>
     */
    @Test
    public void testIsUsePatientDataLookupActivated() {
        assertFalse("The getting is use patient data lookup activated was not the expected one",
            testConfigurationDao.isUsePatientDataLookupActivated());
    }

    /**
     * Test of {@link ConfigurationDaoImpl#isPseudonymizationServiceActivated}.<br>
     */
    @Test
    public void testIsPseudonymizationServiceActivated() {
        assertTrue(
            "The getting is pseudonymization service activated activated was not the expected one",
            testConfigurationDao.isPseudonymizationServiceActivated());
    }
}
