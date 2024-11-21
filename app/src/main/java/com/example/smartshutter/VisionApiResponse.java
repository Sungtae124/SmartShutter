package com.example.smartshutter;

import java.util.List;

public class VisionApiResponse {

    public static class LabelAnnotation {
        public String description;
        public float score;
    }

    public static class Response {
        public List<LabelAnnotation> labelAnnotations;
    }

    public List<Response> responses;
}
