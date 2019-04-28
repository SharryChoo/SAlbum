package com.sharry.picturepicker.facade;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * 图片加载的接口, 由外界实现图片加载的策略
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/18 16:18
 */
public interface IPictureLoaderEngine {

    /**
     * 加载图片的实现
     */
    void load(@NonNull Context context, @NonNull String uri, @NonNull ImageView imageView);

}
