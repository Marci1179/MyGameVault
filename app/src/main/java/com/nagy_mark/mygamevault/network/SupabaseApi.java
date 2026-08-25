package com.nagy_mark.mygamevault.network;

import com.nagy_mark.mygamevault.models.AuthResponse;
import com.nagy_mark.mygamevault.models.AuthRequest;
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
}