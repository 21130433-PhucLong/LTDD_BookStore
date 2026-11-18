package com.example.bookstore.util;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtil {

    public static String formatPrice(long price) {
        NumberFormat format = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        return format.format(price) + "đ";
    }
}