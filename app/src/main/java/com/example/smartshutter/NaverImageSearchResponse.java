package com.example.smartshutter;

import java.util.List;

public class NaverImageSearchResponse {
    public List<Item> items;

    public static class Item {
        public String link; // 이미지 URL
        public String title; // 이미지 제목
    }
}
