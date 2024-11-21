package com.example.smartshutter;

import android.graphics.Bitmap;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class ResultItem {
    private String imageUrl; // 검색 결과 이미지 URL
    private String description; // 검색 결과 설명
    private int engineIcon; // 검색 엔진 아이콘 리소스 ID
    private Bitmap bitmap; // 변환된 Bitmap 저장

    public ResultItem(String imageUrl, String description, int engineIcon) {
        // 여기서 imageUrl이 유효한 이미지 URL인지 검사 후 저장
        if (isValidImageUrl(imageUrl)) {
            this.imageUrl = imageUrl;
        } else {
            this.imageUrl = "";  // 잘못된 URL은 빈 문자열로 처리
        }
        this.description = description;
        this.engineIcon = engineIcon;
    }

    // 이미지 URL 유효성 검사
    private boolean isValidImageUrl(String url) {
        try {
            new URL(url);  // URL이 유효한지 확인
            return true;
        } catch (MalformedURLException e) {
            Log.e("Image URL Error", "Invalid URL: " + url);
            return false;
        }
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
