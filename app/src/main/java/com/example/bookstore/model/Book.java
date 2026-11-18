package com.example.bookstore.model;

import java.io.Serializable;

public class Book implements Serializable {

    private int id;
    private String title;
    private String author;
    private String publisher;
    private long price;          // giá bán
    private long originalPrice;  // giá gốc, dùng để tính phần trăm giảm
    private int stock;           // số lượng tồn kho
    private int sold;            // số lượng đã bán
    private float rating;
    private String description;
    private String image;
    private int categoryId;
    private boolean hidden;
    private String createdAt;

    public Book() {
    }

    public Book(int id, String title, String author, String publisher, long price, long originalPrice,
                int stock, int sold, float rating, String description, String image, int categoryId) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.originalPrice = originalPrice;
        this.stock = stock;
        this.sold = sold;
        this.rating = rating;
        this.description = description;
        this.image = image;
        this.categoryId = categoryId;
        this.hidden = false;
        this.createdAt = "";
    }

    // Phần trăm giảm giá, 0 nếu không giảm
    public int getDiscountPercent() {
        if (originalPrice > price && originalPrice > 0) {
            return (int) ((originalPrice - price) * 100 / originalPrice);
        }
        return 0;
    }

    public boolean isInStock() {
        return stock > 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(long originalPrice) {
        this.originalPrice = originalPrice;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
