package com.nagy_mark.mygamevault.models;

import com.google.gson.annotations.SerializedName;

public class FollowRelationship {
    @SerializedName("follower_id")
    private String followerId;

    @SerializedName("following_id")
    private String followingId;

    public FollowRelationship() {
    }

    public FollowRelationship(String followerId, String followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public String getFollowingId() {
        return followingId;
    }
}
