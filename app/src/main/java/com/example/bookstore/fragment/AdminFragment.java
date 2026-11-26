package com.example.bookstore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookstore.R;
import com.example.bookstore.activity.AdminBookListActivity;
import com.example.bookstore.activity.OrderListActivity;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Order;

public class AdminFragment extends Fragment {

    private TextView tvPending;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvPending = view.findViewById(R.id.tvPending);

        view.findViewById(R.id.btnManageBooks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(), AdminBookListActivity.class));
            }
        });
        view.findViewById(R.id.btnManageOrders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), OrderListActivity.class);
                intent.putExtra(OrderListActivity.EXTRA_ADMIN_MODE, true);
                startActivity(intent);
            }
        });
    }

    // Quay về từ màn quản lý đơn thì cập nhật lại số đơn chờ
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.tab_admin);
        int pending = DatabaseHelper.getInstance(requireContext()).countOrdersByStatus(Order.STATUS_PENDING);
        tvPending.setText(getString(R.string.admin_pending_orders, pending));
    }
}