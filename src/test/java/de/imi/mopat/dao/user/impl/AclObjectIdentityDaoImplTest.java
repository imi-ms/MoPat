package de.imi.mopat.dao.user.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleTest;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.ClinicTest;
import de.imi.mopat.model.user.AclObjectIdentity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@WebAppConfiguration
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
public class AclObjectIdentityDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    AclObjectIdentityDao testAclObjectIdentityDao;
    @Autowired
    AclClassDao aclClassDao;
    @Autowired
    BundleDao bundleDao;
    @Autowired
    ClinicDao clinicDao;
    @Autowired
    UserDao userDao;

    /**
     * Test of {@link AclObjectIdentityDaoImpl#getElementByClassAndObjectId}.<br> Invalid input:
     * <br> Valid input:
     */
    @Test
    @Transactional("MoPat_User")
    public void testGetElementByClassAndObjectId() {
        assertNull(
            "An AclObjectIdentiy with ID=Long.MAX_VALUE was found although it was not created before",
            testAclObjectIdentityDao.getElementByClassAndObjectId(
                aclClassDao.getElementByClass(Clinic.class.getName()), Long.MAX_VALUE));

        Bundle testBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundle);
        AclObjectIdentity bundleObjectIdentity = new AclObjectIdentity(testBundle.getId(),
            Boolean.TRUE, aclClassDao.getElementByClass(Bundle.class.getName()),
            userDao.getElementById(1L), null);
        testAclObjectIdentityDao.persist(bundleObjectIdentity);
        assertEquals("The getting AclObjectidentity was not the expected one", bundleObjectIdentity,
            testAclObjectIdentityDao.getElementByClassAndObjectId(
                aclClassDao.getElementByClass(Bundle.class.getName()), testBundle.getId()));

        Clinic testClinic = ClinicTest.getNewValidClinic();
        clinicDao.merge(testClinic);
        // Create a new ACLObjectIdentity for the bundle and save it
        AclObjectIdentity clinicObjectIdentity = new AclObjectIdentity(testClinic.getId(),
            Boolean.TRUE, aclClassDao.getElementByClass(Clinic.class.getName()),
            userDao.getElementById(1L), null);
        testAclObjectIdentityDao.persist(clinicObjectIdentity);
        assertEquals("The getting AclObjectidentity was not the expected one", clinicObjectIdentity,
            testAclObjectIdentityDao.getElementByClassAndObjectId(
                aclClassDao.getElementByClass(Clinic.class.getName()), testClinic.getId()));

        testAclObjectIdentityDao.remove(bundleObjectIdentity);
        testAclObjectIdentityDao.remove(clinicObjectIdentity);
    }

    /**
     * Test of {@link AclObjectIdentityDaoImpl#getElementsByClass}.<br> Invalid input: <br> Valid
     * input:
     */
    @Test
    @Transactional("MoPat_User")
    public void testGetElementsByClass() {
        List<AclObjectIdentity> testBundleList = new ArrayList<>();
        int count = random.nextInt(15) + 1;
        for (int i = 0; i < count; i++) {
            Bundle testBundle = BundleTest.getNewValidBundle();
            bundleDao.merge(testBundle);
            // Create a new ACLObjectIdentity for the bundle and save it
            AclObjectIdentity bundleObjectIdentity = new AclObjectIdentity(testBundle.getId(),
                Boolean.TRUE, aclClassDao.getElementByClass(Bundle.class.getName()),
                userDao.getElementById(1L), null);
            testAclObjectIdentityDao.persist(bundleObjectIdentity);
            testBundleList.add(bundleObjectIdentity);
        }
        assertTrue("The getting list was not the expected one", testBundleList.containsAll(
            testAclObjectIdentityDao.getElementsByClass(
                aclClassDao.getElementByClass(Bundle.class.getName())))
            && testAclObjectIdentityDao.getElementsByClass(
            aclClassDao.getElementByClass(Bundle.class.getName())).containsAll(testBundleList));

        List<AclObjectIdentity> testClinicList = new ArrayList<>();
        count = random.nextInt(15) + 1;
        for (int i = 0; i < count; i++) {
            Clinic testClinic = ClinicTest.getNewValidClinic();
            clinicDao.merge(testClinic);
            // Create a new ACLObjectIdentity for the bundle and save it
            AclObjectIdentity clinicObjectIdentity = new AclObjectIdentity(testClinic.getId(),
                Boolean.TRUE, aclClassDao.getElementByClass(Clinic.class.getName()),
                userDao.getElementById(1L), null);
            testAclObjectIdentityDao.persist(clinicObjectIdentity);
            testClinicList.add(clinicObjectIdentity);
        }
        assertTrue("The getting list was not the expected one", testClinicList.containsAll(
            testAclObjectIdentityDao.getElementsByClass(
                aclClassDao.getElementByClass(Clinic.class.getName())))
            && testAclObjectIdentityDao.getElementsByClass(
            aclClassDao.getElementByClass(Clinic.class.getName())).containsAll(testClinicList));

        for (AclObjectIdentity aclObjectIdentity : testBundleList) {
            testAclObjectIdentityDao.remove(aclObjectIdentity);
        }
        for (AclObjectIdentity aclObjectIdentity : testClinicList) {
            testAclObjectIdentityDao.remove(aclObjectIdentity);
        }
    }
}
