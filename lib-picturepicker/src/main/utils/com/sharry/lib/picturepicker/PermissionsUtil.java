package com.sharry.lib.picturepicker;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

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
class PermissionsUtil {

    public static final String TAG = PermissionsUtil.class.getSimpleName();
    private PermissionsFragment mPermissionsFragment;
    private String[] mPermissions;

    public static PermissionsUtil with(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new PermissionsUtil(activity);
        } else {
            throw new IllegalArgumentException("PermissionsUtil.with -> Context can not cast to Activity");
        }
    }

    private PermissionsUtil(Activity activity) {
        mPermissionsFragment = getPermissionsFragment(activity);
    }

    /**
     * 添加需要请求的权限
     */
    PermissionsUtil request(String... permissions) {
        return requestArray(permissions);
    }

    /**
     * 添加需要请求的权限(Kotlin 不支持从不定长参数转为 Array)
     */
    PermissionsUtil requestArray(String[] permissions) {
        ensure(permissions);
        mPermissions = permissions;
        return this;
    }

    /**
     * 执行权限请求
     */
    void execute(PermissionsCallback permissionsCallback) {
        if (permissionsCallback == null) {
            throw new IllegalArgumentException("PermissionsUtil.execute -> PermissionsCallback must not be null");
        }
        executeActual(mPermissions, permissionsCallback);
    }

    /**
     * 判断权限是否被授权
     */
    boolean isGranted(String permission) {
        return !isMarshmallow() || (mPermissionsFragment != null && mPermissionsFragment.isGranted(permission));
    }

    /**
     * 判断权限是否被撤回
     * <p>
     * Always false if SDK &lt; 23.
     */
    @SuppressWarnings("WeakerAccess")
    boolean isRevoked(String permission) {
        return isMarshmallow() && (mPermissionsFragment != null && mPermissionsFragment.isRevoked(permission));
    }

    /**
     * 获取 PermissionsFragment
     */
    private PermissionsFragment getPermissionsFragment(Activity activity) {
        if (ActivityStateUtil.isIllegalState(activity)) {
            return null;
        }
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
            throw new IllegalArgumentException("PermissionsUtil.request -> requestEach requires at least one input permission");
        }
    }

    /**
     * 执行权限请求
     */
    private void executeActual(String[] permissions, PermissionsCallback callback) {
        if (mPermissionsFragment == null) {
            Log.e(TAG, "Request failed.");
            callback.onResult(false);
            return;
        }
        List<String> unrequestedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            Log.i(TAG, "Requesting permission -> " + permission);
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
