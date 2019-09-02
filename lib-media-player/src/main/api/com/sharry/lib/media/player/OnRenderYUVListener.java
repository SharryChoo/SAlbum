package com.sharry.lib.media.player;

import androidx.annotation.WorkerThread;

/**
 * 播放进度回调
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-26
 */
public interface OnRenderYUVListener {

    /**
     * 渲染 YUV
     */
    @WorkerThread
    void onRenderYUV(int width, int height, byte[] y, byte[] u, byte[] v);

}
