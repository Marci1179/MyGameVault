package com.nagy_mark.mygamevault.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.nagy_mark.mygamevault.R;

public class FeedFragment extends Fragment {

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

        MaterialButton btnFindUsersFeed = view.findViewById(R.id.btnFindUsersFeed);
        if (btnFindUsersFeed != null) {
            btnFindUsersFeed.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.action_feedFragment_to_usersFragment);
            });
        }
    }
}