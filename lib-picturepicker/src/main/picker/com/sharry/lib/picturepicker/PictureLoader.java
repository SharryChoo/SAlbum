package com.sharry.lib.picturepicker;

import android.content.Context;
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
class PictureLoader {

    private static final String TAG = PictureLoader.class.getSimpleName();
    private static IPictureLoaderEngine sEngine;

    static void setPictureLoader(@NonNull IPictureLoaderEngine engine) {
        Preconditions.checkNotNull(engine, "Please ensure IPictureLoaderEngine not null!");
        sEngine = engine;
    }

    static IPictureLoaderEngine getPictureLoader() {
        return sEngine;
    }

    static void loadPicture(@NonNull Context context, @NonNull String uri, @NonNull ImageView imageView) {
        if (sEngine == null) {
            Log.e(TAG, "PictureLoader.loadPicture -> please invoke PictureLoader.setPictureLoader first");
            return;
        }
        sEngine.loadPicture(context, uri, imageView);
    }

    static void loadGif(@NonNull Context context, @NonNull String uri, @NonNull ImageView imageView) {
        if (sEngine == null) {
            Log.e(TAG, "PictureLoader.loadPicture -> please invoke PictureLoader.setPictureLoader first");
            return;
        }
        sEngine.loadGif(context, uri, imageView);
    }

    static void loadVideo(@NonNull Context context, @NonNull String uri, @Nullable String thumbnailPath, @NonNull ImageView imageView) {
        if (sEngine == null) {
            Log.e(TAG, "PictureLoader.loadPicture -> please invoke PictureLoader.setPictureLoader first");
            return;
        }
        sEngine.loadVideo(context, uri, thumbnailPath, imageView);
    }

}
