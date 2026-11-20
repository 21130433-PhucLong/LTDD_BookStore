package com.example.bookstore.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;

import com.example.bookstore.R;
import com.example.bookstore.adapter.CartAdapter;
import com.example.bookstore.data.DatabaseHelper;
import com.example.bookstore.model.CartItem;
import com.example.bookstore.util.FormatUtil;
import com.example.bookstore.util.SessionManager;
import com.example.bookstore.activity.CheckoutActivity;

import java.util.List;

//Tab giỏ hàng: danh sách sách đã thêm, đổi số lượng, nhấn giữ để xóa, tổng tiền.
public class CartFragment extends Fragment {

    // MainActivity này để cập nhật số nhỏ trên tab khi giỏ thay đổi
    public interface OnCartChangedListener {
        void onCartChanged();
    }

    private OnCartChangedListener listener;

    private ListView lvCart;
    private TextView tvCartEmpty;
    private TextView tvCartHint;
    private TextView tvTotal;
    private Button btnCheckout;
    private CartAdapter adapter;

    private DatabaseHelper db;
    private int userId;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCartChangedListener) {
            listener = (OnCartChangedListener) context;
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
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lvCart = view.findViewById(R.id.lvCart);
        tvCartEmpty = view.findViewById(R.id.tvCartEmpty);
        tvCartHint = view.findViewById(R.id.tvCartHint);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        db = DatabaseHelper.getInstance(requireContext());
        userId = new SessionManager(requireContext()).getUserId();

        adapter = new CartAdapter(requireContext(), new CartAdapter.OnQuantityChangeListener() {
            @Override
            public void onQuantityChange(CartItem item, int newQuantity) {
                changeQuantity(item, newQuantity);
            }
        });
        lvCart.setAdapter(adapter);

        setupContextMenu();

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(), CheckoutActivity.class));
            }
        });

        loadCart();
    }

    // Đổi số lượng một dòng, kiểm tra giới hạn tối thiểu và tồn kho
    private void changeQuantity(CartItem item, int newQuantity) {
        if (newQuantity < 1) {
            Toast.makeText(requireContext(), R.string.msg_min_quantity, Toast.LENGTH_SHORT).show();
            return;
        }
        int stock = item.getBook().getStock();
        if (newQuantity > stock) {
            Toast.makeText(requireContext(), getString(R.string.msg_exceed_stock, stock), Toast.LENGTH_SHORT).show();
            return;
        }
        db.updateCartQuantity(item.getId(), newQuantity);
        loadCart();
    }

    // Context Menu: nhấn giữ một dòng để hiện menu "Xóa khỏi giỏ hàng"
    private void setupContextMenu() {
        lvCart.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                if (info == null) {
                    return;
                }
                final CartItem item = adapter.getItem(info.position);

                menu.setHeaderTitle(item.getBook().getTitle());
                MenuItem removeItem = menu.add(0, 0, 0, R.string.menu_remove_from_cart);
                removeItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        db.deleteCartItem(item.getId());
                        Toast.makeText(requireContext(), R.string.msg_removed_from_cart, Toast.LENGTH_SHORT).show();
                        loadCart();
                        return true;
                    }
                });
            }
        });
    }

    // Đọc giỏ từ database rồi hiển thị, tính tổng tiền
    private void loadCart() {
        List<CartItem> items = db.getCartItems(userId);
        adapter.setData(items);

        long total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        tvTotal.setText(FormatUtil.formatPrice(total));

        boolean empty = items.isEmpty();
        tvCartEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        tvCartHint.setVisibility(empty ? View.GONE : View.VISIBLE);
        btnCheckout.setEnabled(!empty);

        if (listener != null) {
            listener.onCartChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.tab_cart);
        loadCart();
    }
}