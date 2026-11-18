package com.example.bookstore.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.bookstore.model.User;
import com.example.bookstore.util.PasswordUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "bookstore.db";
    private static final int DB_VERSION = 1;

    // Bảng users và các cột
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

    // Tài khoản quản trị tạo sẵn để thử
    public static final String ADMIN_EMAIL = "admin@bookstore.com";
    private static final String ADMIN_PASSWORD = "Admin123";

    private static DatabaseHelper instance;

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
    public void onCreate(SQLiteDatabase db) {
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

        insertDefaultAdmin(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

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
}