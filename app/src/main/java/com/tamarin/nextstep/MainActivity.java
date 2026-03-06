package com.tamarin.nextstep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

// --- CORREÇÃO: Inicializar o Gerente de Sessão AQUI ---
        SessionManager.init(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String senha = etPassword.getText().toString();

                if (email.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                fazerLogin(email, senha);
            }
        });
    }

    private void fazerLogin(String email, String senha) {
        LoginRequest loginRequest = new LoginRequest(email, senha);

        // ATENÇÃO: Verifique se na sua SupabaseApi o método chama "login" ou "loginUser".
        // No código que te passei anteriormente, chamamos de "login".
        RetrofitClient.getApi().login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // 1. Pegamos o Token
                    String token = response.body().getAccessToken();

                    // 2. Pegamos o ID do Usuário (NOVO!)
                    // Se der erro aqui, verifique se fez o Passo 1 e 2 (User e LoginResponse)
                    String userId = "";
                    if (response.body().getUser() != null) {
                        userId = response.body().getUser().getId();
                    }

                    // 3. Salvamos os dois na Sessão (Agora funciona!)
                    SessionManager.init(MainActivity.this); // Garante que iniciou
                    SessionManager.saveSession(token, userId);

                    Toast.makeText(MainActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                    // Vai para o Dashboard
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