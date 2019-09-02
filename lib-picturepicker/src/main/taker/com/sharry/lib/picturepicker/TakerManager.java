package com.sharry.lib.picturepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import static android.app.Activity.RESULT_OK;

/**
 * 从相机拍照获取图片的入口
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:02 PM
 */
public class TakerManager {

    private static final String TAG = TakerManager.class.getSimpleName();
    private static String[] sRequirePermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static TakerManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new TakerManager(activity);
        } else {
            throw new IllegalArgumentException("TakerManager.with -> Context can not cast to Activity");
        }
    }

    private Activity mBind;
    private TakerConfig mConfig;

    private TakerManager(Activity activity) {
        this.mBind = activity;
    }

    /**
     * 设置配置属性
     */
    public TakerManager setConfig(@NonNull TakerConfig config) {
        this.mConfig = Preconditions.checkNotNull(config, "Please ensure TakerConfig not null!");
        return this;
    }

    /**
     * 获取拍摄照片
     */
    public void take(@NonNull final TakerCallback callback) {
        Preconditions.checkNotNull(callback, "Please ensure callback not null!");
        Preconditions.checkNotNull(mConfig, "Please ensure U set TakerConfig correct!");
        PermissionsUtil.with(mBind)
                .request(sRequirePermissions)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) {
                            takeActual(callback);
                        }
                    }
                });
    }

    private void takeActual(TakerCallback callback) {
        completionConfig();
        // 2. 获取回调的 Fragment
        CallbackFragment callbackFragment = CallbackFragment.getInstance(mBind);
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
                        // TODO 获取拍照后的图片地址
                        break;
                    default:
                        break;
                }
            }
        });
        // TODO 启动拍照录像的 Activity
        TakerActivity.launch(mBind, mConfig);
    }

    private void completionConfig() {
        // 1. 若为指定照片默认输出路径, 给予指定默认的拍照路径
        if (TextUtils.isEmpty(mConfig.getCameraDirectoryPath())) {
            mConfig.rebuild().setCameraDirectory(FileUtil.createDefaultDirectory(mBind).getAbsolutePath());
        }
        // 2. 若未指定 FileProvider 的 authority, 则给予默认值
        if (TextUtils.isEmpty(mConfig.getAuthority())) {
            mConfig.rebuild().setFileProviderAuthority(FileUtil.getDefaultFileProviderAuthority(mBind));
        }
        // 3. 处理图片裁剪的缺省值
        if (mConfig.isCropSupport()) {
            // 给图片裁剪添加缺省的输出文件夹
            if (TextUtils.isEmpty(mConfig.getCropperConfig().getCropDirectoryPath())) {
                mConfig.getCropperConfig().rebuild()
                        .setCropDirectory(mConfig.getCameraDirectoryPath());
            }
            // 给图片裁剪配置缺省 authority.
            if (TextUtils.isEmpty(mConfig.getCropperConfig().getAuthority())) {
                mConfig.getCropperConfig().rebuild()
                        .setFileProviderAuthority(mConfig.getAuthority());
            }
        }
    }

}
