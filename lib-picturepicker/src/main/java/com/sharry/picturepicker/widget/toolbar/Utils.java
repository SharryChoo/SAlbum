package com.sharry.picturepicker.widget.toolbar;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.ViewGroup;

import java.util.Collection;

/**
 * @author Sharry <a href="frankchoochina@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/8/27 23:21
 */
class Utils {

    Utils() {
        throw new UnsupportedOperationException(this + " cannot be instantiated");
    }

    /**
     * 判断是否为 5.0 以上的系统
     *
     * @return if true is over Lollipop, false is below Lollipop.
     */
    static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * 判断 Map 是否为空
     */
    static boolean isNotEmpty(Collection collection) {
        return null != collection && collection.size() != 0;
    }

    /**
     * 是否为 LayoutParams 特殊的参数
     */
    static boolean isLayoutParamsSpecialValue(int paramsValue) {
        return ViewGroup.LayoutParams.MATCH_PARENT == paramsValue
                || ViewGroup.LayoutParams.WRAP_CONTENT == paramsValue;
    }

    /**
     * @param baseColor    需要进行透明的Color
     * @param alphaPercent 透明图(0-1)
     */
    static int alphaColor(int baseColor, float alphaPercent) {
        if (alphaPercent < 0) alphaPercent = 0f;
        if (alphaPercent > 1) alphaPercent = 1f;
        // 计算基础透明度
        int baseAlpha = (baseColor & 0xff000000) >>> 24;
        // 根基需求计算透明度
        int alpha = (int) (baseAlpha * alphaPercent);
        // 根基透明度拼接新的color
        return alpha << 24 | (baseColor & 0xffffff);
    }

    /**
     * Dip convert 2 pixel
     */
    static int dp2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * Pixel convert 2 dip
     */
    static int px2dp(Context context, float px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * Get action bar heightExcludePadding associated with the app.
     */
    static int getActionBarHeight(Context context) {
        TypedValue typedValue = new TypedValue();
        // 将属性解析到TypedValue中
        context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        return TypedValue.complexToDimensionPixelSize(typedValue.data,
                context.getResources().getDisplayMetrics());
    }

    /**
     * Get status bar heightExcludePadding associated with the app.
     */
    static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        return resourceId > 0 ? context.getResources()
                .getDimensionPixelSize(resourceId) : 0;
    }

}
