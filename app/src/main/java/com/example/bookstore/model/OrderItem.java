package com.example.bookstore.model;


public class OrderItem {

    private int id;
    private int orderId;
    private int bookId;
    private int quantity;
    private long unitPrice;
    private String bookTitle; // lấy kèm khi đọc, không lưu trong bảng order_items
    private String bookImage;

    public OrderItem() {
    }

    public long getSubtotal() {
        return unitPrice * quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public long getUnitPrice() { return unitPrice; }
    public void setUnitPrice(long unitPrice) { this.unitPrice = unitPrice; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getBookImage() { return bookImage; }
    public void setBookImage(String bookImage) { this.bookImage = bookImage; }
}