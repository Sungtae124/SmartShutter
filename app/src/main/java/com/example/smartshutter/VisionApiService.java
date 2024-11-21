package com.example.smartshutter;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface VisionApiService {

    @Headers("Content-Type: application/json")
    @POST("v1/images:annotate")
    Call<VisionApiResponse> analyzeImage(
            @Query("key") String apiKey,
            @Body VisionApiRequest request
    );
}
