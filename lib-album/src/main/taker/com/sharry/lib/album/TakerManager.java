package com.sharry.lib.album;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import static android.app.Activity.RESULT_OK;

/**
 * 从相机拍照获取图片的入口
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:02 PM
 */
public class TakerManager {

    private static final String TAG = TakerManager.class.getSimpleName();
    private static String[] sRequirePermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
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
        PermissionsHelper.with(mBind)
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

    private void takeActual(final TakerCallback callback) {
        completionConfig();
        // 获取回调的 Fragment
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
                    case TakerActivity.REQUEST_CODE:
                        MediaMeta mediaMeta = data.getParcelableExtra(TakerActivity.RESULT_EXTRA_MEDIA_META);
                        // 2. 处理图片裁剪
                        if (mConfig.isCropSupport() && mediaMeta.isPicture) {
                            // 进行裁剪
                            performCropPicture(callback, mediaMeta);
                        } else {
                            // 3. 回调
                            callback.onCameraTakeComplete(mediaMeta);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        // 启动拍照录像的页面
        TakerActivity.launchForResult(callbackFragment, mConfig);
    }

    private void completionConfig() {
        // 1. 若为指定照片默认输出路径, 给予指定默认的拍照路径
        if (TextUtils.isEmpty(mConfig.getDirectoryPath())) {
            mConfig.rebuild().setDirectoryPath(FileUtil.createDefaultDirectory(mBind).getAbsolutePath());
        }
        // 3. 处理图片裁剪的缺省值
        if (mConfig.isCropSupport()) {
            // 给图片裁剪添加缺省的输出文件夹
            if (TextUtils.isEmpty(mConfig.getCropperConfig().getCropDirectoryPath())) {
                mConfig.getCropperConfig().rebuild()
                        .setCropDirectory(mConfig.getDirectoryPath());
            }
        }
    }

    private void performCropPicture(final TakerCallback callback, MediaMeta mediaMeta) {
        CropperManager.with(mBind)
                .setConfig(
                        mConfig.getCropperConfig().rebuild()
                                // 需要裁剪的文件路径
                                .setOriginFile(mediaMeta.contentUri)
                                .build()
                )
                .crop(new CropperCallback() {
                    @Override
                    public void onCropComplete(@NonNull Uri path) {
                        callback.onCameraTakeComplete(MediaMeta.create(path, true));
                    }
                });
    }

}
