package com.sharry.lib.album;

/**
 * Created by Sharry on 2018/6/13.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 图片选择器的回调
 */
public interface WatcherCallback {

    /**
     * The callback method will call when pick picture from watcher complete.
     *
     * @param isEnsure is clicked ensure button.
     */
    void onWatcherPickedComplete(boolean isEnsure);

}
