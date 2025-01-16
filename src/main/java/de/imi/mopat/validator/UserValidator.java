package de.imi.mopat.validator;

import de.imi.mopat.auth.MoPatActiveDirectoryLdapAuthenticationProvider;
import de.imi.mopat.auth.PepperedBCryptPasswordEncoder;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * The validator for {@link User User} objects.
 */
@Component
public class UserValidator implements Validator {

    @Autowired
    UserDao userDao;
    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private PepperedBCryptPasswordEncoder passwordEncoder;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ApplicationContext appContext;

    /**
     * Helper function to check, if pin is a consecutive number like 123456, 12345678, etc.
     *
     * @param pin to be checked
     * @return true if pin is consecutive number, false otherwise
     */
    private static boolean isConsecutiveSequence(String pin) {
        for (int i = 0; i <= pin.length() - 2; i++) {
            int first = Character.getNumericValue(pin.charAt(i));
            int second = Character.getNumericValue(pin.charAt(i + 1));

            if (first + 1 != second && first - 1 != second) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean supports(final Class<?> type) {
        return User.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        User user = (User) target;

        // [bt] first, let the standard validator validate the target object
        // with respect to it's JSR-303 constraints (jakarta.validation
        // .constraints annotations)
        validator.validate(target, errors);

        Boolean checkNewPassword = false;
        // If the user is already in database
        if (user.getId() != null) {
            if (user.getOldPassword() != null && !user.getOldPassword().isEmpty()) {
                // Check if the old password is correct
                if (!isPasswordCorrect(user)) {
                    errors.rejectValue("oldPassword", "errormessage",
                        messageSource.getMessage("user.error.passwordNotCorrect", new Object[]{},
                            LocaleContextHolder.getLocale()));
                } else {
                    checkNewPassword = true;
                }
            }
        } else if (user.isLdap()) {
            if (user.getNewPassword() == null || user.getNewPassword().isEmpty()) {
                errors.rejectValue("newPassword", "errormessage",
                    messageSource.getMessage("user.error.passwordNotSet", new Object[]{},
                        LocaleContextHolder.getLocale()));
            } else {
                MoPatActiveDirectoryLdapAuthenticationProvider adAuthenticationProvider = appContext.getBean(
                    "adAuthenticationProvider",
                    MoPatActiveDirectoryLdapAuthenticationProvider.class);
                // Try authentication via active directory
                try {
                    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        user.getUsername(), user.getNewPassword());
                    adAuthenticationProvider.authenticate(token);
                    // If a Ldap user was authenticated successfully, check
                    // if the username is already in use
                    if (userDao.loadUserByUsername(user.getUsername()) != null) {
                        errors.rejectValue("username", "errormessage",
                            messageSource.getMessage("user.error.usernameInUse", new Object[]{},
                                LocaleContextHolder.getLocale()));
                    }
                } catch (BadCredentialsException thrownException) {
                    // If authentication via active directory was not
                    // successful return the thrown exception
                    errors.rejectValue("newPassword", "errormessage",
                        messageSource.getMessage("user.error.passwordNotCorrect", new Object[]{},
                            LocaleContextHolder.getLocale()));
                } catch (InsufficientAuthenticationException thrownException) {
                    // This exception returns if a user is not stored in the
                    // database, which is correct for a new registered user
                    // If a Ldap user was authenticated successfully, check
                    // if the username is already in use
                    if (userDao.loadUserByUsername(user.getUsername()) != null) {
                        errors.rejectValue("username", "errormessage",
                            messageSource.getMessage("user.error.usernameInUse", new Object[]{},
                                LocaleContextHolder.getLocale()));
                    }
                }
            }
        } else {
            // Check if the username is already in use
            if (userDao.loadUserByUsername(user.getUsername()) != null) {
                errors.rejectValue("username", "errormessage",
                    messageSource.getMessage("user.error.usernameInUse", new Object[]{},
                        LocaleContextHolder.getLocale()));
            } else {
                checkNewPassword = true;
            }
        }
        if (checkNewPassword) {
            // Check if the new password is set and has the correct length
            if (user.getNewPassword() == null
                || user.getNewPassword().length() < Constants.PASSWORD_MINIMUM_SIZE
                || user.getNewPassword().length() > Constants.PASSWORD_MAXIMUM_SIZE) {
                errors.rejectValue("newPassword", "errormessage",
                    messageSource.getMessage("user.error.passwordSize",
                        new Object[]{Constants.PASSWORD_MINIMUM_SIZE,
                            Constants.PASSWORD_MAXIMUM_SIZE}, LocaleContextHolder.getLocale()));
            }
            // Check if the new password and the password approval fields are
            // the same
            if (user.getNewPassword() != null && !user.getNewPassword()
                .equals(user.getPasswordCheck())) {
                errors.rejectValue("passwordCheck", "errormessage",
                    messageSource.getMessage("user.error.passwordsNotMatching", new Object[]{},
                        LocaleContextHolder.getLocale()));
            }
        }

        //Check if pin is activated, but not set
        if (user.getUsePin()) {
            if (user.getPin().isEmpty() || user.getPin() == null) {
                errors.rejectValue("pin", "errormessage",
                    messageSource.getMessage("user.error.pinActivatedButNull", new Object[]{},
                        LocaleContextHolder.getLocale()));
            } else {
                // Check if Pin is long enough
                if (user.getPin().length() < Constants.PIN_MINIMUM_SIZE) {
                    errors.rejectValue("pin", "errormessage",
                        messageSource.getMessage("user.error.pinTooShort", new Object[]{},
                            LocaleContextHolder.getLocale()));
                }
                //Check if pin is a single repeated digit or a consecutive number
                if (user.getPin().matches("\\b(\\d)\\1+\\b") || isConsecutiveSequence(
                    user.getPin())) {
                    errors.rejectValue("pin", "errormessage",
                        messageSource.getMessage("user.error.pinNotSecure", new Object[]{},
                            LocaleContextHolder.getLocale()));
                }
            }
        }
    }

    public boolean isPasswordCorrect(final User user) {
        return passwordEncoder.matches(user.getOldPassword(), user.getPassword());
    }

}