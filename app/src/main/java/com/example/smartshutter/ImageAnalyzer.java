package com.example.smartshutter;

import android.util.Base64;
import android.util.Log;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageAnalyzer {

    private static final String TAG = "ImageAnalyzer";

    // 라벨을 기반으로 이미지 검색 결과를 반환
    public static List<String> analyzeImage(File imageFile, String apiKey, String searchEngineId) {
        List<String> labels = new ArrayList<>();
        try {
            // 이미지를 Base64로 변환
            ByteString imgBytes = ByteString.readFrom(new FileInputStream(imageFile));

            // Vision API 요청 준비
            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder()
                            .addFeatures(feat)
                            .setImage(img)
                            .build();
            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            // Vision API 호출
            try (com.google.cloud.vision.v1.ImageAnnotatorClient client = com.google.cloud.vision.v1.ImageAnnotatorClient.create()) {
                List<AnnotateImageResponse> responses = client.batchAnnotateImages(requests).getResponsesList();
                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        Log.e(TAG, "Error: " + res.getError().getMessage());
                        return labels;
                    }

                    // 라벨 분석 결과를 리스트에 추가
                    res.getLabelAnnotationsList().forEach(annotation -> {
                        labels.add(annotation.getDescription());
                        Log.d(TAG, "Label: " + annotation.getDescription());
                    });
                }
            }

            // 라벨을 기반으로 검색을 실행하여 이미지 URL을 가져옵니다
            List<String> imageUrls = searchImagesByLabel(labels, apiKey, searchEngineId);
            return imageUrls;

        } catch (IOException e) {
            Log.e(TAG, "Failed to analyze image", e);
        }
        return labels;
    }

    // 라벨을 기반으로 이미지 검색 API 호출
    private static List<String> searchImagesByLabel(List<String> labels, String apiKey, String searchEngineId) {
        List<String> imageUrls = new ArrayList<>();
        // 라벨이 없으면 반환하지 않음
        if (labels.isEmpty()) {
            return imageUrls;
        }

        // 각 라벨을 기반으로 검색 API 호출
        for (String label : labels) {
            // Google 이미지 검색 API 호출
            GoogleImageSearchApi googleImageSearchApi = ApiClient.getClient().create(GoogleImageSearchApi.class);
            googleImageSearchApi.searchImage(apiKey, searchEngineId, label, "image", 3).enqueue(new retrofit2.Callback<GoogleImageSearchResponse>() {
                @Override
                public void onResponse(retrofit2.Call<GoogleImageSearchResponse> call, retrofit2.Response<GoogleImageSearchResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        GoogleImageSearchResponse googleResponse = response.body();
                        if (googleResponse.items != null && !googleResponse.items.isEmpty()) {
                            // 검색된 첫 번째 이미지 URL을 리스트에 추가
                            imageUrls.add(googleResponse.items.get(0).link);
                            Log.d(TAG, "Image URL: " + googleResponse.items.get(0).link);
                        } else {
                            // 결과가 없을 경우 기본 이미지 처리
                            Log.e(TAG, "No image found for label: " + label);
                            imageUrls.add("");  // 기본 이미지 URL을 빈 문자열로 처리
                        }
                    } else {
                        Log.e(TAG, "Google Image Search API Response Error: " + response.errorBody());
                        imageUrls.add("");  // 기본 이미지 URL을 빈 문자열로 처리
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<GoogleImageSearchResponse> call, Throwable t) {
                    Log.e(TAG, "API Call Failed", t);
                    imageUrls.add("");  // 기본 이미지 URL을 빈 문자열로 처리
                }
            });
        }

        return imageUrls;
    }
}