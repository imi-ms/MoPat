package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.helper.model.EncounterScheduledDTOMapper;
import de.imi.mopat.model.EncounterScheduledTest;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;
import de.imi.mopat.utils.Helper;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
public class EncounterScheduledDTOValidatorTest {

    private static final Random random = new Random();
    @Autowired
    EncounterScheduledDTOValidator encounterScheduledDTOValidator;
    @Autowired
    MessageSource messageSource;
    @Autowired
    private EncounterScheduledDTOMapper encounterScheduledDTOMapper;

    /**
     * Test of {@link EncounterScheduledDTOValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link EncounterScheduledDTO#class}<br> Invalid input: Other classes than
     * {@link EncounterScheduledDTO#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for EncounterScheduledDTO.class failed. ExportRulesDTOValidator didn't support EncounterScheduledDTO.class except it was expected to do.",
            encounterScheduledDTOValidator.supports(EncounterScheduledDTO.class));
        assertFalse(
            "Supports method for random class failed. ExportRulesDTOValidator supported that class except it wasn't expected to do.",
            encounterScheduledDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link EncounterScheduledDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}.
     * <br>
     * Valid input: Instance of {@link EncounterScheduledDTO} that doesn't fit to all of the lower
     * conditions for invalid object and instance of {@link Errors}. <br> Invalid input: Instance of
     * {@link EncounterScheduledDTO} containing a startDate or endDate in the past.<br> For an
     * instance containing different {@link EncounterScheduledSerialType} to <code>UNIQUELY</code>
     * are the following cases invalid.<br> The startDate and/or endDate are empty or
     * <code>null</code>.<br> The endDate is before or equal to startDate.<br> The repeat period is
     * less or equal to zero or equals
     * <code>null</code>.<br>
     * The reply mail is invalid mail address.
     */
    @Test
    public void testValidate() {
        //Setting the date
        Long now = System.currentTimeMillis();
        Date startDate = new Date((now + ((Integer) random.nextInt(1337)).longValue()));
        Date endDate = new Date(startDate.getTime() + ((Integer) random.nextInt(1337)).longValue());
        Date pastDate = new Date(now - Math.abs(random.nextLong()));

        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));

        EncounterScheduledDTO encounterScheduledDTO = encounterScheduledDTOMapper.apply(
            EncounterScheduledTest.getNewValidEncounterScheduled());
        BundleDTO spyBundleDTO = Mockito.spy(encounterScheduledDTO.getBundleDTO());
        Mockito.when(spyBundleDTO.getId()).thenReturn(Math.abs(random.nextLong()));
        encounterScheduledDTO.setBundleDTO(spyBundleDTO);
        encounterScheduledDTO.setReplyMail("");

        String message, testErrorMessage;

        //Test valid encounterScheduled
        encounterScheduledDTO.setStartDate(startDate);
        encounterScheduledDTO.setEndDate(endDate);
        Integer distance =
            ((Long) endDate.getTime()).intValue() - ((Long) startDate.getTime()).intValue();
        encounterScheduledDTO.setRepeatPeriod(random.nextInt(distance) + 1);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertFalse(
            "Validation of encounterScheduled failed. The returned result has caught errors except it wasn't expected to do. ",
            result.hasErrors());

        //Test startDate before today
        encounterScheduledDTO.setStartDate(pastDate);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed for startDate before today. The returned result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage(
            "encounterScheduled.validator.startdateCanNotBeInThePast", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for startDate before today. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        encounterScheduledDTO.setStartDate(startDate);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //EncocunterScheduledSerialType weekly
        encounterScheduledDTO.setEncounterScheduledSerialType(EncounterScheduledSerialType.WEEKLY);
        encounterScheduledDTO.setEndDate(null);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed for empty endDate. The returned result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("encounterScheduled.validator.enddateEmpty",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for empty endDate. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //EncocunterScheduledSerialType monthly
        encounterScheduledDTO.setEncounterScheduledSerialType(EncounterScheduledSerialType.MONTHLY);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed for empty endDate. The returned result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("encounterScheduled.validator.enddateEmpty",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for empty endDate. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        //EncocunterScheduledSerialType repeatedly
        encounterScheduledDTO.setEncounterScheduledSerialType(
            EncounterScheduledSerialType.REPEATEDLY);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed for empty endDate. The returned result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("encounterScheduled.validator.enddateEmpty",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for empty endDate. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        encounterScheduledDTO.setEndDate(pastDate);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed for endDate before today. The returned result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("encounterScheduled.validator.enddateCanNotBeInThePast",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for endDate before today. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        encounterScheduledDTO.setEndDate(startDate);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed for endDate equal to startDate. The returned result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        encounterScheduledDTO.setStartDate(endDate);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed for endDate less or equal to startDate. The returned result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage(
            "encounterScheduled.validator.enddateMustBeAfterStartdate", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for endDate less  or equal to startDate. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        encounterScheduledDTO.setStartDate(startDate);
        encounterScheduledDTO.setEndDate(endDate);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        encounterScheduledDTO.setEncounterScheduledSerialType(
            EncounterScheduledSerialType.REPEATEDLY);
        encounterScheduledDTO.setRepeatPeriod(null);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed for repeat period null. The returned result hasn't caught errors except it was expected to do.",
            result.hasErrors());
        encounterScheduledDTO.setRepeatPeriod(-1 * random.nextInt());
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed for repeat period less or equal to zero. The returned result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage(
            "encounterScheduled.validator.repeatPeriodGreaterThanZero", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for repeat period less or equal to zero. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        encounterScheduledDTO.setRepeatPeriod(random.nextInt(6) + 1);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        String email = Helper.getRandomAlphabeticString(random.nextInt(10)) + "@"
            + Helper.getRandomAlphabeticString(random.nextInt(3) + 3);
        encounterScheduledDTO.setReplyMail(email);
        encounterScheduledDTO.setReplyMails(new HashMap<>());
        encounterScheduledDTO.getReplyMails().put(spyBundleDTO.getId(), new HashSet<>());
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed for invalid replyMail. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("encounterScheduled.validator.invalidReplyMail",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for invalid replyMail. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));

        Set<String> emails = new HashSet<>();
        for (int i = 0; i < random.nextInt(5) + 1; i++) {
            emails.add(Helper.getRandomAlphabeticString(random.nextInt(10)) + "@"
                + Helper.getRandomAlphabeticString(random.nextInt(3) + 3));
        }
        emails.add(email);

        encounterScheduledDTO.getReplyMails().get(spyBundleDTO.getId()).add(email);
        encounterScheduledDTO.getReplyMails().get(spyBundleDTO.getId()).addAll(emails);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertFalse(
            "Validation of encounterScheduled failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        encounterScheduledDTO.setStartDate(new Date());
        encounterScheduledDTO.setEndDate(new Date(
            encounterScheduledDTO.getStartDate().getTime() + ((Integer) random.nextInt(
                324550)).longValue() + 1000));
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertFalse(
            "Validation of encounterScheduled failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        encounterScheduledDTO.setEncounterScheduledSerialType(
            EncounterScheduledSerialType.UNIQUELY);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertFalse(
            "Validation of encounterScheduled failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        encounterScheduledDTO.setEncounterScheduledSerialType(
            EncounterScheduledSerialType.REPEATEDLY);
        encounterScheduledDTO.setStartDate(null);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("encounterScheduled.validator.startDateEmpty",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for startDate null. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        Calendar calendar = Calendar.getInstance();
        Date today = new Date();
        calendar.setTime(today);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        today = calendar.getTime();

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        encounterScheduledDTO.setStartDate(pastDate);
        encounterScheduledDTO.setEndDate(today);
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        message = messageSource.getMessage(
            "encounterScheduled.validator.startdateCanNotBeInThePast", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for startDate in the past. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        message = messageSource.getMessage(
            "encounterScheduled.validator.enddateMustBeAfterStartdate", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(1).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for endDate after startDate. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        encounterScheduledDTO.setStartDate(startDate);
        encounterScheduledDTO.setEndDate(endDate);
        encounterScheduledDTO.setEncounterScheduledSerialType(
            EncounterScheduledSerialType.REPEATEDLY);
        encounterScheduledDTO.setRepeatPeriod(-1 * Math.abs(random.nextInt()));
        encounterScheduledDTOValidator.validate(encounterScheduledDTO, result);
        assertTrue(
            "Validation of encounterScheduled failed for repeatPeriod less than 0. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        message = messageSource.getMessage(
            "encounterScheduled.validator.repeatPeriodGreaterThanZero", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of encounterScheduled failed for repeatPeriod less than 0. The returned error message didn't match the expected one.",
            message, testErrorMessage);
    }
}
