package de.imi.mopat.dao.user.impl;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.user.UserDao;
import java.util.Random;
import org.junit.Ignore;
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
public class AclEntryDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    AclEntryDao testAclEntryDao;
    @Autowired
    UserDao userDao;
    @Autowired
    ClinicDao clinicDao;

    /**
     * Test of {@link AclEntryDaoImpl#getEntryForObjectUserAndRight}.<br> Valid input:
     */
    @Test
    @Ignore
    public void testGetEntryForObjectUserAndRight() {

    }

    /**
     * Test of {@link AclEntryDaoImpl#getObjectIdsForClassUserAndRight}.<br> Valid input:
     */
    @Test
    @Ignore
    public void testGetObjectIdsForClassUserAndRight() {

    }

    /**
     * Test of {@link AclEntryDaoImpl#getUserRightsByObject}.<br> Valid input:
     */
    @Test
    @Ignore
    public void testGetUserRightsByObject() {

    }
}
