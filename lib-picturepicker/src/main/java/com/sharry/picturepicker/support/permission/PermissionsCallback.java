package com.sharry.picturepicker.support.permission;

/**
 * 权限请求的回调
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/1/5 16:22
 */
public interface PermissionsCallback {
    void onResult(boolean granted);
}
