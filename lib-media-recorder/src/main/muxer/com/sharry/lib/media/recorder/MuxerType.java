package com.sharry.lib.media.recorder;

import android.media.MediaMuxer;

/**
 * 混音器(描述视频的封装格式)
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 1/21/2019 5:13 PM
 */
public enum MuxerType {

    MP4(MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4, ".mp4");

    private int outputFormat;
    private String suffix;

    /**
     * 定义录音类型
     *
     * @param mime   编码格式
     * @param suffix 录音文件扩展名
     */
    MuxerType(int mime, String suffix) {
        this.outputFormat = mime;
        this.suffix = suffix;
    }

    public int getMIME() {
        return outputFormat;
    }

    public String getFileSuffix() {
        return suffix;
    }

}
