package com.sharry.lib.media.player;

import androidx.annotation.UiThread;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-23 12:49
 */
public interface OnStatusChangedListener {

    class Adapter implements OnStatusChangedListener{

        @Override
        public void onPrepared(int totalDuration) {

        }

        @Override
        public void onProgress(int current, int total) {

        }

        @Override
        public void onLoading(boolean isLoading) {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onResumed() {

        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onStopped() {

        }
    }

    /**
     * 异步初始化成功的回调
     *
     * @param totalDuration 总时长, 单位(秒)
     */
    @UiThread
    void onPrepared(int totalDuration);

    /**
     * 音视频播放进度回调
     *
     * @param current 当前播放进度(秒)
     * @param total   总共的时长(秒)
     */
    @UiThread
    void onProgress(int current, int total);

    /**
     * 正在加载中
     */
    @UiThread
    void onLoading(boolean isLoading);

    /**
     * 被暂停了
     */
    @UiThread
    void onPaused();

    /**
     * 被恢复了
     */
    @UiThread
    void onResumed();

    /**
     * 播放完成
     */
    @UiThread
    void onCompleted();

    /**
     * 被停止了
     */
    @UiThread
    void onStopped();

}
