package com.example.bookstore.model;


public class Order {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SHIPPING = "shipping";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_CANCELLED = "cancelled";

    public static final String PAYMENT_COD = "cod";
    public static final String PAYMENT_BANK = "bank";

    public static final long SHIPPING_FEE = 20000;
    public static final long FREE_SHIPPING_FROM = 300000;

    private int id;
    private int userId;
    private long total;
    private long shippingFee;
    private String status;
    private String receiverName;
    private String phone;
    private String address;
    private String paymentMethod;
    private String note;
    private String cancelReason;
    private String createdAt;
    private int itemCount;

    public Order() {
    }

    // Phí giao hàng theo tiền hàng: từ 300.000đ thì miễn phí
    public static long calculateShippingFee(long subtotal) {
        return subtotal >= FREE_SHIPPING_FROM ? 0 : SHIPPING_FEE;
    }

    // Tiền hàng chưa gồm phí giao
    public long getSubtotal() {
        return total - shippingFee;
    }

    // Chỉ hủy được khi đơn còn chờ xác nhận
    public boolean canCancel() {
        return STATUS_PENDING.equals(status);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getShippingFee() { return shippingFee; }
    public void setShippingFee(long shippingFee) { this.shippingFee = shippingFee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
}