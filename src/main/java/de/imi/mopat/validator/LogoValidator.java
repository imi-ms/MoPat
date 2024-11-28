package de.imi.mopat.validator;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * Validator for logo file uploads.
 */
@Component
public class LogoValidator {

    private static final List<String> SUPPORTED_IMAGE_TYPES = List.of("png", "jpg", "jpeg");

    @Autowired
    private MessageSource messageSource;

    /**
     * Validates the provided logo file.
     *
     * @param logo   The {@link MultipartFile} containing the logo.
     * @param result The {@link BindingResult} to record validation errors.
     */
    public void validateLogo(MultipartFile logo, BindingResult result) {
        if (logo != null && !logo.isEmpty()) {
            String logoExtension = getLogoExtension(logo);
            if (!isSupportedImageType(logoExtension)) {
                rejectUnsupportedImageType(result);
            }
        }
    }

    /**
     * Retrieves the file extension of the logo.
     *
     * @param logo The {@link MultipartFile} containing the logo.
     * @return The file extension as a string.
     */
    private String getLogoExtension(MultipartFile logo) {
        return Objects.requireNonNull(FilenameUtils.getExtension(logo.getOriginalFilename())).toLowerCase();
    }

    /**
     * Checks if the provided file extension is a supported image type.
     *
     * @param extension The file extension as a string.
     * @return true if the extension is one of supported types, false otherwise.
     */
    private boolean isSupportedImageType(String extension) {
        return SUPPORTED_IMAGE_TYPES.contains(extension);
    }

    /**
     * Records a validation error for an unsupported image type.
     *
     * @param result The {@link BindingResult} to record validation errors.
     */
    private void rejectUnsupportedImageType(BindingResult result) {
        result.rejectValue("logo", "error.wrongImageType",
                messageSource.getMessage("bundle.error.wrongImageType", new Object[]{},
                        LocaleContextHolder.getLocale()));
    }
}