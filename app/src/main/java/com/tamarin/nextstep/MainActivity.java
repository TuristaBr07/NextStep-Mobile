package com.tamarin.nextstep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SessionManager.init(this);

        if (SessionManager.hasSession()) {
            openDashboard();
            return;
        }

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RegisterActivity.class))
        );

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class))
        );

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        clearErrors();

        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String senha = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean hasError = false;

        if (email.isEmpty()) {
            tilEmail.setError("Informe seu e-mail");
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("E-mail inválido");
            hasError = true;
        }

        if (senha.isEmpty()) {
            tilPassword.setError("Informe sua senha");
            hasError = true;
        } else if (senha.length() < 6) {
            tilPassword.setError("A senha deve ter pelo menos 6 caracteres");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        fazerLogin(email, senha);
    }

    private void fazerLogin(String email, String senha) {
        setLoading(true);

        LoginRequest loginRequest = new LoginRequest(email, senha);

        RetrofitClient.getApi().login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getAccessToken();
                    String userId = "";

                    if (response.body().getUser() != null && response.body().getUser().getId() != null) {
                        userId = response.body().getUser().getId();
                    }

                    if (token == null || token.trim().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Falha ao obter token de sessão.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    SessionManager.saveSession(token, userId);

                    Toast.makeText(MainActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    openDashboard();

                } else if (response.code() == 400 || response.code() == 401) {
                    tilEmail.setError("Verifique suas credenciais");
                    tilPassword.setError("E-mail ou senha inválidos");
                } else {
                    Toast.makeText(
                            MainActivity.this,
                            "Erro ao fazer login. Código: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(
                        MainActivity.this,
                        "Falha na conexão. Verifique sua internet.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Entrando..." : getString(R.string.btn_login_text));

        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        tvForgotPassword.setEnabled(!loading);
        tvGoToRegister.setEnabled(!loading);
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private void openDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}