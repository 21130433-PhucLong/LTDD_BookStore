package com.example.bookstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bookstore.R;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.CartItem;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

// Đưa các dòng trong giỏ hàng lên ListView, báo ngược về Fragment qua interface.

public class CartAdapter extends BaseAdapter {

    // Fragment này để xử lý khi người dùng bấm + hoặc -
    public interface OnQuantityChangeListener {
        void onQuantityChange(CartItem item, int newQuantity);
    }

    private final Context context;
    private final List<CartItem> items;
    private final OnQuantityChangeListener listener;

    public CartAdapter(Context context, OnQuantityChangeListener listener) {
        this.context = context;
        this.items = new ArrayList<>();
        this.listener = listener;
    }

    public void setData(List<CartItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public CartItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    private static class ViewHolder {
        ImageView imgCover;
        TextView tvTitle;
        TextView tvPrice;
        TextView tvQuantity;
        TextView tvSubtotal;
        View btnMinus;
        View btnPlus;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
            holder = new ViewHolder();
            holder.imgCover = convertView.findViewById(R.id.imgCover);
            holder.tvTitle = convertView.findViewById(R.id.tvTitle);
            holder.tvPrice = convertView.findViewById(R.id.tvPrice);
            holder.tvQuantity = convertView.findViewById(R.id.tvQuantity);
            holder.tvSubtotal = convertView.findViewById(R.id.tvSubtotal);
            holder.btnMinus = convertView.findViewById(R.id.btnMinus);
            holder.btnPlus = convertView.findViewById(R.id.btnPlus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Biến final để dùng được trong sự kiện bấm nút
        final CartItem item = getItem(position);
        Book book = item.getBook();

        holder.imgCover.setImageResource(ImageUtil.getDrawableId(context, book.getImage()));
        holder.tvTitle.setText(book.getTitle());
        holder.tvPrice.setText(FormatUtil.formatPrice(book.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        // Số lượng vượt tồn kho (do người khác vừa mua hoặc admin sửa) thì tô đỏ để biết cần giảm
        holder.tvQuantity.setTextColor(context.getColor(
                item.getQuantity() > book.getStock() ? R.color.error_red : R.color.text_main));
        holder.tvSubtotal.setText(FormatUtil.formatPrice(item.getSubtotal()));

        holder.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onQuantityChange(item, item.getQuantity() - 1);
            }
        });
        holder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onQuantityChange(item, item.getQuantity() + 1);
            }
        });
        return convertView;
    }
}