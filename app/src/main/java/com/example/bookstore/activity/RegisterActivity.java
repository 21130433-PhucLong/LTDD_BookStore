package com.example.bookstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.example.bookstore.R;
import com.example.bookstore.util.ValidationUtil;
import com.example.bookstore.util.WindowUtil;
import com.example.bookstore.data.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPhone;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private EditText edtFullName;
    private EditText edtEmail;
    private EditText edtPhone;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private Button btnRegister;
    private TextView tvGoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), true);

        initViews();
        setEvents();
    }

    private void initViews() {
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);
    }

    private void setEvents() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });


        tvGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Xóa toàn bộ thông báo lỗi cũ
    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    // Kiểm tra từng trường theo thứ tự, gặp lỗi thì dừng
    private void handleRegister() {
        clearErrors();

        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirm = edtConfirmPassword.getText().toString();

        if (fullName.isEmpty()) {
            tilFullName.setError(getString(R.string.error_name_empty));
            edtFullName.requestFocus();
            return;
        }
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
        if (!ValidationUtil.isValidPhone(phone)) {
            tilPhone.setError(getString(R.string.error_phone_invalid));
            edtPhone.requestFocus();
            return;
        }
        if (!ValidationUtil.isValidPassword(password)) {
            tilPassword.setError(getString(R.string.error_password_short));
            edtPassword.requestFocus();
            return;
        }
        if (confirm.isEmpty()) {
            tilConfirmPassword.setError(getString(R.string.error_confirm_empty));
            edtConfirmPassword.requestFocus();
            return;
        }
        if (!confirm.equals(password)) {
            tilConfirmPassword.setError(getString(R.string.error_confirm_not_match));
            edtConfirmPassword.requestFocus();
            return;
        }

        // Kiểm tra email đã có người dùng chưa
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        if (db.isEmailExists(email)) {
            tilEmail.setError(getString(R.string.error_email_exists));
            edtEmail.requestFocus();
            return;
        }

        // Lưu tài khoản vào database
        long newId = db.insertUser(fullName, email, phone, password);
        if (newId == -1) {
            Toast.makeText(this, R.string.msg_register_failed, Toast.LENGTH_LONG).show();
            return;
        }

        // Quay về màn hình đăng nhập kèm email để điền sẵn
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("email", email);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}