package com.sharry.picturepicker.watcher.manager;

import java.util.ArrayList;

/**
 * Created by Sharry on 2018/6/13.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 图片选择器的回调
 */
public interface WatcherCallback {
    void onWatcherPickedComplete(boolean isEnsure, ArrayList<String> userPickedSet);
}
