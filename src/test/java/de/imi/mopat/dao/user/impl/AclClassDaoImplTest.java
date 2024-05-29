package de.imi.mopat.dao.user.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.utils.Helper;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AclClassDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    AclClassDao testAclClassDao;

    /**
     * Test of {@link AclClassDaoImpl#getElementByClass}.<br> Invalid input: random String<br> Valid
     * input: de.imi.mopat.model.Clinic and de.imi.mopat.model.Bundle as String
     */
    @Test
    public void testGetElementByClass() {
        assertNull(testAclClassDao.getElementByClass(
            Helper.getRandomAlphanumericString(random.nextInt(50) + 1)));
        assertEquals("The getting AclClass was not the expected one", 1,
            (long) testAclClassDao.getElementByClass("de.imi.mopat.model.Clinic").getId());
        assertEquals("The getting AclClass was not the expected one", 2,
            (long) testAclClassDao.getElementByClass("de.imi.mopat.model.Bundle").getId());
    }
}
