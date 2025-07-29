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
        
        // Expected format should be something like:
        // {"keys":[{"kty":"oct","k":"B8f5lrFzTqKIZBpo4c_cTQ","kid":"JhUSnvLIRqm71DpkHHMD7w"}],"type":"temporary"}
    }
}