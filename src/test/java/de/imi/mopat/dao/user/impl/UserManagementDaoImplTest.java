package de.imi.mopat.dao.user.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.user.ForgotPasswordTokenDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.user.ForgotPasswordToken;
import de.imi.mopat.model.user.User;
import de.imi.mopat.model.user.UserTest;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Ignore;
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
public class UserManagementDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    UserDao userDao;
    @Autowired
    ForgotPasswordTokenDao forgotPasswordTokenDao;

    /**
     * Test of {@link UserManagementDaoImpl#getEntityClass}.<br>
     */
    @Test
    public void testGetEntityClass() {
        assertEquals("The getting entityClass was not the expected one", User.class,
            userDao.getEntityClass());
    }

    /**
     * Test of {@link UserManagementDaoImpl#merge}, {@link UserManagementDaoImpl#persist},
     * {@link UserManagementDaoImpl#getElementById} and {@link UserManagementDaoImpl#remove}.<br>
     * Valid input: random User with new mail address
     */
    @Test
    public void testPersistAndMergeAndGetElementByIdAndRemove() {
        User testUser = UserTest.getNewValidUser();
        testUser.setFirstname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
        testUser.setLastname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
        testUser.setEmail(Helper.getRandomMailAddress());
        testUser.setPrincipal(random.nextBoolean());
        testUser.setSalt(Helper.getRandomAlphabeticString(random.nextInt(10) + 1));
        userDao.persist(testUser);
        assertEquals("The getting user was not the expected one", testUser,
            userDao.getElementById(testUser.getId()));
        testUser.setEmail(Helper.getRandomMailAddress());
        assertNotEquals("The Email address was updated without a merge", testUser.getEmail(),
            userDao.getElementById(testUser.getId()).getEmail());
        userDao.merge(testUser);
        assertEquals("The Email address was not merged properly", testUser.getEmail(),
            userDao.getElementById(testUser.getId()).getEmail());
        userDao.remove(testUser);
        assertNull("The getting user was not null after removing it",
            userDao.getElementById(testUser.getId()));
    }

    /**
     * Test of {@link UserManagementDaoImpl#getElementByUUID}.<br> Valid input: not existing UUID
     * and UUID of an existing element
     */
    @Test
    public void testGetElementByUUID() {
        assertNull("The getting Element was not null although the UUID was just created",
            forgotPasswordTokenDao.getElementByUUID(UUIDGenerator.createUUID()));
        User testUser = UserTest.getNewValidUser();
        testUser.setFirstname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
        testUser.setLastname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
        testUser.setEmail(Helper.getRandomMailAddress());
        testUser.setPrincipal(random.nextBoolean());
        testUser.setSalt(Helper.getRandomAlphabeticString(random.nextInt(10) + 1));
        userDao.persist(testUser);
        ForgotPasswordToken testForgotPasswordToken = new ForgotPasswordToken(testUser);
        forgotPasswordTokenDao.persist(testForgotPasswordToken);
        assertEquals("The getting ForgotPasswordToken was not the expected one",
            testForgotPasswordToken,
            forgotPasswordTokenDao.getElementByUUID(testForgotPasswordToken.getUuid()));
        forgotPasswordTokenDao.remove(testForgotPasswordToken);
        userDao.remove(testUser);
    }

    /**
     * Test of {@link UserManagementDaoImpl#getElementsById}.<br> Valid input: empty list and list
     * of existing ids
     */
    @Test
    public void testGetElementsById() {
        List<User> newUsers = new ArrayList<>();
        List<Long> newUserIds = new ArrayList<>();
        assertTrue(
            "The getting list of elements was not empty although the given list of ids was empty",
            userDao.getElementsById(newUserIds).isEmpty());
        int count = random.nextInt(25) + 1;
        for (int i = 0; i < count; i++) {
            User testUser = UserTest.getNewValidUser();
            testUser.setFirstname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
            testUser.setLastname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
            testUser.setEmail(Helper.getRandomMailAddress());
            testUser.setPrincipal(random.nextBoolean());
            testUser.setSalt(Helper.getRandomAlphabeticString(random.nextInt(10) + 1));
            userDao.persist(testUser);
            newUserIds.add(testUser.getId());
            newUsers.add(testUser);
        }
        assertEquals("The getting list of elements was not the expected one", newUsers,
            userDao.getElementsById(newUserIds));

        for (User user : newUsers) {
            userDao.remove(user);
        }
    }

    /**
     * Test of {@link UserManagementDaoImpl#getAllElements}.
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetAllElements() {
        List<User> existingUsers = userDao.getAllElements();
        List<User> newUsers = new ArrayList<>();
        int count = random.nextInt(25) + 1;
        for (int i = 0; i < count; i++) {
            User testUser = UserTest.getNewValidUser();
            testUser.setFirstname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
            testUser.setLastname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
            testUser.setEmail(Helper.getRandomMailAddress());
            testUser.setPrincipal(random.nextBoolean());
            testUser.setSalt(Helper.getRandomAlphabeticString(random.nextInt(10) + 1));
            userDao.persist(testUser);
            newUsers.add(testUser);
        }
        existingUsers.addAll(newUsers);
        assertEquals("The getting list of all elements was not the expected one", existingUsers,
            userDao.getAllElements());

        for (User user : newUsers) {
            userDao.remove(user);
        }
    }

    /**
     * Test of {@link UserManagementDaoImpl#}.
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetCount() {
        Long countUsers = (long) userDao.getAllElements().size();
        List<User> newUsers = new ArrayList<>();
        int count = random.nextInt(25) + 1;
        for (int i = 0; i < count; i++) {
            User testUser = UserTest.getNewValidUser();
            testUser.setFirstname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
            testUser.setLastname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
            testUser.setEmail(Helper.getRandomMailAddress());
            testUser.setPrincipal(random.nextBoolean());
            testUser.setSalt(Helper.getRandomAlphabeticString(random.nextInt(10) + 1));
            userDao.persist(testUser);
            newUsers.add(testUser);
        }
        countUsers += count;
        assertEquals("The getting count of all elements was not the expected one", countUsers,
            userDao.getCount());

        for (User user : newUsers) {
            userDao.remove(user);
        }
    }

    /**
     * Test of {@link UserManagementDaoImpl#grantRight}.<br> Valid input:
     */
    @Test
    @Ignore
    public void testGrantRight() {

    }

    /**
     * Test of {@link UserManagementDaoImpl#grantInheritedRight}.<br> Valid input:
     */
    @Test
    @Ignore
    public void testGrantInheritedRight() {

    }

    /**
     * Test of {@link UserManagementDaoImpl#revokeRight}.<br> Valid input:
     */
    @Test
    @Ignore
    public void testRevokeRight() {

    }

    /**
     * Test of {@link UserManagementDaoImpl#revokeInheritedRight}.<br> Valid input:
     */
    @Test
    @Ignore
    public void testRevokeInheritedRight() {

    }
}
