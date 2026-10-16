package lk.susa.plant_lk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.model.Product;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> implements Filterable {

    private List<Product> products;
    private List<Product> productsAll;
    private OnListingItemClickListener listener;
    private Map<String, Boolean> wishlistState = new HashMap<>();

    public ListingAdapter(List<Product> products, OnListingItemClickListener listener) {
        this.products = new ArrayList<>(products);
        this.productsAll = new ArrayList<>(products);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText("LKR " + product.getPrice());

        if (product.getImages() != null && !product.getImages().isEmpty()) {

            Glide.with(holder.itemView.getContext())
                    .load(product.getImages().get(0))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .placeholder(R.drawable.plantlk)
                    .error(R.drawable.error)
                    .into(holder.productImage);
        } else {

            holder.productImage.setImageResource(R.drawable.plantlk);
        }

        boolean isWishlisted = Boolean.TRUE.equals(wishlistState.get(product.getProductId()));
        updateHeartIcon(holder.wishlistBtn, isWishlisted);

        holder.itemView.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.click_animation);
            v.startAnimation(animation);
            if (listener != null) listener.onListingItemClick(product);
        });

        holder.wishlistBtn.setOnClickListener(v -> toggleWishlist(v, product, holder.wishlistBtn));


    }

    private void toggleWishlist(View v, Product product, ImageView heartIcon) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(v.getContext(), "Please sign in to use wishlist", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isWishlisted = Boolean.TRUE.equals(wishlistState.get(product.getProductId()));

        if (isWishlisted) {
            FirebaseFirestore.getInstance()
                    .collection("users").document(user.getUid())
                    .collection("wishlist")
                    .whereEqualTo("productId", product.getProductId())
                    .get()
                    .addOnSuccessListener(querySnapshots -> {
                        for (QueryDocumentSnapshot doc : querySnapshots) {
                            doc.getReference().delete();
                        }
                        wishlistState.put(product.getProductId(), false);
                        updateHeartIcon(heartIcon, false);
                        Toast.makeText(v.getContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", product.getProductId());

            FirebaseFirestore.getInstance()
                    .collection("users").document(user.getUid())
                    .collection("wishlist")
                    .add(item)
                    .addOnSuccessListener(ref -> {
                        wishlistState.put(product.getProductId(), true);
                        updateHeartIcon(heartIcon, true);
                        Toast.makeText(v.getContext(), "Added to wishlist!", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateHeartIcon(ImageView imageView, boolean isWishlisted) {
        if (isWishlisted) {
            imageView.setColorFilter(
                    ContextCompat.getColor(imageView.getContext(), R.color.md_theme_error));
        } else {
            imageView.setColorFilter(
                    ContextCompat.getColor(imageView.getContext(), R.color.md_theme_primary));
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private final Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Product> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(productsAll);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Product product : productsAll) {
                    if (product.getTitle().toLowerCase().contains(filterPattern)) {
                        filteredList.add(product);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            products.clear();
            products.addAll((List<Product>) results.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage, wishlistBtn;
        TextView productTitle, productPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.listing_item_image);
            wishlistBtn = itemView.findViewById(R.id.listing_item_wishlist);
            productTitle = itemView.findViewById(R.id.listing_item_name);
            productPrice = itemView.findViewById(R.id.listing_item_price);
        }
    }

    public interface OnListingItemClickListener {
        void onListingItemClick(Product product);
    }
}