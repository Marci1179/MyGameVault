package com.nagy_mark.mygamevault.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nagy_mark.mygamevault.R;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnLogout = view.findViewById(R.id.btnLogout);
        Spinner spLanguage = view.findViewById(R.id.spLanguage);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);

        String[] languages = {"Magyar", "English"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, languages);
        spLanguage.setAdapter(adapter);

        String defaultLanguage;
        if (LocaleListCompat.getDefault().get(0).toLanguageTag().startsWith("hu")) {
            defaultLanguage = "hu";
        } else {
            defaultLanguage = "en";
        }

        String savedLanguage = prefs.getString("SELECTED_LANGUAGE", defaultLanguage);

        if (savedLanguage.equals("hu")) {
            spLanguage.setSelection(0);
        } else {
            spLanguage.setSelection(1);
        }

        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String targetLocale;
                if (position == 0) {
                    targetLocale = "hu";
                } else {
                    targetLocale = "en";
                }

                String currentSaved = prefs.getString("SELECTED_LANGUAGE", defaultLanguage);

                if (!targetLocale.equals(currentSaved)) {
                    prefs.edit().putString("SELECTED_LANGUAGE", targetLocale).apply();

                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(targetLocale)
                    );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(getString(R.string.logout))
                    .setMessage(getString(R.string.logout_message))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        prefs.edit().remove("JWT_TOKEN").apply();

                        Toast.makeText(getContext(), getString(R.string.success_logout), Toast.LENGTH_SHORT).show();

                        Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_loginFragment);
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
    }
}