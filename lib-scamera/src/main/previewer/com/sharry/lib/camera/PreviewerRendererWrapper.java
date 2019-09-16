package com.sharry.lib.camera;

import android.graphics.SurfaceTexture;

import androidx.annotation.NonNull;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-08-19
 */
public abstract class PreviewerRendererWrapper implements IPreviewer.Renderer {

    private IPreviewer.Renderer mImpl;

    public PreviewerRendererWrapper(IPreviewer.Renderer impl) {
        this.mImpl = impl;
    }

    @Override
    public void onEGLContextCreated() {
        mImpl.onEGLContextCreated();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mImpl.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame() {
        mImpl.onDrawFrame();
        onDrawTexture(mImpl.getTextureId());
    }

    @Override
    public void onDataSourceChanged(SurfaceTexture oesTexture) {
        mImpl.onDataSourceChanged(oesTexture);
    }

    @Override
    public int getTextureId() {
        return mImpl.getTextureId();
    }

    @Override
    public void resetMatrix() {
        mImpl.resetMatrix();
    }

    @Override
    public void rotate(int degrees) {
        mImpl.rotate(degrees);
    }

    @Override
    public void centerCrop(boolean isLandscape, Size surfaceSize, Size textureSize) {
        mImpl.centerCrop(isLandscape, surfaceSize, textureSize);
    }

    @Override
    public void applyMatrix() {
        mImpl.applyMatrix();
    }

    @NonNull
    @Override
    public float[] getMatrix() {
        return mImpl.getMatrix();
    }

    @Override
    public void setMatrix(@NonNull float[] matrix) {
        mImpl.setMatrix(matrix);
    }

    protected abstract void onDrawTexture(int textureId);

}
