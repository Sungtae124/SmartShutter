package com.example.smartshutter;

import android.graphics.Bitmap;

public class ResultItem {
    private String imageUrl; // 검색 결과 이미지 URL
    private String description; // 검색 결과 설명
    private int engineIcon; // 검색 엔진 아이콘 리소스 ID
    private Bitmap bitmap; // 변환된 Bitmap 저장

    public ResultItem(String imageUrl, String description, int engineIcon) {
        this.imageUrl = imageUrl;
        this.description = description;
        this.engineIcon = engineIcon;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public int getEngineIcon() {
        return engineIcon;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isSvg() {
        return imageUrl != null && imageUrl.endsWith(".svg");
    }
}
