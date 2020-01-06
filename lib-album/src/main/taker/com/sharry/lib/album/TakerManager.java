package com.sharry.lib.album;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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
    public void take(@NonNull final TakerCallbackLambda callbackLambda) {
        take(new TakerCallback() {
            @Override
            public void onCameraTakeComplete(@NonNull MediaMeta newMeta) {
                callbackLambda.onCameraTake(newMeta);
            }

            @Override
            public void onTakeFailed() {
                callbackLambda.onCameraTake(null);
            }
        });
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
        // 获取回调的 Fragment
        CallbackFragment callbackFragment = CallbackFragment.getInstance(mBind);
        if (callbackFragment == null) {
            callback.onTakeFailed();
            return;
        }
        callbackFragment.setCallback(new CallbackFragment.Callback() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (resultCode != RESULT_OK || null == data) {
                    callback.onTakeFailed();
                    return;
                }
                switch (requestCode) {
                    case TakerActivity.REQUEST_CODE:
                        MediaMeta mediaMeta = data.getParcelableExtra(TakerActivity.RESULT_EXTRA_MEDIA_META);
                        if (mediaMeta == null) {
                            callback.onTakeFailed();
                            return;
                        }
                        // 2. 处理图片裁剪
                        if (mConfig.getCropConfig() != null && mediaMeta.isPicture) {
                            performCropPicture(mediaMeta, callback);
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

    /**
     * 处理裁剪
     */
    private void performCropPicture(MediaMeta mediaMeta, final TakerCallback callback) {
        CropperManager.with(mBind)
                .setConfig(
                        mConfig.getCropConfig().rebuild()
                                // 需要裁剪的文件路径
                                .setOriginUri(mediaMeta.getContentUri())
                                .build()
                )
                .crop(new CropperCallback() {
                    @Override
                    public void onCropComplete(@NonNull MediaMeta meta) {
                        callback.onCameraTakeComplete(meta);
                    }

                    @Override
                    public void onCropFailed() {
                        callback.onTakeFailed();
                    }
                });
    }


}
