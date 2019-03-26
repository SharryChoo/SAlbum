package com.sharry.picturepicker.picker;

import java.util.ArrayList;

/**
 * Created by Sharry on 2018/6/13.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 图片选择器的回调
 */
public interface PickerCallback {
    void onPickedComplete(ArrayList<String> userPickedSet);
}
