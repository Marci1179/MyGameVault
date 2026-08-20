package com.nagy_mark.mygamevault.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.nagy_mark.mygamevault.BuildConfig;
import com.nagy_mark.mygamevault.models.TwitchTokenResponse;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IgdbApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            SharedPreferences prefs = context.getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);

            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    String token = prefs.getString("TWITCH_TOKEN", null);

                    if (token == null) {
                        Retrofit twitchRetrofit = new Retrofit.Builder()
                                .baseUrl("https://id.twitch.tv/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        TwitchApi twitchApi = twitchRetrofit.create(TwitchApi.class);

                        retrofit2.Response<TwitchTokenResponse> tokenResponse = twitchApi.getAppAccessToken(
                                BuildConfig.IGDB_CLIENT_ID,
                                BuildConfig.IGDB_CLIENT_SECRET
                        ).execute();

                        if (tokenResponse.isSuccessful() && tokenResponse.body() != null) {
                            token = tokenResponse.body().getAccessToken();
                            prefs.edit().putString("TWITCH_TOKEN", token).apply();
                        }
                    }

                    Request originalRequest = chain.request();
                    Request.Builder builder = originalRequest.newBuilder()
                            .header("Client-ID", BuildConfig.IGDB_CLIENT_ID)
                            .header("Authorization", "Bearer " + token)
                            .header("Accept", "application/json");

                    return chain.proceed(builder.build());
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.igdb.com/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
