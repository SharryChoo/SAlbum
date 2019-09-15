package com.sharry.lib.album;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 2019-09-02 17:09
 */
class DateUtil {

    private static final SimpleDateFormat HMS_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
    private static final SimpleDateFormat MS_FORMAT = new SimpleDateFormat("mm:ss", Locale.CHINA);

    static {
        HMS_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        MS_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
    }

    static String format(long duration) {
        if (duration < 3600000) {
            return MS_FORMAT.format(duration);
        } else {
            return HMS_FORMAT.format(duration);
        }
    }

}
