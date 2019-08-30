package com.sharry.lib.picturepicker;

import androidx.annotation.NonNull;

/**
 * 图片裁剪的回调
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:07 PM
 */
public interface CropperCallback {

    /**
     * 裁剪完成的回调
     *
     * @param path 输出路径
     */
    void onCropComplete(@NonNull String path);

}
