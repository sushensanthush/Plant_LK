package lk.susa.plant_lk.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import lk.susa.plant_lk.R;

public class ContactFragment extends Fragment implements OnMapReadyCallback {

    private static final String PHONE_NUMBER = "+94771234567";
    private static final String EMAIL_ADDRESS = "support@plantlk.com";
    private static final String WHATSAPP_NUMBER = "94766363755";
    private static final LatLng HQ_LOCATION = new LatLng(51.508082, -0.14078);

    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        autoFillUserDetails(view);


        view.findViewById(R.id.contact_btn_phone).setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + PHONE_NUMBER)));
        });

        view.findViewById(R.id.contact_btn_email).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + EMAIL_ADDRESS));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Plant.LK Support");
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        view.findViewById(R.id.contact_btn_whatsapp).setOnClickListener(v -> {
            try {
                String url = "https://wa.me/" + WHATSAPP_NUMBER + "?text=Hi Plant.LK, I need help with...";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception e) {
                Toast.makeText(getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.contact_btn_send).setOnClickListener(v -> sendMessage(view));

        view.findViewById(R.id.clickable_address_card).setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:" + HQ_LOCATION.latitude + "," + HQ_LOCATION.longitude + "?q=PlantLK+Headquarters");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.addMarker(new MarkerOptions()
                .position(HQ_LOCATION)
                .title("Plant.LK Headquarters")
                .snippet("No. 45, Green Lane, London, United Kingdom"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HQ_LOCATION, 15f));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
    }

    private void autoFillUserDetails(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        TextInputEditText nameInput = view.findViewById(R.id.contact_input_name);
        TextInputEditText emailInput = view.findViewById(R.id.contact_input_email);

        if (user.getEmail() != null) {
            emailInput.setText(user.getEmail());
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getString("name") != null) {
                        nameInput.setText(doc.getString("name"));
                    }
                });
    }

    private void sendMessage(View view) {
        TextInputEditText nameInput = view.findViewById(R.id.contact_input_name);
        TextInputEditText emailInput = view.findViewById(R.id.contact_input_email);
        TextInputEditText subjectInput = view.findViewById(R.id.contact_input_subject);
        TextInputEditText messageInput = view.findViewById(R.id.contact_input_message);

        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String subject = subjectInput.getText() != null ? subjectInput.getText().toString().trim() : "";
        String message = messageInput.getText() != null ? messageInput.getText().toString().trim() : "";

        if (name.isEmpty()) {
            nameInput.setError("Please enter your name");
            nameInput.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            emailInput.setError("Please enter your email");
            emailInput.requestFocus();
            return;
        }
        if (subject.isEmpty()) {
            subjectInput.setError("Please enter a subject");
            subjectInput.requestFocus();
            return;
        }
        if (message.isEmpty()) {
            messageInput.setError("Please enter your message");
            messageInput.requestFocus();
            return;
        }

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("name", name);
        messageData.put("email", email);
        messageData.put("subject", subject);
        messageData.put("message", message);
        messageData.put("timestamp", com.google.firebase.Timestamp.now());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            messageData.put("userId", user.getUid());
        }

        view.findViewById(R.id.contact_btn_send).setEnabled(false);

        FirebaseFirestore.getInstance()
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(getContext(),
                            "Message sent! We'll get back to you soon",
                            Toast.LENGTH_LONG).show();

                    subjectInput.setText("");
                    messageInput.setText("");
                    view.findViewById(R.id.contact_btn_send).setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Failed to send message. Please try again.",
                            Toast.LENGTH_SHORT).show();
                    view.findViewById(R.id.contact_btn_send).setEnabled(true);
                });
    }
}
