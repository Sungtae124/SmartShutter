package com.example.smartshutter;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImageSearchManager {
    private static final String TAG = "ImageSearchManager";
    private VisionApiService visionApiService;
    private KnowledgeGraphApiService kgApiService;
    private String KG_API_KEY;
    private CustomSearchApiService customSearchApiService;
    private String CUSTOM_SEARCH_API_KEY;
    private String SEARCH_ENGINE_ID;

    public ImageSearchManager(Context context) {
        SecretsManager.loadSecrets(context);

        String visionApiKey = SecretsManager.getSecret("VISION_API_KEY");
        KG_API_KEY = SecretsManager.getSecret("KG_API_KEY");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://vision.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        visionApiService = retrofit.create(VisionApiService.class);

        Retrofit kgRetrofit = new Retrofit.Builder()
                .baseUrl("https://kgsearch.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        kgApiService = kgRetrofit.create(KnowledgeGraphApiService.class);
    }

    public LiveData<List<ResultItem>> analyzeImage(File imageFile) {
        MutableLiveData<List<ResultItem>> liveData = new MutableLiveData<>();

        try {
            String base64Image = ImageUtils.convertImageToBase64(imageFile);
            List<VisionApiRequest.Feature> features = new ArrayList<>();
            features.add(new VisionApiRequest.Feature("LABEL_DETECTION", 10));
            features.add(new VisionApiRequest.Feature("WEB_DETECTION", 10));
            features.add(new VisionApiRequest.Feature("LOGO_DETECTION", 5));
            features.add(new VisionApiRequest.Feature("TEXT_DETECTION", 5));

            VisionApiRequest.Image image = new VisionApiRequest.Image(base64Image);
            VisionApiRequest.Request visionRequest = new VisionApiRequest.Request(image, features);
            VisionApiRequest request = new VisionApiRequest().addRequest(visionRequest);

            String apiKey = SecretsManager.getSecret("VISION_API_KEY");

            visionApiService.analyzeImage(apiKey, request).enqueue(new Callback<VisionApiResponse>() {
                @Override
                public void onResponse(Call<VisionApiResponse> call, Response<VisionApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        VisionApiResponse.Response visionResponse = response.body().responses.get(0);

                        // 키워드 추출
                        List<String> logos = extractLogoKeywords(visionResponse);
                        List<String> texts = extractTextKeywords(visionResponse);
                        List<String> labels = extractLabels(visionResponse);
                        List<String> webs = extractWebKeywords(visionResponse);

                        // 로그 추가: 추출된 키워드 출력
                        Log.d(TAG, "Extracted Logos: " + logos.toString());
                        Log.d(TAG, "Extracted Texts: " + texts.toString());
                        Log.d(TAG, "Extracted Labels: " + labels.toString());
                        Log.d(TAG, "Extracted Web Keywords: " + webs.toString());

                        // 키워드 조합 및 필터링
                        List<String> combinedQueries = buildCombinedQueries(logos, texts, labels, webs);

                        // 로그 추가: 조합된 검색어 출력
                        Log.d(TAG, "Combined Queries for API: " + combinedQueries.toString());

                        // 최종 상위 5개 선택
                        List<ResultItem> resultItems = new ArrayList<>();
                        int count = 0;

                        for (String query : combinedQueries) {
                            resultItems.add(new ResultItem("", query, android.R.drawable.ic_menu_search));
                            count++;
                            if (count == 5) break; // 상위 5개까지만 추가
                        }

                        liveData.setValue(resultItems);
                    } else {
                        Log.e(TAG, "Response not successful");
                        liveData.setValue(new ArrayList<>());
                    }
                }

                @Override
                public void onFailure(Call<VisionApiResponse> call, Throwable t) {
                    Log.e(TAG, "API call failed: " + t.getMessage());
                    liveData.setValue(new ArrayList<>());
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Failed to process image: " + e.getMessage());
            liveData.setValue(new ArrayList<>());
        }

        return liveData;
    }

    private List<ResultItem> extractCustomSearchResults(CustomSearchResponse response) {
        List<ResultItem> resultItems = new ArrayList<>();

        if (response.getItems() != null) {
            Log.d(TAG, "Extracting Results from CustomSearchResponse...");
            for (CustomSearchResponse.Item item : response.getItems()) {
                String imageUrl = null;

                // pagemap에 cse_image가 있는지 확인
                if (item.getPagemap() != null && item.getPagemap().get("cse_image") != null) {
                    List<Map<String, String>> cseImages = (List<Map<String, String>>) item.getPagemap().get("cse_image");
                    if (!cseImages.isEmpty()) {
                        imageUrl = cseImages.get(0).get("src"); // 이미지 URL 추출
                    }
                }

                if (imageUrl != null) {
                    Log.d(TAG, "Result Item: Title=" + item.getTitle() + ", Image URL=" + imageUrl);
                    resultItems.add(new ResultItem(imageUrl, item.getTitle(), android.R.drawable.ic_menu_search));
                }
            }
        }

        return resultItems;
    }

    public void searchWithCustomSearchAPI(List<String> queries, MutableLiveData<List<ResultItem>> liveData) {
        for (String query : queries) {
            customSearchApiService.searchWeb(query, CUSTOM_SEARCH_API_KEY, SEARCH_ENGINE_ID, 5)
                    .enqueue(new Callback<CustomSearchResponse>() {
                        @Override
                        public void onResponse(Call<CustomSearchResponse> call, Response<CustomSearchResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<ResultItem> results = extractCustomSearchResults(response.body());
                                // 로그 추가: 검색 결과 출력
                                Log.d(TAG, "Search API Response Items:");
                                for (ResultItem item : results) {
                                    Log.d(TAG, "Title: " + item.getDescription() + ", Image URL: " + item.getImageUrl());
                                }
                                liveData.postValue(results);
                            } else {
                                liveData.postValue(new ArrayList<>()); // 실패 시 빈 리스트 반환
                            }
                        }

                        @Override
                        public void onFailure(Call<CustomSearchResponse> call, Throwable t) {
                            liveData.postValue(new ArrayList<>()); // 실패 시 빈 리스트 반환
                        }
                    });
        }
    }




    private List<String> extractLabels(VisionApiResponse.Response visionResponse) {
        List<String> labels = new ArrayList<>();
        if (visionResponse.labelAnnotations != null) {
            for (VisionApiResponse.LabelAnnotation label : visionResponse.labelAnnotations) {
                if (label.score >= 0.7f) {
                    labels.add(label.description.toLowerCase());
                }
            }
        }
        return labels;
    }

    private List<String> extractLogoKeywords(VisionApiResponse.Response visionResponse) {
        List<String> logoKeywords = new ArrayList<>();
        if (visionResponse.logoAnnotations != null) {
            for (VisionApiResponse.LogoAnnotation logo : visionResponse.logoAnnotations) {
                if (logo.score >= 0.7f) {
                    logoKeywords.add(logo.description);
                }
            }
        }
        return logoKeywords;
    }

    private List<String> extractTextKeywords(VisionApiResponse.Response visionResponse) {
        List<String> textKeywords = new ArrayList<>();
        if (visionResponse.textAnnotations != null && !visionResponse.textAnnotations.isEmpty()) {
            String[] tokens = visionResponse.textAnnotations.get(0).description.split("\\s+");
            for (String token : tokens) {
                if (token.matches("(?i)[a-z0-9]{3,}")) {
                    textKeywords.add(token.toLowerCase());
                }
            }
        }
        return textKeywords;
    }

    private List<String> extractWebKeywords(VisionApiResponse.Response visionResponse) {
        List<String> webKeywords = new ArrayList<>();
        if (visionResponse.webDetection != null && visionResponse.webDetection.webEntities != null) {
            for (VisionApiResponse.WebDetection.WebEntity entity : visionResponse.webDetection.webEntities) {
                if (entity.description != null) {
                    webKeywords.add(entity.description.toLowerCase());
                }
            }
        }
        return webKeywords;
    }

    private List<String> extractKnowledgeGraphKeywords(KnowledgeGraphResponse response) {
        List<String> keywords = new ArrayList<>();
        if (response != null && response.getEntities() != null) {
            for (KnowledgeGraphResponse.Entity entity : response.getEntities()) {
                if (entity.getName() != null) {
                    keywords.add(entity.getName().toLowerCase());
                }
            }
        }
        return keywords;
    }

    private List<String> buildCombinedQueries(List<String> logos, List<String> texts, List<String> labels, List<String> webs) {
        Set<String> queries = new HashSet<>();

        // 1. 로고 키워드와 라벨, 텍스트 조합
        if (!logos.isEmpty()) {
            for (String logo : logos) {
                for (String text : texts) {
                    for (String label : labels) {
                        queries.add(logo + " " + text + " " + label);
                    }
                }
                for (String label : labels) {
                    queries.add(logo + " " + label);
                }
                for (String text : texts) {
                    queries.add(logo + " " + text);
                }
            }
        }

        // 2. 웹 키워드 추가 (중복 제거)
        queries.addAll(webs);

        return new ArrayList<>(queries);
    }
}