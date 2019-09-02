package com.sharry.lib.media.recorder;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-07-15 17:33
 */
public interface IMediaRecorder {

    /**
     * 启动视频的录制
     */
    void start();

    /**
     * 暂停视频的录制
     */
    void pause();

    /**
     * 恢复录制
     */
    void resume();

    /**
     * 取消视频的录制
     */
    void cancel();

    /**
     * 完成视频的录制
     */
    void complete();

}
