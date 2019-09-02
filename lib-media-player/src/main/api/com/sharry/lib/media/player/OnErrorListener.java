package com.sharry.lib.media.player;

import androidx.annotation.UiThread;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-26
 */
public interface OnErrorListener {

    /**
     * 播放错误处理
     *
     * @param errorCode 错误码
     * @param errorMsg  错误信息
     */
    @UiThread
    void onError(int errorCode, String errorMsg);

}
