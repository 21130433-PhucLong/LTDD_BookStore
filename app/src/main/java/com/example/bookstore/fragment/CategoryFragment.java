package com.example.bookstore.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookstore.R;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragment extends Fragment {

    // Activity chứa Fragment
    public interface OnCategoryClickListener {
        void onCategoryClick(int categoryId);
    }

    private OnCategoryClickListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCategoryClickListener) {
            listener = (OnCategoryClickListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView lvCategories = view.findViewById(R.id.lvCategories);

        // Đọc thể loại kèm số sách, tạo dòng chữ hiển thị cho từng thể loại
        final List<Category> categories = DatabaseHelper.getInstance(requireContext()).getCategoriesWithCount();
        List<String> labels = new ArrayList<>();
        for (Category category : categories) {
            labels.add(getString(R.string.category_label, category.getName(), category.getBookCount()));
        }

        // Danh sách chỉ có một dòng chữ nên dùng ArrayAdapter có sẵn
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_list_item_1, labels);
        lvCategories.setAdapter(adapter);

        lvCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemView, int position, long id) {
                if (listener != null) {
                    listener.onCategoryClick(categories.get(position).getId());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.tab_category);
    }
}