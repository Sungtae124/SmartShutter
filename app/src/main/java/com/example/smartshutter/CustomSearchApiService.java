package com.example.smartshutter;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CustomSearchApiService {
    @GET("customsearch/v1")
    Call<CustomSearchResponse> searchWeb(
            @Query("q") String query,       // 검색어
            @Query("key") String apiKey,    // API 키
            @Query("cx") String searchEngineId, // Custom Search 엔진 ID
            @Query("num") int num           // 검색 결과 수
    );
}
