package com.example.bookstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookstore.R;
import com.example.bookstore.adapter.BookAdapter;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Book;
import com.example.bookstore.util.AdminUtil;
import com.example.bookstore.util.UiUtil;
import com.example.bookstore.util.WindowUtil;

import java.util.ArrayList;
import java.util.List;

public class AdminBookListActivity extends AppCompatActivity {

    private EditText edtSearch;
    private TextView tvEmpty;
    private BookAdapter adapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chỉ quản trị viên mới được vào
        if (!AdminUtil.isCurrentUserAdmin(this)) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_book_list);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), false);
        WindowUtil.applyBottomInsetPadding(findViewById(R.id.contentFrame));
        UiUtil.setupBackToolbar(this, R.string.title_admin_books);

        db = DatabaseHelper.getInstance(this);
        edtSearch = findViewById(R.id.edtSearch);
        tvEmpty = findViewById(R.id.tvEmpty);
        ListView lvBooks = findViewById(R.id.lvBooks);

        adapter = new BookAdapter(this, new ArrayList<Book>());
        adapter.setAdminMode(true);
        lvBooks.setAdapter(adapter);

        // Bấm một sách: mở form sửa
        lvBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AdminBookListActivity.this, BookEditActivity.class);
                intent.putExtra(BookEditActivity.EXTRA_BOOK_ID, adapter.getItem(position).getId());
                startActivity(intent);
            }
        });

        // Nút +: mở form thêm sách mới
        findViewById(R.id.fabAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminBookListActivity.this, BookEditActivity.class));
            }
        });

        // Gõ đến đâu lọc đến đó
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadBooks();
            }
        });
    }

    private void loadBooks() {
        List<Book> books = db.getAllBooksForAdmin(edtSearch.getText().toString());
        adapter.setData(books);
        tvEmpty.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Quay về từ form thêm/sửa thì tải lại danh sách
    @Override
    protected void onResume() {
        super.onResume();
        loadBooks();
    }
}