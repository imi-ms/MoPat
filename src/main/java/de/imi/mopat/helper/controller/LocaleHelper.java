package de.imi.mopat.helper.controller;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
     * Formats a timestamp to a date fitting the Locale
     * @param timestamp to format
     * @return String of date with the correct format
     */
    public static String formatTimestampToLocaleDate(Timestamp timestamp) {
        Locale locale = Locale.getDefault();
        Date date = new Date(timestamp.getTime());
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
        return dateFormat.format(date);
    }
    
    /**
     * Formats a timestamp to a datetime fitting the Locale
     * @param timestamp to format
     * @return String of datetime with the correct format
     */
    public static String formatTimstampToLocaleDateTime(Timestamp timestamp) {
        Locale locale = Locale.getDefault();
        LocalDateTime dateTime = timestamp.toLocalDateTime();
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale);
        return myFormatObj.format(zonedDateTime);
    }
    
    /**
     * Formats a data to fit to the locale
     * @param date to format
     * @return String of date with the correct format
     */
    public static String formatDateToLocaleDate(Date date) {
        Locale locale = Locale.getDefault();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
        return dateFormat.format(date);
    }
    
}
