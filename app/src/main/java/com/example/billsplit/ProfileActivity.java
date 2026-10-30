package com.example.billsplit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class ProfileActivity extends AppCompatActivity {

    private EditText etProfileName;
    private EditText etProfileEmail;
    private SharedPreferences preferences;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        preferences = getSharedPreferences("SplitMateData", MODE_PRIVATE);
        sessionManager = new SessionManager(this);

        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);

        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);
        Button btnLogout = findViewById(R.id.btnLogout);
        View btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> navigateToMain());

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToMain();
            }
        });
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnLogout.setOnClickListener(v -> logout());

        loadProfileData();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void loadProfileData() {
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        etProfileName.setText(name);
        etProfileEmail.setText(email);
    }

    private void saveProfile() {
        String name = etProfileName.getText().toString().trim();
        String email = etProfileEmail.getText().toString().trim();

        if (name.isEmpty()) {
            etProfileName.setError("Enter your name");
            return;
        }

        if (email.isEmpty()) {
            etProfileEmail.setError("Enter your email");
            return;
        }

        sessionManager.createLoginSession(
                sessionManager.getToken() != null ? sessionManager.getToken() : "demo_token",
                sessionManager.getUserId() != null ? sessionManager.getUserId() : "user_123",
                name,
                email
        );

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        sessionManager.logoutUser();
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
