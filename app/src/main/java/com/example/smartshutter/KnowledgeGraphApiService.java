package com.example.smartshutter;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface KnowledgeGraphApiService {
    @GET("v1/entities:search")
    Call<KnowledgeGraphResponse> searchEntities(
            @Query("query") String query,
            @Query("key") String apiKey,
            @Query("limit") int limit,
            @Query("indent") boolean indent
    );
}
