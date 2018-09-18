package com.sharry.picturepicker.support.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * PicturePicker 加载图片的工具类
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/6/21 16:19
 */
public class PictureLoader {

    private static IPictureLoader mPictureLoader;

    public static void setPictureLoader(@NonNull IPictureLoader loader) {
        mPictureLoader = loader;
    }

    public static IPictureLoader getPictureLoader() {
        return mPictureLoader;
    }

    public static void load(Context context, String uri, ImageView imageView) {
        if (mPictureLoader == null) {
            throw new UnsupportedOperationException("PictureLoader.load -> please invoke setPictureLoader first");
        }
        mPictureLoader.load(context, uri, imageView);
    }

}
