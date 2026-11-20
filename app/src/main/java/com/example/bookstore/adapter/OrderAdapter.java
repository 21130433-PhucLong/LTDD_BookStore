package com.example.bookstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bookstore.R;
import com.example.bookstore.model.Order;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.StatusUtil;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends BaseAdapter {

    private final Context context;
    private final List<Order> orders = new ArrayList<>();

    public OrderAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Order> newOrders) {
        orders.clear();
        orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Order getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return orders.get(position).getId();
    }

    private static class ViewHolder {
        TextView tvOrderCode;
        TextView tvStatus;
        TextView tvDate;
        TextView tvItemCount;
        TextView tvTotal;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
            holder = new ViewHolder();
            holder.tvOrderCode = convertView.findViewById(R.id.tvOrderCode);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            holder.tvItemCount = convertView.findViewById(R.id.tvItemCount);
            holder.tvTotal = convertView.findViewById(R.id.tvTotal);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Order order = getItem(position);
        holder.tvOrderCode.setText(context.getString(R.string.label_order_code, order.getId()));
        holder.tvStatus.setText(StatusUtil.getLabel(context, order.getStatus()));
        holder.tvStatus.setTextColor(StatusUtil.getColor(context, order.getStatus()));
        holder.tvDate.setText(FormatUtil.formatDateTime(order.getCreatedAt()));
        holder.tvItemCount.setText(context.getString(R.string.order_item_count, order.getItemCount()));
        holder.tvTotal.setText(FormatUtil.formatPrice(order.getTotal()));
        return convertView;
    }
}