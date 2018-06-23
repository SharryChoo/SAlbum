package com.frank.picturepicker.support.manager.crop;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.frank.picturepicker.support.callback.CropCallback;
import com.frank.picturepicker.support.callback.PermissionsCallback;
import com.frank.picturepicker.support.config.CropConfig;
import com.frank.picturepicker.support.manager.permission.PermissionsManager;

import java.io.File;
import java.util.Date;

/**
 * Created by think on 2018/6/20.
 * Email: frankchoochina@gmail.com
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
        this.mConfig = new CropConfig();
    }

    /**
     * 设置是否为圆形裁剪区域
     */
    public PictureCropManager setCropCircle(boolean isCropCircle) {
        this.mConfig.isCropCircle = isCropCircle;
        return this;
    }

    /**
     * 设置裁剪的尺寸
     */
    public PictureCropManager setDesireSize(int width, int height) {
        this.mConfig.outputX = width;
        this.mConfig.outputY = height;
        return this;
    }

    /**
     * 设置裁剪的比例
     */
    public PictureCropManager setAspectSize(int x, int y) {
        this.mConfig.aspectX = x;
        this.mConfig.aspectY = y;
        return this;
    }

    /**
     * 设置 FileProvider 的路径, 7.0 以后用于查找 URI
     */
    public PictureCropManager setFileProviderAuthority(String authorities) {
        mConfig.authority = authorities;
        return this;
    }

    /**
     * 设置需要裁剪的文件地址
     */
    public PictureCropManager setOriginFilePath(@NonNull String filePath) {
        this.mConfig.originFilePath = filePath;
        return this;
    }

    /**
     * 设置需要裁剪的文件地址
     */
    public PictureCropManager setDestFilePath(@NonNull String filePath) {
        this.mConfig.destFilePath = filePath;
        return this;
    }

    /**
     * 设置裁剪后压缩的质量
     */
    public PictureCropManager setQuality(int quality) {
        mConfig.destQuality = quality;
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
        // 若未指定目的路径, 则在系统相册的路径下创建图片文件
        if (mConfig.originFilePath == null) {
            throw new UnsupportedOperationException(TAG + ".takeActual -> Please ensure crop " +
                    "target path is valuable.");
        }
        // 指定默认的裁剪路径
        if (TextUtils.isEmpty(mConfig.destFilePath)) {
            mConfig.destFilePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    new Date().getTime() + ".jpg").getAbsolutePath();
        }
        // 指定默认, FileProvider 的 authority
        if (TextUtils.isEmpty(mConfig.authority)) {
            mConfig.authority = mActivity.getPackageName() + ".FileProvider";
        }
        mCropFragment.cropPicture(mConfig, callback);
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
