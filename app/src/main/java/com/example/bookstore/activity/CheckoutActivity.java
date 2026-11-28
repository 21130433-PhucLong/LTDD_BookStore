package com.example.bookstore.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.CartItem;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.User;
import com.example.bookstore.model.Voucher;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.SessionManager;
import com.example.bookstore.util.UiUtil;
import com.example.bookstore.util.ValidationUtil;
import com.example.bookstore.util.VoucherUtil;
import com.example.bookstore.util.WindowUtil;

import java.util.List;

/** Màn hình đặt hàng: nhập thông tin giao hàng, chọn thanh toán, xem tổng tiền và xác nhận đơn. */
public class CheckoutActivity extends AppCompatActivity {

    private static final String STATE_VOUCHER = "state_voucher";

    private TextInputLayout tilReceiver;
    private TextInputLayout tilPhone;
    private TextInputLayout tilAddress;
    private TextInputLayout tilVoucher;
    private EditText edtReceiver;
    private EditText edtPhone;
    private EditText edtAddress;
    private EditText edtNote;
    private EditText edtVoucher;
    private RadioGroup rgPayment;
    private TextView tvPaymentNote;
    private TextView tvVoucherStatus;
    private TextView tvBottomTotal;
    private LinearLayout llSummary;
    private Button btnPlaceOrder;

    private DatabaseHelper db;
    private int userId;
    private long subtotal;           // tiền hàng
    private Voucher appliedVoucher;  // mã đang áp dụng, null nếu chưa dùng

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
        showItems(items);

        // Chỉ điền sẵn khi mở mới; xoay màn hình thì giữ chữ người dùng đã sửa
        if (savedInstanceState == null) {
            fillShippingInfo();
        } else {
            // Khôi phục mã giảm giá đang áp dụng
            String code = savedInstanceState.getString(STATE_VOUCHER, "");
            if (!code.isEmpty()) {
                edtVoucher.setText(code);
                applyVoucher();
            }
        }
        renderSummary();
        setEvents();
    }

    private void initViews() {
        tilReceiver = findViewById(R.id.tilReceiver);
        tilPhone = findViewById(R.id.tilPhone);
        tilAddress = findViewById(R.id.tilAddress);
        tilVoucher = findViewById(R.id.tilVoucher);
        edtReceiver = findViewById(R.id.edtReceiver);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtNote = findViewById(R.id.edtNote);
        edtVoucher = findViewById(R.id.edtVoucher);
        rgPayment = findViewById(R.id.rgPayment);
        tvPaymentNote = findViewById(R.id.tvPaymentNote);
        tvVoucherStatus = findViewById(R.id.tvVoucherStatus);
        tvBottomTotal = findViewById(R.id.tvBottomTotal);
        llSummary = findViewById(R.id.llSummary);
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

    // Hiển thị danh sách sản phẩm (chỉ làm một lần) và tính tiền hàng
    private void showItems(List<CartItem> items) {
        LinearLayout llItems = findViewById(R.id.llItems);
        subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getSubtotal();
            String name = getString(R.string.order_item_line, item.getBook().getTitle(), item.getQuantity());
            UiUtil.addLine(this, llItems, name, FormatUtil.formatPrice(item.getSubtotal()), false);
        }
    }

    // Vẽ lại các dòng tạm tính, phí giao hàng, giảm giá và tổng (gọi mỗi khi đổi mã giảm giá)
    private void renderSummary() {
        llSummary.removeAllViews();

        long shippingFee = Order.calculateShippingFee(subtotal);
        long discount = appliedVoucher == null ? 0 : appliedVoucher.calculateDiscount(subtotal);
        long total = subtotal + shippingFee - discount;
        String feeText = shippingFee == 0 ? getString(R.string.label_free) : FormatUtil.formatPrice(shippingFee);

        UiUtil.addLine(this, llSummary, getString(R.string.label_subtotal), FormatUtil.formatPrice(subtotal), false);
        UiUtil.addLine(this, llSummary, getString(R.string.label_shipping), feeText, false);
        if (appliedVoucher != null) {
            UiUtil.addLine(this, llSummary, getString(R.string.label_discount, appliedVoucher.getCode()),
                    "-" + FormatUtil.formatPrice(discount), false);
        }
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

        findViewById(R.id.btnApplyVoucher).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyVoucher();
            }
        });

        findViewById(R.id.tvVoucherList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVoucherList();
            }
        });

        // Sửa mã sau khi đã áp dụng thì bỏ giảm giá, bắt áp dụng lại
        edtVoucher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (appliedVoucher != null
                        && !s.toString().trim().equalsIgnoreCase(appliedVoucher.getCode())) {
                    removeVoucher();
                }
            }
        });

        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrder();
            }
        });
    }

    // Kiểm tra mã người dùng nhập; hợp lệ thì áp dụng, không thì báo lý do
    private void applyVoucher() {
        tilVoucher.setError(null);
        String code = edtVoucher.getText().toString().trim();
        if (code.isEmpty()) {
            removeVoucher();
            return;
        }

        Voucher voucher = db.getVoucherByCode(code);
        int result = VoucherUtil.check(voucher, subtotal);
        if (result != VoucherUtil.OK) {
            removeVoucher();
            tilVoucher.setError(getVoucherError(result, voucher));
            return;
        }

        appliedVoucher = voucher;
        tvVoucherStatus.setVisibility(View.VISIBLE);
        tvVoucherStatus.setText(getString(R.string.voucher_applied, voucher.getCode(),
                voucher.getDiscountPercent(), FormatUtil.formatPrice(voucher.getMaxDiscount())));
        renderSummary();
    }

    private String getVoucherError(int result, Voucher voucher) {
        if (result == VoucherUtil.NOT_FOUND) {
            return getString(R.string.voucher_not_found);
        } else if (result == VoucherUtil.INACTIVE) {
            return getString(R.string.voucher_inactive);
        } else if (result == VoucherUtil.EXPIRED) {
            return getString(R.string.voucher_expired);
        }
        return getString(R.string.voucher_min_order, FormatUtil.formatPrice(voucher.getMinOrder()));
    }

    private void removeVoucher() {
        appliedVoucher = null;
        tvVoucherStatus.setVisibility(View.GONE);
        renderSummary();
    }

    // Hộp thoại liệt kê các mã đang dùng được; chọn một mã thì điền vào ô và áp dụng luôn
    private void showVoucherList() {
        final List<Voucher> vouchers = db.getActiveVouchers();
        if (vouchers.isEmpty()) {
            Toast.makeText(this, R.string.voucher_list_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence[] labels = new CharSequence[vouchers.size()];
        for (int i = 0; i < vouchers.size(); i++) {
            Voucher v = vouchers.get(i);
            labels[i] = getString(R.string.voucher_item_label, v.getCode(), v.getDiscountPercent(),
                    FormatUtil.formatPrice(v.getMaxDiscount()), FormatUtil.formatPrice(v.getMinOrder()));
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.voucher_list_title)
                .setItems(labels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        edtVoucher.setText(vouchers.get(which).getCode());
                        applyVoucher();
                    }
                })
                .show();
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
        String voucherCode = appliedVoucher == null ? null : appliedVoucher.getCode();

        btnPlaceOrder.setEnabled(false); // tránh bấm hai lần tạo hai đơn
        long result = db.placeOrder(userId, receiver, phone, address, payment, note, voucherCode);

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
        } else if (result == DatabaseHelper.ORDER_ERR_VOUCHER) {
            messageRes = R.string.error_order_voucher;
            applyVoucher(); // kiểm tra lại để hiện lý do cụ thể ở ô mã giảm giá
        } else {
            messageRes = R.string.error_order_other;
        }
        Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_VOUCHER, appliedVoucher == null ? "" : appliedVoucher.getCode());
    }
}