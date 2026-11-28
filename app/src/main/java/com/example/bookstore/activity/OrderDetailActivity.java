package com.example.bookstore.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderItem;
import com.example.bookstore.model.User;
import com.example.bookstore.util.AdminUtil;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.InvoiceUtil;
import com.example.bookstore.util.SessionManager;
import com.example.bookstore.util.StatusUtil;
import com.example.bookstore.util.UiUtil;
import com.example.bookstore.util.WindowUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_ADMIN_MODE = "admin_mode";

    private DatabaseHelper db;
    private int userId;
    private int orderId;
    private boolean adminMode;
    private Order order;

    private TextView tvOrderCode;
    private TextView tvStatus;
    private TextView tvDate;
    private TextView tvCustomer;
    private TextView tvReceiver;
    private TextView tvPhone;
    private TextView tvAddress;
    private TextView tvPayment;
    private TextView tvNote;
    private TextView tvCancelReason;
    private LinearLayout llItems;
    private LinearLayout llSummary;
    private Button btnAction;
    private Button btnInvoice;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adminMode = getIntent().getBooleanExtra(EXTRA_ADMIN_MODE, false);
        if (adminMode && !AdminUtil.isCurrentUserAdmin(this)) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_detail);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), false);
        WindowUtil.applyBottomInsetPadding(findViewById(R.id.contentLayout));
        UiUtil.setupBackToolbar(this, R.string.title_order_detail);

        initViews();
        db = DatabaseHelper.getInstance(this);
        userId = new SessionManager(this).getUserId();
        orderId = getIntent().getIntExtra(EXTRA_ORDER_ID, -1);

        if (!showOrder()) {
            return;
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog();
            }
        });
        btnInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportInvoice();
            }
        });
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNextStatus();
            }
        });
    }

    private void initViews() {
        tvOrderCode = findViewById(R.id.tvOrderCode);
        tvStatus = findViewById(R.id.tvStatus);
        tvDate = findViewById(R.id.tvDate);
        tvCustomer = findViewById(R.id.tvCustomer);
        tvReceiver = findViewById(R.id.tvReceiver);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvPayment = findViewById(R.id.tvPayment);
        tvNote = findViewById(R.id.tvNote);
        tvCancelReason = findViewById(R.id.tvCancelReason);
        llItems = findViewById(R.id.llItems);
        llSummary = findViewById(R.id.llSummary);
        btnAction = findViewById(R.id.btnAction);
        btnInvoice = findViewById(R.id.btnInvoice);
        btnCancel = findViewById(R.id.btnCancel);
    }

    // Đọc đơn từ database và hiển thị; trả về false (và đóng màn hình) nếu không xem được đơn này
    private boolean showOrder() {
        order = db.getOrderById(orderId);
        // Khách chỉ xem được đơn của chính mình; quản trị viên xem được mọi đơn
        if (order == null || (!adminMode && order.getUserId() != userId)) {
            finish();
            return false;
        }

        tvOrderCode.setText(getString(R.string.label_order_code, order.getId()));
        tvStatus.setText(StatusUtil.getLabel(this, order.getStatus()));
        tvStatus.setTextColor(StatusUtil.getColor(this, order.getStatus()));
        tvDate.setText(getString(R.string.label_order_date, FormatUtil.formatDateTime(order.getCreatedAt())));
        tvReceiver.setText(getString(R.string.label_receiver_value, order.getReceiverName()));
        tvPhone.setText(getString(R.string.label_phone_value, order.getPhone()));
        tvAddress.setText(getString(R.string.label_address_value, order.getAddress()));
        tvPayment.setText(getString(R.string.label_payment_value,
                StatusUtil.getPaymentLabel(this, order.getPaymentMethod())));

        boolean hasNote = order.getNote() != null && !order.getNote().isEmpty();
        tvNote.setVisibility(hasNote ? View.VISIBLE : View.GONE);
        if (hasNote) {
            tvNote.setText(getString(R.string.label_note_value, order.getNote()));
        }

        boolean hasReason = order.getCancelReason() != null && !order.getCancelReason().isEmpty();
        tvCancelReason.setVisibility(hasReason ? View.VISIBLE : View.GONE);
        if (hasReason) {
            tvCancelReason.setText(getString(R.string.label_cancel_reason_value, order.getCancelReason()));
        }

        // Sản phẩm và tổng tiền
        llItems.removeAllViews();
        llSummary.removeAllViews();
        for (OrderItem item : db.getOrderItems(orderId)) {
            String name = getString(R.string.order_item_line, item.getBookTitle(), item.getQuantity());
            UiUtil.addLine(this, llItems, name, FormatUtil.formatPrice(item.getSubtotal()), false);
        }
        String feeText = order.getShippingFee() == 0
                ? getString(R.string.label_free) : FormatUtil.formatPrice(order.getShippingFee());
        UiUtil.addLine(this, llSummary, getString(R.string.label_subtotal),
                FormatUtil.formatPrice(order.getSubtotal()), false);
        UiUtil.addLine(this, llSummary, getString(R.string.label_shipping), feeText, false);

        if (order.getDiscountAmount() > 0) {
            UiUtil.addLine(this, llSummary, getString(R.string.label_discount, order.getVoucherCode()),
                    "-" + FormatUtil.formatPrice(order.getDiscountAmount()), false);
        }

        UiUtil.addLine(this, llSummary, getString(R.string.label_total),
                FormatUtil.formatPrice(order.getTotal()), true);

        showButtons();
        return true;
    }

    // Quyết định hiện nút nào tùy chế độ và trạng thái đơn
    private void showButtons() {
        if (adminMode) {
            User customer = db.getUserById(order.getUserId());
            String name = customer == null ? "" : customer.getName();
            String email = customer == null ? "" : customer.getEmail();
            tvCustomer.setVisibility(View.VISIBLE);
            tvCustomer.setText(getString(R.string.label_customer_value, name, email));

            // Nút chuyển sang bước tiếp theo
            if (Order.STATUS_PENDING.equals(order.getStatus())) {
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText(R.string.btn_confirm_shipping);
            } else if (Order.STATUS_SHIPPING.equals(order.getStatus())) {
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText(R.string.btn_confirm_delivered);
            } else {
                btnAction.setVisibility(View.GONE);
            }
            btnCancel.setVisibility(
                    Order.canTransition(order.getStatus(), Order.STATUS_CANCELLED) ? View.VISIBLE : View.GONE);
        } else {
            tvCustomer.setVisibility(View.GONE);
            btnAction.setVisibility(View.GONE);
            // Khách chỉ hủy được khi đơn còn chờ xác nhận
            btnCancel.setVisibility(order.canCancel() ? View.VISIBLE : View.GONE);
        }
    }

    // Quản trị viên: chờ xác nhận -> đang giao -> đã giao
    private void moveToNextStatus() {
        String next;
        if (Order.STATUS_PENDING.equals(order.getStatus())) {
            next = Order.STATUS_SHIPPING;
        } else if (Order.STATUS_SHIPPING.equals(order.getStatus())) {
            next = Order.STATUS_DELIVERED;
        } else {
            return;
        }
        reportStatusResult(db.changeOrderStatus(orderId, next, null));
    }

    private void reportStatusResult(int result) {
        int messageRes;
        if (result == DatabaseHelper.CHANGE_OK) {
            messageRes = R.string.msg_status_changed;
        } else if (result == DatabaseHelper.CHANGE_INVALID) {
            messageRes = R.string.msg_status_invalid;
        } else {
            messageRes = R.string.msg_update_failed;
        }
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
        showOrder(); // đọc lại để hiển thị trạng thái mới nhất
    }

    // Hộp thoại nhập lý do hủy; kiểm tra lý do mà không tự đóng hộp thoại khi sai
    private void showCancelDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cancel_reason, null);
        final TextInputLayout tilReason = dialogView.findViewById(R.id.tilCancelReason);
        final EditText edtReason = dialogView.findViewById(R.id.edtCancelReason);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_cancel_title)
                .setView(dialogView)
                .setPositiveButton(R.string.btn_confirm_cancel, null) // sự kiện gắn bên dưới
                .setNegativeButton(R.string.btn_back, null)
                .create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason = edtReason.getText().toString().trim();
                if (reason.length() < 5) {
                    tilReason.setError(getString(R.string.error_cancel_reason));
                    return;
                }
                dialog.dismiss();
                if (adminMode) {
                    reportStatusResult(db.changeOrderStatus(orderId, Order.STATUS_CANCELLED, reason));
                } else {
                    boolean ok = db.cancelOrder(orderId, userId, reason);
                    Toast.makeText(OrderDetailActivity.this,
                            ok ? R.string.msg_order_cancelled : R.string.msg_cancel_failed,
                            Toast.LENGTH_SHORT).show();
                    showOrder();
                }
            }
        });
    }

    // Ghi hóa đơn ra file .txt trong bộ nhớ trong, đọc lại và hiển thị
    private void exportInvoice() {
        try {
            List<OrderItem> items = db.getOrderItems(orderId);
            File file = InvoiceUtil.export(this, order, items,
                    StatusUtil.getLabel(this, order.getStatus()),
                    StatusUtil.getPaymentLabel(this, order.getPaymentMethod()));
            String content = InvoiceUtil.read(file);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.invoice_title)
                    .setMessage(file.getAbsolutePath() + "\n\n" + content)
                    .setPositiveButton(R.string.btn_close, null)
                    .show();
        } catch (IOException e) {
            Toast.makeText(this, R.string.msg_invoice_error, Toast.LENGTH_SHORT).show();
        }
    }
}