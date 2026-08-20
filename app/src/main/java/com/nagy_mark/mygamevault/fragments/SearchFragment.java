package com.nagy_mark.mygamevault.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.adapters.GameSearchAdapter;
import com.nagy_mark.mygamevault.models.Game;
import com.nagy_mark.mygamevault.models.MyGame;
import com.nagy_mark.mygamevault.models.SavedGameModel;
import com.nagy_mark.mygamevault.network.IgdbApi;
import com.nagy_mark.mygamevault.network.IgdbApiClient;
import com.nagy_mark.mygamevault.network.SupabaseApi;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SearchFragment extends Fragment {
    private RecyclerView rvSearchResults;
    private GameSearchAdapter adapter;
    private TextInputEditText etSearch;

    private final Map<String, Integer> savedGamesMap = new HashMap<>();

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etSearch);
        rvSearchResults = view.findViewById(R.id.rvSearch);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new GameSearchAdapter((game, statusId) -> {
            saveGameToSupabase(game, statusId);
        }, savedGamesMap);

        rvSearchResults.setAdapter(adapter);

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    searchGames(query);
                }
                return true;
            }
            return false;
        });

        loadTopGames();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserSavedGames();
    }

    private void loadUserSavedGames() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        SupabaseApi api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);
        api.getUserSavedGames("eq." + userId, "game_name,status_id").enqueue(new Callback<List<SavedGameModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<SavedGameModel>> call, @NonNull Response<List<SavedGameModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    savedGamesMap.clear();
                    for (SavedGameModel item : response.body()) {
                        savedGamesMap.put(item.getGameName(), item.getStatusId());
                    }
                    if (adapter != null) adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SavedGameModel>> call, @NonNull Throwable t) {

            }
        });
    }

    private void loadTopGames() {
        String query = "fields name, cover.image_id, first_release_date, involved_companies.company.name, involved_companies.publisher; where rating_count > 500 & parent_game = null; sort rating desc; limit 10;";
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), query);

        IgdbApi api = IgdbApiClient.getClient(requireContext()).create(IgdbApi.class);

        api.getTopGames(body).enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(@NonNull Call<List<Game>> call, @NonNull Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setGames(response.body());
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_download), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Game>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_network) + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchGames(String searchQuery) {
        String query = "search \"" + searchQuery + "\"; fields name, cover.image_id, first_release_date, involved_companies.company.name, involved_companies.publisher; where parent_game = null; limit 20;";
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), query);

        IgdbApi api = IgdbApiClient.getClient(requireContext()).create(IgdbApi.class);

        api.getTopGames(body).enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(@NonNull Call<List<Game>> call, @NonNull Response<List<Game>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setGames(response.body());

                    if (response.body().isEmpty()) {
                        Toast.makeText(getContext(), getString(R.string.no_results), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_search), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Game>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_network) + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveGameToSupabase(Game game, int statusId) {
        String releaseDateFormatted = null;
        if (game.getFirstReleaseDate() != null) {
            Date date = new Date(game.getFirstReleaseDate() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            releaseDateFormatted = sdf.format(date);
        }

        String coverId = (game.getCover() != null) ? game.getCover().getImageId() : null;

        String currentUserId = getCurrentUserId();

        MyGame newGame = new MyGame(
                game.getName(),
                releaseDateFormatted,
                game.getPublisherName(),
                coverId,
                statusId,
                currentUserId
        );

        SupabaseApi api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);
        api.insertGame(newGame).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    savedGamesMap.put(game.getName(), statusId);
                    adapter.notifyDataSetChanged();

                    String message = (statusId == 1) ? getString(R.string.game_added_library) : getString(R.string.game_added_wishlist);
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String unknownError = getString(R.string.error_unknown_supabase);
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : unknownError;

                        android.util.Log.e("SUPABASE_ERROR", "Mentési hiba kód: " + response.code() + " | Üzenet: " + errorBody);
                        Toast.makeText(getContext(), getString(R.string.error_save_failed), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), getString(R.string.error_network) + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrentUserId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
        return prefs.getString("USER_ID", null);
    }
}