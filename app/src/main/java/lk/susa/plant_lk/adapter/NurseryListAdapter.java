package lk.susa.plant_lk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import lk.susa.plant_lk.R;
import lk.susa.plant_lk.model.Nursery;

public class NurseryListAdapter extends RecyclerView.Adapter<NurseryListAdapter.VH> {
    private final List<Nursery> list;
    private final OnClick listener;

    public interface OnClick {
        void onClick(Nursery n);
    }

    public NurseryListAdapter(List<Nursery> l, OnClick c) {
        this.list = l;
        this.listener = c;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_nursery, p, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int p) {
        Nursery n = list.get(p);
        h.tvName.setText(n.getName());
        h.tvAddress.setText(n.getAddress());
        h.tvHours.setText(n.getHours());
        h.itemView.setOnClickListener(v -> listener.onClick(n));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvHours;

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_item_name);
            tvAddress = v.findViewById(R.id.tv_item_address);
            tvHours = v.findViewById(R.id.tv_item_hours);
        }
    }
}