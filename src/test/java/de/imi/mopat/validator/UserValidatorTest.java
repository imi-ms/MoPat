package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.user.User;
import de.imi.mopat.utils.Helper;
import java.util.HashMap;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class UserValidatorTest {

    private static final Random random = new Random();
    @Autowired
    UserDao userDao;
    @Autowired
    UserValidator userValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link UserValidator#supports(java.lang.Class)} <br> Valid input:
     * {@link UserValidator#class} <br> Invalid input: Other classes than
     * {@link UserValidator#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for SliderAnswer.class failed. SliderAnswerValidator didn't support SliderAnswer.class although it was expected to do.",
            userValidator.supports(User.class));
        assertFalse(
            "Supports method for random class failed. SliderAnswerValidator supported that class although it wasn't expected to do.",
            userValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link UserValidator#validate(java.lang.Object, org.springframework.validation.Errors)}<br>
     * Valid input: Instance of {@link BindingResult} and instance of {@link User} which contains
     * correct password if it's no new user. If the user got no id, it's valid input if the username
     * is not in use and newPassword equals checkPassword.<br> Invalid input: If old user contains
     * password, which doesn't equal the oldPassowrd or the newPassword doesn't match the
     * checkPassword. Another invalid case is that the user's newPassword is of incorrect size.<br>
     * <br>
     * Notice: Got no possibility to test the LDAP-user due to no connection to the LDAP-provider.
     */
    @Test
    public void testValidate() {
        String newPassword, oldPassword, message, testErrorMessage;
        User user;

        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));

        //Get valid user from database by choosing randomly between the two users admin or selenium_user
        if (random.nextBoolean()) {
            user = userDao.loadUserByUsername("admin");
            oldPassword = "admin123";
        } else {
            user = userDao.loadUserByUsername("user");
            oldPassword = "user1234";
        }

        //Set new password
        newPassword = Helper.getRandomAlphabeticString(random.nextInt(13) + 8);

        //Just validate the current user from database without changing any data
        userValidator.validate(user, result);
        assertFalse(
            "Validation of user failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        //valid case
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        user.setOldPassword(oldPassword);
        user.setNewPassword(newPassword);
        user.setPasswordCheck(newPassword);
        userValidator.validate(user, result);
        assertFalse(
            "Validation of user failed for valid instance with newPassword is fitting checkPassword. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        //invalid case
        //new password doesn't fit with the password check
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        user.setPasswordCheck(
            newPassword + Helper.getRandomAlphabeticString(random.nextInt(7) + 1));
        userValidator.validate(user, result);
        assertTrue(
            "Validation of user failed for invalid instance with newPassword not fitting checkPassword. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("user.error.passwordsNotMatching", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of user failed for invalid instance with incorrect password. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //invalid case
        //old password doesn't fit
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        user.setPasswordCheck(newPassword);
        user.setOldPassword(newPassword);
        userValidator.validate(user, result);
        assertTrue(
            "Validation of user failed for invalid instance with incorrect oldPassword. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("user.error.passwordNotCorrect", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of user failed for invalid instance with incorrect oldPassword. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        user.setOldPassword(oldPassword);

        //new user with valid username
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        User newUser = new User();
        newUser.setUsername(Helper.getRandomAlphabeticString(random.nextInt(11) + 3));
        newUser.setPassword(newPassword);
        newUser.setNewPassword(newPassword);
        newUser.setPasswordCheck(newPassword);
        newUser.setSalt(((Integer) Math.abs(random.nextInt())).toString());
        newUser.setEmail(Helper.getRandomMailAddress());
        newUser.setFirstname(Helper.getRandomString(random.nextInt(10) + 3));
        newUser.setLastname(Helper.getRandomString(random.nextInt(10) + 3));
        newUser.setPrincipal(random.nextBoolean());

        userValidator.validate(newUser, result);
        assertFalse(
            "Validation of a valid new user failed. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        //Invalid newPasswordSize
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        newUser.setNewPassword(Helper.getRandomAlphabeticString(random.nextInt(8)));
        userValidator.validate(newUser, result);
        assertTrue(
            "Validation of a invalid new user with new password length to short failed. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("user.error.passwordSize",
            new Object[]{Constants.PASSWORD_MINIMUM_SIZE, Constants.PASSWORD_MAXIMUM_SIZE},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of a new user failed for username already in use. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        newUser.setNewPassword(Helper.getRandomAlphabeticString(256 + random.nextInt(50)));
        userValidator.validate(newUser, result);
        assertTrue(
            "Validation of a invalid new user with new password length to long failed. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of a new user failed for username already in use. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        newUser.setNewPassword(null);
        userValidator.validate(newUser, result);
        assertTrue(
            "Validation of a invalid new user with new password length to long failed. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of a new user failed for username already in use. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        newUser.setNewPassword(newPassword);

        //username already in use
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        newUser.setUsername("admin");
        userValidator.validate(newUser, result);
        assertTrue(
            "Validation of a new user failed for username already in use. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("user.error.usernameInUse", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of a new user failed for username already in use. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //Invalid LDAP user with new password not set
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        newUser.setPassword("");
        newUser.setNewPassword("");
        userValidator.validate(newUser, result);
        assertTrue(
            "Validation of LDAP user failed for password not set. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("user.error.passwordNotSet", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of a new user failed for password not set. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //Valid user with empty old password
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        user.setOldPassword("");
        userValidator.validate(user, result);
        assertFalse(
            "Valdation of user failed. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));

        newUser = new User();
        newUser.setUsername(Helper.getRandomAlphabeticString(random.nextInt(11) + 3));
        newUser.setPasswordCheck(newPassword);
        newUser.setSalt(((Integer) Math.abs(random.nextInt())).toString());
        newUser.setEmail(Helper.getRandomMailAddress());
        newUser.setFirstname(Helper.getRandomString(random.nextInt(10) + 3));
        newUser.setLastname(Helper.getRandomString(random.nextInt(10) + 3));
        newUser.setPrincipal(random.nextBoolean());
        userValidator.validate(newUser, result);
        assertTrue(
            "Validation of user failed. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("user.error.passwordNotSet", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of a new user failed for password not set. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        newUser.setNewPassword("");
        userValidator.validate(newUser, result);
        assertTrue(
            "Validation of user failed. The result hasn't caught errors although it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("user.error.passwordNotSet", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of a new user failed for password not set. The returned error message didn't match the expected one.",
            message, testErrorMessage);
    }

    /**
     * Test of
     * {@link UserValidator#validate(java.lang.Object, org.springframework.validation.Errors)} with
     * LDAP user <br> Invalid input: Instance of {@link User} that contains wrong password or is not
     * listed in the active directory
     * <p>
     * Notice: This this test method only works at local UKM systems and/or systems that has
     * configured a connection to a LDAP/active directory. Otherwise this method shall be
     * ignored.<br> In the case that there is no connection configured please arm this method with
     *
     * @Ignore annotation for successfull build.
     */
    @Test
    public void testValidateLDAPUser() {
        User user = new User();
        String password = UUIDGenerator.createUUID();
        user.setUsername(password);
        user.setEmail(Helper.getRandomMailAddress());
        user.setFirstname(password);
        user.setLastname(password);
        user.setPrincipal(false);
        user.setOldPassword(password);
        user.setNewPassword(password);

        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));

        userValidator.validate(user, result);
        assertTrue(
            "Validation of LDAP user failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        String message = messageSource.getMessage("user.error.passwordNotCorrect", new Object[]{},
            LocaleContextHolder.getLocale());
        String testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of LDAP user failed for invalid user whose user name is in use. The returned error message didn't match the expected one.",
            message, testErrorMessage);
    }

    /**
     * Test of {@link UserValidator#isPasswordCorrect(de.imi.mopat.model.user.User)}<br Invalid
     * input: Instance with a password that doesn't match the old password.
     */
    @Test
    public void testIsPasswordCorrect() {
        User user;
        if (random.nextBoolean()) {
            user = userDao.loadUserByUsername("admin");
        } else {
            user = userDao.loadUserByUsername("user");
        }

        user.setOldPassword(Helper.getRandomAlphabeticString(random.nextInt(11) + 8));
        assertFalse(
            "IsPasswordCorrect failed. The returned value was true although false was expected.",
            userValidator.isPasswordCorrect(user));
    }
}
