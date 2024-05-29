package de.imi.mopat.validator;

import de.imi.mopat.model.dto.InvitationDTO;
import de.imi.mopat.model.dto.InvitationUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The validator for {@link InvitationDTO} objects.
 */
@Component
public class InvitationDTOValidator implements Validator {

    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(final Class<?> type) {
        return InvitationDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        InvitationDTO invitationDTO = (InvitationDTO) o;

        for (int i = 0; i < invitationDTO.getInvitationUsers().size(); i++) {
            InvitationUserDTO invitationUserDTO = invitationDTO.getInvitationUsers().get(i);
            // First name
            if (invitationUserDTO.getFirstName() == null || invitationUserDTO.getFirstName().trim()
                .isEmpty()) {
                invitationUserDTO.setFirstName("");
                errors.rejectValue("invitationUsers[" + i + "].firstName",
                    MoPatValidator.ERRORCODE_NOT_NULL,
                    messageSource.getMessage("invitation.firstName.notNull", new Object[]{},
                        LocaleContextHolder.getLocale()));
            }
            // Last name
            if (invitationUserDTO.getLastName() == null || invitationUserDTO.getLastName().trim()
                .isEmpty()) {
                invitationUserDTO.setLastName("");
                errors.rejectValue("invitationUsers[" + i + "].lastName",
                    MoPatValidator.ERRORCODE_NOT_NULL,
                    messageSource.getMessage("invitation.lastName.notNull", new Object[]{},
                        LocaleContextHolder.getLocale()));
            }
            // Email
            if (invitationUserDTO.getEmail() == null || invitationUserDTO.getEmail().trim()
                .isEmpty()) {
                invitationUserDTO.setEmail("");
                errors.rejectValue("invitationUsers[" + i + "].email",
                    MoPatValidator.ERRORCODE_NOT_NULL,
                    messageSource.getMessage("invitation.email.notNull", new Object[]{},
                        LocaleContextHolder.getLocale()));
            } else if (!invitationUserDTO.getEmail().matches(
                "[A-Za-z0-9" + ".!#$%&'*+-/=?^_" + "`{|}~]+@[A-Za-z0-9" + ".!#$%&'*+-/=?^_"
                    + "`{|}~]+\\" + ".[A-Za-z]{2,}+")) {
                errors.rejectValue("invitationUsers[" + i + "].email",
                    MoPatValidator.ERRORCODE_NOT_NULL,
                    messageSource.getMessage("global.datatype.email" + ".notValid", new Object[]{},
                        LocaleContextHolder.getLocale()));
            }
        }

    }
}
