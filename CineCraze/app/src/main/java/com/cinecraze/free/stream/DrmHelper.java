package com.cinecraze.free.stream;

import android.util.Base64;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.drm.DrmConfiguration;
import com.cinecraze.free.stream.models.Server;
import java.util.HashMap;
import java.util.Map;

public class DrmHelper {

    /**
     * Creates a MediaItem with DRM configuration if the server has DRM protection
     */
    public static MediaItem createMediaItem(Server server) {
        MediaItem.Builder builder = new MediaItem.Builder().setUri(server.getUrl());
        
        if (server.isDrmProtected() && server.getDrmKey() != null && server.getDrmKid() != null) {
            DrmConfiguration drmConfig = createDrmConfiguration(server);
            if (drmConfig != null) {
                builder.setDrmConfiguration(drmConfig);
            }
        }
        
        return builder.build();
    }

    /**
     * Creates DRM configuration using ClearKey (for cases where you have the actual keys)
     */
    private static DrmConfiguration createDrmConfiguration(Server server) {
        try {
            // Use ClearKey DRM when you have the actual decryption keys
            Map<String, String> keyMap = new HashMap<>();
            
            // Format the KID and Key properly
            String formattedKid = formatKeyId(server.getDrmKid());
            String formattedKey = server.getDrmKey();
            
            keyMap.put(formattedKid, formattedKey);
            
            return new DrmConfiguration.Builder(C.CLEARKEY_UUID)
                    .setMultiSession(false)
                    .setForceDefaultLicenseUri(false)
                    .build();
                    
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates Widevine DRM configuration (requires license server)
     */
    public static DrmConfiguration createWidevineDrmConfiguration(Server server) {
        if (server.getLicenseUrl() == null) {
            return null;
        }
        
        return new DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setLicenseUri(server.getLicenseUrl())
                .setMultiSession(false)
                .build();
    }

    /**
     * Formats Key ID from UUID format to base64url format for ClearKey
     */
    private static String formatKeyId(String kid) {
        // Remove hyphens from UUID format
        String cleanKid = kid.replace("-", "");
        
        // Convert hex to bytes
        byte[] kidBytes = hexStringToByteArray(cleanKid);
        
        // Encode to base64url (no padding)
        return Base64.encodeToString(kidBytes, Base64.URL_SAFE | Base64.NO_PADDING);
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

    /**
     * Creates a custom license request for ClearKey
     */
    public static String createClearKeyLicense(String kid, String key) {
        try {
            // ClearKey license format
            String license = String.format(
                "{\"keys\":[{\"kty\":\"oct\",\"k\":\"%s\",\"kid\":\"%s\"}],\"type\":\"temporary\"}",
                key, formatKeyId(kid)
            );
            return license;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}