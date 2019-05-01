package com.thm.gr_application.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtils {
    public static String getFormattedDateTimeStringFromISOString(String isoString) {
        DateTime dateTime = new DateTime(isoString);
        DateTimeFormatter builder = DateTimeFormat.forPattern("dd-MM-yyyy ~ hh:mm a");
        return builder.print(dateTime);
    }

    public static String getFormattedDateStringFromISOString(String isoString) {
        DateTime dateTime = new DateTime(isoString);
        DateTimeFormatter builder = DateTimeFormat.forPattern("dd-MM-yyyy");
        return builder.print(dateTime);
    }

    public static String getMonthShortNameFromInteger(int month) {
        switch (month) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";

            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jan";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return "LAME";
        }
    }
}
