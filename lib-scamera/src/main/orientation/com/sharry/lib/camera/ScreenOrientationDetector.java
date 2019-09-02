package com.sharry.lib.camera;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;

/**
 * 屏幕方向探测器
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-08-05
 */
class ScreenOrientationDetector {

    /**
     * Mapping from Surface.Rotation_n to degrees.
     */
    private static final SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();

    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 90);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 180);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 270);
    }

    private final OrientationEventListener mOrientationEventListener;
    private OnDisplayChangedListener mListener;
    /**
     * This is either Surface.Rotation_0, _90, _180, _270, or -1 (invalid).
     */
    private int mLastRotation = 0;
    private Display mDisplay;

    ScreenOrientationDetector(Context context, final OnDisplayChangedListener listener) {
        this.mListener = listener;
        this.mOrientationEventListener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN ||
                        mDisplay == null) {
                    return;
                }
                final int rotation = mDisplay.getRotation();
                if (mLastRotation != rotation) {
                    mLastRotation = rotation;
                    mListener.onDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(mLastRotation));
                }
            }
        };
    }

    void enable(Display display) {
        mDisplay = display;
        mOrientationEventListener.enable();
        // callback at once
        mLastRotation = mDisplay.getRotation();
        mListener.onDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(mLastRotation));
    }

    void disable() {
        mOrientationEventListener.disable();
        mDisplay = null;
    }

    boolean isLandscape() {
        int screenOrientationDegrees = DISPLAY_ORIENTATIONS.get(mLastRotation);
        return (screenOrientationDegrees == Constants.LANDSCAPE_90
                || screenOrientationDegrees == Constants.LANDSCAPE_270);
    }

    interface OnDisplayChangedListener {

        /**
         * Called when display orientation is changed.
         *
         * @param displayOrientation One of 0, 90, 180, and 270.
         */
        void onDisplayOrientationChanged(int displayOrientation);
    }

}
