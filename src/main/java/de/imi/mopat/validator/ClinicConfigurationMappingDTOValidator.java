package de.imi.mopat.validator;

import de.imi.mopat.dao.ClinicConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.model.ClinicConfiguration;
import de.imi.mopat.model.dto.ClinicConfigurationMappingDTO;
import de.imi.mopat.model.dto.ConfigurationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.io.File;

/**
 * The validator for {@link ClinicConfigurationMappingDTO} objects. The purpose for this validator is to check the data posted from
 * form.
 */
@Component
public class ClinicConfigurationMappingDTOValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ClinicConfigurationDao clinicConfigurationDao;

    @Override
    public boolean supports(final Class<?> type) {
        return ClinicConfigurationMappingDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        validator.validate(o, errors);

        ClinicConfigurationMappingDTO clinicConfigurationMappingDTO = (ClinicConfigurationMappingDTO) o;

        String message = null;
        switch (clinicConfigurationMappingDTO.getConfigurationType()) {
            case BOOLEAN:
                String value = clinicConfigurationMappingDTO.getValue();

                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    message = messageSource.getMessage("configuration.validate" + ".boolean",
                        new Object[]{}, LocaleContextHolder.getLocale());
                }
                if(value.equalsIgnoreCase("true")){
                    ClinicConfiguration clinicConfiguration = clinicConfigurationDao.getElementById(
                        clinicConfigurationMappingDTO.getClinicConfigurationId());
                    if(clinicConfiguration.getMappedConfigurationGroup() != null && clinicConfigurationMappingDTO.getMappedConfigurationGroup()==null){
                        message = messageSource.getMessage("configuration.validate" + ".mappedConfigurationNotFound",
                            new Object[]{}, LocaleContextHolder.getLocale());
                    }
                }
                break;
            case DOUBLE:
                try {
                    Double.valueOf(clinicConfigurationMappingDTO.getValue());
                } catch (NumberFormatException e) {
                    message = messageSource.getMessage("configuration.validate" + ".double",
                        new Object[]{}, LocaleContextHolder.getLocale());
                }
                break;
            case IMAGE:
                break;
            case INTEGER:
                try {
                    Integer.valueOf(clinicConfigurationMappingDTO.getValue());
                } catch (NumberFormatException e) {
                    message = messageSource.getMessage("configuration.validate" + ".integer",
                        new Object[]{}, LocaleContextHolder.getLocale());
                }
                break;
            case LONG:
                try {
                    Long.valueOf(clinicConfigurationMappingDTO.getValue());
                } catch (NumberFormatException e) {
                    message = messageSource.getMessage("configuration.validate" + ".long",
                        new Object[]{}, LocaleContextHolder.getLocale());
                }
                break;
            case LOCAL_PATH:
                File dir = new File(clinicConfigurationMappingDTO.getValue());
                if ((!dir.canRead() || !dir.canWrite()) || clinicConfigurationMappingDTO.getValue().isEmpty()) {
                    message = messageSource.getMessage("configuration.validate" + ".localPath",
                        new Object[]{}, LocaleContextHolder.getLocale());
                }
                break;
            case PASSWORD:
                break;
            case PATTERN:
                String pattern = clinicConfigurationMappingDTO.getPattern();
                if (!clinicConfigurationMappingDTO.getValue().matches(pattern)) {
                    message = messageSource.getMessage("configuration.validate" + ".pattern",
                        new Object[]{}, LocaleContextHolder.getLocale());
                }
                break;
            case STRING:
                break;
            case WEB_PATH:
                break;
            default:
                break;
        }

        if (message != null) {
            message = message.replace("{field}",
                "'" + messageSource.getMessage(clinicConfigurationMappingDTO.getLabelMessageCode(),
                    new Object[]{}, LocaleContextHolder.getLocale()) + "'");
            errors.rejectValue("value", "errormessage", message);
        }
    }
}