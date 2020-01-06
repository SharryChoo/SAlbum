package com.sharry.lib.album;

import androidx.annotation.NonNull;

/**
 * 图片裁剪的回调
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:07 PM
 */
public interface CropperCallback {

    /**
     * 裁剪完成的回调
     */
    void onCropComplete(@NonNull MediaMeta meta);

    void onCropFailed();

}
