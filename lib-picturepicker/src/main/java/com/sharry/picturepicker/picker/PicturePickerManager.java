package com.sharry.picturepicker.picker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sharry.picturepicker.support.fragment.CallbackFragment;
import com.sharry.picturepicker.support.loader.IPictureLoader;
import com.sharry.picturepicker.support.loader.PictureLoader;
import com.sharry.picturepicker.support.permission.PermissionsCallback;
import com.sharry.picturepicker.support.permission.PermissionsManager;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Sharry on 2018/6/13.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.1
 * Description: 图片选择器的管理类
 */
public class PicturePickerManager {

    public static final String TAG = PicturePickerManager.class.getSimpleName();

    public static PicturePickerManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new PicturePickerManager(activity);
        } else {
            throw new IllegalArgumentException("PicturePickerManager.with -> Context can not cast to Activity");
        }
    }

    private String[] mPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private Activity mActivity;
    private PickerConfig mConfig;

    private PicturePickerManager(@NonNull Activity activity) {
        this.mActivity = activity;
    }

    /**
     * 设置图片加载方案
     */
    public PicturePickerManager setPictureLoader(@NonNull IPictureLoader loader) {
        PictureLoader.setPictureLoader(loader);
        return this;
    }

    /**
     * 设置图片选择的配置
     */
    public PicturePickerManager setPickerConfig(PickerConfig config) {
        this.mConfig = config;
        return this;
    }

    /**
     * 发起请求
     *
     * @param pickerCallback 图片选中的回调
     */
    public void start(@NonNull final PickerCallback pickerCallback) {
        PermissionsManager.getManager(mActivity)
                .request(mPermissions)
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
        verify();
        CallbackFragment callbackFragment = CallbackFragment.getInstance(mActivity);
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
        PicturePickerActivity.startActivityForResult(mActivity, callbackFragment, mConfig);
    }

    private void verify() {
        // 1. 验证是否实现了图片加载器
        if (PictureLoader.getPictureLoader() == null) {
            throw new UnsupportedOperationException("PictureLoader.load -> please invoke setPictureLoader first");
        }
        // 2. 若开启了裁剪, 则只能选中一张图片
        if (mConfig.isCropSupport()) {
            mConfig.rebuild()
                    .setThreshold(1)
                    .setPickedPictures(null)
                    .build();
        }
    }

}
