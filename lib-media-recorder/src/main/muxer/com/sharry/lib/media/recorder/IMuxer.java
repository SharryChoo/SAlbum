package com.sharry.lib.media.recorder;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.net.Uri;

import androidx.annotation.IntDef;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 1/18/2019 5:40 PM
 */
public interface IMuxer {

    IMuxer MPEG_4 = new MPEG4Muxer();


    /**
     * 编码前的准备工作
     * <p>
     * Subsequent calls to {@link #execute} only the encoder prepare invoked.
     */
    @MainThread
    void prepare(Context context, Uri uri) throws Throwable;

    /**
     * 添加视频轨
     */
    @MainThread
    void addVideoTrack(@NonNull MediaFormat videoFormat);

    /**
     * 添加音轨
     */
    @MainThread
    void addAudioTrack(@NonNull MediaFormat audioFormat);

    /**
     * 执行编码
     *
     * @param data 原生音频的数据源
     */
    @MainThread
    void execute(@NonNull Parcel data) throws Throwable;

    /**
     * 释放资源
     */
    void stop();


    /**
     * 混音器的元数据
     */
    class Parcel {

        static final int TRACK_VIDEO = 316;
        static final int TRACK_AUDIO = 748;

        @IntDef(flag = true, value = {
                TRACK_VIDEO,
                TRACK_AUDIO,
        })
        @interface TrackType {
        }

        static Parcel newInstance(@TrackType int trackType, ByteBuffer byteBuf,
                                  MediaCodec.BufferInfo bufferInfo) {
            return new Parcel(trackType, byteBuf, bufferInfo);
        }

        int trackType;
        ByteBuffer byteBuff;
        MediaCodec.BufferInfo bufferInfo;

        private Parcel(@TrackType int trackType, ByteBuffer byteBuff, MediaCodec.BufferInfo bufferInfo) {
            this.trackType = trackType;
            this.byteBuff = byteBuff;
            this.bufferInfo = bufferInfo;
        }

    }

}
