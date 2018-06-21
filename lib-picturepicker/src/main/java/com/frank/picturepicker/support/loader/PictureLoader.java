package com.frank.picturepicker.support.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * Created by Frank on 2018/6/21.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 用于加载图片
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
        mPictureLoader.loadImage(context, uri, imageView);
    }

}
