package com.frank.picturepicker.picturewatcher.manager;

import java.util.ArrayList;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 图片选择器的回调
 */
public interface WatcherCallback {
    void onWatcherPickedComplete(ArrayList<String> userPickedSet);
}
