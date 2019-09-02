package com.sharry.lib.camera;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-08-05
 */
abstract class AbsCameraDevice implements ICameraDevice {

    final CameraContext context;
    OnCameraReadyListener listener;
    AspectRatio aspectRatio = AspectRatio.DEFAULT;
    int facing;
    boolean autoFocus;
    int flashMode;
    int screenOrientationDegrees;
    int previewWidth, previewHeight;

    AbsCameraDevice(CameraContext context, OnCameraReadyListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void notifyFacingChanged() {
        if (this.facing == context.getFacing()) {
            return;
        }
        this.facing = context.getFacing();
        if (isCameraOpened()) {
            open();
        }
    }

    @Override
    public void notifyAspectRatioChanged() {
        // Handle this later when camera is opened
        if (!isCameraOpened()) {
            aspectRatio = context.getAspectRatio();
        }
        // if camera opened
        if (!aspectRatio.equals(context.getAspectRatio())) {
            aspectRatio = context.getAspectRatio();
            open();
        }
    }

    @Override
    public void notifyScreenOrientationChanged() {
        if (this.screenOrientationDegrees == context.getScreenOrientationDegrees()) {
            return;
        }
        this.screenOrientationDegrees = context.getScreenOrientationDegrees();
        if (isCameraOpened()) {
            open();
        }
    }

    @Override
    public void notifyDesiredSizeChanged() {
        if (previewWidth == context.getDesiredSize().getWidth()
                && previewHeight == context.getDesiredSize().getHeight()) {
            return;
        }
        previewWidth = context.getDesiredSize().getWidth();
        previewHeight = context.getDesiredSize().getHeight();
        if (isCameraOpened()) {
            open();
        }
    }

}
