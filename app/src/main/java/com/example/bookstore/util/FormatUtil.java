package com.example.bookstore.util;

import java.text.NumberFormat;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtil {

    public static String formatPrice(long price) {
        NumberFormat format = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        return format.format(price) + "đ";
    }

    // chuyen doi tu "2026-10-16 14:30:05" -> "16/10/2026 14:30"
    public static String formatDateTime(String raw) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(raw);
            return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date);
        } catch (Exception e) {
            return raw;
        }
    }
}