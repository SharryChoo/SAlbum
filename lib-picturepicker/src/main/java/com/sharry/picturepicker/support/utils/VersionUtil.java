package com.sharry.picturepicker.support.utils;

import android.os.Build;

/**
 * 版本控制相关的工具类
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/22 17:46
 */
public class VersionUtil {

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

}
