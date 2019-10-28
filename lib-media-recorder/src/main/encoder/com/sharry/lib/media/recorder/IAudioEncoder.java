package com.sharry.lib.media.recorder;

import android.media.MediaCodec;
import android.media.MediaFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 1/18/2019 5:40 PM
 */
public interface IAudioEncoder {

    /**
     * 编码前的准备工作
     * <p>
     * Subsequent calls to {@link #encode} only the encoder prepare invoked.
     */
    void prepare(@NonNull Context context) throws Throwable;

    /**
     * 执行编码
     */
    void encode(@Nullable byte[] inputBytes) throws Throwable;

    /**
     * 停止编码
     */
    void stop();

    /**
     * 编码的回调
     */
    interface Callback {

        /**
         * 输出格式时回调
         */
        void onAudioFormatChanged(MediaFormat outputFormat);

        /**
         * 在编码过程中回调
         *
         * @param byteBuffer 编码后的数据帧
         * @param bufferInfo 数据帧的信息
         */
        void onAudioEncoded(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);
    }

    class Context {
        final int sampleRate;                           // 采样率
        final int channelCount;                         // 通道数
        final int perSampleSize;                        // 每个采样点的大小
        final boolean isJustEncode;                     // 只进行编码不写入文件
        final FileDescriptor outputFd;                        // 音频输出的文件
        final IAudioEncoder.Callback callback;          // 视频录制的回调

        public Context(int sampleRate, int channelCount, int perSampleSize, boolean isJustEncode,
                       FileDescriptor outputFd, Callback callback) {
            this.sampleRate = sampleRate;
            this.channelCount = channelCount;
            this.perSampleSize = perSampleSize;
            this.isJustEncode = isJustEncode;
            this.outputFd = outputFd;
            this.callback = callback;
        }
    }

}
