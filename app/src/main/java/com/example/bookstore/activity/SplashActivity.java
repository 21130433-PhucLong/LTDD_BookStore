package com.example.bookstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.util.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2 giây

    private final Handler handler = new Handler(Looper.getMainLooper());

    // Hết thời gian chờ: vào thẳng Main nếu đã ghi nhớ đăng nhập, ngược lại sang Login
    private final Runnable goNext = new Runnable() {
        @Override
        public void run() {
            Class<?> target = isAutoLoginValid() ? MainActivity.class : LoginActivity.class;
            startActivity(new Intent(SplashActivity.this, target));
            finish(); // đóng Splash để bấm Back không quay lại nó
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler.postDelayed(goNext, SPLASH_DELAY);
    }

    // Có ghi nhớ đăng nhập và tài khoản vẫn còn trong database thì mới tự đăng nhập
    private boolean isAutoLoginValid() {
        SessionManager session = new SessionManager(this);
        if (session.isRemembered() && session.isLoggedIn()) {
            if (DatabaseHelper.getInstance(this).getUserById(session.getUserId()) != null) {
                return true;
            }
        }
        session.logout(); // không ghi nhớ hoặc tài khoản không còn: xóa phiên cũ
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(goNext);
    }
}