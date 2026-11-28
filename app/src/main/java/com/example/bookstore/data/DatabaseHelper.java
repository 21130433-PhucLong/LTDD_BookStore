package com.example.bookstore.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.CartItem;
import com.example.bookstore.model.Category;
import com.example.bookstore.model.Order;
import com.example.bookstore.model.OrderItem;
import com.example.bookstore.model.Review;
import com.example.bookstore.model.User;
import com.example.bookstore.model.Voucher;
import com.example.bookstore.util.PasswordUtil;
import com.example.bookstore.util.TextUtil;
import com.example.bookstore.util.VoucherUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "bookstore.db";

    // Phiên bản 6: reviews, vouchers, giảm giá trong orders
    private static final int DB_VERSION = 6;

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

    // ---- Bảng orders ----
    public static final String TABLE_ORDERS = "orders";
    public static final String COL_ORDER_ID = "id";
    public static final String COL_ORDER_USER = "user_id";
    public static final String COL_ORDER_TOTAL = "total";
    public static final String COL_ORDER_SHIPPING = "shipping_fee";
    public static final String COL_ORDER_STATUS = "status";
    public static final String COL_ORDER_RECEIVER = "receiver_name";
    public static final String COL_ORDER_PHONE = "phone";
    public static final String COL_ORDER_ADDRESS = "address";
    public static final String COL_ORDER_PAYMENT = "payment_method";
    public static final String COL_ORDER_NOTE = "note";
    public static final String COL_ORDER_CANCEL_REASON = "cancel_reason";
    public static final String COL_ORDER_VOUCHER = "voucher_code";
    public static final String COL_ORDER_DISCOUNT = "discount_amount";
    public static final String COL_ORDER_CREATED = "created_at";

    // ---- Bảng order_items ----
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String COL_OI_ID = "id";
    public static final String COL_OI_ORDER = "order_id";
    public static final String COL_OI_BOOK = "book_id";
    public static final String COL_OI_QTY = "quantity";
    public static final String COL_OI_PRICE = "unit_price";

    // Kết quả của hàm placeOrder: số dương là id đơn mới, số âm là lỗi
    public static final long ORDER_ERR_EMPTY_CART = -1;
    public static final long ORDER_ERR_STOCK = -2;
    public static final long ORDER_ERR_OTHER = -3;
    public static final long ORDER_ERR_VOUCHER = -4;

    // Kết quả của hàm addToCart
    public static final int ADD_SUCCESS = 0;
    public static final int ADD_EXCEEDS_STOCK = 1;
    public static final int ADD_ERROR = 2;

    // Các cách sắp xếp (thứ tự khớp với mảng sort_options trong strings.xml)
    public static final int SORT_NEWEST = 0;
    public static final int SORT_PRICE_ASC = 1;
    public static final int SORT_PRICE_DESC = 2;
    public static final int SORT_BEST_SELLER = 3;

    // Tài khoản quản trị tạo sẵn để thử
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
        seedBooks(db);
        createCartTable(db);
        createOrderTables(db);
        createWishlistTable(db);
        addVoucherColumnsToOrders(db);
        createReviewTable(db);
        createVoucherTable(db);
        seedVouchers(db);
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
        if (oldVersion < 4) {
            createOrderTables(db);
        }
        if (oldVersion < 5) {
            createWishlistTable(db);
        }
        if (oldVersion < 6) {
            addVoucherColumnsToOrders(db);
            createReviewTable(db);
            createVoucherTable(db);
            seedVouchers(db);
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

    // Mỗi người dùng chỉ có một dòng cho mỗi sách; xóa người dùng hoặc sách thì xóa luôn dòng giỏ
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

    // ---------------- Đơn hàng ----------------

    private void createOrderTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " ("
                + COL_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ORDER_USER + " INTEGER NOT NULL, "
                + COL_ORDER_TOTAL + " INTEGER NOT NULL, "
                + COL_ORDER_SHIPPING + " INTEGER NOT NULL DEFAULT 0, "
                + COL_ORDER_STATUS + " TEXT NOT NULL DEFAULT 'pending', "
                + COL_ORDER_RECEIVER + " TEXT NOT NULL, "
                + COL_ORDER_PHONE + " TEXT NOT NULL, "
                + COL_ORDER_ADDRESS + " TEXT NOT NULL, "
                + COL_ORDER_PAYMENT + " TEXT NOT NULL, "
                + COL_ORDER_NOTE + " TEXT, "
                + COL_ORDER_CANCEL_REASON + " TEXT, "
                + COL_ORDER_CREATED + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_ORDER_USER + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))");

        // Xóa đơn thì xóa luôn các dòng chi tiết; sách đã có trong đơn thì không xóa được (chỉ ẩn)
        db.execSQL("CREATE TABLE " + TABLE_ORDER_ITEMS + " ("
                + COL_OI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_OI_ORDER + " INTEGER NOT NULL, "
                + COL_OI_BOOK + " INTEGER NOT NULL, "
                + COL_OI_QTY + " INTEGER NOT NULL, "
                + COL_OI_PRICE + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + COL_OI_ORDER + ") REFERENCES " + TABLE_ORDERS + "(" + COL_ORDER_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (" + COL_OI_BOOK + ") REFERENCES " + TABLE_BOOKS + "(" + COL_BOOK_ID + "))");

        db.execSQL("CREATE INDEX idx_orders_user ON " + TABLE_ORDERS + "(" + COL_ORDER_USER + ")");
        db.execSQL("CREATE INDEX idx_order_items_order ON " + TABLE_ORDER_ITEMS + "(" + COL_OI_ORDER + ")");
    }

    // Câu SELECT đơn hàng kèm tổng số lượng sách
    private static final String ORDER_SELECT = "SELECT o.*, "
            + "(SELECT COALESCE(SUM(oi." + COL_OI_QTY + "), 0) FROM " + TABLE_ORDER_ITEMS
            + " oi WHERE oi." + COL_OI_ORDER + " = o." + COL_ORDER_ID + ") AS item_count"
            + " FROM " + TABLE_ORDERS + " o";

    // Đặt hàng: tạo đơn, tạo các dòng chi tiết, trừ tồn kho, xóa giỏ.
    // Tất cả nằm trong MỘT transaction: một bước lỗi thì mọi thay đổi bị hủy.
    // Trả về id đơn (> 0) hoặc mã lỗi (ORDER_ERR_*)
    public long placeOrder(int userId, String receiverName, String phone, String address,
                           String paymentMethod, String note, String voucherCode) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            List<CartItem> items = getCartItems(userId);
            if (items.isEmpty()) {
                return ORDER_ERR_EMPTY_CART;
            }

            long subtotal = 0;
            for (CartItem item : items) {
                subtotal += item.getSubtotal();
            }
            long shippingFee = Order.calculateShippingFee(subtotal);

            // Kiểm tra lại mã giảm giá tại đây, không tin dữ liệu từ giao diện
            long discount = 0;
            String savedCode = null;
            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                Voucher voucher = getVoucherByCode(voucherCode);
                if (VoucherUtil.check(voucher, subtotal) != VoucherUtil.OK) {
                    return ORDER_ERR_VOUCHER;
                }
                discount = voucher.calculateDiscount(subtotal);
                savedCode = voucher.getCode();
            }

            ContentValues orderValues = new ContentValues();
            orderValues.put(COL_ORDER_USER, userId);
            orderValues.put(COL_ORDER_TOTAL, subtotal + shippingFee - discount);
            orderValues.put(COL_ORDER_SHIPPING, shippingFee);
            orderValues.put(COL_ORDER_STATUS, Order.STATUS_PENDING);
            orderValues.put(COL_ORDER_RECEIVER, receiverName);
            orderValues.put(COL_ORDER_PHONE, phone);
            orderValues.put(COL_ORDER_ADDRESS, address);
            orderValues.put(COL_ORDER_PAYMENT, paymentMethod);
            orderValues.put(COL_ORDER_NOTE, note);
            orderValues.put(COL_ORDER_VOUCHER, savedCode);
            orderValues.put(COL_ORDER_DISCOUNT, discount);
            orderValues.put(COL_ORDER_CREATED, now());
            long orderId = db.insert(TABLE_ORDERS, null, orderValues);
            if (orderId == -1) {
                return ORDER_ERR_OTHER;
            }

            // Câu lệnh biên dịch sẵn: trừ tồn kho chỉ khi còn đủ hàng (stock >= số lượng mua)
            SQLiteStatement updateStock = db.compileStatement(
                    "UPDATE " + TABLE_BOOKS + " SET " + COL_BOOK_STOCK + " = " + COL_BOOK_STOCK + " - ?, "
                            + COL_BOOK_SOLD + " = " + COL_BOOK_SOLD + " + ? WHERE " + COL_BOOK_ID + " = ? AND "
                            + COL_BOOK_STOCK + " >= ?");
            try {
                for (CartItem item : items) {
                    int qty = item.getQuantity();

                    updateStock.clearBindings();
                    updateStock.bindLong(1, qty);
                    updateStock.bindLong(2, qty);
                    updateStock.bindLong(3, item.getBookId());
                    updateStock.bindLong(4, qty);
                    if (updateStock.executeUpdateDelete() != 1) {
                        return ORDER_ERR_STOCK; // không đủ hàng: thoát, transaction không được đánh dấu thành công nên bị hủy
                    }

                    ContentValues lineValues = new ContentValues();
                    lineValues.put(COL_OI_ORDER, orderId);
                    lineValues.put(COL_OI_BOOK, item.getBookId());
                    lineValues.put(COL_OI_QTY, qty);
                    lineValues.put(COL_OI_PRICE, item.getBook().getPrice());
                    if (db.insert(TABLE_ORDER_ITEMS, null, lineValues) == -1) {
                        return ORDER_ERR_OTHER;
                    }
                }
            } finally {
                updateStock.close();
            }

            db.delete(TABLE_CART, COL_CART_USER + " = ?", new String[]{String.valueOf(userId)});

            db.setTransactionSuccessful(); // mọi bước thành công
            return orderId;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi đặt hàng", e);
            return ORDER_ERR_OTHER;
        } finally {
            db.endTransaction(); // chưa setTransactionSuccessful thì toàn bộ thay đổi bị hủy
        }
    }

    // Lấy một đơn hàng theo id, không có thì trả về null
    public Order getOrderById(int orderId) {
        Cursor cursor = getReadableDatabase().rawQuery(
                ORDER_SELECT + " WHERE o." + COL_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)});
        try {
            return cursor.moveToFirst() ? cursorToOrder(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    // Đổi một dòng Cursor thành đối tượng Order
    private Order cursorToOrder(Cursor cursor) {
        Order order = new Order();
        order.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_ID)));
        order.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ORDER_USER)));
        order.setTotal(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ORDER_TOTAL)));
        order.setShippingFee(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ORDER_SHIPPING)));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_STATUS)));
        order.setReceiverName(cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_RECEIVER)));
        order.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_PHONE)));
        order.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_ADDRESS)));
        order.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_PAYMENT)));
        order.setNote(cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_NOTE)));
        order.setCancelReason(cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_CANCEL_REASON)));
        order.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_CREATED)));
        int countIndex = cursor.getColumnIndex("item_count");
        if (countIndex >= 0) {
            order.setItemCount(cursor.getInt(countIndex));
        }
        int nameIndex = cursor.getColumnIndex("customer_name");
        if (nameIndex >= 0) {
            order.setCustomerName(cursor.getString(nameIndex));
        }
        int voucherIndex = cursor.getColumnIndex(COL_ORDER_VOUCHER);
        if (voucherIndex >= 0) {
            order.setVoucherCode(cursor.getString(voucherIndex));
        }
        int discountIndex = cursor.getColumnIndex(COL_ORDER_DISCOUNT);
        if (discountIndex >= 0) {
            order.setDiscountAmount(cursor.getLong(discountIndex));
        }
        return order;
    }

    // Danh sách đơn của một người dùng. status rỗng: mọi trạng thái; newestFirst: mới nhất trước
    public List<Order> getOrdersByUser(int userId, String status, boolean newestFirst) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(ORDER_SELECT + " WHERE o." + COL_ORDER_USER + " = ?");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));
        if (status != null && !status.isEmpty()) {
            sql.append(" AND o.").append(COL_ORDER_STATUS).append(" = ?");
            args.add(status);
        }
        String direction = newestFirst ? "DESC" : "ASC";
        sql.append(" ORDER BY o.").append(COL_ORDER_CREATED).append(" ").append(direction)
                .append(", o.").append(COL_ORDER_ID).append(" ").append(direction);

        Cursor cursor = getReadableDatabase().rawQuery(sql.toString(), args.toArray(new String[0]));
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToOrder(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    // Các dòng chi tiết của một đơn, kèm tên và ảnh sách (INNER JOIN)
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.*, b." + COL_BOOK_TITLE + " AS book_title, b." + COL_BOOK_IMAGE + " AS book_image"
                + " FROM " + TABLE_ORDER_ITEMS + " oi"
                + " INNER JOIN " + TABLE_BOOKS + " b ON b." + COL_BOOK_ID + " = oi." + COL_OI_BOOK
                + " WHERE oi." + COL_OI_ORDER + " = ? ORDER BY oi." + COL_OI_ID;
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(orderId)});
        try {
            while (cursor.moveToNext()) {
                OrderItem item = new OrderItem();
                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_OI_ID)));
                item.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_OI_ORDER)));
                item.setBookId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_OI_BOOK)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COL_OI_QTY)));
                item.setUnitPrice(cursor.getLong(cursor.getColumnIndexOrThrow(COL_OI_PRICE)));
                item.setBookTitle(cursor.getString(cursor.getColumnIndexOrThrow("book_title")));
                item.setBookImage(cursor.getString(cursor.getColumnIndexOrThrow("book_image")));
                list.add(item);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    // ---------------- Hồ sơ cá nhân ----------------

    // Kết quả đổi mật khẩu
    public static final int PW_OK = 0;
    public static final int PW_WRONG_OLD = 1;
    public static final int PW_ERROR = 2;

    // Cập nhật họ tên, số điện thoại, địa chỉ (email không đổi)
    public boolean updateProfile(int userId, String name, String phone, String address) {
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name.trim());
        values.put(COL_USER_PHONE, phone.trim());
        values.put(COL_USER_ADDRESS, address.trim());
        int rows = getWritableDatabase().update(TABLE_USERS, values,
                COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    // Đổi mật khẩu: kiểm tra mật khẩu cũ, rồi lưu salt mới và hash mới
    public int changePassword(int userId, String oldPassword, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();
        String salt;
        String savedHash;
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_USER_SALT + ", " + COL_USER_HASH + " FROM " + TABLE_USERS
                        + " WHERE " + COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        try {
            if (!cursor.moveToFirst()) {
                return PW_ERROR;
            }
            salt = cursor.getString(0);
            savedHash = cursor.getString(1);
        } finally {
            cursor.close();
        }

        if (!PasswordUtil.hash(oldPassword, salt).equals(savedHash)) {
            return PW_WRONG_OLD;
        }

        String newSalt = PasswordUtil.generateSalt();
        ContentValues values = new ContentValues();
        values.put(COL_USER_SALT, newSalt);
        values.put(COL_USER_HASH, PasswordUtil.hash(newPassword, newSalt));
        int rows = db.update(TABLE_USERS, values, COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        return rows > 0 ? PW_OK : PW_ERROR;
    }

    // ---------------- Quản trị: sách ----------------

    public Book getBookById(int bookId) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_BOOKS + " WHERE " + COL_BOOK_ID + " = ?",
                new String[]{String.valueOf(bookId)});
        try {
            return cursor.moveToFirst() ? cursorToBook(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    // Danh sách cho quản trị viên: gồm cả sách đang ẩn, mới nhất trước
    public List<Book> getAllBooksForAdmin(String keyword) {
        List<Book> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_BOOKS + " WHERE 1 = 1");
        List<String> args = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND ").append(COL_BOOK_SEARCH).append(" LIKE ?");
            args.add("%" + TextUtil.toSearchText(keyword) + "%");
        }
        sql.append(" ORDER BY ").append(COL_BOOK_ID).append(" DESC");
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

    // Sửa thông tin sách. Không đụng tới số đã bán, điểm sao, ngày tạo.
    public boolean updateBook(Book book) {
        ContentValues values = new ContentValues();
        values.put(COL_BOOK_TITLE, book.getTitle());
        values.put(COL_BOOK_AUTHOR, book.getAuthor());
        values.put(COL_BOOK_PUBLISHER, book.getPublisher());
        values.put(COL_BOOK_PRICE, book.getPrice());
        values.put(COL_BOOK_ORIGINAL_PRICE, book.getOriginalPrice());
        values.put(COL_BOOK_STOCK, book.getStock());
        values.put(COL_BOOK_DESC, book.getDescription());
        values.put(COL_BOOK_IMAGE, book.getImage());
        values.put(COL_BOOK_CATEGORY, book.getCategoryId());
        values.put(COL_BOOK_HIDDEN, book.isHidden() ? 1 : 0);
        values.put(COL_BOOK_SEARCH, TextUtil.toSearchText(book.getTitle() + " " + book.getAuthor()));
        int rows = getWritableDatabase().update(TABLE_BOOKS, values,
                COL_BOOK_ID + " = ?", new String[]{String.valueOf(book.getId())});
        return rows > 0;
    }

    // ---------------- Quản trị: đơn hàng ----------------

    // Kết quả của changeOrderStatus
    public static final int CHANGE_OK = 0;
    public static final int CHANGE_INVALID = 1;
    public static final int CHANGE_ERROR = 2;

    // Đơn của mọi khách kèm tên khách (INNER JOIN users). status rỗng: mọi trạng thái
    public List<Order> getAllOrders(String status, boolean newestFirst) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT o.*, u." + COL_USER_NAME + " AS customer_name, "
                + "(SELECT COALESCE(SUM(oi." + COL_OI_QTY + "), 0) FROM " + TABLE_ORDER_ITEMS
                + " oi WHERE oi." + COL_OI_ORDER + " = o." + COL_ORDER_ID + ") AS item_count"
                + " FROM " + TABLE_ORDERS + " o"
                + " INNER JOIN " + TABLE_USERS + " u ON u." + COL_USER_ID + " = o." + COL_ORDER_USER
                + " WHERE 1 = 1");
        List<String> args = new ArrayList<>();
        if (status != null && !status.isEmpty()) {
            sql.append(" AND o.").append(COL_ORDER_STATUS).append(" = ?");
            args.add(status);
        }
        String direction = newestFirst ? "DESC" : "ASC";
        sql.append(" ORDER BY o.").append(COL_ORDER_CREATED).append(" ").append(direction)
                .append(", o.").append(COL_ORDER_ID).append(" ").append(direction);
        Cursor cursor = getReadableDatabase().rawQuery(sql.toString(), args.toArray(new String[0]));
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToOrder(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public int countOrdersByStatus(String status) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_ORDERS + " WHERE " + COL_ORDER_STATUS + " = ?",
                new String[]{status});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    // Quản trị viên đổi trạng thái đơn theo đúng quy tắc (Order.canTransition).
    // Hủy đơn thì trả lại tồn kho. Toàn bộ trong một transaction.
    public int changeOrderStatus(int orderId, String newStatus, String reason) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Order order = getOrderById(orderId);
            if (order == null) {
                return CHANGE_ERROR;
            }
            if (!Order.canTransition(order.getStatus(), newStatus)) {
                return CHANGE_INVALID;
            }
            ContentValues values = new ContentValues();
            values.put(COL_ORDER_STATUS, newStatus);
            boolean cancelling = Order.STATUS_CANCELLED.equals(newStatus);
            if (cancelling) {
                values.put(COL_ORDER_CANCEL_REASON, reason);
            }
            // Điều kiện status = trạng thái vừa đọc: nếu đơn vừa bị đổi ở nơi khác thì rows = 0
            int rows = db.update(TABLE_ORDERS, values,
                    COL_ORDER_ID + " = ? AND " + COL_ORDER_STATUS + " = ?",
                    new String[]{String.valueOf(orderId), order.getStatus()});
            if (rows != 1) {
                return CHANGE_INVALID;
            }
            if (cancelling) {
                restockOrder(db, orderId);
            }
            db.setTransactionSuccessful();
            return CHANGE_OK;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi đổi trạng thái đơn", e);
            return CHANGE_ERROR;
        } finally {
            db.endTransaction();
        }
    }

    // Trả lại tồn kho và giảm lượt bán cho mọi sách trong đơn (dùng khi hủy đơn)
    private void restockOrder(SQLiteDatabase db, int orderId) {
        for (OrderItem item : getOrderItems(orderId)) {
            db.execSQL("UPDATE " + TABLE_BOOKS + " SET " + COL_BOOK_STOCK + " = " + COL_BOOK_STOCK + " + ?, "
                            + COL_BOOK_SOLD + " = MAX(" + COL_BOOK_SOLD + " - ?, 0) WHERE " + COL_BOOK_ID + " = ?",
                    new Object[]{item.getQuantity(), item.getQuantity(), item.getBookId()});
        }
    }

    // Khách tự hủy đơn: chỉ được khi đơn của mình và còn chờ xác nhận (bản gọn hơn nhờ restockOrder)
    public boolean cancelOrder(int orderId, int userId, String reason) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COL_ORDER_STATUS, Order.STATUS_CANCELLED);
            values.put(COL_ORDER_CANCEL_REASON, reason);
            int rows = db.update(TABLE_ORDERS, values,
                    COL_ORDER_ID + " = ? AND " + COL_ORDER_USER + " = ? AND " + COL_ORDER_STATUS + " = ?",
                    new String[]{String.valueOf(orderId), String.valueOf(userId), Order.STATUS_PENDING});
            if (rows != 1) {
                return false;
            }
            restockOrder(db, orderId);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi hủy đơn", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // ---------------- Yêu thích ----------------

    // Khóa chính ghép (user_id, book_id): mỗi người chỉ yêu thích một cuốn một lần
    private void createWishlistTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE wishlist ("
                + "user_id INTEGER NOT NULL, "
                + "book_id INTEGER NOT NULL, "
                + "created_at TEXT NOT NULL, "
                + "PRIMARY KEY (user_id, book_id), "
                + "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (book_id) REFERENCES " + TABLE_BOOKS + "(" + COL_BOOK_ID + ") ON DELETE CASCADE)");
    }

    public boolean isInWishlist(int userId, int bookId) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT 1 FROM wishlist WHERE user_id = ? AND book_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(bookId)});
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // Đang yêu thích thì bỏ, chưa thì thêm; trả về trạng thái mới (true = đang yêu thích)
    public boolean toggleWishlist(int userId, int bookId) {
        SQLiteDatabase db = getWritableDatabase();
        if (isInWishlist(userId, bookId)) {
            db.delete("wishlist", "user_id = ? AND book_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(bookId)});
            return false;
        }
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("book_id", bookId);
        values.put("created_at", now());
        db.insert("wishlist", null, values);
        return true;
    }

    // Tập id các sách người dùng đã yêu thích (để vẽ trái tim trên danh sách)
    public Set<Integer> getWishlistBookIds(int userId) {
        Set<Integer> ids = new HashSet<>();
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT book_id FROM wishlist WHERE user_id = ?", new String[]{String.valueOf(userId)});
        try {
            while (cursor.moveToNext()) {
                ids.add(cursor.getInt(0));
            }
        } finally {
            cursor.close();
        }
        return ids;
    }

    // Sách yêu thích (bỏ qua sách đang ẩn), mới thêm hiện trước
    public List<Book> getWishlistBooks(int userId) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT b.* FROM wishlist w INNER JOIN " + TABLE_BOOKS + " b ON b." + COL_BOOK_ID + " = w.book_id"
                + " WHERE w.user_id = ? AND b." + COL_BOOK_HIDDEN + " = 0"
                + " ORDER BY w.created_at DESC, w.rowid DESC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)});
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToBook(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    // ---------------- Đánh giá sách ----------------

    // Mỗi người đánh giá một sách tối đa một lần (UNIQUE); xóa sách hoặc người dùng thì xóa đánh giá
    private void createReviewTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE reviews ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "book_id INTEGER NOT NULL, "
                + "user_id INTEGER NOT NULL, "
                + "rating INTEGER NOT NULL, "
                + "comment TEXT, "
                + "created_at TEXT NOT NULL, "
                + "UNIQUE (book_id, user_id), "
                + "FOREIGN KEY (book_id) REFERENCES " + TABLE_BOOKS + "(" + COL_BOOK_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE)");
    }

    // Chỉ người có đơn "Đã giao" chứa cuốn sách này mới được đánh giá
    public boolean canReview(int userId, int bookId) {
        String sql = "SELECT 1 FROM " + TABLE_ORDER_ITEMS + " oi"
                + " INNER JOIN " + TABLE_ORDERS + " o ON o." + COL_ORDER_ID + " = oi." + COL_OI_ORDER
                + " WHERE o." + COL_ORDER_USER + " = ? AND oi." + COL_OI_BOOK + " = ?"
                + " AND o." + COL_ORDER_STATUS + " = ? LIMIT 1";
        Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(userId), String.valueOf(bookId), Order.STATUS_DELIVERED});
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // Đánh giá của một sách kèm tên người viết, mới nhất trước
    public List<Review> getReviewsByBook(int bookId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, u." + COL_USER_NAME + " AS user_name FROM reviews r"
                + " INNER JOIN " + TABLE_USERS + " u ON u." + COL_USER_ID + " = r.user_id"
                + " WHERE r.book_id = ? ORDER BY r.created_at DESC, r.id DESC";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(bookId)});
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToReview(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    // Đánh giá của chính người dùng về sách này, chưa có thì trả về null
    public Review getUserReview(int userId, int bookId) {
        String sql = "SELECT r.*, u." + COL_USER_NAME + " AS user_name FROM reviews r"
                + " INNER JOIN " + TABLE_USERS + " u ON u." + COL_USER_ID + " = r.user_id"
                + " WHERE r.user_id = ? AND r.book_id = ?";
        Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{String.valueOf(userId), String.valueOf(bookId)});
        try {
            return cursor.moveToFirst() ? cursorToReview(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    private Review cursorToReview(Cursor cursor) {
        Review review = new Review();
        review.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        review.setBookId(cursor.getInt(cursor.getColumnIndexOrThrow("book_id")));
        review.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
        review.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));
        review.setRating(cursor.getInt(cursor.getColumnIndexOrThrow("rating")));
        review.setComment(cursor.getString(cursor.getColumnIndexOrThrow("comment")));
        review.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return review;
    }

    // Lưu đánh giá (chưa có thì thêm, có rồi thì sửa) rồi tính lại số sao trung bình của sách.
    // Cả hai việc nằm trong một transaction.
    public boolean saveReview(int userId, int bookId, int rating, String comment) {
        if (!canReview(userId, bookId)) {
            return false; // phòng trường hợp gọi khi chưa nhận hàng
        }
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("rating", rating);
            values.put("comment", comment);
            int rows = db.update("reviews", values, "user_id = ? AND book_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(bookId)});
            if (rows == 0) {
                values.put("user_id", userId);
                values.put("book_id", bookId);
                values.put("created_at", now());
                if (db.insert("reviews", null, values) == -1) {
                    return false;
                }
            }
            // Số sao của sách = trung bình các đánh giá
            db.execSQL("UPDATE " + TABLE_BOOKS + " SET " + COL_BOOK_RATING
                            + " = (SELECT AVG(rating) FROM reviews WHERE book_id = ?) WHERE " + COL_BOOK_ID + " = ?",
                    new Object[]{bookId, bookId});
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lưu đánh giá", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // ---------------- Mã giảm giá ----------------

    // Thêm cột giảm giá cho bảng orders (đơn cũ có mặc định không giảm)
    private void addVoucherColumnsToOrders(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + TABLE_ORDERS + " ADD COLUMN " + COL_ORDER_VOUCHER + " TEXT");
        db.execSQL("ALTER TABLE " + TABLE_ORDERS + " ADD COLUMN " + COL_ORDER_DISCOUNT + " INTEGER NOT NULL DEFAULT 0");
    }

    private void createVoucherTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE vouchers ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "code TEXT NOT NULL UNIQUE, "
                + "description TEXT, "
                + "discount_percent INTEGER NOT NULL, "
                + "max_discount INTEGER NOT NULL, "
                + "min_order INTEGER NOT NULL DEFAULT 0, "
                + "is_active INTEGER NOT NULL DEFAULT 1, "
                + "expired_at TEXT NOT NULL)");
    }

    // Mã mẫu; TET2026 đã hết hạn để thử trường hợp mã hết hạn
    private void seedVouchers(SQLiteDatabase db) {
        insertVoucherRow(db, "WELCOME10", "Chào mừng khách mới", 10, 30000, 100000, "2027-12-31");
        insertVoucherRow(db, "SACH20", "Ưu đãi đơn lớn", 20, 80000, 250000, "2027-12-31");
        insertVoucherRow(db, "TET2026", "Mã đã hết hạn (dùng để thử)", 15, 50000, 100000, "2026-02-26");
    }

    private void insertVoucherRow(SQLiteDatabase db, String code, String description,
                                  int percent, long maxDiscount, long minOrder, String expiredAt) {
        ContentValues values = new ContentValues();
        values.put("code", code);
        values.put("description", description);
        values.put("discount_percent", percent);
        values.put("max_discount", maxDiscount);
        values.put("min_order", minOrder);
        values.put("is_active", 1);
        values.put("expired_at", expiredAt);
        db.insert("vouchers", null, values);
    }

    // Tìm mã theo nội dung người dùng nhập (không phân biệt hoa thường), không có thì trả về null
    public Voucher getVoucherByCode(String code) {
        if (code == null) {
            return null;
        }
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM vouchers WHERE code = ?",
                new String[]{code.trim().toUpperCase(Locale.ROOT)});
        try {
            return cursor.moveToFirst() ? cursorToVoucher(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    // Các mã đang bật và chưa hết hạn, để gợi ý cho khách
    public List<Voucher> getActiveVouchers() {
        List<Voucher> list = new ArrayList<>();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM vouchers WHERE is_active = 1 AND expired_at >= ? ORDER BY min_order",
                new String[]{today});
        try {
            while (cursor.moveToNext()) {
                list.add(cursorToVoucher(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private Voucher cursorToVoucher(Cursor cursor) {
        Voucher voucher = new Voucher();
        voucher.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        voucher.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
        voucher.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        voucher.setDiscountPercent(cursor.getInt(cursor.getColumnIndexOrThrow("discount_percent")));
        voucher.setMaxDiscount(cursor.getLong(cursor.getColumnIndexOrThrow("max_discount")));
        voucher.setMinOrder(cursor.getLong(cursor.getColumnIndexOrThrow("min_order")));
        voucher.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1);
        voucher.setExpiredAt(cursor.getString(cursor.getColumnIndexOrThrow("expired_at")));
        return voucher;
    }
}