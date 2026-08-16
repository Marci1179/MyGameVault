package com.nagy_mark.mygamevault.Fragments;

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
import com.nagy_mark.mygamevault.BuildConfig;
import com.nagy_mark.mygamevault.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Struct;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

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

            CompletableFuture.runAsync(() -> {
                try {
                    String supabaseUrl = BuildConfig.SUPABASE_URL + "/auth/v1/signup";
                    String supabaseApiKey = BuildConfig.SUPABASE_API_KEY;

                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("email", email);
                    jsonBody.put("password", password);

                    URL url = new URL(supabaseUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.addRequestProperty("apikey", supabaseApiKey);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonBody.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();

                    if (responseCode == 200 || responseCode == 201) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), getString(R.string.success_registration), Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_loginFragment);
                        });
                    } else {
                        InputStream errorStream = conn.getErrorStream();
                        String errorMsg = "";
                        if (errorStream != null) {
                            Scanner s = new Scanner(errorStream, "UTF-8").useDelimiter("\\A");
                            String result;
                            if (s.hasNext()) {
                                result = s.next();
                            } else {
                                result = "";
                            }
                            try {
                                JSONObject errObj = new org.json.JSONObject(result);
                                errorMsg = errObj.optString("msg", result);
                            } catch (Exception e) {
                                errorMsg = result;
                            }
                        }

                        final boolean isEmailRegistered = errorMsg.contains("User already registered");
                        final String translatedError;
                        if (isEmailRegistered) {
                            translatedError = getString(R.string.error_email_registered);
                        } else {
                            translatedError = getString(R.string.error_unknown, errorMsg);
                        }

                        getActivity().runOnUiThread(() -> {
                            if (isEmailRegistered && tilRegisterEmail != null) {
                                tilRegisterEmail.setError(translatedError);
                            } else {
                                Toast.makeText(getContext(), translatedError, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    conn.disconnect();

                } catch (Exception e) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), getString(R.string.error_network, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }
}