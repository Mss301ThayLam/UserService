package com.mss.user_service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DotEnvConfig {

    public static void loadEnv() {
        try (InputStream is = DotEnvConfig.class.getClassLoader().getResourceAsStream(".env")) {
            if (is == null) {
                System.out.println("[DotEnvConfig] .env file not found in classpath, skipping...");
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx < 0) continue;
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                // Only set if not already defined (allows OS env vars to override)
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                }
            }
            System.out.println("[DotEnvConfig] .env loaded successfully");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load .env file", e);
        }
    }
}
