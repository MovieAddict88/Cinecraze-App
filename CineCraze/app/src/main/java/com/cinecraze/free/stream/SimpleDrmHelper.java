package com.cinecraze.free.stream;

import android.util.Base64;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.cinecraze.free.stream.models.Server;

public class SimpleDrmHelper {

    /**
     * Creates a MediaItem - for ExoPlayer 2.19.1, DRM is handled at player level
     */
    public static MediaItem createMediaItem(Server server) {
        return MediaItem.fromUri(server.getUrl());
    }

    /**
     * Check if server needs DRM
     */
    public static boolean needsDrm(Server server) {
        return server.isDrmProtected() && 
               (hasValidDrmKeys(server));
    }
    
    /**
     * Check if server has valid DRM keys in any format
     */
    public static boolean hasValidDrmKeys(Server server) {
        // Check if separate kid/key are provided
        if (server.getDrmKey() != null && server.getDrmKid() != null) {
            return true;
        }
        // Check if combined format is provided
        if (server.getDrmKeys() != null && server.getDrmKeys().contains(":")) {
            return true;
        }
        return false;
    }
    
    /**
     * Check if we have DRM keys available for this server
     */
    public static boolean hasDrmKey(Server server) {
        if (!server.isDrmProtected()) return false;
        return hasValidDrmKeys(server);
    }
    
    /**
     * Get DRM keys for server from JSON data
     */
    public static DrmKeyManager.DrmKeyPair getDrmKeys(Server server) {
        if (!hasValidDrmKeys(server)) {
            return null;
        }
        
        String kid = server.getDrmKid();
        String key = server.getDrmKey();
        
        if (kid != null && key != null) {
            return new DrmKeyManager.DrmKeyPair(kid, key);
        }
        
        return null;
    }

    /**
     * Creates a ClearKey license JSON for the given KID and key (both in hex format)
     */
    public static String createClearKeyLicense(String kid, String key) {
        try {
            // Format both KID and key from hex to base64url
            String formattedKid = formatKeyId(kid);
            String formattedKey = formatKey(key);
            
            // ClearKey license format
            String license = String.format(
                "{\"keys\":[{\"kty\":\"oct\",\"k\":\"%s\",\"kid\":\"%s\"}],\"type\":\"temporary\"}",
                formattedKey, formattedKid
            );
            return license;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Formats Key ID from hex format to base64url format for ClearKey
     */
    private static String formatKeyId(String kid) {
        // Remove hyphens if present (UUID format)
        String cleanKid = kid.replace("-", "");
        
        // Convert hex to bytes
        byte[] kidBytes = hexStringToByteArray(cleanKid);
        
        // Encode to base64url (no padding)
        return Base64.encodeToString(kidBytes, Base64.URL_SAFE | Base64.NO_PADDING);
    }

    /**
     * Formats decryption key from hex format to base64url format for ClearKey
     */
    private static String formatKey(String key) {
        // Convert hex to bytes
        byte[] keyBytes = hexStringToByteArray(key);
        
        // Encode to base64url (no padding)
        return Base64.encodeToString(keyBytes, Base64.URL_SAFE | Base64.NO_PADDING);
    }

    /**
     * Converts hex string to byte array
     */
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}