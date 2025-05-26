package de.imi.mopat.helper.controller;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

/**
 * @since v1.5
 */
@Service(value = "LocaleHelper")
public class LocaleHelper {

    // All locales that are used in this application to localize the survey.
    private static final String[] LOCALESUSEDINSURVEY = {"de_DE", "en_GB", "es_ES", "fr_FR",
        "hi_IN", "it_IT", "nl_NL", "no_NO", "pl_PL", "pt_PT", "ru_RU", "sv_SE", "tr_TR", "ar",
        "fa_IR", "dari", "ku"};

    /**
     * Returns a list with available locales usable during a survey.
     *
     * @return A list with available locales usable during a survey.
     */
    public static String[] getLocalesUsedInSurvey() {
        return LOCALESUSEDINSURVEY;
    }

    /**
     * Returns a list with available locales. Removes all locales with more then two identifiers.
     *
     * @return A list with available locales.
     */
    public static List<String> getAvailableLocales() {
        List<String> availableLocaleStrings = new ArrayList<>();

        // Get all locales with more than 2 characters. The others are not
        // iso conform
        List<Locale> availableLocales = new ArrayList<>(
            Arrays.asList(Locale.getAvailableLocales()));
        for (Iterator<Locale> iterator = availableLocales.iterator(); iterator.hasNext(); ) {
            Locale locale = iterator.next();
            if (locale.toString().length() == 5) {
                availableLocaleStrings.add(locale.toString());
            }
        }

        // Get the iso conform language codes and add them to the locale list
        List<String> languageCodes = new ArrayList<>(Arrays.asList(Locale.getISOLanguages()));
        // Remove languages codes which are not iso conform
        availableLocaleStrings.remove("in_ID");
        availableLocaleStrings.remove("iw_IL");
        languageCodes.remove("in");
        languageCodes.remove("iw");
        languageCodes.remove("ji");
        languageCodes.remove("mo");
        languageCodes.add("dari");
        languageCodes.add("ru_IL");
        availableLocaleStrings.addAll(languageCodes);

        // Sort the list
        Collections.sort(availableLocaleStrings);

        return availableLocaleStrings;
    }

    /**
     * Checks whether a file at the given path exists or not.
     *
     * @param path Path for the file to be checked
     * @return True if the file exists otherwise false
     */
    public static boolean checkFileExistence(final String path) {
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    /**
     * Creates a {@link java.util.Locale} for a given string representation.
     *
     * @param localeString The string which should be converted to a {@link java.util.Locale}.
     * @return The {@link java.util.Locale} represented by the given string.
     */
    public static Locale getLocaleFromString(final String localeString) {
        String[] localeSplit = localeString.split("_");
        Locale locale = new Locale(localeSplit[0]);
        if (localeSplit.length == 2) {
            locale = new Locale(localeSplit[0], localeSplit[1]);
        }
        return locale;
    }

    /**
     * Formats a {@link Timestamp} object into a date string.
     * Uses "dd-MM-yyyy" format for German locale, otherwise ISO "yyyy-MM-dd".
     *
     * @param timestamp the {@link Timestamp} to format
     * @return formatted date string
     */
    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            throw new NullPointerException("Timestamp cannot be null.");
        }
        Locale locale = Locale.getDefault();
        String pattern = isGermanLocale(locale) ? "dd.MM.yyyy" : "yyyy-MM-dd";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(timestamp);
    }

    /**
     * Formats a {@link Date} object into a date string.
     * Uses "dd-MM-yyyy" format for German locale, otherwise ISO "yyyy-MM-dd".
     *
     * @param date the {@link Date} to format
     * @return formatted date string
     */
    public static String formatDate(Date date) {
        if (date == null) {
            throw new NullPointerException("Date cannot be null.");
        }
        Locale locale = Locale.getDefault();
        String pattern = isGermanLocale(locale) ? "dd.MM.yyyy" : "yyyy-MM-dd";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    /**
     * Formats a {@link Timestamp} object into a date-time string.
     * Uses "dd-MM-yyyy'T'HH:mm:ss" format for German locale, otherwise ISO "yyyy-MM-dd'T'HH:mm:ss".
     *
     * @param timestamp the {@link Timestamp} to format
     * @return formatted date-time string
     */
    public static String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            throw new NullPointerException("Timestamp cannot be null.");
        }
        Locale locale = Locale.getDefault();
        String pattern = isGermanLocale(locale) ? "dd.MM.yyyy' - 'HH:mm:ss" : "yyyy-MM-dd'T'HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(timestamp);
    }

    private static boolean isGermanLocale(Locale locale) {
        return locale != null && ("de".equals(locale.getLanguage()) || Locale.GERMANY.equals(locale));
    }

}
