package de.imi.mopat.dao.user.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.auth.PepperedBCryptPasswordEncoder;
import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.user.User;
import de.imi.mopat.model.user.UserTest;
import de.imi.mopat.utils.Helper;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class UserDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    UserDao testUserDao;
    @Autowired
    private PepperedBCryptPasswordEncoder passwordEncoder;

    /**
     * Test of {@link UserDaoImpl#loadUserByUsername}.<br> Valid input: existing and not existing
     * username
     */
    @Test
    public void testLoadUserByUsername() {
        assertNull("There was a user for a random username", testUserDao.loadUserByUsername(
                Helper.getRandomAlphabeticString(random.nextInt(25) + 3)));
        User testUser = UserTest.getNewValidUser();
        testUser.setFirstname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
        testUser.setLastname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
        testUser.setEmail(Helper.getRandomMailAddress());
        testUser.setPrincipal(random.nextBoolean());
        testUser.setSalt(Helper.getRandomAlphabeticString(random.nextInt(10) + 1));
        testUserDao.persist(testUser);
        assertEquals("The getting User was not the expected one", testUser,
                testUserDao.loadUserByUsername(testUser.getUsername()));
        testUserDao.remove(testUser);
    }


    @Test
    public void testLoadUserByUsernamePreventsSqlInjection() {
        String suffix = UUID.randomUUID()
                .toString().replace("-", "");

        User user1 = UserTest.getNewValidUser();
        user1.setUsername("testuser1" + suffix);
        user1.setEmail("user1" + suffix + "@example.com");
        user1.setFirstname("Test");
        user1.setLastname("One");
        user1.setSalt("testsalt1");

        User user2 = UserTest.getNewValidUser();
        user2.setUsername("testuser2" + suffix);
        user2.setEmail("user2" + suffix + "@example.com");
        user2.setFirstname("Test");
        user2.setLastname("Two");
        user2.setSalt("testsalt2");

        boolean user1Persisted = false;
        boolean user2Persisted = false;

        try {
            testUserDao.persist(user1);
            user1Persisted = true;

            testUserDao.persist(user2);
            user2Persisted = true;

            // 1. Correct user must be returned
            assertEquals(
                    "Wrong user returned for username 1",
                    user1,
                    testUserDao.loadUserByUsername(
                            user1.getUsername()
                    )
            );

            // 2. Second user must be returned separately
            assertEquals(
                    "Wrong user returned for username 2",
                    user2,
                    testUserDao.loadUserByUsername(
                            user2.getUsername()
                    )
            );

            // 3. Injection must not return any user
            assertNull(
                    "JPQL injection returned an unexpected user",
                    testUserDao.loadUserByUsername(
                            "' OR '1'='1"
                    )
            );

            // 4. Non-existing username must return null
            assertNull(
                    "Non-existing username returned a user",
                    testUserDao.loadUserByUsername(
                            "nonexistent" + suffix
                    )
            );

            // 4. SQL injection must not execute a DROP TABLE statement
            String dropTableInjection = "'; DROP TABLE users; --";

            assertNull(
                    "SQL injection with DROP TABLE unexpectedly returned a user",
                    testUserDao.loadUserByUsername(dropTableInjection)
            );

            // 5. Verify that the database is still accessible
            // and both users can still be retrieved
            assertEquals(
                    "User1 could not be retrieved after DROP TABLE injection",
                    user1,
                    testUserDao.loadUserByUsername(user1.getUsername())
            );

            assertEquals(
                    "User2 could not be retrieved after DROP TABLE injection",
                    user2,
                    testUserDao.loadUserByUsername(user2.getUsername())
            );


        } finally {
            if (user2Persisted) {
                testUserDao.remove(user2);
            }
            if (user1Persisted) {
                testUserDao.remove(user1);
            }
        }
    }


    /**
     * Test of {@link UserDaoImpl#setPassword} and {@link UserDaoImpl#isCorrectPassword}.<br> Valid
     * input: random password as String, the set password and a random String
     */
    @Test
    public void testSetPasswordAndIsCorrectPassword() {
        User testUser = UserTest.getNewValidUser();
        testUser.setFirstname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
        testUser.setLastname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
        testUser.setEmail(Helper.getRandomMailAddress());
        testUser.setPrincipal(random.nextBoolean());
        testUser.setSalt(Helper.getRandomAlphabeticString(random.nextInt(10) + 1));
        testUserDao.persist(testUser);

        String testPassword = Helper.getRandomAlphanumericString(random.nextInt(25) + 5);
        testUser.setPassword(testPassword);
        testUserDao.setPassword(testUser);
        assertTrue("The setPassword method did not work properly",
                passwordEncoder.matches(testPassword, testUser.getPassword()));
        assertTrue(
                "The isCorrectPassword method did not work properly, was false but he password was corrct",
                testUserDao.isCorrectPassword(testUser, testPassword));
        assertFalse(
                "The isCorrectPassword method did not work properly, was true but the password was incorrect",
                testUserDao.isCorrectPassword(testUser,
                        Helper.getRandomAlphanumericString(random.nextInt(25) + 5)));

        testUserDao.remove(testUser);
    }

    /**
     * Test of {@link UserDaoImpl#getAllEnabledEMailAddressesDistinct}.<br> Valid input: random
     * enabled or disabled users
     */
    @Test
    public void testGetAllEnabledEMailAddressesDistinct() {
        Set<String> testMails = testUserDao.getAllEnabledEMailAddressesDistinct();
        Set<User> createdUsers = new HashSet<>();

        int count = random.nextInt(25) + 1;
        for (int i = 0; i < count; i++) {
            User testUser = UserTest.getNewValidUser();
            testUser.setFirstname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
            testUser.setLastname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
            testUser.setEmail(Helper.getRandomMailAddress());
            testUser.setPrincipal(random.nextBoolean());
            testUser.setSalt(Helper.getRandomAlphabeticString(random.nextInt(10) + 1));
            if (random.nextBoolean()) {
                testUser.setIsEnabled(Boolean.TRUE);
                testMails.add(testUser.getEmail());
            } else {
                testUser.setIsEnabled(Boolean.FALSE);
            }
            testUserDao.persist(testUser);
            createdUsers.add(testUser);
        }
        assertEquals("The getting list of Mailaddresses was not the expected one", testMails,
                testUserDao.getAllEnabledEMailAddressesDistinct());

        for (User user : createdUsers) {
            testUserDao.remove(user);
        }
    }
}
