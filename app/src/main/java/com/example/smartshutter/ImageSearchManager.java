package com.example.smartshutter;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
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

    public ImageSearchManager(Context context) {
        SecretsManager.loadSecrets(context);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://vision.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        visionApiService = retrofit.create(VisionApiService.class);
    }

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
    }
}
