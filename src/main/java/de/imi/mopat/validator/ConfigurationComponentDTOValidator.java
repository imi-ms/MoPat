package de.imi.mopat.validator;

import de.imi.mopat.model.dto.ConfigurationComponentDTO;
import de.imi.mopat.model.dto.ConfigurationDTO;
import de.imi.mopat.model.dto.ConfigurationGroupDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * The validator for {@link ConfigurationComponentDTO} objects. The purpose for this validator is to
 * check the data posted from the edit.jsp form.
 */
@Component
public class ConfigurationComponentDTOValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ConfigurationDTOValidator configurationDTOValidator;

    @Override
    public boolean supports(final Class<?> type) {
        return ConfigurationComponentDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        // standard validation
        validator.validate(o, errors);

        ConfigurationComponentDTO configurationComponentDTO = (ConfigurationComponentDTO) o;
        List<String> nameValidationList;

        for (String key : configurationComponentDTO.getConfigurationGroupDTOs().keySet()) {
            //List to validate names
            nameValidationList = new ArrayList<>();
            for (ConfigurationGroupDTO configurationGroupDTO : configurationComponentDTO.getConfigurationGroupDTOs()
                .get(key)) {
                if (configurationGroupDTO.getId() != null
                    || configurationGroupDTO.getReferringId() != null) {
                    if (!configurationComponentDTO.getConfigurationsToDelete()
                        .contains(configurationGroupDTO.getId())) {
                        int indexOfConfigurationGroup = configurationComponentDTO.getConfigurationGroupDTOs()
                            .get(key).indexOf(configurationGroupDTO);
                        //Validate configuration groups' name by comparing it
                        // with the other ones' names
                        String nameMessage = null;

                        if (nameValidationList.contains(configurationGroupDTO.getName())) {
                            //Name exists multiple times
                            nameMessage = messageSource.getMessage(
                                "configuration" + ".validate.multipleName", new Object[]{},
                                LocaleContextHolder.getLocale());
                            errors.rejectValue(
                                "configurationGroupDTOs[" + key + "][" + indexOfConfigurationGroup
                                    + "].name", "errormessage", nameMessage);
                        }
                        if (configurationGroupDTO.getName() == null
                            || configurationGroupDTO.getName().equalsIgnoreCase("")) {
                            //Configuration group has got an empty or no name
                            nameMessage = messageSource.getMessage(
                                "configuration" + ".validate.noName", new Object[]{},
                                LocaleContextHolder.getLocale());
                            errors.rejectValue(
                                "configurationGroupDTOs[" + key + "][" + indexOfConfigurationGroup
                                    + "].name", "errormessage", nameMessage);
                        } else {
                            //There's no other group that has got the same
                            // name yet
                            nameValidationList.add(configurationGroupDTO.getName());
                        }

                        //validate the configurationDTOs adhering to
                        // configurationGroupDTO
                        int indexOfConfiguration = 0;
                        for (ConfigurationDTO configurationDTO : configurationGroupDTO.getConfigurationDTOs()) {
                            if (configurationDTO.getParent() == null) {
                                errors.pushNestedPath("configurationGroupDTOs[" + key + "]["
                                    + indexOfConfigurationGroup + "].configurationDTOs["
                                    + indexOfConfiguration + "]");
                                configurationDTOValidator.validate(configurationDTO, errors);
                                errors.popNestedPath();
                                if (configurationDTO.getChildren() != null
                                    && configurationDTO.getValue().equalsIgnoreCase("true")) {
                                    int indexOfChild = 0;
                                    for (ConfigurationDTO childDTO : configurationDTO.getChildren()) {
                                        errors.pushNestedPath("configurationGroupDTOs[" + key + "]["
                                            + indexOfConfigurationGroup + "]"
                                            + ".configurationDTOs[" + indexOfConfiguration + "]"
                                            + ".children[" + indexOfChild + "]");
                                        configurationDTOValidator.validate(childDTO, errors);
                                        errors.popNestedPath();
                                        indexOfChild++;
                                    }
                                }
                                indexOfConfiguration++;
                            }
                        }
                    }
                }
            }
        }
    }
}
