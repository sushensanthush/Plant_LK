package lk.susa.plant_lk.fragment;

import lk.susa.plant_lk.BuildConfig;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.model.Nursery;
import lk.susa.plant_lk.adapter.NurseryListAdapter;

public class NurseryMapFragment extends Fragment implements OnMapReadyCallback, SensorEventListener {
    private MaterialCardView cardCountBadge;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LatLng userCurrentLocation;
    private Marker userMarker;
    private Polyline currentRoute;
    private boolean isFirstLocation = true;
    private static final int PERMISSION_CODE = 100;

    private FirebaseFirestore db;

    private final List<Nursery> nurseryList = new ArrayList<>();
    private final List<Nursery> filteredList = new ArrayList<>();
    private final List<Marker> nurseryMarkers = new ArrayList<>();
    private final String GOOGLE_API_KEY = BuildConfig.DIRECTIONS_API_KEY;
    private final ExecutorService routeExecutor = Executors.newSingleThreadExecutor();

    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable;
    private boolean longPressTriggered = false;
    private static final long LONG_PRESS_MS = 650L;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private static final float SHAKE_THRESHOLD = 12f;
    private static final long SHAKE_COOLDOWN = 1500L;

    private View bottomSheet, nurseryDrawer;
    private EditText search;
    private TextView tvSheetName, tvSheetAddress, tvSheetHours,
            tvSheetPhone, tvSheetDistance, tvNurseryCount;
    private MaterialButton btnSheetRoute, btnSheetCall;
    private ImageButton btnSheetClose, btnDrawerClose, btnZoomIn, btnZoomOut;
    private RecyclerView rvNurseries;
    private NurseryListAdapter listAdapter;
    private Nursery selectedNursery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nursery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000)
                .setMinUpdateIntervalMillis(2000).build();

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        bottomSheet = view.findViewById(R.id.bottom_sheet_nursery);
        nurseryDrawer = view.findViewById(R.id.nursery_drawer);
        search = view.findViewById(R.id.search);
        tvSheetName = view.findViewById(R.id.tv_sheet_name);
        tvSheetAddress = view.findViewById(R.id.tv_sheet_address);
        tvSheetHours = view.findViewById(R.id.tv_sheet_hours);
        tvSheetPhone = view.findViewById(R.id.tv_sheet_phone);
        tvSheetDistance = view.findViewById(R.id.tv_sheet_distance);
        tvNurseryCount = view.findViewById(R.id.tv_nursery_count);
        btnSheetRoute = view.findViewById(R.id.btn_sheet_route);
        btnSheetCall = view.findViewById(R.id.btn_sheet_call);
        btnSheetClose = view.findViewById(R.id.btn_sheet_close);
        btnDrawerClose = view.findViewById(R.id.btn_drawer_close);
        rvNurseries = view.findViewById(R.id.rv_nurseries);
        cardCountBadge = view.findViewById(R.id.card_count_badge);


        listAdapter = new NurseryListAdapter(filteredList, nursery -> {
            hideNurseryDrawer();
            if (mMap != null) {
                LatLng pos = new LatLng(nursery.getLatitude(), nursery.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f));
                for (Marker m : nurseryMarkers) {
                    if (m.getTag() instanceof Nursery && ((Nursery) m.getTag()).getName().equals(nursery.getName())) {
                        m.showInfoWindow();
                        break;
                    }
                }
            }
        });
        rvNurseries.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNurseries.setAdapter(listAdapter);


        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNurseries(s.toString());
            }
        });


        btnZoomIn = view.findViewById(R.id.btn_zoom_in);
        btnZoomOut = view.findViewById(R.id.btn_zoom_out);
        btnZoomIn.setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomIn());
        });
        btnZoomOut.setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomOut());
        });

        cardCountBadge.setOnClickListener(v -> showNurseryDrawer());
        btnDrawerClose.setOnClickListener(v -> hideNurseryDrawer());
        btnSheetClose.setOnClickListener(v -> hideBottomSheet());

        btnSheetRoute.setOnClickListener(v -> {
            if (selectedNursery != null) {
                LatLng dest = new LatLng(selectedNursery.getLatitude(), selectedNursery.getLongitude());
                fetchUserLocationAndDrawRoute(dest);
            }
            hideBottomSheet();
        });

        btnSheetCall.setOnClickListener(v -> {
            if (selectedNursery != null) dialPhone(selectedNursery.getPhone());
        });

        view.findViewById(R.id.btn_my_location).setOnClickListener(v -> zoomToUser());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.nursery_map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        loadNurseriesFromFirebase();
    }

    private void loadNurseriesFromFirebase() {
        db.collection("nurseries").get().addOnSuccessListener(snapshot -> {
            nurseryList.clear();
            for (QueryDocumentSnapshot doc : snapshot) {
                Nursery n = doc.toObject(Nursery.class);
                n.setDocId(doc.getId());
                nurseryList.add(n);
            }
            tvNurseryCount.setText(nurseryList.size() + " Nurseries");
            filterNurseries(search.getText().toString());
            if (mMap != null) placeNurseryMarkers();
        });
    }

    private void placeNurseryMarkers() {
        for (Marker m : nurseryMarkers) m.remove();
        nurseryMarkers.clear();
        for (Nursery n : nurseryList) {
            LatLng pos = new LatLng(n.getLatitude(), n.getLongitude());
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(n.getName())
                    .snippet("Tap · Route | Long-press · Details")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            if (m != null) {
                m.setTag(n);
                nurseryMarkers.add(m);
            }
        }
    }

    private void filterNurseries(String query) {
        filteredList.clear();
        String q = query.toLowerCase(Locale.ROOT).trim();
        for (Nursery n : nurseryList) {
            if (q.isEmpty() || n.getName().toLowerCase(Locale.ROOT).contains(q)) {
                filteredList.add(n);
            }
        }
        if (listAdapter != null) listAdapter.notifyDataSetChanged();

        if (mMap != null) {
            for (Marker m : nurseryMarkers) {
                Nursery n = (Nursery) m.getTag();
                boolean match = q.isEmpty() || n.getName().toLowerCase(Locale.ROOT).contains(q);
                m.setIcon(BitmapDescriptorFactory.defaultMarker(match ? BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_CYAN));
                m.setAlpha(match ? 1f : 0.4f);
            }
        }
    }

    private void showNurseryDetails(Nursery n) {
        tvSheetName.setText(n.getName());
        tvSheetAddress.setText(n.getAddress());
        tvSheetPhone.setText(n.getPhone());
        tvSheetHours.setText(n.getHours());

        boolean open = n.getHours() != null && n.getHours().toLowerCase(Locale.ROOT).startsWith("open");
        tvSheetHours.setTextColor(ContextCompat.getColor(requireContext(), open ? R.color.md_theme_tertiary : R.color.md_theme_error));

        if (userCurrentLocation != null) {
            float[] r = new float[1];
            Location.distanceBetween(userCurrentLocation.latitude, userCurrentLocation.longitude, n.getLatitude(), n.getLongitude(), r);
            tvSheetDistance.setText(String.format(Locale.ROOT, "%.1f km away", r[0] / 1000f));
        }

        bottomSheet.setVisibility(View.VISIBLE);
        bottomSheet.setTranslationY(bottomSheet.getHeight() + 300);
        bottomSheet.animate()
                .translationY(0)
                .setDuration(380)
                .setInterpolator(new OvershootInterpolator(0.8f))
                .start();
    }


    private void showNurseryDrawer() {
        if (nurseryDrawer == null) return;
        nurseryDrawer.setVisibility(View.VISIBLE);
        if (cardCountBadge != null) cardCountBadge.setVisibility(View.GONE);
        nurseryDrawer.setTranslationX(-800);
        nurseryDrawer.animate().translationX(0).setDuration(320).setInterpolator(new OvershootInterpolator(0.6f)).start();
    }

    private void hideNurseryDrawer() {
        if (nurseryDrawer == null) return;
        nurseryDrawer.animate().translationX(-800).setDuration(260).withEndAction(() -> {
            nurseryDrawer.setVisibility(View.GONE);
            if (cardCountBadge != null) cardCountBadge.setVisibility(View.VISIBLE);
        }).start();
    }

    private void hideBottomSheet() {
        bottomSheet.animate().translationY(bottomSheet.getHeight() + 300).setDuration(280).withEndAction(() -> bottomSheet.setVisibility(View.GONE)).start();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        checkLocationPermission();
        if (!nurseryList.isEmpty()) placeNurseryMarkers();

        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() instanceof Nursery) {
                longPressHandler.removeCallbacks(longPressRunnable);
                longPressRunnable = () -> {
                    longPressTriggered = true;
                    selectedNursery = (Nursery) marker.getTag();
                    showNurseryDetails(selectedNursery);
                };
                longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_MS);
            }
            return false;
        });

        mMap.setOnInfoWindowClickListener(marker -> {
            if (longPressRunnable != null) longPressHandler.removeCallbacks(longPressRunnable);
            if (!longPressTriggered && marker.getTag() instanceof Nursery) {
                Nursery n = (Nursery) marker.getTag();
                fetchUserLocationAndDrawRoute(new LatLng(n.getLatitude(), n.getLongitude()));
            }
        });

        mMap.setOnMapClickListener(latLng -> {
            if (longPressRunnable != null) longPressHandler.removeCallbacks(longPressRunnable);
            hideBottomSheet();
            hideNurseryDrawer();
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            startLocationUpdates();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                for (Location l : result.getLocations()) updateUserLocation(l);
            }
        }, Looper.getMainLooper());
    }

    private void updateUserLocation(Location l) {
        userCurrentLocation = new LatLng(l.getLatitude(), l.getLongitude());
        if (userMarker == null) {
            userMarker = mMap.addMarker(new MarkerOptions().position(userCurrentLocation).title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        } else {
            userMarker.setPosition(userCurrentLocation);
        }
        if (isFirstLocation) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 15f));
            isFirstLocation = false;
        }
    }

    private void zoomToUser() {
        if (userCurrentLocation != null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userCurrentLocation, 16f));
        else Toast.makeText(getContext(), "Fetching location…", Toast.LENGTH_SHORT).show();
    }

    private void fetchUserLocationAndDrawRoute(LatLng dest) {
        if (userCurrentLocation != null) drawRoute(userCurrentLocation, dest);
    }

    private void drawRoute(LatLng start, LatLng end) {
        routeExecutor.execute(() -> {
            try (GeoApiContext ctx = new GeoApiContext.Builder().apiKey(GOOGLE_API_KEY).build()) {
                DirectionsResult res = DirectionsApi.newRequest(ctx)
                        .mode(TravelMode.DRIVING)
                        .origin(start.latitude + "," + start.longitude)
                        .destination(end.latitude + "," + end.longitude)
                        .await();
                if (res.routes.length > 0) {
                    List<LatLng> pts = PolyUtil.decode(res.routes[0].overviewPolyline.getEncodedPath());
                    requireActivity().runOnUiThread(() -> {
                        if (currentRoute != null) currentRoute.remove();
                        currentRoute = mMap.addPolyline(new PolylineOptions()
                                .addAll(pts).color(0xFF2E7D32).width(10f)
                                .jointType(JointType.ROUND).startCap(new RoundCap()).endCap(new RoundCap()));
                        LatLngBounds.Builder b = new LatLngBounds.Builder().include(start).include(end);
                        for (LatLng p : pts) b.include(p);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(b.build(), 150));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0], y = event.values[1], z = event.values[2];
            if (Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH > SHAKE_THRESHOLD) {
                long now = System.currentTimeMillis();
                if (now - lastShakeTime > SHAKE_COOLDOWN) {
                    lastShakeTime = now;
                    zoomToUser();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor s, int a) {
    }

    private void dialPhone(String p) {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + p)));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (fusedLocationClient != null && locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        routeExecutor.shutdown();
    }
}