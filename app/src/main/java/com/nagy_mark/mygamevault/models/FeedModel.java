package com.nagy_mark.mygamevault.models;

import com.google.gson.annotations.SerializedName;

public class FeedModel {
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("action_type")
    private String actionType;

    @SerializedName("game_name")
    private String gameName;

    @SerializedName("created_at")
    private String createdAt;

    private Float rating;

    @SerializedName("review_text")
    private String reviewText;

    @SerializedName("profiles")
    private ProfileModel profile;

    public FeedModel() {
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getGameName() {
        return gameName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Float getRating() {
        return rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public ProfileModel getProfile() {
        return profile;
    }

    public boolean hasReviewOrNote() {
        return (rating != null && rating > 0) || (reviewText != null && !reviewText.trim().isEmpty());
    }
}
