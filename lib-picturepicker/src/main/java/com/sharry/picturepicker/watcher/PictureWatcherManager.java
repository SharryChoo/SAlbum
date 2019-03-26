package com.sharry.picturepicker.watcher;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.sharry.picturepicker.support.fragment.CallbackFragment;
import com.sharry.picturepicker.support.loader.IPictureLoader;
import com.sharry.picturepicker.support.loader.PictureLoader;
import com.sharry.picturepicker.support.permission.PermissionsCallback;
import com.sharry.picturepicker.support.permission.PermissionsManager;

import java.util.ArrayList;

/**
 * Created by Sharry on 2018/6/19.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 图片查看器的管理类
 */
public class PictureWatcherManager {

    public static final String TAG = PictureWatcherManager.class.getSimpleName();

    public static PictureWatcherManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new PictureWatcherManager(activity);
        } else {
            throw new IllegalArgumentException("PictureWatcherManager.with -> Context can not cast to Activity");
        }
    }

    private String[] mPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private Activity mActivity;
    private WatcherConfig mConfig;
    private View mTransitionView;

    private PictureWatcherManager(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * 设置共享元素
     */
    public PictureWatcherManager setSharedElement(View transitionView) {
        mTransitionView = transitionView;
        return this;
    }

    /**
     * 设置图片预览的配置
     */
    public PictureWatcherManager setConfig(@NonNull WatcherConfig config) {
        this.mConfig = config;
        return this;
    }

    /**
     * 设置图片加载方案
     */
    public PictureWatcherManager setPictureLoader(@NonNull IPictureLoader loader) {
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
        // 请求权限
        PermissionsManager.getManager(mActivity)
                .request(mPermissions)
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
    private void startForResultActual(@Nullable final WatcherCallback callback) {
        verify();
        CallbackFragment callbackFragment = CallbackFragment.getInstance(mActivity);
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
        PictureWatcherActivity.startActivityForResult(mActivity, callbackFragment, mConfig, mTransitionView);
    }

    /**
     * 验证 Activity 启动参数
     */
    private void verify() {
        if (PictureLoader.getPictureLoader() == null) {
            throw new UnsupportedOperationException("PictureLoader.load -> please invoke setPictureLoader first");
        }
    }

}
