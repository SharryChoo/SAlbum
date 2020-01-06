package com.sharry.lib.album;

import androidx.annotation.Nullable;

/**
 * 图片裁剪的回调
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:07 PM
 */
public interface CropperCallbackLambda {

    void onCropped(@Nullable MediaMeta meta);

}
