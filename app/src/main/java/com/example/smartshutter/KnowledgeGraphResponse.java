package com.example.smartshutter;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeGraphResponse {
    public List<ItemListElement> itemListElement;

    // ItemListElement 클래스
    public static class ItemListElement {
        public String resultScore;
        public Result result;

        public static class Result {
            public String name; // 엔티티 이름
            public String description; // 엔티티 설명
            public List<String> types; // 타입 정보(있다면)
        }
    }

    // Entity 클래스
    public static class Entity {
        private String name; // 엔티티 이름 필드
        private String description;

        public Entity(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    // getEntities() 메서드: ItemListElement를 Entity로 변환
    public List<Entity> getEntities() {
        List<Entity> entities = new ArrayList<>();
        if (itemListElement != null) {
            for (ItemListElement element : itemListElement) {
                if (element.result != null) {
                    entities.add(new Entity(element.result.name, element.result.description));
                }
            }
        }
        return entities; // Entity 리스트 반환
    }
}
