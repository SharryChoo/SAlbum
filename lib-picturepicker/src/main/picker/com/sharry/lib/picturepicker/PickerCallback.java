package com.sharry.lib.picturepicker;

import java.util.ArrayList;

/**
 * 图片选择器的回调
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:03 PM
 */
public interface PickerCallback {

    void onPickedComplete(ArrayList<String> userPickedSet);

}
