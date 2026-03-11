package com.tamarin.nextstep;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etForgotEmail;
    private Button btnSendResetLink;
    private TextView tvBackToLoginFromForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etForgotEmail = findViewById(R.id.etForgotEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        tvBackToLoginFromForgot = findViewById(R.id.tvBackToLoginFromForgot);

        tvBackToLoginFromForgot.setOnClickListener(v -> finish());

        btnSendResetLink.setOnClickListener(v -> enviarLink());
    }

    private void enviarLink() {
        String email = etForgotEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor, informe seu e-mail.", Toast.LENGTH_SHORT).show();
            return;
        }

        RecoverRequest request = new RecoverRequest(email);

        RetrofitClient.getApi().recoverPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // O Supabase geralmente retorna sucesso (200) mesmo se o e-mail não existir por questões de segurança
                Toast.makeText(ForgotPasswordActivity.this, "Se houver uma conta, um link foi enviado.", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}