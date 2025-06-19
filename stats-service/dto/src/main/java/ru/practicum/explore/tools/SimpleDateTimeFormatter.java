package ru.practicum.explore.tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleDateTimeFormatter {
    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    private SimpleDateTimeFormatter() {

    }

    public static String toString(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(PATTERN));
    }

    public static LocalDateTime parse(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(PATTERN));
    }
}