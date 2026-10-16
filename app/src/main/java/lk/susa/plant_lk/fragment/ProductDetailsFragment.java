package lk.susa.plant_lk.fragment;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.activity.SignInActivity;
import lk.susa.plant_lk.adapter.ProductSliderAdapter;
import lk.susa.plant_lk.adapter.SectionAdapter;
import lk.susa.plant_lk.databinding.FragmentProductDetailsBinding;
import lk.susa.plant_lk.model.CartItem;
import lk.susa.plant_lk.model.Product;

public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private String productId;
    private int quantity = 1;
    private int avbQuantity;
    private boolean isWishlisted = false;
    private Map<String, ChipGroup> attributeGroups = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        hideBottomNav();
        setupBackPress();
        loadProduct();
        loadWishlistState();
        setupQuantityButtons();
        setupWishlistButtons();
        setupCartAndBuyButtons();
        loadTopSellProduct();
    }

    private void hideBottomNav() {
        View nav = getActivity().findViewById(R.id.bottom_navigation_view);
        if (nav != null) nav.setVisibility(View.GONE);
    }

    private void setupBackPress() {
        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                });
    }

    private void loadProduct() {
        db.collection("products")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(qds -> {
                    if (!isAdded() || binding == null || qds.isEmpty()) return;

                    Product product = qds.getDocuments().get(0).toObject(Product.class);

                    ProductSliderAdapter adapter = new ProductSliderAdapter(product.getImages());
                    binding.productImageSlider.setAdapter(adapter);
                    binding.dotsIndicator.attachTo(binding.productImageSlider);

                    binding.productDetailsTitle.setText(product.getTitle());
                    binding.productDetailsDescription.setText(product.getDescription());
                    binding.productDetailsRating.setRating(product.getRating());
                    binding.productDetailsRatingValue.setText(String.valueOf(product.getRating()));
                    binding.productDetailsPrice.setText("LKR " + product.getPrice());
                    binding.productDetailsAvbQty.setText(String.valueOf(product.getStockCount()));
                    avbQuantity = product.getStockCount();

                    if (product.getAttributes() != null) {
                        product.getAttributes().forEach(attribute ->
                                renderAttribute(attribute, binding.productDetailsAttributeContainer));
                    }
                });
    }

    private void loadWishlistState() {
        if (firebaseAuth.getCurrentUser() == null) return;

        db.collection("users")
                .document(firebaseAuth.getCurrentUser().getUid())
                .collection("wishlist")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (!isAdded() || binding == null) return;
                    isWishlisted = !querySnapshots.isEmpty();
                    updateWishlistButtons();
                });
    }

    private void setupQuantityButtons() {
        binding.productDetailsBtnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.productDetailsQuantity.setText(String.valueOf(quantity));
            }
        });

        binding.productDetailsBtnPlus.setOnClickListener(v -> {
            if (quantity < avbQuantity) {
                quantity++;
                binding.productDetailsQuantity.setText(String.valueOf(quantity));
            }
        });
    }

    private void setupWishlistButtons() {
        binding.productDetailsBtnWishlist.setOnClickListener(v -> toggleWishlist());
        binding.productDetailsBtnWishlistBar.setOnClickListener(v -> toggleWishlist());
    }

    private void setupCartAndBuyButtons() {
        binding.productDetailsBtnAddCart.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                startActivity(new Intent(getActivity(), SignInActivity.class));
                return;
            }
            CartItem cartItem = new CartItem(productId, quantity, getFinalSelections());
            String uid = firebaseAuth.getCurrentUser().getUid();
            db.collection("users").document(uid)
                    .collection("cart")
                    .document()
                    .set(cartItem)
                    .addOnSuccessListener(unused -> {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Item added to cart!", Toast.LENGTH_SHORT).show();
                    });
        });

        binding.productDetailsBtnBuyNow.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null) {
                startActivity(new Intent(getActivity(), SignInActivity.class));
                return;
            }
            CartItem cartItem = new CartItem(productId, quantity, getFinalSelections());
            Bundle bundle = new Bundle();
            bundle.putSerializable("cartItem", cartItem);

            CheckoutFragment checkoutFragment = new CheckoutFragment();
            checkoutFragment.setArguments(bundle);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, checkoutFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void toggleWishlist() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please sign in to use wishlist", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = firebaseAuth.getCurrentUser().getUid();

        if (isWishlisted) {
            db.collection("users").document(uid)
                    .collection("wishlist")
                    .whereEqualTo("productId", productId)
                    .get()
                    .addOnSuccessListener(querySnapshots -> {
                        if (!isAdded()) return;
                        for (QueryDocumentSnapshot doc : querySnapshots) {
                            doc.getReference().delete();
                        }
                        isWishlisted = false;
                        updateWishlistButtons();
                        Toast.makeText(getContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", productId);
            db.collection("users").document(uid)
                    .collection("wishlist")
                    .add(item)
                    .addOnSuccessListener(ref -> {
                        if (!isAdded()) return;
                        isWishlisted = true;
                        updateWishlistButtons();
                        Toast.makeText(getContext(), "Added to wishlist!", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateWishlistButtons() {
        if (binding == null) return;
        int color = isWishlisted
                ? ContextCompat.getColor(requireContext(), R.color.md_theme_error)
                : ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        binding.productDetailsBtnWishlist.setColorFilter(color);
        binding.productDetailsBtnWishlistBar.setColorFilter(color);
    }

    private void loadTopSellProduct() {
        db.collection("products")
                .whereNotEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(qds -> {
                    if (!isAdded() || binding == null || qds.isEmpty()) return;

                    List<Product> products = qds.toObjects(Product.class);
                    binding.productDetailsTopSellSection.itemSectionContainer.setLayoutManager(
                            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

                    SectionAdapter adapter = new SectionAdapter(products, product -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("productId", product.getProductId());
                        ProductDetailsFragment fragment = new ProductDetailsFragment();
                        fragment.setArguments(bundle);
                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    });

                    binding.productDetailsTopSellSection.itemSectionTitle.setText("Top Selling Products");
                    binding.productDetailsTopSellSection.itemSectionContainer.setAdapter(adapter);
                });
    }

    private void renderAttribute(Product.Attribute attribute, ViewGroup container) {
        LinearLayout section = new LinearLayout(getContext());
        section.setOrientation(LinearLayout.VERTICAL);

        int pad = dpToPx(16);
        int marginBottom = dpToPx(12);
        section.setPadding(pad, pad, pad, pad);

        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sectionParams.setMargins(0, 0, 0, marginBottom);
        section.setLayoutParams(sectionParams);


        TextView label = new TextView(getContext());
        label.setText(attribute.getName().toUpperCase());
        label.setTextSize(11f);
        label.setLetterSpacing(0.07f);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.setMargins(0, 0, 0, dpToPx(10));
        label.setLayoutParams(labelParams);
        section.addView(label);


        ChipGroup group = new ChipGroup(getContext());
        group.setSelectionRequired(true);
        group.setSingleSelection(true);

        boolean isColor = "color".equals(attribute.getType());

        attribute.getValues().forEach(value -> {
            Chip chip = new Chip(getContext());
            chip.setCheckable(true);
            chip.setTag(value);
            chip.setEnsureMinTouchTargetSize(false);

            if (isColor) {
                int size = dpToPx(40);
                ChipGroup.LayoutParams chipParams = new ChipGroup.LayoutParams(size, size);
                chip.setLayoutParams(chipParams);
                chip.setText("");
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(value)));
                chip.setChipCornerRadius(size / 2f);
                chip.setCheckedIconVisible(false);
                chip.setRippleColor(null);

                int[][] states = {{android.R.attr.state_checked}, {}};
                int[] strokeColors = {
                        ContextCompat.getColor(requireContext(), R.color.md_theme_primary),
                        Color.TRANSPARENT
                };
                chip.setChipStrokeWidth(dpToPx(2));
                chip.setChipStrokeColor(new ColorStateList(states, strokeColors));

            } else {
                chip.setText(value);
                chip.setTextSize(13f);
                chip.setCheckedIconVisible(false);

                int[][] states = {{android.R.attr.state_checked}, {}};
                int[] bgColors = {
                        ContextCompat.getColor(requireContext(), R.color.md_theme_primaryContainer),
                        ContextCompat.getColor(requireContext(), R.color.md_theme_surfaceVariant)
                };
                int[] strokeColors = {
                        ContextCompat.getColor(requireContext(), R.color.md_theme_primary),
                        ContextCompat.getColor(requireContext(), R.color.md_theme_outlineVariant)
                };
                int[] textColors = {
                        ContextCompat.getColor(requireContext(), R.color.md_theme_primary),
                        ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant)
                };

                chip.setChipBackgroundColor(new ColorStateList(states, bgColors));
                chip.setChipStrokeWidth(1.5f);
                chip.setChipStrokeColor(new ColorStateList(states, strokeColors));
                chip.setTextColor(new ColorStateList(states, textColors));
            }

            group.addView(chip);
        });


        if (group.getChildCount() > 0) {
            ((Chip) group.getChildAt(0)).setChecked(true);
        }

        section.addView(group);
        container.addView(section);
        attributeGroups.put(attribute.getName(), group);
    }

    private List<CartItem.Attribute> getFinalSelections() {
        List<CartItem.Attribute> attributes = new ArrayList<>();
        for (Map.Entry<String, ChipGroup> entry : attributeGroups.entrySet()) {
            ChipGroup chipGroup = entry.getValue();
            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId != View.NO_ID) {
                Chip chip = chipGroup.findViewById(checkedChipId);
                if (chip != null && chip.getTag() != null) {
                    attributes.add(new CartItem.Attribute(entry.getKey(), chip.getTag().toString()));
                }
            }
        }
        return attributes;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onStop() {
        super.onStop();
        View nav = getActivity().findViewById(R.id.bottom_navigation_view);
        if (nav != null) nav.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        View nav = getActivity().findViewById(R.id.bottom_navigation_view);
        if (nav != null) nav.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}