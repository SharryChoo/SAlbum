package com.sharry.picturepicker.support.loader;

import android.content.Context;
import android.widget.ImageView;

/**
 * 图片加载的接口, 由外界实现图片加载的策略
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/18 16:18
 */
public interface IPictureLoader {
    void load(Context context, String uri, ImageView imageView);
}
