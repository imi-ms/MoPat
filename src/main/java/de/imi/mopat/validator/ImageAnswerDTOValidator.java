package de.imi.mopat.validator;

import de.imi.mopat.model.dto.AnswerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * The validator for {@link AnswerDTO AnswerDTO} objects for the Questiontype IMAGE.
 */
@Component
public class ImageAnswerDTOValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(final Class<?> type) {
        return AnswerDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        // [bt] first, let the standard validator validate the target object
        // with respect to it's JSR-303 constraints (javax.validation
        // .constraints annotations)
        validator.validate(target, errors);

        AnswerDTO answerDTO = (AnswerDTO) target;
        // If the answerDTO has no image path yet that means the image
        // question was new created, check if the given file is not empty
        if (answerDTO.getImagePath() == null) {
            if (answerDTO.getImageFile() == null || answerDTO.getImageFile().isEmpty()) {
                errors.rejectValue("imageFile", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("imageAnswer.validator" + ".noFilePath",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }
        // If a new file was uploaded check if it is not empty, not too big
        // and the type is supported
        if (answerDTO.getImageFile().getSize() != 0) {
            if (answerDTO.getImageFile().isEmpty()) {
                errors.rejectValue("imageFile", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("imageAnswer.validator" + ".noFilePath",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            } else if (!answerDTO.getImageFile().getContentType().equals("image/jpeg")
                && !answerDTO.getImageFile().getContentType().equals("image/jpg")
                && !answerDTO.getImageFile().getContentType().equals("image/png")) {
                errors.rejectValue("imageFile", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("imageAnswer.validator" + ".noImageFile",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            } else if (answerDTO.getImageFile().getSize() > 2097152) {
                errors.rejectValue("imageFile", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                    messageSource.getMessage("imageAnswer.validator" + ".fileTooBig",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }
    }
}
