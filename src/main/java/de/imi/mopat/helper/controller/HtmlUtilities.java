package de.imi.mopat.helper.controller;

import org.springframework.stereotype.Service;

/**
 * Helper class for html rendering.
 */
// The value makes this dao reacheable in the jsp's
@Service(value = "HtmlUtilities")
public class HtmlUtilities {

    /**
     * Escapes the given string from html.
     *
     * @param htmlString The string which should be escaped from html.
     * @return The given string without html tags.
     */
    public static String getStringWithoutHtml(final String htmlString) {
        if (htmlString == null) {
            return null;
        }
        return htmlString.replaceAll("\\<[^>]*>", "");
    }
}
