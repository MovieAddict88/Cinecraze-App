package com.cinecraze.free.stream;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinecraze.free.stream.models.Entry;
import com.cinecraze.free.stream.models.Season;
import com.cinecraze.free.stream.models.Episode;
import com.cinecraze.free.stream.models.Server;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import android.widget.ImageButton;
import com.google.android.exoplayer2.ui.PlayerView;
import android.content.pm.ActivityInfo;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import com.google.gson.Gson;



import java.util.ArrayList;
import java.util.List;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import java.util.HashMap;
import java.util.UUID;
import com.google.android.exoplayer2.drm.LocalMediaDrmCallback;
import android.util.Log;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import com.cinecraze.free.stream.LocalWebServer;
import java.io.IOException;

public class DetailsActivity extends AppCompatActivity {

    public static void start(Context context, Entry entry, List<Entry> allEntries) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra("entry", new Gson().toJson(entry));
        intent.putExtra("allEntries", new Gson().toJson(allEntries));
        context.startActivity(intent);
    }

    private PlayerView playerView;
    private ExoPlayer player;
    private TextView title;
    private TextView description;
    private RecyclerView relatedContentRecyclerView;
    private MovieAdapter relatedContentAdapter;
    private Entry entry;
    private List<Entry> allEntries;
    
    // TV Series components
    private LinearLayout seasonSelectorContainer;
    private LinearLayout episodeSelectorContainer;
    private RecyclerView seasonRecyclerView;
    private RecyclerView episodeRecyclerView;
    private SeasonAdapter seasonAdapter;
    private EpisodeAdapter episodeAdapter;
    private List<Season> seasons;
    private Season currentSeason;
    private Episode currentEpisode;
    
    // Quality selection
    private ImageButton qualityButton;
    private int currentServerIndex = 0;
    private SmartServerSpinner serverSpinner;
    private boolean isInFullscreen = false;

    private WebView webViewPlayer;
    private String fallbackUrl = null;
    private String fallbackKid = null;
    private String fallbackKey = null;

    private LocalWebServer localWebServer;
    private int localPort = 8080;

    // Set this to true to use the remote Shaka Player page as fallback
    // private boolean useRemoteShakaPlayer = false; // Set to true to use remote fallback

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        playerView = findViewById(R.id.player_view);
        webViewPlayer = findViewById(R.id.webview_player);
        // WebView setup
        WebSettings webSettings = webViewPlayer.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webViewPlayer.setWebViewClient(new WebViewClient());
        // Start local web server for fallback
        localWebServer = new LocalWebServer(this, localPort);
        try {
            localWebServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        relatedContentRecyclerView = findViewById(R.id.related_content_recycler_view);
        
        // Initialize TV Series components
        seasonSelectorContainer = findViewById(R.id.season_selector_container);
        episodeSelectorContainer = findViewById(R.id.episode_selector_container);
        seasonRecyclerView = findViewById(R.id.season_recycler_view);
        episodeRecyclerView = findViewById(R.id.episode_recycler_view);
        
        // Initialize quality button
        qualityButton = findViewById(R.id.exo_quality_button);

        String entryJson = getIntent().getStringExtra("entry");
        if (entryJson != null) {
            entry = new Gson().fromJson(entryJson, Entry.class);
        }

        String allEntriesJson = getIntent().getStringExtra("allEntries");
        if (allEntriesJson != null) {
            allEntries = new Gson().fromJson(allEntriesJson, new com.google.gson.reflect.TypeToken<List<Entry>>() {}.getType());
        }

        if (entry != null) {
            title.setText(entry.getTitle());
            description.setText(entry.getDescription());
            
            // Check if this is a TV series (has seasons)
            if (entry.getSeasons() != null && !entry.getSeasons().isEmpty()) {
                setupTVSeries();
            } else {
                // This is a movie or single video
                setupMovie();
            }
            
            setupRelatedContentRecyclerView();
        }
    }

    private void setupTVSeries() {
        seasons = entry.getSeasons();
        if (seasons == null || seasons.isEmpty()) {
            setupMovie(); // Fallback to movie mode if no seasons
            return;
        }
        
        currentSeason = seasons.get(0); // Start with first season
        if (currentSeason.getEpisodes() == null || currentSeason.getEpisodes().isEmpty()) {
            setupMovie(); // Fallback to movie mode if no episodes
            return;
        }
        
        currentEpisode = currentSeason.getEpisodes().get(0); // Start with first episode
        
        // Show season and episode selectors
        seasonSelectorContainer.setVisibility(View.VISIBLE);
        episodeSelectorContainer.setVisibility(View.VISIBLE);
        
        // Setup season adapter
        seasonAdapter = new SeasonAdapter(this, seasons, new SeasonAdapter.OnSeasonClickListener() {
            @Override
            public void onSeasonClick(Season season, int position) {
                currentSeason = season;
                if (season.getEpisodes() != null && !season.getEpisodes().isEmpty()) {
                    currentEpisode = season.getEpisodes().get(0); // Reset to first episode of new season
                    currentServerIndex = 0; // Reset server index for new season
                    updateEpisodeList();
                    playCurrentEpisode();
                }
            }
        });
        
        seasonRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        seasonRecyclerView.setAdapter(seasonAdapter);
        
        // Setup episode adapter
        episodeAdapter = new EpisodeAdapter(this, currentSeason.getEpisodes(), new EpisodeAdapter.OnEpisodeClickListener() {
            @Override
            public void onEpisodeClick(Episode episode, int position) {
                currentEpisode = episode;
                playCurrentEpisode();
            }
        });
        
        episodeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        episodeRecyclerView.setAdapter(episodeAdapter);
        
        // Initialize player with first episode
        initializePlayer();
        
        // Show quality button if first episode has multiple servers
        if (currentEpisode != null && currentEpisode.getServers() != null && currentEpisode.getServers().size() > 1) {
            qualityButton.setVisibility(View.VISIBLE);
        }
        
        playCurrentEpisode();
    }
    
    private void setupMovie() {
        // Hide season and episode selectors for movies
        seasonSelectorContainer.setVisibility(View.GONE);
        episodeSelectorContainer.setVisibility(View.GONE);
        
        // Initialize player for movie
        initializePlayer();
        
        // Show quality button if multiple servers available
        if (entry.getServers() != null && entry.getServers().size() > 1) {
            qualityButton.setVisibility(View.VISIBLE);
        }
        
        // Play the movie
        playCurrentVideo();
    }
    
    private void updateEpisodeList() {
        episodeAdapter = new EpisodeAdapter(this, currentSeason.getEpisodes(), new EpisodeAdapter.OnEpisodeClickListener() {
            @Override
            public void onEpisodeClick(Episode episode, int position) {
                currentEpisode = episode;
                playCurrentEpisode();
            }
        });
        episodeRecyclerView.setAdapter(episodeAdapter);
    }
    
    private void playCurrentEpisode() {
        if (currentEpisode != null && currentEpisode.getServers() != null && !currentEpisode.getServers().isEmpty()) {
            // Show quality button if multiple servers available
            if (currentEpisode.getServers().size() > 1) {
                qualityButton.setVisibility(View.VISIBLE);
            } else {
                qualityButton.setVisibility(View.GONE);
            }
            
            // Reset server index when changing episodes
            currentServerIndex = 0;
            
            playCurrentVideo();
            
            // Update episode adapter selection
            if (episodeAdapter != null) {
                int episodeIndex = currentSeason.getEpisodes().indexOf(currentEpisode);
                if (episodeIndex >= 0) {
                    episodeAdapter.setSelectedEpisode(episodeIndex);
                }
            }
        }
    }
    
    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setControllerShowTimeoutMs(2000);
        playerView.setControllerHideOnTouch(true);

        player.addListener(new com.google.android.exoplayer2.Player.Listener() {
            @Override
            public void onPlayerError(com.google.android.exoplayer2.PlaybackException error) {
                Log.e("ExoPlayer", "Playback error: " + error.getMessage(), error);
                Toast.makeText(DetailsActivity.this, "Playback error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                // Fallback to WebView if we have a key and fallback info is set
                if (fallbackUrl != null && fallbackKid != null && fallbackKey != null) {
                    fallbackToWebViewPlayer(fallbackUrl, fallbackKid, fallbackKey);
                }
            }
        });

        ImageButton fullscreenButton = playerView.findViewById(R.id.exo_fullscreen_button);
        fullscreenButton.setOnClickListener(v -> {
            String videoUrl = getCurrentVideoUrl();
            if (videoUrl != null && player != null) {
                long currentPosition = player.getCurrentPosition();
                boolean isPlaying = player.isPlaying();
                
                // Pause the current player before going to fullscreen
                if (player.isPlaying()) {
                    player.pause();
                }
                isInFullscreen = true;
                
                FullScreenActivity.start(this, videoUrl, currentPosition, isPlaying, currentServerIndex);
            }
        });

        // Setup next/previous episode buttons for TV series
        setupEpisodeNavigationButtons();

        // Setup quality button
        setupQualityButton();
    }
    
    private void setupQualityButton() {
        qualityButton.setOnClickListener(v -> {
            List<Server> servers = getCurrentServers();
            if (servers != null && servers.size() > 1) {
                // Create or update smart server spinner
                if (serverSpinner == null) {
                    serverSpinner = new SmartServerSpinner(this, servers, currentServerIndex);
                    serverSpinner.setOnServerSelectedListener(new SmartServerSpinner.OnServerSelectedListener() {
                        @Override
                        public void onServerSelected(Server server, int position) {
                            currentServerIndex = position;
                            playCurrentVideo();
                        }
                    });
                } else {
                    // Update existing spinner with new servers and current index
                    serverSpinner = new SmartServerSpinner(this, servers, currentServerIndex);
                    serverSpinner.setOnServerSelectedListener(new SmartServerSpinner.OnServerSelectedListener() {
                        @Override
                        public void onServerSelected(Server server, int position) {
                            currentServerIndex = position;
                            playCurrentVideo();
                        }
                    });
                }
                
                // Show smart spinner
                serverSpinner.show(qualityButton);
            }
        });
    }
    
    private List<Server> getCurrentServers() {
        if (currentEpisode != null && currentEpisode.getServers() != null && !currentEpisode.getServers().isEmpty()) {
            return currentEpisode.getServers();
        } else if (entry.getServers() != null && !entry.getServers().isEmpty()) {
            return entry.getServers();
        }
        return null;
    }
    
    private String getCurrentVideoUrl() {
        List<Server> servers = getCurrentServers();
        if (servers != null && currentServerIndex < servers.size()) {
            return servers.get(currentServerIndex).getUrl();
        }
        return null;
    }
    
    private void playCurrentVideo() {
        List<Server> servers = getCurrentServers();
        if (servers != null && currentServerIndex < servers.size()) {
            Server server = servers.get(currentServerIndex);
            String videoUrl = server.getUrl();
            fallbackUrl = null;
            fallbackKid = null;
            fallbackKey = null;
            if (videoUrl != null) {
                String kid = null;
                String key = null;
                if ((server.isDrmProtected() && server.getDrmKid() != null && server.getDrmKey() != null && videoUrl.endsWith(".mpd")) ||
                    (server.getLicense() != null && server.getLicense().contains(":") && videoUrl.endsWith(".mpd"))) {
                    try {
                        // Support both separate kid/key and combined license field
                        if (server.getLicense() != null && server.getLicense().contains(":")) {
                            String[] parts = server.getLicense().split(":");
                            if (parts.length == 2) {
                                kid = parts[0];
                                key = parts[1];
                            }
                        } else {
                            kid = server.getDrmKid();
                            key = server.getDrmKey();
                        }
                        if (kid != null && key != null) {
                            fallbackUrl = videoUrl;
                            fallbackKid = kid;
                            fallbackKey = key;
                            String kidB64 = android.util.Base64.encodeToString(hexStringToByteArray(kid), android.util.Base64.NO_WRAP);
                            String keyB64 = android.util.Base64.encodeToString(hexStringToByteArray(key), android.util.Base64.NO_WRAP);
                            String clearkeyJson = "{\"keys\":[{\"kty\":\"oct\",\"kid\":\"" + kidB64 + "\",\"k\":\"" + keyB64 + "\"}],\"type\":\"temporary\"}";
                            // Setup DRM session manager
                            java.util.UUID drmSchemeUuid = com.google.android.exoplayer2.C.CLEARKEY_UUID;
                            com.google.android.exoplayer2.drm.LocalMediaDrmCallback drmCallback = new com.google.android.exoplayer2.drm.LocalMediaDrmCallback(clearkeyJson.getBytes());
                            com.google.android.exoplayer2.drm.DefaultDrmSessionManager drmSessionManager = new com.google.android.exoplayer2.drm.DefaultDrmSessionManager.Builder()
                                    .setUuidAndExoMediaDrmProvider(drmSchemeUuid, com.google.android.exoplayer2.drm.FrameworkMediaDrm.DEFAULT_PROVIDER)
                                    .build(drmCallback);
                            com.google.android.exoplayer2.source.dash.DashMediaSource.Factory dashFactory = new com.google.android.exoplayer2.source.dash.DashMediaSource.Factory(new com.google.android.exoplayer2.upstream.DefaultHttpDataSource.Factory().setUserAgent(com.google.android.exoplayer2.util.Util.getUserAgent(this, "CineCraze")));
                            dashFactory.setDrmSessionManagerProvider(mediaItem -> drmSessionManager);
                            com.google.android.exoplayer2.MediaItem dashMediaItem = com.google.android.exoplayer2.MediaItem.fromUri(videoUrl);
                            com.google.android.exoplayer2.source.dash.DashMediaSource dashMediaSource = dashFactory.createMediaSource(dashMediaItem);
                            playerView.setVisibility(View.VISIBLE);
                            webViewPlayer.setVisibility(View.GONE);
                            player.setMediaSource(dashMediaSource);
                            player.prepare();
                            player.play();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Fallback immediately if ExoPlayer setup fails
                        if (kid != null && key != null) {
                            fallbackToWebViewPlayer(videoUrl, kid, key);
                        }
                    }
                } else {
                    playerView.setVisibility(View.VISIBLE);
                    webViewPlayer.setVisibility(View.GONE);
                    com.google.android.exoplayer2.MediaItem mediaItem = com.google.android.exoplayer2.MediaItem.fromUri(videoUrl);
                    player.setMediaItem(mediaItem);
                    player.prepare();
                    player.play();
                }
            }
        }
    }

    private void fallbackToWebViewPlayer(String url, String kid, String key) {
        fallbackToWebViewPlayer(url, kid, key, false);
    }

    // Two-step fallback: try local first, then remote if local fails
    private void fallbackToWebViewPlayer(String url, String kid, String key, boolean triedRemote) {
        playerView.setVisibility(View.GONE);
        webViewPlayer.setVisibility(View.VISIBLE);
        String htmlUrl;
        if (triedRemote) {
            // Use remote Shaka Player page
            htmlUrl = "https://movie-fcs.fwh.is/shakaplayer/?url=" + android.net.Uri.encode(url)
                    + "&kid=" + kid + "&key=" + key;
        } else {
            // Use local NanoHTTPD server
            htmlUrl = "http://localhost:" + localPort + "/shaka_player.html?url=" + android.net.Uri.encode(url)
                    + "&kid=" + kid + "&key=" + key;
        }
        webViewPlayer.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // If local fails, try remote
                if (!triedRemote) {
                    fallbackToWebViewPlayer(url, kid, key, true);
                }
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                // Optionally, inject JS to listen for Shaka Player errors and trigger remote fallback
            }
        });
        webViewPlayer.loadUrl(htmlUrl);
    }

    // Helper to convert hex string to byte array
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private void setupEpisodeNavigationButtons() {
        // Find next and previous episode buttons from the player control view
        ImageButton nextButton = playerView.findViewById(R.id.exo_next_episode);
        ImageButton prevButton = playerView.findViewById(R.id.exo_prev_episode);
        
        if (nextButton != null && prevButton != null) {
            // Only show navigation buttons for TV series
            if (isTVSeries()) {
                nextButton.setVisibility(View.VISIBLE);
                prevButton.setVisibility(View.VISIBLE);
                
                nextButton.setOnClickListener(v -> playNextEpisode());
                prevButton.setOnClickListener(v -> playPreviousEpisode());
            } else {
                // Hide navigation buttons for movies
                nextButton.setVisibility(View.GONE);
                prevButton.setVisibility(View.GONE);
            }
        }
    }
    
    private boolean isTVSeries() {
        return seasons != null && !seasons.isEmpty() && currentSeason != null && currentEpisode != null;
    }
    
    private void playNextEpisode() {
        if (!isTVSeries()) return;
        
        List<Episode> episodes = currentSeason.getEpisodes();
        if (episodes == null || episodes.isEmpty()) return;
        
        int currentEpisodeIndex = episodes.indexOf(currentEpisode);
        
        if (currentEpisodeIndex < episodes.size() - 1) {
            // Next episode in current season
            currentEpisode = episodes.get(currentEpisodeIndex + 1);
            currentServerIndex = 0; // Reset server index for new episode
            playCurrentEpisode();
        } else {
            // Try to move to next season
            int currentSeasonIndex = seasons.indexOf(currentSeason);
            if (currentSeasonIndex < seasons.size() - 1) {
                Season nextSeason = seasons.get(currentSeasonIndex + 1);
                if (nextSeason.getEpisodes() != null && !nextSeason.getEpisodes().isEmpty()) {
                    currentSeason = nextSeason;
                    currentEpisode = nextSeason.getEpisodes().get(0);
                    currentServerIndex = 0; // Reset server index for new season
                    updateEpisodeList();
                    updateSeasonSelection();
                    playCurrentEpisode();
                }
            }
        }
    }
    
    private void playPreviousEpisode() {
        if (!isTVSeries()) return;
        
        List<Episode> episodes = currentSeason.getEpisodes();
        if (episodes == null || episodes.isEmpty()) return;
        
        int currentEpisodeIndex = episodes.indexOf(currentEpisode);
        
        if (currentEpisodeIndex > 0) {
            // Previous episode in current season
            currentEpisode = episodes.get(currentEpisodeIndex - 1);
            currentServerIndex = 0; // Reset server index for new episode
            playCurrentEpisode();
        } else {
            // Try to move to previous season
            int currentSeasonIndex = seasons.indexOf(currentSeason);
            if (currentSeasonIndex > 0) {
                Season prevSeason = seasons.get(currentSeasonIndex - 1);
                if (prevSeason.getEpisodes() != null && !prevSeason.getEpisodes().isEmpty()) {
                    currentSeason = prevSeason;
                    List<Episode> prevSeasonEpisodes = prevSeason.getEpisodes();
                    currentEpisode = prevSeasonEpisodes.get(prevSeasonEpisodes.size() - 1); // Last episode of previous season
                    currentServerIndex = 0; // Reset server index for new season
                    updateEpisodeList();
                    updateSeasonSelection();
                    playCurrentEpisode();
                }
            }
        }
    }
    
    private void updateSeasonSelection() {
        if (seasonAdapter != null) {
            int seasonIndex = seasons.indexOf(currentSeason);
            if (seasonIndex >= 0) {
                seasonAdapter.setSelectedSeason(seasonIndex);
                seasonRecyclerView.scrollToPosition(seasonIndex);
            }
        }
    }

    private void setupRelatedContentRecyclerView() {
        if (allEntries != null) {
            List<Entry> relatedEntries = new ArrayList<>();
            for (Entry e : allEntries) {
                if (e.getSubCategory().equals(entry.getSubCategory()) && !e.getTitle().equals(entry.getTitle())) {
                    relatedEntries.add(e);
                }
            }
            relatedContentAdapter = new MovieAdapter(this, relatedEntries, false);
            relatedContentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            relatedContentRecyclerView.setAdapter(relatedContentAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Only resume player if we're not coming back from fullscreen
        // and if we're not currently in fullscreen mode
        if (player != null && !isInFullscreen) {
            // Don't auto-resume here, let the user control playback
            // This prevents unwanted auto-play when returning from other activities
        }
        
        // Reset fullscreen flag when we return
        isInFullscreen = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Returning from fullscreen
            long finalPosition = data.getLongExtra("final_position", 0);
            boolean wasPlaying = data.getBooleanExtra("was_playing", false);
            
            if (player != null) {
                // Seek to the position from fullscreen
                player.seekTo(finalPosition);
                
                // Wait for player to be ready before resuming playback
                if (wasPlaying) {
                    // Use a small delay to ensure seeking is complete
                    playerView.postDelayed(() -> {
                        if (player != null) {
                            player.play();
                        }
                    }, 100);
                } else {
                    player.pause();
                }
            }
        }
        
        // Reset fullscreen flag
        isInFullscreen = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null && !isInFullscreen) {
            player.pause();
        }
    }



    @Override
    protected void onDestroy() {
        if (localWebServer != null) {
            localWebServer.stop();
        }
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
