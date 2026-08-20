package com.nagy_mark.mygamevault.network;

import com.nagy_mark.mygamevault.models.Game;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IgdbApi {
    @POST("/v4/games")
    Call<List<Game>> getTopGames(@Body RequestBody query);
}
