package com.example.bookstore.util;

import android.util.Patterns;

public class ValidationUtil {

    public static final int MIN_PASSWORD_LENGTH = 6;

    // Email đúng định dạng chuẩn
    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Số điện thoại: 10 số, bắt đầu bằng 0
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("0\\d{9}");
    }

    // Mật khẩu từ 6 ký tự trở lên, có cả chữ lẫn số
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }
}
