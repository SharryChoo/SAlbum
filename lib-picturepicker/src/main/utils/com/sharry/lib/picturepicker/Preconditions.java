package com.sharry.lib.picturepicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.util.Collection;

/**
 * Contains common assertions.
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 3/29/2019 2:14 PM
 */
final class Preconditions {

    private Preconditions() {
        // Utility class.
    }

    public static void checkArgument(boolean expression, @NonNull String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    @NonNull
    static <T> T checkNotNull(@Nullable T arg) {
        return checkNotNull(arg, "Argument must not be null");
    }

    @NonNull
    static <T> T checkNotNull(@Nullable T arg, @NonNull String message) {
        if (arg == null) {
            throw new NullPointerException(message);
        }
        return arg;
    }

    @NonNull
    static String checkNotEmpty(@Nullable String string) {
        if (TextUtils.isEmpty(string)) {
            throw new IllegalArgumentException("Must not be null or empty");
        }
        return string;
    }

    @NonNull
    static <T extends Collection<Y>, Y> T checkNotEmpty(@NonNull T collection) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Must not be empty.");
        }
        return collection;
    }

}