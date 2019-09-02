package com.sharry.lib.camera;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.util.Rational;

import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.LifecycleOwner;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-07-26 13:57
 */
@TargetApi(21)
class CameraXDevice extends AbsCameraDevice implements Preview.OnPreviewOutputUpdateListener {

    private Preview mPreview;
    private LifecycleOwner mLifecycleOwner;

    CameraXDevice(CameraContext context, OnCameraReadyListener listener) {
        super(context, listener);
        this.mLifecycleOwner = (LifecycleOwner) context.getBaseContext();
    }

    @Override
    public boolean isCameraOpened() {
        return mPreview != null && CameraX.isBound(mPreview);
    }

    @Override
    public void open() {
        close();
        setupPreview();
        CameraX.bindToLifecycle(mLifecycleOwner, mPreview);
    }

    @Override
    public void close() {
        CameraX.unbindAll();
    }

    @Nullable
    @Override
    public Bitmap takePicture() {
        return null;
    }

    @Override
    public void notifyAutoFocusChanged() {
        if (this.autoFocus == context.isAutoFocus()) {
            return;
        }
        this.autoFocus = context.isAutoFocus();
        if (isCameraOpened()) {
            // TODO: 探究 CameraX 的自动对焦技术
        }
    }

    @Override
    public void notifyFlashModeChanged() {
        if (flashMode == context.getFlashMode()) {
            return;
        }
        flashMode = context.getFlashMode();
        if (isCameraOpened()) {
            // TODO: 探究 CameraX 的闪光灯控制
        }
    }

    @Override
    public void onUpdated(final Preview.PreviewOutput output) {
        listener.onCameraReady(
                output.getSurfaceTexture(),
                new Size(output.getTextureSize().getWidth(), output.getTextureSize().getHeight()),
                screenOrientationDegrees
        );
    }

    private void setupPreview() {
        PreviewConfig config = new PreviewConfig.Builder()
                // CameraX 的宽高比和 Camera1 相反, 为 3:4 9:16......
                .setTargetAspectRatio(new Rational(aspectRatio.getY(), aspectRatio.getX()))
                // 分辨率
                .setTargetResolution(new android.util.Size(previewWidth, previewHeight))
                // 前置与否
                .setLensFacing(facing == Constants.FACING_FRONT ? CameraX.LensFacing.FRONT
                        : CameraX.LensFacing.BACK)
                .build();
        mPreview = new Preview(config);
        mPreview.setOnPreviewOutputUpdateListener(this);
    }


}