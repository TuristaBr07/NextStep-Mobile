package com.tamarin.nextstep;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etForgotEmail;
    private Button btnSendResetLink;
    private TextView tvBackToLoginFromForgot;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etForgotEmail = findViewById(R.id.etForgotEmail);
        btnSendResetLink = findViewById(R.id.btnSendResetLink);
        tvBackToLoginFromForgot = findViewById(R.id.tvBackToLoginFromForgot);

        btnSendResetLink.setOnClickListener(v -> {
            if (!isLoading) {
                validateAndRecover();
            }
        });

        tvBackToLoginFromForgot.setOnClickListener(v -> finish());
    }

    private void validateAndRecover() {
        String email = etForgotEmail.getText() != null
                ? etForgotEmail.getText().toString().trim()
                : "";

        if (email.isEmpty()) {
            etForgotEmail.setError("Informe seu e-mail");
            etForgotEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etForgotEmail.setError("E-mail inválido");
            etForgotEmail.requestFocus();
            return;
        }

        etForgotEmail.setError(null);
        recoverPassword(email);
    }

    private void recoverPassword(String email) {
        setLoading(true);

        RecoverRequest request = new RecoverRequest(email);

        RetrofitClient.getApi().recoverPassword(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setLoading(false);

                if (response.isSuccessful()) {
                    UiUtils.showLongToast(
                            ForgotPasswordActivity.this,
                            "Se o e-mail existir, você receberá instruções para redefinir a senha."
                    );
                    finish();
                } else {
                    UiUtils.showLongToast(
                            ForgotPasswordActivity.this,
                            "Não foi possível processar a solicitação agora."
                    );
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setLoading(false);
                UiUtils.showLongToast(
                        ForgotPasswordActivity.this,
                        "Falha na conexão. Verifique sua internet."
                );
            }
        });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        etForgotEmail.setEnabled(!loading);
        btnSendResetLink.setEnabled(!loading);
        tvBackToLoginFromForgot.setEnabled(!loading);

        btnSendResetLink.setText(loading ? "ENVIANDO..." : "ENVIAR LINK");
    }
}