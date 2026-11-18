package com.example.bookstore.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.bookstore.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.example.bookstore.fragment.AccountFragment;
import com.example.bookstore.fragment.CartFragment;
import com.example.bookstore.fragment.CategoryFragment;
import com.example.bookstore.fragment.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setupSystemBarsPadding();

        toolbar = findViewById(R.id.toolbar);
        bottomNav = findViewById(R.id.bottomNav);
        setSupportActionBar(toolbar);

        // Lấy email do màn hình đăng nhập gửi sang
        Bundle bundle = getIntent().getBundleExtra("userData");
        if (bundle != null) {
            email = bundle.getString("email", "");
        }

        setupBottomNav();

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

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
                    showFragment(new HomeFragment(), R.string.tab_home);
                    return true;
                } else if (id == R.id.nav_category) {
                    showFragment(new CategoryFragment(), R.string.tab_category);
                    return true;
                } else if (id == R.id.nav_cart) {
                    showFragment(new CartFragment(), R.string.tab_cart);
                    return true;
                } else if (id == R.id.nav_account) {
                    showFragment(AccountFragment.newInstance(email), R.string.tab_account);
                    return true;
                }
                return false;
            }
        });
    }

    private void showFragment(Fragment fragment, int titleResId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        toolbar.setTitle(titleResId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            // Chức năng tìm kiếm sẽ làm ở bước sau
            Toast.makeText(this, R.string.msg_feature_coming, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_cart) {
            bottomNav.setSelectedItemId(R.id.nav_cart);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}