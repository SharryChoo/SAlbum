package com.sharry.picturepicker.facade;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.sharry.picturepicker.utils.Preconditions;

/**
 * PicturePicker 加载图片的工具类
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/6/21 16:19
 */
public class PictureLoader {

    private static final String TAG = PictureLoader.class.getSimpleName();
    private static IPictureLoaderEngine sEngine;

    static void setPictureLoader(@NonNull IPictureLoaderEngine engine) {
        Preconditions.checkNotNull(engine, "Please ensure IPictureLoaderEngine not null!");
        sEngine = engine;
    }

    public static IPictureLoaderEngine getPictureLoader() {
        return sEngine;
    }

    public static void load(@NonNull Context context, @NonNull String uri, @NonNull ImageView imageView) {
        if (sEngine == null) {
            Log.e(TAG, "PictureLoader.load -> please invoke PictureLoader.setPictureLoader first");
        }
        sEngine.load(context, uri, imageView);
    }

}
