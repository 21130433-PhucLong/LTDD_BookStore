package com.example.bookstore.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookstore.R;
import com.example.bookstore.model.Book;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.ImageUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookGridAdapter extends RecyclerView.Adapter<BookGridAdapter.ViewHolder> {

    // Màn hình chứa adapter cài đặt interface này để xử lý bấm vào thẻ và bấm vào trái tim
    public interface OnBookActionListener {
        void onBookClick(Book book);

        void onWishlistClick(Book book, int position);
    }

    private final Context context;
    private final List<Book> books = new ArrayList<>();
    private final Set<Integer> wishlistIds = new HashSet<>();
    private final OnBookActionListener listener;

    public BookGridAdapter(Context context, OnBookActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // Đổi dữ liệu: danh sách sách và tập id sách đang yêu thích
    public void setData(List<Book> newBooks, Set<Integer> likedIds) {
        books.clear();
        books.addAll(newBooks);
        wishlistIds.clear();
        wishlistIds.addAll(likedIds);
        notifyDataSetChanged();
    }

    // Cập nhật trạng thái tim của một sách (rồi gọi notifyItemChanged ở nơi gọi)
    public void setLiked(int bookId, boolean liked) {
        if (liked) {
            wishlistIds.add(bookId);
        } else {
            wishlistIds.remove(bookId);
        }
    }

    // Giữ sẵn các view của một thẻ
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgCover;
        final ImageView ivHeart;
        final TextView tvDiscount;
        final TextView tvSoldOut;
        final TextView tvTitle;
        final TextView tvAuthor;
        final TextView tvPrice;
        final TextView tvOldPrice;
        final TextView tvMeta;

        ViewHolder(View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            ivHeart = itemView.findViewById(R.id.ivHeart);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvSoldOut = itemView.findViewById(R.id.tvSoldOut);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
            tvMeta = itemView.findViewById(R.id.tvMeta);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Book book = books.get(position);

        holder.imgCover.setImageResource(ImageUtil.getDrawableId(context, book.getImage()));
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvPrice.setText(FormatUtil.formatPrice(book.getPrice()));
        holder.tvMeta.setText(context.getString(R.string.book_meta, book.getRating(), book.getSold()));

        int discount = book.getDiscountPercent();
        if (discount > 0) {
            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText(context.getString(R.string.discount_format, discount));
            holder.tvOldPrice.setVisibility(View.VISIBLE);
            holder.tvOldPrice.setText(FormatUtil.formatPrice(book.getOriginalPrice()));
            holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
            holder.tvOldPrice.setVisibility(View.GONE);
        }

        holder.tvSoldOut.setVisibility(book.isInStock() ? View.GONE : View.VISIBLE);

        // Trái tim: đầy màu đỏ khi đã yêu thích, rỗng màu xám khi chưa
        boolean liked = wishlistIds.contains(book.getId());
        holder.ivHeart.setImageResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        holder.ivHeart.setColorFilter(context.getColor(liked ? R.color.heart_red : R.color.text_sub));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBookClick(book);
            }
        });
        holder.ivHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onWishlistClick(book, pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
}