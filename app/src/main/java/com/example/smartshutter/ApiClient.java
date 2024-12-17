package com.example.smartshutter;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Retrofit 클라이언트 설정
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.googleapis.com/") // 구글 API의 기본 URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
