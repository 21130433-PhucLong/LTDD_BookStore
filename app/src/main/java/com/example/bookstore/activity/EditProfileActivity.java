package com.example.bookstore.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.User;
import com.example.bookstore.util.SessionManager;
import com.example.bookstore.util.UiUtil;
import com.example.bookstore.util.ValidationUtil;
import com.example.bookstore.util.WindowUtil;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputLayout tilName;
    private TextInputLayout tilPhone;
    private EditText edtName;
    private EditText edtPhone;
    private EditText edtAddress;

    private DatabaseHelper db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), false);
        WindowUtil.applyBottomInsetPadding(findViewById(R.id.contentLayout));
        UiUtil.setupBackToolbar(this, R.string.title_edit_profile);

        tilName = findViewById(R.id.tilName);
        tilPhone = findViewById(R.id.tilPhone);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);

        db = DatabaseHelper.getInstance(this);
        userId = new SessionManager(this).getUserId();

        // Chỉ điền dữ liệu từ database khi mở mới; xoay màn hình thì giữ chữ đang sửa
        if (savedInstanceState == null) {
            User user = db.getUserById(userId);
            if (user == null) {
                finish();
                return;
            }
            ((TextView) findViewById(R.id.tvEmail)).setText(getString(R.string.label_email_value, user.getEmail()));
            edtName.setText(user.getName());
            edtPhone.setText(user.getPhone());
            edtAddress.setText(user.getAddress());
        } else {
            User user = db.getUserById(userId);
            if (user != null) {
                ((TextView) findViewById(R.id.tvEmail)).setText(getString(R.string.label_email_value, user.getEmail()));
            }
        }

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    private void save() {
        tilName.setError(null);
        tilPhone.setError(null);

        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty()) {
            tilName.setError(getString(R.string.error_name_empty));
            edtName.requestFocus();
            return;
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            tilPhone.setError(getString(R.string.error_phone_invalid));
            edtPhone.requestFocus();
            return;
        }

        if (db.updateProfile(userId, name, phone, address)) {
            Toast.makeText(this, R.string.msg_profile_saved, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.msg_update_failed, Toast.LENGTH_SHORT).show();
        }
    }
}