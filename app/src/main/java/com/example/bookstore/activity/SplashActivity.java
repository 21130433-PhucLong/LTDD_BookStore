package com.example.bookstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookstore.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2 giây

    private final Handler handler = new Handler(Looper.getMainLooper());

    // Công việc chạy sau khi hết thời gian chờ: sang màn hình đăng nhập
    private final Runnable goToLogin = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish(); // đóng Splash để bấm Back không quay lại nó
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(goToLogin, SPLASH_DELAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy lịch nếu Activity bị đóng trước khi hết giờ
        handler.removeCallbacks(goToLogin);
    }
}