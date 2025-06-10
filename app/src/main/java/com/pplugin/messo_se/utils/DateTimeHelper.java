package com.pplugin.messo_se.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeHelper {
    private static DateTimeHelper instance;
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

    private DateTimeHelper() {}

    public static DateTimeHelper getInstance() {
        if (instance == null) {
            instance = new DateTimeHelper();
        }
        return instance;
    }

    public String formatDateOfBirth(String isoDate) {
        try {
            Date date = inputFormat.parse(isoDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return null;
        }
    }
}

