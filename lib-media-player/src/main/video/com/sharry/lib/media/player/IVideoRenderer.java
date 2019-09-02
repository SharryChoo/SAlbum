package com.sharry.lib.media.player;

import androidx.annotation.IntDef;

import com.sharry.lib.opengles.ITextureRenderer;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-08-22 10:41
 */
public interface IVideoRenderer extends ITextureRenderer, OnRenderYUVListener {

    int SCALE_TYPE_FIT_XY = 0;
    int SCALE_TYPE_CENTER_CROP = 1;
    int SCALE_TYPE_FIT_CENTER = 2;

    @IntDef(value = {
            SCALE_TYPE_FIT_XY,
            SCALE_TYPE_CENTER_CROP,
            SCALE_TYPE_FIT_CENTER
    })
    @interface ScaleType {
    }

    void setScaleMode(@ScaleType int scaleType);

    int getTextureId();


}
