package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.ConfigurationGroupTest;
import de.imi.mopat.model.dto.ConfigurationComponentDTO;
import de.imi.mopat.model.dto.ConfigurationDTO;
import de.imi.mopat.model.dto.ConfigurationGroupDTO;
import de.imi.mopat.model.enumeration.ConfigurationType;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
public class ConfigurationComponentDTOValidatorTest {

    private static final Random random = new Random();

    @Autowired
    private ConfigurationComponentDTOValidator configurationComponentDTOValidator;
    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    /**
     * Test of {@link ConfigurationComponentDTOValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link ConfigurationComponentDTO#class}<br> Invalid input: Other class than
     * {@link ConfigurationComponentDTO#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for ConfigurationComponentDTO.class failed. ConfigurationComponentDTOValidator didn't support ConfigurationComponentDTO.class except it was expected to do.",
            configurationComponentDTOValidator.supports(ConfigurationComponentDTO.class));
        assertFalse(
            "Supports method for random class failed. ConfigurationComponentDTOValidator supported that class except it wasn't expected to do.",
            configurationComponentDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link ConfigurationComponentDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}.<br> Valid input: Instance of
     * {@link ConfigurationComponentDTO} containing instances of {@link ConfigurationGroupDTO} with
     * unique and required names and instance of {@link Errors}.<br> Invalid input: Instance of
     * [@link ConfigurationCompentDTO} containing multiple instances of
     * {@link ConfigurationGroupDTO} owning the same name or any instance without name.
     */
    @Test
    public void testValidate() {
        Map<String, List<ConfigurationGroupDTO>> configurationGroupDTOs = new LinkedHashMap<>();
        Integer size = random.nextInt(11) + 3;
        Integer position = random.nextInt(
            size);    //Random position to get at least one duplicate configurationGroupDTO

        List<ConfigurationGroupDTO> configurationGroupDTOList;

        //Notice one configurationGroupDTO to duplicate name to check if validation of name is correct.
        ConfigurationGroup configurationGroupDuplicated = ConfigurationGroupTest.getNewValidConfigurationGroup();
        configurationGroupDuplicated.setName(
            Helper.getRandomAlphabeticString(random.nextInt(13) + 5));
        ConfigurationGroupDTO configurationGroupDTODuplicatedName = Mockito.spy(
            configurationGroupDuplicated.toConfigurationGroupDTO());
        Mockito.when(configurationGroupDTODuplicatedName.getId())
            .thenReturn(Math.abs(random.nextLong()) + 1);
        String fakeName = "";

        for (int i = 0; i < size; i++) {
            configurationGroupDTOList = new ArrayList<>();
            String key = Helper.getRandomAlphabeticString(random.nextInt(13) + 5);

            ConfigurationGroup configurationGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();
            configurationGroup.setName(Helper.getRandomAlphabeticString(random.nextInt(13) + 5));
            configurationGroup.setLabelMessageCode(key);
            ConfigurationGroupDTO configurationGroupDTO = Mockito.spy(
                configurationGroup.toConfigurationGroupDTO());
            Mockito.when(configurationGroupDTO.getId()).thenReturn(Math.abs(random.nextLong()) + 1);
            configurationGroupDTOList.add(configurationGroupDTO);

            //Set the duplicated name configurationGroup
            if (i == position) {
                configurationGroupDTODuplicatedName.setReferringId(configurationGroupDTO.getId());
                configurationGroupDTODuplicatedName.setLabelMessageCode(key);
                configurationGroupDTOList.add(configurationGroupDTODuplicatedName);
                fakeName = configurationGroupDTO.getName();
            }

            //Add configurationGroupDTOs with the same LabelMessageCode to the Map
            if (random.nextBoolean()) {
                for (int j = 0; j < random.nextInt(2); j++) {
                    ConfigurationGroup configurationGroup2 = ConfigurationGroupTest.getNewValidConfigurationGroup();
                    configurationGroup2.setName(
                        Helper.getRandomAlphabeticString(random.nextInt(13) + 5));
                    configurationGroup2.setLabelMessageCode(key);
                    ConfigurationGroupDTO configurationGroupDTO2 = Mockito.spy(
                        configurationGroup2.toConfigurationGroupDTO());
                    Mockito.when(configurationGroupDTO2.getId())
                        .thenReturn(Math.abs(random.nextLong()) + 1);
                    configurationGroupDTO2.setReferringId(configurationGroupDTO.getId());
                    configurationGroupDTO2.setLabelMessageCode(key);
                    configurationGroupDTOList.add(configurationGroupDTO2);
                }
            }

            configurationGroupDTOs.put(key, configurationGroupDTOList);
        }

        ConfigurationComponentDTO configurationComponentDTO = new ConfigurationComponentDTO(
            configurationGroupDTOs);

        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        configurationComponentDTOValidator.validate(configurationComponentDTO, result);
        assertFalse(
            "The validation of configurationComponent failed. The resuled caught errors although it wasn't expected to do.",
            result.hasErrors());

        configurationGroupDTODuplicatedName.setName(fakeName);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        configurationComponentDTOValidator.validate(configurationComponentDTO, result);
        assertTrue(
            "The validation of configurationComponent failed. The resuled hasn't caught erros although it was expected to do.",
            result.hasErrors());
        String message = messageSource.getMessage("configuration.validate.multipleName",
            new Object[]{}, LocaleContextHolder.getLocale());
        String testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "The validation of configurationComponent failed. The result's error message didn't match 'configuration.validate.multipleName'.",
            message, testErrorMessage);

        configurationGroupDTODuplicatedName.setName(null);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        configurationComponentDTOValidator.validate(configurationComponentDTO, result);
        assertTrue(
            "The validation of configurationComponent failed. The resuled hasn't caught erros although it was expected to do.",
            result.hasErrors());
        message = messageSource.getMessage("configuration.validate.noName", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "The validation of configurationComponent failed. The result's error message didn't match 'configuration.validate.noName'.",
            message, testErrorMessage);

        configurationGroupDTODuplicatedName.setName("");
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        configurationComponentDTOValidator.validate(configurationComponentDTO, result);
        assertTrue(
            "The validation of configurationComponent failed. The resuled hasn't caught erros although it was expected to do.",
            result.hasErrors());
        message = messageSource.getMessage("configuration.validate.noName", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "The validation of configurationComponent failed. The result's error message didn't match 'configuration.validate.noName'.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        configurationGroupDTOs.clear();
        for (int i = 0; i < random.nextInt(11) + 4; i++) {
            configurationGroupDTOList = new ArrayList<>();
            ConfigurationGroupDTO configurationGroup = new ConfigurationGroupDTO();

            switch (i) {
                case 0:
                    configurationGroup.setReferringId(Math.abs(random.nextLong()));
                    break;
                case 1:
                    break;
                case 2:
                    Long id = Math.abs(random.nextLong());
                    configurationComponentDTO.getConfigurationsToDelete().add(id);
                    configurationGroup.setId(id);
                default:
                    configurationGroup.setId(Math.abs(random.nextLong()));
                    break;
            }
            configurationGroup.setName(Helper.getRandomAlphabeticString(random.nextInt(13) + 5));

            configurationGroup.setLabelMessageCode(
                Helper.getRandomAlphabeticString(random.nextInt(13) + 5));

            //create some valid configurationDTOs to push the cobertura line coverage
            List<ConfigurationDTO> configurationDTOs = new ArrayList<>();
            for (int j = 0; j < random.nextInt(7) + 4; j++) {
                ConfigurationDTO configurationDTO = new ConfigurationDTO();
                configurationDTO.setConfigurationType(ConfigurationType.BOOLEAN);
                List<ConfigurationDTO> children = new ArrayList<>();
                switch (j) {
                    case 0:
                        configurationDTO.setConfigurationType(ConfigurationType.BOOLEAN);
                        configurationDTO.setValue("true");
                        for (int k = 0; k < random.nextInt(7) + 2; k++) {
                            ConfigurationDTO child = new ConfigurationDTO();
                            child.setParent(configurationDTO);
                            child.setConfigurationType(ConfigurationType.IMAGE);
                            children.add(child);
                        }
                        configurationDTO.setChildren(children);
                        break;
                    case 1:
                        for (int k = 0; k < random.nextInt(7) + 2; k++) {
                            ConfigurationDTO child = new ConfigurationDTO();
                            child.setParent(configurationDTO);
                            child.setConfigurationType(ConfigurationType.IMAGE);
                            children.add(child);
                        }
                        configurationDTO.setChildren(children);
                        configurationDTO.setValue("false");
                        break;
                    case 2:
                        configurationDTO.setValue("true");
                        configurationDTO.setChildren(null);
                        break;
                    case 3:
                        configurationDTO.setParent(new ConfigurationDTO());
                    default:
                        configurationDTO.setConfigurationType(ConfigurationType.STRING);
                }
                configurationDTOs.add(configurationDTO);
            }

            configurationGroup.setConfigurationDTOs(configurationDTOs);
            configurationGroupDTOList.add(configurationGroup);
            configurationGroupDTOs.put(Helper.getRandomAlphabeticString(random.nextInt(11) + 1),
                configurationGroupDTOList);
        }
        configurationComponentDTO.setConfigurationGroupDTOs(configurationGroupDTOs);
        configurationComponentDTOValidator.validate(configurationComponentDTO, result);
        assertFalse(
            "Validation of configurationComponentDTO failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());

        ConfigurationGroupDTO configurationGroupDTO = new ConfigurationGroupDTO();
        configurationGroupDTO = Mockito.spy(configurationGroupDTO);
        configurationGroupDTOList = new ArrayList<>();
        configurationGroupDTOList.add(configurationGroupDTO);
        configurationGroupDTO.setName(Helper.getRandomAlphabeticString(random.nextInt(55) + 3));
        Mockito.when(configurationGroupDTO.getId()).thenReturn(Math.abs(random.nextLong()));
        configurationComponentDTO.getConfigurationGroupDTOs()
            .put(configurationGroupDTO.getName(), configurationGroupDTOList);
        configurationComponentDTO.getConfigurationsToDelete().add(configurationGroupDTO.getId());
        configurationComponentDTOValidator.validate(configurationComponentDTO, result);
        assertFalse(
            "Validation of configurationComponentDTO failed for valid instance. The result has caught errors although it wasn't expected to do.",
            result.hasErrors());
    }
}
