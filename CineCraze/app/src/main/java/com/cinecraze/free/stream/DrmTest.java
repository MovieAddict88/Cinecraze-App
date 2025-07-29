package com.cinecraze.free.stream;

import android.util.Log;

public class DrmTest {
    
    public static void testTV5Keys() {
        String kid = "2615129ef2c846a9bbd43a641c7303ef";
        String key = "07c7f996b1734ea288641a68e1cfdc4d";
        
        String license = DrmHelper.createClearKeyLicense(kid, key);
        
        Log.d("DrmTest", "TV5 KID: " + kid);
        Log.d("DrmTest", "TV5 Key: " + key);
        Log.d("DrmTest", "Generated License: " + license);
    }
    
    public static void testOneSportsKeys() {
        String kid = "322d06e9326f4753a7ec0908030c13d8";
        String key = "1e3e0ca32d421fbfec86feced0efefda";
        
        String license = DrmHelper.createClearKeyLicense(kid, key);
        
        Log.d("DrmTest", "One Sports+ KID: " + kid);
        Log.d("DrmTest", "One Sports+ Key: " + key);
        Log.d("DrmTest", "Generated License: " + license);
    }
    
    public static void testAZ2Keys() {
        String kid = "f703e4c8ec9041eeb5028ab4248fa094";
        String key = "c22f2162e176eee6273a5d0b68d19530";
        
        String license = DrmHelper.createClearKeyLicense(kid, key);
        
        Log.d("DrmTest", "AZ2 KID: " + kid);
        Log.d("DrmTest", "AZ2 Key: " + key);
        Log.d("DrmTest", "Generated License: " + license);
    }
    
    public static void testAllKeys() {
        testAZ2Keys();
        testTV5Keys();
        testOneSportsKeys();
    }
}