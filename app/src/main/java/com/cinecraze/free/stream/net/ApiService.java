package com.cinecraze.free.stream.net;

import com.cinecraze.free.stream.models.Playlist;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("playlist.json")
    Call<Playlist> getPlaylist();
}
