package com.nagy_mark.mygamevault.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

        btnLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(getString(R.string.logout))
                    .setMessage(getString(R.string.logout_message))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        SharedPreferences prefs = requireActivity().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
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