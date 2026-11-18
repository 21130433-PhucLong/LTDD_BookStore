package com.example.bookstore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bookstore.R;
import com.example.bookstore.activity.BookDetailActivity;
import com.example.bookstore.adapter.BookAdapter;
import com.example.bookstore.data.SampleData;
import com.example.bookstore.model.Book;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private ListView lvBooks;
    private BookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lvBooks = view.findViewById(R.id.lvBooks);

        List<Book> bookList = SampleData.getBooks();
        adapter = new BookAdapter(requireContext(), bookList);
        lvBooks.setAdapter(adapter);

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

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}