package lk.susa.plant_lk.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import lk.susa.plant_lk.databinding.ActivitySignInBinding;
import lk.susa.plant_lk.util.NotificationHelper;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        binding.signinBtnSignin.setOnClickListener(view -> {
            String email = binding.signinInputEmail.getText().toString().trim();
            String password = binding.signinInputPassword.getText().toString().trim();

            if (email.isEmpty()) {
                binding.signinInputEmail.setError("Email is required");
                binding.signinInputEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.signinInputEmail.setError("Please enter a valid email");
                binding.signinInputEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.signinInputPassword.setError("Password is required");
                binding.signinInputPassword.requestFocus();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                            updateUI(firebaseAuth.getCurrentUser());
                        } else {
                            Toast.makeText(SignInActivity.this,
                                    "Authentication failed. Check your email and password.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        binding.signinForgotPassword.setOnClickListener(v -> {

            String email = binding.signinInputEmail.getText().toString().trim();

            if (email.isEmpty()) {
                binding.signinInputEmail.setError("Enter your email first");
                binding.signinInputEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.signinInputEmail.setError("Enter valid email");
                binding.signinInputEmail.requestFocus();
                return;
            }

            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this,
                                    "Password reset email sent!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SignInActivity.this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });


        binding.signinBtnSignup.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });
    }

    @SuppressLint("MissingPermission")
    private void updateUI(FirebaseUser user) {
        if (user == null) return;

        // Update FCM
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getUid())
                                .update("fcmToken", token)
                                .addOnFailureListener(e -> e.printStackTrace());
                    }
                });

        String username = user.getEmail();
        NotificationHelper.showLoginNotification(this, username);

        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();
    }
}