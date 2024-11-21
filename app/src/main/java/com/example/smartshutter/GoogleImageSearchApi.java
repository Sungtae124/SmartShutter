package com.example.smartshutter;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleImageSearchApi {
    @GET("customsearch/v1")
    Call<GoogleImageSearchResponse> searchImages(
            @Query("key") String apiKey,
            @Query("cx") String searchEngineId,
            @Query("q") String query,
            @Query("searchType") String searchType,
            @Query("num") int numResults
    );
}
