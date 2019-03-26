package com.sharry.picturepicker.crop;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.sharry.picturepicker.support.permission.PermissionsCallback;
import com.sharry.picturepicker.support.permission.PermissionsManager;
import com.sharry.picturepicker.support.utils.FileUtil;

/**
 * Created by think on 2018/6/20.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 从相机拍照获取图片的 Manager
 */
public class PictureCropManager {

    private static final String TAG = PictureCropManager.class.getSimpleName();

    public static PictureCropManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new PictureCropManager(activity);
        } else {
            throw new IllegalArgumentException(TAG + ".with -> Context can not cast to Activity");
        }
    }

    private String[] mPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private Activity mActivity;
    private PictureCropFragment mCropFragment;
    private CropConfig mConfig;

    private PictureCropManager(@NonNull Activity activity) {
        this.mActivity = activity;
        this.mCropFragment = getCallbackFragment(activity);
    }

    /**
     * 设置配置属性
     */
    public PictureCropManager setConfig(@NonNull CropConfig config) {
        this.mConfig = config;
        return this;
    }

    /**
     * 裁剪图片
     */
    public void crop(@NonNull final CropCallback callback) {
        PermissionsManager.getManager(mActivity)
                .request(mPermissions)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) cropActual(callback);
                    }
                });
    }

    /**
     * 裁剪图片
     */
    private void cropActual(@NonNull final CropCallback callback) {
        verify();
        mCropFragment.cropPicture(mConfig, callback);
    }

    private void verify() {
        // 若未指定目的路径, 则在系统相册的路径下创建图片文件
        if (mConfig.getOriginFilePath() == null) {
            throw new UnsupportedOperationException(TAG + ".takeActual -> Please ensure crop " +
                    "target path is valuable.");
        }
        // 指定默认的裁剪路径
        if (TextUtils.isEmpty(mConfig.getCropDirectoryPath())) {
            mConfig.rebuild().setCropDirectory(FileUtil.createDefaultDirectory(mActivity).getAbsolutePath());
        }
        // 指定默认, FileProvider 的 authority
        if (TextUtils.isEmpty(mConfig.getAuthority())) {
            mConfig.rebuild().setFileProviderAuthority(FileUtil.getDefaultFileProviderAuthority(mActivity));
        }
    }

    /**
     * 获取用于回调的 Fragment
     */
    private PictureCropFragment getCallbackFragment(Activity activity) {
        PictureCropFragment callbackFragment = findCallbackFragment(activity);
        if (callbackFragment == null) {
            callbackFragment = PictureCropFragment.newInstance();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction().add(callbackFragment, TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return callbackFragment;
    }

    /**
     * 在 Activity 中通过 TAG 去寻找我们添加的 Fragment
     */
    private PictureCropFragment findCallbackFragment(Activity activity) {
        return (PictureCropFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }
}
