package com.nagy_mark.mygamevault.network;

import android.content.Context;

import com.nagy_mark.mygamevault.BuildConfig;
import com.nagy_mark.mygamevault.models.AuthResponse;
import com.nagy_mark.mygamevault.utils.SessionManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            SessionManager sessionManager = new SessionManager(context);

            OkHttpClient authClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .header("apikey", BuildConfig.SUPABASE_API_KEY)
                                .header("Content-Type", "application/json")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            Retrofit authRetrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL)
                    .client(authClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            SupabaseApi authApi = authRetrofit.create(SupabaseApi.class);

            Interceptor headerInterceptor = chain -> {
                Request originalRequest = chain.request();
                String savedToken = sessionManager.getJwtToken();

                Request.Builder builder = originalRequest.newBuilder()
                        .header("apikey", BuildConfig.SUPABASE_API_KEY)
                        .header("Content-Type", "application/json");

                if (savedToken != null) {
                    builder.header("Authorization", "Bearer " + savedToken);
                }

                return chain.proceed(builder.build());
            };

            okhttp3.Authenticator tokenAuthenticator = (route, response) -> {
                if (responseCount(response) >= 2) {
                    return null;
                }

                String refreshToken = sessionManager.getRefreshToken();

                if (refreshToken == null) {
                    return null;
                }

                try {
                    Map<String, String> body = new HashMap<>();
                    body.put("refresh_token", refreshToken);

                    Call<AuthResponse> call = authApi.refreshToken(body);
                    retrofit2.Response<AuthResponse> res = call.execute();

                    if (res.isSuccessful() && res.body() != null) {
                        AuthResponse authResponse = res.body();
                        String newAccessToken = authResponse.getAccessToken();
                        String newRefreshToken = authResponse.getRefreshToken();

                        String userId = sessionManager.getUserId();
                        if (authResponse.getUser() != null && authResponse.getUser().getId() != null) {
                            userId = authResponse.getUser().getId();
                        }

                        sessionManager.saveSession(newAccessToken, newRefreshToken, userId);

                        return response.request().newBuilder()
                                .header("Authorization", "Bearer " + newAccessToken)
                                .build();
                    } else {
                        sessionManager.clearSession();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerInterceptor)
                    .authenticator(tokenAuthenticator)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}