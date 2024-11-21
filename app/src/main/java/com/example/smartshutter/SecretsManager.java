package com.example.smartshutter;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SecretsManager {

    private static final String TAG = "SecretsManager";

    private static Map<String, String> secrets = new HashMap<>();

    // secrets/api_keys.txt 파일에서 키-값 쌍 읽기
    public static void loadSecrets(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("secrets/api_keys.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    secrets.put(parts[0].trim(), parts[1].trim());
                }
            }

            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to load secrets", e);
        }
    }

    public static String getSecret(String key) {
        return secrets.get(key);
    }
}
