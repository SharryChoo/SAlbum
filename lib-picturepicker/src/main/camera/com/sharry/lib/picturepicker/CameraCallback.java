package com.sharry.lib.picturepicker;

import androidx.annotation.NonNull;

/**
 * 拍照回调
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-30 09:08
 */
public interface CameraCallback {

    /**
     * 拍照完成的回调
     *
     * @param path 照片输出路径
     */
    void onCameraTakeComplete(@NonNull String path);

}
