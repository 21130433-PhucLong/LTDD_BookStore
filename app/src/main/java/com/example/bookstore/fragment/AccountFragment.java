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

import com.example.bookstore.R;
import com.example.bookstore.activity.ChangePasswordActivity;
import com.example.bookstore.activity.EditProfileActivity;
import com.example.bookstore.activity.LoginActivity;
import com.example.bookstore.activity.OrderListActivity;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.User;
import com.example.bookstore.util.SessionManager;
import com.example.bookstore.activity.WishlistActivity;

public class AccountFragment extends Fragment {

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvAddress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);

        Button btnOrders = view.findViewById(R.id.btnOrders);
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        btnOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(), OrderListActivity.class));
            }
        });

        view.findViewById(R.id.btnWishlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(), WishlistActivity.class));
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(), EditProfileActivity.class));
            }
        });
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class));
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xóa phiên đăng nhập rồi về màn hình đăng nhập
                new SessionManager(requireContext()).logout();
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                // Xóa toàn bộ back stack, Login trở thành màn hình đầu tiên
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    // Hiển thị lại thông tin mỗi lần quay về (ví dụ sau khi sửa hồ sơ)
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.tab_account);
        showUser();
    }

    private void showUser() {
        int userId = new SessionManager(requireContext()).getUserId();
        User user = DatabaseHelper.getInstance(requireContext()).getUserById(userId);
        if (user == null) {
            return;
        }
        tvName.setText(user.getName());
        tvEmail.setText(getString(R.string.label_email_value, user.getEmail()));
        tvPhone.setText(getString(R.string.label_phone_value, valueOrDefault(user.getPhone())));
        tvAddress.setText(getString(R.string.label_address_value, valueOrDefault(user.getAddress())));
    }

    // Trường chưa có dữ liệu thì hiện "Chưa cập nhật"
    private String valueOrDefault(String value) {
        return (value == null || value.isEmpty()) ? getString(R.string.not_updated) : value;
    }
}