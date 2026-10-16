package lk.susa.plant_lk.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.susa.plant_lk.R;
import lk.susa.plant_lk.activity.MainActivity;
import lk.susa.plant_lk.activity.SignInActivity;
import lk.susa.plant_lk.util.LocaleHelper;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "plant_lk_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    private final String[] languageNames = {
            "English",
            "සිංහල",
            "தமிழ்",
            "Suomi",
            "Deutsch",
            "中文",
            "日本語"
    };

    private final String[] languageCodes = {
            "en", "si", "ta", "fi", "de", "zh", "ja"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        TextView languageValue = view.findViewById(R.id.settings_language_value);
        String currentCode = LocaleHelper.getSavedLanguage(requireContext());
        languageValue.setText(getLanguageNameFromCode(currentCode));

        view.findViewById(R.id.settings_edit_profile).setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(null)
                        .commit());

        view.findViewById(R.id.settings_my_orders).setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new OrdersFragment())
                        .addToBackStack(null)
                        .commit());

        view.findViewById(R.id.settings_wishlist).setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new WishlistFragment())
                        .addToBackStack(null)
                        .commit());

        com.google.android.material.materialswitch.MaterialSwitch notifSwitch =
                view.findViewById(R.id.settings_switch_notifications);

        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        notifSwitch.setChecked(notificationsEnabled);

        notifSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(getContext(),
                    isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        com.google.android.material.materialswitch.MaterialSwitch darkSwitch =
                view.findViewById(R.id.settings_switch_dark_mode);

        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        darkSwitch.setChecked(isDarkMode);

        darkSwitch.setOnCheckedChangeListener((btn, isChecked) -> {

            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();


            AppCompatDelegate.setDefaultNightMode(
                    isChecked
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });


        view.findViewById(R.id.settings_language).setOnClickListener(v ->
                new AlertDialog.Builder(getContext())
                        .setTitle("Select Language")
                        .setItems(languageNames, (dialog, which) -> {
                            LocaleHelper.setLocale(requireContext(), languageCodes[which]);
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .show());


        view.findViewById(R.id.settings_contact_support).setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ContactFragment())
                        .addToBackStack(null)
                        .commit());


        view.findViewById(R.id.settings_privacy_policy).setOnClickListener(v ->
                new AlertDialog.Builder(getContext())
                        .setTitle("Privacy Policy - Plant.LK")
                        .setMessage(
                                "Plant.LK Privacy Policy\n\n" +
                                        "Effective Date: March 26, 2026\n\n" +
                                        "1. Introduction\n" +
                                        "Plant.LK is Sri Lanka's premier online plant marketplace. We respect your privacy and are committed to protecting your personal information.\n\n" +
                                        "2. Information We Collect\n" +
                                        "- Personal details you provide when creating an account (name, email, phone number, delivery address).\n" +
                                        "- Payment information processed securely via our payment partners.\n" +
                                        "- Usage data such as pages viewed, searches, and app interactions.\n\n" +
                                        "3. How We Use Your Information\n" +
                                        "- To provide and improve our services.\n" +
                                        "- To process orders and deliveries.\n" +
                                        "- To communicate offers, updates, and support.\n\n" +
                                        "4. Sharing Information\n" +
                                        "- We do not sell your personal information.\n" +
                                        "- Information may be shared with delivery partners or service providers strictly for order fulfillment.\n\n" +
                                        "5. Data Security\n" +
                                        "- We implement industry-standard security measures to protect your data.\n\n" +
                                        "6. Your Rights\n" +
                                        "- You can access, update, or delete your personal information from your account.\n" +
                                        "- You can opt out of marketing communications at any time.\n\n" +
                                        "7. Children’s Privacy\n" +
                                        "- Plant.LK is not intended for children under 13. We do not knowingly collect data from children.\n\n" +
                                        "8. Changes to This Policy\n" +
                                        "- We may update this policy occasionally. Updates will be reflected within the app.\n\n" +
                                        "Contact us: support@plant.lk\n\n" +
                                        "© 2026 Plant.LK All rights reserved."
                        )
                        .setPositiveButton("OK", null)
                        .show());


        view.findViewById(R.id.settings_about).setOnClickListener(v ->
                new AlertDialog.Builder(getContext())
                        .setTitle("About PlantLK")
                        .setMessage("PlantLK v1.1.3\n\nSri Lanka's premier online plant marketplace.\n\n© 2026 Plant.LK All rights reserved.")
                        .setPositiveButton("OK", null)
                        .show());


        view.findViewById(R.id.settings_sign_out).setOnClickListener(v ->
                new AlertDialog.Builder(getContext())
                        .setTitle("Sign Out")
                        .setMessage("Are you sure you want to sign out?")
                        .setPositiveButton("Sign Out", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getActivity(), SignInActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show());

        view.findViewById(R.id.settings_delete_account).setOnClickListener(v ->
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Account")
                        .setMessage("This will permanently delete your account and all data. This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user == null) return;
                            String uid = user.getUid();
                            FirebaseFirestore.getInstance()
                                    .collection("users").document(uid)
                                    .delete()
                                    .addOnSuccessListener(unused ->
                                            user.delete().addOnSuccessListener(a -> {
                                                Toast.makeText(getContext(), "Account deleted",
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getActivity(), SignInActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }));
                        })
                        .setNegativeButton("Cancel", null)
                        .show());
    }

    private String getLanguageNameFromCode(String code) {
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(code)) return languageNames[i];
        }
        return "English";
    }
}