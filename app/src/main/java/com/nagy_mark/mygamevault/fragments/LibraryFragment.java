package com.nagy_mark.mygamevault.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.adapters.LibraryAdapter;
import com.nagy_mark.mygamevault.models.FeedActivityRequest;
import com.nagy_mark.mygamevault.models.SavedGameModel;
import com.nagy_mark.mygamevault.network.SupabaseApi;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryFragment extends Fragment {
    private RecyclerView rvLibrary;
    private LibraryAdapter adapter;
    private AutoCompleteTextView actvSortLibrary;
    private TextInputEditText etSearchLibrary;
    private TextView tvEmptyLibrary;
    private SwitchMaterial swFavoritesFilterLibrary;

    private SupabaseApi api;
    private SharedPreferences prefs;

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
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            currentSortPosition = savedInstanceState.getInt("SORT_POSITION", 0);
        }

        api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);
        prefs = requireActivity().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);

        rvLibrary = view.findViewById(R.id.rvLibrary);
        actvSortLibrary = view.findViewById(R.id.actvSortLibrary);
        etSearchLibrary = view.findViewById(R.id.etSearchLibrary);
        tvEmptyLibrary = view.findViewById(R.id.tvEmptyLibrary);
        swFavoritesFilterLibrary = view.findViewById(R.id.swFavoritesFilterLibrary);

        setupRecyclerView();
        setupSorting();
        setupSearch();
        setupFavoriteFilter();

        loadLibraryGames();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("SORT_POSITION", currentSortPosition);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (actvSortLibrary != null) {
            String[] sortOptions = getResources().getStringArray(R.array.sort_options);
            actvSortLibrary.setText(sortOptions[currentSortPosition], false);
        }
    }

    private void setupFavoriteFilter() {
        swFavoritesFilterLibrary.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyFilterAndSort();
        });
    }

    private void setupRecyclerView() {
        rvLibrary.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LibraryAdapter(getContext(), new LibraryAdapter.OnLibraryItemClickListener() {
            @Override
            public void onDeleteClick(SavedGameModel game) {
                if (!isAdded()) return;
                new MaterialAlertDialogBuilder(requireContext())
                        .setIcon(R.drawable.ic_warning)
                        .setTitle(getString(R.string.delete_title))
                        .setMessage(getString(R.string.delete_message_library, game.getGameName()))
                        .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                            deleteGameFromDatabase(game);
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
            }

            @Override
            public void onItemClick(SavedGameModel game) {
                if (!isAdded()) return;
                Bundle bundle = new Bundle();
                bundle.putSerializable("gameData", game);

                Navigation.findNavController(requireView()).navigate(
                        R.id.action_libraryFragment_to_libraryDetailView,
                        bundle
                );
            }

            @Override
            public void onFavoriteClick(SavedGameModel game, int position) {
                if (!isAdded()) {
                    return;
                }
                toggleFavoriteStatus(game, position);
            }
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
        actvSortLibrary.setText(sortOptions[currentSortPosition], false);

        actvSortLibrary.setOnItemClickListener((parent, view, position, id) -> {
            currentSortPosition = position;
            applyFilterAndSort();
        });
    }

    private void setupSearch() {
        etSearchLibrary.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString().toLowerCase().trim();
                applyFilterAndSort();
            }
        });
    }

    private void loadLibraryGames() {
        String currentUserId = getCurrentUserId();

        if (currentUserId == null) return;

        api.getGamesByStatus("eq." + currentUserId, "in.(1,2,3)").enqueue(new Callback<List<SavedGameModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<SavedGameModel>> call, @NonNull Response<List<SavedGameModel>> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        allGames = response.body();
                        applyFilterAndSort();
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.error_data_load), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SavedGameModel>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_network_base), Toast.LENGTH_SHORT).show();
                    Log.e("API_HIBA", "Library load failure: " + t.getMessage());
                }
            }
        });
    }

    private void toggleFavoriteStatus(SavedGameModel game, int position) {
        boolean newFavoriteStatus = !game.isFavorite();

        Map<String, Boolean> updateBody = new HashMap<>();
        updateBody.put("is_favorite", newFavoriteStatus);

        api.updateFavoriteStatus("eq." + game.getId(), updateBody).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        game.setFavorite(newFavoriteStatus);

                        if (newFavoriteStatus) {
                            String currentUserId = getCurrentUserId();
                            if (currentUserId != null) {
                                logActivityToFeed(currentUserId, "ADDED_TO_FAVORITES", game.getGameName());
                            }
                        }

                        if (swFavoritesFilterLibrary.isChecked() && !newFavoriteStatus) {
                            applyFilterAndSort();
                        } else {
                            adapter.notifyItemChanged(position);
                        }
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.error_network_base), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void applyFilterAndSort() {
        displayedGames.clear();
        boolean showOnlyFavorites = swFavoritesFilterLibrary.isChecked();

        for (SavedGameModel game : allGames) {
            boolean matchesSearch = true;
            if (!currentSearchText.isEmpty()) {
                if (game.getGameName() == null || !game.getGameName().toLowerCase().contains(currentSearchText)) {
                    matchesSearch = false;
                }
            }

            boolean matchesFavorite = true;
            if (showOnlyFavorites) {
                matchesFavorite = game.isFavorite();
            }

            if (matchesSearch && matchesFavorite) {
                displayedGames.add(game);
            }
        }

        Collections.sort(displayedGames, (g1, g2) -> {
            String name1 = g1.getGameName() != null ? g1.getGameName() : "";
            String name2 = g2.getGameName() != null ? g2.getGameName() : "";

            String year1 = g1.getReleaseYear() != null ? g1.getReleaseYear() : "";
            String year2 = g2.getReleaseYear() != null ? g2.getReleaseYear() : "";

            switch (currentSortPosition) {
                case 0: return name1.compareToIgnoreCase(name2);
                case 1: return name2.compareToIgnoreCase(name1);
                case 2: return year2.compareTo(year1);
                case 3: return year1.compareTo(year2);
                default: return 0;
            }
        });

        adapter.setGames(displayedGames);

        if (displayedGames.isEmpty()) {
            tvEmptyLibrary.setVisibility(View.VISIBLE);
            rvLibrary.setVisibility(View.INVISIBLE);
        } else {
            tvEmptyLibrary.setVisibility(View.GONE);
            rvLibrary.setVisibility(View.VISIBLE);
        }
    }

    private void deleteGameFromDatabase(SavedGameModel game) {
        api.deleteGame("eq." + game.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        allGames.remove(game);
                        displayedGames.remove(game);
                        adapter.notifyDataSetChanged();

                        if (displayedGames.isEmpty()) {
                            tvEmptyLibrary.setVisibility(View.VISIBLE);
                            rvLibrary.setVisibility(View.INVISIBLE);
                        }

                        Toast.makeText(requireContext(), getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.delete_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    Log.e("API_HIBA", "Library delete failure: " + t.getMessage());
                }
            }
        });
    }

    private void logActivityToFeed(String userId, String actionType, String gameName) {
        FeedActivityRequest request = new FeedActivityRequest(userId, actionType, gameName, null, null);

        api.logFeedActivity(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.e("FEED_ERROR", "Nem sikerült a kedvenc eseményt naplózni: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("FEED_ERROR", "Hálózati hiba a feed naplózásakor", t);
            }
        });
    }

    private String getCurrentUserId() {
        return prefs.getString("USER_ID", null);
    }
}