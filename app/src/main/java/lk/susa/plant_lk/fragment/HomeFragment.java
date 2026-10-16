package lk.susa.plant_lk.fragment;

import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.adapter.CategoryAdapter;
import lk.susa.plant_lk.adapter.HomeCareAdapter;
import lk.susa.plant_lk.adapter.NurseryMapAdapter;
import lk.susa.plant_lk.adapter.ProductAdapter;
import lk.susa.plant_lk.databinding.FragmentHomeBinding;
import lk.susa.plant_lk.model.Category;
import lk.susa.plant_lk.model.Nursery;
import lk.susa.plant_lk.model.PlantCareTip;
import lk.susa.plant_lk.model.Product;

import android.net.Uri;
import android.widget.VideoView;
import android.widget.ImageView;

public class HomeFragment extends BaseFragment {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private boolean statsAnimated = false;

    private MediaPlayer mediaPlayer;
    private boolean isMuted = true;
    private boolean isVideoVisible = false;

    private ExoPlayer exoPlayer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        setupHomeVideo();

        setupCategoryRecycler();
        setupTopSellingRecycler();
        setupNewArrivalsRecycler();
        setupCareRecycler();
        setupNurseriesRecycler();
        setupPromoCard();
        setupScrollListener();

        binding.homeSeeAllCategories.setOnClickListener(v ->
                navigateTo(new CategoryFragment()));

        binding.homeSeeAllProducts.setOnClickListener(v ->
                navigateTo(new CategoryFragment()));

        binding.homeSeeAllNewArrivals.setOnClickListener(v ->
                navigateTo(new CategoryFragment()));

        binding.homeSeeAllCare.setOnClickListener(v ->
                navigateTo(new CareFragment()));

        binding.homeSeeAllNurseries.setOnClickListener(v ->
                navigateTo(new NurseryMapFragment()));


        optimizeRecyclerViews();
    }
    private void setupHomeVideo() {
        PlayerView playerView = binding.homePlayerView;
        ImageButton muteBtn = binding.videoMuteBtn;

        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        playerView.setPlayer(exoPlayer);

        Uri videoUri = Uri.parse("android.resource://" +
                requireContext().getPackageName() + "/" + R.raw.plant_lk_video);

        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        exoPlayer.setMediaItem(mediaItem);

        exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
        exoPlayer.prepare();
        exoPlayer.play();

        // mute default
        exoPlayer.setVolume(0f);
        isMuted = true;
        muteBtn.setImageResource(R.drawable.volume_off_24px);

        muteBtn.setOnClickListener(v -> {
            if (isMuted) {
                exoPlayer.setVolume(1f);
                muteBtn.setImageResource(R.drawable.volume_up_24px);
            } else {
                exoPlayer.setVolume(0f);
                muteBtn.setImageResource(R.drawable.volume_off_24px);
            }
            isMuted = !isMuted;
        });
    }
    private void setupCareRecycler() {

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        binding.homeRecyclerCare.setLayoutManager(layoutManager);

        binding.homeRecyclerCare.setHasFixedSize(true);
        binding.homeRecyclerCare.setNestedScrollingEnabled(false);
        binding.homeRecyclerCare.setItemViewCacheSize(10);

        List<PlantCareTip> careTips = CareFragment.getCareTips();

        HomeCareAdapter careAdapter = new HomeCareAdapter(careTips, tip ->
                navigateTo(new CareFragment()));

        binding.homeRecyclerCare.setAdapter(careAdapter);
    }

    private void setupNurseriesRecycler() {
        binding.homeRecyclerNurseries.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        db.collection("nurseries").limit(6).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        List<Nursery> nurseries = querySnapshot.toObjects(Nursery.class);

                        NurseryMapAdapter adapter = new NurseryMapAdapter(nurseries, nursery ->
                                navigateTo(new NurseryMapFragment()));


                        binding.homeRecyclerNurseries.setAdapter(adapter);
                    }

                });
    }

    private void setupPromoCard() {
        binding.homePromoCard.setOnClickListener(v ->
                navigateTo(new CategoryFragment()));
    }

    private void setupScrollListener() {
        View root = binding.getRoot();
        if (root instanceof ScrollView) {
            ((ScrollView) root).setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {


                if (!statsAnimated) {
                    View statsRow = binding.homeStatsRow;
                    int[] location = new int[2];
                    statsRow.getLocationOnScreen(location);
                    int statsTop = location[1];
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;

                    if (statsTop < screenHeight) {
                        statsAnimated = true;
                        animateCounter(binding.statPlants, 0, 500, "500+", 1200);
                        animateCounter(binding.statNurseries, 0, 50, "50+", 1000);
                        animateCounter(binding.statCustomers, 0, 10000, "10K+", 1500);
                    }
                }

                int[] location = new int[2];
                binding.homePlayerView.getLocationOnScreen(location);
                int videoTop = location[1];
                int videoBottom = videoTop + binding.homePlayerView.getHeight();
                int screenHeight = getResources().getDisplayMetrics().heightPixels;

                if (videoBottom < 0 || videoTop > screenHeight) {

                    if (exoPlayer.isPlaying()) exoPlayer.pause();
                } else {

                    if (!exoPlayer.isPlaying()) exoPlayer.play();
                }
            });
        }
    }
    private void optimizeRecyclerViews() {

        binding.homeRecyclerCategories.setNestedScrollingEnabled(false);
        binding.homeRecyclerTopSelling.setNestedScrollingEnabled(false);
        binding.homeRecyclerNewArrivals.setNestedScrollingEnabled(false);
        binding.homeRecyclerCare.setNestedScrollingEnabled(false);
        binding.homeRecyclerNurseries.setNestedScrollingEnabled(false);

        binding.homeRecyclerCategories.setHasFixedSize(true);
        binding.homeRecyclerTopSelling.setHasFixedSize(true);
        binding.homeRecyclerNewArrivals.setHasFixedSize(true);
        binding.homeRecyclerCare.setHasFixedSize(true);
        binding.homeRecyclerNurseries.setHasFixedSize(true);
    }

    private void animateCounter(TextView textView, int from, int to, String finalText, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(duration);
        animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            if (value >= to) {
                textView.setText(finalText);
            } else if (value >= 1000) {
                textView.setText((value / 1000) + "K");
            } else {
                textView.setText(String.valueOf(value));
            }
        });
        animator.start();
    }

    private void setupCategoryRecycler() {
        showLoading();
        binding.homeRecyclerCategories.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        db.collection("categories").limit(8).get()
                .addOnSuccessListener(querySnapshot -> {
                    hideLoading();
                    List<Category> categories = querySnapshot.toObjects(Category.class);
                    CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("categoryId", category.getCategoryId());
                        ListingFragment fragment = new ListingFragment();
                        fragment.setArguments(bundle);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    binding.homeRecyclerCategories.setAdapter(adapter);
                })
        .addOnFailureListener(e -> {
            hideLoading();
        });
    }

    private void setupTopSellingRecycler() {
        binding.homeRecyclerTopSelling.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        db.collection("products").limit(10).get()
                .addOnSuccessListener(querySnapshot -> {
                    hideLoading();
                    List<Product> products = querySnapshot.toObjects(Product.class);
                    ProductAdapter adapter = new ProductAdapter(products, product -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("productId", product.getProductId());
                        ProductDetailsFragment fragment = new ProductDetailsFragment();
                        fragment.setArguments(bundle);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    binding.homeRecyclerTopSelling.setAdapter(adapter);
                });
    }

    private void setupNewArrivalsRecycler() {
        binding.homeRecyclerNewArrivals.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        db.collection("products")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    hideLoading();
                    List<Product> products = querySnapshot.toObjects(Product.class);
                    ProductAdapter adapter = new ProductAdapter(products, product -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("productId", product.getProductId());
                        ProductDetailsFragment fragment = new ProductDetailsFragment();
                        fragment.setArguments(bundle);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    binding.homeRecyclerNewArrivals.setAdapter(adapter);
                });
    }

    private void navigateTo(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        statsAnimated = false;

        binding.statPlants.setText("0");
        binding.statNurseries.setText("0");
        binding.statCustomers.setText("0");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) exoPlayer.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        binding = null;
    }

}