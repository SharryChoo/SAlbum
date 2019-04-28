package com.sharry.picturepicker.facade;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sharry.picturepicker.activity.PicturePickerActivity;
import com.sharry.picturepicker.fragment.CallbackFragment;
import com.sharry.picturepicker.utils.Preconditions;
import com.sharry.picturepicker.utils.permission.PermissionsCallback;
import com.sharry.picturepicker.utils.permission.PermissionsUtil;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * 图片选择器的管理类
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:03 PM
 */
public class PicturePickerManager {

    public static final String TAG = PicturePickerManager.class.getSimpleName();
    private static String[] sRequirePermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static PicturePickerManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new PicturePickerManager(activity);
        } else {
            throw new IllegalArgumentException("PicturePickerManager.with -> Context can not cast to Activity");
        }
    }

    private Activity mActivity;
    private PickerConfig mConfig;

    private PicturePickerManager(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * 设置图片加载方案
     */
    public PicturePickerManager setPictureLoader(@NonNull IPictureLoaderEngine loader) {
        Preconditions.checkNotNull(loader, "Please ensure IPictureLoaderEngine not null!");
        PictureLoader.setPictureLoader(loader);
        return this;
    }

    /**
     * 设置图片选择的配置
     */
    public PicturePickerManager setPickerConfig(@NonNull PickerConfig config) {
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
     * 处理 PicturePickerActivity 的启动
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
                    case PicturePickerActivity.REQUEST_CODE:
                        ArrayList<String> paths = data.getStringArrayListExtra(
                                PicturePickerActivity.RESULT_EXTRA_PICKED_PICTURES);
                        if (paths != null) {
                            pickerCallback.onPickedComplete(paths);
                        } else {
                            Log.e(TAG, "Picked path from PicturePickerActivity is null.");
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        PicturePickerActivity.launchActivityForResult(mActivity, callbackFragment, mConfig);
    }

}
