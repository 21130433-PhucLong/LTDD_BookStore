package com.example.bookstore.util;

import java.text.Normalizer;
import java.util.Locale;

public class TextUtil {

    // "Dế Mèn Phiêu Lưu Ký" -> "de men phieu luu ky"
    public static String toSearchText(String text) {
        if (text == null) {
            return "";
        }
        // Tách dấu ra khỏi chữ cái rồi xóa các dấu
        String result = Normalizer.normalize(text, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // Chữ đ không tách được bằng cách trên nên đổi riêng
        result = result.replace('đ', 'd').replace('Đ', 'D');
        return result.toLowerCase(Locale.ROOT).trim();
    }
}