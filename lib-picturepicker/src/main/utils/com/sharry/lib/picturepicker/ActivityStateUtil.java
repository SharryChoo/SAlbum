package com.sharry.lib.picturepicker;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 2:43 PM
 */
public class ActivityStateUtil {

    private static final String TAG = ActivityStateUtil.class.getSimpleName();

    public static boolean isIllegalState(Activity activity) {
        if (activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
            Log.e(TAG, "Activity in error state.");
            return true;
        }
        return false;
    }

}
