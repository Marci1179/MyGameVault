package com.nagy_mark.mygamevault.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.models.Game;
import com.nagy_mark.mygamevault.models.SavedGameModel;
import com.nagy_mark.mygamevault.network.IgdbApi;
import com.nagy_mark.mygamevault.network.IgdbApiClient;
import com.nagy_mark.mygamevault.network.SupabaseApi;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryDetailViewFragment extends Fragment {

    private ImageView ivGameCoverLibraryDetail;
    private TextView tvGameTitleLibraryDetail, tvGameYearLibraryDetail, tvGamePublisherLibraryDetail, tvGameDeveloperLibraryDetail, tvGameSummaryLibraryDetail;
    private ChipGroup cgGenresLibraryDetail, cgPlatformsLibraryDetail, cgGameModesLibraryDetail;
    private AutoCompleteTextView actvGameStatusLibraryDetail;
    private RatingBar rbGameRatingLibraryDetail;
    private TextInputEditText etGameNoteLibraryDetail;
    private MaterialButton btnSaveGameLibraryDetail;
    private ScrollView svLibraryDetail;
    private android.widget.ProgressBar pbLibraryDetail;

    private SavedGameModel currentGame;
    private SupabaseApi api;

    private final int[] statusIds = {1, 2, 3};
    private String[] statusOptions;

    public LibraryDetailViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library_detail_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivGameCoverLibraryDetail = view.findViewById(R.id.ivGameCoverLibraryDetail);
        tvGameTitleLibraryDetail = view.findViewById(R.id.tvGameTitleLibraryDetail);
        tvGameYearLibraryDetail = view.findViewById(R.id.tvGameYearLibraryDetail);
        tvGamePublisherLibraryDetail = view.findViewById(R.id.tvGamePublisherLibraryDetail);
        actvGameStatusLibraryDetail = view.findViewById(R.id.actvGameStatusLibraryDetail);
        rbGameRatingLibraryDetail = view.findViewById(R.id.rbGameRatingLibraryDetail);
        etGameNoteLibraryDetail = view.findViewById(R.id.etGameNoteLibraryDetail);
        btnSaveGameLibraryDetail = view.findViewById(R.id.btnSaveGameLibraryDetail);
        tvGameDeveloperLibraryDetail = view.findViewById(R.id.tvGameDeveloperLibraryDetail);
        tvGameSummaryLibraryDetail = view.findViewById(R.id.tvGameSummaryLibraryDetail);
        cgGenresLibraryDetail = view.findViewById(R.id.cgGenresLibraryDetail);
        cgPlatformsLibraryDetail = view.findViewById(R.id.cgPlatformsLibraryDetail);
        cgGameModesLibraryDetail = view.findViewById(R.id.cgGameModesLibraryDetail);
        svLibraryDetail = view.findViewById(R.id.svLibraryDetail);
        pbLibraryDetail = view.findViewById(R.id.pbLibraryDetail);

        api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);

        statusOptions = new String[] {
                getString(R.string.status_owned),
                getString(R.string.status_in_progress),
                getString(R.string.status_finished)
        };

        if (getArguments() != null) {
            currentGame = (SavedGameModel) getArguments().getSerializable("gameData");
            if (currentGame != null) {
                populateUI();
            }
        }

        setupDropDown();
        fetchExtraDetailsFromIgdb();

        btnSaveGameLibraryDetail.setOnClickListener(v -> saveChangesToDatabase());
    }

    private void populateUI() {
        tvGameTitleLibraryDetail.setText(currentGame.getGameName() != null ? currentGame.getGameName() : getString(R.string.unknown_game));

        String rawDate = currentGame.getReleaseYear();
        String yearOnly = "-";
        if (rawDate != null && rawDate.length() >= 4) {
            yearOnly = rawDate.substring(0, 4);
        }
        tvGameYearLibraryDetail.setText(getString(R.string.format_release_year, yearOnly));

        String publisher = currentGame.getPublisher() != null ? currentGame.getPublisher() : getString(R.string.unknown_publisher);
        tvGamePublisherLibraryDetail.setText(getString(R.string.format_publisher, publisher));

        if (currentGame.getCover() != null && !currentGame.getCover().isEmpty()) {
            String imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/" + currentGame.getCover() + ".jpg";
            Glide.with(this).load(imageUrl).into(ivGameCoverLibraryDetail);
        }

        int currentStatusId = currentGame.getStatusId();
        for (int i = 0; i < statusIds.length; i++) {
            if (statusIds[i] == currentStatusId) {
                actvGameStatusLibraryDetail.setText(statusOptions[i], false);
                break;
            }
        }

        if (currentGame.getRating() != null) {
            rbGameRatingLibraryDetail.setRating(currentGame.getRating());
        } else {
            rbGameRatingLibraryDetail.setRating(0f);
        }

        if (currentGame.getNote() != null) {
            etGameNoteLibraryDetail.setText(currentGame.getNote());
        }
    }

    private void setupDropDown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statusOptions);
        actvGameStatusLibraryDetail.setAdapter(adapter);
    }

    private void saveChangesToDatabase() {
        btnSaveGameLibraryDetail.setEnabled(false);

        String selectedStatusText = actvGameStatusLibraryDetail.getText().toString();
        int newStatusId = currentGame.getStatusId();

        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equals(selectedStatusText)) {
                newStatusId = statusIds[i];
                break;
            }
        }

        float newRating = rbGameRatingLibraryDetail.getRating();
        String newNote = etGameNoteLibraryDetail.getText() != null ? etGameNoteLibraryDetail.getText().toString().trim() : "";

        Map<String, Object> updates = new HashMap<>();
        updates.put("status_id", newStatusId);
        updates.put("rating", newRating);
        updates.put("note", newNote);

        api.updateGameDetails("eq." + currentGame.getId(), updates).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isAdded()) {
                    btnSaveGameLibraryDetail.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.error_save_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    btnSaveGameLibraryDetail.setEnabled(true);
                    Toast.makeText(getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchExtraDetailsFromIgdb() {
        if (currentGame == null || currentGame.getGameName() == null) return;

        String query = "search \"" + currentGame.getGameName() + "\"; fields summary, platforms.name, genres.name, game_modes.name, involved_companies.company.name, involved_companies.developer, involved_companies.publisher; limit 1;";
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), query);

        IgdbApi igdbApi = IgdbApiClient.getClient(requireContext()).create(IgdbApi.class);

        igdbApi.getTopGames(body).enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(@NonNull Call<List<Game>> call, @NonNull Response<List<Game>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    pbLibraryDetail.setVisibility(View.GONE);
                    svLibraryDetail.setVisibility(View.VISIBLE);

                    Game igdbData = response.body().get(0);

                    String developerName = getString(R.string.unknown_developer);
                    if (igdbData.getDeveloperName() != null) {
                        developerName = igdbData.getDeveloperName();
                    }
                    tvGameDeveloperLibraryDetail.setText(getString(R.string.developer_format, developerName));

                    if (igdbData.getSummary() != null && !igdbData.getSummary().isEmpty()) {
                        tvGameSummaryLibraryDetail.setText(igdbData.getSummary());
                    } else {
                        tvGameSummaryLibraryDetail.setText(getString(R.string.no_summary_found));
                    }

                    cgGenresLibraryDetail.removeAllViews();
                    if (igdbData.getGenres() != null) {
                        for (Game.Genre genre : igdbData.getGenres()) {
                            cgGenresLibraryDetail.addView(createUnclickableChip(genre.getName()));
                        }
                    }

                    cgPlatformsLibraryDetail.removeAllViews();
                    if (igdbData.getPlatforms() != null) {
                        for (Game.Platform platform : igdbData.getPlatforms()) {
                            cgPlatformsLibraryDetail.addView(createUnclickableChip(platform.getName()));
                        }
                    }

                    cgGameModesLibraryDetail.removeAllViews();
                    if (igdbData.getGameModes() != null) {
                        for (Game.GameMode mode : igdbData.getGameModes()) {
                            cgGameModesLibraryDetail.addView(createUnclickableChip(mode.getName()));
                        }
                    }

                } else if (isAdded()) {
                    pbLibraryDetail.setVisibility(View.GONE);
                    svLibraryDetail.setVisibility(View.VISIBLE);

                    tvGameSummaryLibraryDetail.setText(getString(R.string.error_igdb_extra_data));
                    tvGameDeveloperLibraryDetail.setText(getString(R.string.developer_unknown));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Game>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    pbLibraryDetail.setVisibility(View.GONE);
                    svLibraryDetail.setVisibility(View.VISIBLE);

                    tvGameSummaryLibraryDetail.setText(getString(R.string.error_no_internet_extra_data));
                    tvGameDeveloperLibraryDetail.setText(getString(R.string.developer_unknown));
                }
            }
        });
    }

    private Chip createUnclickableChip(String text) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setClickable(false);
        chip.setCheckable(false);

        int GreenColor = ContextCompat.getColor(requireContext(), R.color.green);
        int LightGreenColor = ContextCompat.getColor(requireContext(), R.color.light_green);

        chip.setChipBackgroundColor(ColorStateList.valueOf(LightGreenColor));
        chip.setTextColor(ColorStateList.valueOf(GreenColor));
        chip.setChipStrokeWidth(3f);
        chip.setChipStrokeColor(ColorStateList.valueOf(GreenColor));
        chip.setTextAppearanceResource(com.google.android.material.R.style.TextAppearance_MaterialComponents_Chip);

        return chip;
    }
}