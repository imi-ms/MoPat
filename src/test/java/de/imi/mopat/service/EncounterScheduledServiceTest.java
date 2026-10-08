package de.imi.mopat.service;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.helper.model.UserDTOMapper;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.ClinicTest;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.EncounterScheduledTest;
import de.imi.mopat.model.dto.UserDTO;
import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.AclClass;
import de.imi.mopat.model.user.AclObjectIdentity;
import de.imi.mopat.model.user.User;
import de.imi.mopat.model.user.UserRole;
import de.imi.mopat.model.user.UserTest;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class EncounterScheduledServiceTest {

    @Autowired
    private EncounterScheduledService encounterScheduledService;

    @Autowired
    private ClinicDao clinicDao;

    @Autowired
    private EncounterScheduledDao encounterScheduledDao;

    @Autowired
    private AclEntryDao aclEntryDao;

    @Autowired
    private AclObjectIdentityDao aclObjectIdentityDao;

    @Autowired
    private AclClassDao aclClassDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserDTOMapper userDTOMapper;

    @Autowired
    private BundleDao bundleDao;

    @Before
    public void setUp() {
        // Create a valid user for the security context
        User currentUser = UserTest.getNewValidUser(UserRole.ROLE_ADMIN);
        userDao.persist(currentUser);

        SecurityContextHolder.setContext(new SecurityContextImpl(
            new UsernamePasswordAuthenticationToken(currentUser, "password",
                currentUser.getAuthorities())
        ));
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // --- Helper: Link User to Clinic via ACL ---
    // The service checks `aclEntryDao.getUserRightsByObject(clinic)` to find users.
    // We must manually create an AclEntry for this relationship.
    private void grantUserRightsOnClinic(User user, Clinic clinic) {
        AclObjectIdentity clinicObjectIdentity = new AclObjectIdentity(clinic.getId(),
            Boolean.TRUE, aclClassDao.getElementByClass(Clinic.class.getName()), user,
            null);
        aclObjectIdentityDao.persist(clinicObjectIdentity);

        UserDTO userDTO = userDTOMapper.apply(user);
        clinicDao.updateUserRights(clinic, List.of(), List.of(userDTO));
    }

    // --- Test 1: New ES -> Rights granted to all Clinic Users with ROLE_ENCOUNTERMANAGER ---
    @Test
    @Transactional(value = "MoPat_User")
    public void testGrantUserRightsForNewEncounterScheduled() {
        // Arrange
        Clinic clinic = ClinicTest.getNewValidClinic();
        clinicDao.merge(clinic);

        // Create a user who is an Encounter Manager
        User emUser = UserTest.getNewValidUser(UserRole.ROLE_ENCOUNTERMANAGER);
        userDao.persist(emUser);

        // Link EM User to Clinic (so the service sees them as a clinic user)
        grantUserRightsOnClinic(emUser, clinic);

        // Create ES
        EncounterScheduled es = EncounterScheduledTest.getNewValidEncounterScheduled();
        // Ensure the ES belongs to the specific clinic we set up
        // Note: getNewValidEncounterScheduled creates its own Clinic.
        // We must re-associate it with our test clinic.
        es.setClinic(clinic);
        bundleDao.merge(es.getBundle());
        encounterScheduledDao.merge(es);

        // Act
        encounterScheduledService.grantUserRightsForEncounterScheduledByClinicUsers(clinic, es);

        // Assert
        Map<User, PermissionType> rights = aclEntryDao.getUserRightsByObject(es);
        assertTrue("EM User should have rights on ES", rights.containsKey(emUser));
        assertEquals(PermissionType.READ, rights.get(emUser));
    }

    // --- Test 2: ES Deleted -> Rights revoked + ObjectIdentity deleted ---
    @Test
    @Transactional(value = "MoPat_User")
    public void testDeleteEncounterScheduledAclEntries() {
        // Arrange
        Clinic clinic = ClinicTest.getNewValidClinic();
        clinicDao.merge(clinic);

        User emUser = UserTest.getNewValidUser(UserRole.ROLE_ENCOUNTERMANAGER);
        userDao.persist(emUser);

        grantUserRightsOnClinic(emUser, clinic);

        EncounterScheduled es = EncounterScheduledTest.getNewValidEncounterScheduled();
        es.setClinic(clinic);
        bundleDao.merge(es.getBundle());
        encounterScheduledDao.merge(es);

        // Setup: Grant rights first
        encounterScheduledService.grantUserRightsForEncounterScheduledByClinicUsers(clinic, es);
        assertTrue("Rights should exist before delete", aclEntryDao.getUserRightsByObject(es).containsKey(emUser));

        // Act
        encounterScheduledService.deleteEncounterScheduledAclEntries(es);

        // Assert
        Map<User, PermissionType> rights = aclEntryDao.getUserRightsByObject(es);
        assertFalse("Rights should be removed", rights.containsKey(emUser));

        AclClass esClass = aclClassDao.getElementByClass(EncounterScheduled.class.getName());
        AclObjectIdentity identity = aclObjectIdentityDao.getElementByClassAndObjectId(esClass, es.getId());
        assertNull("ObjectIdentity should be deleted", identity);
    }

    // --- Test 3: User added to Clinic (IS Encounter Manager) -> Gets rights on all ES ---
    @Test
    @Transactional(value = "MoPat_User")
    public void testAddUserRightsWhenUserIsEncounterManager() {
        // Arrange
        Clinic clinic = ClinicTest.getNewValidClinic();
        clinicDao.merge(clinic);

        User emUser = UserTest.getNewValidUser(UserRole.ROLE_ENCOUNTERMANAGER);
        userDao.persist(emUser);

        grantUserRightsOnClinic(emUser, clinic);

        // Create 2 ES for the clinic
        EncounterScheduled es1 = EncounterScheduledTest.getNewValidEncounterScheduled();
        es1.setClinic(clinic);
        bundleDao.merge(es1.getBundle());
        encounterScheduledDao.merge(es1);

        EncounterScheduled es2 = EncounterScheduledTest.getNewValidEncounterScheduled();
        es2.setClinic(clinic);
        bundleDao.merge(es2.getBundle());
        encounterScheduledDao.merge(es2);

        // Act
        encounterScheduledService.addUserRightsForEncounterScheduledsOfClinic(clinic, emUser);

        // Assert
        assertTrue("User should have rights on ES1", aclEntryDao.getUserRightsByObject(es1).containsKey(emUser));
        assertTrue("User should have rights on ES2", aclEntryDao.getUserRightsByObject(es2).containsKey(emUser));
    }

    // --- Test 4: User added to Clinic (NOT Encounter Manager) -> No rights on ES ---
    @Test
    @Transactional(value = "MoPat_User")
    public void testAddUserRightsWhenUserIsNotEncounterManager() {
        // Arrange
        Clinic clinic = ClinicTest.getNewValidClinic();
        clinicDao.merge(clinic);

        User regularUser = UserTest.getNewValidUser(UserRole.ROLE_USER);
        userDao.persist(regularUser);

        grantUserRightsOnClinic(regularUser, clinic);

        EncounterScheduled es1 = EncounterScheduledTest.getNewValidEncounterScheduled();
        es1.setClinic(clinic);
        bundleDao.merge(es1.getBundle());
        encounterScheduledDao.merge(es1);

        // Act
        encounterScheduledService.addUserRightsForEncounterScheduledsOfClinic(clinic, regularUser);

        // Assert
        assertFalse("Non-EM User should NOT have rights", aclEntryDao.getUserRightsByObject(es1).containsKey(regularUser));
    }

    // --- Test 5: User removed from Clinic -> All ES rights revoked ---
    @Test
    @Transactional(value = "MoPat_User")
    public void testRemoveUserRightsFromClinic() {
        // Arrange
        Clinic clinic = ClinicTest.getNewValidClinic();
        clinicDao.merge(clinic);

        User emUser = UserTest.getNewValidUser(UserRole.ROLE_ENCOUNTERMANAGER);
        userDao.persist(emUser);

        grantUserRightsOnClinic(emUser, clinic);

        EncounterScheduled es1 = EncounterScheduledTest.getNewValidEncounterScheduled();
        es1.setClinic(clinic);
        bundleDao.merge(es1.getBundle());
        encounterScheduledDao.merge(es1);

        // Setup: Grant rights
        encounterScheduledService.addUserRightsForEncounterScheduledsOfClinic(clinic, emUser);
        assertTrue("Rights should exist", aclEntryDao.getUserRightsByObject(es1).containsKey(emUser));

        // Act
        encounterScheduledService.removeUserRightsForEncounterScheduledsOfClinic(clinic, emUser);

        // Assert
        assertFalse("Rights should be removed", aclEntryDao.getUserRightsByObject(es1).containsKey(emUser));
    }

    // --- Test 6: User promoted to Encounter Manager -> Gets rights ---
    @Test
    @Transactional(value = "MoPat_User")
    public void testUpdateUserRightsWhenPromotedToEncounterManager() {
        // Arrange
        Clinic clinic = ClinicTest.getNewValidClinic();
        clinicDao.merge(clinic);

        // Start as Regular User
        User user = UserTest.getNewValidUser(UserRole.ROLE_USER);
        userDao.persist(user);

        grantUserRightsOnClinic(user, clinic);

        EncounterScheduled es1 = EncounterScheduledTest.getNewValidEncounterScheduled();
        es1.setClinic(clinic);
        bundleDao.merge(es1.getBundle());
        encounterScheduledDao.merge(es1);

        // Setup: No rights initially
        assertFalse("User should not have rights initially", aclEntryDao.getUserRightsByObject(es1).containsKey(user));

        // Act: Promote
        user.replaceRolesWith(UserRole.ROLE_ENCOUNTERMANAGER);
        userDao.persist(user);

        encounterScheduledService.updateUserRightsForEncounterScheduledsOfAllAssignedClinics(user);

        // Assert
        assertTrue("User should have rights after promotion", aclEntryDao.getUserRightsByObject(es1).containsKey(user));
    }

    // --- Test 7: User demoted to Regular User -> Loses rights ---
    @Test
    @Transactional(value = "MoPat_User")
    public void testUpdateUserRightsWhenDemotedToRegularUser() {
        // Arrange
        Clinic clinic = ClinicTest.getNewValidClinic();
        clinicDao.merge(clinic);

        // Start as EM
        User user = UserTest.getNewValidUser(UserRole.ROLE_ENCOUNTERMANAGER);
        userDao.persist(user);

        grantUserRightsOnClinic(user, clinic);

        EncounterScheduled es1 = EncounterScheduledTest.getNewValidEncounterScheduled();
        es1.setClinic(clinic);
        bundleDao.merge(es1.getBundle());
        encounterScheduledDao.merge(es1);

        // Setup: Grant rights
        encounterScheduledService.addUserRightsForEncounterScheduledsOfClinic(clinic, user);
        assertTrue("Rights should exist", aclEntryDao.getUserRightsByObject(es1).containsKey(user));

        // Act: Demote
        user.replaceRolesWith(UserRole.ROLE_USER);
        userDao.persist(user);

        encounterScheduledService.updateUserRightsForEncounterScheduledsOfAllAssignedClinics(user);

        // Assert
        assertFalse("Rights should be removed after demotion", aclEntryDao.getUserRightsByObject(es1).containsKey(user));
    }

    // --- Test 8: Refresh All Rights ---
    @Test
    @Transactional(value = "MoPat_User")
    public void testRefreshAllUserRightsForEncounterScheduled() {
        // Arrange
        Clinic clinic = ClinicTest.getNewValidClinic();
        clinicDao.merge(clinic);

        User emUser = UserTest.getNewValidUser(UserRole.ROLE_ENCOUNTERMANAGER);
        userDao.persist(emUser);

        grantUserRightsOnClinic(emUser, clinic);

        EncounterScheduled es1 = EncounterScheduledTest.getNewValidEncounterScheduled();
        es1.setClinic(clinic);
        bundleDao.merge(es1.getBundle());
        encounterScheduledDao.merge(es1);

        // Act
        encounterScheduledService.refreshAllUserRightsForEncounterScheduled();

        // Assert
        assertTrue("User should have rights after refresh", aclEntryDao.getUserRightsByObject(es1).containsKey(emUser));
    }
}