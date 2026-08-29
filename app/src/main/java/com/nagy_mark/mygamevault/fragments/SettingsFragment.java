package com.nagy_mark.mygamevault.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nagy_mark.mygamevault.BuildConfig;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.database.AppDatabase;
import com.nagy_mark.mygamevault.models.ProfileModel;
import com.nagy_mark.mygamevault.network.SupabaseApi;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {

    private ShapeableImageView ivProfilePictureSettings;
    private TextInputEditText etUsernameSettings;
    private Button btnLogoutSettings, btnSaveProfileSettings, btnStatisticsSettings;
    private AutoCompleteTextView actvLanguageSettings;
    private TextInputLayout tilUsernameSettings;
    private SwitchMaterial swDarkModeSettings;

    private Uri selectedImageUri = null;
    private String currentUserId = null;
    private String currentAvatarUrl = null;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && isAdded()) {
                    selectedImageUri = uri;

                    Glide.with(requireContext())
                            .load(uri)
                            .circleCrop()
                            .into(ivProfilePictureSettings);
                }
            }
    );

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogoutSettings = view.findViewById(R.id.btnLogoutSettings);
        btnSaveProfileSettings = view.findViewById(R.id.btnSaveProfileSettings);
        btnStatisticsSettings = view.findViewById(R.id.btnStatisticsSettings);
        actvLanguageSettings = view.findViewById(R.id.actvLanguageSettings);
        ivProfilePictureSettings = view.findViewById(R.id.ivProfilePictureSettings);
        etUsernameSettings = view.findViewById(R.id.etUsernameSettings);
        tilUsernameSettings = view.findViewById(R.id.tilUsernameSettings);
        swDarkModeSettings = view.findViewById(R.id.swDarkModeSettings);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("USER_ID", null);

        loadProfile();

        ivProfilePictureSettings.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSaveProfileSettings.setOnClickListener(v -> saveProfile());

        etUsernameSettings.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                v.clearFocus();
                return true;
            }
            return false;
        });

        btnStatisticsSettings.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_statisticsFragment);
        });

        String[] languages = {"Magyar", "English"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, languages) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = languages;
                        results.count = languages.length;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };
        actvLanguageSettings.setAdapter(adapter);

        String defaultLanguage = LocaleListCompat.getDefault().get(0).toLanguageTag().startsWith("hu") ? "hu" : "en";
        String savedLanguage = prefs.getString("SELECTED_LANGUAGE", defaultLanguage);

        if (savedLanguage.equals("hu")) {
            actvLanguageSettings.setText(languages[0], false);
        } else {
            actvLanguageSettings.setText(languages[1], false);
        }

        actvLanguageSettings.setOnItemClickListener((parent, view1, position, id) -> {
            actvLanguageSettings.dismissDropDown();
            actvLanguageSettings.clearFocus();

            String targetLocale = (position == 0) ? "hu" : "en";
            String currentSaved = prefs.getString("SELECTED_LANGUAGE", defaultLanguage);

            if (!targetLocale.equals(currentSaved)) {
                prefs.edit().putString("SELECTED_LANGUAGE", targetLocale).apply();
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(targetLocale));
            }
        });

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES;
        boolean isSystemDark = currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        boolean isDarkSaved = prefs.getBoolean("DARK_MODE", isSystemDark);

        swDarkModeSettings.setChecked(isDarkSaved);

        swDarkModeSettings.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("DARK_MODE", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        btnLogoutSettings.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(getString(R.string.logout))
                    .setMessage(getString(R.string.logout_message))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        prefs.edit().remove("JWT_TOKEN").apply();

                        Context appContext = requireContext();
                        Executors.newSingleThreadExecutor().execute(() -> {
                            AppDatabase.getDatabase(appContext).clearAllTables();
                        });

                        Toast.makeText(requireContext(), getString(R.string.success_logout), Toast.LENGTH_SHORT).show();

                        Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_loginFragment);
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    private void loadProfile() {
        if (currentUserId == null) return;

        SupabaseApi api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);
        api.getProfile("eq." + currentUserId).enqueue(new Callback<List<ProfileModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProfileModel>> call, @NonNull Response<List<ProfileModel>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    ProfileModel profile = response.body().get(0);

                    if (profile.getUsername() != null) {
                        etUsernameSettings.setText(profile.getUsername());
                    }

                    currentAvatarUrl = profile.getAvatarUrl();

                    if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(currentAvatarUrl)
                                .circleCrop()
                                .into(ivProfilePictureSettings);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProfileModel>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e("API_HIBA", "Load profile failure: " + t.getMessage());
                }
            }
        });
    }

    private void saveProfile() {
        String username = etUsernameSettings.getText() != null ? etUsernameSettings.getText().toString().trim() : "";

        tilUsernameSettings.setError(null);

        if (username.isEmpty()) {
            tilUsernameSettings.setError(getString(R.string.error_username_required));
            return;
        }

        if (selectedImageUri != null) {
            uploadImageAndSaveProfile(username);
        } else {
            saveProfileDataToSupabase(username, currentAvatarUrl);
        }
    }

    private void uploadImageAndSaveProfile(String username) {
        if (currentUserId == null) return;

        try {
            Bitmap originalBitmap;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), selectedImageUri);
                originalBitmap = ImageDecoder.decodeBitmap(source);
            } else {
                @SuppressWarnings("deprecation")
                Bitmap fallbackBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImageUri);
                originalBitmap = fallbackBitmap;
            }

            if (originalBitmap == null) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_unsupported_image_format), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            int maxWidth = 800;
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();

            if (width > maxWidth) {
                float ratio = (float) width / height;
                width = maxWidth;
                height = (int) (width / ratio);
            }
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, buffer);
            byte[] imageBytes = buffer.toByteArray();

            String fileName = currentUserId + "_" + System.currentTimeMillis() + ".jpg";
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);

            SupabaseApi api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);
            api.uploadAvatar(fileName, requestBody).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (isAdded()) {
                        if (response.isSuccessful()) {
                            String fileUrl = BuildConfig.SUPABASE_URL + "/storage/v1/object/public/avatars/" + fileName;
                            saveProfileDataToSupabase(username, fileUrl);
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.error_uploading_image), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), getString(R.string.error_uploading_image), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            if (isAdded()) {
                Toast.makeText(requireContext(), getString(R.string.error_uploading_image), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfileDataToSupabase(String username, String avatarUrl) {
        if (currentUserId == null) return;

        String oldAvatarUrlToDelete = currentAvatarUrl;

        ProfileModel updatedProfile = new ProfileModel(currentUserId, username, avatarUrl);
        SupabaseApi api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);

        api.upsertProfile(updatedProfile).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        currentAvatarUrl = avatarUrl;
                        selectedImageUri = null;
                        if (tilUsernameSettings != null) tilUsernameSettings.setError(null);

                        if (oldAvatarUrlToDelete != null && !oldAvatarUrlToDelete.equals(avatarUrl)) {
                            deleteOldAvatar(oldAvatarUrlToDelete);
                        }

                        Toast.makeText(requireContext(), getString(R.string.profile_saved_successfully), Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMsg = getString(R.string.error_saving_profile);

                        try {
                            if (response.errorBody() != null) {
                                String errorJson = response.errorBody().string();

                                if (errorJson.contains("profiles_username_key") || errorJson.contains("duplicate key")) {
                                    errorMsg = getString(R.string.error_username_taken);
                                    if (tilUsernameSettings != null) tilUsernameSettings.setError(errorMsg);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_saving_profile), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteOldAvatar(String oldAvatarUrl) {
        if (oldAvatarUrl == null || oldAvatarUrl.isEmpty() || !oldAvatarUrl.contains("avatars/")) {
            return;
        }

        String oldFileName = oldAvatarUrl.substring(oldAvatarUrl.lastIndexOf("avatars/") + 8);

        Map<String, List<String>> payload = new HashMap<>();
        payload.put("prefixes", Arrays.asList(oldFileName));

        SupabaseApi api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);
        api.deleteAvatars(payload).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("SUPABASE_STORAGE", "Régi kép törölve: " + oldFileName);
                } else {
                    Log.e("SUPABASE_STORAGE", "Nem sikerült törölni a régi képet: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("SUPABASE_STORAGE", "Hálózati hiba törléskor: " + t.getMessage());
            }
        });
    }
}