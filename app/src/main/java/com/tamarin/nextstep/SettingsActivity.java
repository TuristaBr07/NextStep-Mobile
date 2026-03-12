package com.tamarin.nextstep;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText etProfileName, etProfileCompany, etNewCatName;
    private Button btnSaveProfile, btnAddCat, btnLogout;
    private Spinner spinnerCatType;
    private ImageView ivAvatar; // Novo

    private RecyclerView rvCategoriesSettings;
    private CategorySettingsAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();

    // Variável para guardar a foto em formato de texto
    private String currentAvatarBase64 = null;

    // Lançador para abrir a galeria de fotos
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                        // Encolhe a imagem para não estourar o banco de dados
                        Bitmap resizedImage = resizeBitmap(selectedImage, 400);
                        ivAvatar.setImageBitmap(resizedImage);

                        // Converte para texto
                        currentAvatarBase64 = encodeImageToBase64(resizedImage);
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao carregar a imagem", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

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
        ivAvatar = findViewById(R.id.ivAvatar); // Ligação do Avatar

        rvCategoriesSettings.setLayoutManager(new LinearLayoutManager(this));

        // Clicar na imagem abre a galeria
        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnAddCat.setOnClickListener(v -> addCategory());

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

                    // Se o usuário tiver uma foto salva, decodifica e mostra
                    if (p.getAvatar() != null && !p.getAvatar().isEmpty()) {
                        currentAvatarBase64 = p.getAvatar();
                        Bitmap bitmap = decodeBase64ToBitmap(currentAvatarBase64);
                        if (bitmap != null) {
                            ivAvatar.setImageBitmap(bitmap);
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {}
        });
    }

    private void saveProfile() {
        String userId = SessionManager.getUserId();

        String name = etProfileName.getText() != null ? etProfileName.getText().toString().trim() : "";
        String company = etProfileCompany.getText() != null ? etProfileCompany.getText().toString().trim() : "";

        Profile profileUpdate = new Profile(name, company);
        profileUpdate.setAvatar(currentAvatarBase64); // Adiciona a foto no envio!

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

    // --- MÉTODOS AUXILIARES PARA A IMAGEM ---

    private Bitmap resizeBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Qualidade 70%
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private Bitmap decodeBase64ToBitmap(String b64) {
        try {
            byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    // --- CÓDIGO DE CATEGORIAS MANTIDO INTACTO ABAIXO ---

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
                    etNewCatName.setText("");
                    loadCategories();
                    Toast.makeText(SettingsActivity.this, "Categoria adicionada!", Toast.LENGTH_SHORT).show();
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
                    loadCategories();
                    Toast.makeText(SettingsActivity.this, "Categoria removida!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}