package com.nagy_mark.mygamevault.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.nagy_mark.mygamevault.BuildConfig;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            SharedPreferences prefs = context.getSharedPreferences("MyGameVaultPrefs", Context.MODE_PRIVATE);

            Interceptor headerInterceptor = chain -> {
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

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
