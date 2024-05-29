package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.AuditEntry;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import de.imi.mopat.utils.Helper;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
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
public class AuditEntryValidatorTest {

    private static final Random random = new Random();
    @Autowired
    private AuditEntryValidator auditEntryValidator;
    @Autowired
    private MessageSource messageSource;

    /**
     * Test of {@link AuditEntryValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link AuditEntry#class}<br> Invalid input: Other class than {@link AuditEntry#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for AuditEntry.class failed. AuditEntryValidator didn't support AuditEntry.class except it was expected to do.",
            auditEntryValidator.supports(AuditEntry.class));
        assertFalse(
            "Supports method for random class failed. AuditEntryValidator supported that class except it wasn't expected to do.",
            auditEntryValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link AuditEntryValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}<br> Valid input: Instance of {@link AuditEntry} and
     * instance of {@link Errors}.<br>
     * <p>
     * Notice: The validation case for senderReciever equals null or is empty cannot be tested due
     * to assert statements implemented in the auditEntry constructor.
     */
    @Test
    public void testValidate() {
        Set<AuditPatientAttribute> patientAttributes = new HashSet<>();

        Collections.addAll(patientAttributes, AuditPatientAttribute.values());

        AuditEntryActionType type;
        do {
            type = Helper.getRandomEnum(AuditEntryActionType.class);
        } while (type == AuditEntryActionType.RECEIVED || type == AuditEntryActionType.SENT);

        //Valid instance
        AuditEntry auditEntry = new AuditEntry(
            Helper.getRandomAlphabeticString(random.nextInt(35) + 1),
            Helper.getRandomAlphabeticString(random.nextInt(35) + 1),
            Helper.getRandomAlphabeticString(random.nextInt(35) + 1), patientAttributes, type);
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        auditEntryValidator.validate(auditEntry, result);
        assertFalse(
            "Validation of valid instance failed. The returned result has caught errors although it wasn't expected to do so.",
            result.hasErrors());

        //Invalid instance actionType SENT
        auditEntry = new AuditEntry(Helper.getRandomAlphabeticString(random.nextInt(35) + 1),
            Helper.getRandomAlphabeticString(random.nextInt(35) + 1),
            Helper.getRandomAlphabeticString(random.nextInt(35) + 1), patientAttributes,
            AuditEntryActionType.SENT);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        auditEntryValidator.validate(auditEntry, result);
        assertTrue(
            "Validation of invalid instance failed. The returned result hasn't caught errors although it wasn't expected to do so.",
            result.hasErrors());

        String message = messageSource.getMessage("auditEntry.error.noSenderReceiver",
            new Object[]{}, LocaleContextHolder.getLocale());
        String testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of invalid instance failed. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //Invalid instance actionType RECEIVED
        auditEntry = new AuditEntry(Helper.getRandomAlphabeticString(random.nextInt(35) + 1),
            Helper.getRandomAlphabeticString(random.nextInt(35) + 1),
            Helper.getRandomAlphabeticString(random.nextInt(35) + 1), patientAttributes,
            AuditEntryActionType.RECEIVED);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        auditEntryValidator.validate(auditEntry, result);
        assertTrue(
            "Validation of invalid instance failed. The returned result hasn't caught errors although it wasn't expected to do so.",
            result.hasErrors());

        message = messageSource.getMessage("auditEntry.error.noSenderReceiver", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of invalid instance failed. The returned error message didn't match the expected one.",
            message, testErrorMessage);
    }
}
