package com.sharry.picturepicker.utils.permission;

import android.support.annotation.NonNull;

/**
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2019/3/30 13:16
 */
public interface IPermissions {

    boolean isGranted(String permission);

    boolean isRevoked(String permission);

    void requestPermissions(@NonNull String[] permissions, PermissionsCallback callback);

}
