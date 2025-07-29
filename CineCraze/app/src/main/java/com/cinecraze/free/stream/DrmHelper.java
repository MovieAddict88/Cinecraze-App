package com.cinecraze.free.stream;

import android.util.Base64;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.cinecraze.free.stream.models.Server;
import java.util.HashMap;
import java.util.Map;

public class DrmHelper {

    /**
     * Creates a MediaItem with DRM configuration if the server has DRM protection
     * For ExoPlayer 2.19.1 compatibility
     */
    public static MediaItem createMediaItem(Server server) {
        MediaItem.Builder builder = new MediaItem.Builder().setUri(server.getUrl());
        
        if (server.isDrmProtected() && server.getDrmKey() != null && server.getDrmKid() != null) {
            // For ExoPlayer 2.19.1, we need to set DRM init data
            try {
                String license = createClearKeyLicense(server.getDrmKid(), server.getDrmKey());
                if (license != null) {
                    String licenseUri = "data:application/json;base64," + 
                        Base64.encodeToString(license.getBytes(), Base64.NO_WRAP);
                    
                    // Create DRM init data for ClearKey
                    DrmInitData drmInitData = new DrmInitData(
                        C.CLEARKEY_UUID,
                        null, // No scheme data needed for ClearKey
                        new DrmInitData.SchemeData[0]
                    );
                    
                    builder.setDrmInitData(drmInitData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return builder.build();
    }

    /**
     * Creates DRM session manager for ClearKey (ExoPlayer 2.19.1)
     */
    public static DrmSessionManager createClearKeyDrmSessionManager(Server server) {
        try {
            if (!server.isDrmProtected() || server.getDrmKey() == null || server.getDrmKid() == null) {
                return null;
            }
            
            String license = createClearKeyLicense(server.getDrmKid(), server.getDrmKey());
            if (license == null) {
                return null;
            }
            
            String licenseUri = "data:application/json;base64," + 
                Base64.encodeToString(license.getBytes(), Base64.NO_WRAP);
            
            DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
            HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUri, dataSourceFactory);
            
            return new DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID, 
                        uuid -> {
                            try {
                                return new FrameworkMediaDrm(uuid);
                            } catch (Exception e) {
                                return null;
                            }
                        })
                    .build(drmCallback);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates Widevine DRM session manager (requires license server)
     */
    public static DrmSessionManager createWidevineDrmSessionManager(Server server) {
        try {
            if (server.getLicenseUrl() == null) {
                return null;
            }
            
            DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();
            HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(server.getLicenseUrl(), dataSourceFactory);
            
            return new DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID, 
                        uuid -> {
                            try {
                                return new FrameworkMediaDrm(uuid);
                            } catch (Exception e) {
                                return null;
                            }
                        })
                    .build(drmCallback);
                    
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
}