package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
public class ConfigurationGroupDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    ConfigurationGroupDao testConfigurationGroupDao;

    /**
     * Test of {@link onfigurationGroupDaoImpl#getConfigurationGroups}.<br> Valid input: random
     * labelMessageCode as String
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetConfigurationGroups() {
        String testLabel = Helper.getRandomAlphanumericString(random.nextInt(25) + 1);
        int countConfigurationGroups = random.nextInt(25) + 1;
        List<ConfigurationGroup> testConfigurationGroups = new ArrayList<>();
        for (int i = 0; i < countConfigurationGroups; i++) {
            ConfigurationGroup testConfigurationGroup = new ConfigurationGroup();
            if (random.nextBoolean()) {
                testConfigurationGroup.setLabelMessageCode(testLabel);
                testConfigurationGroup.setPosition(Math.abs(random.nextInt()) + 10000);
                testConfigurationGroups.add(testConfigurationGroup);
            } else {
                testConfigurationGroup.setLabelMessageCode(
                    Helper.getRandomAlphanumericString(random.nextInt(25) + 1));
                testConfigurationGroup.setPosition(Math.abs(random.nextInt()) + 10000);
            }
            testConfigurationGroupDao.merge(testConfigurationGroup);
        }
        assertEquals("The getting list of ConfigurationGroups was not the expected one",
            testConfigurationGroups, testConfigurationGroupDao.getConfigurationGroups(testLabel));

        List<ConfigurationGroup> allConfigurationGroup = testConfigurationGroupDao.getAllElements();
        for (ConfigurationGroup configurationGroup : allConfigurationGroup) {
            if (configurationGroup.getPosition() >= 10000) {
                testConfigurationGroupDao.remove(configurationGroup);
            }
        }
    }
}
