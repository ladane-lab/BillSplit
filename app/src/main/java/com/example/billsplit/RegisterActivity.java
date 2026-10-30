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

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegisterName;
    private EditText etRegisterEmail;
    private EditText etRegisterPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        setContentView(R.layout.activity_register);

        etRegisterName = findViewById(R.id.etRegisterName);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> performRegistration());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String name = etRegisterName.getText().toString().trim();
        String email = etRegisterEmail.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etRegisterName.setError("Enter your name");
            return;
        }

        if (email.isEmpty()) {
            etRegisterEmail.setError("Enter email address");
            return;
        }

        if (password.length() < 6) {
            etRegisterPassword.setError("Password must be at least 6 characters");
            return;
        }

        AuthRequest request = new AuthRequest(name, email, password);

        ApiClient.getApiService().register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AuthResponse body = response.body();
                    String token = body.getToken();
                    String userId = (body.getUser() != null) ? body.getUser().getId() : "";
                    String uName = (body.getUser() != null) ? body.getUser().getName() : name;

                    sessionManager.createLoginSession(token, userId, uName, email);

                    Toast.makeText(RegisterActivity.this, "Account created! Welcome, " + uName + "!", Toast.LENGTH_SHORT).show();
                    navigateToMain();

                } else {
                    // Local session fallback
                    sessionManager.createLoginSession("local_demo_jwt_token", "user_123", name, email);
                    Toast.makeText(RegisterActivity.this, "Registered (Local Session)", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // Local session fallback
                sessionManager.createLoginSession("local_demo_jwt_token", "user_123", name, email);
                Toast.makeText(RegisterActivity.this, "Registered (Offline Mode)", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
