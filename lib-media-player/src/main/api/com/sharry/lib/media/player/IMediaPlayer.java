package com.sharry.lib.media.player;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-21 10:34
 */
public interface IMediaPlayer {

    /**
     * 异步的准备
     */
    void prepare();

    /**
     * 播放音频
     * <p>
     * please invoke at {@link OnStatusChangedListener#onPrepared(int)}
     */
    void play();

    /**
     * 指定位置播放
     *
     * @param second second
     */
    void seek(int second);

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 恢复播放
     */
    void resume();

    /**
     * 暂停播放
     */
    void stop();

    /**
     * 播放下一个资源
     */
    void next(@NonNull String nextDataSource);

    /**
     * 设置数据源
     */
    void setDataSource(@NonNull String dataSource);

    /**
     * 设置音频播放音量的大小
     *
     * @param percent [0, 100]
     */
    void setVolume(@IntRange(from = 0, to = 100) int percent);

    int CHANNEL_LAYOUT_RIGHT = 0;
    int CHANNEL_LAYOUT_LEFT = 1;
    int CHANNEL_LAYOUT_STEREO = 2;

    @IntDef(value = {CHANNEL_LAYOUT_RIGHT, CHANNEL_LAYOUT_LEFT, CHANNEL_LAYOUT_STEREO})
    @interface ChannelLayout {
    }

    /**
     * 设置音频的声道
     */
    void setChannelLayout(@ChannelLayout int channelLayout);

    /**
     * 设置声调
     *
     * @param pitch base is 1.0
     */
    void setPitch(float pitch);

    /**
     * 设置播放速度
     *
     * @param tempo base is 1.0
     */
    void setTempo(float tempo);

    /**
     * 状态变更回调
     */
    void setOnStatusChangedListener(@Nullable OnStatusChangedListener statusChangedListener);

    /**
     * 设置音量的分贝添加
     */
    void setOnVolumeDBListener(@Nullable OnVolumeDBListener volumeDBListener);

    /**
     * 设置错误回调
     */
    void setOnErrorListener(@Nullable OnErrorListener errorListener);

    /**
     * 视频 YUV 数据渲染回调
     */
    void setOnRenderYUVListener(@Nullable OnRenderYUVListener renderYUVListener);

    int getChannelLayout();

    int getVolumePercent();

    int getCurDuration();

    int getTotalDuration();

}
