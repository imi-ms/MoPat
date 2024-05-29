package de.imi.mopat.validator;

import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.dto.ExportRuleFormatDTO;
import de.imi.mopat.model.dto.ExportRulesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * The validator for {@link ExportRulesDTO ExportRulesDTO} objects. The purpose for this validator
 * is to check the data posted from the mapping/map.jsp form.
 */
@Component
public class ExportRulesDTOValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;

    @Override
    public boolean supports(final Class<?> type) {
        return ExportRulesDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        // standard validation
        validator.validate(target, errors);

        // validate the export rules dto
        ExportRulesDTO exportRulesDTO = (ExportRulesDTO) target;

        if (exportRulesDTO.getExportRuleFormats() != null) {
            for (Long formatId : exportRulesDTO.getExportRuleFormats().keySet()) {
                ExportRuleFormatDTO formatDTO = exportRulesDTO.getExportRuleFormats().get(formatId);
                // check if decimal places has the correct number format
                if (formatDTO.getDecimalPlaces() != null) {
                    if (!formatDTO.getDecimalPlaces().matches(Constants.NUMBER_FORMAT)) {
                        errors.rejectValue("exportRuleFormats[" + formatId + "].decimalPlaces",
                            "mapping.error.decimalPlacesWrongFormat");
                    }
                }
            }
        }
    }
}