package com.example.bookstore.activity;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.bookstore.R;
import com.example.bookstore.model.Book;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.ImageUtil;
import com.example.bookstore.util.WindowUtil;

public class BookDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOK = "book";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_detail);

        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), false);

        setupToolbar();


        Book book = (Book) getIntent().getSerializableExtra(EXTRA_BOOK);
        if (book == null) {
            finish(); // không nhận được dữ liệu thì đóng màn hình
            return;
        }
        showBook(book);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true); // hiện mũi tên quay lại
            actionBar.setTitle(R.string.title_book_detail);
        }
    }

    // Hiển thị thông tin sách lên giao diện
    private void showBook(Book book) {
        ImageView imgCover = findViewById(R.id.imgCover);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvAuthor = findViewById(R.id.tvAuthor);
        TextView tvPublisher = findViewById(R.id.tvPublisher);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvOldPrice = findViewById(R.id.tvOldPrice);
        TextView tvDiscount = findViewById(R.id.tvDiscount);
        TextView tvStock = findViewById(R.id.tvStock);
        TextView tvMeta = findViewById(R.id.tvMeta);
        TextView tvDescription = findViewById(R.id.tvDescription);

        imgCover.setImageResource(ImageUtil.getDrawableId(this, book.getImage()));
        tvTitle.setText(book.getTitle());
        tvAuthor.setText(getString(R.string.label_author, book.getAuthor()));
        tvPublisher.setText(getString(R.string.label_publisher, book.getPublisher()));
        tvPrice.setText(FormatUtil.formatPrice(book.getPrice()));

        int discount = book.getDiscountPercent();
        if (discount > 0) {
            tvOldPrice.setVisibility(View.VISIBLE);
            tvOldPrice.setText(FormatUtil.formatPrice(book.getOriginalPrice()));
            tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvDiscount.setVisibility(View.VISIBLE);
            tvDiscount.setText("-" + discount + "%");
        } else {
            tvOldPrice.setVisibility(View.GONE);
            tvDiscount.setVisibility(View.GONE);
        }

        if (book.isInStock()) {
            tvStock.setText(getString(R.string.stock_available, book.getStock()));
            tvStock.setTextColor(getColor(R.color.brand_green));
        } else {
            tvStock.setText(R.string.stock_out);
            tvStock.setTextColor(getColor(R.color.error_red));
        }

        tvMeta.setText(getString(R.string.book_meta, book.getRating(), book.getSold()));
        tvDescription.setText(book.getDescription());
    }

    // Bấm mũi tên quay lại trên thanh tiêu đề
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}