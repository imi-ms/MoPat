package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.dto.InvitationDTO;
import de.imi.mopat.utils.Helper;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

public class InvitationDTOValidatorTest {

    @Autowired
    InvitationDTOValidator invitationDTOValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Creates an InvitationDTO that passes validation, so single fields can be
     * modified in isolation to test their specific constraints.
     */
    private InvitationDTO createValidInvitationDTO() {
        InvitationDTO dto = new InvitationDTO();
        dto.setLocale("de_DE");
        dto.setPersonalText(Helper.getRandomAlphabeticString(20));
        return dto;
    }

    /**
     * Test of
     * {@link InvitationDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)} regarding the length of the personal text
     * (labelled "message" in the UI). <br> Valid input: A personal text with at most
     * {@link InvitationDTOValidator#MAX_PERSONAL_TEXT_LENGTH} characters. <br> Invalid
     * input: A personal text exceeding that limit.
     */
    @Test
    public void testValidatePersonalTextLength() {
        int maxLength = InvitationDTOValidator.MAX_PERSONAL_TEXT_LENGTH;

        // Case 1: exactly at the limit -> no error expected
        InvitationDTO invitationDTO = createValidInvitationDTO();
        invitationDTO.setPersonalText(Helper.getRandomAlphabeticString(maxLength));

        BindingResult result = new MapBindingResult(new HashMap<>(), "invitationDTO");
        invitationDTOValidator.validate(invitationDTO, result);

        assertFalse("Personal text at the limit should not raise an error.",
                result.hasFieldErrors("personalText"));

        // Case 2: one character over the limit -> error expected
        int tooLongLength = maxLength + 1;
        invitationDTO = createValidInvitationDTO();
        invitationDTO.setPersonalText(Helper.getRandomAlphabeticString(tooLongLength));

        result = new MapBindingResult(new HashMap<>(), "invitationDTO");
        invitationDTOValidator.validate(invitationDTO, result);

        assertTrue("Too long personal text should raise an error.",
                result.hasFieldErrors("personalText"));

        String expectedMessage = messageSource.getMessage("invitation.message.tooLong",
                new Object[]{tooLongLength, maxLength},
                LocaleContextHolder.getLocale());

        String actualMessage = (result.getFieldError("personalText") != null) ? result.getFieldError("personalText").getDefaultMessage() : "";

        assertEquals("The returned error message for a too long personal text didn't match the expected one.",
                expectedMessage,
                actualMessage);
    }
}