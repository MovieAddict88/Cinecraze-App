package com.cinecraze.free.stream.models;

import com.google.gson.annotations.SerializedName;

public class Server {

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    @SerializedName("drm_key")
    private String drmKey;

    @SerializedName("drm_kid")
    private String drmKid;

    @SerializedName("drm_keys")
    private String drmKeys; // Format: "kid:key"

    @SerializedName("license_url")
    private String licenseUrl;

    @SerializedName("is_drm_protected")
    private boolean isDrmProtected;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }



    public String getDrmKid() {
        // If separate kid is provided, use it
        if (drmKid != null) {
            return drmKid;
        }
        // If combined format is provided, extract kid
        if (drmKeys != null && drmKeys.contains(":")) {
            return drmKeys.split(":")[0].trim();
        }
        return null;
    }

    public String getDrmKey() {
        // If separate key is provided, use it
        if (drmKey != null) {
            return drmKey;
        }
        // If combined format is provided, extract key
        if (drmKeys != null && drmKeys.contains(":")) {
            String[] parts = drmKeys.split(":");
            if (parts.length >= 2) {
                return parts[1].trim();
            }
        }
        return null;
    }

    public String getDrmKeys() {
        return drmKeys;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public boolean isDrmProtected() {
        return isDrmProtected;
    }
}
