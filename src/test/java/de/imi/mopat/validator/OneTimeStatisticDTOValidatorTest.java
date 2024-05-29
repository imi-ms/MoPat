package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.dto.OneTimeStatisticDTO;
import de.imi.mopat.utils.Helper;
import java.util.Date;
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
public class OneTimeStatisticDTOValidatorTest {

    public static Random random = new Random();
    @Autowired
    OneTimeStatisticDTOValidator oneTimeStatisticDTOValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link OneTimeStatisticDTOValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link OneTimeStatisticDTOValidator#class}<br> Invalid input: Other class than
     * {@link OneTimeStatisticDTOValidator}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for OneTimeStatisticDTO.class failed. OneTimeStatisticDTOValidator didn't support OneTimeStatisticDTO.class except it was expected to do.",
            oneTimeStatisticDTOValidator.supports(OneTimeStatisticDTO.class));
        assertFalse(
            "Supports method for random class failed. OneTimeStatisticDTOValidator supported that class except it wasn't expected to do.",
            oneTimeStatisticDTOValidator.supports(Random.class));
    }

    /**
     * Test of {@link OneTimeStatisticDTOValidatorTest}.<br> Valid input: Instance of
     * {@link OneTimeStatisticDTO} with <code>bundleStartDate</code> before
     * <code>bundleEndDate</code>, <code>patientStartDate</code> before
     * <code>patientEndDate</code>,
     * <code>bundlePatientStartDate</code> before <code>bundlePatientEndDate</code> and instance of
     * instantiable sublcass of {@link BindingResult}. Invalid input: Instance of
     * {@link OneTimeStatisticDTO} where an endDate is before startDate.
     */
    @Test
    public void testValidate() {
        OneTimeStatisticDTO oneTimeStatisticDTO = new OneTimeStatisticDTO();
        Date startDate = new Date();
        Date endDate = new Date(
            startDate.getTime() + Math.abs(((Integer) random.nextInt()).longValue()));

        //case 1: test valid instance
        oneTimeStatisticDTO.setBundleStartDate(startDate);
        oneTimeStatisticDTO.setBundleEndDate(endDate);
        oneTimeStatisticDTO.setPatientStartDate(startDate);
        oneTimeStatisticDTO.setPatientEndDate(endDate);
        oneTimeStatisticDTO.setBundlePatientStartDate(startDate);
        oneTimeStatisticDTO.setBundlePatientEndDate(endDate);
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        oneTimeStatisticDTOValidator.validate(oneTimeStatisticDTO, result);
        assertFalse(
            "Validation of oneTimeStatisticDTO failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //case 2: test invalid instance with bundle-enddate before bundle-startdate
        oneTimeStatisticDTO.setBundleStartDate(endDate);
        oneTimeStatisticDTO.setBundleEndDate(startDate);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        oneTimeStatisticDTOValidator.validate(oneTimeStatisticDTO, result);
        assertTrue(
            "Validation of oneTimeStatisticDTO failed for invalid instance with bundle-enddate before bundle-startdate. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        String message = messageSource.getMessage("statistic.error.enddateBeforeStartdate",
            new Object[]{}, LocaleContextHolder.getLocale());
        String testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of oneTimeStatisticDTO failed for invalid instance with bundle-enddate before bundle-startdate. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        oneTimeStatisticDTO.setBundleStartDate(startDate);
        oneTimeStatisticDTO.setBundleEndDate(endDate);

        //case 3: test invalid instance with patient-enddate before patient-startdate
        oneTimeStatisticDTO.setPatientStartDate(endDate);
        oneTimeStatisticDTO.setPatientEndDate(startDate);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        oneTimeStatisticDTOValidator.validate(oneTimeStatisticDTO, result);
        assertTrue(
            "Validation of oneTimeStatisticDTO failed for invalid instance with patient-enddate before patient-startdate. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("statistic.error.enddateBeforeStartdate", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of oneTimeStatisticDTO failed for invalid instance with patient-enddate before patient-startdate. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        oneTimeStatisticDTO.setPatientStartDate(startDate);
        oneTimeStatisticDTO.setPatientEndDate(endDate);

        //case 4: test invalid instance with bundle-patient-enddate before bundle-patient-startdate
        oneTimeStatisticDTO.setBundlePatientStartDate(endDate);
        oneTimeStatisticDTO.setBundlePatientEndDate(startDate);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        oneTimeStatisticDTOValidator.validate(oneTimeStatisticDTO, result);
        assertTrue(
            "Validation of oneTimeStatisticDTO failed for invalid instance with bundle-patient-enddate before bundle-patient-startdate. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("statistic.error.enddateBeforeStartdate", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of oneTimeStatisticDTO failed for invalid instance with bundle-patient-enddate before bundle-patient-startdate. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        oneTimeStatisticDTO.setBundlePatientStartDate(startDate);
        oneTimeStatisticDTO.setBundlePatientEndDate(endDate);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        oneTimeStatisticDTO.setBundleEndDate(null);
        oneTimeStatisticDTOValidator.validate(oneTimeStatisticDTO, result);
        assertFalse(
            "Validation of oneTimeStatisticDTO failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        oneTimeStatisticDTO.setBundleEndDate(endDate);
        oneTimeStatisticDTO.setBundleStartDate(null);
        oneTimeStatisticDTOValidator.validate(oneTimeStatisticDTO, result);
        assertFalse(
            "Validation of oneTimeStatisticDTO failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        oneTimeStatisticDTO.setBundleStartDate(startDate);
        oneTimeStatisticDTO.setBundlePatientEndDate(null);
        oneTimeStatisticDTOValidator.validate(oneTimeStatisticDTO, result);
        assertFalse(
            "Validation of oneTimeStatisticDTO failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        oneTimeStatisticDTO.setBundlePatientEndDate(endDate);
        oneTimeStatisticDTO.setBundlePatientStartDate(null);
        oneTimeStatisticDTOValidator.validate(oneTimeStatisticDTO, result);
        assertFalse(
            "Validation of oneTimeStatisticDTO failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        oneTimeStatisticDTO.setBundlePatientStartDate(startDate);
        oneTimeStatisticDTO.setPatientEndDate(null);
        oneTimeStatisticDTOValidator.validate(oneTimeStatisticDTO, result);
        assertFalse(
            "Validation of oneTimeStatisticDTO failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        oneTimeStatisticDTO.setPatientEndDate(endDate);
        oneTimeStatisticDTO.setPatientStartDate(null);
        oneTimeStatisticDTOValidator.validate(oneTimeStatisticDTO, result);
        assertFalse(
            "Validation of oneTimeStatisticDTO failed. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());
    }
}
