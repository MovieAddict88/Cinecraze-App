package com.cinecraze.free.stream;

import android.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class LocalLicenseServer {
    
    private static final Map<String, String> keyStore = new HashMap<>();
    
    /**
     * Add a key/ID pair to the local store
     */
    public static void addKey(String kid, String key) {
        keyStore.put(formatKeyId(kid), key);
    }
    
    /**
     * Generate ClearKey license response for a given KID
     */
    public static String generateLicense(String kid) {
        String formattedKid = formatKeyId(kid);
        String key = keyStore.get(formattedKid);
        
        if (key == null) {
            return null;
        }
        
        return String.format(
            "{\"keys\":[{\"kty\":\"oct\",\"k\":\"%s\",\"kid\":\"%s\"}],\"type\":\"temporary\"}",
            key, formattedKid
        );
    }
    
    /**
     * Initialize with your known keys
     */
    public static void initializeKeys() {
        // Add your keys here (hex format)
        addKey("f703e4c8ec9041eeb5028ab4248fa094", "c22f2162e176eee6273a5d0b68d19530"); // AZ2
        addKey("2615129ef2c846a9bbd43a641c7303ef", "07c7f996b1734ea288641a68e1cfdc4d"); // TV5
        addKey("322d06e9326f4753a7ec0908030c13d8", "1e3e0ca32d421fbfec86feced0efefda"); // One Sports+
        addKey("872910C8-4329-4319-800D-85F9A0940607", "your_key_for_hbo_family");
        // Add more keys as needed
    }
    
    private static String formatKeyId(String kid) {
        String cleanKid = kid.replace("-", "");
        byte[] kidBytes = hexStringToByteArray(cleanKid);
        return Base64.encodeToString(kidBytes, Base64.URL_SAFE | Base64.NO_PADDING);
    }
    
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