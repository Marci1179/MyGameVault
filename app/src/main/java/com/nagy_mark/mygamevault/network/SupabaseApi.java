package com.nagy_mark.mygamevault.network;

import com.nagy_mark.mygamevault.models.AuthResponse;
import com.nagy_mark.mygamevault.models.AuthRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SupabaseApi {
        @POST("/auth/v1/token?grant_type=password")
        Call<AuthResponse> login(@Body AuthRequest request);

        @POST("/auth/v1/signup")
        Call<AuthResponse> register(@Body AuthRequest request);
}