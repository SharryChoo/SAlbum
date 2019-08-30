package com.sharry.lib.picturepicker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.sharry.lib.picturepicker.permission.PermissionsCallback;
import com.sharry.lib.picturepicker.permission.PermissionsUtil;

/**
 * 图片裁剪的入口
 *
 * @author Sharry <a href="xiaoyu.zhu@1hai.cn">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 5:09 PM
 */
public class CropRequestManager {

    private static final String TAG = CropRequestManager.class.getSimpleName();
    private static String[] sPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static CropRequestManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new CropRequestManager(activity);
        } else {
            throw new IllegalArgumentException(TAG + ".with -> Context can not cast to Activity");
        }
    }

    private Activity mBind;
    private CropConfig mConfig;

    private CropRequestManager(Activity activity) {
        this.mBind = activity;
    }

    /**
     * 设置配置属性
     */
    public CropRequestManager setConfig(@NonNull CropConfig config) {
        this.mConfig = Preconditions.checkNotNull(config, "Please ensure config not null!");
        return this;
    }

    /**
     * 裁剪图片
     */
    public void crop(@NonNull final CropCallback callback) {
        Preconditions.checkNotNull(callback, "Please ensure callback not null!");
        Preconditions.checkNotNull(mConfig, "Please ensure setConfig correct!");
        PermissionsUtil.with(mBind)
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
    private void cropActual(@NonNull final CropCallback callback) {
        // 若未指定目的路径, 则在系统相册的路径下创建图片文件
        if (mConfig.getOriginFilePath() == null) {
            throw new UnsupportedOperationException(TAG + ".takeActual -> Please ensure crop " +
                    "target path is valuable.");
        }
        // 指定默认的裁剪路径
        if (TextUtils.isEmpty(mConfig.getCropDirectoryPath())) {
            mConfig.rebuild().setCropDirectory(FileUtil.createDefaultDirectory(mBind).getAbsolutePath());
        }
        // 指定默认, FileProvider 的 authority
        if (TextUtils.isEmpty(mConfig.getAuthority())) {
            mConfig.rebuild().setFileProviderAuthority(FileUtil.getDefaultFileProviderAuthority(mBind));
        }
        // 执行回调
        PictureCropFragment callbackFragment = PictureCropFragment.getInstance(mBind);
        if (callbackFragment == null) {
            Log.e(TAG, "Launch crop activity failed.");
            return;
        }
        callbackFragment.cropPicture(mConfig, callback);
    }

}
