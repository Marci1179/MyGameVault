package com.nagy_mark.mygamevault.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.nagy_mark.mygamevault.BuildConfig;
import com.nagy_mark.mygamevault.models.AuthResponse;

import java.io.IOException;

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
            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            SupabaseApi authApi = retrofit.create(SupabaseApi.class);

            Interceptor headerInterceptor = chain -> {
                SharedPreferences prefs = context.getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
                Request originalRequest = chain.request();
                String savedToken = prefs.getString("JWT_TOKEN", null);

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

                SharedPreferences prefs = context.getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);
                String refreshToken = prefs.getString("REFRESH_TOKEN", null);

                if (refreshToken == null) {
                    return null;
                }

                try {
                    Call<AuthResponse> call = authApi.refreshToken(refreshToken);
                    retrofit2.Response<AuthResponse> res = call.execute();

                    if (res.isSuccessful() && res.body() != null) {
                        AuthResponse authResponse = res.body();
                        String newAccessToken = authResponse.getAccessToken();
                        String newRefreshToken = authResponse.getRefreshToken();

                        prefs.edit()
                                .putString("JWT_TOKEN", newAccessToken)
                                .putString("REFRESH_TOKEN", newRefreshToken)
                                .apply();

                        return response.request().newBuilder()
                                .header("Authorization", "Bearer " + newAccessToken)
                                .build();
                    } else {
                        prefs.edit().clear().apply();
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
