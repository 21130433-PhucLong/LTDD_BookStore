package com.example.bookstore.util;

import android.content.Context;

import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Xuất hóa đơn ra file .txt trong bộ nhớ trong của ứng dụng và đọc lại.
// File nằm ở thư mục files/invoices, chỉ ứng dụng này truy cập được.

public class InvoiceUtil {

    // Ghi hóa đơn, trả về file vừa tạo (ghi đè nếu đã có)
    public static File export(Context context, Order order, List<OrderItem> items,
                              String statusLabel, String paymentLabel) throws IOException {
        File dir = new File(context.getFilesDir(), "invoices");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Không tạo được thư mục hóa đơn");
        }
        File file = new File(dir, "hoadon_" + order.getId() + ".txt");

        StringBuilder sb = new StringBuilder();
        sb.append("HÓA ĐƠN BÁN HÀNG - BOOKSTORE\n");
        sb.append("Mã đơn: #").append(order.getId()).append("\n");
        sb.append("Ngày đặt: ").append(FormatUtil.formatDateTime(order.getCreatedAt())).append("\n");
        sb.append("Trạng thái: ").append(statusLabel).append("\n");
        sb.append("Người nhận: ").append(order.getReceiverName()).append("\n");
        sb.append("Điện thoại: ").append(order.getPhone()).append("\n");
        sb.append("Địa chỉ: ").append(order.getAddress()).append("\n");
        sb.append("Thanh toán: ").append(paymentLabel).append("\n");
        sb.append("----------------------------------------\n");
        int index = 1;
        for (OrderItem item : items) {
            sb.append(index++).append(". ").append(item.getBookTitle()).append("\n");
            sb.append("   ").append(item.getQuantity()).append(" x ")
                    .append(FormatUtil.formatPrice(item.getUnitPrice())).append(" = ")
                    .append(FormatUtil.formatPrice(item.getSubtotal())).append("\n");
        }
        sb.append("----------------------------------------\n");
        sb.append("Tạm tính: ").append(FormatUtil.formatPrice(order.getSubtotal())).append("\n");
        sb.append("Phí giao hàng: ").append(FormatUtil.formatPrice(order.getShippingFee())).append("\n");
        if (order.getDiscountAmount() > 0) {
            sb.append("Giảm giá (").append(order.getVoucherCode()).append("): -")
                    .append(FormatUtil.formatPrice(order.getDiscountAmount())).append("\n");
        }
        sb.append("TỔNG THANH TOÁN: ").append(FormatUtil.formatPrice(order.getTotal())).append("\n");

        // try-with-resources: tự đóng file khi xong
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(sb.toString());
        }
        return file;
    }

    // Đọc toàn bộ nội dung một file văn bản
    public static String read(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}