package br.nnpe;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FormatDate {

    private static final DateTimeFormatter DEFAULT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DEFAULT_DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DEFAULT_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String format(java.util.Date date) {
        if (date == null) return "";
        return DEFAULT_DATE.format(date.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate());
    }

    public static String format(java.sql.Timestamp timestamp) {
        if (timestamp == null) return "";
        return DEFAULT_DATETIME.format(timestamp.toLocalDateTime());
    }

    public static String format(java.sql.Time time) {
        if (time == null) return "";
        return DEFAULT_TIME.format(time.toLocalTime());
    }

    public static java.sql.Date parseDate(String date) throws Exception {
        return parseDate(date, "dd/MM/yyyy");
    }

    public static java.sql.Date parseDate(String date, String pattern) throws Exception {
        if (date == null || date.isEmpty()) return null;
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
        return java.sql.Date.valueOf(localDate);
    }

    public static java.util.Date parse(String date) throws Exception {
        return parse(date, "dd/MM/yyyy");
    }

    public static java.util.Date parse(String date, String pattern) throws Exception {
        if (date == null || date.isEmpty()) return null;
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern));
        return java.sql.Date.valueOf(localDate);
    }
}
