package com.tamarin.nextstep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            // Validação simples (MVP)
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                // --- NAVEGAÇÃO (INTENT) ---
                // Cria a intenção de ir da MainActivity para a DashboardActivity
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);

                // Inicia a nova tela
                startActivity(intent);

                // Fecha a tela de login para o usuário não voltar nela ao clicar em "Voltar"
                finish();
            }
        });
    }
}