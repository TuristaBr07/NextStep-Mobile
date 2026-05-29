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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
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
    private MaterialButtonToggleGroup toggleGroupCatType;
    private ImageView ivAvatar;
    private TextView tvCategoryCount;

    private RecyclerView rvCategoriesSettings;
    private CategorySettingsAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();

    private String currentAvatarBase64 = null;
    private boolean isSavingProfile = false;
    private boolean isAddingCategory = false;

    private final java.util.List<retrofit2.Call<?>> pendingCalls = new java.util.ArrayList<>();

    private <T> retrofit2.Call<T> track(retrofit2.Call<T> call) {
        pendingCalls.add(call);
        return call;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (retrofit2.Call<?> c : pendingCalls) {
            c.cancel();
        }
        pendingCalls.clear();
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                        if (selectedImage == null) {
                            Toast.makeText(this, getString(R.string.error_image_load), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Bitmap resizedImage = resizeBitmap(selectedImage, 400);
                        ivAvatar.setImageBitmap(resizedImage);
                        ivAvatar.setImageTintList(null);
                        ivAvatar.setPadding(0, 0, 0, 0);
                        currentAvatarBase64 = encodeImageToBase64(resizedImage);

                        Toast.makeText(this, getString(R.string.photo_ready_to_save), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.error_image_load), Toast.LENGTH_SHORT).show();
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

        toggleGroupCatType = findViewById(R.id.toggleGroupCatType);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnAddCat = findViewById(R.id.btnAddCat);
        btnLogout = findViewById(R.id.btnLogout);
        rvCategoriesSettings = findViewById(R.id.rvCategoriesSettings);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvCategoryCount = findViewById(R.id.tvCategoryCount);

        toggleGroupCatType.check(R.id.btnCatIncome);

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

        setupGoal();
        loadProfile();
        loadCategories();
    }

    private void setupGoal() {
        com.google.android.material.textfield.TextInputEditText etGoal = findViewById(R.id.etGoal);
        com.google.android.material.button.MaterialButton btnSaveGoal = findViewById(R.id.btnSaveGoal);
        if (etGoal == null || btnSaveGoal == null) return;

        float savedGoal = getSharedPreferences(OnboardingActivity.PREFS_NAME, MODE_PRIVATE)
                .getFloat("monthly_goal", 0f);
        if (savedGoal > 0) {
            etGoal.setText(String.format(java.util.Locale.getDefault(), "%.2f", savedGoal));
        }

        btnSaveGoal.setOnClickListener(v -> {
            String raw = etGoal.getText() != null ? etGoal.getText().toString().trim() : "";
            if (raw.isEmpty()) {
                getSharedPreferences(OnboardingActivity.PREFS_NAME, MODE_PRIVATE)
                        .edit().remove("monthly_goal").apply();
                Toast.makeText(this, getString(R.string.settings_goal_cleared), Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                float goal = Float.parseFloat(raw.replace(",", "."));
                getSharedPreferences(OnboardingActivity.PREFS_NAME, MODE_PRIVATE)
                        .edit().putFloat("monthly_goal", goal).apply();
                Toast.makeText(this, getString(R.string.settings_goal_saved), Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, getString(R.string.settings_goal_invalid), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfile() {
        String userId = SessionManager.getUserId();
        if (userId == null || userId.trim().isEmpty()) return;

        track(RetrofitClient.getApi().getProfile(userId)).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (isFinishing() || isDestroyed()) return;
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
                            ivAvatar.setImageTintList(null);
                            ivAvatar.setPadding(0, 0, 0, 0);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                Toast.makeText(SettingsActivity.this, getString(R.string.error_load_profile), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndSaveProfile() {
        clearProfileErrors();

        String name = etProfileName.getText() != null ? etProfileName.getText().toString().trim() : "";
        String company = etProfileCompany.getText() != null ? etProfileCompany.getText().toString().trim() : "";

        boolean hasError = false;

        if (name.isEmpty()) {
            tilProfileName.setError(getString(R.string.error_name_required));
            hasError = true;
        }

        if (company.length() > 60) {
            tilProfileCompany.setError(getString(R.string.error_company_max_length));
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
            Toast.makeText(this, getString(R.string.error_session_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        setProfileLoading(true);

        Profile profileUpdate = new Profile(name, company);
        profileUpdate.setAvatar(currentAvatarBase64);

        track(RetrofitClient.getApi().updateProfile(userId, profileUpdate)).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (isFinishing() || isDestroyed()) return;
                setProfileLoading(false);

                if (response.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, getString(R.string.profile_updated_success), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(SettingsActivity.this, getString(R.string.error_session_expired), Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this, getString(R.string.error_update_profile), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                setProfileLoading(false);
                Toast.makeText(SettingsActivity.this, getString(R.string.error_connection_save_profile), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        track(RetrofitClient.getApi().getCategories()).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body();
                    adapter = new CategorySettingsAdapter(categoryList, cat -> confirmDeleteCategory(cat));
                    rvCategoriesSettings.setAdapter(adapter);
                    updateCategoryCount();
                } else {
                    Toast.makeText(SettingsActivity.this, getString(R.string.error_loading_categories), Toast.LENGTH_SHORT).show();
                    categoryList.clear();
                    updateCategoryCount();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                Toast.makeText(SettingsActivity.this, getString(R.string.error_connection_loading_categories), Toast.LENGTH_SHORT).show();
                categoryList.clear();
                updateCategoryCount();
            }
        });
    }

    private void validateAndAddCategory() {
        clearCategoryErrors();

        String name = etNewCatName.getText() != null ? etNewCatName.getText().toString().trim() : "";
        String type = toggleGroupCatType.getCheckedButtonId() == R.id.btnCatExpense
                ? getString(R.string.transaction_type_expense)
                : getString(R.string.transaction_type_income);

        if (name.isEmpty()) {
            tilNewCatName.setError(getString(R.string.error_category_name_required));
            return;
        }

        if (name.length() < 2) {
            tilNewCatName.setError(getString(R.string.error_category_name_min));
            return;
        }

        for (Category category : categoryList) {
            if (category.getName() != null
                    && category.getType() != null
                    && category.getName().trim().equalsIgnoreCase(name)
                    && category.getType().trim().equalsIgnoreCase(type)) {
                tilNewCatName.setError(getString(R.string.error_category_duplicate));
                return;
            }
        }

        addCategory(name, type);
    }

    private void addCategory(String name, String type) {
        String userId = SessionManager.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.error_session_invalid), Toast.LENGTH_SHORT).show();
            return;
        }

        setCategoryLoading(true);

        Category newCat = new Category();
        newCat.setName(name);
        newCat.setType(type);
        newCat.setUserId(userId);

        track(RetrofitClient.getApi().createCategory(newCat)).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (isFinishing() || isDestroyed()) return;
                setCategoryLoading(false);

                if (response.isSuccessful()) {
                    etNewCatName.setText("");
                    loadCategories();
                    Toast.makeText(SettingsActivity.this, getString(R.string.category_added_success), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(SettingsActivity.this, getString(R.string.error_session_expired), Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this, getString(R.string.error_add_category), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                setCategoryLoading(false);
                Toast.makeText(SettingsActivity.this, getString(R.string.error_connection_add_category), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteCategory(Category cat) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_remove_category_title))
                .setMessage(getString(R.string.dialog_remove_category_message, cat.getName()))
                .setNegativeButton(getString(R.string.action_cancel), null)
                .setPositiveButton(getString(R.string.action_remove), (dialog, which) -> deleteCategory(cat))
                .show();
    }

    private void deleteCategory(Category cat) {
        track(RetrofitClient.getApi().deleteCategory(Long.valueOf(String.valueOf(cat.getId())))).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    loadCategories();
                    Toast.makeText(SettingsActivity.this, getString(R.string.category_removed_success), Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(SettingsActivity.this, getString(R.string.error_session_expired), Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this, getString(R.string.error_remove_category), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                Toast.makeText(SettingsActivity.this, getString(R.string.error_connection_remove_category), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_logout_title))
                .setMessage(getString(R.string.dialog_logout_message))
                .setNegativeButton(getString(R.string.action_cancel), null)
                .setPositiveButton(getString(R.string.settings_logout), (dialog, which) -> {
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
        btnSaveProfile.setText(loading ? getString(R.string.profile_saving) : getString(R.string.settings_save_profile));
        etProfileName.setEnabled(!loading);
        etProfileCompany.setEnabled(!loading);
        ivAvatar.setEnabled(!loading);
    }

    private void setCategoryLoading(boolean loading) {
        isAddingCategory = loading;
        btnAddCat.setEnabled(!loading);
        btnAddCat.setText(loading ? getString(R.string.category_adding) : getString(R.string.settings_add_category));
        etNewCatName.setEnabled(!loading);
        for (int i = 0; i < toggleGroupCatType.getChildCount(); i++) {
            toggleGroupCatType.getChildAt(i).setEnabled(!loading);
        }
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
            tvCategoryCount.setText(getString(R.string.categories_empty));
        } else if (count == 1) {
            tvCategoryCount.setText(getString(R.string.categories_count_single));
        } else {
            tvCategoryCount.setText(getString(R.string.categories_count_plural, count));
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