package com.nagy_mark.mygamevault.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.models.FeedModel;
import com.nagy_mark.mygamevault.models.ProfileModel;

import java.sql.Struct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private List<FeedModel> feedList = new ArrayList<>();

    public FeedAdapter() {

    }

    public void setFeedData(List<FeedModel> newFeedList) {
        this.feedList = newFeedList != null ? newFeedList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedModel feed = feedList.get(position);
        Context context = holder.itemView.getContext();

        ProfileModel profile = feed.getProfile();
        if (profile != null) {
            holder.tvUsernameItemFeed.setText(profile.getUsername());

            if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
                Glide.with(context)
                        .load(profile.getAvatarUrl())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(holder.ivAvatarItemFeed);
            } else {
                holder.ivAvatarItemFeed.setImageResource(R.drawable.ic_person);
            }
        } else {
            holder.tvUsernameItemFeed.setText(context.getString(R.string.unknown_user));
            holder.ivAvatarItemFeed.setImageResource(R.drawable.ic_person);
        }

        String actionText = "";
        String game = feed.getGameName() != null ? feed.getGameName() : context.getString(R.string.unknown_game_feed);
        String actionType = feed.getActionType() != null ? feed.getActionType() : "";

        switch (actionType) {
            case "ADDED_TO_LIBRARY":
                actionText = context.getString(R.string.feed_action_added_library, game);
                break;
            case "ADDED_TO_WISHLIST":
                actionText = context.getString(R.string.feed_action_added_wishlist, game);
                break;
            case "STATUS_IN_PROGRESS":
                actionText = context.getString(R.string.feed_action_in_progress, game);
                break;
            case "STATUS_COMPLETED":
                actionText = context.getString(R.string.feed_action_completed, game);
                break;
            case "REVIEWED_GAME":
                actionText = context.getString(R.string.feed_action_reviewed, game);
                break;
            case "ADDED_TO_FAVORITES":
                actionText = context.getString(R.string.feed_action_added_favorites, game);
                break;
            default:
                actionText = context.getString(R.string.feed_action_updated, game);
                break;
        }
        holder.tvActionItemFeed.setText(actionText);

        if (feed.hasReviewOrNote()) {
            holder.llReviewContainerItemFeed.setVisibility(View.VISIBLE);

            if (feed.getRating() != null && feed.getRating() > 0) {
                holder.tvRatingItemFeed.setVisibility(View.VISIBLE);
                String ratingStr = (feed.getRating() % 1 == 0)
                        ? String.format(Locale.getDefault(), "%.0f / 5", feed.getRating())
                        : String.format(Locale.getDefault(), "%.1f / 5", feed.getRating());
                holder.tvRatingItemFeed.setText(ratingStr);
            } else {
                holder.tvRatingItemFeed.setVisibility(View.GONE);
            }

            if (feed.getReviewText() != null && !feed.getReviewText().trim().isEmpty()) {
                holder.tvNoteItemFeed.setVisibility(View.VISIBLE);
                holder.tvNoteItemFeed.setText(feed.getReviewText());
            } else {
                holder.tvNoteItemFeed.setVisibility(View.GONE);
            }
        } else {
            holder.llReviewContainerItemFeed.setVisibility(View.GONE);
        }

        holder.tvTimeItemFeed.setText(formatDate(feed.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    private String formatDate(String isoDateString) {
        if (isoDateString == null) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(isoDateString);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy. MM. dd. HH:mm", Locale.getDefault());
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return isoDateString;
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatarItemFeed;
        TextView tvUsernameItemFeed, tvTimeItemFeed, tvActionItemFeed;
        LinearLayout llReviewContainerItemFeed;
        TextView tvRatingItemFeed, tvNoteItemFeed;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatarItemFeed = itemView.findViewById(R.id.ivAvatarItemFeed);
            tvUsernameItemFeed = itemView.findViewById(R.id.tvUsernameItemFeed);
            tvTimeItemFeed = itemView.findViewById(R.id.tvTimeItemFeed);
            tvActionItemFeed = itemView.findViewById(R.id.tvActionItemFeed);
            llReviewContainerItemFeed = itemView.findViewById(R.id.llReviewContainerItemFeed);
            tvRatingItemFeed = itemView.findViewById(R.id.tvRatingItemFeed);
            tvNoteItemFeed = itemView.findViewById(R.id.tvNoteItemFeed);
        }
    }
}
