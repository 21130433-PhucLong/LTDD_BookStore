package com.example.bookstore.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bookstore.R;
import com.example.bookstore.model.Book;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

// Adapter đưa danh sách Book cho ListView hiển thị danh sách sách
public class BookAdapter extends BaseAdapter {

    private final Context context;
    private final List<Book> bookList;
    private boolean adminMode = false;

    public BookAdapter(Context context, List<Book> books) {
        this.context = context;
        this.bookList = new ArrayList<>(books);
    }

    public void setAdminMode(boolean adminMode) {
        this.adminMode = adminMode;
    }

    @Override
    public int getCount() {
        return bookList.size();
    }

    @Override
    public Book getItem(int position) {
        return bookList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return bookList.get(position).getId();
    }

    // Đổi dữ liệu hiển thị (dùng khi lọc, tìm kiếm)
    public void setData(List<Book> books) {
        bookList.clear();
        bookList.addAll(books);
        notifyDataSetChanged();
    }

    // Giữ sẵn các view của một dòng, không phải findViewById lại mỗi lần cuộn
    private static class ViewHolder {
        ImageView imgCover;
        TextView tvTitle;
        TextView tvAuthor;
        TextView tvPrice;
        TextView tvOldPrice;
        TextView tvDiscount;
        TextView tvMeta;
        TextView tvHidden;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_book, parent, false);
            holder = new ViewHolder();
            holder.imgCover = convertView.findViewById(R.id.imgCover);
            holder.tvTitle = convertView.findViewById(R.id.tvTitle);
            holder.tvAuthor = convertView.findViewById(R.id.tvAuthor);
            holder.tvPrice = convertView.findViewById(R.id.tvPrice);
            holder.tvOldPrice = convertView.findViewById(R.id.tvOldPrice);
            holder.tvDiscount = convertView.findViewById(R.id.tvDiscount);
            holder.tvMeta = convertView.findViewById(R.id.tvMeta);
            holder.tvHidden = convertView.findViewById(R.id.tvHidden);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Book book = getItem(position);
        holder.imgCover.setImageResource(ImageUtil.getDrawableId(context, book.getImage()));
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvPrice.setText(FormatUtil.formatPrice(book.getPrice()));

        int discount = book.getDiscountPercent();
        if (discount > 0) {
            holder.tvOldPrice.setVisibility(View.VISIBLE);
            holder.tvOldPrice.setText(FormatUtil.formatPrice(book.getOriginalPrice()));
            holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText(context.getString(R.string.discount_format, discount));
        } else {
            holder.tvOldPrice.setVisibility(View.GONE);
            holder.tvDiscount.setVisibility(View.GONE);
        }

        if (adminMode) {
            holder.tvMeta.setText(context.getString(R.string.book_meta_admin, book.getStock(), book.getSold()));
            holder.tvHidden.setVisibility(book.isHidden() ? View.VISIBLE : View.GONE);
        } else {
            holder.tvMeta.setText(context.getString(R.string.book_meta, book.getRating(), book.getSold()));
            holder.tvHidden.setVisibility(View.GONE);
        }
        return convertView;
    }
}