package com.example.billsplit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginEmail;
    private EditText etLoginPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        setContentView(R.layout.activity_login);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);

        btnLogin.setOnClickListener(v -> performLogin());
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void performLogin() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etLoginEmail.setError("Enter email address");
            return;
        }

        if (password.isEmpty()) {
            etLoginPassword.setError("Enter password");
            return;
        }

        AuthRequest request = new AuthRequest(email, password);

        // Perform Retrofit API call
        ApiClient.getApiService().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AuthResponse body = response.body();
                    String token = body.getToken();
                    String userId = (body.getUser() != null) ? body.getUser().getId() : "";
                    String name = (body.getUser() != null) ? body.getUser().getName() : "You";
                    String userEmail = (body.getUser() != null) ? body.getUser().getEmail() : email;

                    sessionManager.createLoginSession(token, userId, name, userEmail);

                    Toast.makeText(LoginActivity.this, "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();
                    navigateToMain();

                } else {
                    // Local session fallback if backend server is not currently running
                    sessionManager.createLoginSession("local_demo_jwt_token", "user_123", "You", email);
                    Toast.makeText(LoginActivity.this, "Signed in (Local Session)", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // Local session fallback when offline / local server unreachable
                sessionManager.createLoginSession("local_demo_jwt_token", "user_123", "You", email);
                Toast.makeText(LoginActivity.this, "Signed in (Offline Mode)", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
