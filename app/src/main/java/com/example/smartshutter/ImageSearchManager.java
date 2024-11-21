package com.example.smartshutter;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageSearchManager {

    private GoogleImageSearchApi googleApi;
    private NaverImageSearchApi naverApi;

    public ImageSearchManager(Context context) {
        // SecretsManager 초기화
        SecretsManager.loadSecrets(context);

        // Retrofit 초기화
        Retrofit googleRetrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        googleApi = googleRetrofit.create(GoogleImageSearchApi.class);

        Retrofit naverRetrofit = new Retrofit.Builder()
                .baseUrl("https://openapi.naver.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        naverApi = naverRetrofit.create(NaverImageSearchApi.class);
    }

    public LiveData<List<ResultItem>> searchGoogleImages(String query) {
        MutableLiveData<List<ResultItem>> resultItemsLiveData = new MutableLiveData<>();

        googleApi.searchImages(
                SecretsManager.getSecret("GOOGLE_API_KEY"),
                SecretsManager.getSecret("GOOGLE_SEARCH_ENGINE_ID"),
                query,
                "image",
                10
        ).enqueue(new Callback<GoogleImageSearchResponse>() {
            @Override
            public void onResponse(Call<GoogleImageSearchResponse> call, Response<GoogleImageSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ResultItem> results = new ArrayList<>();
                    for (GoogleImageSearchResponse.Item item : response.body().items) {
                        results.add(new ResultItem(item.link, item.title, R.drawable.ic_google));
                    }
                    resultItemsLiveData.setValue(results);
                }
            }

            @Override
            public void onFailure(Call<GoogleImageSearchResponse> call, Throwable t) {
                Log.e("GoogleAPI", "Failed to fetch images", t);
            }
        });

        return resultItemsLiveData;
    }

    public LiveData<List<ResultItem>> searchNaverImages(String query) {
        MutableLiveData<List<ResultItem>> resultItemsLiveData = new MutableLiveData<>();

        naverApi.searchImages(
                SecretsManager.getSecret("NAVER_CLIENT_ID"),
                SecretsManager.getSecret("NAVER_CLIENT_SECRET"),
                query,
                10,
                1
        ).enqueue(new Callback<NaverImageSearchResponse>() {
            @Override
            public void onResponse(Call<NaverImageSearchResponse> call, Response<NaverImageSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ResultItem> results = new ArrayList<>();
                    for (NaverImageSearchResponse.Item item : response.body().items) {
                        results.add(new ResultItem(item.link, item.title, R.drawable.ic_naver));
                    }
                    resultItemsLiveData.setValue(results);
                }
            }

            @Override
            public void onFailure(Call<NaverImageSearchResponse> call, Throwable t) {
                Log.e("NaverAPI", "Failed to fetch images", t);
            }
        });

        return resultItemsLiveData;
    }
}
