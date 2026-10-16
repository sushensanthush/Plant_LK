package lk.susa.plant_lk.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.adapter.PlantCareAdapter;
import lk.susa.plant_lk.model.PlantCareTip;

public class CareFragment extends Fragment {

    private RecyclerView careRecyclerView;
    private PlantCareAdapter careAdapter;
    private List<PlantCareTip> careTips;
    private List<PlantCareTip> filteredTips;

    public static List<PlantCareTip> getCareTips() {
        List<PlantCareTip> tips = new ArrayList<>();
        tips.add(new PlantCareTip(R.drawable.plant_water, "Watering",
                "Water your plants 2-3 times a week depending on species. Check soil moisture before watering to avoid root rot. " +
                        "Use lukewarm water for sensitive plants and water in the morning to reduce evaporation."));
        tips.add(new PlantCareTip(R.drawable.plant_sunlight, "Sunlight",
                "Place your plants in indirect sunlight for optimal growth. " +
                        "Adjust according to species: succulents prefer bright light, while ferns thrive in low light. " +
                        "Rotate plants weekly to ensure even growth."));
        tips.add(new PlantCareTip(R.drawable.plant_fertilizer, "Fertilizer",
                "Use organic fertilizer once a month for healthy growth. " +
                        "For flowering plants, apply a bloom-boosting fertilizer every 6 weeks. " +
                        "Avoid over-fertilizing as it can damage roots."));
        tips.add(new PlantCareTip(R.drawable.plant_pruning, "Pruning",
                "Remove dead leaves and stems regularly to promote new growth. " +
                        "Trim overgrown branches to maintain shape and improve air circulation. " +
                        "Use clean, sharp tools to prevent infections."));
        tips.add(new PlantCareTip(R.drawable.plant_pest, "Pest Control",
                "Check for pests like aphids, mites, and mealybugs regularly. " +
                        "Treat naturally with neem oil or insecticidal soap. " +
                        "Quarantine new plants to prevent infestations."));
        tips.add(new PlantCareTip(R.drawable.plant_soil, "Soil Care",
                "Use well-draining soil suited to your plant type. " +
                        "Mix compost or perlite for better aeration. " +
                        "Repot every 1-2 years to refresh soil nutrients."));
        tips.add(new PlantCareTip(R.drawable.plant_humidity, "Humidity",
                "Maintain 40-60% humidity for tropical plants. " +
                        "Use a humidifier or place a water tray nearby. " +
                        "Group plants together to naturally increase humidity."));
        tips.add(new PlantCareTip(R.drawable.plant_temperature, "Temperature",
                "Keep indoor plants in a stable temperature range (18-24°C). " +
                        "Avoid placing them near heaters, air conditioners, or drafty windows."));
        tips.add(new PlantCareTip(R.drawable.plant_season, "Seasonal Care",
                "Adjust watering, sunlight, and fertilization according to season. " +
                        "Reduce watering in winter when growth slows. " +
                        "Increase light exposure during short winter days."));
        tips.add(new PlantCareTip(R.drawable.plant_support, "Support & Training",
                "Use stakes, trellises, or plant ties for climbing or heavy plants. " +
                        "Train vines gradually to prevent breakage and encourage proper growth direction."));
        return tips;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_care, container, false);

        careRecyclerView = view.findViewById(R.id.recycler_plant_care);
        careRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        careTips = getCareTips();
        filteredTips = new ArrayList<>(careTips);

        careAdapter = new PlantCareAdapter(filteredTips);
        careRecyclerView.setAdapter(careAdapter);

        TextInputEditText searchInput = view.findViewById(R.id.search_care);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void filterList(String query) {
        filteredTips.clear();
        if (query.isEmpty()) {
            filteredTips.addAll(careTips);
        } else {
            for (PlantCareTip tip : careTips) {
                if (tip.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        tip.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredTips.add(tip);
                }
            }
        }
        careAdapter.notifyDataSetChanged();
    }
}