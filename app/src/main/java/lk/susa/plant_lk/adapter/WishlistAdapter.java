package lk.susa.plant_lk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.model.Product;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<Product> products;
    private OnWishlistActionListener listener;

    public WishlistAdapter(List<Product> products, OnWishlistActionListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wishlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.name.setText(product.getTitle());
        holder.price.setText("LKR " + product.getPrice());

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImages().get(0))
                    .centerCrop()
                    .placeholder(R.drawable.person_24)
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
        });

        // Remove from wishlist
        holder.removeBtn.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveClick(product, position);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void removeItem(int position) {
        products.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, products.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, removeBtn;
        TextView name, price;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.wishlist_item_image);
            removeBtn = itemView.findViewById(R.id.wishlist_item_remove);
            name = itemView.findViewById(R.id.wishlist_item_name);
            price = itemView.findViewById(R.id.wishlist_item_price);
        }
    }

    public interface OnWishlistActionListener {
        void onProductClick(Product product);
        void onRemoveClick(Product product, int position);
    }
}
