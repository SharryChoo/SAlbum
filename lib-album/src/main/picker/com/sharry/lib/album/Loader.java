package com.sharry.lib.album;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * PicturePicker 加载图片的工具类
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/6/21 16:19
 */
final class Loader {

    private static final String TAG = Loader.class.getSimpleName();
    private static ILoaderEngine sEngine;

    static void setLoaderEngine(@Nullable ILoaderEngine engine) {
        if (engine != null) {
            sEngine = engine;
        }
    }

    static ILoaderEngine getPictureLoader() {
        return sEngine;
    }

    static void loadPicture(@NonNull Context context, @NonNull Uri uri, @NonNull ImageView imageView) {
        if (sEngine == null) {
            Log.e(TAG, "Loader.loadPicture -> please invoke Loader.setLoaderEngine first");
            return;
        }
        sEngine.loadPicture(context, uri, imageView);
    }

    static void loadGif(@NonNull Context context, @NonNull Uri uri, @NonNull ImageView imageView) {
        if (sEngine == null) {
            Log.e(TAG, "Loader.loadPicture -> please invoke Loader.setLoaderEngine first");
            return;
        }
        sEngine.loadGif(context, uri, imageView);
    }

    static void loadVideo(@NonNull Context context, @NonNull Uri uri, @Nullable String thumbnailPath, @NonNull ImageView imageView) {
        if (sEngine == null) {
            Log.e(TAG, "Loader.loadPicture -> please invoke Loader.setLoaderEngine first");
            return;
        }
        sEngine.loadVideoThumbnails(context, uri, thumbnailPath, imageView);
    }

}
