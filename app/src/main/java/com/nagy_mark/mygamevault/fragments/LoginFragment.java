package com.nagy_mark.mygamevault.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.models.AuthResponse;
import com.nagy_mark.mygamevault.models.AuthRequest;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;
import com.nagy_mark.mygamevault.network.SupabaseApi;

public class LoginFragment extends Fragment {

    TextView register;
    TextInputLayout tilLoginEmail, tilLoginPassword;
    TextInputEditText etLoginEmail, etLoginPassword;
    Button btnLogin;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
        String savedToken = prefs.getString("JWT_TOKEN", null);

        if (savedToken != null && !savedToken.isEmpty()) {
            NavController navController = Navigation.findNavController(view);

            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.loginFragment) {
                navController.navigate(R.id.action_loginFragment_to_libraryFragment);
            }
            return;
        }

        register = view.findViewById(R.id.tvRegister);
        tilLoginEmail = view.findViewById(R.id.tilLoginEmail);
        tilLoginPassword = view.findViewById(R.id.tilLoginPassword);
        etLoginEmail = view.findViewById(R.id.etLoginEmail);
        etLoginPassword = view.findViewById(R.id.etLoginPassword);
        btnLogin = view.findViewById(R.id.btnLogin);

        register.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment);
        });

        btnLogin.setOnClickListener(v -> {
            if (tilLoginEmail != null) tilLoginEmail.setError(null);
            if (tilLoginPassword != null) tilLoginPassword.setError(null);

            String email = etLoginEmail.getText().toString().trim();
            String password = etLoginPassword.getText().toString().trim();

            boolean hasError = false;

            if (email.isEmpty()) {
                tilLoginEmail.setError(getString(R.string.error_email_required));
                hasError = true;
            }

            if (password.isEmpty()) {
                tilLoginPassword.setError(getString(R.string.error_password_required));
                hasError = true;
            }

            if (hasError) {
                return;
            }

            AuthRequest request = new AuthRequest(email, password);

            SupabaseApi api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);

            api.login(request).enqueue(new retrofit2.Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<AuthResponse> call, @NonNull retrofit2.Response<AuthResponse> response) {
                    if (isAdded()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String accessToken = response.body().getAccessToken();
                            String refreshToken = response.body().getRefreshToken();
                            String userId = response.body().getUser().getId();

                            prefs.edit()
                                    .putString("JWT_TOKEN", accessToken)
                                    .putString("REFRESH_TOKEN", refreshToken)
                                    .putString("USER_ID", userId)
                                    .apply();

                            Toast.makeText(requireContext(), getString(R.string.success_login), Toast.LENGTH_SHORT).show();

                            NavController navController = Navigation.findNavController(view);
                            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.loginFragment) {
                                navController.navigate(R.id.action_loginFragment_to_libraryFragment);
                            }
                        } else {
                            String errorMsg = getString(R.string.error_unknown);
                            try {
                                if (response.errorBody() != null) {
                                    String errorJson = response.errorBody().string();
                                    if (errorJson.contains("Invalid login credentials")) {
                                        errorMsg = getString(R.string.error_invalid_credentials);
                                    } else {
                                        errorMsg = getString(R.string.error_occurred, errorJson);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                            if (tilLoginEmail != null) tilLoginEmail.setError(" ");
                            if (tilLoginPassword != null) tilLoginPassword.setError(" ");
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<AuthResponse> call, @NonNull Throwable t) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}