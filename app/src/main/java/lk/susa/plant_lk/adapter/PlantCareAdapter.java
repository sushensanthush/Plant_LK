package lk.susa.plant_lk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.fragment.PlantCareDetailFragment;
import lk.susa.plant_lk.model.PlantCareTip;

public class PlantCareAdapter extends RecyclerView.Adapter<PlantCareAdapter.CareViewHolder> {

    private List<PlantCareTip> careTips;

    public PlantCareAdapter(List<PlantCareTip> careTips) {
        this.careTips = careTips;
    }

    @NonNull
    @Override
    public CareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant_care, parent, false);
        return new CareViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CareViewHolder holder, int position) {
        PlantCareTip tip = careTips.get(position);
        holder.title.setText(tip.getTitle());
        holder.description.setText(tip.getDescription());
        holder.image.setImageResource(tip.getImageRes());

        holder.itemView.setOnClickListener(v -> {
            if (v.getContext() instanceof androidx.fragment.app.FragmentActivity) {
                ((androidx.fragment.app.FragmentActivity) v.getContext())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, PlantCareDetailFragment.newInstance(tip))
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return careTips.size();
    }

    static class CareViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, description;

        public CareViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.care_image);
            title = itemView.findViewById(R.id.care_title);
            description = itemView.findViewById(R.id.care_description);
        }
    }
}