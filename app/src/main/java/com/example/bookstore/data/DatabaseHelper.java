package com.example.bookstore.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.Category;
import com.example.bookstore.model.User;
import com.example.bookstore.util.PasswordUtil;
import com.example.bookstore.util.TextUtil;
import com.example.bookstore.model.CartItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "bookstore.db";

    private static final int DB_VERSION = 3;

    // ---- Bảng users ----
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PHONE = "phone";
    public static final String COL_USER_ADDRESS = "address";
    public static final String COL_USER_ROLE = "role";
    public static final String COL_USER_HASH = "password_hash";
    public static final String COL_USER_SALT = "salt";
    public static final String COL_USER_CREATED = "created_at";

    // ---- Bảng categories ----
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_CAT_ID = "id";
    public static final String COL_CAT_NAME = "name";

    // ---- Bảng books ----
    public static final String TABLE_BOOKS = "books";
    public static final String COL_BOOK_ID = "id";
    public static final String COL_BOOK_TITLE = "title";
    public static final String COL_BOOK_AUTHOR = "author";
    public static final String COL_BOOK_PUBLISHER = "publisher";
    public static final String COL_BOOK_PRICE = "price";
    public static final String COL_BOOK_ORIGINAL_PRICE = "original_price";
    public static final String COL_BOOK_STOCK = "stock";
    public static final String COL_BOOK_SOLD = "sold";
    public static final String COL_BOOK_RATING = "rating";
    public static final String COL_BOOK_DESC = "description";
    public static final String COL_BOOK_IMAGE = "image";
    public static final String COL_BOOK_CATEGORY = "category_id";
    public static final String COL_BOOK_HIDDEN = "is_hidden";
    public static final String COL_BOOK_SEARCH = "search_text"; // tên + tác giả đã bỏ dấu, dùng để tìm kiếm
    public static final String COL_BOOK_CREATED = "created_at";

    // ---- Bảng cart_items ----
    public static final String TABLE_CART = "cart_items";
    public static final String COL_CART_ID = "id";
    public static final String COL_CART_USER = "user_id";
    public static final String COL_CART_BOOK = "book_id";
    public static final String COL_CART_QTY = "quantity";

    // Kết quả của hàm addToCart
    public static final int ADD_SUCCESS = 0;
    public static final int ADD_EXCEEDS_STOCK = 1;
    public static final int ADD_ERROR = 2;

    // Các cách sắp xếp (thứ tự khớp với mảng sort_options trong strings.xml)
    public static final int SORT_NEWEST = 0;
    public static final int SORT_PRICE_ASC = 1;
    public static final int SORT_PRICE_DESC = 2;
    public static final int SORT_BEST_SELLER = 3;

    // Tài khoản quản trị tạo sẵn để thử (chỉ dùng khi học)
    public static final String ADMIN_EMAIL = "admin@bookstore.com";
    private static final String ADMIN_PASSWORD = "Admin123";

    private static DatabaseHelper instance;

    // Chỉ dùng một đối tượng DatabaseHelper cho cả app để tránh lỗi "database is locked"
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // onCreate và onUpgrade đều được SQLiteOpenHelper chạy trong một transaction,
    // nếu có lỗi thì mọi thay đổi bị hủy
    @Override
    public void onCreate(SQLiteDatabase db) {
        createUserTable(db);
        insertDefaultAdmin(db);
        createBookTables(db);
        createCartTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nâng cấp từng bước và giữ nguyên dữ liệu cũ
        if (oldVersion < 2) {
            createBookTables(db);
            seedBooks(db);
        }
        if (oldVersion < 3) {
            createCartTable(db);
        }
    }

    private void createUserTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_NAME + " TEXT NOT NULL, "
                + COL_USER_EMAIL + " TEXT NOT NULL UNIQUE, "
                + COL_USER_PHONE + " TEXT, "
                + COL_USER_ADDRESS + " TEXT, "
                + COL_USER_ROLE + " TEXT NOT NULL DEFAULT 'customer', "
                + COL_USER_HASH + " TEXT NOT NULL, "
                + COL_USER_SALT + " TEXT NOT NULL, "
                + COL_USER_CREATED + " TEXT)");
    }

    private void createBookTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " ("
                + COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CAT_NAME + " TEXT NOT NULL UNIQUE)");

        db.execSQL("CREATE TABLE " + TABLE_BOOKS + " ("
                + COL_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_BOOK_TITLE + " TEXT NOT NULL, "
                + COL_BOOK_AUTHOR + " TEXT NOT NULL, "
                + COL_BOOK_PUBLISHER + " TEXT, "
                + COL_BOOK_PRICE + " INTEGER NOT NULL, "
                + COL_BOOK_ORIGINAL_PRICE + " INTEGER NOT NULL, "
                + COL_BOOK_STOCK + " INTEGER NOT NULL DEFAULT 0, "
                + COL_BOOK_SOLD + " INTEGER NOT NULL DEFAULT 0, "
                + COL_BOOK_RATING + " REAL NOT NULL DEFAULT 0, "
                + COL_BOOK_DESC + " TEXT, "
                + COL_BOOK_IMAGE + " TEXT, "
                + COL_BOOK_CATEGORY + " INTEGER NOT NULL, "
                + COL_BOOK_HIDDEN + " INTEGER NOT NULL DEFAULT 0, "
                + COL_BOOK_SEARCH + " TEXT, "
                + COL_BOOK_CREATED + " TEXT, "
                + "FOREIGN KEY (" + COL_BOOK_CATEGORY + ") REFERENCES "
                + TABLE_CATEGORIES + "(" + COL_CAT_ID + "))");

        // Chỉ mục giúp lọc theo thể loại nhanh hơn
        db.execSQL("CREATE INDEX idx_books_category ON " + TABLE_BOOKS + "(" + COL_BOOK_CATEGORY + ")");
    }

    private void seedBooks(SQLiteDatabase db) {
        String[] names = SeedData.CATEGORY_NAMES;
        for (int i = 0; i < names.length; i++) {
            ContentValues values = new ContentValues();
            values.put(COL_CAT_ID, i + 1);
            values.put(COL_CAT_NAME, names[i]);
            db.insert(TABLE_CATEGORIES, null, values);
        }
        for (Book book : SeedData.getBooks()) {
            insertBookRow(db, book);
        }
    }

    // Thêm tài khoản admin khi database được tạo lần đầu
    private void insertDefaultAdmin(SQLiteDatabase db) {
        String salt = PasswordUtil.generateSalt();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, "Quản trị viên");
        values.put(COL_USER_EMAIL, ADMIN_EMAIL);
        values.put(COL_USER_PHONE, "0900000000");
        values.put(COL_USER_ADDRESS, "");
        values.put(COL_USER_ROLE, User.ROLE_ADMIN);
        values.put(COL_USER_SALT, salt);
        values.put(COL_USER_HASH, PasswordUtil.hash(ADMIN_PASSWORD, salt));
        values.put(COL_USER_CREATED, now());
        db.insert(TABLE_USERS, null, values);
    }

    // Email được lưu và so sánh dạng chữ thường
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    //  ---------Người dùng------

    // Kiểm tra email đã được đăng ký chưa
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + " = ?",
                new String[]{normalizeEmail(email)});
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // Thêm khách hàng mới; trả về id, hoặc -1 nếu lỗi
    public long insertUser(String name, String email, String phone, String password) {
        String salt = PasswordUtil.generateSalt();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name.trim());
        values.put(COL_USER_EMAIL, normalizeEmail(email));
        values.put(COL_USER_PHONE, phone);
        values.put(COL_USER_ADDRESS, "");
        values.put(COL_USER_ROLE, User.ROLE_CUSTOMER);
        values.put(COL_USER_SALT, salt);
        values.put(COL_USER_HASH, PasswordUtil.hash(password, salt));
        values.put(COL_USER_CREATED, now());
        return getWritableDatabase().insert(TABLE_USERS, null, values);
    }

    // Kiểm tra đăng nhập; đúng thì trả về User, sai thì trả về null
    public User checkLogin(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_EMAIL + " = ?",
                new String[]{normalizeEmail(email)});
        try {
            if (cursor.moveToFirst()) {
                String salt = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_SALT));
                String savedHash = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_HASH));
                // Băm mật khẩu vừa nhập với salt đã lưu rồi so với hash trong database
                if (PasswordUtil.hash(password, salt).equals(savedHash)) {
                    return cursorToUser(cursor);
                }
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    // Lấy người dùng theo id (dùng khi khôi phục phiên đăng nhập)
    public User getUserById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USER_ID + " = ?",
                new String[]{String.valueOf(id)});
        try {
            if (cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        } finally {
            cursor.close();
        }
    }

    // Đổi một dòng trong Cursor thành đối tượng User
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE)));
        user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ADDRESS)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE)));
        return user;
    }

    // ---------------- Thể loại ----------------

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_CATEGORIES + " ORDER BY " + COL_CAT_ID, null);
        try {
            while (cursor.moveToNext()) {
                Category category = new Category();
                category.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CAT_ID)));
                category.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_NAME)));
                list.add(category);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    // Thể loại kèm số sách đang hiển thị
    public List<Category> getCategoriesWithCount() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT c." + COL_CAT_ID + ", c." + COL_CAT_NAME + ", COUNT(b." + COL_BOOK_ID + ") AS book_count"
                + " FROM " + TABLE_CATEGORIES + " c"
                + " LEFT JOIN " + TABLE_BOOKS + " b ON b." + COL_BOOK_CATEGORY + " = c." + COL_CAT_ID
                + " AND b." + COL_BOOK_HIDDEN + " = 0"
                + " GROUP BY c." + COL_CAT_ID + ", c." + COL_CAT_NAME
                + " ORDER BY c." + COL_CAT_ID;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        try {
            while (cursor.moveToNext()) {
                Category category = new Category();
                category.setId(cursor.getInt(0));
                category.setName(cursor.getString(1));
                category.setBookCount(cursor.getInt(2));
                list.add(category);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    // ---------------- Sách ----------------

    // Thêm một sách vào database
    private long insertBookRow(SQLiteDatabase db, Book book) {
        ContentValues values = new ContentValues();
        values.put(COL_BOOK_TITLE, book.getTitle());
        values.put(COL_BOOK_AUTHOR, book.getAuthor());
        values.put(COL_BOOK_PUBLISHER, book.getPublisher());
        values.put(COL_BOOK_PRICE, book.getPrice());
        values.put(COL_BOOK_ORIGINAL_PRICE, book.getOriginalPrice());
        values.put(COL_BOOK_STOCK, book.getStock());
        values.put(COL_BOOK_SOLD, book.getSold());
        values.put(COL_BOOK_RATING, book.getRating());
        values.put(COL_BOOK_DESC, book.getDescription());
        values.put(COL_BOOK_IMAGE, book.getImage());
        values.put(COL_BOOK_CATEGORY, book.getCategoryId());
        values.put(COL_BOOK_HIDDEN, book.isHidden() ? 1 : 0);
        values.put(COL_BOOK_SEARCH, TextUtil.toSearchText(book.getTitle() + " " + book.getAuthor()));
        values.put(COL_BOOK_CREATED, now());
        return db.insert(TABLE_BOOKS, null, values);
    }

    public long insertBook(Book book) {
        return insertBookRow(getWritableDatabase(), book);
    }

    // Lấy danh sách sách đang hiển thị.
    // categoryId <= 0: mọi thể loại; keyword rỗng: không tìm kiếm
    public List<Book> getBooks(int categoryId, String keyword, int sortMode) {
        List<Book> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_BOOKS
                + " WHERE " + COL_BOOK_HIDDEN + " = 0");
        List<String> args = new ArrayList<>();

        if (categoryId > 0) {
            sql.append(" AND ").append(COL_BOOK_CATEGORY).append(" = ?");
            args.add(String.valueOf(categoryId));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND ").append(COL_BOOK_SEARCH).append(" LIKE ?");
            args.add("%" + TextUtil.toSearchText(keyword) + "%");
        }

        sql.append(" ORDER BY ").append(getOrderBy(sortMode));

        Cursor cursor = getReadableDatabase().rawQuery(sql.toString(), args.toArray(new String[0]));
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToBook(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private String getOrderBy(int sortMode) {
        switch (sortMode) {
            case SORT_PRICE_ASC:
                return COL_BOOK_PRICE + " ASC, " + COL_BOOK_ID + " DESC";
            case SORT_PRICE_DESC:
                return COL_BOOK_PRICE + " DESC, " + COL_BOOK_ID + " DESC";
            case SORT_BEST_SELLER:
                return COL_BOOK_SOLD + " DESC, " + COL_BOOK_ID + " DESC";
            default:
                return COL_BOOK_ID + " DESC"; // mới nhất
        }
    }

    // Đổi một dòng trong Cursor thành đối tượng Book
    private Book cursorToBook(Cursor cursor) {
        Book book = new Book();
        book.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOK_ID)));
        book.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_TITLE)));
        book.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_AUTHOR)));
        book.setPublisher(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_PUBLISHER)));
        book.setPrice(cursor.getLong(cursor.getColumnIndexOrThrow(COL_BOOK_PRICE)));
        book.setOriginalPrice(cursor.getLong(cursor.getColumnIndexOrThrow(COL_BOOK_ORIGINAL_PRICE)));
        book.setStock(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOK_STOCK)));
        book.setSold(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOK_SOLD)));
        book.setRating(cursor.getFloat(cursor.getColumnIndexOrThrow(COL_BOOK_RATING)));
        book.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_DESC)));
        book.setImage(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_IMAGE)));
        book.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOK_CATEGORY)));
        book.setHidden(cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOK_HIDDEN)) == 1);
        book.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOK_CREATED)));
        return book;
    }

    // ---------------- Giỏ hàng ----------------

    // Mỗi người dùng chỉ có một dòng cho mỗi sách (UNIQUE); xóa người dùng hoặc sách thì xóa luôn dòng giỏ
    private void createCartTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CART + " ("
                + COL_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CART_USER + " INTEGER NOT NULL, "
                + COL_CART_BOOK + " INTEGER NOT NULL, "
                + COL_CART_QTY + " INTEGER NOT NULL DEFAULT 1, "
                + "UNIQUE (" + COL_CART_USER + ", " + COL_CART_BOOK + "), "
                + "FOREIGN KEY (" + COL_CART_USER + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + COL_CART_BOOK + ") REFERENCES " + TABLE_BOOKS + "(" + COL_BOOK_ID + ") ON DELETE CASCADE)");
    }

    // Tồn kho của một sách, -1 nếu không có sách đó
    private int getBookStock(SQLiteDatabase db, int bookId) {
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_BOOK_STOCK + " FROM " + TABLE_BOOKS + " WHERE " + COL_BOOK_ID + " = ?",
                new String[]{String.valueOf(bookId)});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : -1;
        } finally {
            cursor.close();
        }
    }

    // Thêm sách vào giỏ: đã có thì cộng dồn số lượng, nhưng tổng không được vượt tồn kho
    public int addToCart(int userId, int bookId, int quantity) {
        SQLiteDatabase db = getWritableDatabase();

        int stock = getBookStock(db, bookId);
        if (stock < 0) {
            return ADD_ERROR;
        }

        // Tìm dòng giỏ hiện có của sách này
        int cartId = -1;
        int currentQty = 0;
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_CART_ID + ", " + COL_CART_QTY + " FROM " + TABLE_CART
                        + " WHERE " + COL_CART_USER + " = ? AND " + COL_CART_BOOK + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(bookId)});
        try {
            if (cursor.moveToFirst()) {
                cartId = cursor.getInt(0);
                currentQty = cursor.getInt(1);
            }
        } finally {
            cursor.close();
        }

        int newQty = currentQty + quantity;
        if (newQty > stock) {
            return ADD_EXCEEDS_STOCK;
        }

        if (cartId == -1) {
            ContentValues values = new ContentValues();
            values.put(COL_CART_USER, userId);
            values.put(COL_CART_BOOK, bookId);
            values.put(COL_CART_QTY, newQty);
            return db.insert(TABLE_CART, null, values) == -1 ? ADD_ERROR : ADD_SUCCESS;
        } else {
            return updateCartQuantity(cartId, newQty) ? ADD_SUCCESS : ADD_ERROR;
        }
    }

    // Đọc giỏ hàng của một người dùng kèm thông tin sách, bỏ qua sách đã bị ẩn
    public List<CartItem> getCartItems(int userId) {
        List<CartItem> list = new ArrayList<>();
        String sql = "SELECT c." + COL_CART_ID + " AS cart_id, c." + COL_CART_QTY + ", b.*"
                + " FROM " + TABLE_CART + " c"
                + " INNER JOIN " + TABLE_BOOKS + " b ON b." + COL_BOOK_ID + " = c." + COL_CART_BOOK
                + " WHERE c." + COL_CART_USER + " = ? AND b." + COL_BOOK_HIDDEN + " = 0"
                + " ORDER BY c." + COL_CART_ID + " DESC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)});
        try {
            while (cursor.moveToNext()) {
                Book book = cursorToBook(cursor);
                CartItem item = new CartItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow("cart_id")));
                item.setUserId(userId);
                item.setBookId(book.getId());
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CART_QTY)));
                item.setBook(book);
                list.add(item);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    // Đổi số lượng của một dòng giỏ
    public boolean updateCartQuantity(int cartId, int quantity) {
        ContentValues values = new ContentValues();
        values.put(COL_CART_QTY, quantity);
        int rows = getWritableDatabase().update(TABLE_CART, values,
                COL_CART_ID + " = ?", new String[]{String.valueOf(cartId)});
        return rows > 0;
    }

    public void deleteCartItem(int cartId) {
        getWritableDatabase().delete(TABLE_CART, COL_CART_ID + " = ?", new String[]{String.valueOf(cartId)});
    }

    // Xóa sạch giỏ của một người dùng
    public void clearCart(int userId) {
        getWritableDatabase().delete(TABLE_CART, COL_CART_USER + " = ?", new String[]{String.valueOf(userId)});
    }

    // Số dòng trong giỏ, dùng cho số nhỏ trên biểu tượng tab Giỏ hàng
    public int getCartCount(int userId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_CART + " c"
                + " INNER JOIN " + TABLE_BOOKS + " b ON b." + COL_BOOK_ID + " = c." + COL_CART_BOOK
                + " WHERE c." + COL_CART_USER + " = ? AND b." + COL_BOOK_HIDDEN + " = 0";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }
}