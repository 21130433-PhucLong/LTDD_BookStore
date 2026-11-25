package com.example.bookstore.util;

import android.content.Context;

import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.User;


public class AdminUtil {

    //KT nguoi dung dang nhap co phai admin hay khong
    public static boolean isCurrentUserAdmin(Context context) {
        int userId = new SessionManager(context).getUserId();
        User user = DatabaseHelper.getInstance(context).getUserById(userId);
        return user != null && user.isAdmin();
    }
}