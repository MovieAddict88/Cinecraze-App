package com.cinecraze.free.stream;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.exoplayer2.ui.PlayerView;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class CustomPlayerView extends PlayerView {

    private GestureDetector gestureDetector;

    public CustomPlayerView(Context context) {
        super(context);
        init();
    }

    public CustomPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gestureDetector = new GestureDetector(getContext(), new PlayerGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

    private class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {

        private boolean isHorizontalScroll;
        private boolean isVerticalScroll;

        @Override
        public boolean onDown(MotionEvent e) {
            isHorizontalScroll = false;
            isVerticalScroll = false;
            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isVerticalScroll && Math.abs(distanceX) > Math.abs(distanceY)) {
                isHorizontalScroll = true;
            } else if (!isHorizontalScroll) {
                isVerticalScroll = true;
            }

            if (isHorizontalScroll) {
                if (playerScrollListener != null) {
                    playerScrollListener.onHorizontalScroll(e2.getX() - e1.getX());
                }
            } else if (isVerticalScroll) {
                if (playerScrollListener != null) {
                    playerScrollListener.onVerticalScroll(e2.getY() - e1.getY(), e1.getX() > getWidth() / 2);
                }
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (e.getX() > getWidth() / 2) {
                // Double tap on the right side of the screen
                if (playerDoubleTapListener != null) {
                    playerDoubleTapListener.onDoubleTapForward();
                }
            } else {
                // Double tap on the left side of the screen
                if (playerDoubleTapListener != null) {
                    playerDoubleTapListener.onDoubleTapRewind();
                }
            }
            return true;
        }
    }

    private PlayerDoubleTapListener playerDoubleTapListener;
    private PlayerScrollListener playerScrollListener;

    public void setPlayerDoubleTapListener(PlayerDoubleTapListener playerDoubleTapListener) {
        this.playerDoubleTapListener = playerDoubleTapListener;
    }

    public void setPlayerScrollListener(PlayerScrollListener playerScrollListener) {
        this.playerScrollListener = playerScrollListener;
    }

    public interface PlayerDoubleTapListener {
        void onDoubleTapForward();
        void onDoubleTapRewind();
    }

    public interface PlayerScrollListener {
        void onHorizontalScroll(float distanceX);
        void onVerticalScroll(float distanceY, boolean isRightSide);
    }
}
