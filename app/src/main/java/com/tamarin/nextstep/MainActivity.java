package com.tamarin.nextstep;

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

        // Conecta o código Java aos componentes da tela
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        // Resolvido: btnLogin como variável local e uso de Lambda para o clique
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            } else {
                // Toast estilizado conforme a imagem de sucesso enviada
                Toast.makeText(MainActivity.this, "Bem-vindo ao NextStep!", Toast.LENGTH_LONG).show();
            }
        });
    }
}