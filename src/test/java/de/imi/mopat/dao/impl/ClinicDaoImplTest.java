package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.ClinicTest;
import de.imi.mopat.model.user.AclClass;
import de.imi.mopat.model.user.AclObjectIdentity;
import de.imi.mopat.model.user.UserTest;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.Collection;
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
public class ClinicDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    ClinicDao testClinicDao;
    @Autowired
    AclEntryDao aclEntryDao;
    @Autowired
    AclClassDao aclClassDao;
    @Autowired
    AclObjectIdentityDao aclObjectIdentityDao;
    @Autowired
    BundleDao bundleDao;
    @Autowired
    UserDao userDao;

    /**
     * Test of {@link ClinicDaoImpl#isClinicNameUnused}.<br> Valid input: random name and random Id,
     * existing name and null, existing name and random Id, existing name and associated Id
     */
    @Test
    public void testIsClinicNameUnused() {
        Clinic testClinic = ClinicTest.getNewValidClinic();
        String testName = Helper.getRandomAlphabeticString(random.nextInt(253) + 3);

        assertTrue("A random new name was already used",
            testClinicDao.isClinicNameUnused(testName, random.nextLong()));

        testClinic.setName(testName);
        testClinicDao.merge(testClinic);

        assertFalse("The method returned true although the given Id was null",
            testClinicDao.isClinicNameUnused(testName, null));
        assertFalse("The method returned true although the given Id was random",
            testClinicDao.isClinicNameUnused(testName, random.nextLong()));
        assertTrue("The name was already used in another Bundle",
            testClinicDao.isClinicNameUnused(testName, testClinic.getId()));
    }

    /**
     * Test of {@link ClinicDaoImpl#getClinicsFromAclObjectIdentitys}.<br> Valid input: Collection
     * with {@link AclObjectIdentity AclObjectIdentities} with existing {@link Clinic}-Ids
     */
    @Test
    public void testGetClinicsFromAclObjectIdentitys() {
        int countClinics = random.nextInt(25) + 1;
        Collection<Clinic> testCollection = new ArrayList<>();
        Collection<AclObjectIdentity> aclCollection = new ArrayList<>();
        for (int i = 0; i < countClinics; i++) {
            Clinic testClinic = ClinicTest.getNewValidClinic();
            testClinicDao.merge(testClinic);
            testCollection.add(testClinic);
            aclCollection.add(new AclObjectIdentity(testClinic.getId(), false,
                new AclClass("de.imi.mopat.model.Clinic"), UserTest.getNewValidUser(), null));
        }

        assertEquals("The getting collection of clinics was not the expected one", testCollection,
            testClinicDao.getClinicsFromAclObjectIdentitys(aclCollection));
    }

    /**
     * Test of {@link ClinicDaoImpl#grantInheritedRight} and
     * {@link ClinicDaoImpl#revokeInheritedRight}.<br> Valid input:
     */
    @Test
    @Ignore
    public void testGrantInheritedRightAndRevokeInheritedRight() {

    }

    /**
     * Test of {@link ClinicDaoImpl#updateUserRights}.<br> Valid input:
     */
    @Test
    @Ignore
    public void testUpdateUserRights() {

    }

    /**
     * Test of {@link ClinicDaoImpl#}.<br> Valid input:
     */
    @Test
    @Ignore
    public void testIsInheritedRightDeletable() {

    }
}
