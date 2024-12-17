package com.example.smartshutter;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.smartshutter.SecretsManager;

public class ImageSearchManager {

    private static final String TAG = "ImageSearchManager";
    private VisionApiService visionApiService;
    private GoogleImageSearchApi googleImageSearchApi;

    public ImageSearchManager(Context context) {
        SecretsManager.loadSecrets(context);

        String visionApiKey = SecretsManager.getSecret("VISION_API_KEY");
        String googleApiKey = SecretsManager.getSecret("GOOGLE_API_KEY");
        String searchEngineId = SecretsManager.getSecret("GOOGLE_SEARCH_ENGINE_ID");

        Log.d(TAG, "VISION_API_KEY: " + visionApiKey);
        Log.d(TAG, "GOOGLE_API_KEY: " + googleApiKey);
        Log.d(TAG, "GOOGLE_SEARCH_ENGINE_ID: " + searchEngineId);

        // Vision API 호출을 위한 Retrofit 설정
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://vision.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        visionApiService = retrofit.create(VisionApiService.class);

        // Google 이미지 검색 API 설정
        googleImageSearchApi = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(GoogleImageSearchApi.class);
    }

    // 이미지 분석 후 라벨을 기반으로 이미지 URL을 반환하는 메서드
    public LiveData<List<ResultItem>> analyzeImage(File imageFile) {
        MutableLiveData<List<ResultItem>> liveData = new MutableLiveData<>();

        try {
            String base64Image = ImageUtils.convertImageToBase64(imageFile);

            VisionApiRequest request = new VisionApiRequest();
            VisionApiRequest.Image image = new VisionApiRequest.Image(base64Image);
            List<VisionApiRequest.Feature> features = new ArrayList<>();
            features.add(new VisionApiRequest.Feature("LABEL_DETECTION", 10));
            VisionApiRequest.Request visionRequest = new VisionApiRequest.Request(image, features);

            request.addRequest(visionRequest);

            String apiKey = SecretsManager.getSecret("VISION_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                Log.e(TAG, "Vision API key not found in secrets");
                liveData.setValue(new ArrayList<>());
                return liveData;
            }

            // Vision API로 이미지 분석 요청
            visionApiService.analyzeImage(apiKey, request).enqueue(new Callback<VisionApiResponse>() {
                @Override
                public void onResponse(Call<VisionApiResponse> call, Response<VisionApiResponse> response) {
                    Log.d(TAG, "Vision API onResponse called. isSuccessful: " + response.isSuccessful());
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "Vision API Response: " + response.body().toString()); // toString()구현 확인
                        List<ResultItem> resultItems = new ArrayList<>();
                        VisionApiResponse.Response visionResponse = response.body().responses.get(0);

                        if (visionResponse.labelAnnotations != null && !visionResponse.labelAnnotations.isEmpty()) {
                            Log.d(TAG, "Label Annotations count: " + visionResponse.labelAnnotations.size());
                            int labelCount = visionResponse.labelAnnotations.size();

                            // 모든 라벨에 대한 Google API 호출 완료를 추적하기 위한 카운터
                            AtomicInteger pendingCount = new AtomicInteger(labelCount);

                            for (VisionApiResponse.LabelAnnotation label : visionResponse.labelAnnotations) {
                                String labelDescription = label.description;
                                Log.d(TAG, "Label: " + labelDescription);
                                fetchImageUrlFromLabel(labelDescription, resultItems, () -> {
                                    // 한 번의 라벨에 대한 Google 이미지 검색 완료 후 실행되는 콜백
                                    int remaining = pendingCount.decrementAndGet();
                                    Log.d(TAG, "Remaining label searches: " + remaining);

                                    // 모든 라벨 처리 완료 시점에 LiveData 업데이트
                                    if (remaining == 0) {
                                        Log.d(TAG, "All label searches completed. Updating LiveData.");
                                        liveData.setValue(new ArrayList<>(resultItems));
                                    }
                                });
                            }
                        } else {
                            Log.e(TAG, "No labelAnnotations found in Vision API response");
                            liveData.setValue(new ArrayList<>());
                        }
                    } else {
                        Log.e(TAG, "API Response Error or empty response: " + response.errorBody());
                        liveData.setValue(new ArrayList<>());
                    }
                }

                @Override
                public void onFailure(Call<VisionApiResponse> call, Throwable t) {
                    Log.e(TAG, "API Call Failed", t);
                    liveData.setValue(new ArrayList<>());
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Failed to convert image to Base64", e);
            liveData.setValue(new ArrayList<>());
        }

        return liveData;
    }

    // 라벨을 기반으로 이미지 URL을 검색하는 함수 (구글 API 활용)
    // 콜백(Runnable onComplete)을 추가해, 각 라벨 처리 완료 시 알림
    private void fetchImageUrlFromLabel(String label, List<ResultItem> resultItems, Runnable onComplete) {
        String apiKey = SecretsManager.getSecret("GOOGLE_API_KEY"); // 구글 API 키
        String searchEngineId = SecretsManager.getSecret("GOOGLE_SEARCH_ENGINE_ID"); // 구글 검색 엔진 ID

        Log.d(TAG, "fetchImageUrlFromLabel called with label: " + label);
        Log.d(TAG, "Google API Key: " + apiKey);
        Log.d(TAG, "Google Search Engine ID: " + searchEngineId);

        googleImageSearchApi.searchImage(apiKey, searchEngineId, label, "image", 1).enqueue(new Callback<GoogleImageSearchResponse>() {
            @Override
            public void onResponse(Call<GoogleImageSearchResponse> call, Response<GoogleImageSearchResponse> response) {
                Log.d(TAG, "GoogleImageSearchApi onResponse for label: " + label + ", success: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    // 응답 내용을 로그로 출력하여 확인
                    Log.d(TAG, "API Response: " + response.body());

                    List<GoogleImageSearchResponse.Item> items = response.body().items;
                    if (items != null && !items.isEmpty()) {
                        String imageUrl = items.get(0).link;
                        Log.d(TAG, "Image found for label " + label + ": " + imageUrl);
                        resultItems.add(new ResultItem(imageUrl, label, android.R.drawable.ic_menu_search));
                    } else {
                        Log.e(TAG, "No image found for label: " + label);
                        resultItems.add(new ResultItem("", label, android.R.drawable.ic_menu_report_image));
                    }
                } else {
                    Log.e(TAG, "Google Image Search API Error for label: " + label + ", response code: " + response.code());
                    resultItems.add(new ResultItem("", label, android.R.drawable.ic_menu_report_image));
                }

                if (onComplete != null) {
                    onComplete.run(); // 이 라벨 처리 완료 후 카운트 감소 등 처리
                }
            }

            @Override
            public void onFailure(Call<GoogleImageSearchResponse> call, Throwable t) {
                Log.e(TAG, "Google Image Search API Call Failed for label: " + label, t);
                resultItems.add(new ResultItem("", label, android.R.drawable.ic_menu_report_image));

                if (onComplete != null) {
                    onComplete.run(); // 실패에도 완료 콜백은 호출
                }
            }
        });
    }

}

