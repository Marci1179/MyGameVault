package com.nagy_mark.mygamevault.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.models.FeedModel;
import com.nagy_mark.mygamevault.models.FollowRelationship;
import com.nagy_mark.mygamevault.models.SavedGameModel;
import com.nagy_mark.mygamevault.network.SupabaseApi;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsFragment extends Fragment {

    private TextView tvOwnedStatistics, tvInProgressStatistics, tvFinishedStatistics, tvWishlistStatistics, tvReviewsStatistics, tvAvgRatingStatistics, tvSocialStats, tvCompletionRateValueStatistics;
    private ProgressBar pbCompletionRateStatistics;
    private BarChart barChartStatistics;

    private SupabaseApi api;
    private String currentUserId;

    private int followersCount = 0;
    private int followingCount = 0;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvOwnedStatistics = view.findViewById(R.id.tvOwnedStatistics);
        tvInProgressStatistics = view.findViewById(R.id.tvInProgressStatistics);
        tvFinishedStatistics = view.findViewById(R.id.tvFinishedStatistics);
        tvWishlistStatistics = view.findViewById(R.id.tvWishlistStatistics);
        tvReviewsStatistics = view.findViewById(R.id.tvReviewsStatistics);
        tvAvgRatingStatistics = view.findViewById(R.id.tvAvgRatingStatistics);
        tvSocialStats = view.findViewById(R.id.tvSocialStats);
        tvCompletionRateValueStatistics = view.findViewById(R.id.tvCompletionRateValueStatistics);
        pbCompletionRateStatistics = view.findViewById(R.id.pbCompletionRateStatistics);
        barChartStatistics = view.findViewById(R.id.barChartStatistics);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("USER_ID", null);

        api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);

        setupChart();

        if (currentUserId != null) {
            loadGameLibraryStats();
            loadFeedStats();
            loadSocialStats();
        }
    }

    private void setupChart() {
        barChartStatistics.getDescription().setEnabled(false);
        barChartStatistics.getLegend().setEnabled(false);
        barChartStatistics.setDrawGridBackground(false);
        barChartStatistics.setDrawBorders(false);
        barChartStatistics.setTouchEnabled(false);

        int textColor = isDarkMode() ? Color.WHITE : Color.DKGRAY;

        XAxis xAxis = barChartStatistics.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextColor(textColor);

        YAxis leftAxis = barChartStatistics.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextColor(textColor);

        barChartStatistics.getAxisRight().setEnabled(false);
    }

    private void loadGameLibraryStats() {
        api.getUserSavedGames("eq." + currentUserId, "*").enqueue(new Callback<List<SavedGameModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<SavedGameModel>> call, @NonNull Response<List<SavedGameModel>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    int owned = 0;
                    int inProgress = 0;
                    int finished = 0;
                    int wishlist = 0;
                    float totalRating = 0;
                    int ratedGamesCount = 0;

                    for (SavedGameModel game : response.body()) {
                        int status = game.getStatusId();

                        if (status == 4) {
                            wishlist++;
                        } else {
                            owned++;
                            if (status == 2) {
                                inProgress++;
                            }
                            if (status == 3) {
                                finished++;
                            }
                        }

                        if (game.getRating() != null && game.getRating() > 0) {
                            totalRating += game.getRating();
                            ratedGamesCount++;
                        }
                    }

                    int completionRate = 0;
                    if (owned > 0) {
                        completionRate = (int) (((float) finished / owned) * 100);
                    }

                    float avgRating = 0;
                    if (ratedGamesCount > 0) {
                        avgRating = totalRating / ratedGamesCount;
                    }

                    tvOwnedStatistics.setText(String.valueOf(owned));
                    tvInProgressStatistics.setText(String.valueOf(inProgress));
                    tvFinishedStatistics.setText(String.valueOf(finished));
                    tvWishlistStatistics.setText(String.valueOf(wishlist));
                    tvAvgRatingStatistics.setText(String.format(Locale.getDefault(), "%.1f", avgRating));
                    tvCompletionRateValueStatistics.setText(completionRate + "%");
                    pbCompletionRateStatistics.setProgress(completionRate);
                } else if (isAdded()) {
                    Log.e("API_HIBA", "Library Stats Hiba: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SavedGameModel>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e("API_HIBA", "Library Stats Hálózati hiba: " + t.getMessage());
                }
            }
        });
    }

    private void loadFeedStats() {
        api.getFeedActivities("eq." + currentUserId, "*", "created_at.asc").enqueue(new Callback<List<FeedModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<FeedModel>> call, @NonNull Response<List<FeedModel>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    int reviewsCount = 0;
                    int[] last12MonthsCompletions = new int[12];

                    Calendar cal = Calendar.getInstance();
                    int currentYear = cal.get(Calendar.YEAR);
                    int currentMonth = cal.get(Calendar.MONTH);

                    for (FeedModel activity : response.body()) {
                        String type = activity.getActionType();
                        String dateStr = activity.getCreatedAt();

                        if ("REVIEWED_GAME".equals(type) || "REVIEWED".equals(type)) {
                            reviewsCount++;
                        }

                        if ("STATUS_COMPLETED".equals(type) || "FINISHED".equals(type)) {
                            if (dateStr != null && dateStr.length() >= 10) {
                                try {
                                    int year = Integer.parseInt(dateStr.substring(0, 4));
                                    int monthIndex = Integer.parseInt(dateStr.substring(5, 7)) - 1;
                                    int monthsAgo = (currentYear - year) * 12 + (currentMonth - monthIndex);

                                    if (monthsAgo >= 0 && monthsAgo < 12) {
                                        int arrayIndex = 11 - monthsAgo;
                                        last12MonthsCompletions[arrayIndex]++;
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }

                    tvReviewsStatistics.setText(String.valueOf(reviewsCount));
                    updateChartData(last12MonthsCompletions);
                } else if (isAdded()) {
                    Log.e("API_HIBA", "Feed Stats Hiba: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FeedModel>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e("API_HIBA", "Feed Stats Hálózati hiba: " + t.getMessage());
                }
            }
        });
    }

    private void loadSocialStats() {
        api.getMyFollowing("eq." + currentUserId).enqueue(new Callback<List<FollowRelationship>>() {
            @Override
            public void onResponse(@NonNull Call<List<FollowRelationship>> call, @NonNull Response<List<FollowRelationship>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    followingCount = response.body().size();
                    updateSocialStatsUI();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<FollowRelationship>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e("API_HIBA", "Following Hiba: " + t.getMessage());
                }
            }
        });

        api.getMyFollowers("eq." + currentUserId).enqueue(new Callback<List<FollowRelationship>>() {
            @Override
            public void onResponse(@NonNull Call<List<FollowRelationship>> call, @NonNull Response<List<FollowRelationship>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    followersCount = response.body().size();
                    updateSocialStatsUI();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<FollowRelationship>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e("API_HIBA", "Followers Hiba: " + t.getMessage());
                }
            }
        });
    }

    private void updateSocialStatsUI() {
        if (isAdded()) {
            tvSocialStats.setText(getString(R.string.social_stats_format, followersCount, followingCount));
        }
    }

    private void updateChartData(int[] monthlyCompletions) {
        if (!isAdded() || barChartStatistics == null) return;

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> activeMonthLabels = new ArrayList<>();

        String[] allLocalizedMonths = getResources().getStringArray(R.array.short_months);
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);

        int validIndex = 0;
        for (int i = 0; i < 12; i++) {
            int m = (currentMonth - 11 + i) % 12;
            if (m < 0) {
                m += 12;
            }

            int completions = monthlyCompletions[i];
            if (completions > 0) {
                entries.add(new BarEntry(validIndex, completions));
                activeMonthLabels.add(allLocalizedMonths[m]);
                validIndex++;
            }
        }

        if (entries.isEmpty()) {
            entries.add(new BarEntry(0, 0f));
            activeMonthLabels.add(allLocalizedMonths[currentMonth]);
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.completed_games_chart_label));
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.green));
        dataSet.setValueTextSize(12f);

        int textColor = isDarkMode() ? Color.WHITE : Color.DKGRAY;
        dataSet.setValueTextColor(textColor);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value > 0 ? String.valueOf((int) value) : "";
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);

        XAxis xAxis = barChartStatistics.getXAxis();
        xAxis.setLabelCount(activeMonthLabels.size(), false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(activeMonthLabels.toArray(new String[0])));

        barChartStatistics.setData(barData);
        barChartStatistics.setFitBars(true);
        barChartStatistics.fitScreen();
        barChartStatistics.invalidate();
        barChartStatistics.animateY(1000);
    }

    private boolean isDarkMode() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }
}