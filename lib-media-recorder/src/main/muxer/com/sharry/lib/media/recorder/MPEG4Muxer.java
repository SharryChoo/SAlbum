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

    private MediaMuxer mImpl;

    /**
     * SVideoPlayer track index associated with this mixer
     * <p>
     * init when {@link #addVideoTrack(MediaFormat)} invoked.
     */
    private int mVideoTrackIndex = -1;

    /**
     * SAudioPlayer track index associated with this mixer
     * <p>
     * init when {@link #addAudioTrack(MediaFormat)} invoked.
     */
    private int mAudioTrackIndex = -1;

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
        if (mAudioTrackIndex != -1) {
            Log.i(TAG, "SAudioPlayer track already added.");
        } else {
            mAudioTrackIndex = mImpl.addTrack(audioFormat);
            Log.i(TAG, "SAudioPlayer track add successful.");
        }
        // 尝试启动混合器
        tryToLaunchMixer();
    }

    @Override
    public void addVideoTrack(@NonNull MediaFormat videoFormat) {
        if (isMixerStart) {
            Log.i(TAG, "All track already added.");
            return;
        }
        if (mVideoTrackIndex != -1) {
            Log.i(TAG, "SVideoPlayer track already added.");
        } else {
            mVideoTrackIndex = mImpl.addTrack(videoFormat);
            Log.i(TAG, "SVideoPlayer track add successful.");
        }
        // 尝试启动混合器
        tryToLaunchMixer();
    }

    @Override
    public void execute(@NonNull Parcel data) throws Throwable {
        if (!isMixerStart) {
            return;
        }
        int trackIndex;
        if (data.trackType == Parcel.TRACK_VIDEO) {
            Log.v(TAG, "Writing video, byte size is: " + data.bufferInfo.size
                    + ", pts is " + data.bufferInfo.presentationTimeUs / 1000 / 1000 + "s");
            trackIndex = mVideoTrackIndex;
        } else {
            trackIndex = mAudioTrackIndex;
            Log.v(TAG, "Writing audio, byte size is, " + data.bufferInfo.size
                    + ", pts is " + data.bufferInfo.presentationTimeUs / 1000 / 1000 + "s");
        }
        mImpl.writeSampleData(trackIndex, data.byteBuff, data.bufferInfo);
    }

    @Override
    public void release() {
        // 释放数据
        try {
            mImpl.stop();
        } catch (Throwable e) {
            // ignore.
        }
        try {
            mImpl.release();
        } catch (Throwable e) {
            // ignore.
        }
        mAudioTrackIndex = -1;
        mVideoTrackIndex = -1;
        isMixerStart = false;
    }

    /**
     * 请求混合器开始启动
     */
    private void tryToLaunchMixer() {
        if (isMixerStart) {
            Log.i(TAG, "Mixer already launched.");
            return;
        }
        if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1) {
            mImpl.start();
            isMixerStart = true;
            Log.i(TAG, "Mixer launch successful.");
        } else {
            Log.i(TAG, "Mixer launch failed, waiting all track added.");
        }
    }

}
