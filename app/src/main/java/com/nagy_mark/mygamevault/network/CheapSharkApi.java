package com.nagy_mark.mygamevault.network;

import com.nagy_mark.mygamevault.models.CheapSharkGameDetailResponse;
import com.nagy_mark.mygamevault.models.CheapSharkGameSearchResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface CheapSharkApi {
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    @GET("api/1.0/games")
    Call<List<CheapSharkGameSearchResult>> searchGame(
            @Query("title") String gameTitle,
            @Query("limit") int limit
    );

    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    @GET("api/1.0/games")
    Call<CheapSharkGameDetailResponse> getGameDetails(
            @Query("id") String gameId
    );
}