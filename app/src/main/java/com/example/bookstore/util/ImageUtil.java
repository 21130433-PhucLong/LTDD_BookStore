package com.example.bookstore.util;

import android.content.Context;

public class ImageUtil {

    public static int getDrawableId(Context context, String name) {
        int id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        if (id == 0) {
            id = android.R.drawable.ic_menu_gallery;
        }
        return id;
    }
}