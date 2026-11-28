package com.example.bookstore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.example.bookstore.R;
import com.example.bookstore.activity.BookDetailActivity;
import com.example.bookstore.adapter.BannerAdapter;
import com.example.bookstore.adapter.BookGridAdapter;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Category;
import com.example.bookstore.util.SessionManager;

import java.util.List;

public class HomeFragment extends Fragment implements BookGridAdapter.OnBookActionListener {

    private static final String ARG_KEYWORD = "keyword";
    private static final String ARG_CATEGORY = "category_id";
    private static final String STATE_KEYWORD = "state_keyword";
    private static final String STATE_CATEGORY = "state_category";
    private static final String STATE_SORT = "state_sort";

    private static final int[] BANNERS = {R.drawable.banner_1, R.drawable.banner_2, R.drawable.banner_3};
    private static final long BANNER_DELAY = 3500; // 3,5 giây đổi banner

    private ViewPager2 viewPagerBanner;
    private ChipGroup chipGroup;
    private Spinner spinnerSort;
    private RecyclerView rvBooks;
    private TextView tvEmpty;
    private TextView tvResultCount;
    private BookGridAdapter adapter;

    private DatabaseHelper db;
    private int userId;

    // Điều kiện đang áp dụng cho danh sách
    private String keyword = "";
    private int categoryId = 0; // 0 = tất cả thể loại
    private int sortMode = DatabaseHelper.SORT_NEWEST;

    // Hẹn giờ tự chuyển banner
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private final Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPagerBanner != null) {
                int next = (viewPagerBanner.getCurrentItem() + 1) % BANNERS.length;
                viewPagerBanner.setCurrentItem(next, true);
                bannerHandler.postDelayed(this, BANNER_DELAY);
            }
        }
    };

    // Tạo Fragment kèm từ khóa và thể loại ban đầu
    public static HomeFragment newInstance(String keyword, int categoryId) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEYWORD, keyword);
        args.putInt(ARG_CATEGORY, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // Khôi phục điều kiện lọc sau khi xoay màn hình
            keyword = savedInstanceState.getString(STATE_KEYWORD, "");
            categoryId = savedInstanceState.getInt(STATE_CATEGORY, 0);
            sortMode = savedInstanceState.getInt(STATE_SORT, DatabaseHelper.SORT_NEWEST);
        } else if (getArguments() != null) {
            keyword = getArguments().getString(ARG_KEYWORD, "");
            categoryId = getArguments().getInt(ARG_CATEGORY, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        chipGroup = view.findViewById(R.id.chipGroup);
        spinnerSort = view.findViewById(R.id.spinnerSort);
        rvBooks = view.findViewById(R.id.rvBooks);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvResultCount = view.findViewById(R.id.tvResultCount);

        db = DatabaseHelper.getInstance(requireContext());
        userId = new SessionManager(requireContext()).getUserId();

        // Lưới 2 cột
        adapter = new BookGridAdapter(requireContext(), this);
        rvBooks.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvBooks.setAdapter(adapter);

        setupBanner(view);
        setupCategoryChips();
        setupSortSpinner();
        loadBooks();
    }

    // Banner + chấm tròn chỉ trang hiện tại
    private void setupBanner(View view) {
        viewPagerBanner.setAdapter(new BannerAdapter(BANNERS));
        TabLayout tabDots = view.findViewById(R.id.tabDots);
        new TabLayoutMediator(tabDots, viewPagerBanner, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                // Không cần chữ, chỉ cần chấm tròn
            }
        }).attach();
    }

    // Tạo hàng nút thể loại: "Tất cả" và từng thể loại trong database
    private void setupCategoryChips() {
        List<Category> categories = db.getAllCategories();
        chipGroup.removeAllViews();
        addChip(getString(R.string.all_categories), 0);
        for (Category category : categories) {
            addChip(category.getName(), category.getId());
        }

        // Chọn sẵn nút ứng với thể loại đang lọc (đặt trước khi gắn listener nên chưa gọi lại tải sách)
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (((Integer) chip.getTag()) == categoryId) {
                chip.setChecked(true);
                break;
            }
        }

        chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if (checkedIds.isEmpty()) {
                    return;
                }
                Chip chip = group.findViewById(checkedIds.get(0));
                categoryId = (Integer) chip.getTag();
                loadBooks();
            }
        });
    }

    // Tạo một nút thể loại, id thể loại được lưu trong tag
    private void addChip(String text, int id) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_category, chipGroup, false);
        chip.setId(View.generateViewId());
        chip.setText(text);
        chip.setTag(id);
        chipGroup.addView(chip);
    }

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.sort_options, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setSelection(sortMode);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Spinner tự gọi hàm này lần đầu; chỉ tải lại khi cách sắp xếp thật sự đổi
                if (position != sortMode) {
                    sortMode = position;
                    loadBooks();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Đọc lại sách từ database theo điều kiện hiện tại rồi hiển thị (kèm tập sách đã yêu thích)
    private void loadBooks() {
        List<Book> books = db.getBooks(categoryId, keyword, sortMode);
        adapter.setData(books, db.getWishlistBookIds(userId));
        tvResultCount.setText(getString(R.string.result_count, books.size()));
        tvEmpty.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // MainActivity gọi hàm này khi người dùng gõ từ khóa trên thanh tìm kiếm
    public void setKeyword(String newKeyword) {
        keyword = newKeyword;
        if (getView() != null) { // chỉ tải lại khi giao diện đã được tạo
            loadBooks();
        }
    }

    // ---- Bấm vào thẻ sách / trái tim ----

    @Override
    public void onBookClick(Book book) {
        Intent intent = new Intent(requireContext(), BookDetailActivity.class);
        intent.putExtra(BookDetailActivity.EXTRA_BOOK, book);
        startActivity(intent);
    }

    @Override
    public void onWishlistClick(Book book, int position) {
        boolean liked = db.toggleWishlist(userId, book.getId());
        adapter.setLiked(book.getId(), liked);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.tab_home);
        // Quay về (ví dụ từ màn chi tiết) thì làm mới tim và tồn kho; rồi bật lại hẹn giờ banner
        if (getView() != null) {
            loadBooks();
        }
        bannerHandler.removeCallbacks(bannerRunnable);
        bannerHandler.postDelayed(bannerRunnable, BANNER_DELAY);
    }

    @Override
    public void onPause() {
        super.onPause();
        bannerHandler.removeCallbacks(bannerRunnable); // dừng tự chuyển banner khi rời màn hình
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPagerBanner = null; // tránh giữ view cũ trong Runnable
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_KEYWORD, keyword);
        outState.putInt(STATE_CATEGORY, categoryId);
        outState.putInt(STATE_SORT, sortMode);
    }
}