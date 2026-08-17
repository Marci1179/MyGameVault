package com.nagy_mark.mygamevault.Fragments;

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
import com.nagy_mark.mygamevault.BuildConfig;
import com.nagy_mark.mygamevault.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class LoginFragment extends Fragment {

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

        SharedPreferences checkPrefs = requireActivity().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
        String savedToken = checkPrefs.getString("JWT_TOKEN", null);

        if (savedToken != null && !savedToken.isEmpty()) {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_libraryFragment);
            return;
        }

        TextView register = view.findViewById(R.id.tvRegister);
        TextInputLayout tilLoginEmail = view.findViewById(R.id.tilLoginEmail);
        TextInputLayout tilLoginPassword = view.findViewById(R.id.tilLoginPassword);
        TextInputEditText etLoginEmail = view.findViewById(R.id.etLoginEmail);
        TextInputEditText etLoginPassword = view.findViewById(R.id.etLoginPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);

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

            CompletableFuture.runAsync(() -> {
               try {
                   String supabaseUrl = BuildConfig.SUPABASE_URL + "/auth/v1/token?grant_type=password";
                   String supabaseApiKey = BuildConfig.SUPABASE_API_KEY;

                   JSONObject jsonBody = new JSONObject();
                   jsonBody.put("email", email);
                   jsonBody.put("password", password);

                   URL url = new URL(supabaseUrl);
                   HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                   conn.setRequestMethod("POST");
                   conn.setRequestProperty("apikey", supabaseApiKey);
                   conn.setRequestProperty("Content-Type", "application/json");
                   conn.setDoOutput(true);

                   try (OutputStream os = conn.getOutputStream()) {
                       byte[] input = jsonBody.toString().getBytes("utf-8");
                       os.write(input, 0, input.length);
                   }

                   int responseCode = conn.getResponseCode();

                   if (responseCode == 200) {
                       InputStream responseStream = conn.getInputStream();
                       Scanner s = new Scanner(responseStream, "UTF-8").useDelimiter("\\A");
                       String result;
                       if (s.hasNext()) {
                           result = s.next();
                       } else {
                           result = "";
                       }

                       JSONObject jsonResponse = new JSONObject(result);
                       String accessToken = jsonResponse.getString("access_token");

                       SharedPreferences prefs = requireActivity().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
                       prefs.edit().putString("JWT_TOKEN", accessToken).apply();

                       getActivity().runOnUiThread(() -> {
                           Toast.makeText(getContext(), getString(R.string.success_login), Toast.LENGTH_SHORT).show();
                           Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_libraryFragment);
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

                       final String translatedError;
                       if (errorMsg.contains("Invalid login credentials")) {
                           translatedError = getString(R.string.error_invalid_credentials);
                       } else {
                           translatedError = getString(R.string.error_occurred, errorMsg);
                       }

                       getActivity().runOnUiThread(() -> {
                           Toast.makeText(getContext(), translatedError, Toast.LENGTH_SHORT).show();

                           if (tilLoginEmail != null) tilLoginEmail.setError(" ");
                           if (tilLoginPassword != null) tilLoginPassword.setError(" ");
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