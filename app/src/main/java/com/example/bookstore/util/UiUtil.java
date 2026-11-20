package com.example.bookstore.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.bookstore.R;

public class UiUtil {

    // Thêm một dòng "bên trái ... bên phải" (ví dụ: tên sách ... thành tiền) vào danh sách dọc
    public static void addLine(Context context, LinearLayout parent, String left, String right, boolean bold) {
        View line = LayoutInflater.from(context).inflate(R.layout.item_order_line, parent, false);
        TextView tvLeft = line.findViewById(R.id.tvLineName);
        TextView tvRight = line.findViewById(R.id.tvLineAmount);
        tvLeft.setText(left);
        tvRight.setText(right);
        if (bold) {
            tvLeft.setTypeface(null, Typeface.BOLD);
            tvRight.setTypeface(null, Typeface.BOLD);
        }
        parent.addView(line);
    }

    // Thanh tiêu đề có mũi tên quay lại (layout phải có MaterialToolbar id toolbar)
    public static void setupBackToolbar(final AppCompatActivity activity, int titleRes) {
        MaterialToolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(titleRes);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }
}