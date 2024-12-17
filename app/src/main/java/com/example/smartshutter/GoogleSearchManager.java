package com.example.smartshutter;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GoogleSearchManager {
    private static final String TAG = "GoogleSearchManager";
    private GoogleImageSearchApi googleImageSearchApi;

    public GoogleSearchManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        googleImageSearchApi = retrofit.create(GoogleImageSearchApi.class);
    }

    public LiveData<List<ResultItem>> searchImages(List<String> queries) {
        MutableLiveData<List<ResultItem>> liveData = new MutableLiveData<>();
        List<ResultItem> results = new ArrayList<>();
        AtomicInteger pendingCount = new AtomicInteger(queries.size());

        for (String query : queries) {
            fetchImage(query, results, () -> {
                if (pendingCount.decrementAndGet() == 0) {
                    liveData.setValue(results);
                }
            });
        }

        return liveData;
    }

    private void fetchImage(String query, List<ResultItem> results, Runnable onComplete) {
        String apiKey = SecretsManager.getSecret("GOOGLE_API_KEY");
        String searchEngineId = SecretsManager.getSecret("GOOGLE_SEARCH_ENGINE_ID");

        googleImageSearchApi.searchImage(apiKey, searchEngineId, query, "image", 1)
                .enqueue(new Callback<GoogleImageSearchResponse>() {
                    @Override
                    public void onResponse(Call<GoogleImageSearchResponse> call, Response<GoogleImageSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().items.isEmpty()) {
                            // ResultItem 생성 시 엔진 아이콘 추가
                            results.add(new ResultItem(
                                    response.body().items.get(0).link, // 이미지 URL
                                    query,                           // 설명 (검색어)
                                    android.R.drawable.ic_menu_search // 기본 아이콘
                            ));
                        } else {
                            Log.e("Image Fetch", "No results found for query: " + query);
                        }
                        onComplete.run();
                    }

                    @Override
                    public void onFailure(Call<GoogleImageSearchResponse> call, Throwable t) {
                        Log.e("Image Fetch", "Error fetching image for query: " + query, t);
                        onComplete.run();
                    }
                });
    }

}
