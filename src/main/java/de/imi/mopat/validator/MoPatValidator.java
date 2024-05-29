package de.imi.mopat.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MoPatValidator implements Validator {

    public static final String ERRORCODE_ERRORMESSAGE = "errormessage";
    public static final String ERRORCODE_NOT_NULL = "not.null";

    @Override
    public boolean supports(final Class c) {
        return true;
    }

    @Override
    public void validate(final Object o, final Errors errors) {
    }
}