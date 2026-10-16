package lk.susa.plant_lk.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.adapter.WishlistAdapter;
import lk.susa.plant_lk.model.Product;

public class WishlistFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private View emptyState;
    private TextView wishlistCount;
    private WishlistAdapter adapter;
    private List<Product> wishlistProducts = new ArrayList<>();

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    public WishlistFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.wishlist_recycler);
        emptyState = view.findViewById(R.id.wishlist_empty_state);
        wishlistCount = view.findViewById(R.id.wishlist_count);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new WishlistAdapter(wishlistProducts, new WishlistAdapter.OnWishlistActionListener() {
            @Override
            public void onProductClick(Product product) {
                Bundle bundle = new Bundle();
                bundle.putString("productId", product.getProductId());

                ProductDetailsFragment fragment = new ProductDetailsFragment();
                fragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onRemoveClick(Product product, int position) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) return;
                showLoading();


                db.collection("users").document(user.getUid())
                        .collection("wishlist")
                        .whereEqualTo("productId", product.getProductId())
                        .get()
                        .addOnSuccessListener(querySnapshots -> {
                            for (QueryDocumentSnapshot doc : querySnapshots) {
                                doc.getReference().delete();
                            }
                            hideLoading();
                            adapter.removeItem(position);
                            updateCountAndEmptyState();
                            Toast.makeText(getContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        recyclerView.setAdapter(adapter);
        loadWishlist();
    }

    private void loadWishlist() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            showEmptyState();
            return;
        }
        showLoading();

        db.collection("users").document(user.getUid())
                .collection("wishlist")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    wishlistProducts.clear();

                    if (querySnapshots.isEmpty()) {
                        hideLoading();
                        showEmptyState();
                        return;
                    }

                    int[] loadedCount = {0};
                    int total = querySnapshots.size();

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String productId = doc.getString("productId");
                        if (productId == null) {
                            loadedCount[0]++;
                            if (loadedCount[0] == total) finishLoading();
                            continue;
                        }

                        db.collection("products")
                                .whereEqualTo("productId", productId)
                                .get()
                                .addOnSuccessListener(productSnap -> {
                                    if (!productSnap.isEmpty()) {
                                        Product product = productSnap.getDocuments().get(0).toObject(Product.class);
                                        if (product != null) wishlistProducts.add(product);
                                    }
                                    loadedCount[0]++;
                                    if (loadedCount[0] == total)

                                        hideLoading();

                                        finishLoading();
                                });
                    }
                })
                .addOnFailureListener(e -> showEmptyState());
    }

    private void finishLoading() {
        adapter.notifyDataSetChanged();
        updateCountAndEmptyState();
    }

    private void updateCountAndEmptyState() {
        int count = wishlistProducts.size();
        wishlistCount.setText(count + " item" + (count == 1 ? "" : "s"));

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
        wishlistCount.setText("0 items");
    }
}