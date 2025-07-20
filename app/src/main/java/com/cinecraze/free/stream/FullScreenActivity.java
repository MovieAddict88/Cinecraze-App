package com.cinecraze.free.stream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

public class FullScreenActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;

    public static void start(Context context, String videoUrl, long currentPosition) {
        Intent intent = new Intent(context, FullScreenActivity.class);
        intent.putExtra("video_url", videoUrl);
        intent.putExtra("current_position", currentPosition);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        playerView = findViewById(R.id.player_view_fullscreen);

        String videoUrl = getIntent().getStringExtra("video_url");
        long currentPosition = getIntent().getLongExtra("current_position", 0);

        if (videoUrl != null) {
            initializePlayer(videoUrl, currentPosition);
        }
    }

    private void initializePlayer(String videoUrl, long currentPosition) {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);
        player.seekTo(currentPosition);
        player.prepare();
        player.play();
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
