package com.example.bookstore.util;

import android.content.Context;
import android.content.SharedPreferences;

// Lu phiên đăng nhập bằng SharedPrefernences
public class SessionManager {

    private static final String PREF_NAME = "bookstore_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_REMEMBER = "remember";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // remember = true: lần mở app sau tự đăng nhập lại
    public void saveLogin(int userId, boolean remember) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putBoolean(KEY_REMEMBER, remember)
                .apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public boolean isRemembered() {
        return prefs.getBoolean(KEY_REMEMBER, false);
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }

    // Đăng xuất: xóa toàn bộ phiên
    public void logout() {
        prefs.edit().clear().apply();
    }
}