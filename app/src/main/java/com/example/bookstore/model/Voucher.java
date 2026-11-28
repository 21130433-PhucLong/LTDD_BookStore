package com.example.bookstore.model;

public class Voucher {

    private int id;
    private String code;
    private String description;
    private int discountPercent;
    private long maxDiscount;
    private long minOrder;
    private boolean active;
    private String expiredAt;

    public Voucher() {
    }

    // Số tiền được giảm cho đơn có tiền hàng là subtotal
    public long calculateDiscount(long subtotal) {
        long discount = subtotal * discountPercent / 100;
        return Math.min(discount, maxDiscount);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }

    public long getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(long maxDiscount) { this.maxDiscount = maxDiscount; }

    public long getMinOrder() { return minOrder; }
    public void setMinOrder(long minOrder) { this.minOrder = minOrder; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getExpiredAt() { return expiredAt; }
    public void setExpiredAt(String expiredAt) { this.expiredAt = expiredAt; }
}