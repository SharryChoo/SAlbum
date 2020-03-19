package com.sharry.lib.media.recorder;

import androidx.annotation.NonNull;

/**
 * 编码器 的 简单工厂
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 1/2/2019 4:24 PM
 */
class EncoderFactory {

    /**
     * 创建视频编码器
     */
    @NonNull
    static IVideoEncoder create(EncodeType.Video videoEncodeType) {
        IVideoEncoder result;
        switch (videoEncodeType) {
            case H264:
                result = new H264Encoder();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return result;
    }

    /**
     * 创建音频编码器
     */
    @NonNull
    static IAudioEncoder create(EncodeType.Audio type) {
        IAudioEncoder result;
        switch (type) {
            case AAC:
                result = new AACEncoder();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return result;
    }

}
