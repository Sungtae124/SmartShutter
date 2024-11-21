package com.example.smartshutter;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SecretsManager {

    private static final String TAG = "SecretsManager";
    private static final String SECRETS_FILE = "secrets/api_keys.txt";
    private static Map<String, String> secrets = Collections.emptyMap();
    private static boolean isLoaded = false;

    /**
     * Load secrets from the assets file. Should be called once at app startup.
     *
     * @param context The application context.
     */
    public static synchronized void loadSecrets(Context context) {
        if (isLoaded) {
            Log.w(TAG, "Secrets already loaded. Skipping reload.");
            return;
        }

        secrets = new HashMap<>();

        try (InputStream inputStream = context.getAssets().open(SECRETS_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    // Skip empty lines or comments (lines starting with #)
                    continue;
                }

                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    secrets.put(parts[0].trim(), parts[1].trim());
                } else {
                    Log.w(TAG, "Invalid format in secrets file: " + line);
                }
            }

            isLoaded = true;
            Log.i(TAG, "Secrets loaded successfully.");
        } catch (IOException e) {
            Log.e(TAG, "Failed to load secrets from " + SECRETS_FILE, e);
        }
    }

    /**
     * Retrieve a secret by its key.
     *
     * @param key The key of the secret to retrieve.
     * @return The secret value, or null if the key is not found.
     */
    public static String getSecret(String key) {
        if (!isLoaded) {
            Log.w(TAG, "Secrets not loaded. Ensure loadSecrets() is called before accessing secrets.");
        }
        return secrets.getOrDefault(key, null);
    }

    /**
     * Retrieve a secret by its key with a default value.
     *
     * @param key          The key of the secret to retrieve.
     * @param defaultValue A default value to return if the key is not found.
     * @return The secret value, or the default value if the key is not found.
     */
    public static String getSecret(String key, String defaultValue) {
        if (!isLoaded) {
            Log.w(TAG, "Secrets not loaded. Ensure loadSecrets() is called before accessing secrets.");
        }
        return secrets.getOrDefault(key, defaultValue);
    }

    /**
     * Check if secrets have been successfully loaded.
     *
     * @return True if secrets are loaded, false otherwise.
     */
    public static boolean isLoaded() {
        return isLoaded;
    }
}
