package com.sharry.lib.media.player;

import androidx.annotation.UiThread;

/**
 * 音频播放时 PCM 音频帧信息的回调
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-26
 */
public interface OnPCMListener {

    @UiThread
    void onPCM(byte[] pcm);

}
