package com.example.smartshutter;

import java.util.List;

public class VisionApiResponse {

    public static class LabelAnnotation {
        public String description;
        public float score;
    }

    // Web Detection 결과 구조 추가
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

        // 웹 엔티티 목록 (주요 키워드 정보)
        public List<WebEntity> webEntities;
        // 완전히 일치하는 이미지
        public List<WebImage> fullMatchingImages;
        // 부분적으로 일치하는 이미지
        public List<WebImage> partialMatchingImages;
        // 페이지 정보, etc. 필요하면 추가 가능
        // public List<PagesWithMatchingImages> pagesWithMatchingImages; // 필요시 정의
    }

    public static class Response {
        public List<LabelAnnotation> labelAnnotations;
        public WebDetection webDetection; // 새로 추가한 필드
    }

    public List<Response> responses;
}
