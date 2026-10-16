package lk.susa.plant_lk.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.Arrays;
import java.util.List;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.adapter.ListingAdapter;
import lk.susa.plant_lk.databinding.FragmentListingBinding;
import lk.susa.plant_lk.model.Product;

public class ListingFragment extends Fragment {

    private FragmentListingBinding binding;
    private ListingAdapter adapter;
    private String categoryId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.recyclerViewListing.setLayoutManager(new GridLayoutManager(getContext(), 2));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        Product p1 = new Product("pid1", "Snake Plant", "Low maintenance indoor plant.", 1500, "cat1", Arrays.asList("image1", "image2"), 10, true);
//        Product p2 = new Product("pid2", "Mango Plant", "Tropical fruit plant for gardens.", 2500, "cat2", Arrays.asList("image1", "image2"), 8, true);
//        Product p3 = new Product("pid3", "Organic Compost", "Natural fertilizer for healthy plant growth.", 800, "cat3", Arrays.asList("image1", "image2"), 20, true);
//        Product p4 = new Product("pid4", "Tomato Seeds", "High yield hybrid seeds.", 300, "cat4", Arrays.asList("image1", "image2"), 50, true);
//        Product p5 = new Product("pid5", "Rose Plant", "Red flowering plant for gardens.", 2000, "cat5", Arrays.asList("image1", "image2"), 10, true);
//        Product p6 = new Product("pid6", "Ceramic Plant Pot", "Stylish indoor planter pot.", 1200, "cat6", Arrays.asList("image1", "image2"), 15, true);
//        Product p7 = new Product("pid7", "Garden Tool Set", "Includes shovel, rake, and pruner.", 3500, "cat7", Arrays.asList("image1", "image2"), 12, true);
//        Product p8 = new Product("pid8", "Aloe Vera", "Easy-care succulent with medicinal uses.", 900, "cat8", Arrays.asList("image1", "image2"), 18, true);
//        Product p9 = new Product("pid9", "Mint Plant", "Fresh herb plant for cooking.", 600, "cat9", Arrays.asList("image1", "image2"), 25, true);
//        Product p10 = new Product("pid10", "Watering Can", "Ergonomic watering can for plants.", 950, "cat10", Arrays.asList("image1", "image2"), 20, true);
//        Product p11 = new Product("pid11", "Home Gardening Guide", "Beginner-friendly gardening book.", 1500, "cat11", Arrays.asList("image1", "image2"), 30, true);
//        Product p12 = new Product("pid12", "Hydroponic Grow Kit", "Complete indoor growing system.", 8500, "cat12", Arrays.asList("image1", "image2"), 5, true);
//
//
//        List<Product> list = List.of(
//                p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12
//        );
//
//        WriteBatch batch = db.batch();
//
//        for (Product p : list) {
//            DocumentReference ref = db.collection("products").document();
//            batch.set(ref, p);
//        }
//
//        batch.commit();

//        Product p12 = new Product("pid12", "Peace Lily", "Spathiphyllum, commonly known as Peace Lily, is an elegant indoor plant with white blooms that purify indoor air by removing toxins. Thrives in low light and requires moderate watering.", 1200, "cat1", Arrays.asList("https://m.media-amazon.com/images/I/61peace1.jpg", "https://m.media-amazon.com/images/I/61peace2.jpg"), 12, true);
//        Product p13= new Product("pid13", "Fiddle Leaf Fig", "Ficus lyrata, or Fiddle Leaf Fig, is a striking indoor plant with large, violin-shaped leaves. Prefers bright, indirect light and regular watering, making it perfect for living rooms and offices.", 2200, "cat1", Arrays.asList("https://m.media-amazon.com/images/I/61fiddle1.jpg", "https://m.media-amazon.com/images/I/61fiddle2.jpg"), 12, true);
//        Product p14 = new Product("pid14", "ZZ Plant", "Zamioculcas zamiifolia (ZZ Plant) is a hardy indoor plant that tolerates low light and irregular watering. Its glossy green leaves add a touch of elegance to any indoor space.", 1400, "cat1", Arrays.asList("https://m.media-amazon.com/images/I/61zz1.jpg", "https://m.media-amazon.com/images/I/61zz2.jpg"), 15, true);
//        Product p15 = new Product("pid15", "Spider Plant", "Chlorophytum comosum, or Spider Plant, is a resilient indoor plant that produces arching leaves and small plantlets. Great for beginners, it also improves air quality.", 800, "cat1", Arrays.asList("https://m.media-amazon.com/images/I/61spider1.jpg", "https://m.media-amazon.com/images/I/61spider2.jpg"), 20, true);
//        Product p16 = new Product("pid16", "Rubber Plant", "Ficus elastica (Rubber Plant) is a popular indoor plant with large, glossy leaves. Prefers bright indirect light and occasional watering. Adds a bold statement to home interiors.", 1800, "cat1", Arrays.asList("https://m.media-amazon.com/images/I/61rubber1.jpg", "https://m.media-amazon.com/images/I/61rubber2.jpg"), 9, true);
//        Product p17 = new Product("pid17", "Pothos", "Epipremnum aureum, also known as Devil's Ivy or Pothos, is a trailing indoor plant that grows easily in low light. Very forgiving, it purifies air and looks great in hanging pots or shelves.", 1000, "cat1", Arrays.asList("https://m.media-amazon.com/images/I/61pothos1.jpg", "https://m.media-amazon.com/images/I/61pothos2.jpg"), 22, true);
//        Product p18 = new Product("pid18", "Boston Fern", "Nephrolepis exaltata (Boston Fern) is a lush indoor fern with arching green fronds. Thrives in humid environments, improves indoor air quality, and adds a vibrant touch to home decor.", 1300, "cat1", Arrays.asList("https://m.media-amazon.com/images/I/61fern1.jpg", "https://m.media-amazon.com/images/I/61fern2.jpg"), 16, true);
//        Product p19 = new Product("pid19", "Calathea", "Calathea is a decorative indoor plant with large, patterned leaves that fold at night. Prefers indirect light and regular watering, making it a perfect indoor statement plant.", 1500, "cat1", Arrays.asList("https://m.media-amazon.com/images/I/61calathea1.jpg", "https://m.media-amazon.com/images/I/61calathea2.jpg"), 14, true);
//        Product p20 = new Product("pid20", "Monstera Deliciosa", "Monstera Deliciosa, or Swiss Cheese Plant, is an iconic indoor plant with large, fenestrated leaves. Thrives in bright, indirect light and moderate watering, adding a tropical vibe to any room.", 2000, "cat1", Arrays.asList("https://m.media-amazon.com/images/I/61monstera1.jpg", "https://m.media-amazon.com/images/I/61monstera2.jpg"), 10, true);
//
//        List<Product> list = List.of(p12,p13,p14,p15,p16,p17,p18,p19,p20);
//
//        WriteBatch batch = db.batch();
//
//        for (Product p : list) {
//            DocumentReference ref = db.collection("products").document();
//            batch.set(ref, p);
//        }
//
//        batch.commit();

        db.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(ds -> {
                    if (!ds.isEmpty()) {
                        List<Product> products = ds.toObjects(Product.class);

                        adapter = new ListingAdapter(products, product -> {

                            Bundle bundle = new Bundle();
                            bundle.putString("productId",product.getProductId());

                            ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                            productDetailsFragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, productDetailsFragment)
                                    .addToBackStack(null)
                                    .commit();
                        });

                        binding.recyclerViewListing.setAdapter(adapter);
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error:"+e.getMessage());
                    }
                });

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        if (getActivity() != null) {
            View searchInput = getActivity().findViewById(R.id.search_input);
            if (searchInput != null) {
                searchInput.setVisibility(View.GONE);
            }
        }



    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getActivity() != null) {
            View searchInput = getActivity().findViewById(R.id.search_input);
            if (searchInput != null) {
                searchInput.setVisibility(View.VISIBLE);
            }
        }
        binding = null;
    }
}