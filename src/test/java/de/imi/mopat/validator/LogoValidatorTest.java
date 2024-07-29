package de.imi.mopat.validator;

import de.imi.mopat.utils.MultipartFileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LogoValidatorTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private LogoValidator logoValidator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(messageSource.getMessage(eq("bundle.error.wrongImageType"), any(), any(Locale.class)))
                .thenReturn("Invalid image type");
    }

    @Test
    public void testValidateLogo_ValidImage() {
        // Arrange
        MultipartFile validLogo = MultipartFileUtils.getValidLogoFile();
        BindingResult result = new MapBindingResult(new HashMap<>(), MultipartFileUtils.LOGO_FIELD_NAME);

        // Act
        logoValidator.validateLogo(validLogo, result);

        // Assert
        assertFalse("No validation errors should be present for a valid image", result.hasErrors());
    }

    @Test
    public void testValidateLogo_InvalidImageType() {
        // Arrange
        MultipartFile invalidLogo = MultipartFileUtils.getInvalidLogoFile();
        BindingResult result = new MapBindingResult(new HashMap<>(), MultipartFileUtils.LOGO_FIELD_NAME);

        // Act
        logoValidator.validateLogo(invalidLogo, result);

        // Assert
        assertTrue("Validation errors should be present for an invalid image type", result.hasErrors());
        assertEquals("Invalid image type", result.getFieldError(MultipartFileUtils.LOGO_FIELD_NAME).getDefaultMessage());
    }

    @Test
    public void testValidateLogo_EmptyFile() {
        // Arrange
        MultipartFile emptyLogo = MultipartFileUtils.getEmptyLogo();
        BindingResult result = new MapBindingResult(new HashMap<>(), MultipartFileUtils.LOGO_FIELD_NAME);

        // Act
        logoValidator.validateLogo(emptyLogo, result);

        // Assert
        assertFalse("No validation errors should be present for an empty file", result.hasErrors());
    }

    @Test
    public void testValidateLogo_UnsupportedImageType() {
        // Arrange
        MultipartFile unsupportedLogo = new MockMultipartFile(
                MultipartFileUtils.LOGO_FIELD_NAME,
                "test.bmp",
                "image/bmp",
                MultipartFileUtils.VALID_LOGO_CONTENT
        );
        BindingResult result = new MapBindingResult(new HashMap<>(), MultipartFileUtils.LOGO_FIELD_NAME);

        // Act
        logoValidator.validateLogo(unsupportedLogo, result);

        // Assert
        assertTrue("Validation errors should be present for an unsupported image type", result.hasErrors());
        assertEquals("Invalid image type", result.getFieldError(MultipartFileUtils.LOGO_FIELD_NAME).getDefaultMessage());
    }
}


