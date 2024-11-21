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

    public static List<String> analyzeImage(File imageFile) {
        List<String> labels = new ArrayList<>();
        try {
            ByteString imgBytes = ByteString.readFrom(new FileInputStream(imageFile));

            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder()
                            .addFeatures(feat)
                            .setImage(img)
                            .build();
            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            try (com.google.cloud.vision.v1.ImageAnnotatorClient client = com.google.cloud.vision.v1.ImageAnnotatorClient.create()) {
                List<AnnotateImageResponse> responses = client.batchAnnotateImages(requests).getResponsesList();
                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        Log.e(TAG, "Error: " + res.getError().getMessage());
                        return labels;
                    }

                    res.getLabelAnnotationsList().forEach(annotation -> {
                        labels.add(annotation.getDescription());
                        Log.d(TAG, "Label: " + annotation.getDescription());
                    });
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to analyze image", e);
        }
        return labels;
    }
}
