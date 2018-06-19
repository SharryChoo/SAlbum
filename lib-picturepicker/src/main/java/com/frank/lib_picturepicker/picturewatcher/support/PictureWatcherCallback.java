package com.frank.lib_picturepicker.picturewatcher.support;

import java.util.ArrayList;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 图片选择器的回调
 */
public interface PictureWatcherCallback {
    void onResult(ArrayList<String> userPickedSet);
}
