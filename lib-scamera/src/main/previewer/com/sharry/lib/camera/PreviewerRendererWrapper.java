package com.sharry.lib.camera;

import android.graphics.SurfaceTexture;

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
    public void onAttach() {
        mImpl.onAttach();
    }

    @Override
    public void onSizeChanged(int width, int height) {
        mImpl.onSizeChanged(width, height);
    }

    @Override
    public void onDraw() {
        mImpl.onDraw();
        onDrawTexture(mImpl.getPreviewerTextureId());
    }

    @Override
    public void onDetach() {
        mImpl.onDetach();
    }

    @Override
    public void setDataSource(SurfaceTexture dataSource) {
        mImpl.setDataSource(dataSource);
    }

    @Override
    public int getPreviewerTextureId() {
        return mImpl.getPreviewerTextureId();
    }

    @Override
    public void setRotate(int degrees) {
        mImpl.setRotate(degrees);
    }

    @Override
    public void setScaleType(ScaleType type, boolean landscape, Size dataSourceSize, Size viewSize) {
        mImpl.setScaleType(type, landscape, dataSourceSize, viewSize);
    }

    protected abstract void onDrawTexture(int textureId);

}
