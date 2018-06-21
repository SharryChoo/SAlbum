package com.frank.picturepicker.support.loader;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Frank on 2018/6/21.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description:
 */
public interface IPictureLoader {
    void load(Context context, String uri, ImageView imageView);
}
