package de.imi.mopat.validator;

import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.model.dto.ClinicConfigurationMappingDTO;
import de.imi.mopat.model.dto.ClinicDTO;

import de.imi.mopat.model.enumeration.ClinicConfigurationsPatientRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The validator for {@link ClinicDTO ClinicDTO} objects.
 */
@Component
public class ClinicDTOValidator implements Validator {

    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ClinicDao clinicDao;
    @Autowired
    private ClinicConfigurationMappingDTOValidator clinicConfigurationMappingDTOValidator;

    @Override
    public boolean supports(final Class<?> type) {
        return ClinicDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        ClinicDTO clinicDTO = (ClinicDTO) o;

        if (!clinicDTO.getName().isEmpty() && clinicDTO.getName().trim().isEmpty()) {
            clinicDTO.setName("");
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("clinic.error.nameIsEmpty", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        if (!clinicDTO.getName().matches("^[\\p{L}0-9\\s\\-_.:()\\[\\]!+?]+$")) {
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("clinic.error.nameContainsSpecialCharacters",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }

        if (clinicDTO.getName().matches("^[\\p{L}0-9\\s\\-_.:()\\[\\]!+?]+$")
            && !clinicDao.isClinicNameUnused(clinicDTO.getName(), clinicDTO.getId())) {
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("clinic.error.nameInUse", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }
        if(!checkIfAnyOnePatientRetrieverIsEnabled(clinicDTO)){
            errors.rejectValue("clinicConfigurationMappingDTOS[0].value", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("clinic.error.noConfiguration",
                    new Object[]{}, LocaleContextHolder.getLocale()));
        }
        int indexOfConfiguration = 0;
        for (ClinicConfigurationMappingDTO clinicConfigurationMappingDTO : clinicDTO.getClinicConfigurationMappingDTOS()) {
            if (clinicConfigurationMappingDTO.getParent() == null) {
                errors.pushNestedPath("clinicConfigurationMappingDTOS["
                    + indexOfConfiguration + "]");
                clinicConfigurationMappingDTOValidator.validate(clinicConfigurationMappingDTO, errors);
                errors.popNestedPath();
                if (clinicConfigurationMappingDTO.getChildren() != null
                    && clinicConfigurationMappingDTO.getValue().equalsIgnoreCase("true")) {
                    int indexOfChild = 0;
                    for (ClinicConfigurationMappingDTO childDTO : clinicConfigurationMappingDTO.getChildren()) {
                        errors.pushNestedPath("clinicConfigurationMappingDTOS[" + indexOfConfiguration + "]"
                            + ".children[" + indexOfChild + "]");
                        clinicConfigurationMappingDTOValidator.validate(childDTO, errors);
                        errors.popNestedPath();
                        indexOfChild++;
                    }
                }
                indexOfConfiguration++;
            }
        }
    }

    private static boolean checkIfAnyOnePatientRetrieverIsEnabled(ClinicDTO clinicDTO) {
        boolean check = false;
        for (ClinicConfigurationMappingDTO clinicConfigurationMappingDTO : clinicDTO.getClinicConfigurationMappingDTOS()) {
            if (clinicConfigurationMappingDTO.getAttribute()
                .equals(ClinicConfigurationsPatientRetriever.usePseudonymizationService.getTextValue())) {
                check = clinicConfigurationMappingDTO.getValue().equals("true");
            }
            else if (clinicConfigurationMappingDTO.getAttribute()
                .equals(ClinicConfigurationsPatientRetriever.usePatientDataLookup.getTextValue())) {
                check = clinicConfigurationMappingDTO.getValue().equals("true");
            }
            else if (clinicConfigurationMappingDTO.getAttribute()
                .equals(ClinicConfigurationsPatientRetriever.registerPatientData.getTextValue())) {
                check = clinicConfigurationMappingDTO.getValue().equals("true");
            }
            if(check){
                break;
            }
        }
        return check;
    }
}