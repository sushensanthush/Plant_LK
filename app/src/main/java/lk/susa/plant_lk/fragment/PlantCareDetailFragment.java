package lk.susa.plant_lk.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.model.PlantCareTip;
import lk.susa.plant_lk.database.DatabaseHelper;

public class PlantCareDetailFragment extends Fragment {

    private static final String ARG_TIP = "plant_care_tip";
    private PlantCareTip tip;

    private ImageView image;
    private TextView title;
    private TextView description;
    private TextView treatments;
    private TextView pests;
    private TextView sideEffects;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static PlantCareDetailFragment newInstance(PlantCareTip tip) {
        PlantCareDetailFragment fragment = new PlantCareDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIP, tip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tip = (PlantCareTip) getArguments().getSerializable(ARG_TIP);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_plant_care_detail, container, false);

        image = view.findViewById(R.id.detail_image);
        title = view.findViewById(R.id.detail_title);
        description = view.findViewById(R.id.detail_description);
        treatments = view.findViewById(R.id.detail_treatments);
        pests = view.findViewById(R.id.detail_pests);
        sideEffects = view.findViewById(R.id.detail_side_effects);

        ImageView btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        if (tip != null) {
            image.setImageResource(tip.getImageRes());
            title.setText(tip.getTitle());
            description.setText(tip.getDescription());
            loadTipFromDatabase(tip.getTitle());
        }

        return view;
    }

    private void loadTipFromDatabase(String tipTitle) {
        executor.execute(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(getContext());
            PlantCareTip dbTip = dbHelper.getTipByTitle(tipTitle);

            mainHandler.post(() -> {
                if (dbTip != null) {
                    Log.d("DB_DEBUG", "Data found in SQLite for: " + tipTitle);

                    tip.setTreatments(dbTip.getTreatments());
                    tip.setPests(dbTip.getPests());
                    tip.setSideEffects(dbTip.getSideEffects());

                    treatments.setText(dbTip.getTreatments());
                    pests.setText(dbTip.getPests());
                    sideEffects.setText(dbTip.getSideEffects());
                } else {
                    Log.d("DB_DEBUG", "No data found in SQLite for: " + tipTitle);
                    treatments.setText("");
                    pests.setText("");
                    sideEffects.setText("");
                }
            });
        });
    }
}