package com.example.bookstore.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Order;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.WindowUtil;


public class OrderSuccessActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_success);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), true);

        int orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);
        Order order = DatabaseHelper.getInstance(this).getOrderById(orderId);
        if (order == null) {
            finish();
            return;
        }

        TextView tvOrderCode = findViewById(R.id.tvOrderCode);
        TextView tvOrderTotal = findViewById(R.id.tvOrderTotal);
        Button btnContinue = findViewById(R.id.btnContinue);

        tvOrderCode.setText(getString(R.string.order_code, order.getId()));
        tvOrderTotal.setText(getString(R.string.order_total_line, FormatUtil.formatPrice(order.getTotal())));

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // màn hình đặt hàng đã đóng nên quay về màn hình chính
            }
        });
    }
}