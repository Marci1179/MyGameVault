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
import com.nagy_mark.mygamevault.adapters.WishlistAdapter;
import com.nagy_mark.mygamevault.database.AppDatabase;
import com.nagy_mark.mygamevault.database.WishlistPriceEntity;
import com.nagy_mark.mygamevault.models.CheapSharkDealInfo;
import com.nagy_mark.mygamevault.models.CheapSharkGameDetailResponse;
import com.nagy_mark.mygamevault.models.CheapSharkGameSearchResult;
import com.nagy_mark.mygamevault.models.SavedGameModel;
import com.nagy_mark.mygamevault.network.CheapSharkApi;
import com.nagy_mark.mygamevault.network.CheapSharkApiClient;
import com.nagy_mark.mygamevault.network.SupabaseApi;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistFragment extends Fragment {
    private RecyclerView rvWishlist;
    private WishlistAdapter adapter;
    private AutoCompleteTextView actvSortWishlist;
    private TextInputEditText etSearchWishlist;

    private SupabaseApi api;

    private List<SavedGameModel> allGames = new ArrayList<>();
    private List<SavedGameModel> displayedGames = new ArrayList<>();

    private int currentSortPosition = 0;
    private String currentSearchText = "";

    public WishlistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvWishlist = view.findViewById(R.id.rvWishlist);
        actvSortWishlist = view.findViewById(R.id.actvSortWishlist);
        etSearchWishlist = view.findViewById(R.id.etSearchWishlist);

        api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);

        setupRecyclerView();
        setupSorting();
        setupSearch();

        loadWishlistGames();
    }

    private void setupRecyclerView() {
        rvWishlist.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new WishlistAdapter(getContext(), game -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(getString(R.string.delete_title))
                    .setMessage(getString(R.string.delete_message_wishlist, game.getGameName()))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        deleteGameFromDatabase(game);
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });
        rvWishlist.setAdapter(adapter);
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

        actvSortWishlist.setAdapter(sortAdapter);
        actvSortWishlist.setText(sortOptions[0], false);

        actvSortWishlist.setOnItemClickListener((parent, view, position, id) -> {
            currentSortPosition = position;
            applyFilterAndSort();
        });
    }

    private void setupSearch() {
        etSearchWishlist.addTextChangedListener(new TextWatcher() {
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

    private void loadWishlistGames() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("USER_ID", null);

        if (currentUserId == null) return;

        api.getGamesByStatus("eq." + currentUserId, "eq.4").enqueue(new Callback<List<SavedGameModel>>() {
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
        fetchPricesForWishlist(displayedGames);
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

                    Executors.newSingleThreadExecutor().execute(() -> {
                        if (getContext() != null) {
                            AppDatabase.getDatabase(getContext()).wishlistPriceDao().deletePrice(game.getId());
                        }
                    });
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

    private String getStoreName(String storeId) {
        switch (storeId) {
            case "1": return "Steam";
            case "3": return "GreenManGaming";
            case "7": return "GOG";
            case "8": return "EA/Origin";
            case "11": return "Humble Store";
            case "13": return "Ubisoft";
            case "15": return "Fanatical";
            case "25": return "Epic Games";
            default: return null;
        }
    }

    private void fetchPricesForWishlist(List<SavedGameModel> gamesToFetch) {
        AppDatabase db = AppDatabase.getDatabase(requireContext());

        Executors.newSingleThreadExecutor().execute(() -> {
            for (SavedGameModel game : gamesToFetch) {
                if (game.getGameName() == null || game.getGameName().isEmpty()) continue;

                var cachedPrice = db.wishlistPriceDao().getPriceForGame(game.getId());

                if (cachedPrice != null && cachedPrice.getLastKnownPrice() > 0) {
                    String priceText = getString(R.string.lowest_price_format, String.valueOf(cachedPrice.getLastKnownPrice()), cachedPrice.getStoreName());

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> adapter.setGamePrice(game.getId(), priceText));
                    }
                } else {
                    fetchAndSavePriceFromApi(game, db);
                }
            }
        });
    }

    private  void fetchAndSavePriceFromApi(SavedGameModel game, AppDatabase db) {
        CheapSharkApi cheapSharkApi = CheapSharkApiClient.getClient().create(CheapSharkApi.class);

        cheapSharkApi.searchGame(game.getGameName(), 1).enqueue(new Callback<List<CheapSharkGameSearchResult>>() {
            @Override
            public void onResponse(Call<List<CheapSharkGameSearchResult>> call, Response<List<CheapSharkGameSearchResult>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String gameId = response.body().get(0).getGameId();

                    cheapSharkApi.getGameDetails(gameId).enqueue(new Callback<CheapSharkGameDetailResponse>() {
                        @Override
                        public void onResponse(Call<CheapSharkGameDetailResponse> call, Response<CheapSharkGameDetailResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getDeals() != null) {

                                double lowestPrice = Double.MAX_VALUE;
                                String bestStoreName = "";

                                for (CheapSharkDealInfo deal : response.body().getDeals()) {
                                    String storeName = getStoreName(deal.getStoreId());
                                    if (storeName != null) {
                                        try {
                                            double currentPrice = Double.parseDouble(deal.getPrice());
                                            if (currentPrice < lowestPrice) {
                                                lowestPrice = currentPrice;
                                                bestStoreName = storeName;
                                            }
                                        } catch (NumberFormatException ignored) {}
                                    }
                                }

                                if (lowestPrice != Double.MAX_VALUE) {
                                    final double finalPrice = lowestPrice;
                                    final String finalStoreName = bestStoreName;
                                    String priceText = getString(R.string.lowest_price_format, String.valueOf(finalPrice), finalStoreName);

                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> adapter.setGamePrice(game.getId(), priceText));
                                    }

                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        WishlistPriceEntity newEntity = new WishlistPriceEntity(game.getId(), finalPrice, finalStoreName);
                                        db.wishlistPriceDao().insertOrUpdatePrice(newEntity);
                                    });

                                } else {
                                    setNotFound(game.getId());
                                }
                            } else {
                                setNotFound(game.getId());
                            }
                        }

                        @Override
                        public void onFailure(Call<CheapSharkGameDetailResponse> call, Throwable t) {
                            setNotFound(game.getId());
                        }
                    });
                } else {
                    setNotFound(game.getId());
                }
            }

            @Override
            public void onFailure(Call<List<CheapSharkGameSearchResult>> call, Throwable t) {
                setNotFound(game.getId());
            }
        });
    }

    private void setNotFound(int gameId) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> adapter.setGamePrice(gameId, getString(R.string.price_not_found)));
        }
    }
}