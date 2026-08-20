package com.nagy_mark.mygamevault.network;

import com.nagy_mark.mygamevault.models.TwitchTokenResponse;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TwitchApi {
    @POST("oauth2/token?grant_type=client_credentials")
    Call<TwitchTokenResponse> getAppAccessToken(
            @Query("client_id") String clientId,
            @Query("client_secret") String clientSecret
    );
}
