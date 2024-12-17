package com.example.smartshutter;

import java.util.List;
import java.util.Map;

public class CustomSearchResponse {
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public static class Item {
        private String title;
        private Map<String, Object> pagemap;

        public String getTitle() {
            return title;
        }

        public Map<String, Object> getPagemap() {
            return pagemap;
        }
    }
}
