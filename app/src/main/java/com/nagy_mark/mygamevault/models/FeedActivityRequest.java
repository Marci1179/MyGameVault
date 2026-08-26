package com.nagy_mark.mygamevault.models;

import com.google.gson.annotations.SerializedName;

public class FeedActivityRequest {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("action_type")
    private String actionType;

    @SerializedName("game_name")
    private String gameName;

    @SerializedName("rating")
    private Float rating;

    @SerializedName("review_text")
    private String reviewText;

    public FeedActivityRequest(String userId, String actionType, String gameName) {
        this.userId = userId;
        this.actionType = actionType;
        this.gameName = gameName;
    }

    public FeedActivityRequest(String userId, String actionType, String gameName, Float rating, String reviewText) {
        this.userId = userId;
        this.actionType = actionType;
        this.gameName = gameName;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
}
