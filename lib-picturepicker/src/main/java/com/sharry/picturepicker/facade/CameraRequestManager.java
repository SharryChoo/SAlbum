package com.sharry.picturepicker.facade;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.sharry.picturepicker.fragment.CameraRequestFragment;
import com.sharry.picturepicker.utils.permission.PermissionsCallback;
import com.sharry.picturepicker.utils.permission.PermissionsManager;
import com.sharry.picturepicker.utils.ActivityStateUtil;
import com.sharry.picturepicker.utils.FileUtil;

/**
 * Created by think on 2018/6/20.
 * Email: SharryChooCHN@Gmail.com
 * Version: 1.0
 * Description: 从相机拍照获取图片的 Manager
 */
public class CameraRequestManager {

    private static final String TAG = CameraRequestManager.class.getSimpleName();

    public static CameraRequestManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new CameraRequestManager(activity);
        } else {
            throw new IllegalArgumentException("CameraRequestManager.with -> Context can not cast to Activity");
        }
    }

    // 所需要的权限
    private String[] mPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private Activity mActivity;
    private CameraRequestFragment mCallbackFragment;
    private CameraConfig mConfig;

    private CameraRequestManager(@NonNull Activity activity) {
        this.mActivity = activity;
        this.mCallbackFragment = getCallbackFragment(activity);
    }

    /**
     * 设置配置属性
     */
    public CameraRequestManager setConfig(@NonNull CameraConfig config) {
        this.mConfig = config;
        return this;
    }

    /**
     * 获取拍摄照片
     */
    public void take(@NonNull final CameraCallback callback) {
        PermissionsManager.getManager(mActivity)
                .request(mPermissions)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) {
                            takeActual(callback);
                        }
                    }
                });
    }

    private void takeActual(CameraCallback callback) {
        // 1. 若为指定照片默认输出路径, 给予指定默认的拍照路径
        if (TextUtils.isEmpty(mConfig.getCameraDirectoryPath())) {
            mConfig.rebuild().setCameraDirectory(FileUtil.createDefaultDirectory(mActivity).getAbsolutePath());
        }
        // 2. 若未指定 FileProvider 的 authority, 则给予默认值
        if (TextUtils.isEmpty(mConfig.getAuthority())) {
            mConfig.rebuild().setFileProviderAuthority(FileUtil.getDefaultFileProviderAuthority(mActivity));
        }
        if (mConfig.isCropSupport()) {
            // 给图片裁剪添加缺省的输出文件夹
            if (TextUtils.isEmpty(mConfig.getCropConfig().getCropDirectoryPath())) {
                mConfig.getCropConfig().rebuild()
                        .setCropDirectory(mConfig.getCameraDirectoryPath());
            }
            // 给图片裁剪配置缺省 authority.
            if (TextUtils.isEmpty(mConfig.getCropConfig().getAuthority())) {
                mConfig.getCropConfig().rebuild()
                        .setFileProviderAuthority(mConfig.getAuthority());
            }
        }
        // 发起请求
        mCallbackFragment.takePicture(mConfig, callback);
    }

    /**
     * 获取用于回调的 Fragment
     */
    private CameraRequestFragment getCallbackFragment(Activity activity) {
        if (ActivityStateUtil.isIllegalState(activity)) {
            return null;
        }
        CameraRequestFragment callbackFragment = findCallbackFragment(activity);
        if (callbackFragment == null) {
            callbackFragment = CameraRequestFragment.newInstance();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction().add(callbackFragment, TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return callbackFragment;
    }

    /**
     * 在 Activity 中通过 TAG 去寻找我们添加的 Fragment
     */
    private CameraRequestFragment findCallbackFragment(Activity activity) {
        return (CameraRequestFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }

}
