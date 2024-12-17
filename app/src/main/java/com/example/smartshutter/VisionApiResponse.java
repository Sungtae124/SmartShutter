package com.example.smartshutter;

import java.util.List;

public class VisionApiResponse {

    public static class LabelAnnotation {
        public String description;
        public float score;
    }

    public static class WebDetection {
        public static class WebEntity {
            public String entityId;
            public float score;
            public String description;
        }
        public static class WebImage {
            public String url;
            public float score;
        }
        public List<WebEntity> webEntities;
        public List<WebImage> fullMatchingImages;
        public List<WebImage> partialMatchingImages;
    }

    // 로고 인식 결과 추가
    public static class LogoAnnotation {
        public String description; // 로고 명(브랜드명)
        public float score;
    }

    // 텍스트 인식 결과 추가 (단순 OCR 결과)
    public static class TextAnnotation {
        public String description; // 인식된 전체 텍스트
    }

    public static class Response {
        public List<LabelAnnotation> labelAnnotations;
        public WebDetection webDetection;
        public List<LogoAnnotation> logoAnnotations; // 로고 결과
        public List<TextAnnotation> textAnnotations; // 텍스트 결과 (첫 번째 항목이 전체 텍스트일 수도 있음)
    }

    public List<Response> responses;
}
