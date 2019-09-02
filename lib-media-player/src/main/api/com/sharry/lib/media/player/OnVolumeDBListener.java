package com.sharry.lib.media.player;

import androidx.annotation.UiThread;

/**
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-06-26
 */
public interface OnVolumeDBListener {

    /**
     * 分贝回调
     */
    @UiThread
    void onVolumeDBChanged(int db);

}
