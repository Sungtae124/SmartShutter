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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageSearchManager {

    private static final String TAG = "ImageSearchManager";
    private VisionApiService visionApiService;
    private GoogleImageSearchApi googleImageSearchApi;

    public ImageSearchManager(Context context) {
        SecretsManager.loadSecrets(context);

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
                    if (response.isSuccessful() && response.body() != null) {
                        List<ResultItem> resultItems = new ArrayList<>();
                        VisionApiResponse.Response visionResponse = response.body().responses.get(0);

                        if (visionResponse.labelAnnotations != null) {
                            for (VisionApiResponse.LabelAnnotation label : visionResponse.labelAnnotations) {
                                String labelDescription = label.description;
                                // 라벨을 기반으로 이미지를 구글 이미지 검색 API로 찾아서 이미지 URL을 가져옴
                                fetchImageUrlFromLabel(labelDescription, resultItems);
                            }
                        }

                        liveData.setValue(resultItems);
                    } else {
                        Log.e(TAG, "API Response Error: " + response.errorBody());
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
    private void fetchImageUrlFromLabel(String label, List<ResultItem> resultItems) {
        // 구글 이미지 검색 API를 사용하여 라벨에 해당하는 이미지를 검색
        String apiKey = SecretsManager.getSecret("GOOGLE_API_KEY"); // 구글 API 키
        String searchEngineId = SecretsManager.getSecret("GOOGLE_SEARCH_ENGINE_ID"); // 구글 검색 엔진 ID

        Log.d(TAG, "Google API Key: " + apiKey);
        Log.d(TAG, "Google Search Engine ID: " + searchEngineId);

        googleImageSearchApi.searchImage(apiKey, searchEngineId, label, "image", 1).enqueue(new Callback<GoogleImageSearchResponse>() {
            @Override
            public void onResponse(Call<GoogleImageSearchResponse> call, Response<GoogleImageSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 응답 내용을 로그로 출력하여 확인
                    Log.d(TAG, "API Response: " + response.body());

                    List<GoogleImageSearchResponse.Item> items = response.body().items;
                    if (items != null && !items.isEmpty()) {
                        // 검색된 첫 번째 이미지 URL을 ResultItem에 추가
                        String imageUrl = items.get(0).link;
                        resultItems.add(new ResultItem(imageUrl, label, android.R.drawable.ic_menu_search));
                    } else {
                        // 이미지가 없을 경우 기본 아이콘을 사용
                        Log.e(TAG, "No image found for label: " + label);
                        resultItems.add(new ResultItem("", label, android.R.drawable.ic_menu_report_image));
                    }
                } else {
                    Log.e(TAG, "Google Image Search API Response Error: " + response.errorBody());
                    resultItems.add(new ResultItem("", label, android.R.drawable.ic_menu_report_image));
                }
            }

            @Override
            public void onFailure(Call<GoogleImageSearchResponse> call, Throwable t) {
                Log.e(TAG, "Google Image Search API Call Failed", t);
                resultItems.add(new ResultItem("", label, android.R.drawable.ic_menu_report_image));
            }
        });
    }

}

    /*
    public LiveData<List<String>> analyzeImage(File imageFile) {
        MutableLiveData<List<String>> liveData = new MutableLiveData<>();

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

            visionApiService.analyzeImage(apiKey, request).enqueue(new Callback<VisionApiResponse>() {
                @Override
                public void onResponse(Call<VisionApiResponse> call, Response<VisionApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<String> labels = new ArrayList<>();
                        VisionApiResponse.Response visionResponse = response.body().responses.get(0);

                        if (visionResponse.labelAnnotations != null) {
                            for (VisionApiResponse.LabelAnnotation label : visionResponse.labelAnnotations) {
                                labels.add(label.description);
                            }
                        }

                        liveData.setValue(labels);
                    } else {
                        Log.e(TAG, "API Response Error: " + response.errorBody());
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
    }*/


/*
    public LiveData<List<ResultItem>> analyzeImage(File imageFile) {
        MutableLiveData<List<ResultItem>> liveData = new MutableLiveData<>();

        try {
            String base64Image = ImageUtils.convertImageToBase64(imageFile);

            VisionApiRequest request = new VisionApiRequest();
            VisionApiRequest.Image image = new VisionApiRequest.Image(base64Image);
            List<VisionApiRequest.Feature> features = new ArrayList<>();
            features.add(new VisionApiRequest.Feature("LABEL_DETECTION", 10)); // 라벨 감지
            VisionApiRequest.Request visionRequest = new VisionApiRequest.Request(image, features);

            request.addRequest(visionRequest);

            String apiKey = SecretsManager.getSecret("VISION_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                Log.e(TAG, "Vision API key not found in secrets");
                liveData.setValue(new ArrayList<>());
                return liveData;
            }

            visionApiService.analyzeImage(apiKey, request).enqueue(new Callback<VisionApiResponse>() {
                @Override
                public void onResponse(Call<VisionApiResponse> call, Response<VisionApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<ResultItem> resultItems = new ArrayList<>();
                        VisionApiResponse.Response visionResponse = response.body().responses.get(0);

                        if (visionResponse.labelAnnotations != null) {
                            for (VisionApiResponse.LabelAnnotation label : visionResponse.labelAnnotations) {
                                String labelText = label.description;

                                // 라벨을 기준으로 이미지 URL을 검색하는 메서드 호출
                                List<String> imageUrls = searchImageUrlsFromLabel(labelText);

                                for (String url : imageUrls) {
                                    // 이미지 URL을 ResultItem에 추가
                                    resultItems.add(new ResultItem(url, "Description for " + labelText, android.R.drawable.ic_menu_search));
                                }
                            }
                        }

                        liveData.setValue(resultItems);
                    } else {
                        Log.e(TAG, "API Response Error: " + response.errorBody());
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
    }*/

