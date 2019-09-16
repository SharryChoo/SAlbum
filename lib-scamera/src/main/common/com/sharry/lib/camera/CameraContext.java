package com.sharry.lib.camera;

import android.content.Context;
import android.content.ContextWrapper;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-08-07
 */
class CameraContext extends ContextWrapper {

    private AspectRatio aspectRatio = AspectRatio.DEFAULT;
    private int facing;
    private boolean autoFocus;
    private int flashMode;
    private int screenOrientationDegrees;
    boolean adjustViewBounds;
    Size desiredSize;

    CameraContext(Context base) {
        super(base);
    }

    AspectRatio getAspectRatio() {
        return aspectRatio;
    }

    void setAspectRatio(AspectRatio aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    int getFacing() {
        return facing;
    }

    void setFacing(int facing) {
        this.facing = facing;
    }

    boolean isAutoFocus() {
        return autoFocus;
    }

    void setAutoFocus(boolean autoFocus) {
        this.autoFocus = autoFocus;
    }

    int getFlashMode() {
        return flashMode;
    }

    void setFlashMode(int flashMode) {
        this.flashMode = flashMode;
    }

    int getScreenOrientationDegrees() {
        return screenOrientationDegrees;
    }

    void setScreenOrientationDegrees(int screenOrientationDegrees) {
        this.screenOrientationDegrees = screenOrientationDegrees;
    }

    boolean isAdjustViewBounds() {
        return adjustViewBounds;
    }

    public void setAdjustViewBounds(boolean adjustViewBounds) {
        this.adjustViewBounds = adjustViewBounds;
    }

    public boolean getAdjustViewBounds() {
        return adjustViewBounds;
    }

    public void setDesiredSize(Size desiredSize) {
        this.desiredSize = desiredSize;
    }

    public Size getDesiredSize() {
        return desiredSize;
    }
}
