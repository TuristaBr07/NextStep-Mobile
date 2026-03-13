package com.tamarin.nextstep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etRegEmail, etRegPassword, etConfirmPassword;
    private TextInputLayout tilFullName, tilRegEmail, tilRegPassword, tilConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tilFullName = findViewById(R.id.tilFullName);
        tilRegEmail = findViewById(R.id.tilRegEmail);
        tilRegPassword = findViewById(R.id.tilRegPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(v -> {
            if (!isLoading) {
                validateAndRegister();
            }
        });

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void validateAndRegister() {
        clearErrors();

        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etRegEmail.getText() != null ? etRegEmail.getText().toString().trim() : "";
        String password = etRegPassword.getText() != null ? etRegPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        boolean hasError = false;

        if (fullName.isEmpty()) {
            tilFullName.setError("Informe seu nome completo");
            hasError = true;
        }

        if (email.isEmpty()) {
            tilRegEmail.setError("Informe seu e-mail");
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilRegEmail.setError("E-mail inválido");
            hasError = true;
        }

        if (password.isEmpty()) {
            tilRegPassword.setError("Informe sua senha");
            hasError = true;
        } else if (password.length() < 8) {
            tilRegPassword.setError("A senha deve ter pelo menos 8 caracteres");
            hasError = true;
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Confirme sua senha");
            hasError = true;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("As senhas não coincidem");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        registerUser(fullName, email, password);
    }

    private void registerUser(String fullName, String email, String password) {
        setLoading(true);

        SignUpRequest request = new SignUpRequest(email, password, fullName);

        RetrofitClient.getApi().register(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setLoading(false);

                if (response.isSuccessful()) {
                    UiUtils.showLongToast(RegisterActivity.this, "Cadastro realizado com sucesso! Faça login para continuar.");
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                } else if (response.code() == 400 || response.code() == 422) {
                    UiUtils.showLongToast(RegisterActivity.this, "Não foi possível cadastrar. Verifique se o e-mail já está em uso.");
                } else {
                    UiUtils.showLongToast(RegisterActivity.this, "Erro ao cadastrar. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setLoading(false);
                UiUtils.showLongToast(RegisterActivity.this, "Falha na conexão. Verifique sua internet.");
            }
        });
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilRegEmail.setError(null);
        tilRegPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        etFullName.setEnabled(!loading);
        etRegEmail.setEnabled(!loading);
        etRegPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
        tvBackToLogin.setEnabled(!loading);
        btnRegister.setEnabled(!loading);

        btnRegister.setText(loading ? "Criando conta..." : "CADASTRAR");
    }
}