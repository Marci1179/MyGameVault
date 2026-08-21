package com.nagy_mark.mygamevault.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.adapters.LibraryAdapter;
import com.nagy_mark.mygamevault.models.SavedGameModel;
import com.nagy_mark.mygamevault.network.SupabaseApi;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryFragment extends Fragment {
    private RecyclerView rvLibrary;
    private LibraryAdapter adapter;
    private AutoCompleteTextView actvSortLibrary;
    private TextInputEditText etSearchLibrary;

    private SupabaseApi api;

    private List<SavedGameModel> allGames = new ArrayList<>();
    private List<SavedGameModel> displayedGames = new ArrayList<>();

    private int currentSortPosition = 0;
    private String currentSearchText = "";

    public LibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLibrary = view.findViewById(R.id.rvLibrary);
        actvSortLibrary = view.findViewById(R.id.actvSortLibrary);
        etSearchLibrary = view.findViewById(R.id.etSearchLibrary);

        api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);

        setupRecyclerView();
        setupSorting();
        setupSearch();

        loadLibraryGames();
    }

    private void setupRecyclerView() {
        rvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LibraryAdapter(getContext(), game -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(getString(R.string.delete_title))
                    .setMessage(getString(R.string.delete_message_library, game.getGameName()))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        deleteGameFromDatabase(game);
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });
        rvLibrary.setAdapter(adapter);
    }

    private void setupSorting() {
        String[] sortOptions = getResources().getStringArray(R.array.sort_options);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, sortOptions) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = sortOptions;
                        results.count = sortOptions.length;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };

        actvSortLibrary.setAdapter(sortAdapter);
        actvSortLibrary.setText(sortOptions[0], false);

        actvSortLibrary.setOnItemClickListener((parent, view, position, id) -> {
            currentSortPosition = position;
            applyFilterAndSort();
        });
    }

    private void setupSearch() {
        etSearchLibrary.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString().toLowerCase().trim();
                applyFilterAndSort();
            }
        });
    }

    private void loadLibraryGames() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("USER_ID", null);

        if (currentUserId == null) return;

        api.getGamesByStatus("eq." + currentUserId, "in.(1,2,3)").enqueue(new Callback<List<SavedGameModel>>() {
            @Override
            public void onResponse(Call<List<SavedGameModel>> call, Response<List<SavedGameModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allGames = response.body();
                    applyFilterAndSort();
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_data_load), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SavedGameModel>> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_network_base), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilterAndSort() {
        displayedGames.clear();

        if (currentSearchText.isEmpty()) {
            displayedGames.addAll(allGames);
        } else {
            for (SavedGameModel game : allGames) {
                if (game.getGameName() != null && game.getGameName().toLowerCase().contains(currentSearchText)) {
                    displayedGames.add(game);
                }
            }
        }

        Collections.sort(displayedGames, (g1, g2) -> {
            String name1 = g1.getGameName() != null ? g1.getGameName() : "";
            String name2 = g2.getGameName() != null ? g2.getGameName() : "";

            String year1 = g1.getReleaseYear() != null ? g1.getReleaseYear() : "";
            String year2 = g2.getReleaseYear() != null ? g2.getReleaseYear() : "";

            switch (currentSortPosition) {
                case 0: // Név (A-Z)
                    return name1.compareToIgnoreCase(name2);
                case 1: // Név (Z-A)
                    return name2.compareToIgnoreCase(name1);
                case 2: // Megjelenési év (Legújabb) -> Csökkenő sorrend
                    return year2.compareTo(year1);
                case 3: // Megjelenési év (Legrégebbi) -> Növekvő sorrend
                    return year1.compareTo(year2);
                default:
                    return 0;
            }
        });

        adapter.setGames(displayedGames);
    }

    private void deleteGameFromDatabase(SavedGameModel game) {
        api.deleteGame("eq." + game.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    allGames.remove(game);
                    displayedGames.remove(game);

                    adapter.notifyDataSetChanged();

                    Toast.makeText(getContext(), getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.delete_error), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }
}