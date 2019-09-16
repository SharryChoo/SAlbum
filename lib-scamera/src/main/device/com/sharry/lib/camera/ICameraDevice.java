package com.sharry.lib.camera;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;

import androidx.annotation.NonNull;

/**
 * The interface desc camera device.
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-04-24
 */
interface ICameraDevice {

    void open();

    void close();

    Bitmap takePicture();

    boolean isCameraOpened();

    void notifyFacingChanged();

    void notifyAspectRatioChanged();

    void notifyAutoFocusChanged();

    void notifyFlashModeChanged();

    void notifyScreenOrientationChanged();

    void notifyDesiredSizeChanged();

    interface OnCameraReadyListener {

        void onCameraReady(@NonNull SurfaceTexture cameraTexture, @NonNull Size textureSize, int displayRotation);

    }

}
