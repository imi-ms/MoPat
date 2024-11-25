package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.helper.model.ClinicDTOMapper;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.ClinicConfiguration;
import de.imi.mopat.model.ClinicConfigurationMapping;
import de.imi.mopat.model.ClinicConfigurationMappingTest;
import de.imi.mopat.model.ClinicConfigurationTest;
import de.imi.mopat.model.ClinicTest;
import de.imi.mopat.model.dto.ClinicDTO;
import de.imi.mopat.model.enumeration.ClinicConfigurationsPatientRetriever;
import de.imi.mopat.model.enumeration.ConfigurationType;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class ClinicDTOValidatorTest {

    private static final Random random = new Random();

    @Autowired
    private ClinicDTOValidator clinicDTOValidator;
    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;
    @Autowired
    private ClinicDao clinicDao;
    @Autowired
    private ClinicDTOMapper clinicDTOMapper;

    /**
     * Test of {@link ClinicDTOValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link ClinicDTO#class}<br> Invalid input: Other class than {@link ClinicDTO#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for ClinicDTO.class failed. ClinicDTOValidator didn't support ClinicDTO.class except it was expected to do.",
            clinicDTOValidator.supports(ClinicDTO.class));
        assertFalse(
            "Supports method for random class failed. ClinicDTOValidator supported that class except it wasn't expected to do.",
            clinicDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link ClinicDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}.<br> Valid input: Instance of {@link ClinicDTO} and
     * instance of {@link Errors}.<br> Invalid input: Instance of {@link ClinicDTO} which name has
     * got less than three characters, more than 255 or is already in use.
     */
    @Test
    public void testValidate() {
        Clinic clinic = ClinicTest.getNewValidClinic();
        ClinicConfigurationMapping clinicConfigurationMapping2 = spy(ClinicConfigurationMappingTest.getNewValidConfiguration());
        ClinicConfiguration clinicConfiguration = ClinicConfigurationTest.getNewValidConfiguration();
        clinicConfiguration.setConfigurationType(ConfigurationType.STRING);
        clinicConfiguration.setAttribute(ClinicConfigurationsPatientRetriever.usePatientDataLookup.getTextValue());
        clinicConfigurationMapping2.setValue("true");
        clinicConfigurationMapping2.setClinicConfiguration(clinicConfiguration);
        clinicConfigurationMapping2.setClinic(clinic);
        List<ClinicConfigurationMapping> clinicConfigurationMappings = new ArrayList<>();
        clinicConfigurationMappings.add(clinicConfigurationMapping2);
        clinic.setClinicConfigurationMappings(clinicConfigurationMappings);
        ClinicDTO clinicDTO = clinicDTOMapper.apply(clinic);
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        clinicDTOValidator.validate(clinicDTO, result);
        assertFalse(
            "The validation of clinicDTO failed. The result caught errors except it wasn't expected to do.",
            result.hasErrors());

        //Set up clinic with name, to test the validate method
        Clinic clinic1 = ClinicTest.getNewValidClinic();
        String name = Helper.getRandomAlphabeticString(random.nextInt(13) + 3);
        clinicDTO.setName(name);
        clinic1.setName(name);
        clinicDao.merge(clinic1);
        clinicDTOValidator.validate(clinicDTO, result);
        assertTrue(
            "The validation of clinicDTO name failed. The result didn't catch errors excepted it was expected to do.",
            result.hasErrors());

        String message = messageSource.getMessage("clinic.error.nameInUse", new Object[]{},
            LocaleContextHolder.getLocale());
        String testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "The validation of the clinicDTO name failed. The returned error message didn't match the expected one.",
            message, testErrorMessage);
    }
}
