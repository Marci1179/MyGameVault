package com.nagy_mark.mygamevault.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.models.CheapSharkDealInfo;
import com.nagy_mark.mygamevault.models.CheapSharkGameDetailResponse;
import com.nagy_mark.mygamevault.models.CheapSharkGameSearchResult;
import com.nagy_mark.mygamevault.models.SavedGameModel;
import com.nagy_mark.mygamevault.network.CheapSharkApi;
import com.nagy_mark.mygamevault.network.CheapSharkApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistDetailViewFragment extends Fragment {

    private ProgressBar pbWishlistDetail;
    private ScrollView svWishlistDetail;
    private ImageView ivGameCoverWishlistDetail;
    private TextView tvGameTitleWishlistDetail, tvGameYearWishlistDetail, tvGamePublisherWishlistDetail;
    private LinearLayout llPricesContainerWishlistDetail;

    private SavedGameModel currentGame;

    public WishlistDetailViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wishlist_detail_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pbWishlistDetail = view.findViewById(R.id.pbWishlistDetail);
        svWishlistDetail = view.findViewById(R.id.svWishlistDetail);
        ivGameCoverWishlistDetail = view.findViewById(R.id.ivGameCoverWishlistDetail);
        tvGameTitleWishlistDetail = view.findViewById(R.id.tvGameTitleWishlistDetail);
        tvGameYearWishlistDetail = view.findViewById(R.id.tvGameYearWishlistDetail);
        tvGamePublisherWishlistDetail = view.findViewById(R.id.tvGamePublisherWishlistDetail);
        llPricesContainerWishlistDetail = view.findViewById(R.id.llPricesContainerWishlistDetail);

        if (getArguments() != null) {
            currentGame = (SavedGameModel) getArguments().getSerializable("gameData");
            if (currentGame != null) {
                populateUI();
            }
        }

        fetchAllPricesFromApi();
    }

    private void populateUI() {
        String gameName = currentGame.getGameName() != null ? currentGame.getGameName() : getString(R.string.unknown_game);
        tvGameTitleWishlistDetail.setText(gameName);

        String rawDate = currentGame.getReleaseYear();
        String yearOnly = getString(R.string.unknown_year);
        if (rawDate != null && rawDate.length() >= 4) {
            yearOnly = rawDate.substring(0, 4);
        }
        tvGameYearWishlistDetail.setText(getString(R.string.format_release_year, yearOnly));

        String publisher = (currentGame.getPublisher() != null && !currentGame.getPublisher().isEmpty())
                ? currentGame.getPublisher()
                : getString(R.string.unknown_publisher);
        tvGamePublisherWishlistDetail.setText(getString(R.string.format_publisher, publisher));

        if (currentGame.getCover() != null && !currentGame.getCover().isEmpty()) {
            String imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/" + currentGame.getCover() + ".jpg";
            Glide.with(this).load(imageUrl).into(ivGameCoverWishlistDetail);
        }
    }

    private void fetchAllPricesFromApi() {
        if (currentGame == null || currentGame.getGameName() == null || currentGame.getGameName().isEmpty()) {
            if (isAdded()) {
                Toast.makeText(requireContext(), getString(R.string.error_loading_prices), Toast.LENGTH_SHORT).show();
                showPriceMessage(getString(R.string.error_loading_prices));
            }
            return;
        }

        pbWishlistDetail.setVisibility(View.VISIBLE);
        svWishlistDetail.setVisibility(View.INVISIBLE);

        CheapSharkApi api = CheapSharkApiClient.getClient().create(CheapSharkApi.class);

        api.searchGame(currentGame.getGameName(), 1).enqueue(new Callback<List<CheapSharkGameSearchResult>>() {
            @Override
            public void onResponse(@NonNull Call<List<CheapSharkGameSearchResult>> call, @NonNull Response<List<CheapSharkGameSearchResult>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String cheapSharkId = response.body().get(0).getGameId();

                    api.getGameDetails(cheapSharkId).enqueue(new Callback<CheapSharkGameDetailResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<CheapSharkGameDetailResponse> call, @NonNull Response<CheapSharkGameDetailResponse> response) {
                            if (isAdded() && response.isSuccessful() && response.body() != null && response.body().getDeals() != null) {
                                populatePricesList(response.body().getDeals());
                            } else {
                                showPriceMessage(getString(R.string.price_not_found));
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<CheapSharkGameDetailResponse> call, @NonNull Throwable t) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), getString(R.string.error_loading_prices), Toast.LENGTH_SHORT).show();
                                showPriceMessage(getString(R.string.error_loading_prices));
                            }
                        }
                    });
                } else {
                    showPriceMessage(getString(R.string.price_not_found));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CheapSharkGameSearchResult>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), getString(R.string.error_loading_prices), Toast.LENGTH_SHORT).show();
                    showPriceMessage(getString(R.string.error_loading_prices));
                }
            }
        });
    }

    private void populatePricesList(List<CheapSharkDealInfo> deals) {
        if (!isAdded()) return;

        llPricesContainerWishlistDetail.removeAllViews();
        boolean hasPrices = false;

        for (CheapSharkDealInfo deal : deals) {
            String storeName = getStoreName(deal.getStoreId());
            if (storeName != null) {
                TextView tvStore = new TextView(requireContext());
                tvStore.setText(storeName + ": $" + deal.getPrice());
                tvStore.setTextSize(16f);
                tvStore.setPadding(0, 4, 0, 4);

                llPricesContainerWishlistDetail.addView(tvStore);
                hasPrices = true;
            }
        }

        if (!hasPrices) {
            showPriceMessage(getString(R.string.price_not_found));
        } else {
            if (isAdded()) {
                pbWishlistDetail.setVisibility(View.GONE);
                svWishlistDetail.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showPriceMessage(String message) {
        if (!isAdded()) return;

        llPricesContainerWishlistDetail.removeAllViews();
        TextView tvMessage = new TextView(requireContext());
        tvMessage.setText(message);
        tvMessage.setTextSize(16f);
        tvMessage.setPadding(0, 4, 0, 4);
        llPricesContainerWishlistDetail.addView(tvMessage);

        pbWishlistDetail.setVisibility(View.GONE);
        svWishlistDetail.setVisibility(View.VISIBLE);
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
}