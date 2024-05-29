package de.imi.mopat.dao.user.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.user.ForgotPasswordTokenDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.model.user.ForgotPasswordToken;
import de.imi.mopat.model.user.User;
import de.imi.mopat.model.user.UserTest;
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
public class ForgotPasswordTokenDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    ForgotPasswordTokenDao testForgotPasswordTokenDao;
    @Autowired
    UserDao userDao;

    /**
     * Test of {@link ForgotPasswordTokenDaoImpl#getElementByUser}.<br> Valid input: {@link User}
     * with and without {@link ForgotPasswordToken}
     */
    @Test
    public void testGetElementByUser() {
        User testUser = UserTest.getNewValidUser();
        testUser.setFirstname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
        testUser.setLastname(Helper.getRandomAlphabeticString(random.nextInt(25) + 3));
        testUser.setEmail(Helper.getRandomMailAddress());
        testUser.setPrincipal(random.nextBoolean());
        testUser.setSalt(Helper.getRandomAlphabeticString(random.nextInt(10) + 1));
        userDao.persist(testUser);
        assertNull("The user had a ForgotPasswordToken although it was never created",
            testForgotPasswordTokenDao.getElementByUser(testUser));

        ForgotPasswordToken testForgotPasswordToken = new ForgotPasswordToken(testUser);
        testForgotPasswordTokenDao.persist(testForgotPasswordToken);
        assertEquals("The getting ForgotPasswordToken was not the expected one",
            testForgotPasswordToken, testForgotPasswordTokenDao.getElementByUser(testUser));

        testForgotPasswordTokenDao.remove(testForgotPasswordToken);
        userDao.remove(testUser);
    }
}
