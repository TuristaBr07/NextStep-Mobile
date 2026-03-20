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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText etProfileName, etProfileCompany, etNewCatName;
    private TextInputLayout tilProfileName, tilProfileCompany, tilNewCatName;
    private Button btnSaveProfile, btnAddCat, btnLogout;
    private Spinner spinnerCatType;
    private ImageView ivAvatar;
    private TextView tvCategoryCount;

    private RecyclerView rvCategoriesSettings;
    private CategorySettingsAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();

    private String currentAvatarBase64 = null;
    private boolean isSavingProfile = false;
    private boolean isAddingCategory = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                        if (selectedImage == null) {
                            Toast.makeText(this, "Não foi possível carregar a imagem.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Bitmap resizedImage = resizeBitmap(selectedImage, 400);
                        ivAvatar.setImageBitmap(resizedImage);
                        currentAvatarBase64 = encodeImageToBase64(resizedImage);

                        Toast.makeText(this, "Foto pronta para salvar no perfil.", Toast.LENGTH_SHORT).show();
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

        tilProfileName = findViewById(R.id.tilProfileName);
        tilProfileCompany = findViewById(R.id.tilProfileCompany);
        tilNewCatName = findViewById(R.id.tilNewCatName);

        spinnerCatType = findViewById(R.id.spinnerCatType);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnAddCat = findViewById(R.id.btnAddCat);
        btnLogout = findViewById(R.id.btnLogout);
        rvCategoriesSettings = findViewById(R.id.rvCategoriesSettings);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvCategoryCount = findViewById(R.id.tvCategoryCount);

        rvCategoriesSettings.setLayoutManager(new LinearLayoutManager(this));
        rvCategoriesSettings.setHasFixedSize(true);

        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSaveProfile.setOnClickListener(v -> {
            if (!isSavingProfile) {
                validateAndSaveProfile();
            }
        });

        btnAddCat.setOnClickListener(v -> {
            if (!isAddingCategory) {
                validateAndAddCategory();
            }
        });

        btnLogout.setOnClickListener(v -> confirmLogout());

        loadProfile();
        loadCategories();
    }

    private void loadProfile() {
        String userId = SessionManager.getUserId();
        if (userId == null || userId.trim().isEmpty()) return;

        // Reativado e removido o "eq."
        RetrofitClient.getApi().getProfile(userId).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Profile p = response.body().get(0);

                    if (p.getFullName() != null) {
                        etProfileName.setText(p.getFullName());
                    }

                    if (p.getCompanyName() != null) {
                        etProfileCompany.setText(p.getCompanyName());
                    }

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
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "Erro ao carregar perfil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndSaveProfile() {
        clearProfileErrors();

        String name = etProfileName.getText() != null ? etProfileName.getText().toString().trim() : "";
        String company = etProfileCompany.getText() != null ? etProfileCompany.getText().toString().trim() : "";

        boolean hasError = false;

        if (name.isEmpty()) {
            tilProfileName.setError("Informe seu nome");
            hasError = true;
        }

        if (company.length() > 60) {
            tilProfileCompany.setError("Use no máximo 60 caracteres");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        saveProfile(name, company);
    }

    private void saveProfile(String name, String company) {
        String userId = SessionManager.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(this, "Sessão inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        setProfileLoading(true);

        Profile profileUpdate = new Profile(name, company);
        profileUpdate.setAvatar(currentAvatarBase64);

        // Reativado e removido o "eq."
        RetrofitClient.getApi().updateProfile(userId, profileUpdate).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                setProfileLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(SettingsActivity.this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this, "Erro ao atualizar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                setProfileLoading(false);
                Toast.makeText(SettingsActivity.this, "Falha de conexão ao salvar perfil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        RetrofitClient.getApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body();
                    adapter = new CategorySettingsAdapter(categoryList, cat -> confirmDeleteCategory(cat));
                    rvCategoriesSettings.setAdapter(adapter);
                    updateCategoryCount();
                } else {
                    Toast.makeText(SettingsActivity.this, "Erro ao carregar categorias.", Toast.LENGTH_SHORT).show();
                    categoryList.clear();
                    updateCategoryCount();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "Falha ao carregar categorias.", Toast.LENGTH_SHORT).show();
                categoryList.clear();
                updateCategoryCount();
            }
        });
    }

    private void validateAndAddCategory() {
        clearCategoryErrors();

        String name = etNewCatName.getText() != null ? etNewCatName.getText().toString().trim() : "";
        String type = spinnerCatType.getSelectedItem() != null ? spinnerCatType.getSelectedItem().toString() : "";

        if (name.isEmpty()) {
            tilNewCatName.setError("Informe o nome da categoria");
            return;
        }

        if (name.length() < 2) {
            tilNewCatName.setError("Use pelo menos 2 caracteres");
            return;
        }

        for (Category category : categoryList) {
            if (category.getName() != null
                    && category.getType() != null
                    && category.getName().trim().equalsIgnoreCase(name)
                    && category.getType().trim().equalsIgnoreCase(type)) {
                tilNewCatName.setError("Essa categoria já existe para esse tipo");
                return;
            }
        }

        addCategory(name, type);
    }

    private void addCategory(String name, String type) {
        String userId = SessionManager.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(this, "Sessão inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        setCategoryLoading(true);

        Category newCat = new Category();
        newCat.setName(name);
        newCat.setType(type);
        newCat.setUserId(userId);

        RetrofitClient.getApi().createCategory(newCat).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                setCategoryLoading(false);

                if (response.isSuccessful()) {
                    etNewCatName.setText("");
                    loadCategories();
                    Toast.makeText(SettingsActivity.this, "Categoria adicionada!", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(SettingsActivity.this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this, "Erro ao adicionar categoria.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                setCategoryLoading(false);
                Toast.makeText(SettingsActivity.this, "Falha de conexão ao adicionar categoria.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteCategory(Category cat) {
        new AlertDialog.Builder(this)
                .setTitle("Remover categoria")
                .setMessage("Deseja remover a categoria \"" + cat.getName() + "\"?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Remover", (dialog, which) -> deleteCategory(cat))
                .show();
    }

    private void deleteCategory(Category cat) {
        RetrofitClient.getApi().deleteCategory(Long.valueOf(String.valueOf(cat.getId()))).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadCategories();
                    Toast.makeText(SettingsActivity.this, "Categoria removida!", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(SettingsActivity.this, "Sessão expirada.", Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this, "Erro ao remover categoria.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "Falha de conexão ao remover categoria.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Sair da conta")
                .setMessage("Deseja realmente encerrar sua sessão?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sair", (dialog, which) -> {
                    SessionManager.clear();
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void setProfileLoading(boolean loading) {
        isSavingProfile = loading;
        btnSaveProfile.setEnabled(!loading);
        btnSaveProfile.setText(loading ? "Salvando..." : "Salvar perfil");

        etProfileName.setEnabled(!loading);
        etProfileCompany.setEnabled(!loading);
        ivAvatar.setEnabled(!loading);
    }

    private void setCategoryLoading(boolean loading) {
        isAddingCategory = loading;
        btnAddCat.setEnabled(!loading);
        btnAddCat.setText(loading ? "Adicionando..." : "Adicionar categoria");

        etNewCatName.setEnabled(!loading);
        spinnerCatType.setEnabled(!loading);
    }

    private void clearProfileErrors() {
        tilProfileName.setError(null);
        tilProfileCompany.setError(null);
    }

    private void clearCategoryErrors() {
        tilNewCatName.setError(null);
    }

    private void updateCategoryCount() {
        if (tvCategoryCount == null) return;

        int count = categoryList != null ? categoryList.size() : 0;
        if (count == 0) {
            tvCategoryCount.setText("Nenhuma categoria cadastrada");
        } else if (count == 1) {
            tvCategoryCount.setText("1 categoria cadastrada");
        } else {
            tvCategoryCount.setText(count + " categorias cadastradas");
        }
    }

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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
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
}