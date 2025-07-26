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
import android.widget.Button;
import com.google.android.exoplayer2.ui.PlayerView;
import android.content.pm.ActivityInfo;

import android.content.Context;
import android.content.Intent;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

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
    private LinearLayout qualityButtonsContainer;
    private LinearLayout qualityButtonsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        playerView = findViewById(R.id.player_view);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        relatedContentRecyclerView = findViewById(R.id.related_content_recycler_view);
        
        // Initialize TV Series components
        seasonSelectorContainer = findViewById(R.id.season_selector_container);
        episodeSelectorContainer = findViewById(R.id.episode_selector_container);
        seasonRecyclerView = findViewById(R.id.season_recycler_view);
        episodeRecyclerView = findViewById(R.id.episode_recycler_view);
        
        // Initialize quality components
        qualityButton = findViewById(R.id.exo_quality_button);
        qualityButton.setVisibility(View.GONE); // Hide gear button, use quality buttons instead
        qualityButtonsContainer = findViewById(R.id.quality_buttons_container);
        qualityButtonsLayout = findViewById(R.id.quality_buttons_layout);

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
        
        // Setup quality buttons for first episode
        setupQualityButtons(currentEpisode.getServers());
        
        playCurrentEpisode();
    }
    
    private void setupMovie() {
        // Hide season and episode selectors for movies
        seasonSelectorContainer.setVisibility(View.GONE);
        episodeSelectorContainer.setVisibility(View.GONE);
        
        // Initialize player for movie
        initializePlayer();
        
        // Setup quality buttons
        setupQualityButtons(entry.getServers());
        
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
            // Setup quality buttons for current episode
            setupQualityButtons(currentEpisode.getServers());
            
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

        ImageButton fullscreenButton = playerView.findViewById(R.id.exo_fullscreen_button);
        fullscreenButton.setOnClickListener(v -> {
            String videoUrl = getCurrentVideoUrl();
            if (videoUrl != null && player != null) {
                long currentPosition = player.getCurrentPosition();
                boolean isPlaying = player.isPlaying();
                FullScreenActivity.start(this, videoUrl, currentPosition, isPlaying, currentServerIndex);
            }
        });
    }
    
    private void setupQualityButtons(List<Server> servers) {
        if (qualityButtonsContainer == null || qualityButtonsLayout == null) {
            return; // Views not initialized yet
        }
        
        if (servers == null || servers.size() <= 1) {
            qualityButtonsContainer.setVisibility(View.GONE);
            return;
        }

        qualityButtonsContainer.setVisibility(View.VISIBLE);
        qualityButtonsLayout.removeAllViews();

        for (int i = 0; i < servers.size(); i++) {
            Server server = servers.get(i);
            if (server == null || server.getName() == null) {
                continue; // Skip null servers
            }
            
            Button qualityButton = new Button(this);
            
            // Set small button size
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 0, 4, 0); // Small margins
            qualityButton.setLayoutParams(params);
            
            // Set button properties
            qualityButton.setText(server.getName());
            qualityButton.setTextSize(10); // Very small text
            qualityButton.setBackgroundResource(R.drawable.quality_button_background);
            qualityButton.setTextColor(0xFFFFFFFF); // White text
            qualityButton.setSelected(i == currentServerIndex);
            
            // Set click listener
            final int position = i;
            qualityButton.setOnClickListener(v -> {
                currentServerIndex = position;
                updateQualityButtonsSelection();
                playCurrentVideo();
            });
            
            qualityButtonsLayout.addView(qualityButton);
        }
    }

    private void updateQualityButtonsSelection() {
        if (qualityButtonsLayout == null) {
            return;
        }
        
        for (int i = 0; i < qualityButtonsLayout.getChildCount(); i++) {
            View child = qualityButtonsLayout.getChildAt(i);
            if (child instanceof Button) {
                child.setSelected(i == currentServerIndex);
            }
        }
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
        String videoUrl = getCurrentVideoUrl();
        if (videoUrl != null) {
            MediaItem mediaItem = MediaItem.fromUri(videoUrl);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
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
        // Resume player if it was paused
        if (player != null && !player.isPlaying()) {
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
