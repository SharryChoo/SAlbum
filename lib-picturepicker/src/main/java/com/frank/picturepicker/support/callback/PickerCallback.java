package com.frank.picturepicker.support.callback;

import java.util.ArrayList;

/**
 * Created by Frank on 2018/6/13.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 图片选择器的回调
 */
public interface PickerCallback {
    void onPickedComplete(ArrayList<String> userPickedSet);
}
