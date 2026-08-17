package com.nagy_mark.mygamevault.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.nagy_mark.mygamevault.network.ApiClient;
import com.nagy_mark.mygamevault.network.SupabaseApi;

public class RegisterFragment extends Fragment {

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText etRegisterEmail = view.findViewById(R.id.etRegisterEmail);
        TextInputEditText etRegisterPassword = view.findViewById(R.id.etRegisterPassword);
        TextInputEditText etRegisterPasswordConfirm = view.findViewById(R.id.etRegisterPasswordConfirm);
        TextInputLayout tilRegisterEmail = view.findViewById(R.id.tilRegisterEmail);
        TextInputLayout tilRegisterPassword = view.findViewById(R.id.tilRegisterPassword);
        TextInputLayout tilRegisterPasswordConfirm = view.findViewById(R.id.tilRegisterPasswordConfirm);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        TextView login = view.findViewById(R.id.tvLogin);

        login.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment);
        });

        btnRegister.setOnClickListener(v -> {
            if (tilRegisterEmail != null) tilRegisterEmail.setError(null);
            if (tilRegisterPassword != null) tilRegisterPassword.setError(null);
            if (tilRegisterPasswordConfirm != null) tilRegisterPasswordConfirm.setError(null);

            String email = etRegisterEmail.getText().toString().trim();
            String password = etRegisterPassword.getText().toString().trim();
            String passwordConfirm = etRegisterPasswordConfirm.getText().toString().trim();

            boolean hasError = false;

            if (email.isEmpty()) {
                tilRegisterEmail.setError(getString(R.string.error_email_required));
                hasError = true;
            }

            if (password.isEmpty()) {
                tilRegisterPassword.setError(getString(R.string.error_password_required));
                hasError = true;
            } else if (password.length() < 6) {
                tilRegisterPassword.setError(getString(R.string.error_password_too_short));
                hasError = true;
            }

            if (passwordConfirm.isEmpty()) {
                tilRegisterPasswordConfirm.setError(getString(R.string.error_password_confirm_required));
                hasError = true;
            } else if (!password.equals(passwordConfirm)) {
                tilRegisterPasswordConfirm.setError(getString(R.string.error_passwords_mismatch));
                hasError = true;
            }

            if (hasError) {
                return;
            }

            AuthRequest request = new AuthRequest(email, password);

            SupabaseApi api = ApiClient.getClient(requireContext()).create(SupabaseApi.class);

            api.register(request).enqueue(new retrofit2.Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<AuthResponse> call, @NonNull retrofit2.Response<AuthResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), getString(R.string.success_registration), Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment);
                    } else {
                        String errorMsg = getString(R.string.error_unknown);
                        boolean isEmailRegistered = false;

                        try {
                            if (response.errorBody() != null) {
                                String errorJson = response.errorBody().string();

                                if (errorJson.contains("User already registered")) {
                                    isEmailRegistered = true;
                                    errorMsg = getString(R.string.error_email_registered);
                                } else {
                                    errorMsg = errorMsg + " " + errorJson;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (isEmailRegistered && tilRegisterEmail != null) {
                            tilRegisterEmail.setError(errorMsg);
                        } else {
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<AuthResponse> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), getString(R.string.error_network, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}