package com.example.bookstore.activity;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Book;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.ImageUtil;
import com.example.bookstore.util.SessionManager;
import com.example.bookstore.util.WindowUtil;


public class BookDetailActivity extends AppCompatActivity {

    // Khóa dùng để gửi/nhận đối tượng Book qua Intent
    public static final String EXTRA_BOOK = "book";
    private static final String STATE_QUANTITY = "state_quantity";

    private Book book;
    private int quantity = 1; // số lượng định thêm vào giỏ

    private TextView tvQty;
    private Button btnAddToCart;
    private View btnDecrease;
    private View btnIncrease;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_detail);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), false);
        WindowUtil.applyBottomInsetPadding(findViewById(R.id.bottomBar));

        setupToolbar();

        // Nhận đối tượng Book do HomeFragment gửi sang
        book = (Book) getIntent().getSerializableExtra(EXTRA_BOOK);
        if (book == null) {
            finish(); // không nhận được dữ liệu thì đóng màn hình
            return;
        }
        if (savedInstanceState != null) {
            quantity = savedInstanceState.getInt(STATE_QUANTITY, 1);
        }

        showBook();
        setupAddToCart();
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
    private void showBook() {
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
            tvDiscount.setText(getString(R.string.discount_format, discount));
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

    // Thanh chọn số lượng và nút thêm vào giỏ
    private void setupAddToCart() {
        tvQty = findViewById(R.id.tvQty);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);

        // Hết hàng: khóa các nút
        if (!book.isInStock()) {
            quantity = 0;
            btnAddToCart.setEnabled(false);
            btnAddToCart.setText(R.string.stock_out);
            btnDecrease.setEnabled(false);
            btnIncrease.setEnabled(false);
            tvQty.setText(String.valueOf(quantity));
            return;
        }

        // Số lượng định thêm luôn nằm trong khoảng 1..tồn kho
        if (quantity < 1) {
            quantity = 1;
        }
        if (quantity > book.getStock()) {
            quantity = book.getStock();
        }
        tvQty.setText(String.valueOf(quantity));

        btnDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 1) {
                    quantity--;
                    tvQty.setText(String.valueOf(quantity));
                }
            }
        });

        btnIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity < book.getStock()) {
                    quantity++;
                    tvQty.setText(String.valueOf(quantity));
                } else {
                    Toast.makeText(BookDetailActivity.this,
                            getString(R.string.msg_exceed_stock, book.getStock()), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
            }
        });
    }

    // Thêm sách vào giỏ của người dùng đang đăng nhập
    private void addToCart() {
        int userId = new SessionManager(this).getUserId();
        int result = DatabaseHelper.getInstance(this).addToCart(userId, book.getId(), quantity);

        if (result == DatabaseHelper.ADD_SUCCESS) {
            Toast.makeText(this, R.string.msg_added_to_cart, Toast.LENGTH_SHORT).show();
        } else if (result == DatabaseHelper.ADD_EXCEEDS_STOCK) {
            // Trong giỏ đã có sẵn một số cuốn nên tổng vượt tồn kho
            Toast.makeText(this, getString(R.string.msg_exceed_stock, book.getStock()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.msg_cart_error, Toast.LENGTH_SHORT).show();
        }
    }

    // Bấm mũi tên quay lại trên thanh tiêu đề
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_QUANTITY, quantity);
    }
}