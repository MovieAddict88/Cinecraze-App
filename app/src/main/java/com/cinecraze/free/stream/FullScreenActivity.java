package com.cinecraze.free.stream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;

public class FullScreenActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private ImageButton resizeModeButton;
    private int currentResizeMode = 0;
    private static final int[] RESIZE_MODES = {
            AspectRatioFrameLayout.RESIZE_MODE_FIT,
            AspectRatioFrameLayout.RESIZE_MODE_FILL,
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    };
    private static final int[] RESIZE_MODE_ICONS = {
            R.drawable.ic_fit,
            R.drawable.ic_fill,
            R.drawable.ic_zoom
    };

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

        hideSystemUI();

        playerView = findViewById(R.id.player_view_fullscreen);
        resizeModeButton = findViewById(R.id.exo_resize_mode);

        String videoUrl = getIntent().getStringExtra("video_url");
        long currentPosition = getIntent().getLongExtra("current_position", 0);

        if (videoUrl != null) {
            initializePlayer(videoUrl, currentPosition);
        }

        resizeModeButton.setOnClickListener(v -> {
            currentResizeMode = (currentResizeMode + 1) % RESIZE_MODES.length;
            playerView.setResizeMode(RESIZE_MODES[currentResizeMode]);
            resizeModeButton.setImageResource(RESIZE_MODE_ICONS[currentResizeMode]);
        });
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN);
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
