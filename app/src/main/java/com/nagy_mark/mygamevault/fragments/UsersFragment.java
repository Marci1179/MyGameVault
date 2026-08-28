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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.adapters.UsersAdapter;
import com.nagy_mark.mygamevault.models.FollowRelationship;
import com.nagy_mark.mygamevault.models.ProfileModel;
import com.nagy_mark.mygamevault.network.SupabaseApi;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersFragment extends Fragment {

    private RecyclerView rvUsers;
    private ProgressBar pbUsers;
    private TextInputEditText etSearchUsers;
    private MaterialButtonToggleGroup btgUsers;

    private UsersAdapter usersAdapter;
    private SupabaseApi api;

    private final List<ProfileModel> displayedUsers = new ArrayList<>();
    private final Set<String> followingIds = new HashSet<>();

    private String currentSearchText = "";
    private boolean isShowingFollowing = false;

    private int currentOffset = 0;
    private static final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvUsers = view.findViewById(R.id.rvUsers);
        pbUsers = view.findViewById(R.id.pbUsers);
        etSearchUsers = view.findViewById(R.id.etSearchUsers);
        btgUsers = view.findViewById(R.id.btgUsers);

        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);

        setupRecyclerView();
        setupSearchAndToggle();

        fetchFollowingIdsAndLoadUsers();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchFollowingIdsAndLoadUsers();
    }

    private String getCurrentUserId() {
        Context context = getContext();
        if (context == null) return null;
        SharedPreferences prefs = context.getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
        return prefs.getString("USER_ID", null);
    }

    private void setupRecyclerView() {
        usersAdapter = new UsersAdapter((user, isCurrentlyFollowing, position) -> {
            String userId = getCurrentUserId();
            if (userId == null) return;

            if (isCurrentlyFollowing) {
                api.unfollowUser("eq." + userId, "eq." + user.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (isAdded()) {
                            if (response.isSuccessful()) {
                                followingIds.remove(user.getId().toLowerCase());
                                usersAdapter.notifyItemChanged(position);
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), getString(R.string.error_network_base), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                FollowRelationship follow = new FollowRelationship(userId, user.getId());
                api.followUser(follow).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (isAdded()) {
                            if (response.isSuccessful()) {
                                followingIds.add(user.getId().toLowerCase());
                                usersAdapter.notifyItemChanged(position);
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), getString(R.string.error_network_base), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }, followingIds);

        rvUsers.setAdapter(usersAdapter);

        rvUsers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                        if (!isLoading && !isLastPage && !isShowingFollowing && currentSearchText.isEmpty()) {
                            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                loadMoreUsersPaginated();
                            }
                        }
                    }
                }
            }
        });
    }

    private void setupSearchAndToggle() {
        btgUsers.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isShowingFollowing = (checkedId == R.id.btnFollowingUsers);
                resetAndLoadData();
            }
        });

        etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    currentSearchText = "";
                    resetAndLoadData();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearchUsers.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentSearchText = etSearchUsers.getText().toString().trim();

                if (getActivity() != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }

                if (!currentSearchText.isEmpty()) {
                    performServerSearch();
                }
                return true;
            }
            return false;
        });
    }

    private void fetchFollowingIdsAndLoadUsers() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        api.getMyFollowing("eq." + userId).enqueue(new Callback<List<FollowRelationship>>() {
            @Override
            public void onResponse(@NonNull Call<List<FollowRelationship>> call, @NonNull Response<List<FollowRelationship>> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null) {
                        followingIds.clear();
                        for (FollowRelationship f : response.body()) {
                            if (f.getFollowingId() != null) {
                                followingIds.add(f.getFollowingId().toLowerCase());
                            }
                        }
                    }
                    resetAndLoadData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FollowRelationship>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    resetAndLoadData();
                }
            }
        });
    }

    private void resetAndLoadData() {
        currentOffset = 0;
        isLastPage = false;
        displayedUsers.clear();
        usersAdapter.updateData(displayedUsers, followingIds);

        if (!currentSearchText.isEmpty()) {
            performServerSearch();
        } else if (isShowingFollowing) {
            loadFollowingProfiles();
        } else {
            loadMoreUsersPaginated();
        }
    }

    private void loadMoreUsersPaginated() {
        if (isLoading || isLastPage) return;
        isLoading = true;
        pbUsers.setVisibility(View.VISIBLE);

        int rangeEnd = currentOffset + PAGE_SIZE - 1;
        String rangeHeader = currentOffset + "-" + rangeEnd;

        api.getProfilesPaginated(rangeHeader).enqueue(new Callback<List<ProfileModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProfileModel>> call, @NonNull Response<List<ProfileModel>> response) {
                if (isAdded()) {
                    isLoading = false;
                    pbUsers.setVisibility(View.GONE);
                    rvUsers.setVisibility(View.VISIBLE);

                    if (response.isSuccessful() && response.body() != null) {
                        List<ProfileModel> newUsers = filterOutCurrentUser(response.body());
                        displayedUsers.addAll(newUsers);
                        usersAdapter.updateData(displayedUsers, followingIds);

                        currentOffset += PAGE_SIZE;
                        if (response.body().size() < PAGE_SIZE) {
                            isLastPage = true;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProfileModel>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    isLoading = false;
                    pbUsers.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadFollowingProfiles() {
        if (followingIds.isEmpty()) {
            displayedUsers.clear();
            usersAdapter.updateData(displayedUsers, followingIds);
            return;
        }

        pbUsers.setVisibility(View.VISIBLE);
        rvUsers.setVisibility(View.INVISIBLE);

        String idInQuery = "in.(" + TextUtils.join(",", followingIds) + ")";

        api.getProfilesByIds(idInQuery).enqueue(new Callback<List<ProfileModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProfileModel>> call, @NonNull Response<List<ProfileModel>> response) {
                if (isAdded()) {
                    pbUsers.setVisibility(View.GONE);
                    rvUsers.setVisibility(View.VISIBLE);

                    if (response.isSuccessful() && response.body() != null) {
                        displayedUsers.clear();
                        displayedUsers.addAll(response.body());
                        usersAdapter.updateData(displayedUsers, followingIds);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProfileModel>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    pbUsers.setVisibility(View.GONE);
                }
            }
        });
    }

    private void performServerSearch() {
        pbUsers.setVisibility(View.VISIBLE);
        rvUsers.setVisibility(View.INVISIBLE);

        displayedUsers.clear();
        usersAdapter.updateData(displayedUsers, followingIds);

        String queryParam = "ilike.*" + currentSearchText + "*";

        api.searchProfiles(queryParam).enqueue(new Callback<List<ProfileModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProfileModel>> call, @NonNull Response<List<ProfileModel>> response) {
                if (isAdded()) {
                    pbUsers.setVisibility(View.GONE);
                    rvUsers.setVisibility(View.VISIBLE);

                    if (response.isSuccessful() && response.body() != null) {
                        List<ProfileModel> results = filterOutCurrentUser(response.body());

                        if (isShowingFollowing) {
                            List<ProfileModel> followingResults = new ArrayList<>();
                            for (ProfileModel p : results) {
                                if (followingIds.contains(p.getId().toLowerCase())) {
                                    followingResults.add(p);
                                }
                            }
                            results = followingResults;
                        }

                        displayedUsers.addAll(results);
                        usersAdapter.updateData(displayedUsers, followingIds);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProfileModel>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    pbUsers.setVisibility(View.GONE);
                }
            }
        });
    }

    private List<ProfileModel> filterOutCurrentUser(List<ProfileModel> users) {
        String myId = getCurrentUserId();
        List<ProfileModel> filtered = new ArrayList<>();
        for (ProfileModel user : users) {
            if (myId == null || !user.getId().equals(myId)) {
                filtered.add(user);
            }
        }
        return filtered;
    }
}