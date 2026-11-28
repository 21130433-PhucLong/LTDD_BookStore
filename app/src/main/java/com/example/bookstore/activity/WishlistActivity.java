package com.example.bookstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookstore.R;
import com.example.bookstore.adapter.BookGridAdapter;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Book;
import com.example.bookstore.util.SessionManager;
import com.example.bookstore.util.UiUtil;
import com.example.bookstore.util.WindowUtil;

import java.util.List;

public class WishlistActivity extends AppCompatActivity implements BookGridAdapter.OnBookActionListener {

    private RecyclerView rvWishlist;
    private TextView tvEmpty;
    private BookGridAdapter adapter;
    private DatabaseHelper db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wishlist);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), false);
        UiUtil.setupBackToolbar(this, R.string.title_wishlist);

        rvWishlist = findViewById(R.id.rvWishlist);
        tvEmpty = findViewById(R.id.tvEmpty);
        WindowUtil.applyBottomInsetPadding(rvWishlist);

        db = DatabaseHelper.getInstance(this);
        userId = new SessionManager(this).getUserId();

        adapter = new BookGridAdapter(this, this);
        rvWishlist.setLayoutManager(new GridLayoutManager(this, 2));
        rvWishlist.setAdapter(adapter);
    }

    private void loadWishlist() {
        List<Book> books = db.getWishlistBooks(userId);
        adapter.setData(books, db.getWishlistBookIds(userId));
        tvEmpty.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Quay về từ màn chi tiết (có thể vừa bỏ tim) thì tải lại
    @Override
    protected void onResume() {
        super.onResume();
        loadWishlist();
    }

    @Override
    public void onBookClick(Book book) {
        Intent intent = new Intent(this, BookDetailActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_BOOK, book);
        startActivity(intent);
    }

    // Bấm tim ở đây nghĩa là bỏ yêu thích: sách biến mất khỏi danh sách
    @Override
    public void onWishlistClick(Book book, int position) {
        db.toggleWishlist(userId, book.getId());
        loadWishlist();
    }
}