package com.frank.lib_picturepicker.picturepicker.support.camare;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.util.Date;

/**
 * Created by think on 2018/6/20.
 * Email: frankchoochina@gmail.com
 * Version: 1.0
 * Description: 从相机拍照获取图片的 Manager
 */
public class CameraTakeManager {

    private static final String TAG = CameraTakeManager.class.getSimpleName();

    public static CameraTakeManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new CameraTakeManager(activity);
        } else {
            throw new IllegalArgumentException("PicturePickerManager.with -> Context can not cast to Activity");
        }
    }

    private Activity mActivity;
    private CameraTakeFragment mTakePhotoFragment;
    private CameraTakeConfig mConfig;

    private CameraTakeManager(@NonNull Activity activity) {
        this.mActivity = activity;
        this.mTakePhotoFragment = getCallbackFragment(activity);
        this.mConfig = new CameraTakeConfig();
    }

    /**
     * 设置目的文件
     */
    public CameraTakeManager setDestFile(@NonNull String filePath) {
        this.mConfig.destFilePath = filePath;
        return this;
    }

    /**
     * 设置拍照后的压缩质量
     */
    public CameraTakeManager setDestQuality(int quality) {
        mConfig.destQuality = quality;
        return this;
    }

    /**
     * 设置 FileProvider 的路径, 7.0 以后用于查找 URI
     */
    public CameraTakeManager setFileProviderAuthority(String authorities) {
        mConfig.authority = authorities;
        return this;
    }

    /**
     * 获取拍摄照片
     */
    public void take(@NonNull final CameraTakeCallback callback) {
        mTakePhotoFragment.verifyPermission(new CameraTakeFragment.PermissionsCallback() {
            @Override
            public void onResult(boolean granted) {
                if (granted) takeActual(callback);
            }
        });
    }

    private void takeActual(CameraTakeCallback callback) {
        // 若未指定目的路径, 则在系统相册的路径下创建图片文件
        if (mConfig.destFilePath == null) {
            mConfig.destFilePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    new Date().getTime() + ".jpg").getAbsolutePath();
        }
        // 若未指定 FileProvider 的 authority, 则给予默认值
        if (TextUtils.isEmpty(mConfig.authority)) {
            mConfig.authority = mActivity.getPackageName() + ".FileProvider";
        }
        mTakePhotoFragment.takePhoto(mConfig, callback);
    }

    /**
     * 获取用于回调的 Fragment
     */
    private CameraTakeFragment getCallbackFragment(Activity activity) {
        CameraTakeFragment callbackFragment = findCallbackFragment(activity);
        if (callbackFragment == null) {
            callbackFragment = CameraTakeFragment.newInstance();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction().add(callbackFragment, TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return callbackFragment;
    }

    /**
     * 在 Activity 中通过 TAG 去寻找我们添加的 Fragment
     */
    private CameraTakeFragment findCallbackFragment(Activity activity) {
        return (CameraTakeFragment) activity.getFragmentManager().findFragmentByTag(TAG);
    }

}
