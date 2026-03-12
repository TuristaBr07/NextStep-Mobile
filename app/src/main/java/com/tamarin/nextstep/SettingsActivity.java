package com.tamarin.nextstep;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// IMPORTAÇÃO DO TEXT INPUT EDIT TEXT
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText etProfileName, etProfileCompany, etNewCatName;
    private Button btnSaveProfile, btnAddCat, btnLogout;
    private Spinner spinnerCatType;

    private RecyclerView rvCategoriesSettings;
    private CategorySettingsAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etProfileName = findViewById(R.id.etProfileName);
        etProfileCompany = findViewById(R.id.etProfileCompany);
        etNewCatName = findViewById(R.id.etNewCatName);
        spinnerCatType = findViewById(R.id.spinnerCatType);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnAddCat = findViewById(R.id.btnAddCat);
        btnLogout = findViewById(R.id.btnLogout);
        rvCategoriesSettings = findViewById(R.id.rvCategoriesSettings);

        rvCategoriesSettings.setLayoutManager(new LinearLayoutManager(this));

        // Botão Salvar Perfil
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        // Botão Adicionar Categoria
        btnAddCat.setOnClickListener(v -> addCategory());

        // Botão de Logout
        btnLogout.setOnClickListener(v -> {
            SessionManager.clear();
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        loadProfile();
        loadCategories();
    }

    private void loadProfile() {
        String userId = SessionManager.getUserId();
        if (userId == null) return;

        RetrofitClient.getApi().getProfile("eq." + userId).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Profile p = response.body().get(0);
                    if (p.getFullName() != null) etProfileName.setText(p.getFullName());
                    if (p.getCompanyName() != null) etProfileCompany.setText(p.getCompanyName());
                }
            }
            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {}
        });
    }

    private void saveProfile() {
        String userId = SessionManager.getUserId();

        // Verificação de segurança (evitar nulls)
        String name = etProfileName.getText() != null ? etProfileName.getText().toString().trim() : "";
        String company = etProfileCompany.getText() != null ? etProfileCompany.getText().toString().trim() : "";

        Profile profileUpdate = new Profile(name, company);

        RetrofitClient.getApi().updateProfile("eq." + userId, profileUpdate).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Erro ao atualizar perfil", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {}
        });
    }

    private void loadCategories() {
        RetrofitClient.getApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body();
                    adapter = new CategorySettingsAdapter(categoryList, cat -> deleteCategory(cat));
                    rvCategoriesSettings.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {}
        });
    }

    private void addCategory() {
        String name = etNewCatName.getText() != null ? etNewCatName.getText().toString().trim() : "";
        String type = spinnerCatType.getSelectedItem().toString();

        if (name.isEmpty()) return;

        Category newCat = new Category();
        newCat.setName(name);
        newCat.setType(type);
        newCat.setUserId(SessionManager.getUserId());

        RetrofitClient.getApi().createCategory(newCat).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {
                    etNewCatName.setText(""); // Limpa o campo
                    loadCategories(); // Recarrega a lista
                    Toast.makeText(SettingsActivity.this, "Categoria adicionada!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Erro ao criar: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {}
        });
    }

    private void deleteCategory(Category cat) {
        RetrofitClient.getApi().deleteCategory("eq." + cat.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadCategories(); // Recarrega a lista
                    Toast.makeText(SettingsActivity.this, "Categoria removida!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}