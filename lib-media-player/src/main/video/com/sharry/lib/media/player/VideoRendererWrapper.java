package com.sharry.lib.media.player;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-22 10:45
 */
public abstract class VideoRendererWrapper implements IVideoRenderer {

    private final IVideoRenderer mImpl;

    public VideoRendererWrapper(IVideoRenderer base) {
        this.mImpl = base;
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
    public final void onDrawFrame() {
        mImpl.onDrawFrame();
        onDrawTexture(mImpl.getTextureId());
    }

    @Override
    public void onRenderYUV(int width, int height, byte[] y, byte[] u, byte[] v) {
        mImpl.onRenderYUV(width, height, y, u, v);
    }

    @Override
    public int getTextureId() {
        return mImpl.getTextureId();
    }

    @Override
    public void setScaleMode(@ScaleType int scaleType) {
        mImpl.setScaleMode(scaleType);
    }

    public abstract void onDrawTexture(int yuvTextureId);
}
