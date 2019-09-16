package com.sharry.lib.media.recorder;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 1/2/2019 4:24 PM
 */
class MuxerFactory {

    /**
     * 根据封装类型, 创建混合器
     */
    static IMuxer createEncoder(MuxerType muxerType) {
        IMuxer result;
        switch (muxerType) {
            case MP4:
                result = IMuxer.MPEG_4;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return result;
    }

}
