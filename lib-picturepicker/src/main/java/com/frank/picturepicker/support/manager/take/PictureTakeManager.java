package com.frank.picturepicker.support.manager.take;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.frank.picturepicker.support.callback.TakeCallback;
import com.frank.picturepicker.support.config.TakeConfig;

import java.io.File;
import java.util.Date;

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
    public PictureTakeManager setDestFilePath(@NonNull String filePath) {
        this.mConfig.destFilePath = filePath;
        return this;
    }

    /**
     * 设置拍照后的压缩质量
     */
    public PictureTakeManager setDestQuality(int quality) {
        mConfig.destQuality = quality;
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
     * 获取拍摄照片
     */
    public void take(@NonNull final TakeCallback callback) {
        mTakePhotoFragment.verifyPermission(new PictureTakeFragment.PermissionsCallback() {
            @Override
            public void onResult(boolean granted) {
                if (granted) takeActual(callback);
            }
        });
    }

    private void takeActual(TakeCallback callback) {
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
