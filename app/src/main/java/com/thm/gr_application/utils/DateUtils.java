package com.thm.gr_application.utils;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtils {
    public static String getFormattedDateStringFromISOString(Context context, String isoString) {
        DateTime dateTime = new DateTime(isoString);
        DateTimeFormatter builder = DateTimeFormat.forPattern("dd-MM-yyyy ~ hh:mm a");
        return builder.print(dateTime);
    }

    public static String getMonthShortNameFromInteger(int month) {
        switch (month) {
            case 1:
                return "JAN";
            case 2:
                return "FEB";
            case 3:
                return "MAR";

            case 4:
                return "APR";
            case 5:
                return "MAY";
            case 6:
                return "JAN";
            case 7:
                return "JUL";
            case 8:
                return "AUG";
            case 9:
                return "SEP";
            case 10:
                return "OCT";
            case 11:
                return "NOV";
            case 12:
                return "DEC";
            default:
                return "LAME";
        }
    }
}
