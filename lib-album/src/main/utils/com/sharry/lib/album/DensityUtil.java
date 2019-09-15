package com.sharry.lib.album;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-09-05 09:57
 */
class DensityUtil {

    /**
     * Dip convert 2 pixel
     */
    static int dp2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

}
