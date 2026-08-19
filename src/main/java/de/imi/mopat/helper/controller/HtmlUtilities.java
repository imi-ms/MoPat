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

    /*
     * Converts HTML content to countable visible text for validation.
     * Unlike getStringWithoutHtml(...), this also preserves visible line breaks
     * from tags like <br>, </p> or </div> and decodes common HTML entities
     * such as &nbsp.
     */
    public static String getVisibleText(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        String text = html
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        text = text.replaceAll("(?i)<br\\s*/?>", "\n");
        text = text.replaceAll("(?i)</(?:div|p|li|h[1-6])\\s*>", "\n");
        text = text.replaceAll("(?s)<[^>]+>", "");

        text = text.replace("&nbsp;", " ");
        text = text.replace("&amp;", "&");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&quot;", "\"");
        text = text.replace("&#39;", "'");

        return text.replaceAll("\n{3,}", "\n\n").trim();
    }

}
