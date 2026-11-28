package com.example.bookstore.util;

import com.example.bookstore.model.Voucher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VoucherUtil {

    public static final int OK = 0;
    public static final int NOT_FOUND = 1;
    public static final int INACTIVE = 2;
    public static final int EXPIRED = 3;
    public static final int MIN_ORDER = 4;

    public static int check(Voucher voucher, long subtotal) {
        if (voucher == null) {
            return NOT_FOUND;
        }
        if (!voucher.isActive()) {
            return INACTIVE;
        }
        // Ngày dạng yyyy-MM-dd so sánh chuỗi được; dùng đến hết ngày hết hạn
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        if (today.compareTo(voucher.getExpiredAt()) > 0) {
            return EXPIRED;
        }
        if (subtotal < voucher.getMinOrder()) {
            return MIN_ORDER;
        }
        return OK;
    }
}