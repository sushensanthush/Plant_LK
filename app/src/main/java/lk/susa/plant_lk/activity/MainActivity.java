package lk.susa.plant_lk.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.databinding.ActivityMainBinding;
import lk.susa.plant_lk.databinding.SideNavHeaderBinding;
import lk.susa.plant_lk.fragment.CareFragment;
import lk.susa.plant_lk.fragment.CartFragment;
import lk.susa.plant_lk.fragment.CategoryFragment;
import lk.susa.plant_lk.fragment.ContactFragment;
import lk.susa.plant_lk.fragment.HomeFragment;
import lk.susa.plant_lk.fragment.NurseryMapFragment;
import lk.susa.plant_lk.fragment.OrdersFragment;
import lk.susa.plant_lk.fragment.ProfileFragment;
import lk.susa.plant_lk.fragment.SettingsFragment;
import lk.susa.plant_lk.fragment.WishlistFragment;
import lk.susa.plant_lk.model.User;
import lk.susa.plant_lk.util.NotificationHelper;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {

    private ActivityMainBinding binding;
    private SideNavHeaderBinding sideNavHeaderBinding;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private EditText searchInput;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private TextView networkStatusBar;
    private lk.susa.plant_lk.util.NetworkMonitor networkMonitor;
    private final lk.susa.plant_lk.util.SystemStateReceiver systemStateReceiver = new lk.susa.plant_lk.util.SystemStateReceiver();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        networkStatusBar = findViewById(R.id.networkStatusBar);
        networkMonitor = new lk.susa.plant_lk.util.NetworkMonitor();

        networkMonitor.startMonitoring(this, new lk.susa.plant_lk.util.NetworkMonitor.NetworkListener() {
            @Override
            public void onAvailable() {
                runOnUiThread(() -> {
                    networkStatusBar.setVisibility(View.VISIBLE);
                    networkStatusBar.setText("Back Online");
                    networkStatusBar.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"));

                    new android.os.Handler().postDelayed(() -> {
                        networkStatusBar.setVisibility(View.GONE);
                    }, 3000);
                });
            }

            @Override
            public void onLost() {
                runOnUiThread(() -> {
                    networkStatusBar.setVisibility(View.VISIBLE);
                    networkStatusBar.setText("No Internet Connection");
                    networkStatusBar.setBackgroundColor(android.graphics.Color.parseColor("#FF0000"));
                });
            }
        });


//        lk.susa.plant_lk.database.DatabaseHelper dbHelper =
//                new lk.susa.plant_lk.database.DatabaseHelper(this);
//
//        if (dbHelper.getTipByTitle("Rose") == null) {
//
//            dbHelper.insertTip(
//                    "Rose",
//                    "Apply compost and prune regularly.",
//                    "Aphids, spider mites.",
//                    "Too much watering causes root rot."
//            );
//        }


        androidx.core.content.ContextCompat.registerReceiver(this, systemStateReceiver,
                lk.susa.plant_lk.util.SystemStateReceiver.buildFilter(),
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);

        View headerView = binding.sideNavigationView.getHeaderView(0);
        sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.side_navigation_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        searchInput = findViewById(R.id.search_input);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Fragment currentFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);

                if (currentFragment instanceof CategoryFragment) {
                    ((CategoryFragment) currentFragment).filterCategories(s.toString());

                } else if (s.length() > 0) {
                    CategoryFragment categoryFragment = new CategoryFragment();
                    loadFragment(categoryFragment);
                    bottomNavigationView.getMenu().findItem(R.id.bottom_nav_category).setChecked(true);

                    searchInput.postDelayed(() -> {
                        Fragment frag = getSupportFragmentManager()
                                .findFragmentById(R.id.fragment_container);
                        if (frag instanceof CategoryFragment) {
                            ((CategoryFragment) frag).filterCategories(s.toString());
                        }
                    }, 600);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();


                    getSupportFragmentManager().executePendingTransactions();
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (currentFragment instanceof HomeFragment) {
                        toolbar.setVisibility(View.VISIBLE);
                    }
                } else {
                    finish();
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            navigationView.getMenu().findItem(R.id.side_nav_home).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(ds -> {
                        if (ds.exists()) {
                            User user = ds.toObject(User.class);

                            sideNavHeaderBinding.headerUserName.setText(user.getName());
                            sideNavHeaderBinding.headerUserEmail.setText(user.getEmail());
                            sideNavHeaderBinding.headerStatus.setText("Member");

                            String profileUrl = user.getProfilePicUrl();
                            if (profileUrl != null && !profileUrl.isEmpty()) {
                                Glide.with(MainActivity.this)
                                        .load(profileUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.person_24)
                                        .into(sideNavHeaderBinding.headerProfilePic);
                            } else {
                                sideNavHeaderBinding.headerProfilePic.setImageResource(R.drawable.person_24);
                            }

                        } else {
                            sideNavHeaderBinding.headerUserName.setText("You're not logged in");
                            sideNavHeaderBinding.headerUserEmail.setText("Guest user");
                            sideNavHeaderBinding.headerStatus.setText("Guest");
                            Log.e("Firestore", "User object is null");
                        }

                    }).addOnFailureListener(e -> Log.e("Firestore", "Error:" + e.getMessage()));

            navigationView.getMenu().findItem(R.id.side_nav_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.side_nav_profile).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_orders).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_nurseries).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_settings).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_care).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_wishlist).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_cart).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_contact_support).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_logout).setVisible(true);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 101);
        }


    }


    @SuppressLint("MissingPermission")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        Menu navMenu = navigationView.getMenu();
        Menu bottomMenu = bottomNavigationView.getMenu();

        for (int i = 0; i < navMenu.size(); i++) {
            navMenu.getItem(i).setChecked(false);
        }
        for (int i = 0; i < bottomMenu.size(); i++) {
            bottomMenu.getItem(i).setChecked(false);
        }
        searchInput.setText("");

        if (itemId == R.id.side_nav_home || itemId == R.id.bottom_nav_home) {
            loadFragment(new HomeFragment());
            navigationView.getMenu().findItem(R.id.side_nav_home).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);

        } else if (itemId == R.id.side_nav_profile || itemId == R.id.bottom_nav_profile) {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
            loadFragment(new ProfileFragment());
            navigationView.setCheckedItem(R.id.side_nav_profile);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_profile).setChecked(true);

        } else if (itemId == R.id.side_nav_orders) {
            loadFragment(new OrdersFragment());
            navigationView.getMenu().findItem(R.id.side_nav_orders).setChecked(true);

        } else if (itemId == R.id.side_nav_wishlist) {
            loadFragment(new WishlistFragment());
            navigationView.getMenu().findItem(R.id.side_nav_wishlist).setChecked(true);

        } else if (itemId == R.id.side_nav_cart || itemId == R.id.bottom_nav_cart) {
            loadFragment(new CartFragment());
            navigationView.setCheckedItem(R.id.side_nav_cart);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_cart).setChecked(true);

        } else if (itemId == R.id.side_nav_care) {
            loadFragment(new CareFragment());
            navigationView.getMenu().findItem(R.id.side_nav_care).setChecked(true);

        } else if (itemId == R.id.side_nav_contact_support) {
            loadFragment(new ContactFragment());
            navigationView.getMenu().findItem(R.id.side_nav_contact_support).setChecked(true);

        } else if (itemId == R.id.side_nav_nurseries || itemId == R.id.bottom_nav_nurseries) {
            loadFragment(new NurseryMapFragment());
            navigationView.setCheckedItem(R.id.side_nav_nurseries);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_nurseries).setChecked(true);

        } else if (itemId == R.id.side_nav_settings) {
            loadFragment(new SettingsFragment());
            navigationView.getMenu().findItem(R.id.side_nav_settings).setChecked(true);

        } else if (itemId == R.id.bottom_nav_category) {
            loadFragment(new CategoryFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_category).setChecked(true);

        } else if (itemId == R.id.side_nav_login) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);

        } else if (itemId == R.id.side_nav_logout) {

            FirebaseUser user = firebaseAuth.getCurrentUser();
            String username = (user != null) ? user.getEmail() : "User";


            NotificationHelper.showLogoutNotification(this, username);

            firebaseAuth.signOut();


            navigationView.removeHeaderView(sideNavHeaderBinding.getRoot());
            View headerView = navigationView.inflateHeaderView(R.layout.side_nav_header);
            sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

            sideNavHeaderBinding.headerUserName.setText("You're not logged in");
            sideNavHeaderBinding.headerUserEmail.setText("Guest user");
            sideNavHeaderBinding.headerStatus.setText("Guest");
            sideNavHeaderBinding.headerProfilePic.setImageResource(R.drawable.person_24);


            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.side_nav_menu);


            loadFragment(new HomeFragment());
        }


        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    public void refreshNavHeader() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        firebaseFirestore.collection("users")
                .document(currentUser.getUid())
                .addSnapshotListener((ds, error) -> {
                    if (ds != null && ds.exists()) {
                        User user = ds.toObject(User.class);

                        sideNavHeaderBinding.headerUserName.setText(user.getName());
                        sideNavHeaderBinding.headerUserEmail.setText(user.getEmail());

                        String profileUrl = user.getProfilePicUrl();
                        if (profileUrl != null && !profileUrl.isEmpty()) {
                            Glide.with(MainActivity.this)
                                    .load(profileUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.person_24)
                                    .into(sideNavHeaderBinding.headerProfilePic);
                        }
                    }
                });
    }


    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        if (fragment instanceof HomeFragment || fragment instanceof CategoryFragment || fragment instanceof CartFragment || fragment instanceof WishlistFragment) {
            toolbar.setVisibility(View.VISIBLE);
        } else {
            toolbar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(systemStateReceiver);
        } catch (IllegalArgumentException ignored) {
        }
        if (networkMonitor != null) {
            networkMonitor.stopMonitoring();
        }
    }
}