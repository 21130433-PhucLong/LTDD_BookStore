package com.example.bookstore.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.util.SessionManager;
import com.example.bookstore.util.UiUtil;
import com.example.bookstore.util.ValidationUtil;
import com.example.bookstore.util.WindowUtil;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout tilOld;
    private TextInputLayout tilNew;
    private TextInputLayout tilConfirm;
    private EditText edtOld;
    private EditText edtNew;
    private EditText edtConfirm;

    private DatabaseHelper db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), false);
        WindowUtil.applyBottomInsetPadding(findViewById(R.id.contentLayout));
        UiUtil.setupBackToolbar(this, R.string.title_change_password);

        tilOld = findViewById(R.id.tilOld);
        tilNew = findViewById(R.id.tilNew);
        tilConfirm = findViewById(R.id.tilConfirm);
        edtOld = findViewById(R.id.edtOld);
        edtNew = findViewById(R.id.edtNew);
        edtConfirm = findViewById(R.id.edtConfirm);

        db = DatabaseHelper.getInstance(this);
        userId = new SessionManager(this).getUserId();

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    private void changePassword() {
        tilOld.setError(null);
        tilNew.setError(null);
        tilConfirm.setError(null);

        String oldPassword = edtOld.getText().toString();
        String newPassword = edtNew.getText().toString();
        String confirm = edtConfirm.getText().toString();

        if (oldPassword.isEmpty()) {
            tilOld.setError(getString(R.string.error_password_empty));
            edtOld.requestFocus();
            return;
        }
        if (!ValidationUtil.isValidPassword(newPassword)) {
            tilNew.setError(getString(R.string.error_password_short));
            edtNew.requestFocus();
            return;
        }
        if (newPassword.equals(oldPassword)) {
            tilNew.setError(getString(R.string.error_same_password));
            edtNew.requestFocus();
            return;
        }
        if (!newPassword.equals(confirm)) {
            tilConfirm.setError(getString(R.string.error_confirm_not_match));
            edtConfirm.requestFocus();
            return;
        }

        int result = db.changePassword(userId, oldPassword, newPassword);
        if (result == DatabaseHelper.PW_OK) {
            Toast.makeText(this, R.string.msg_password_changed, Toast.LENGTH_SHORT).show();
            finish();
        } else if (result == DatabaseHelper.PW_WRONG_OLD) {
            tilOld.setError(getString(R.string.error_wrong_password));
            edtOld.requestFocus();
        } else {
            Toast.makeText(this, R.string.msg_update_failed, Toast.LENGTH_SHORT).show();
        }
    }
}