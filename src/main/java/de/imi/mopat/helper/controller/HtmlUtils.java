package de.imi.mopat.helper.controller;

import java.util.regex.Pattern;

public class HtmlUtils {
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    public static String removeHtmlTags(String input) {
        if (input == null) {
            return null;
        }
        return HTML_TAG_PATTERN.matcher(input).replaceAll("");
    }
}