package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.dto.ConfigurationDTO;
import de.imi.mopat.model.enumeration.ConfigurationType;
import de.imi.mopat.utils.Helper;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
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
public class ConfigurationDTOValidatorTest {

    private static final Random random = new Random();
    @Autowired
    private ConfigurationDTOValidator configurationDTOValidator;
    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    /**
     * Test of {@link ConfigurationDTOValidator#supports(java.lang.Class)} Valid input:
     * {@link ConfigurationDTO#class} Invalid input: Other class than
     * {@link ConfigurationDTO#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for ConfigurationDTO.class failed. ConfigurationDTOValidator didn't support ConfigurationDTO.class except it was expected to do.",
            configurationDTOValidator.supports(ConfigurationDTO.class));
        assertFalse(
            "Supports method for random class failed. ConfigurationDTOValidator supported that class except it wasn't expected to do.",
            configurationDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link ConfigurationDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}.
     * <br>
     * Valid input: Instance of {@link ConfigurationDTO} whose value fits to the
     * {@link ConfigurationType} and instance of {@link Errors}. <br> Invalid input: Instance of
     * {@link ConfigurationDTO} whose value doesn't fit to its {@link ConfigurationType}.<br>
     * Configuration of type BOOLEAN is invalid if the value differs from 'true' or 'false'.<br>
     * Configuration of type DOUBLE is invalid if the value not matches the pattern
     * [0-9]*.[0-9]*.<br> Configuration of type INTEGER or LONG is invalid if the value doesn't
     * match the pattern [0-9]*.<br> Configuration of type LOCAL_PATH is invalid if the system can't
     * find the path represented by the value.<br> Configuration of type PATTERN is invalid if the
     * value doesn't match the regular expression.<br>
     */
    @Test
    public void testValidate() {
        ConfigurationDTO configurationDTO = new ConfigurationDTO();
        configurationDTO.setLabelMessageCode("configuration.label.defaultLanguage");

        BindingResult result;
        String message, testErrorMessage, errorMessageCodeType, value;
        for (ConfigurationType configurationType : ConfigurationType.values()) {

            value = "";
            errorMessageCodeType = null;
            result = new MapBindingResult(new HashMap<>(),
                Helper.getRandomAlphabeticString(random.nextInt(13)));
            switch (configurationType) {
                case BOOLEAN:
                    errorMessageCodeType = "boolean";
                    value = ((Boolean) random.nextBoolean()).toString();
                    break;
                case DOUBLE:
                    errorMessageCodeType = "double";
                    value = ((Double) random.nextDouble()).toString();
                    break;
                case INTEGER:
                    errorMessageCodeType = "integer";
                    value = ((Integer) random.nextInt()).toString();
                    break;
                case LONG:
                    errorMessageCodeType = "long";
                    value = ((Long) random.nextLong()).toString();
                    break;
                case LOCAL_PATH:
                    errorMessageCodeType = "localPath";
                    value = System.getProperty("user.dir");
                    break;
                case PATTERN:
                    value = UUID.randomUUID().toString();
                    configurationDTO.setPattern(value);
                    errorMessageCodeType = "pattern";
                    break;
                default:
                    break;
            }

            configurationDTO.setConfigurationType(configurationType);
            configurationDTO.setValue(value);
            configurationDTOValidator.validate(configurationDTO, result);
            assertFalse("Validation of configurationDTO failed for '" + configurationType
                    + "'. The result caught errors except it wasn't expected to do",
                result.hasErrors());

            configurationDTO.setValue(UUID.randomUUID().toString());
            configurationDTOValidator.validate(configurationDTO, result);

            if (errorMessageCodeType != null) {
                assertTrue("Validation of configurationDTO failed '" + configurationType
                        + "'. The result hasn't caught any errors except it was expected to do.",
                    result.hasErrors());
                message = messageSource.getMessage("configuration.validate." + errorMessageCodeType,
                    new Object[]{}, LocaleContextHolder.getLocale());
                message = message.replace("{field}",
                    "'" + messageSource.getMessage(configurationDTO.getLabelMessageCode(),
                        new Object[]{}, LocaleContextHolder.getLocale()) + "'");
                testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
                assertEquals("Validation of configurationDTO failed '" + configurationType
                        + "'. The returned error message didn't match the expected one.", message,
                    testErrorMessage);
            }
        }

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        configurationDTO = new ConfigurationDTO();
        configurationDTO.setLabelMessageCode("configuration.label.defaultLanguage");
        configurationDTO.setConfigurationType(ConfigurationType.LOCAL_PATH);
        configurationDTO.setValue("");
        configurationDTOValidator.validate(configurationDTO, result);
        assertTrue(
            "Validation of configurationDTO failed for localPath. The result hasn't caught any errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("configuration.validate.localPath", new Object[]{},
            LocaleContextHolder.getLocale());
        message = message.replace("{field}",
            "'" + messageSource.getMessage(configurationDTO.getLabelMessageCode(), new Object[]{},
                LocaleContextHolder.getLocale()) + "'");
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of configurationDTO failed for localPath. The returned error message didn't match the expected one.",
            message, testErrorMessage);
    }
}
