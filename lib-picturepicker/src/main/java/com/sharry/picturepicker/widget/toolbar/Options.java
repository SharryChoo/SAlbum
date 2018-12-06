package com.sharry.picturepicker.widget.toolbar;

import android.view.View;

/**
 * Options associated with <T extends View>
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/9/28 8:43
 */
public interface Options<T extends View> {

    /**
     * U can use this options to completion view
     */
    void completion(T view);

}
