package com.example.bookstore.model;


public class CartItem {

    private int id;
    private int userId;
    private int bookId;
    private int quantity;
    private Book book;

    public CartItem() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    // Thành tiền của dòng = giá bán x số lượng
    public long getSubtotal() {
        return book == null ? 0 : book.getPrice() * quantity;
    }
}