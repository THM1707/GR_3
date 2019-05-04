package com.thm.gr_application.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtils {
    public static String getPriceNumber(int price) {
        return getAmountNumber(price) + "/h";
    }

    public static String getAmountNumber(int price) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        return numberFormat.format(price) + " đ";
    }
}
