package com.example.bookstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.CartItem;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.User;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.SessionManager;
import com.example.bookstore.util.UiUtil;
import com.example.bookstore.util.ValidationUtil;
import com.example.bookstore.util.WindowUtil;

import java.util.List;

/** Màn hình đặt hàng: nhập thông tin giao hàng, chọn thanh toán, xem tổng tiền và xác nhận đơn. */
public class CheckoutActivity extends AppCompatActivity {

    private TextInputLayout tilReceiver;
    private TextInputLayout tilPhone;
    private TextInputLayout tilAddress;
    private EditText edtReceiver;
    private EditText edtPhone;
    private EditText edtAddress;
    private EditText edtNote;
    private RadioGroup rgPayment;
    private TextView tvPaymentNote;
    private TextView tvBottomTotal;
    private Button btnPlaceOrder;

    private DatabaseHelper db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), false);
        WindowUtil.applyBottomInsetPadding(findViewById(R.id.bottomBar));
        UiUtil.setupBackToolbar(this, R.string.title_checkout);

        initViews();
        db = DatabaseHelper.getInstance(this);
        userId = new SessionManager(this).getUserId();

        List<CartItem> items = db.getCartItems(userId);
        if (items.isEmpty()) {
            Toast.makeText(this, R.string.error_order_empty_cart, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        showSummary(items);

        // Chỉ điền sẵn khi mở mới; xoay màn hình thì giữ chữ người dùng đã sửa
        if (savedInstanceState == null) {
            fillShippingInfo();
        }
        setEvents();
    }

    private void initViews() {
        tilReceiver = findViewById(R.id.tilReceiver);
        tilPhone = findViewById(R.id.tilPhone);
        tilAddress = findViewById(R.id.tilAddress);
        edtReceiver = findViewById(R.id.edtReceiver);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtNote = findViewById(R.id.edtNote);
        rgPayment = findViewById(R.id.rgPayment);
        tvPaymentNote = findViewById(R.id.tvPaymentNote);
        tvBottomTotal = findViewById(R.id.tvBottomTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
    }

    // Điền sẵn tên, số điện thoại, địa chỉ từ tài khoản
    private void fillShippingInfo() {
        User user = db.getUserById(userId);
        if (user != null) {
            edtReceiver.setText(user.getName());
            edtPhone.setText(user.getPhone());
            edtAddress.setText(user.getAddress());
        }
    }

    // Hiển thị danh sách sản phẩm và các dòng tạm tính, phí giao hàng, tổng
    private void showSummary(List<CartItem> items) {
        LinearLayout llItems = findViewById(R.id.llItems);
        LinearLayout llSummary = findViewById(R.id.llSummary);

        long subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getSubtotal();
            String name = getString(R.string.order_item_line, item.getBook().getTitle(), item.getQuantity());
            UiUtil.addLine(this, llItems, name, FormatUtil.formatPrice(item.getSubtotal()), false);
        }

        long shippingFee = Order.calculateShippingFee(subtotal);
        long total = subtotal + shippingFee;
        String feeText = shippingFee == 0 ? getString(R.string.label_free) : FormatUtil.formatPrice(shippingFee);

        UiUtil.addLine(this, llSummary, getString(R.string.label_subtotal), FormatUtil.formatPrice(subtotal), false);
        UiUtil.addLine(this, llSummary, getString(R.string.label_shipping), feeText, false);
        UiUtil.addLine(this, llSummary, getString(R.string.label_total), FormatUtil.formatPrice(total), true);
        tvBottomTotal.setText(FormatUtil.formatPrice(total));
    }

    private void setEvents() {
        // Chọn chuyển khoản thì hiện ghi chú giả lập
        rgPayment.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                tvPaymentNote.setVisibility(checkedId == R.id.rbBank ? View.VISIBLE : View.GONE);
            }
        });

        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrder();
            }
        });
    }

    // Kiểm tra thông tin rồi tạo đơn
    private void placeOrder() {
        tilReceiver.setError(null);
        tilPhone.setError(null);
        tilAddress.setError(null);

        String receiver = edtReceiver.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String note = edtNote.getText().toString().trim();

        if (receiver.isEmpty()) {
            tilReceiver.setError(getString(R.string.error_receiver_empty));
            edtReceiver.requestFocus();
            return;
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            tilPhone.setError(getString(R.string.error_phone_invalid));
            edtPhone.requestFocus();
            return;
        }
        if (address.length() < 10) {
            tilAddress.setError(getString(R.string.error_address_short));
            edtAddress.requestFocus();
            return;
        }

        String payment = (rgPayment.getCheckedRadioButtonId() == R.id.rbBank)
                ? Order.PAYMENT_BANK : Order.PAYMENT_COD;

        btnPlaceOrder.setEnabled(false); // tránh bấm hai lần tạo hai đơn
        long result = db.placeOrder(userId, receiver, phone, address, payment, note);

        if (result > 0) {
            Intent intent = new Intent(this, OrderSuccessActivity.class);
            intent.putExtra(OrderSuccessActivity.EXTRA_ORDER_ID, (int) result);
            startActivity(intent);
            finish(); // Back từ màn thành công sẽ về màn hình chính, không quay lại đây
            return;
        }

        btnPlaceOrder.setEnabled(true);
        int messageRes;
        if (result == DatabaseHelper.ORDER_ERR_EMPTY_CART) {
            messageRes = R.string.error_order_empty_cart;
        } else if (result == DatabaseHelper.ORDER_ERR_STOCK) {
            messageRes = R.string.error_order_stock;
        } else {
            messageRes = R.string.error_order_other;
        }
        Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show();
    }
}