package com.example.bookstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.example.bookstore.R;
import com.example.bookstore.util.ValidationUtil;
import com.example.bookstore.util.WindowUtil;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private EditText edtEmail;
    private EditText edtPassword;
    private CheckBox cbRemember;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private TextView tvGoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), true);
        Log.d(TAG, "onCreate");

        initViews();
        // Chỉ điền email khi mở mới, không điền lại sau khi xoay màn hình
        if (savedInstanceState == null) {
            showEmailFromRegister();
        }
        setEvents();
    }

    // Ánh xạ các view trong layout
    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        cbRemember = findViewById(R.id.cbRemember);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvGoRegister = findViewById(R.id.tvGoRegister);
    }

    // Nhận email từ màn hình đăng ký (nếu có) để điền sẵn
    private void showEmailFromRegister() {
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            edtEmail.setText(email);
            edtPassword.requestFocus();
            Toast.makeText(this, R.string.msg_register_success, Toast.LENGTH_LONG).show();
        }
    }

    private void setEvents() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        tvGoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, R.string.msg_feature_coming, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Kiểm tra dữ liệu nhập rồi chuyển sang màn hình chính
    private void handleLogin() {
        // Xóa lỗi của lần kiểm tra trước
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.error_email_empty));
            edtEmail.requestFocus();
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            edtEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_password_empty));
            edtPassword.requestFocus();
            return;
        }

        // TODO: kiểm tra tài khoản trong database (D6), hiện tại cho đăng nhập luôn
        Bundle bundle = new Bundle();
        bundle.putString("email", email);

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("userData", bundle);
        startActivity(intent);
        finish(); // đóng màn hình đăng nhập để Back không quay lại
    }

    // Ghi log vòng đời để quan sát trong Logcat
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}