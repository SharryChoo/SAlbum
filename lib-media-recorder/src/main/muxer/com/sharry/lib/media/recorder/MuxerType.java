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

    MP4("video/mp4", ".mp4");

    private String mime;
    private String suffix;

    /**
     * 定义录音类型
     *
     * @param mime   编码格式
     * @param suffix 录音文件扩展名
     */
    MuxerType(String mime, String suffix) {
        this.mime = mime;
        this.suffix = suffix;
    }

    public String getMIME() {
        return mime;
    }

    public String getFileSuffix() {
        return suffix;
    }

}
