package de.imi.mopat.helper.controller;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ThymeleafLocalHelper {

    public static String formatTimestampToLocaleDate(Timestamp timestamp) {
        Locale locale = Locale.getDefault();
        Date date = new Date(timestamp.getTime());
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
        return dateFormat.format(date);
    }

    public static String formatDateToLocaleDate(Date date) {
        Locale locale = Locale.getDefault();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
        return dateFormat.format(date);
    }
}