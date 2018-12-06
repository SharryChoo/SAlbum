package com.sharry.picturepicker.support.permission;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限请求管理类
 * Thanks RxPermissions
 *
 * @author Sharry <a href="SharryChooCHN@Gmail.com">Contact me.</a>
 * @version 1.0
 * @since 2018/1/5 16:23
 */
/*@hind*/
public class PermissionsManager {

    public static final String TAG = PermissionsManager.class.getSimpleName();
    private PermissionsFragment mPermissionsFragment;
    private String[] mPermissions;

    public static PermissionsManager getManager(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new PermissionsManager(activity);
        } else {
            throw new IllegalArgumentException("PermissionsManager.getManager -> Context can not cast to Activity");
        }
    }

    private PermissionsManager(Activity activity) {
        mPermissionsFragment = getPermissionsFragment(activity);
    }

    /**
     * 添加需要请求的权限
     */
    public PermissionsManager request(String... permissions) {
        return requestArray(permissions);
    }

    /**
     * 添加需要请求的权限(Kotlin 不支持从不定长参数转为 Array)
     */
    public PermissionsManager requestArray(String[] permissions) {
        ensure(permissions);
        mPermissions = permissions;
        return this;
    }

    /**
     * 执行权限请求
     */
    public void execute(PermissionsCallback permissionsCallback) {
        if (permissionsCallback == null) {
            throw new IllegalArgumentException("PermissionsManager.execute -> PermissionsCallback must not be null");
        }
        requestImplementation(mPermissions, permissionsCallback);
    }

    /**
     * 判断权限是否被授权
     */
    public boolean isGranted(String permission) {
        return !isMarshmallow() || mPermissionsFragment.isGranted(permission);
    }

    /**
     * 判断权限是否被撤回
     * <p>
     * Always false if SDK &lt; 23.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isRevoked(String permission) {
        return isMarshmallow() && mPermissionsFragment.isRevoked(permission);
    }

    /**
     * 获取 PermissionsFragment
     */
    private PermissionsFragment getPermissionsFragment(Activity activity) {
        PermissionsFragment permissionsFragment = findPermissionsFragment(activity);
        if (permissionsFragment == null) {
            permissionsFragment = PermissionsFragment.getInstance();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction().add(permissionsFragment, TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return permissionsFragment;
    }

    /**
     * 在 Activity 中通过 TAG 去寻找我们添加的 Fragment
     */
    private PermissionsFragment findPermissionsFragment(Activity activity) {
        return (PermissionsFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }


    /**
     * 验证发起请求的权限是否有效
     */
    private void ensure(String[] permissions) {
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("PermissionsManager.request -> requestEach requires at least one input permission");
        }
    }

    /**
     * 执行权限请求
     *
     * @param permissions
     * @param callback
     */
    private void requestImplementation(String[] permissions, PermissionsCallback callback) {
        List<String> unrequestedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            mPermissionsFragment.log("Requesting permission -> " + permission);
            if (isGranted(permission)) {
                // Already granted, or not Android M
                // Return a granted Permission object.
                continue;
            }
            if (isRevoked(permission)) {
                // Revoked by a policy, return a denied Permission object.
                continue;
            }
            unrequestedPermissions.add(permission);
        }
        if (!unrequestedPermissions.isEmpty()) {
            // 细节, toArray的时候指定了数组的长度
            String[] unrequestedPermissionsArray = unrequestedPermissions.toArray(
                    new String[unrequestedPermissions.size()]);
            mPermissionsFragment.requestPermissions(unrequestedPermissionsArray, callback);
        } else {
            callback.onResult(true);
        }
    }

    private boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
