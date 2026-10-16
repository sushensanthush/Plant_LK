package lk.susa.plant_lk.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.adapter.CategoryAdapter;
import lk.susa.plant_lk.databinding.FragmentCategoryBinding;
import lk.susa.plant_lk.model.Category;


public class CategoryFragment extends BaseFragment {

    private FragmentCategoryBinding binding;

    private CategoryAdapter adapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerViewCategories.setLayoutManager(new GridLayoutManager(getContext(),3));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        showLoading();


//        Category c1 = new Category("cat1", "Indoor Plants", "");
//        Category c2 = new Category("cat2", "Outdoor Plants", "");
//        Category c3 = new Category("cat3", "Fertilizers", "");
//        Category c4 = new Category("cat4", "Seeds", "");
//        Category c5 = new Category("cat5", "Flowering Plants", "");
//        Category c6 = new Category("cat6", "Plant Pots & Planters", "");
//        Category c7 = new Category("cat7", "Gardening Tools", "");
//        Category c8 = new Category("cat8", "Succulents & Cacti", "");
//        Category c9 = new Category("cat9", "Herbs & Edibles", "");
//        Category c10 = new Category("cat10", "Plant Care Accessories", "");
//        Category c11 = new Category("cat11", "Gardening Books & Guides", "");
//        Category c12 = new Category("cat12", "Hydroponics & Indoor Growing Kits", "");
//
//        List<Category> cats = List.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10);
//
//        WriteBatch batch = db.batch();
//
//        for (Category category : cats) {
//
//            DocumentReference ref = db.collection("categories").document(category.getCategoryId());
//            batch.set(ref, category);
//        }
//        batch.commit();


        db.collection("categories").get()
                .addOnCompleteListener(task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        List<Category> categories = task.getResult().toObjects(Category.class);
                        adapter = new CategoryAdapter(categories, category -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("categoryId", category.getCategoryId());

                            ListingFragment fragment = new ListingFragment();
                            fragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });
                        binding.recyclerViewCategories.setAdapter(adapter);
                    } else {
                        android.util.Log.e("CategoryFragment", "Error: " + task.getException());
                    }
                });
    }

    public void filterCategories(String query) {
        if (adapter != null) {
            adapter.getFilter().filter(query);
        }
    }
}