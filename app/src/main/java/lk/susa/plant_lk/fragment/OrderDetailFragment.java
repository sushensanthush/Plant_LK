package lk.susa.plant_lk.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.model.Order;

public class OrderDetailFragment extends Fragment {

    private String orderId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout shippingHeader = view.findViewById(R.id.shipping_toggle_header);
        LinearLayout shippingBody = view.findViewById(R.id.shipping_body);
        ImageView shippingArrow = view.findViewById(R.id.shipping_arrow);

        shippingHeader.setOnClickListener(v -> {
            if (shippingBody.getVisibility() == View.VISIBLE) {
                shippingBody.setVisibility(View.GONE);
                shippingArrow.setRotation(0f);
            } else {
                shippingBody.setVisibility(View.VISIBLE);
                shippingArrow.setRotation(180f);
            }
        });

        LinearLayout billingHeader = view.findViewById(R.id.billing_toggle_header);
        LinearLayout billingBody = view.findViewById(R.id.billing_body);
        ImageView billingArrow = view.findViewById(R.id.billing_arrow);

        billingHeader.setOnClickListener(v -> {
            if (billingBody.getVisibility() == View.VISIBLE) {
                billingBody.setVisibility(View.GONE);
                billingArrow.setRotation(0f);
            } else {
                billingBody.setVisibility(View.VISIBLE);
                billingArrow.setRotation(180f);
            }
        });

        FirebaseFirestore.getInstance()
                .collection("orders")
                .whereEqualTo("orderId", orderId)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!querySnapshots.isEmpty()) {
                        Order order = querySnapshots.getDocuments().get(0).toObject(Order.class);
                        if (order != null) bindOrder(view, order);
                    }
                });
    }

    private void bindOrder(View view, Order order) {

        TextView orderIdView = view.findViewById(R.id.detail_order_id);
        String displayId = "#" + order.getOrderId().substring(
                Math.max(0, order.getOrderId().length() - 8));
        orderIdView.setText(displayId);

        TextView statusView = view.findViewById(R.id.detail_order_status);
        statusView.setText(order.getStatus() != null ? order.getStatus() : "PENDING");

        TextView dateView = view.findViewById(R.id.detail_order_date);
        if (order.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            dateView.setText(sdf.format(order.getOrderDate().toDate()));
        }

        LinearLayout itemsContainer = view.findViewById(R.id.detail_order_items_container);
        double subtotal = 0;

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (int i = 0; i < order.getOrderItems().size(); i++) {
                Order.OrderItem item = order.getOrderItems().get(i);
                subtotal += item.getUnitPrice() * item.getQuantity();

                LinearLayout row = new LinearLayout(getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, i < order.getOrderItems().size() - 1 ? 10 : 0);
                row.setLayoutParams(params);

                TextView productLabel = new TextView(getContext());
                LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                productLabel.setLayoutParams(labelParams);
                String pid = item.getProductId() != null ? item.getProductId() : "Unknown";
                productLabel.setText(pid.substring(0, Math.min(16, pid.length())) + "...");
                productLabel.setTextSize(12);
                productLabel.setTextColor(requireContext().getColor(R.color.md_theme_onSurfaceVariant));

                TextView priceLabel = new TextView(getContext());
                priceLabel.setText(item.getQuantity() + " x LKR " +
                        String.format(Locale.US, "%,.2f", item.getUnitPrice()));
                priceLabel.setTextSize(12);
                priceLabel.setTextColor(requireContext().getColor(R.color.md_theme_onSurface));

                row.addView(productLabel);
                row.addView(priceLabel);
                itemsContainer.addView(row);

                if (i < order.getOrderItems().size() - 1) {
                    View divider = new View(getContext());
                    LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    divParams.setMargins(0, 8, 0, 8);
                    divider.setLayoutParams(divParams);
                    divider.setBackgroundColor(requireContext().getColor(R.color.md_theme_outlineVariant));
                    itemsContainer.addView(divider);
                }
            }
        }

        view.<TextView>findViewById(R.id.detail_subtotal)
                .setText(String.format(Locale.US, "LKR %,.2f", subtotal));

        bindAddress(view, order.getShippingAddress(),
                R.id.detail_shipping_name,
                R.id.detail_shipping_address,
                R.id.detail_shipping_contact);

        View billingCard = view.findViewById(R.id.billing_card);
        if (order.getBillingAddress() != null) {
            billingCard.setVisibility(View.VISIBLE);
            bindAddress(view, order.getBillingAddress(),
                    R.id.detail_billing_name,
                    R.id.detail_billing_address,
                    R.id.detail_billing_contact);
        } else {
            billingCard.setVisibility(View.GONE);
        }

        view.<TextView>findViewById(R.id.detail_total)
                .setText(String.format(Locale.US, "LKR %,.2f", order.getTotalAmount()));
    }

    private void bindAddress(View view, Order.Address addr,
                             int nameId, int addressId, int contactId) {
        if (addr == null) return;

        view.<TextView>findViewById(nameId).setText(addr.getName());

        StringBuilder addressStr = new StringBuilder();
        if (addr.getAddress1() != null) addressStr.append(addr.getAddress1());
        if (addr.getAddress2() != null && !addr.getAddress2().isEmpty())
            addressStr.append("\n").append(addr.getAddress2());
        if (addr.getCity() != null) addressStr.append("\n").append(addr.getCity());
        if (addr.getPostcode() != null && !addr.getPostcode().isEmpty())
            addressStr.append(" ").append(addr.getPostcode());
        view.<TextView>findViewById(addressId).setText(addressStr.toString());

        StringBuilder contactStr = new StringBuilder();
        if (addr.getContact() != null) contactStr.append(addr.getContact());
        if (addr.getEmail() != null && !addr.getEmail().isEmpty()) {
            if (contactStr.length() > 0) contactStr.append("\n");
            contactStr.append(addr.getEmail());
        }
        view.<TextView>findViewById(contactId).setText(contactStr.toString());
    }
}