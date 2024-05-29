package de.imi.mopat.validator;

import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.model.AuditEntry;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 *
 */
@Component
public class AuditEntryValidator implements Validator {

    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    AuditEntryDao auditEntryDao;
    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(final Class<?> type) {
        return AuditEntry.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        // [bt] first, let the standard validator validate the target object
        // with respect to it's JSR-303 constraints (javax.validation
        // .constraints annotations)
        validator.validate(target, errors);

        AuditEntry auditEntry = (AuditEntry) target;

        if (auditEntry.getAction() == AuditEntryActionType.RECEIVED
            || auditEntry.getAction() == AuditEntryActionType.SENT) {
            if (auditEntry.getSenderReceiver() == null || auditEntry.getSenderReceiver().trim()
                .isEmpty()) {
                errors.rejectValue("oldPassword", "errormessage",
                    messageSource.getMessage("auditEntry.error" + ".noSenderReceiver",
                        new Object[]{}, LocaleContextHolder.getLocale()));
            }
        }
    }
}
