package com.nagy_mark.mygamevault.network;

import com.nagy_mark.mygamevault.models.AuthResponse;
import com.nagy_mark.mygamevault.models.AuthRequest;
import com.nagy_mark.mygamevault.models.FeedActivityRequest;
import com.nagy_mark.mygamevault.models.FeedModel;
import com.nagy_mark.mygamevault.models.FollowRelationship;
import com.nagy_mark.mygamevault.models.MyGame;
import com.nagy_mark.mygamevault.models.ProfileModel;
import com.nagy_mark.mygamevault.models.SavedGameModel;
import com.nagy_mark.mygamevault.models.SupabaseUserResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseApi {
        @POST("/auth/v1/token?grant_type=password")
        Call<AuthResponse> login(@Body AuthRequest request);

        @POST("/auth/v1/signup")
        Call<AuthResponse> register(@Body AuthRequest request);

        @POST("/auth/v1/token?grant_type=refresh_token")
        Call<AuthResponse> refreshToken(@Body java.util.Map<String, String> body);

        @POST("rest/v1/My_Games")
        Call<Void> insertGame(@Body MyGame myGame);

        @GET("rest/v1/My_Games")
        Call<List<SavedGameModel>> getUserSavedGames(
                @Query("user_id") String userIdFilter,
                @Query("select") String selectFields
        );

        @GET("rest/v1/My_Games")
        Call<List<SavedGameModel>> getGamesByStatus(
                @Query("user_id") String userIdFilter,
                @Query("status_id") String statusFilter
        );

        @DELETE("rest/v1/My_Games")
        Call<Void> deleteGame(@Query("id") String idEq);

        @PATCH("rest/v1/My_Games")
        Call<Void> updateGameDetails(@Query("id") String idQuery, @Body Map<String, Object> updates);

        @GET("rest/v1/profiles")
        Call<List<ProfileModel>> getProfile(@Query("id") String eqUserId);

        @Headers({"Prefer: resolution=merge-duplicates"})
        @POST("rest/v1/profiles")
        Call<Void> upsertProfile(@Body ProfileModel profile);

        @POST("storage/v1/object/avatars/{filePath}")
        Call<Void> uploadAvatar(@Path("filePath") String filePath, @Body okhttp3.RequestBody imageBytes);

        @HTTP(method = "DELETE", path = "storage/v1/object/avatars", hasBody = true)
        Call<Void> deleteAvatars(@Body java.util.Map<String, java.util.List<String>> body);

        @POST("rest/v1/feed_activities")
        Call<Void> logFeedActivity(@Body FeedActivityRequest activityRequest);

        @GET("rest/v1/follows")
        Call<List<FollowRelationship>> getMyFollowing(@Query("follower_id") String followerQuery);

        @POST("rest/v1/follows")
        Call<Void> followUser(@Body FollowRelationship followRelationship);

        @DELETE("rest/v1/follows")
        Call<Void> unfollowUser(
                @Query("follower_id") String followerId,
                @Query("following_id") String followingId
        );

        @GET("rest/v1/profiles")
        Call<List<ProfileModel>> getProfilesPaginated(@Header("Range") String range);

        @GET("rest/v1/profiles")
        Call<List<ProfileModel>> searchProfiles(@Query("username") String usernameQuery);

        @GET("rest/v1/profiles")
        Call<List<ProfileModel>> getProfilesByIds(@Query("id") String idInQuery);

        @GET("rest/v1/feed_activities")
        Call<List<FeedModel>> getFeedActivities(
                @Query("user_id") String userFilter,
                @Query("select") String selectQuery,
                @Query("order") String orderBy
        );

        @GET("rest/v1/follows")
        Call<List<FollowRelationship>> getMyFollowers(@Query("following_id") String followingQuery);

        @PATCH("rest/v1/My_Games")
        Call<Void> updateFavoriteStatus(@Query("id") String idQuery, @Body java.util.Map<String, Boolean> body);

        @POST("rest/v1/rpc/delete_user")
        Call<Void> deleteUser();
}