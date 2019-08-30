package com.sharry.lib.picturepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.sharry.lib.picturepicker.permission.PermissionsCallback;
import com.sharry.lib.picturepicker.permission.PermissionsUtil;

import java.util.ArrayList;

/**
 * 图片查看器的管理类
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 4:34 PM
 */
public class PictureWatcherManager {

    public static final String TAG = PictureWatcherManager.class.getSimpleName();
    private static String[] sPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static PictureWatcherManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new PictureWatcherManager(activity);
        } else {
            throw new IllegalArgumentException("PictureWatcherManager.with -> Context can not cast to Activity");
        }
    }

    private Activity mBind;
    private WatcherConfig mConfig;
    private View mTransitionView;

    private PictureWatcherManager(Activity activity) {
        this.mBind = activity;
    }

    /**
     * 设置共享元素
     */
    public PictureWatcherManager setSharedElement(@NonNull View transitionView) {
        mTransitionView = Preconditions.checkNotNull(transitionView, "Please ensure View not null!");
        return this;
    }

    /**
     * 设置图片预览的配置
     */
    public PictureWatcherManager setConfig(@NonNull WatcherConfig config) {
        this.mConfig = Preconditions.checkNotNull(config, "Please ensure WatcherConfig not null!");
        return this;
    }

    /**
     * 设置图片加载方案
     */
    public PictureWatcherManager setPictureLoader(@NonNull IPictureLoaderEngine loader) {
        PictureLoader.setPictureLoader(loader);
        return this;
    }

    /**
     * 调用图片查看器的方法
     */
    public void start() {
        startForResult(null);
    }

    /**
     * 调用图片查看器, 一般用于相册
     */
    public void startForResult(@Nullable final WatcherCallback callback) {
        Preconditions.checkNotNull(callback, "Please ensure WatcherCall not null!");
        Preconditions.checkNotNull(mConfig, "Please ensure U set WatcherConfig correct.");
        PermissionsUtil.with(mBind)
                .request(sPermissions)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) {
                            startForResultActual(callback);
                        }
                    }
                });
    }

    /**
     * 真正执行 Activity 的启动
     */
    private void startForResultActual(final WatcherCallback callback) {
        CallbackFragment callbackFragment = CallbackFragment.getInstance(mBind);
        if (callbackFragment == null) {
            Log.e(TAG, "launch picture watcher failed.");
            return;
        }
        callbackFragment.setCallback(new CallbackFragment.Callback() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (resultCode != Activity.RESULT_OK || null == data || null == callback) {
                    return;
                }
                switch (requestCode) {
                    case PictureWatcherActivity.REQUEST_CODE:
                        ArrayList<String> paths = data.getStringArrayListExtra(
                                PictureWatcherActivity.RESULT_EXTRA_PICKED_PICTURES);
                        boolean isEnsure = data.getBooleanExtra(
                                PictureWatcherActivity.RESULT_EXTRA_IS_PICKED_ENSURE, false);
                        callback.onWatcherPickedComplete(isEnsure, paths);
                        break;
                    default:
                        break;
                }
            }
        });
        PictureWatcherActivity.launchActivityForResult(mBind, callbackFragment, mConfig, mTransitionView);
    }

}
