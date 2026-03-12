package com.tamarin.nextstep;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// NOVA IMPORTAÇÃO DO TEXT INPUT EDIT TEXT
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etRegEmail, etRegPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        tvBackToLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> fazerRegistro());
    }

    private void fazerRegistro() {
        String nome = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etRegEmail.getText() != null ? etRegEmail.getText().toString().trim() : "";
        String senha = etRegPassword.getText() != null ? etRegPassword.getText().toString() : "";
        String confSenha = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confSenha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confSenha)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        SignUpRequest request = new SignUpRequest(email, senha, nome);

        RetrofitClient.getApi().register(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Cadastro realizado! Verifique seu e-mail.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Erro ao cadastrar. Verifique os dados.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}