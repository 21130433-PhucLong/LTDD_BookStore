package com.example.bookstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.fragment.AccountFragment;
import com.example.bookstore.fragment.CartFragment;
import com.example.bookstore.fragment.CategoryFragment;
import com.example.bookstore.fragment.HomeFragment;
import com.example.bookstore.model.User;
import com.example.bookstore.util.SessionManager;

public class MainActivity extends AppCompatActivity implements CategoryFragment.OnCategoryClickListener {

    private static final String STATE_KEYWORD = "state_keyword";

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private String userName = "";

    private String searchKeyword = "";  // từ khóa đang tìm
    private int pendingCategoryId = 0;  // thể loại được chọn ở tab Danh mục, dùng khi mở tab Trang chủ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chưa đăng nhập hoặc tài khoản không còn: quay về màn hình đăng nhập
        if (!loadCurrentUser()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (savedInstanceState != null) {
            searchKeyword = savedInstanceState.getString(STATE_KEYWORD, "");
        }

        // Cho giao diện tràn viền, sau đó tự chừa chỗ cho thanh hệ thống
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setupSystemBarsPadding();

        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottomNav);
        setSupportActionBar(toolbar);

        setupBottomNav();

        // Chỉ chọn tab đầu khi mở mới; xoay màn hình thì hệ thống tự khôi phục Fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    // Đọc người dùng hiện tại theo phiên đăng nhập
    private boolean loadCurrentUser() {
        SessionManager session = new SessionManager(this);
        User user = DatabaseHelper.getInstance(this).getUserById(session.getUserId());
        if (user == null) {
            session.logout();
            return false;
        }
        userName = user.getName();
        return true;
    }

    // Chừa khoảng trống phía trên (thanh trạng thái) và hai bên
    private void setupSystemBarsPadding() {
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, 0);
                return insets;
            }
        });
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    showFragment(HomeFragment.newInstance(searchKeyword, pendingCategoryId), R.string.tab_home);
                    pendingCategoryId = 0; // đã dùng xong
                    return true;
                } else if (id == R.id.nav_category) {
                    showFragment(new CategoryFragment(), R.string.tab_category);
                    return true;
                } else if (id == R.id.nav_cart) {
                    showFragment(new CartFragment(), R.string.tab_cart);
                    return true;
                } else if (id == R.id.nav_account) {
                    showFragment(AccountFragment.newInstance(userName), R.string.tab_account);
                    return true;
                }
                return false;
            }
        });
    }

    // Thay Fragment đang hiển thị và đổi tiêu đề thanh trên
    private void showFragment(Fragment fragment, int titleResId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        toolbar.setTitle(titleResId);
    }

    // Tab Danh mục báo người dùng vừa chọn một thể loại
    @Override
    public void onCategoryClick(int categoryId) {
        pendingCategoryId = categoryId;
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    // ---------------- Option Menu và tìm kiếm ----------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        setupSearchView(menu.findItem(R.id.action_search));
        return true;
    }

    private void setupSearchView(MenuItem searchItem) {
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doSearch(query);
                searchView.clearFocus(); // ẩn bàn phím
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                doSearch(newText); // tìm ngay khi đang gõ
                return true;
            }
        });

        // Đóng ô tìm kiếm thì bỏ bộ lọc từ khóa
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                doSearch("");
                return true;
            }
        });

        // Sau khi xoay màn hình: mở lại ô tìm kiếm với từ khóa đang dùng
        if (!searchKeyword.isEmpty()) {
            String keep = searchKeyword;
            searchItem.expandActionView();
            searchView.setQuery(keep, false);
        }
    }

    // Áp dụng từ khóa cho Trang chủ
    private void doSearch(String keyword) {
        String newKeyword = (keyword == null) ? "" : keyword.trim();
        if (newKeyword.equals(searchKeyword)) {
            return;
        }
        searchKeyword = newKeyword;

        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (current instanceof HomeFragment) {
            ((HomeFragment) current).setKeyword(searchKeyword);
        } else {
            // Đang ở tab khác: chuyển về Trang chủ, Fragment mới nhận từ khóa lúc được tạo
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_cart) {
            bottomNav.setSelectedItemId(R.id.nav_cart);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_KEYWORD, searchKeyword);
    }
}