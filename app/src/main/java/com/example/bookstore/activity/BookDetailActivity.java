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
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.appbar.MaterialToolbar;
import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Book;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.ImageUtil;
import com.example.bookstore.util.SessionManager;
import com.example.bookstore.util.WindowUtil;
import com.example.bookstore.model.Review;

import java.util.List;


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
        setupReviews();
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

    // ---------------- Đánh giá ----------------

    private LinearLayout llReviews;
    private TextView tvReviewSummary;
    private TextView tvNoReview;
    private Button btnWriteReview;

    private void setupReviews() {
        llReviews = findViewById(R.id.llReviews);
        tvReviewSummary = findViewById(R.id.tvReviewSummary);
        tvNoReview = findViewById(R.id.tvNoReview);
        btnWriteReview = findViewById(R.id.btnWriteReview);

        btnWriteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReviewDialog();
            }
        });
        loadReviews();
    }

    // Đọc đánh giá từ database và vẽ danh sách; quyết định có hiện nút "Viết đánh giá" hay không
    private void loadReviews() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        int userId = new SessionManager(this).getUserId();

        List<Review> reviews = db.getReviewsByBook(book.getId());
        llReviews.removeAllViews();
        for (Review review : reviews) {
            View item = getLayoutInflater().inflate(R.layout.item_review, llReviews, false);
            ((TextView) item.findViewById(R.id.tvReviewer)).setText(review.getUserName());
            ((RatingBar) item.findViewById(R.id.rbItem)).setRating(review.getRating());
            ((TextView) item.findViewById(R.id.tvReviewDate)).setText(FormatUtil.formatDateTime(review.getCreatedAt()));
            TextView tvComment = item.findViewById(R.id.tvReviewComment);
            if (review.getComment() == null || review.getComment().isEmpty()) {
                tvComment.setVisibility(View.GONE);
            } else {
                tvComment.setText(review.getComment());
            }
            llReviews.addView(item);
        }

        boolean empty = reviews.isEmpty();
        tvNoReview.setVisibility(empty ? View.VISIBLE : View.GONE);
        tvReviewSummary.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty) {
            tvReviewSummary.setText(getString(R.string.review_summary, book.getRating(), reviews.size()));
        }

        // Chỉ người đã nhận sách (có đơn "Đã giao" chứa sách này) mới thấy nút đánh giá
        boolean canReview = db.canReview(userId, book.getId());
        btnWriteReview.setVisibility(canReview ? View.VISIBLE : View.GONE);
        boolean hasReviewed = db.getUserReview(userId, book.getId()) != null;
        btnWriteReview.setText(hasReviewed ? R.string.btn_edit_review : R.string.btn_write_review);
    }

    // Hộp thoại chọn số sao và viết nhận xét; đã đánh giá rồi thì điền sẵn để sửa
    private void showReviewDialog() {
        final DatabaseHelper db = DatabaseHelper.getInstance(this);
        final int userId = new SessionManager(this).getUserId();
        Review existing = db.getUserReview(userId, book.getId());

        View view = getLayoutInflater().inflate(R.layout.dialog_review, null);
        final RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        final EditText edtComment = view.findViewById(R.id.edtReviewComment);
        if (existing != null) {
            ratingBar.setRating(existing.getRating());
            edtComment.setText(existing.getComment());
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_review_title)
                .setView(view)
                .setPositiveButton(R.string.btn_send_review, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int rating = (int) ratingBar.getRating();
                        if (rating < 1) {
                            Toast.makeText(BookDetailActivity.this, R.string.msg_review_choose_star,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String comment = edtComment.getText().toString().trim();
                        if (db.saveReview(userId, book.getId(), rating, comment)) {
                            Toast.makeText(BookDetailActivity.this, R.string.msg_review_saved,
                                    Toast.LENGTH_SHORT).show();
                            // Đọc lại sách để cập nhật số sao trung bình, rồi vẽ lại màn hình
                            Book fresh = db.getBookById(book.getId());
                            if (fresh != null) {
                                book = fresh;
                                showBook();
                            }
                            loadReviews();
                        } else {
                            Toast.makeText(BookDetailActivity.this, R.string.msg_review_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.btn_back, null)
                .show();
    }

}