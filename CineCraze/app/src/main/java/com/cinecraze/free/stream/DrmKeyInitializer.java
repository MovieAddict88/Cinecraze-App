package com.cinecraze.free.stream;

import android.util.Log;

public class DrmKeyInitializer {
    
    private static final String TAG = "DrmKeyInitializer";
    
    /**
     * Initialize all your DRM keys here
     * Add as many as you need in the format "kid:key"
     */
    public static void initializeAllKeys() {
        String[] allKeys = {
            // Your example keys
            "f703e4c8ec9041eeb5028ab4248fa094:c22f2162e176eee6273a5d0b68d19530", // AZ2
            "2615129ef2c846a9bbd43a641c7303ef:07c7f996b1734ea288641a68e1cfdc4d", // TV5
            "322d06e9326f4753a7ec0908030c13d8:1e3e0ca32d421fbfec86feced0efefda", // One Sports+
            
            // Add more keys here in the same format:
            // "your_kid_here:your_key_here",
            // "another_kid:another_key",
            // ... add as many as you need
        };
        
        DrmKeyManager.initializeKeys(allKeys);
        Log.d(TAG, "Initialized " + DrmKeyManager.getKeyCount() + " DRM keys");
    }
    
    /**
     * Add keys from external source (like a file or API)
     */
    public static void addKeysFromArray(String[] keyStrings) {
        for (String keyString : keyStrings) {
            DrmKeyManager.addKeyFromString(keyString);
        }
        Log.d(TAG, "Added " + keyStrings.length + " additional keys");
    }
    
    /**
     * Add a single key
     */
    public static void addSingleKey(String keyString) {
        DrmKeyManager.addKeyFromString(keyString);
        Log.d(TAG, "Added single key");
    }
    
    /**
     * Load keys from a text format (one per line)
     */
    public static void loadKeysFromText(String keysText) {
        if (keysText == null || keysText.trim().isEmpty()) {
            return;
        }
        
        String[] lines = keysText.split("\n");
        int added = 0;
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && line.contains(":")) {
                DrmKeyManager.addKeyFromString(line);
                added++;
            }
        }
        
        Log.d(TAG, "Loaded " + added + " keys from text");
    }
    
    /**
     * Example method to show how to add many keys programmatically
     */
    public static void addBulkKeys() {
        // Example: if you have a list of channels with their keys
        addChannelKey("Channel1", "kid1:key1");
        addChannelKey("Channel2", "kid2:key2");
        addChannelKey("Channel3", "kid3:key3");
        // ... add more as needed
    }
    
    private static void addChannelKey(String channelName, String keyString) {
        DrmKeyManager.addKeyFromString(keyString);
        Log.d(TAG, "Added key for channel: " + channelName);
    }
}