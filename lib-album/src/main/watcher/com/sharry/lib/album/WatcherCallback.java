package com.sharry.lib.album;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Sharry on 2018/6/13.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 图片选择器的回调
 */
public interface WatcherCallback {

    WatcherCallback DEFAULT = new WatcherCallback() {

        @Override
        public void onWatcherPickedComplete(@NonNull ArrayList<MediaMeta> pickedSet) {

        }

        @Override
        public void onWatcherPickedFailed() {

        }
    };

    /**
     * The callback method will call when pick picture from watcher complete.
     */
    void onWatcherPickedComplete(@NonNull ArrayList<MediaMeta> pickedSet);

    /**
     * DO nothing at Watcher page.
     */
    void onWatcherPickedFailed();

}
