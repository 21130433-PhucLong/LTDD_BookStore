package com.example.bookstore.util;

import android.content.Context;

import com.example.bookstore.R;
import com.example.bookstore.model.Order;

public class StatusUtil {

    public static String getLabel(Context context, String status) {
        if (Order.STATUS_PENDING.equals(status)) {
            return context.getString(R.string.status_pending);
        } else if (Order.STATUS_SHIPPING.equals(status)) {
            return context.getString(R.string.status_shipping);
        } else if (Order.STATUS_DELIVERED.equals(status)) {
            return context.getString(R.string.status_delivered);
        } else if (Order.STATUS_CANCELLED.equals(status)) {
            return context.getString(R.string.status_cancelled);
        }
        return status;
    }

    public static int getColor(Context context, String status) {
        if (Order.STATUS_PENDING.equals(status)) {
            return context.getColor(R.color.status_pending);
        } else if (Order.STATUS_SHIPPING.equals(status)) {
            return context.getColor(R.color.status_shipping);
        } else if (Order.STATUS_DELIVERED.equals(status)) {
            return context.getColor(R.color.status_delivered);
        }
        return context.getColor(R.color.status_cancelled);
    }

    public static String getPaymentLabel(Context context, String method) {
        if (Order.PAYMENT_BANK.equals(method)) {
            return context.getString(R.string.payment_bank_short);
        }
        return context.getString(R.string.payment_cod_short);
    }
}