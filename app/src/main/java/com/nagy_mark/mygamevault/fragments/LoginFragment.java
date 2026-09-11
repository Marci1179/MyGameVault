package com.nagy_mark.mygamevault.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.nagy_mark.mygamevault.utils.DialogUtils;
import com.nagy_mark.mygamevault.utils.SessionManager;
import com.nagy_mark.mygamevault.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private TextView register, tvForgotPassword;
    private TextInputLayout tilLoginEmail, tilLoginPassword;
    private TextInputEditText etLoginEmail, etLoginPassword;
    private Button btnLogin;

    private SupabaseApi api;
    private SessionManager sessionManager;

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

        api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);
        sessionManager = new SessionManager(requireContext());

        if (sessionManager.isLoggedIn()) {
            NavController navController = Navigation.findNavController(view);

            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.loginFragment) {
                navController.navigate(R.id.action_loginFragment_to_libraryFragment);
            }
            return;
        }

        register = view.findViewById(R.id.tvRegister);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        tilLoginEmail = view.findViewById(R.id.tilLoginEmail);
        tilLoginPassword = view.findViewById(R.id.tilLoginPassword);
        etLoginEmail = view.findViewById(R.id.etLoginEmail);
        etLoginPassword = view.findViewById(R.id.etLoginPassword);
        btnLogin = view.findViewById(R.id.btnLogin);

        register.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment);
        });

        tvForgotPassword.setOnClickListener(v -> {
            DialogUtils.showForgotPasswordDialog(requireContext(), getLayoutInflater(), (email, emailDialog) -> {
                sendPasswordResetEmail(email, emailDialog);
            });
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
            } else if (!ValidationUtils.isValidEmail(email)) {
                tilLoginEmail.setError(getString(R.string.error_invalid_email));
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

            api.login(request).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    if (isAdded()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String accessToken = response.body().getAccessToken();
                            String refreshToken = response.body().getRefreshToken();
                            String userId = response.body().getUser().getId();

                            sessionManager.saveSession(accessToken, refreshToken, userId);

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
                public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void sendPasswordResetEmail(String email, AlertDialog emailDialog) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        api.recoverPassword(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        emailDialog.dismiss();
                        Toast.makeText(requireContext(), getString(R.string.success_password_reset_email), Toast.LENGTH_LONG).show();

                        DialogUtils.showResetPasswordOtpDialog(requireContext(), getLayoutInflater(), (otpCode, newPassword, otpDialog) -> {
                            verifyOtpAndChangePassword(email, otpCode, newPassword, otpDialog);
                        });
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.error_password_reset), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verifyOtpAndChangePassword(String email, String otpCode, String newPassword, AlertDialog otpDialog) {
        Map<String, String> verifyBody = new HashMap<>();
        verifyBody.put("type", "recovery");
        verifyBody.put("email", email);
        verifyBody.put("token", otpCode);

        api.verifyOtp(verifyBody).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String tempAccessToken = response.body().getAccessToken();
                        String refreshToken = response.body().getRefreshToken();
                        String userId = response.body().getUser().getId();

                        Map<String, String> updateBody = new HashMap<>();
                        updateBody.put("password", newPassword);

                        api.updatePassword("Bearer " + tempAccessToken, updateBody).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> updateResponse) {
                                if (isAdded()) {
                                    if (updateResponse.isSuccessful()) {
                                        sessionManager.saveSession(tempAccessToken, refreshToken, userId);

                                        otpDialog.dismiss();
                                        Toast.makeText(requireContext(), getString(R.string.password_changed_successfully), Toast.LENGTH_SHORT).show();

                                        NavController navController = Navigation.findNavController(requireView());
                                        if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.loginFragment) {
                                            navController.navigate(R.id.action_loginFragment_to_libraryFragment);
                                        }
                                    } else {
                                        Toast.makeText(requireContext(), getString(R.string.error_changing_password), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                if (isAdded()) {
                                    Toast.makeText(requireContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.error_invalid_otp), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}