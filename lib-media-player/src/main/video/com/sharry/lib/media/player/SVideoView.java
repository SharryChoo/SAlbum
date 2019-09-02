package com.sharry.lib.media.player;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sharry.lib.opengles.GLTextureView;

/**
 * 基于 FFmpeg 的视频播放器
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-15
 */
public class SVideoView extends GLTextureView implements IMediaPlayer, OnRenderYUVListener {

    /**
     * 桥接实现
     */
    private final IMediaPlayer mImpl;
    protected IVideoRenderer mRenderer;

    public SVideoView(Context context) {
        this(context, null);
    }

    public SVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 初始化音视频引擎
        mImpl = new SMediaPlayer();
        mImpl.setOnRenderYUVListener(this);
        // 初始化 GL
        setRenderer(new DefaultVideoRenderer(context));
    }

    /**
     * 设置渲染器
     */
    public void setRenderer(@NonNull IVideoRenderer renderer) {
        super.setRenderer(renderer);
        this.mRenderer = renderer;
    }

    /**
     * 设置渲染时的缩放方式
     */
    public void setScaleType(@IVideoRenderer.ScaleType int scaleType) {
        mRenderer.setScaleMode(scaleType);
    }

    @Override
    public void onRenderYUV(int width, int height, byte[] y, byte[] u, byte[] v) {
        requestRenderer();
        mRenderer.onRenderYUV(width, height, y, u, v);
    }

    @Override
    public void prepare() {
        mImpl.prepare();
    }

    @Override
    public void play() {
        mImpl.play();
    }

    @Override
    public void seek(int second) {
        mImpl.seek(second);
    }

    @Override
    public void pause() {
        mImpl.pause();
    }

    @Override
    public void resume() {
        mImpl.resume();
    }

    @Override
    public void stop() {
        mImpl.stop();
    }

    @Override
    public void next(@NonNull String nextDataSource) {
        mImpl.next(nextDataSource);
    }

    @Override
    public void setDataSource(@NonNull String uri) {
        mImpl.setDataSource(uri);
    }

    @Override
    public void setVolume(@IntRange(from = 0, to = 100) int percent) {
        mImpl.setVolume(percent);
    }

    @Override
    public void setChannelLayout(@ChannelLayout int channelLayout) {
        mImpl.setChannelLayout(channelLayout);
    }

    @Override
    public void setPitch(float pitch) {
        mImpl.setPitch(pitch);
    }

    @Override
    public void setTempo(float tempo) {
        mImpl.setTempo(tempo);
    }

    @Override
    public void setOnStatusChangedListener(@Nullable OnStatusChangedListener statusChangedListener) {
        mImpl.setOnStatusChangedListener(statusChangedListener);
    }

    @Override
    public void setOnVolumeDBListener(@Nullable OnVolumeDBListener volumeDBListener) {
        mImpl.setOnVolumeDBListener(volumeDBListener);
    }

    @Override
    public void setOnErrorListener(@Nullable OnErrorListener errorListener) {
        mImpl.setOnErrorListener(errorListener);
    }

    @Override
    public void setOnRenderYUVListener(@Nullable OnRenderYUVListener renderYUVListener) {
        mImpl.setOnRenderYUVListener(renderYUVListener);
    }

    @Override
    public int getVolumePercent() {
        return mImpl.getVolumePercent();
    }

    @Override
    public int getChannelLayout() {
        return mImpl.getChannelLayout();
    }

    @Override
    public int getCurDuration() {
        return mImpl.getCurDuration();
    }

    @Override
    public int getTotalDuration() {
        return mImpl.getTotalDuration();
    }

}
