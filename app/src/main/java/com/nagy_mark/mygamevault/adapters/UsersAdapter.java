package com.nagy_mark.mygamevault.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.models.ProfileModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    public interface OnFollowClickListener {
        void onFollowClick(ProfileModel user, boolean isCurrentlyFollowing, int position);
    }

    private List<ProfileModel> userList = new ArrayList<>();
    private final Set<String> followingIds;
    private final OnFollowClickListener listener;

    public UsersAdapter(OnFollowClickListener listener, Set<String> followingIds) {
        this.listener = listener;
        this.followingIds = followingIds != null ? followingIds : new HashSet<>();
    }

    public void updateData(List<ProfileModel> newUsers, Set<String> newFollowingIds) {
        this.userList = newUsers != null ? newUsers : new ArrayList<>();

        if (newFollowingIds != null && this.followingIds != newFollowingIds) {
            this.followingIds.clear();
            this.followingIds.addAll(newFollowingIds);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        ProfileModel user = userList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvUsernameUser.setText(user.getUsername());

        String safeUserId = user.getId().toLowerCase();
        boolean isFollowing = followingIds.contains(safeUserId);

        updateHeartIcon(holder.btnFollowHeartUser, isFollowing);

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(holder.ivAvatarUser);
        } else {
            holder.ivAvatarUser.setImageResource(R.drawable.ic_person);
        }

        holder.btnFollowHeartUser.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                boolean currentlyFollowing = followingIds.contains(safeUserId);
                if (listener != null) {
                    listener.onFollowClick(user, currentlyFollowing, currentPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void updateHeartIcon(ImageButton btn, boolean isFollowing) {
        if (isFollowing) {
            btn.setImageResource(R.drawable.ic_heart_filled);
        } else {
            btn.setImageResource(R.drawable.ic_heart_outline);
        }
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatarUser;
        TextView tvUsernameUser;
        ImageButton btnFollowHeartUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatarUser = itemView.findViewById(R.id.ivAvatarUser);
            tvUsernameUser = itemView.findViewById(R.id.tvUsernameUser);
            btnFollowHeartUser = itemView.findViewById(R.id.btnFollowHeartUser);
        }
    }
}