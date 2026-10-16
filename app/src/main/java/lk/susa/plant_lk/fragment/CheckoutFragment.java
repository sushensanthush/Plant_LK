package lk.susa.plant_lk.fragment;

import lk.susa.plant_lk.BuildConfig;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.databinding.FragmentCheckoutBinding;
import lk.susa.plant_lk.listener.FirestoreCallback;
import lk.susa.plant_lk.model.CartItem;
import lk.susa.plant_lk.model.Order;
import lk.susa.plant_lk.model.Product;
import lk.susa.plant_lk.util.ReceiptStorage;
import lk.susa.plant_lk.util.SystemStateReceiver;
import lk.susa.plant_lk.util.TelephonyHelper;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private double total;
    private boolean paymentActive;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        setupToggles();
        return binding.getRoot();
    }

    private void setupToggles() {
        binding.shippingLayoutBtn.setOnClickListener(v -> {
            if (binding.shippingLayoutBody.getVisibility() == View.GONE) {
                binding.shippingLayoutBody.setVisibility(View.VISIBLE);
                binding.shippingLayoutBtn.setRotation(180f);
            } else {
                binding.shippingLayoutBody.setVisibility(View.GONE);
                binding.shippingLayoutBtn.setRotation(0f);
            }
        });

        binding.billingLayoutBtn.setOnClickListener(v -> {
            if (binding.billingLayoutBody.getVisibility() == View.GONE) {
                binding.billingLayoutBody.setVisibility(View.VISIBLE);
                binding.billingLayoutBtn.setRotation(180f);
            } else {
                binding.billingLayoutBody.setVisibility(View.GONE);
                binding.billingLayoutBtn.setRotation(0f);
            }
        });

        binding.shippingDetailsCheckBilling.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.billingLayout.setVisibility(View.GONE);
            } else {
                binding.billingLayout.setVisibility(View.VISIBLE);
                binding.billingLayoutBody.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserData();
        double shippingCost = 400;

        CartItem directItem = null;
        if (getArguments() != null && getArguments().containsKey("cartItem")) {
            directItem = (CartItem) getArguments().getSerializable("cartItem");
        }

        if (directItem != null) {
            CartItem finalDirectItem = directItem;
            List<String> ids = new ArrayList<>();
            ids.add(finalDirectItem.getProductId());

            getProductsByIds(ids, data -> {
                double subTotal = 0;
                Product product = data.get(finalDirectItem.getProductId());
                if (product != null) {
                    subTotal = product.getPrice() * finalDirectItem.getQuantity();
                }
                updateTotalUI(subTotal, shippingCost);
            });
        } else {

            getCartItems(cartItems -> {
                ArrayList<String> productIds = new ArrayList<>();
                cartItems.forEach(cartItem -> productIds.add(cartItem.getProductId()));

                getProductsByIds(productIds, data -> {
                    double subTotal = 0;
                    for (CartItem cartItem : cartItems) {
                        Product product = data.get(cartItem.getProductId());
                        if (product != null) {
                            subTotal += product.getPrice() * cartItem.getQuantity();
                        }
                    }
                    updateTotalUI(subTotal, shippingCost);
                });
            });
        }

        binding.checkoutBtnProceed.setOnClickListener(v -> {
            if (paymentActive) {
                startPayHerePayment();
            }
        });
    }

    private void updateTotalUI(double subTotal, double shipping) {
        total = subTotal + shipping;
        binding.checkoutSubtotal.setText(String.format(Locale.US, "LKR %,.2f", subTotal));
        binding.checkoutShipping.setText(String.format(Locale.US, "LKR %,.2f", shipping));
        binding.checkoutTotal.setText(String.format(Locale.US, "LKR %,.2f", total));
        paymentActive = true;
    }

    private void startPayHerePayment() {
        InitRequest req = new InitRequest();
        req.setSandBox(true);
        req.setMerchantId(BuildConfig.PAYHERE_MERCHANT_ID);
        req.setMerchantSecret(BuildConfig.PAYHERE_MERCHANT_SECRET);
        req.setCurrency("LKR");
        req.setAmount(total);
        req.setOrderId("PDLOI-" + System.currentTimeMillis());
        req.setItemsDescription("Plant.lk Purchase");

        req.getCustomer().setFirstName(binding.shippingDetailsName.getText().toString());
        req.getCustomer().setLastName("User");
        req.getCustomer().setEmail(binding.shippingDetailsEmail.getText().toString());
        req.getCustomer().setPhone(binding.shippingDetailsContact.getText().toString());
        req.getCustomer().getAddress().setAddress(binding.shippingDetailsAddress1.getText().toString());
        req.getCustomer().getAddress().setCity(binding.shippingDetailsCity.getText().toString());
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(getActivity(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        payhereLauncher.launch(intent);
    }

    private void loadUserData() {
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                binding.shippingDetailsName.setText(doc.getString("name"));
                binding.shippingDetailsEmail.setText(doc.getString("email"));
                binding.shippingDetailsContact.setText(doc.getString("phone"));
                binding.shippingDetailsAddress1.setText(doc.getString("address1"));
                binding.shippingDetailsAddress2.setText(doc.getString("address2"));
                binding.shippingDetailsCity.setText(doc.getString("city"));
                binding.shippingDetailsPostcode.setText(doc.getString("postcode"));
            }
        });
    }

    private void getCartItems(FirestoreCallback<List<CartItem>> callback) {
        String uid = firebaseAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("cart")
                .get()
                .addOnSuccessListener(qds -> callback.onCallback(qds.toObjects(CartItem.class)));
    }

    private void getProductsByIds(List<String> productIds, FirestoreCallback<Map<String, Product>> callback) {
        Map<String, Product> products = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) {
            callback.onCallback(products);
            return;
        }
        db.collection("products").whereIn("productId", productIds).get()
                .addOnSuccessListener(qds -> {
                    qds.getDocuments().forEach(ds -> {
                        Product product = ds.toObject(Product.class);
                        if (product != null) products.put(product.getProductId(), product);
                    });
                    callback.onCallback(products);
                });
    }

    private final ActivityResultLauncher<Intent> payhereLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    PHResponse<StatusResponse> response =
                            (PHResponse<StatusResponse>) result.getData()
                                    .getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                    if (response != null && response.isSuccess()) {
                        saveOrder(response.getData());
                    }
                }
            });

    private void saveOrder(StatusResponse statusResponse) {
        String uid = firebaseAuth.getCurrentUser().getUid();

        CartItem directItem = null;
        if (getArguments() != null && getArguments().containsKey("cartItem")) {
            directItem = (CartItem) getArguments().getSerializable("cartItem");
        }

        if (directItem != null) {
            List<CartItem> list = new ArrayList<>();
            list.add(directItem);
            completeOrderFinal(list, uid, true);
        } else {
            getCartItems(cartItems -> completeOrderFinal(cartItems, uid, false));
        }
    }

    private void completeOrderFinal(List<CartItem> itemsToOrder, String uid, boolean isDirectBuy) {
        ArrayList<String> productIds = new ArrayList<>();
        itemsToOrder.forEach(item -> productIds.add(item.getProductId()));

        getProductsByIds(productIds, productMap -> {
            List<Order.OrderItem> orderItems = new ArrayList<>();

            for (CartItem ci : itemsToOrder) {
                Product p = productMap.get(ci.getProductId());
                if (p == null) continue;

                List<Order.OrderItem.Attribute> attrs = new ArrayList<>();
                if (ci.getAttributes() != null) {
                    ci.getAttributes().forEach(a -> attrs.add(new Order.OrderItem.Attribute(a.getName(), a.getValue())));
                }

                orderItems.add(Order.OrderItem.builder()
                        .productId(ci.getProductId())
                        .unitPrice(p.getPrice())
                        .quantity(ci.getQuantity())
                        .attributes(attrs)
                        .build());
            }

            Order.Address ship = buildShippingAddress();
            Order.Address bill = binding.shippingDetailsCheckBilling.isChecked() ? ship : buildBillingAddress();

            Order order = Order.builder()
                    .orderId(String.valueOf(System.currentTimeMillis()))
                    .userId(uid)
                    .totalAmount(total)
                    .status("PAID")
                    .orderDate(Timestamp.now())
                    .orderItems(orderItems)
                    .shippingAddress(ship)
                    .billingAddress(bill)
                    .build();

            db.collection("orders").add(order).addOnSuccessListener(ref -> {
                updateStock(orderItems);
                Toast.makeText(getContext(), "Order placed!", Toast.LENGTH_SHORT).show();


                android.content.Context appCtx = requireContext().getApplicationContext();
                String receipt = ReceiptStorage.buildReceipt(order, productMap);
                boolean saved = ReceiptStorage.save(appCtx, order.getOrderId(), receipt);
                Toast.makeText(appCtx, saved
                        ? "Receipt saved to Documents/Plant.lk Receipts"
                        : "Receipt save failed", Toast.LENGTH_LONG).show();
                appCtx.sendBroadcast(new Intent(SystemStateReceiver.ACTION_ORDER_PLACED)
                        .setPackage(appCtx.getPackageName())
                        .putExtra(SystemStateReceiver.EXTRA_ORDER_ID, order.getOrderId()));
                offerSmsConfirmation(order, ReceiptStorage.buildSmsMessage(order));

                if (!isDirectBuy) {
                    clearCart(uid);
                }

                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            });
        });
    }

    private void offerSmsConfirmation(Order order, String receipt) {
        Activity act = getActivity();
        if (act == null || !TelephonyHelper.isSimReady(act)) return;
        if (order.getShippingAddress() == null) return;
        String phone = order.getShippingAddress().getContact();
        if (phone == null || phone.trim().isEmpty()) return;

        new androidx.appcompat.app.AlertDialog.Builder(act)
                .setTitle("SMS confirmation")
                .setMessage("Send an order confirmation SMS to " + phone + " using "
                        + TelephonyHelper.getNetworkOperator(act) + "?")
                .setPositiveButton("Send", (d, w) -> TelephonyHelper.openSmsComposer(act, phone, receipt))
                .setNegativeButton("Skip", null)
                .show();
    }

    private Order.Address buildShippingAddress() {
        return Order.Address.builder()
                .name(binding.shippingDetailsName.getText().toString())
                .email(binding.shippingDetailsEmail.getText().toString())
                .contact(binding.shippingDetailsContact.getText().toString())
                .address1(binding.shippingDetailsAddress1.getText().toString())
                .address2(binding.shippingDetailsAddress2.getText().toString())
                .city(binding.shippingDetailsCity.getText().toString())
                .postcode(binding.shippingDetailsPostcode.getText().toString())
                .build();
    }

    private Order.Address buildBillingAddress() {
        return Order.Address.builder()
                .name(binding.billingDetailsName.getText().toString())
                .email(binding.billingDetailsEmail.getText().toString())
                .contact(binding.billingDetailsContact.getText().toString())
                .address1(binding.billingDetailsAddress1.getText().toString())
                .address2(binding.billingDetailsAddress2.getText().toString())
                .city(binding.billingDetailsCity.getText().toString())
                .postcode(binding.billingDetailsPostcode.getText().toString())
                .build();
    }

    private void updateStock(List<Order.OrderItem> items) {
        for (Order.OrderItem item : items) {
            db.collection("products").whereEqualTo("productId", item.getProductId()).get().addOnSuccessListener(q -> {
                if (!q.isEmpty()) {
                    db.runTransaction(t -> {
                        long current = t.get(q.getDocuments().get(0).getReference()).getLong("stockCount");
                        t.update(q.getDocuments().get(0).getReference(), "stockCount", current - item.getQuantity());
                        return null;
                    });
                }
            });
        }
    }

    private void clearCart(String uid) {
        db.collection("users").document(uid).collection("cart").get()
                .addOnSuccessListener(q -> q.getDocuments().forEach(d -> d.getReference().delete()));
    }
}