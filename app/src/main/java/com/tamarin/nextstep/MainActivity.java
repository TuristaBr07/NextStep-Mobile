package com.tamarin.nextstep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// NOVA IMPORTAÇÃO DO TEXT INPUT EDIT TEXT
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager.init(this);

        if (SessionManager.getToken() != null && !SessionManager.getToken().isEmpty()) {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String senha = etPassword.getText() != null ? etPassword.getText().toString() : "";

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(MainActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            fazerLogin(email, senha);
        });
    }

    private void fazerLogin(String email, String senha) {
        LoginRequest loginRequest = new LoginRequest(email, senha);

        RetrofitClient.getApi().login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String token = response.body().getAccessToken();
                    String userId = "";
                    if (response.body().getUser() != null) {
                        userId = response.body().getUser().getId();
                    }

                    SessionManager.init(MainActivity.this);
                    SessionManager.saveSession(token, userId);

                    Toast.makeText(MainActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Erro: Email ou senha inválidos", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}