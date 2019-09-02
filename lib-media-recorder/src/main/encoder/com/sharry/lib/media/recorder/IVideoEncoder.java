package com.sharry.lib.media.recorder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.opengl.EGLContext;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 1/18/2019 5:40 PM
 */
public interface IVideoEncoder {

    /**
     * 编码前的准备工作
     * <p>
     * Subsequent calls to {@link #start} only the encoder prepare invoked.
     */
    void prepare(@NonNull Context context) throws IOException;

    void start();

    void pause();

    void resume();

    void stop();

    interface Callback {

        /**
         * 输出格式时回调
         */
        void onVideoFormatChanged(MediaFormat outputFormat);

        /**
         * 在编码过程中回调
         *
         * @param byteBuffer 编码后的数据帧
         * @param bufferInfo 数据帧的信息
         */
        void onVideoEncoded(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);
    }

    class Context {
        final int frameWidth;                           // 视频帧的宽度
        final int frameHeight;                          // 视频帧的高度
        final int frameRate;                            // 录制的帧率
        final int textureId;                            // Camera 的纹理 ID
        final EGLContext eglContext;                    // Camera 的 GL 上下文
        final Callback callback;          // 视频录制的回调

        public Context(int frameWidth, int frameHeight, int frameRate, int textureId,
                       EGLContext eglContext, Callback callback) {
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
            this.frameRate = frameRate;
            this.textureId = textureId;
            this.eglContext = eglContext;
            this.callback = callback;
        }
    }

}
