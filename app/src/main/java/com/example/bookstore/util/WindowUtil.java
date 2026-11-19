package com.example.bookstore.util;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WindowUtil {

    public static void applySystemBarsPadding(View root, final boolean includeBottom) {
        ViewCompat.setOnApplyWindowInsetsListener(root, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                int types = WindowInsetsCompat.Type.systemBars();
                if (includeBottom) {
                    types = types | WindowInsetsCompat.Type.ime();
                }
                Insets bars = insets.getInsets(types);
                int bottom = includeBottom ? bars.bottom : 0;
                v.setPadding(bars.left, bars.top, bars.right, bottom);
                return insets;
            }
        });
    }

    public static void applyBottomInsetPadding(View view) {
        final int basePaddingBottom = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                        basePaddingBottom + bars.bottom);
                return insets;
            }
        });
    }
}