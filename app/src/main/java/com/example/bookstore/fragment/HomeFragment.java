package com.example.bookstore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.bookstore.R;
import com.example.bookstore.activity.BookDetailActivity;
import com.example.bookstore.adapter.BookAdapter;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Category;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String ARG_KEYWORD = "keyword";
    private static final String ARG_CATEGORY = "category_id";
    private static final String STATE_KEYWORD = "state_keyword";
    private static final String STATE_CATEGORY = "state_category";
    private static final String STATE_SORT = "state_sort";

    private ChipGroup chipGroup;
    private Spinner spinnerSort;
    private ListView lvBooks;
    private TextView tvEmpty;
    private TextView tvResultCount;
    private BookAdapter adapter;

    // Điều kiện đang áp dụng cho danh sách
    private String keyword = "";
    private int categoryId = 0; // 0 = tất cả thể loại
    private int sortMode = DatabaseHelper.SORT_NEWEST;

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

        chipGroup = view.findViewById(R.id.chipGroup);
        spinnerSort = view.findViewById(R.id.spinnerSort);
        lvBooks = view.findViewById(R.id.lvBooks);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvResultCount = view.findViewById(R.id.tvResultCount);

        adapter = new BookAdapter(requireContext(), new ArrayList<Book>());
        lvBooks.setAdapter(adapter);

        setupCategoryChips();
        setupSortSpinner();
        setupBookClick();
        loadBooks();
    }

    // Tạo hàng nút thể loại: "Tất cả" và từng thể loại trong database
    private void setupCategoryChips() {
        List<Category> categories = DatabaseHelper.getInstance(requireContext()).getAllCategories();
        chipGroup.removeAllViews();
        addChip(getString(R.string.all_categories), 0);
        for (Category category : categories) {
            addChip(category.getName(), category.getId());
        }

        // Chọn sẵn nút ứng với thể loại đang lọc
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

    // Bấm vào một sách: mở màn hình chi tiết, gửi cả đối tượng Book qua Intent
    private void setupBookClick() {
        lvBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
                Book book = adapter.getItem(position);
                Intent intent = new Intent(requireContext(), BookDetailActivity.class);
                intent.putExtra(BookDetailActivity.EXTRA_BOOK, book);
                startActivity(intent);
            }
        });
    }

    private void loadBooks() {
        List<Book> books = DatabaseHelper.getInstance(requireContext())
                .getBooks(categoryId, keyword, sortMode);
        adapter.setData(books);
        tvResultCount.setText(getString(R.string.result_count, books.size()));
        tvEmpty.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void setKeyword(String newKeyword) {
        keyword = newKeyword;
        if (getView() != null) {
            loadBooks();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_KEYWORD, keyword);
        outState.putInt(STATE_CATEGORY, categoryId);
        outState.putInt(STATE_SORT, sortMode);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.tab_home);
        if (getView() != null) {
            loadBooks();
        }
    }
}