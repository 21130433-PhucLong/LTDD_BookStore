package com.example.bookstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.bookstore.R;
import com.example.bookstore.adapter.OrderAdapter;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Order;
import com.example.bookstore.util.AdminUtil;
import com.example.bookstore.util.SessionManager;
import com.example.bookstore.util.StatusUtil;
import com.example.bookstore.util.UiUtil;
import com.example.bookstore.util.WindowUtil;

import java.util.List;


public class OrderListActivity extends AppCompatActivity {

    // true: quản trị viên xem đơn của mọi khách
    public static final String EXTRA_ADMIN_MODE = "admin_mode";

    private static final String STATE_STATUS = "state_status";
    private static final String STATE_SORT = "state_sort";

    // Trạng thái dùng cho các nút lọc; rỗng nghĩa là tất cả
    private static final String[] STATUS_FILTERS = {
            "", Order.STATUS_PENDING, Order.STATUS_SHIPPING, Order.STATUS_DELIVERED, Order.STATUS_CANCELLED
    };

    private ChipGroup chipGroup;
    private Spinner spinnerSort;
    private ListView lvOrders;
    private TextView tvEmpty;
    private OrderAdapter adapter;

    private DatabaseHelper db;
    private int userId;
    private boolean adminMode;
    private String statusFilter = "";
    private int sortPosition = 0; // 0 = mới nhất, 1 = cũ nhất

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adminMode = getIntent().getBooleanExtra(EXTRA_ADMIN_MODE, false);
        // Chế độ quản trị chỉ dành cho admin
        if (adminMode && !AdminUtil.isCurrentUserAdmin(this)) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_list);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), false);
        UiUtil.setupBackToolbar(this, adminMode ? R.string.title_admin_orders : R.string.title_orders);

        chipGroup = findViewById(R.id.chipGroup);
        spinnerSort = findViewById(R.id.spinnerSort);
        lvOrders = findViewById(R.id.lvOrders);
        tvEmpty = findViewById(R.id.tvEmpty);
        WindowUtil.applyBottomInsetPadding(lvOrders);

        db = DatabaseHelper.getInstance(this);
        userId = new SessionManager(this).getUserId();

        if (savedInstanceState != null) {
            statusFilter = savedInstanceState.getString(STATE_STATUS, "");
            sortPosition = savedInstanceState.getInt(STATE_SORT, 0);
        }

        adapter = new OrderAdapter(this);
        adapter.setShowCustomer(adminMode);
        lvOrders.setAdapter(adapter);

        setupChips();
        setupSortSpinner();

        // Bấm một đơn: mở chi tiết (giữ nguyên chế độ đang dùng)
        lvOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(OrderListActivity.this, OrderDetailActivity.class);
                intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, adapter.getItem(position).getId());
                intent.putExtra(OrderDetailActivity.EXTRA_ADMIN_MODE, adminMode);
                startActivity(intent);
            }
        });
    }

    // Tạo hàng nút trạng thái
    private void setupChips() {
        chipGroup.removeAllViews();
        for (String status : STATUS_FILTERS) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_category, chipGroup, false);
            chip.setId(View.generateViewId());
            chip.setText(status.isEmpty() ? getString(R.string.status_all) : StatusUtil.getLabel(this, status));
            chip.setTag(status);
            chipGroup.addView(chip);
            if (status.equals(statusFilter)) {
                chip.setChecked(true);
            }
        }

        chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if (checkedIds.isEmpty()) {
                    return;
                }
                Chip chip = group.findViewById(checkedIds.get(0));
                statusFilter = (String) chip.getTag();
                loadOrders();
            }
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                this, R.array.order_sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setSelection(sortPosition);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != sortPosition) {
                    sortPosition = position;
                    loadOrders();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Đọc đơn theo bộ lọc hiện tại rồi hiển thị
    private void loadOrders() {
        boolean newestFirst = sortPosition == 0;
        List<Order> orders = adminMode
                ? db.getAllOrders(statusFilter, newestFirst)
                : db.getOrdersByUser(userId, statusFilter, newestFirst);
        adapter.setData(orders);
        tvEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Quay lại từ màn chi tiết (trạng thái đơn có thể vừa đổi) thì tải lại
    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_STATUS, statusFilter);
        outState.putInt(STATE_SORT, sortPosition);
    }
}