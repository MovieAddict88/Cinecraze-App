package com.cinecraze.free.stream;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import android.view.View;

public class PiPActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private ImageButton pipButton;
    private ImageButton resizeModeButton;
    private int currentResizeMode = 0;
    private int currentServerIndex = 0;
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

    public static void start(Context context, String videoUrl, long currentPosition, boolean wasPlaying, int serverIndex) {
        Intent intent = new Intent(context, PiPActivity.class);
        intent.putExtra("video_url", videoUrl);
        intent.putExtra("current_position", currentPosition);
        intent.putExtra("was_playing", wasPlaying);
        intent.putExtra("server_index", serverIndex);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pip);

        // Hide system UI for clean PiP experience
        hideSystemUI();

        playerView = findViewById(R.id.player_view_pip);
        pipButton = findViewById(R.id.exo_pip_button);
        resizeModeButton = findViewById(R.id.exo_resize_mode);

        String videoUrl = getIntent().getStringExtra("video_url");
        long currentPosition = getIntent().getLongExtra("current_position", 0);
        boolean wasPlaying = getIntent().getBooleanExtra("was_playing", true);
        currentServerIndex = getIntent().getIntExtra("server_index", 0);

        if (videoUrl != null) {
            initializePlayer(videoUrl, currentPosition, wasPlaying);
        }

        // Setup PiP button to exit PiP mode
        pipButton.setOnClickListener(v -> finish());

        // Setup resize mode button
        resizeModeButton.setOnClickListener(v -> {
            currentResizeMode = (currentResizeMode + 1) % RESIZE_MODES.length;
            playerView.setResizeMode(RESIZE_MODES[currentResizeMode]);
            resizeModeButton.setImageResource(RESIZE_MODE_ICONS[currentResizeMode]);
        });

        // Enter PiP mode immediately
        enterPiPMode();
    }

    private void hideSystemUI() {
        // Hide system UI for clean PiP experience
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void enterPiPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                // Get video dimensions for proper aspect ratio
                Rational aspectRatio = new Rational(16, 9); // Default aspect ratio
                
                // Try to get actual video dimensions if available
                if (player != null && player.getVideoFormat() != null) {
                    int videoWidth = player.getVideoFormat().width;
                    int videoHeight = player.getVideoFormat().height;
                    if (videoWidth > 0 && videoHeight > 0) {
                        aspectRatio = new Rational(videoWidth, videoHeight);
                        
                        // Ensure aspect ratio is within acceptable bounds for PiP
                        float ratio = (float) videoWidth / videoHeight;
                        if (ratio < 0.42f) { // Too tall
                            aspectRatio = new Rational(42, 100);
                        } else if (ratio > 2.39f) { // Too wide
                            aspectRatio = new Rational(239, 100);
                        }
                    }
                }
                
                PictureInPictureParams params = new PictureInPictureParams.Builder()
                        .setAspectRatio(aspectRatio)
                        .build();
                        
                enterPictureInPictureMode(params);
            }
        }
    }

    private void initializePlayer(String videoUrl, long currentPosition, boolean wasPlaying) {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);
        player.seekTo(currentPosition);
        player.prepare();
        
        // Resume playing state if it was playing before
        if (wasPlaying) {
            player.play();
        }
        
        // Set controller timeout to show controls longer
        playerView.setControllerShowTimeoutMs(5000);
        playerView.setControllerHideOnTouch(true);
        
        // Use FIT mode to maintain aspect ratio
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, android.content.res.Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        
        if (isInPictureInPictureMode) {
            // Hide controls in PiP mode for cleaner look
            playerView.setUseController(false);
        } else {
            // Show controls when exiting PiP
            playerView.setUseController(true);
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back button press
        finish();
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