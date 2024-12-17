package com.example.smartshutter;

import java.util.ArrayList;
import java.util.List;

public class VisionApiRequest {

    public static class Feature {
        public String type;
        public int maxResults;

        public Feature(String type, int maxResults) {
            this.type = type;
            this.maxResults = maxResults;
        }
    }

    public static class Image {
        public String content;

        public Image(String content) {
            this.content = content;
        }
    }

    public static class Request {
        public Image image;
        public List<Feature> features;

        public Request(Image image, List<Feature> features) {
            this.image = image;
            this.features = features;
        }
    }

    public List<Request> requests = new ArrayList<>();

    public VisionApiRequest addRequest(Request request) {
        requests.add(request);
        return this;
    }
}
