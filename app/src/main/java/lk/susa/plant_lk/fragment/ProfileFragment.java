package lk.susa.plant_lk.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;
import lk.susa.plant_lk.R;
import lk.susa.plant_lk.activity.MainActivity;
import lk.susa.plant_lk.model.User;

public class ProfileFragment extends Fragment {

    private CircleImageView profileImage;
    private FloatingActionButton fabEditPhoto;
    private TextInputEditText inputName, inputEmail, inputPassword, inputRetypePassword;
    private Button btnUpdate;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private Uri selectedImageUri = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        fabEditPhoto = view.findViewById(R.id.fab_edit_photo);
        inputName = view.findViewById(R.id.input_name);
        inputEmail = view.findViewById(R.id.input_email);
        inputPassword = view.findViewById(R.id.input_password);
        inputRetypePassword = view.findViewById(R.id.input_retype_password);
        btnUpdate = view.findViewById(R.id.btn_update_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        loadUserData();

        profileImage.setOnClickListener(v -> pickImage());
        fabEditPhoto.setOnClickListener(v -> pickImage());

        btnUpdate.setOnClickListener(v -> updateProfile());

        return view;
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null
                        && result.getData().getData() != null) {

                    selectedImageUri = result.getData().getData();

                    Glide.with(this)
                            .load(selectedImageUri)
                            .placeholder(R.drawable.person_24)
                            .into(profileImage);
                }
            });

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadUserData() {
        if (currentUser == null) return;

        inputEmail.setText(currentUser.getEmail());

        firestore.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            inputName.setText(user.getName());

                            if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                Glide.with(this)
                                        .load(user.getProfilePicUrl())
                                        .placeholder(R.drawable.person_24)
                                        .into(profileImage);
                            }
                        }
                    }
                });
    }

    private void updateProfile() {
        String name = inputName.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String retype = inputRetypePassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            inputName.setError("Name is required");
            return;
        }

        if (!TextUtils.isEmpty(password)) {
            if (password.length() < 6) {
                inputPassword.setError("Password must be at least 6 characters");
                return;
            }
            if (!password.equals(retype)) {
                inputRetypePassword.setError("Passwords do not match");
                return;
            }
        }

        btnUpdate.setEnabled(false);
        btnUpdate.setText("Updating...");

        if (selectedImageUri != null) {
            uploadImageThenSave(name, password);
        } else {
            saveNameAndPassword(name, password, null);
        }
    }

    private void uploadImageThenSave(String name, String password) {
        String uid = currentUser.getUid();

        StorageReference imageRef = storage.getReference()
                .child("profile-images/" + uid + ".jpg");

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    String imageUrl = downloadUri.toString();
                                    saveNameAndPassword(name, password, imageUrl);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(),
                                            "Failed to get image URL", Toast.LENGTH_SHORT).show();
                                    resetButton();
                                })
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetButton();
                });
    }

    private void saveNameAndPassword(String name, String password, @Nullable String imageUrl) {
        String uid = currentUser.getUid();

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("name", name);
        if (imageUrl != null) {
            updates.put("profilePicUrl", imageUrl);

        }

        firestore.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    selectedImageUri = null;

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refreshNavHeader();
                    }

                    if (!TextUtils.isEmpty(password)) {
                        currentUser.updatePassword(password)
                                .addOnSuccessListener(a ->
                                        Toast.makeText(getContext(),
                                                "Password updated!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Password update failed: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show());
                    }

                    inputPassword.setText("");
                    inputRetypePassword.setText("");
                    resetButton();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetButton();
                });
    }

    private void resetButton() {
        if (btnUpdate != null) {
            btnUpdate.setEnabled(true);
            btnUpdate.setText("Update Profile");
        }
    }
}