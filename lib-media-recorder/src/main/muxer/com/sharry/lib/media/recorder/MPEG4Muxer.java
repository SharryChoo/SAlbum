package com.sharry.lib.media.recorder;

import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

/**
 * Mp4 音视频封装器
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 1/24/2019 3:34 PM
 */
class MPEG4Muxer implements IMuxer {

    private static final String TAG = MPEG4Muxer.class.getSimpleName();

    private static final int NO_INDEX = -1;

    private MediaMuxer mImpl;

    /**
     * SVideoPlayer track index associated with this mixer
     * <p>
     * init when {@link #addVideoTrack(MediaFormat)} invoked.
     */
    private int mVideoTrackIndex = NO_INDEX;

    /**
     * SAudioPlayer track index associated with this mixer
     * <p>
     * init when {@link #addAudioTrack(MediaFormat)} invoked.
     */
    private int mAudioTrackIndex = NO_INDEX;

    /**
     * Flags associated with mixer
     */
    private volatile boolean isMixerStart;

    @Override
    public void prepare(@NonNull File filePath) throws IOException {
        mImpl = new MediaMuxer(filePath.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }

    @Override
    public void addAudioTrack(@NonNull MediaFormat audioFormat) {
        if (isMixerStart) {
            Log.i(TAG, "All track already added.");
            return;
        }
        if (mAudioTrackIndex != NO_INDEX) {
            Log.i(TAG, "SAudioPlayer track already added.");
        } else {
            mAudioTrackIndex = mImpl.addTrack(audioFormat);
            Log.i(TAG, "SAudioPlayer track add successful.");
        }
        // 尝试启动混合器
        tryToLaunchMuxer();
    }

    @Override
    public void addVideoTrack(@NonNull MediaFormat videoFormat) {
        if (isMixerStart) {
            Log.i(TAG, "All track already added.");
            return;
        }
        if (mVideoTrackIndex != NO_INDEX) {
            Log.i(TAG, "SVideoPlayer track already added.");
        } else {
            mVideoTrackIndex = mImpl.addTrack(videoFormat);
            Log.i(TAG, "SVideoPlayer track add successful.");
        }
        // 尝试启动混合器
        tryToLaunchMuxer();
    }

    @Override
    public void execute(@NonNull Parcel data) throws Throwable {
        if (!isMixerStart) {
            return;
        }
        int trackIndex;
        if (data.trackType == Parcel.TRACK_VIDEO) {
            trackIndex = mVideoTrackIndex;
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Writing video, byte size is: " + data.bufferInfo.size
                        + ", pts is " + data.bufferInfo.presentationTimeUs / 1000 / 1000 + "s");
            }
        } else {
            trackIndex = mAudioTrackIndex;
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "Writing audio, byte size is, " + data.bufferInfo.size
                        + ", pts is " + data.bufferInfo.presentationTimeUs / 1000 / 1000 + "s");
            }
        }
        // 写入数据, 可能会产生异常
        mImpl.writeSampleData(trackIndex, data.byteBuff, data.bufferInfo);
    }

    @Override
    public void stop() {
        try {
            // 释放资源
            mImpl.stop();
        } catch (Throwable e) {
            Log.w(TAG, e.getMessage(), e);
        }
        try {
            mImpl.release();
        } catch (Throwable e) {
            Log.w(TAG, e.getMessage(), e);
        }
        mAudioTrackIndex = -1;
        mVideoTrackIndex = -1;
        isMixerStart = false;
        mImpl = null;
    }

    /**
     * 请求混合器开始启动
     */
    private void tryToLaunchMuxer() {
        if (isMixerStart) {
            Log.i(TAG, "Mixer already launched.");
            return;
        }
        if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1) {
            mImpl.start();
            isMixerStart = true;
            Log.i(TAG, "Mixer launch successful.");
        } else {
            Log.i(TAG, "Mixer waiting all track added.");
        }
    }

}
