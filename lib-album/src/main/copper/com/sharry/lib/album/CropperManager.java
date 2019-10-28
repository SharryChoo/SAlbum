package com.sharry.lib.album;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

/**
 * 图片裁剪的入口
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:09 PM
 */
public class CropperManager {

    private static final String TAG = CropperManager.class.getSimpleName();
    private static String[] sPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static CropperManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new CropperManager(activity);
        } else {
            throw new IllegalArgumentException(TAG + ".with -> Context can not cast to Activity");
        }
    }

    private Activity mBind;
    private CropperConfig mConfig;

    private CropperManager(Activity activity) {
        this.mBind = activity;
    }

    /**
     * 设置配置属性
     */
    public CropperManager setConfig(@NonNull CropperConfig config) {
        this.mConfig = Preconditions.checkNotNull(config, "Please ensure config not null!");
        return this;
    }

    /**
     * 裁剪图片
     */
    public void crop(@NonNull final CropperCallback callback) {
        Preconditions.checkNotNull(callback, "Please ensure callback not null!");
        Preconditions.checkNotNull(mConfig, "Please ensure setConfig correct!");
        PermissionsHelper.with(mBind)
                .request(sPermissions)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) {
                            cropActual(callback);
                        }
                    }
                });
    }

    /**
     * 裁剪图片
     */
    private void cropActual(@NonNull final CropperCallback callback) {
        // 若未指定目的路径, 则在系统相册的路径下创建图片文件
        if (mConfig.getOriginUri() == null) {
            throw new UnsupportedOperationException(TAG + ".takeActual -> Please ensure crop " +
                    "target uri is valuable.");
        }
        // 指定默认的裁剪路径
        if (TextUtils.isEmpty(mConfig.getCropDirectoryPath())) {
            mConfig.rebuild().setCropDirectory(FileUtil.createDefaultDirectory(mBind).getAbsolutePath());
        }
        // 指定默认, FileProvider 的 authority
        if (TextUtils.isEmpty(mConfig.getAuthority())) {
            mConfig.rebuild().setAuthority(FileUtil.getDefaultFileProviderAuthority(mBind));
        }
        // 执行回调
        CropperFragment callbackFragment = CropperFragment.getInstance(mBind);
        if (callbackFragment == null) {
            Log.e(TAG, "Launch crop activity failed.");
            return;
        }
        callbackFragment.cropPicture(mConfig, callback);
    }

}
