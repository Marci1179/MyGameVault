package com.nagy_mark.mygamevault.network;

import android.content.Context;

import com.nagy_mark.mygamevault.BuildConfig;
import com.nagy_mark.mygamevault.models.TwitchTokenResponse;
import com.nagy_mark.mygamevault.utils.SessionManager;

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
            SessionManager sessionManager = new SessionManager(context);

            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    String token = sessionManager.getTwitchToken();
                    
                    if (token == null) {
                        token = fetchNewToken();
                        if (token != null) {
                            sessionManager.saveTwitchToken(token);
                        }
                    }

                    Request originalRequest = chain.request();
                    Request requestWithAuth = buildRequest(originalRequest, token);

                    Response response = chain.proceed(requestWithAuth);

                    if (response.code() == 401) {
                        response.close();

                        token = fetchNewToken();
                        if (token != null) {
                            sessionManager.saveTwitchToken(token);

                            Request newRequest = buildRequest(originalRequest, token);
                            return chain.proceed(newRequest);
                        }
                    }

                    return response;
                }

                private String fetchNewToken() throws IOException {
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
                        return tokenResponse.body().getAccessToken();
                    }
                    return null;
                }

                private Request buildRequest(Request originalRequest, String token) {
                    return originalRequest.newBuilder()
                            .header("Client-ID", BuildConfig.IGDB_CLIENT_ID)
                            .header("Authorization", "Bearer " + token)
                            .header("Accept", "application/json")
                            .build();
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