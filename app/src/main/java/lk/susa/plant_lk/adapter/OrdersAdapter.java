package lk.susa.plant_lk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.model.Order;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    private List<Order> orders;
    private OnOrderClickListener listener;

    public OrdersAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);


        String displayId = "#" + order.getOrderId().substring(
                Math.max(0, order.getOrderId().length() - 8));
        holder.orderId.setText(displayId);


        if (order.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.orderDate.setText(sdf.format(order.getOrderDate().toDate()));
        } else {
            holder.orderDate.setText("—");
        }


        holder.orderStatus.setText(order.getStatus() != null ? order.getStatus() : "PENDING");


        int itemCount = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
        holder.orderSummary.setText(itemCount + " item" + (itemCount == 1 ? "" : "s"));


        if (order.getShippingAddress() != null) {
            String addr = order.getShippingAddress().getCity()
                    + ", Sri Lanka";
            holder.orderAddress.setText(addr);
        } else {
            holder.orderAddress.setText("—");
        }

        holder.orderTotal.setText(String.format(Locale.US, "LKR %,.2f", order.getTotalAmount()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderDate, orderStatus, orderSummary, orderAddress, orderTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_item_id);
            orderDate = itemView.findViewById(R.id.order_item_date);
            orderStatus = itemView.findViewById(R.id.order_item_status);
            orderSummary = itemView.findViewById(R.id.order_item_summary);
            orderAddress = itemView.findViewById(R.id.order_item_address);
            orderTotal = itemView.findViewById(R.id.order_item_total);
        }
    }

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }
}
