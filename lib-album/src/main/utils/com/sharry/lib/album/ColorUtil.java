package com.sharry.lib.album;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;

/**
 * 处理颜色相关的工具类
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/22 17:39
 */
class ColorUtil {

    /**
     * Get ARGB color range in [primitiveColor, destColor]
     *
     * @param fraction   range[0, 1]
     * @param colorStart color associated with primitive changed
     * @param colorDest  color associated with dest changed
     */
    static int gradualChanged(float fraction, @ColorInt int colorStart, @ColorInt int colorDest) {
        // split start color.
        float startChannelA = ((colorStart >> 24) & 0xff) / 255.0f;
        float startChannelR = ((colorStart >> 16) & 0xff) / 255.0f;
        float startChannelG = ((colorStart >> 8) & 0xff) / 255.0f;
        float startChannelB = (colorStart & 0xff) / 255.0f;
        // convert from sRGB to linear
        startChannelR = (float) Math.pow(startChannelR, 2.2);
        startChannelG = (float) Math.pow(startChannelG, 2.2);
        startChannelB = (float) Math.pow(startChannelB, 2.2);

        // split dest color.
        float destChannelA = ((colorDest >> 24) & 0xff) / 255.0f;
        float destChannelR = ((colorDest >> 16) & 0xff) / 255.0f;
        float destChannelG = ((colorDest >> 8) & 0xff) / 255.0f;
        float destChannelB = (colorDest & 0xff) / 255.0f;
        destChannelR = (float) Math.pow(destChannelR, 2.2);
        destChannelG = (float) Math.pow(destChannelG, 2.2);
        destChannelB = (float) Math.pow(destChannelB, 2.2);

        // compute the interpolated color in linear space
        float a = startChannelA + fraction * (destChannelA - startChannelA);
        float r = startChannelR + fraction * (destChannelR - startChannelR);
        float g = startChannelG + fraction * (destChannelG - startChannelG);
        float b = startChannelB + fraction * (destChannelB - startChannelB);
        // convert back to sRGB in the [0..255] range
        a = a * 255.0f;
        r = (float) Math.pow(r, 1.0 / 2.2) * 255.0f;
        g = (float) Math.pow(g, 1.0 / 2.2) * 255.0f;
        b = (float) Math.pow(b, 1.0 / 2.2) * 255.0f;

        return Math.round(a) << 24 | Math.round(r) << 16 | Math.round(g) << 8 | Math.round(b);
    }

    /**
     * 颜色透明化
     *
     * @param baseColor     需要更改的颜色
     * @param alphaPercent: 0 代表全透明, 1 代表不透明
     */
    static int alphaColor(int baseColor, @FloatRange(from = 0f, to = 1f) float alphaPercent) {
        if (alphaPercent > 1) {
            alphaPercent = 1;
        }
        if (alphaPercent < 0) {
            alphaPercent = 0;
        }
        int baseAlpha = (baseColor & 0xff000000) >>> 24;
        int alpha = (int) (baseAlpha * alphaPercent);
        return alpha << 24 | (baseColor & 0xffffff);
    }

}
