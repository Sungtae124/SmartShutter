package com.example.smartshutter;

public class ResultItem {
    private String imageUrl; // 검색 결과 이미지 URL
    private String description; // 검색 결과 설명
    private int engineIcon; // 검색 엔진 아이콘 리소스 ID

    // 생성자
    public ResultItem(String imageUrl, String description, int engineIcon) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.engineIcon = engineIcon;
    }

    // Getter 메서드
    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public int getEngineIcon() {
        return engineIcon;
    }
}
