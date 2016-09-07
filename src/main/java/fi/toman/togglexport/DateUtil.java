package fi.toman.togglexport;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import java.text.ParseException;
import java.util.Date;

public class DateUtil {

    private static final ISO8601DateFormat DATE_FORMAT = new ISO8601DateFormat();

    private DateUtil() {}

    public static String formatToISO8601String(final Date date) {
        return DATE_FORMAT.format(date);
    }

    public static Date parseFromISO8601String(final String date) throws ParseException {
        return DATE_FORMAT.parse(date);
    }
}
