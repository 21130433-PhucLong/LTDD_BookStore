package com.example.bookstore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookstore.activity.LoginActivity;
import com.example.bookstore.R;

public class AccountFragment extends Fragment {

    private static final String ARG_EMAIL = "email";

    // Tạo Fragment kèm dữ liệu: đóng gói email vào Bundle rồi gắn làm arguments
    public static AccountFragment newInstance(String email) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // Đọc email từ arguments (có thể null nếu không truyền)
        Bundle args = getArguments();
        String email = "";
        if (args != null) {
            email = args.getString(ARG_EMAIL, "");
        }
        tvWelcome.setText(getString(R.string.welcome_user, email));

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                // Xóa toàn bộ back stack, Login trở thành màn hình đầu tiên
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}
