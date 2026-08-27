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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.adapters.FeedAdapter;
import com.nagy_mark.mygamevault.models.FeedModel;
import com.nagy_mark.mygamevault.models.FollowRelationship;
import com.nagy_mark.mygamevault.network.SupabaseApi;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedFragment extends Fragment {

    private RecyclerView rvFeed;
    private TextView tvEmptyFeed;
    private SwipeRefreshLayout swipeRefreshFeed;

    private FeedAdapter feedAdapter;
    private SupabaseApi api;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFeed = view.findViewById(R.id.rvFeed);
        tvEmptyFeed = view.findViewById(R.id.tvEmptyFeed);
        swipeRefreshFeed = view.findViewById(R.id.swipeRefreshFeed);

        MaterialButton btnFindUsersFeed = view.findViewById(R.id.btnFindUsersFeed);
        if (btnFindUsersFeed != null) {
            btnFindUsersFeed.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.action_feedFragment_to_usersFragment);
            });
        }

        rvFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        feedAdapter = new FeedAdapter();
        rvFeed.setAdapter(feedAdapter);

        api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);

        if (swipeRefreshFeed != null) {
            swipeRefreshFeed.setOnRefreshListener(this::loadFeedData);
        }

        loadFeedData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFeedData();
    }

    private void loadFeedData() {
        if (swipeRefreshFeed != null) {
            swipeRefreshFeed.setRefreshing(true);
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("USER_ID", null);

        if (currentUserId == null) {
            if (swipeRefreshFeed != null) swipeRefreshFeed.setRefreshing(false);
            updateFeedUI(new ArrayList<>());
            return;
        }

        api.getMyFollowing("eq." + currentUserId).enqueue(new Callback<List<FollowRelationship>>() {
            @Override
            public void onResponse(@NonNull Call<List<FollowRelationship>> call, @NonNull Response<List<FollowRelationship>> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<FollowRelationship> follows = response.body();

                        List<String> followedIds = new ArrayList<>();
                        for (FollowRelationship f : follows) {
                            followedIds.add(f.getFollowingId());
                        }

                        if (followedIds.isEmpty()) {
                            if (swipeRefreshFeed != null) swipeRefreshFeed.setRefreshing(false);
                            updateFeedUI(new ArrayList<>());
                            return;
                        }

                        String userFilter = "in.(" + String.join(",", followedIds) + ")";

                        fetchFilteredFeed(userFilter);

                    } else {
                        if (swipeRefreshFeed != null) swipeRefreshFeed.setRefreshing(false);
                        Toast.makeText(getContext(), getString(R.string.error_data_load), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FollowRelationship>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    if (swipeRefreshFeed != null) swipeRefreshFeed.setRefreshing(false);
                    Toast.makeText(getContext(), getString(R.string.error_network_base), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchFilteredFeed(String userFilter) {
        String selectQuery = "*,profiles(*)";
        String orderBy = "created_at.desc";

        api.getFeedActivities(userFilter, selectQuery, orderBy).enqueue(new Callback<List<FeedModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<FeedModel>> call, @NonNull Response<List<FeedModel>> response) {
                if (isAdded()) {
                    if (swipeRefreshFeed != null) {
                        swipeRefreshFeed.setRefreshing(false);
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        List<FeedModel> feedList = response.body();
                        updateFeedUI(feedList);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.error_data_load), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FeedModel>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    if (swipeRefreshFeed != null) {
                        swipeRefreshFeed.setRefreshing(false);
                    }
                    Toast.makeText(getContext(), getString(R.string.error_network_base), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateFeedUI(List<FeedModel> feedList) {
        if (feedList == null || feedList.isEmpty()) {
            rvFeed.setVisibility(View.GONE);
            tvEmptyFeed.setVisibility(View.VISIBLE);
        } else {
            rvFeed.setVisibility(View.VISIBLE);
            tvEmptyFeed.setVisibility(View.GONE);
            feedAdapter.setFeedData(feedList);
        }
    }
}