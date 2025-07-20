package com.cinecraze.free.stream;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cinecraze.free.stream.models.Entry;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.gson.Gson;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private TextView title;
    private TextView description;
    private RecyclerView relatedContentRecyclerView;
    private MovieAdapter relatedContentAdapter;
    private Entry entry;

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

        if (entry.getServers() != null && !entry.getServers().isEmpty()) {
            MediaItem mediaItem = MediaItem.fromUri(entry.getServers().get(0).getUrl());
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }
    }

    private void setupRelatedContentRecyclerView() {
        relatedContentAdapter = new MovieAdapter(this, new ArrayList<>(), false);
        relatedContentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        relatedContentRecyclerView.setAdapter(relatedContentAdapter);
        // In a real app, you would fetch related content from your data source
        // For now, we'll just leave it empty.
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
