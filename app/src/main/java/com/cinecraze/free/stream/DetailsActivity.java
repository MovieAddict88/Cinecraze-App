package com.cinecraze.free.stream;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinecraze.free.stream.models.Entry;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import android.widget.ImageButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        playerView = findViewById(R.id.player_view);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        relatedContentRecyclerView = findViewById(R.id.related_content_recycler_view);

        String entryJson = getIntent().getStringExtra("entry");
        if (entryJson != null) {
            entry = new Gson().fromJson(entryJson, Entry.class);
        }

        String allEntriesJson = getIntent().getStringExtra("allEntries");
        if (allEntriesJson != null) {
            allEntries = new Gson().fromJson(allEntriesJson, new com.google.gson.reflect.TypeToken<List<Entry>>() {}.getType());
        }

        if (entry != null) {
            initializePlayer();
            title.setText(entry.getTitle());
            description.setText(entry.getDescription());
            setupRelatedContentRecyclerView();
        }
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setControllerShowTimeoutMs(2000);
        playerView.setControllerHideOnTouch(true);

        if (entry.getServers() != null && !entry.getServers().isEmpty()) {
            MediaItem mediaItem = MediaItem.fromUri(entry.getServers().get(0).getUrl());
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }

        ImageButton fullscreenButton = playerView.findViewById(R.id.exo_fullscreen_button);
        fullscreenButton.setOnClickListener(v -> {
            if (entry.getServers() != null && !entry.getServers().isEmpty()) {
                String videoUrl = entry.getServers().get(0).getUrl();
                long currentPosition = player.getCurrentPosition();
                FullScreenActivity.start(this, videoUrl, currentPosition);
            }
        });
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
