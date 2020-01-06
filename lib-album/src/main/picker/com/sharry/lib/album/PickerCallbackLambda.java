package com.sharry.lib.album;

import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * 图片选择器的回调
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:03 PM
 */
public interface PickerCallbackLambda {

    /**
     * 获取选中集合
     *
     * @param userPickedSet 用户选中的集合
     */
    void onPicked(@Nullable ArrayList<MediaMeta> userPickedSet);

}