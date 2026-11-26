package com.example.bookstore.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Category;
import com.example.bookstore.util.AdminUtil;
import com.example.bookstore.util.ImageUtil;
import com.example.bookstore.util.UiUtil;
import com.example.bookstore.util.WindowUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class BookEditActivity extends AppCompatActivity {

    // Có id sách: chế độ sửa. Không có: chế độ thêm mới
    public static final String EXTRA_BOOK_ID = "book_id";

    private static final int COVER_COUNT = 16;

    private TextInputLayout tilTitle;
    private TextInputLayout tilAuthor;
    private TextInputLayout tilPrice;
    private TextInputLayout tilOriginalPrice;
    private TextInputLayout tilStock;
    private EditText edtTitle;
    private EditText edtAuthor;
    private EditText edtPublisher;
    private EditText edtPrice;
    private EditText edtOriginalPrice;
    private EditText edtStock;
    private EditText edtDescription;
    private Spinner spinnerCategory;
    private Spinner spinnerCover;
    private ImageView imgCoverPreview;
    private SwitchCompat swVisible;

    private DatabaseHelper db;
    private List<Category> categories;
    private String[] coverNames;
    private int bookId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AdminUtil.isCurrentUserAdmin(this)) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_edit);
        WindowUtil.applySystemBarsPadding(findViewById(R.id.main), false);
        WindowUtil.applyBottomInsetPadding(findViewById(R.id.contentLayout));

        bookId = getIntent().getIntExtra(EXTRA_BOOK_ID, -1);
        UiUtil.setupBackToolbar(this, bookId == -1 ? R.string.title_add_book : R.string.title_edit_book);

        db = DatabaseHelper.getInstance(this);
        initViews();
        setupSpinners();

        // Chế độ sửa: điền sẵn dữ liệu khi mở mới (xoay màn hình thì giữ chữ đang sửa)
        if (bookId != -1 && savedInstanceState == null) {
            Book book = db.getBookById(bookId);
            if (book == null) {
                Toast.makeText(this, R.string.msg_book_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            fillForm(book);
        }

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    private void initViews() {
        tilTitle = findViewById(R.id.tilTitle);
        tilAuthor = findViewById(R.id.tilAuthor);
        tilPrice = findViewById(R.id.tilPrice);
        tilOriginalPrice = findViewById(R.id.tilOriginalPrice);
        tilStock = findViewById(R.id.tilStock);
        edtTitle = findViewById(R.id.edtTitle);
        edtAuthor = findViewById(R.id.edtAuthor);
        edtPublisher = findViewById(R.id.edtPublisher);
        edtPrice = findViewById(R.id.edtPrice);
        edtOriginalPrice = findViewById(R.id.edtOriginalPrice);
        edtStock = findViewById(R.id.edtStock);
        edtDescription = findViewById(R.id.edtDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerCover = findViewById(R.id.spinnerCover);
        imgCoverPreview = findViewById(R.id.imgCoverPreview);
        swVisible = findViewById(R.id.swVisible);
    }

    // Ô chọn thể loại (đọc từ database) và ô chọn ảnh bìa (có xem trước)
    private void setupSpinners() {
        categories = db.getAllCategories();
        List<String> names = new ArrayList<>();
        for (Category category : categories) {
            names.add(category.getName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, names);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        coverNames = new String[COVER_COUNT];
        for (int i = 0; i < COVER_COUNT; i++) {
            coverNames[i] = String.format(Locale.US, "book_%02d", i + 1);
        }
        ArrayAdapter<String> coverAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, coverNames);
        coverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCover.setAdapter(coverAdapter);

        // Chọn ảnh nào thì xem trước ảnh đó
        spinnerCover.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                imgCoverPreview.setImageResource(ImageUtil.getDrawableId(BookEditActivity.this, coverNames[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Điền dữ liệu của sách đang sửa vào form
    private void fillForm(Book book) {
        edtTitle.setText(book.getTitle());
        edtAuthor.setText(book.getAuthor());
        edtPublisher.setText(book.getPublisher());
        edtPrice.setText(String.valueOf(book.getPrice()));
        // Giá gốc bằng giá bán thì để trống cho gọn
        if (book.getOriginalPrice() != book.getPrice()) {
            edtOriginalPrice.setText(String.valueOf(book.getOriginalPrice()));
        }
        edtStock.setText(String.valueOf(book.getStock()));
        edtDescription.setText(book.getDescription());
        swVisible.setChecked(!book.isHidden());

        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == book.getCategoryId()) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < coverNames.length; i++) {
            if (coverNames[i].equals(book.getImage())) {
                spinnerCover.setSelection(i);
                break;
            }
        }
    }

    // Đọc số nguyên không âm tối đa 9 chữ số; sai định dạng trả về -1
    private long parseNumber(String text) {
        if (text.isEmpty() || text.length() > 9) {
            return -1;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Kiểm tra từng ô theo thứ tự, gặp lỗi đầu tiên thì dừng; hợp lệ thì lưu
    private void save() {
        tilTitle.setError(null);
        tilAuthor.setError(null);
        tilPrice.setError(null);
        tilOriginalPrice.setError(null);
        tilStock.setError(null);

        String title = edtTitle.getText().toString().trim();
        String author = edtAuthor.getText().toString().trim();
        String publisher = edtPublisher.getText().toString().trim();
        String priceText = edtPrice.getText().toString().trim();
        String originalText = edtOriginalPrice.getText().toString().trim();
        String stockText = edtStock.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        if (title.isEmpty()) {
            tilTitle.setError(getString(R.string.error_book_title));
            edtTitle.requestFocus();
            return;
        }
        if (author.isEmpty()) {
            tilAuthor.setError(getString(R.string.error_book_author));
            edtAuthor.requestFocus();
            return;
        }

        long price = parseNumber(priceText);
        if (price < 1000 || price > 99999999) {
            tilPrice.setError(getString(R.string.error_book_price));
            edtPrice.requestFocus();
            return;
        }

        // Giá gốc để trống thì bằng giá bán (không giảm giá)
        long originalPrice = originalText.isEmpty() ? price : parseNumber(originalText);
        if (originalPrice < price) {
            tilOriginalPrice.setError(getString(R.string.error_book_original_price));
            edtOriginalPrice.requestFocus();
            return;
        }

        long stock = parseNumber(stockText);
        if (stock < 0 || stock > 99999) {
            tilStock.setError(getString(R.string.error_book_stock));
            edtStock.requestFocus();
            return;
        }

        Book book = new Book();
        book.setId(bookId);
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setPrice(price);
        book.setOriginalPrice(originalPrice);
        book.setStock((int) stock);
        book.setDescription(description);
        book.setImage(coverNames[spinnerCover.getSelectedItemPosition()]);
        book.setCategoryId(categories.get(spinnerCategory.getSelectedItemPosition()).getId());
        book.setHidden(!swVisible.isChecked());

        boolean ok;
        if (bookId == -1) {
            ok = db.insertBook(book) > 0;
        } else {
            ok = db.updateBook(book);
        }

        if (ok) {
            Toast.makeText(this, R.string.msg_book_saved, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.msg_book_save_failed, Toast.LENGTH_SHORT).show();
        }
    }
}