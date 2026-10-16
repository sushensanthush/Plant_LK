package lk.susa.plant_lk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.model.PlantCareTip;

public class HomeCareAdapter extends RecyclerView.Adapter<HomeCareAdapter.ViewHolder> {

    private List<PlantCareTip> tips;
    private OnTipClickListener listener;

    public interface OnTipClickListener {
        void onTipClick(PlantCareTip tip);
    }

    public HomeCareAdapter(List<PlantCareTip> tips, OnTipClickListener listener) {
        this.tips = tips;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_care, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlantCareTip tip = tips.get(position);
        holder.title.setText(tip.getTitle());
        holder.image.setImageResource(tip.getImageRes());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTipClick(tip);
        });
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.home_care_image);
            title = itemView.findViewById(R.id.home_care_title);
        }
    }
}
