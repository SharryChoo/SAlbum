package com.sharry.lib.album;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

/**
 * 图片加载的接口, 由外界实现图片加载的策略
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/18 16:18
 */
public interface ILoaderEngine {

    /**
     * 加载图片的实现
     */
    void loadPicture(@NonNull Context context, @NonNull MediaMeta mediaMeta, @NonNull ImageView imageView);

    /**
     * 加载 Gif 图
     */
    void loadGif(@NonNull Context context, @NonNull MediaMeta mediaMeta, @NonNull ImageView imageView);

    /**
     * 加载视频缩略图
     */
    void loadVideoThumbnails(@NonNull Context context, @NonNull MediaMeta mediaMeta, @NonNull ImageView imageView);

}
