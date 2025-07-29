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

    public String getDrmKey() {
        return drmKey;
    }

    public String getDrmKid() {
        return drmKid;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public boolean isDrmProtected() {
        return isDrmProtected;
    }
}
