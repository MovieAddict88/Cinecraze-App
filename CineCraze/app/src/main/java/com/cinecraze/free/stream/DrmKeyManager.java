package com.cinecraze.free.stream;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class DrmKeyManager {
    
    private static final String TAG = "DrmKeyManager";
    private static final Map<String, DrmKeyPair> keyStore = new HashMap<>();
    
    public static class DrmKeyPair {
        public final String kid;
        public final String key;
        
        public DrmKeyPair(String kid, String key) {
            this.kid = kid;
            this.key = key;
        }
    }
    
    /**
     * Add a key/ID pair from the format "kid:key"
     */
    public static void addKeyFromString(String keyString) {
        try {
            if (keyString != null && keyString.contains(":")) {
                String[] parts = keyString.split(":");
                if (parts.length == 2) {
                    String kid = parts[0].trim();
                    String key = parts[1].trim();
                    addKey(kid, key);
                    Log.d(TAG, "Added DRM key for KID: " + kid.substring(0, 8) + "...");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing key string: " + keyString, e);
        }
    }
    
    /**
     * Add a key/ID pair separately
     */
    public static void addKey(String kid, String key) {
        if (kid != null && key != null) {
            keyStore.put(kid.toLowerCase(), new DrmKeyPair(kid, key));
        }
    }
    
    /**
     * Get key pair for a given KID
     */
    public static DrmKeyPair getKeyPair(String kid) {
        if (kid == null) return null;
        return keyStore.get(kid.toLowerCase());
    }
    
    /**
     * Check if we have a key for this KID
     */
    public static boolean hasKey(String kid) {
        return kid != null && keyStore.containsKey(kid.toLowerCase());
    }
    
    /**
     * Get total number of stored keys
     */
    public static int getKeyCount() {
        return keyStore.size();
    }
    
    /**
     * Clear all keys
     */
    public static void clearKeys() {
        keyStore.clear();
        Log.d(TAG, "Cleared all DRM keys");
    }
    
    /**
     * Initialize with multiple key strings
     */
    public static void initializeKeys(String[] keyStrings) {
        clearKeys();
        for (String keyString : keyStrings) {
            addKeyFromString(keyString);
        }
        Log.d(TAG, "Initialized with " + getKeyCount() + " DRM keys");
    }
    
    /**
     * Log all stored keys (for debugging)
     */
    public static void logAllKeys() {
        Log.d(TAG, "=== DRM Key Store ===");
        Log.d(TAG, "Total keys: " + getKeyCount());
        for (Map.Entry<String, DrmKeyPair> entry : keyStore.entrySet()) {
            String kid = entry.getValue().kid;
            Log.d(TAG, "KID: " + kid.substring(0, 8) + "... -> Key available");
        }
        Log.d(TAG, "===================");
    }
}