package com.sharry.lib.album;

import androidx.annotation.Nullable;

/**
 * 拍照回调
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019-08-30 09:08
 */
public interface TakerCallbackLambda {

    /**
     * 拍照完成的回调
     *
     * @param newMeta null is failed, nonnull is success.
     */
    void onCameraTake(@Nullable MediaMeta newMeta);

}
