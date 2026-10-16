package lk.susa.plant_lk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import lk.susa.plant_lk.R;
import lk.susa.plant_lk.model.Nursery;

public class NurseryMapAdapter extends RecyclerView.Adapter<NurseryMapAdapter.MapViewHolder> {

    private final List<Nursery> nurseries;
    private final OnNurseryClickListener listener;

    public interface OnNurseryClickListener {
        void onNurseryClick(Nursery nursery);
    }

    public NurseryMapAdapter(List<Nursery> nurseries, OnNurseryClickListener listener) {
        this.nurseries = nurseries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nursery_map, parent, false);
        return new MapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MapViewHolder holder, int position) {
        holder.bind(nurseries.get(position));
    }

    @Override
    public int getItemCount() {
        return nurseries.size();
    }

    class MapViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
        private final MapView mapView;
        private final TextView name;
        private final TextView address;
        private GoogleMap googleMap;
        private Nursery currentNursery;

        MapViewHolder(@NonNull View itemView) {
            super(itemView);
            mapView = itemView.findViewById(R.id.nursery_item_map);
            name = itemView.findViewById(R.id.nursery_item_name);
            address = itemView.findViewById(R.id.nursery_item_address);

            if (mapView != null) {
                mapView.onCreate(null);
                mapView.getMapAsync(this);
            }

            itemView.setOnClickListener(v -> listener.onNurseryClick(currentNursery));
        }

        void bind(Nursery nursery) {
            this.currentNursery = nursery;
            name.setText(nursery.getName());
            address.setText(nursery.getAddress());
            updateMapContents();
        }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            this.googleMap = googleMap;
            this.googleMap.getUiSettings().setMapToolbarEnabled(false);
            updateMapContents();
        }

        private void updateMapContents() {
            if (googleMap == null || currentNursery == null) return;

            LatLng location = new LatLng(currentNursery.getLatitude(), currentNursery.getLongitude());
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f));
        }
    }
}