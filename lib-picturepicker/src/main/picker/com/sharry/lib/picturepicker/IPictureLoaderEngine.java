package com.sharry.lib.picturepicker;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    void loadPicture(@NonNull Context context, @NonNull String uri, @NonNull ImageView imageView);

    /**
     * 加载 Gif 图
     */
    void loadGif(@NonNull Context context, @NonNull String uri, @NonNull ImageView imageView);

    /**
     * 加载视频第一帧
     *
     * @param uri           视频地址
     * @param thumbnailPath 视频缩略图
     */
    void loadVideo(@NonNull Context context, @NonNull String uri, @Nullable String thumbnailPath, @NonNull ImageView imageView);

}
