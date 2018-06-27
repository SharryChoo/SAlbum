package com.frank.picturepicker.picturetake.manager;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.frank.picturepicker.support.permission.PermissionsCallback;
import com.frank.picturepicker.support.permission.PermissionsManager;
import com.frank.picturepicker.support.util.Utils;

/**
 * Created by think on 2018/6/20.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 从相机拍照获取图片的 Manager
 */
public class PictureTakeManager {

    private static final String TAG = PictureTakeManager.class.getSimpleName();

    public static PictureTakeManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new PictureTakeManager(activity);
        } else {
            throw new IllegalArgumentException("PictureTakeManager.with -> Context can not cast to Activity");
        }
    }

    // 所需要的权限
    private String[] mPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private Activity mActivity;
    private PictureTakeFragment mTakePhotoFragment;
    private TakeConfig mConfig;

    private PictureTakeManager(@NonNull Activity activity) {
        this.mActivity = activity;
        this.mTakePhotoFragment = getCallbackFragment(activity);
        this.mConfig = new TakeConfig();
    }

    /**
     * 设置目的文件
     */
    public PictureTakeManager setCameraDestFilePath(@NonNull String filePath) {
        this.mConfig.cameraDestFilePath = filePath;
        return this;
    }

    /**
     * 设置拍照后的压缩质量
     */
    public PictureTakeManager setCameraDestQuality(int quality) {
        mConfig.cameraDestQuality = quality;
        return this;
    }

    /**
     * 设置 FileProvider 的路径, 7.0 以后用于查找 URI
     */
    public PictureTakeManager setFileProviderAuthority(String authorities) {
        mConfig.authority = authorities;
        return this;
    }

    /**
     * 设置 FileProvider 的路径, 7.0 以后用于查找 URI
     */
    public PictureTakeManager setCropSupport(boolean isCropSupport) {
        mConfig.isCropSupport = isCropSupport;
        return this;
    }

    /**
     * 裁剪的宽高的设置
     */
    public PictureTakeManager setCropSize(int width, int height) {
        mConfig.cropWidth = width;
        mConfig.cropHeight = height;
        return this;
    }

    /**
     * 设置是否为圆形裁剪区域
     */
    public PictureTakeManager setCropCircle(boolean isCropCircle) {
        mConfig.isCropCircle = isCropCircle;
        return this;
    }

    /**
     * 设置裁剪后的路径
     */
    public PictureTakeManager setCropDestFilePath(@NonNull String filePath) {
        mConfig.cropDestFilePath = filePath;
        return this;
    }

    /**
     * 设置拍照后的压缩质量
     */
    public PictureTakeManager setCropDestQuality(int quality) {
        mConfig.cropDestQuality = quality;
        return this;
    }

    /**
     * 获取拍摄照片
     */
    public void take(@NonNull final TakeCallback callback) {
        PermissionsManager.getManager(mActivity)
                .request(mPermissions)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) takeActual(callback);
                    }
                });
    }

    private void takeActual(TakeCallback callback) {
        // 指定默认的拍照路径
        if (TextUtils.isEmpty(mConfig.cameraDestFilePath)) {
            mConfig.cameraDestFilePath = Utils.createDefaultCameraDestFile(mActivity).getAbsolutePath();
        }
        // 若未指定 FileProvider 的 authority, 则给予默认值
        if (TextUtils.isEmpty(mConfig.authority)) {
            mConfig.authority = Utils.getDefaultFileProviderAuthority(mActivity);
        }
        mTakePhotoFragment.takePicture(mConfig, callback);
    }

    /**
     * 获取用于回调的 Fragment
     */
    private PictureTakeFragment getCallbackFragment(Activity activity) {
        PictureTakeFragment callbackFragment = findCallbackFragment(activity);
        if (callbackFragment == null) {
            callbackFragment = PictureTakeFragment.newInstance();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction().add(callbackFragment, TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return callbackFragment;
    }

    /**
     * 在 Activity 中通过 TAG 去寻找我们添加的 Fragment
     */
    private PictureTakeFragment findCallbackFragment(Activity activity) {
        return (PictureTakeFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }
}
