package de.imi.mopat.helper.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ThymeleafMessageHelper {

    @Autowired
    private ReloadableResourceBundleMessageSource messageSource;

    /**
     * Receive the message for the given code with a default fallback string, if no result is found
     *
     * @param locale
     * @param code          for the message String
     * @param defaultString fallback String if no result is found
     * @return String with either message String or fallback
     */
    public String get(final Locale locale, final String code, final String defaultString) {

        return messageSource.getMessage(
            new DefaultMessageSourceResolvable(new String[]{code}, defaultString), locale);
    }

    public String get(final Locale locale, final String code, final Object[] arguments,
        final String defaultString) {
        return messageSource.getMessage(
            new DefaultMessageSourceResolvable(new String[]{code}, arguments, defaultString),
            locale);
    }

    /**
     * Since Thymeleaf restricts access to the base java classes since v3.1 we need a way to create
     * a new Object array, by parsing a dynamic number of objects that can be passed to the message
     * source
     *
     * @param args dynamic number of arguments
     * @return Object[]
     */
    public Object[] buildArguments(Object... args) {
        return args;
    }
}
