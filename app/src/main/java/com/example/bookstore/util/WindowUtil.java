package com.example.bookstore.util;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WindowUtil {

    // Chừa khoảng trống cho thanh hệ thống bằng padding của view gốc.
    // includeBottom = false khi phía dưới màn hình tự xử lý hoặc cuộn được.
    public static void applySystemBarsPadding(View root, final boolean includeBottom) {
        ViewCompat.setOnApplyWindowInsetsListener(root, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                int bottom = includeBottom ? bars.bottom : 0;
                v.setPadding(bars.left, bars.top, bars.right, bottom);
                return insets;
            }
        });
    }
}
