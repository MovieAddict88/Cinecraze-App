package com.cinecraze.free.stream.models;

import com.google.gson.annotations.SerializedName;

public class Server {

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    @SerializedName("is_drm_protected")
    private boolean isDrmProtected;

    @SerializedName("drm_kid")
    private String drmKid;

    @SerializedName("drm_key")
    private String drmKey;

    @SerializedName("license")
    private String license;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isDrmProtected() {
        return isDrmProtected;
    }

    public String getDrmKid() {
        return drmKid;
    }

    public String getDrmKey() {
        return drmKey;
    }

    public String getLicense() { return license; }
}
