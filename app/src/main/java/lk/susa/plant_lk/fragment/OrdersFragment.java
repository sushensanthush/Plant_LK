package lk.susa.plant_lk.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.adapter.OrdersAdapter;
import lk.susa.plant_lk.model.Order;

public class OrdersFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private View emptyState;
    private TextView ordersCount;
    private OrdersAdapter adapter;
    private List<Order> orders = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.orders_recycler);
        emptyState = view.findViewById(R.id.orders_empty_state);
        ordersCount = view.findViewById(R.id.orders_count);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new OrdersAdapter(orders, order -> {
            Bundle bundle = new Bundle();
            bundle.putString("orderId", order.getOrderId());
            OrderDetailFragment fragment = new OrderDetailFragment();
            fragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);
        loadOrders();
    }

    private void loadOrders() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showEmptyState();
            return;
        }
        showLoading();
        FirebaseFirestore.getInstance()
                .collection("orders")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    orders.clear();
                    hideLoading();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Order order = doc.toObject(Order.class);

                        if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                            order.setOrderId(doc.getId());
                        }

                        orders.add(order);
                    }

                    Collections.sort(orders, (a, b) -> {
                        if (a.getOrderDate() == null && b.getOrderDate() == null) return 0;
                        if (a.getOrderDate() == null) return 1;
                        if (b.getOrderDate() == null) return -1;
                        return b.getOrderDate().compareTo(a.getOrderDate());
                    });

                    adapter.notifyDataSetChanged();
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    Log.e("OrdersFragment", "Error: " + e.getMessage());
                    showEmptyState();
                });
    }

    private void updateUI() {
        int count = orders.size();
        ordersCount.setText(count + " order" + (count == 1 ? "" : "s"));

        if (count == 0) {
            showEmptyState();
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        ordersCount.setText("0 orders");
    }
}