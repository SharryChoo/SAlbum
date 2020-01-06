package com.sharry.lib.album;

import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Watcher Picker callback.
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2020-01-06 16:58
 */
public interface WatcherCallbackLambda {

    void onWatcherPicked(@Nullable ArrayList<MediaMeta> pickedSet);

}
