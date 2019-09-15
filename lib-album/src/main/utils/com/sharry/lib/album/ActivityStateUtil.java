package com.sharry.lib.album;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 2:43 PM
 */
class ActivityStateUtil {

    private static final String TAG = ActivityStateUtil.class.getSimpleName();

    static boolean isIllegalState(Activity activity) {
        if (activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
            Log.e(TAG, "Activity in error state.");
            return true;
        }
        return false;
    }

    static void fixRequestOrientation(Activity activity) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            try {
                Field field = Activity.class.getDeclaredField("mActivityInfo");
                field.setAccessible(true);
                ActivityInfo o = (ActivityInfo) field.get(activity);
                o.screenOrientation = -1;
                field.setAccessible(false);
            } catch (Throwable e) {
                // ignore.
            }
        }
    }

}
