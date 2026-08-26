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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

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

    private UsersAdapter usersAdapter;

    private List<ProfileModel> allUsers = new ArrayList<>();
    private List<ProfileModel> filteredUsers = new ArrayList<>();
    private final Set<String> followingIds = new HashSet<>();

    private String currentSearchText = "";

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvUsers = view.findViewById(R.id.rvUsers);
        pbUsers = view.findViewById(R.id.pbUsers);
        etSearchUsers = view.findViewById(R.id.etSearchUsers);

        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        setupRecyclerView();
        setupSearch();

        loadUsersAndFollows();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsersAndFollows();
    }

    private String getCurrentUserId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
        return prefs.getString("USER_ID", null);
    }

    private void setupRecyclerView() {
        usersAdapter = new UsersAdapter((user, isCurrentlyFollowing, position) -> {
            String userId = getCurrentUserId();
            if (userId == null) return;

            SupabaseApi api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);

            if (isCurrentlyFollowing) {
                api.unfollowUser("eq." + userId, "eq." + user.getId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            followingIds.remove(user.getId().toLowerCase());
                            usersAdapter.notifyItemChanged(position);
                        } else {
                            Toast.makeText(getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), getString(R.string.error_network_base), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                FollowRelationship follow = new FollowRelationship(userId, user.getId());
                api.followUser(follow).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            followingIds.add(user.getId().toLowerCase());
                            usersAdapter.notifyItemChanged(position);
                        } else {
                            Toast.makeText(getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), getString(R.string.error_network_base), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, followingIds);

        rvUsers.setAdapter(usersAdapter);
    }

    private void setupSearch() {
        etSearchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString().toLowerCase().trim();
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearchUsers.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void loadUsersAndFollows() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        pbUsers.setVisibility(View.VISIBLE);
        rvUsers.setVisibility(View.INVISIBLE);

        SupabaseApi api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);

        api.getMyFollowing("eq." + userId).enqueue(new Callback<List<FollowRelationship>>() {
            @Override
            public void onResponse(@NonNull Call<List<FollowRelationship>> call, @NonNull Response<List<FollowRelationship>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    followingIds.clear();
                    for (FollowRelationship f : response.body()) {
                        if (f.getFollowingId() != null) {
                            followingIds.add(f.getFollowingId().toLowerCase());
                        }
                    }
                }
                fetchAllProfiles();
            }

            @Override
            public void onFailure(@NonNull Call<List<FollowRelationship>> call, @NonNull Throwable t) {
                fetchAllProfiles();
            }
        });
    }

    private void fetchAllProfiles() {
        SupabaseApi api = SupabaseApiClient.getClient(requireContext()).create(SupabaseApi.class);

        api.getAllProfiles().enqueue(new Callback<List<ProfileModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProfileModel>> call, @NonNull Response<List<ProfileModel>> response) {
                if (isAdded()) {
                    pbUsers.setVisibility(View.GONE);
                    rvUsers.setVisibility(View.VISIBLE);

                    if (response.isSuccessful() && response.body() != null) {
                        allUsers = response.body();
                        applyFilter();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.error_data_load), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProfileModel>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    pbUsers.setVisibility(View.GONE);
                    rvUsers.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), getString(R.string.error_network_base), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void applyFilter() {
        filteredUsers.clear();
        String userId = getCurrentUserId();

        for (ProfileModel user : allUsers) {
            if (userId != null && user.getId().equals(userId)) {
                continue;
            }

            if (currentSearchText.isEmpty()) {
                filteredUsers.add(user);
            } else {
                if (user.getUsername() != null && user.getUsername().toLowerCase().contains(currentSearchText)) {
                    filteredUsers.add(user);
                }
            }
        }

        if (usersAdapter != null) {
            usersAdapter.updateData(filteredUsers, followingIds);
        }
    }
}