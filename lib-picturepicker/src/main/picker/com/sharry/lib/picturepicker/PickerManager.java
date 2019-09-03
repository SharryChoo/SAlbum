package com.sharry.lib.picturepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * 图片选择器的管理类
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:03 PM
 */
public class PickerManager {

    public static final String TAG = PickerManager.class.getSimpleName();
    private static String[] sRequirePermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static PickerManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new PickerManager(activity);
        } else {
            throw new IllegalArgumentException("PickerManager.with -> Context can not cast to Activity");
        }
    }

    private Activity mActivity;
    private PickerConfig mConfig;

    private PickerManager(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * 设置图片加载方案
     */
    public PickerManager setPictureLoader(@NonNull IPictureLoaderEngine loader) {
        Preconditions.checkNotNull(loader, "Please ensure IPictureLoaderEngine not null!");
        PictureLoader.setPictureLoader(loader);
        return this;
    }

    /**
     * 设置图片选择的配置
     */
    public PickerManager setPickerConfig(@NonNull PickerConfig config) {
        this.mConfig = Preconditions.checkNotNull(config, "Please ensure PickerConfig not null!");
        return this;
    }

    /**
     * 发起请求
     *
     * @param pickerCallback 图片选中的回调
     */
    public void start(@NonNull final PickerCallback pickerCallback) {
        Preconditions.checkNotNull(pickerCallback, "Please ensure PickerCallback not null!");
        Preconditions.checkNotNull(mConfig, "Please ensure U set PickerConfig correct!");
        PermissionsUtil.with(mActivity)
                .request(sRequirePermissions)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) {
                            startActual(pickerCallback);
                        }
                    }
                });
    }

    /**
     * 处理 PickerActivity 的启动
     */
    private void startActual(@NonNull final PickerCallback pickerCallback) {
        // 1. 若开启了裁剪, 则只能选中一张图片
        if (mConfig.isCropSupport()) {
            mConfig.rebuild()
                    .setThreshold(1)
                    .setPickedPictures(null)
                    .build();
        }
        // 2. 获取回调的 Fragment
        CallbackFragment callbackFragment = CallbackFragment.getInstance(mActivity);
        if (callbackFragment == null) {
            Log.e(TAG, "Start Picture picker activity failed.");
            return;
        }
        callbackFragment.setCallback(new CallbackFragment.Callback() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (resultCode != RESULT_OK || null == data) {
                    return;
                }
                switch (requestCode) {
                    case PickerActivity.REQUEST_CODE:
                        ArrayList<MediaMeta> metas = data.getParcelableArrayListExtra(
                                PickerActivity.RESULT_EXTRA_PICKED_PICTURES);
                        if (metas != null) {
                            pickerCallback.onPickedComplete(metas);
                        } else {
                            Log.e(TAG, "Picked path from PickerActivity is null.");
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        PickerActivity.launchActivityForResult(mActivity, callbackFragment, mConfig);
    }

}
