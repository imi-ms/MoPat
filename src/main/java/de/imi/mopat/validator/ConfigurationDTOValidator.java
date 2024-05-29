package de.imi.mopat.validator;

import de.imi.mopat.model.dto.ConfigurationDTO;
import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * The validator for {@link ConfigurationDTO} objects. The purpose for this validator is to check
 * the data posted from the edit.jsp form.
 */
@Component
public class ConfigurationDTOValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;

    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(final Class<?> type) {
        return ConfigurationDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        validator.validate(o, errors);

        ConfigurationDTO configurationDTO = (ConfigurationDTO) o;

        String message = null;
        switch (configurationDTO.getConfigurationType()) {
            case BOOLEAN:
                String value = configurationDTO.getValue();

                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    message = messageSource.getMessage("configuration.validate" + ".boolean",
                        new Object[]{}, LocaleContextHolder.getLocale());
                }
                break;
            case DOUBLE:
                try {
                    Double.valueOf(configurationDTO.getValue());
                } catch (NumberFormatException e) {
                    message = messageSource.getMessage("configuration.validate" + ".double",
                        new Object[]{}, LocaleContextHolder.getLocale());
                }
                break;
            case IMAGE:
                break;
            case INTEGER:
                try {
                    Integer.valueOf(configurationDTO.getValue());
                } catch (NumberFormatException e) {
                    message = messageSource.getMessage("configuration.validate" + ".integer",
                        new Object[]{}, LocaleContextHolder.getLocale());
                }
                break;
            case LONG:
                try {
                    Long.valueOf(configurationDTO.getValue());
                } catch (NumberFormatException e) {
                    message = messageSource.getMessage("configuration.validate" + ".long",
                        new Object[]{}, LocaleContextHolder.getLocale());
                }
                break;
            case LOCAL_PATH:
                File dir = new File(configurationDTO.getValue());
                if ((!dir.canRead() || !dir.canWrite()) || configurationDTO.getValue().isEmpty()) {
                    message = messageSource.getMessage("configuration.validate" + ".localPath",
                        new Object[]{}, LocaleContextHolder.getLocale());
                }
                break;
            case PASSWORD:
                break;
            case PATTERN:
                String pattern = configurationDTO.getPattern();
                if (!configurationDTO.getValue().matches(pattern)) {
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
                "'" + messageSource.getMessage(configurationDTO.getLabelMessageCode(),
                    new Object[]{}, LocaleContextHolder.getLocale()) + "'");
            errors.rejectValue("value", "errormessage", message);
        }
    }
}